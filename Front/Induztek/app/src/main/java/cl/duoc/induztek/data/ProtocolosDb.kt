
package cl.duoc.induztek.data

import cl.duoc.induztek.model.Equipo
import cl.duoc.induztek.model.EstadoProtocolo
import cl.duoc.induztek.model.Protocolo
import cl.duoc.induztek.model.Rol

private const val COLUMNAS_PROTOCOLO =
    "p.id_protocolo,p.numero,p.id_visita,p.id_equipo,p.id_usuario_registra,p.fecha_hora_prueba," +
            "p.temperatura_c,p.humedad_pct,p.estado,p.observaciones,e.tag AS tag,t.nombre AS tipo," +
            "i.nombre AS instalacion,u.nombre AS tecnico"

private const val DESDE_PROTOCOLO =
    " FROM protocolo p JOIN equipo e ON e.id_equipo=p.id_equipo " +
            "JOIN tipo_equipo t ON t.id_tipo_equipo=e.id_tipo_equipo " +
            "JOIN visita v ON v.id_visita=p.id_visita JOIN instalacion i ON i.id_instalacion=v.id_instalacion " +
            "JOIN usuario u ON u.id_usuario=p.id_usuario_registra "

fun InduztekDbHelper.listarProtocolos(): List<Protocolo> {
    val protocolos = mutableListOf<Protocolo>()
    val registros = filas(
        "SELECT $COLUMNAS_PROTOCOLO $DESDE_PROTOCOLO ORDER BY p.fecha_hora_prueba DESC"
    )
    for (registro in registros) {
        protocolos.add(aProtocolo(registro))
    }
    return protocolos
}

fun InduztekDbHelper.listarProtocolosVisitaCliente(idEmpresa: Int): List<Protocolo> {
    val protocolos = mutableListOf<Protocolo>()
    val registros = filas(
        "SELECT $COLUMNAS_PROTOCOLO $DESDE_PROTOCOLO " +
                "WHERE i.id_usuario_cliente=? AND p.estado NOT IN ('BORRADOR','ANULADO') " +
                "ORDER BY p.fecha_hora_prueba DESC",
        listOf(idEmpresa)
    )
    for (registro in registros) {
        protocolos.add(aProtocolo(registro))
    }
    return protocolos
}

fun InduztekDbHelper.protocoloPorId(id: Int): Protocolo? {
    for (protocolo in listarProtocolos()) {
        if (protocolo.idProtocolo == id) {
            return protocolo
        }
    }
    return null
}

fun InduztekDbHelper.formalizadoPor(idProtocolo: Int): String {
    val pasoDelHistorial = fila(
        "SELECT u.nombre AS autor FROM historial_estado h JOIN usuario u ON u.id_usuario=h.id_usuario " +
                "WHERE h.id_protocolo=? AND h.estado_nuevo='FORMALIZADO' " +
                "ORDER BY h.id_historial DESC LIMIT 1",
        listOf(idProtocolo)
    )
    if (pasoDelHistorial != null) {
        val autor = pasoDelHistorial.texto("autor")
        if (autor.isNotBlank()) {
            return autor
        }
    }

    val ingenieroDeLaVisita = fila(
        "SELECT u.nombre AS autor FROM protocolo p JOIN visita v ON v.id_visita=p.id_visita " +
                "JOIN usuario u ON u.id_usuario=v.id_ingeniero_cargo WHERE p.id_protocolo=?",
        listOf(idProtocolo)
    )
    if (ingenieroDeLaVisita != null) {
        return ingenieroDeLaVisita.texto("autor")
    }
    return ""
}

fun InduztekDbHelper.asegurarProtocoloEditable(idEquipo: Int, idUsuario: Int): Protocolo {
    val equipo = equipoPorId(idEquipo)
    if (equipo == null) {
        error("El equipo no existe en los registros.")
    }
    val rol = rolDe(idUsuario)

    var idVisita = 0
    val visitaYaAbierta = visitaAbierta(equipo.idInstalacion)
    if (visitaYaAbierta == null) {
        idVisita = nuevaVisita(equipo, idUsuario, rol)
    } else {
        idVisita = visitaYaAbierta
    }

    val existente = fila(
        "SELECT id_protocolo, estado, id_usuario_registra FROM protocolo WHERE id_visita=? AND id_equipo=?",
        listOf(idVisita, idEquipo)
    )
    if (existente == null) {
        return nuevoProtocolo(idVisita, idEquipo, idUsuario)
    }

    val idProtocolo = existente.entero("id_protocolo")
    val estado = existente.texto("estado")
    val creador = existente.entero("id_usuario_registra")

    if (estado != "BORRADOR" && estado != "OBSERVADO") {
        var aviso = "El protocolo ${numeroDe(idProtocolo)} está anulado en esta visita."
        if (estado == "REGISTRADO") {
            aviso = "El equipo ya tiene el protocolo ${numeroDe(idProtocolo)} registrado en esta visita. " +
                    "Para corregirlo, el ingeniero debe devolverlo con observación."
        }
        if (estado == "FORMALIZADO") {
            aviso = "El protocolo ${numeroDe(idProtocolo)} está formalizado y no se modifica."
        }
        error(aviso)
    }

    if (rol == Rol.TECNICO && creador != idUsuario) {
        error("Este protocolo lo abrió otra persona. Un técnico solo puede editar los protocolos que creó.")
    }

    val editable = protocoloPorId(idProtocolo)
    if (editable == null) {
        error("El protocolo ${numeroDe(idProtocolo)} no se pudo leer.")
    }
    return editable
}

fun InduztekDbHelper.visitaAbierta(idInstalacion: Int): Int? {
    val encontrada = fila(
        "SELECT id_visita FROM visita WHERE id_instalacion=? AND fecha_hora_cierre IS NULL " +
                "ORDER BY id_visita DESC LIMIT 1",
        listOf(idInstalacion)
    )
    if (encontrada == null) {
        return null
    }
    return encontrada.enteroSiHay("id_visita")
}

private fun InduztekDbHelper.nuevaVisita(equipo: Equipo, idUsuario: Int, rol: Rol?): Int {
    val anio = fechaHoy().substring(0, 4)
    val cantidadVisitas = contar("SELECT COUNT(*) FROM visita") + 1
    val datos = mutableMapOf<String, Any?>(
        "numero_ot" to "OT-$anio-${1300 + cantidadVisitas}",
        "id_instalacion" to equipo.idInstalacion,
        "fecha" to fechaHoy(),
        "tipo_visita" to "SOLO_PRUEBAS"
    )
    if (rol == Rol.INGENIERO) {
        datos["id_ingeniero_cargo"] = idUsuario
    }
    val idVisita = insertar("visita", datos).toInt()
    encolarCambio(CAMBIO_VISITA, idVisita)
    return idVisita
}

private fun InduztekDbHelper.nuevoProtocolo(idVisita: Int, idEquipo: Int, idUsuario: Int): Protocolo {
    val idProtocoloNuevo = insertar(
        "protocolo",
        mapOf(
            "numero" to nuevoNumeroProtocolo(),
            "id_visita" to idVisita,
            "id_equipo" to idEquipo,
            "id_usuario_registra" to idUsuario,
            "fecha_hora_prueba" to fechaHoraAhora(),
            "estado" to "BORRADOR"
        )
    ).toInt()
    ejecutar(
        "INSERT INTO historial_estado(id_protocolo,estado_anterior,estado_nuevo,id_usuario,fecha_hora) VALUES(?,NULL,'BORRADOR',?,?)",
        listOf(idProtocoloNuevo, idUsuario, fechaHoraAhora())
    )
    encolarCambio(CAMBIO_PROTOCOLO, idProtocoloNuevo)

    val creado = protocoloPorId(idProtocoloNuevo)
    if (creado == null) {
        error("El protocolo nuevo no se pudo leer.")
    }
    return creado
}

private fun InduztekDbHelper.numeroDe(idProtocolo: Int): String {
    val registro = fila("SELECT numero FROM protocolo WHERE id_protocolo=?", listOf(idProtocolo))
    if (registro == null) {
        return ""
    }
    return registro.texto("numero")
}

private fun InduztekDbHelper.rolDe(idUsuario: Int): Rol? {
    val usuario = buscarUsuarioPorId(idUsuario)
    if (usuario == null) {
        return null
    }
    return usuario.rol
}

internal fun InduztekDbHelper.nuevoNumeroProtocolo(): String {
    val anio = fechaHoy().substring(0, 4)
    val registro = fila(
        "SELECT MAX(numero) AS ultimo FROM protocolo WHERE numero LIKE 'PT-$anio-%'"
    )

    var correlativo = 400
    if (registro != null) {
        val ultimo = registro.texto("ultimo")
        if (ultimo.isNotBlank()) {
            val numero = ultimo.substringAfterLast("-").toIntOrNull()
            if (numero != null) {
                correlativo = numero
            }
        }
    }
    return "PT-$anio-${correlativo + 1}"
}

fun InduztekDbHelper.cambiarEstado(
    idProtocolo: Int, nuevo: EstadoProtocolo, idUsuario: Int, motivo: String = ""
) {
    val registro = fila("SELECT estado FROM protocolo WHERE id_protocolo=?", listOf(idProtocolo))
    if (registro == null) {
        return
    }
    val actual = registro.texto("estado")

    var desde: EstadoProtocolo? = null
    for (estado in EstadoProtocolo.values()) {
        if (estado.codigo == actual) {
            desde = estado
        }
    }
    if (desde == null) {
        error("El estado guardado no existe.")
    }

    val problema = problemaDeCambio(desde, nuevo, rolDe(idUsuario))
    if (problema != null) {
        error(problema)
    }

    if (motivo.isBlank() && (nuevo == EstadoProtocolo.OBSERVADO || nuevo == EstadoProtocolo.ANULADO)) {
        error("El ${nuevo.nombre.lowercase()} requiere escribir un motivo.")
    }

    actualizar("protocolo", mapOf("estado" to nuevo.codigo), "id_protocolo=?", listOf(idProtocolo))
    ejecutar(
        "INSERT INTO historial_estado(id_protocolo,estado_anterior,estado_nuevo,id_usuario,fecha_hora,motivo) VALUES(?,?,?,?,?,?)",
        listOf(idProtocolo, actual, nuevo.codigo, idUsuario, fechaHoraAhora(), motivo)
    )
    val detalle = gsonCambios.toJson(
        DetalleEstado(estado = nuevo.codigo, idUsuario = idUsuario, motivo = motivo)
    )
    encolarCambio(CAMBIO_ESTADO, idProtocolo, detalle)
}

private fun problemaDeCambio(desde: EstadoProtocolo, hacia: EstadoProtocolo, rol: Rol?): String? {
    var esDeTerreno = false
    if (rol == Rol.TECNICO || rol == Rol.INGENIERO) {
        esDeTerreno = true
    }
    var esDeOficina = false
    if (rol == Rol.INGENIERO || rol == Rol.JEFE_TECNICO) {
        esDeOficina = true
    }

    if (desde == EstadoProtocolo.FORMALIZADO) {
        return "Un protocolo formalizado no se modifica ni se anula."
    }

    if (hacia == EstadoProtocolo.REGISTRADO &&
        (desde == EstadoProtocolo.BORRADOR || desde == EstadoProtocolo.OBSERVADO)
    ) {
        if (esDeTerreno) {
            return null
        }
        return "Solo un técnico o un ingeniero pueden registrar en terreno."
    }

    if (hacia == EstadoProtocolo.FORMALIZADO && desde == EstadoProtocolo.REGISTRADO) {
        if (esDeOficina) {
            return null
        }
        return "Solo el ingeniero o el jefe técnico pueden formalizar."
    }

    if (hacia == EstadoProtocolo.OBSERVADO && desde == EstadoProtocolo.REGISTRADO) {
        if (esDeOficina) {
            return null
        }
        return "Solo el ingeniero o el jefe técnico devuelven con observación."
    }

    if (hacia == EstadoProtocolo.ANULADO) {
        if (esDeOficina) {
            return null
        }
        return "Solo el ingeniero o el jefe técnico pueden anular."
    }

    return "La transición ${desde.nombre} → ${hacia.nombre} no existe."
}

fun InduztekDbHelper.ultimoMotivo(idProtocolo: Int): String {
    val registro = fila(
        "SELECT motivo FROM historial_estado WHERE id_protocolo=? AND estado_nuevo='OBSERVADO' " +
                "ORDER BY id_historial DESC LIMIT 1",
        listOf(idProtocolo)
    )
    if (registro == null) {
        return ""
    }
    return registro.texto("motivo")
}

fun InduztekDbHelper.guardarAmbientes(
    idProtocolo: Int, temp: Double, humedad: Double, observaciones: String = ""
) {
    actualizar(
        "protocolo",
        mapOf("temperatura_c" to temp, "humedad_pct" to humedad, "observaciones" to observaciones),
        "id_protocolo=?",
        listOf(idProtocolo)
    )
    encolarCambio(CAMBIO_AMBIENTES, idProtocolo)
}

fun InduztekDbHelper.guardarObservacionesProtocolo(idProtocolo: Int, texto: String) {
    actualizar("protocolo", mapOf("observaciones" to texto), "id_protocolo=?", listOf(idProtocolo))
    encolarCambio(CAMBIO_AMBIENTES, idProtocolo)
}

fun InduztekDbHelper.protocolosDeVisita(idVisita: Int): List<Protocolo> {
    val deLaVisita = mutableListOf<Protocolo>()
    for (protocolo in listarProtocolos()) {
        if (protocolo.idVisita == idVisita) {
            deLaVisita.add(protocolo)
        }
    }
    return deLaVisita
}

fun InduztekDbHelper.protocolosDeVisitaCliente(idVisita: Int, idEmpresa: Int): List<Protocolo> {
    val esDeLaEmpresa = fila(
        "SELECT 1 FROM visita v JOIN instalacion i ON i.id_instalacion=v.id_instalacion " +
                "WHERE v.id_visita=? AND i.id_usuario_cliente=?",
        listOf(idVisita, idEmpresa)
    )
    if (esDeLaEmpresa == null) {
        return emptyList()
    }
    return protocolosDeVisita(idVisita)
}
