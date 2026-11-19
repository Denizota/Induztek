
package cl.duoc.induztek.data

import cl.duoc.induztek.model.Equipo
import cl.duoc.induztek.model.EstadoProtocolo
import cl.duoc.induztek.model.Instalacion
import cl.duoc.induztek.model.Instrumento
import cl.duoc.induztek.model.Protocolo
import cl.duoc.induztek.model.Prueba
import cl.duoc.induztek.model.RecepcionActa
import cl.duoc.induztek.model.Resultado
import cl.duoc.induztek.model.Rol
import cl.duoc.induztek.model.Solicitud
import cl.duoc.induztek.model.TipoPrueba
import cl.duoc.induztek.model.Usuario
import cl.duoc.induztek.model.Visita


fun aUsuario(fila: Map<String, String?>): Usuario {
    var idEmpresa: Int? = fila.entero("id_empresa")
    if (idEmpresa == 0) idEmpresa = null
    return Usuario(
        idUsuario = fila.entero("id_usuario"),
        idRol = fila.entero("id_rol"),
        rut = fila.texto("rut"),
        nombre = fila.texto("nombre"),
        correo = fila.texto("correo"),
        telefono = fila.texto("telefono"),
        activo = fila.siNo("activo"),
        rol = rolDe(fila.texto("codigo")),
        idEmpresa = idEmpresa,
        cargo = fila.texto("cargo"),
        giro = fila.texto("giro"),
        bio = fila.texto("bio"),
        especialidades = fila.texto("especialidades"),
        formacion = fila.texto("formacion"),
        certificaciones = fila.texto("certificaciones"),
        desde = fila.enteroSiHay("desde"),
        calificacion = fila.enteroSiHay("calificacion")
    )
}

fun aEquipo(fila: Map<String, String?>): Equipo = Equipo(
    idEquipo = fila.entero("id_equipo"),
    idInstalacion = fila.entero("id_instalacion"),
    tag = fila.texto("tag"),
    idTipoEquipo = fila.entero("id_tipo_equipo"),
    tipoEquipo = fila.texto("tipo"),
    clasificacion = fila.texto("clasificacion"),
    marca = fila.texto("marca"),
    modelo = fila.texto("modelo"),
    numeroSerie = fila.texto("numero_serie"),
    anioFabricacion = fila.entero("anio_fabricacion"),
    potenciaKva = fila.decimal("potencia_kva"),
    tensionPrimariaKv = fila.decimal("tension_primaria_kv"),
    tensionSecundariaKv = fila.decimal("tension_secundaria_kv"),
    corrienteNominalA = fila.decimal("corriente_nominal_a"),
    maxContactosUohm = fila.decimal("max_contactos_uohm"),
    observaciones = fila.texto("observaciones"),
    instalacionNombre = fila.texto("instalacion")
)

fun aProtocolo(fila: Map<String, String?>): Protocolo = Protocolo(
    idProtocolo = fila.entero("id_protocolo"),
    numero = fila.texto("numero"),
    idVisita = fila.entero("id_visita"),
    idEquipo = fila.entero("id_equipo"),
    idUsuarioRegistra = fila.entero("id_usuario_registra"),
    fechaHoraPrueba = fila.texto("fecha_hora_prueba"),
    temperaturaC = fila.decimal("temperatura_c"),
    humedadPct = fila.decimal("humedad_pct"),
    estado = estadoDe(fila.texto("estado")),
    observaciones = fila.texto("observaciones"),
    equipoTag = fila.texto("tag"),
    tipoEquipo = fila.texto("tipo"),
    instalacionNombre = fila.texto("instalacion"),
    tecnicoNombre = fila.texto("tecnico")
)

fun aPrueba(fila: Map<String, String?>, mediciones: Map<String, Double>): Prueba {
    val numeroInstrumento = fila.entero("id_instrumento")
    var idInstrumento: Int? = numeroInstrumento
    if (numeroInstrumento <= 0) idInstrumento = null
    return Prueba(
        idPrueba = fila.entero("id_prueba"),
        idProtocolo = fila.entero("id_protocolo"),
        tipoPrueba = TipoPrueba.valueOf(fila.texto("codigo_tipo_prueba")),
        elemento = fila.texto("elemento"),
        realizada = fila.siNo("realizada"),
        motivoNoRealizada = fila.texto("motivo_no_realizada"),
        idInstrumento = idInstrumento,
        resultado = resultadoDe(fila["resultado"]),
        comentario = fila.texto("comentario"),
        mediciones = mediciones
    )
}

fun aInstrumento(fila: Map<String, String?>): Instrumento = Instrumento(
    idInstrumento = fila.entero("id_instrumento"),
    nombre = fila.texto("nombre"),
    marca = fila.texto("marca"),
    modelo = fila.texto("modelo"),
    numeroSerie = fila.texto("numero_serie"),
    fechaUltimaCal = fila.texto("fecha_ultima_cal"),
    fechaVence = fila.texto("fecha_vence")
)

fun aSolicitud(fila: Map<String, String?>): Solicitud = Solicitud(
    idSolicitud = fila.entero("id_solicitud"),
    nombre = fila.texto("sol_nombre"),
    apellido = fila.texto("sol_apellido"),
    pais = fila.texto("sol_pais"),
    ciudad = fila.texto("sol_ciudad"),
    correo = fila.texto("sol_correo"),
    justificacion = fila.texto("sol_justificacion"),
    estado = fila.texto("sol_estado"),
    tipo = fila.texto("sol_tipo"),
    idUsuario = fila.enteroSiHay("sol_id_usuario"),
    campo = fila.texto("sol_campo"),
    valor = fila.texto("sol_valor")
)

fun aVisita(fila: Map<String, String?>): Visita {
    val numeroIngeniero = fila.entero("id_ingeniero_cargo")
    var idIngenieroCargo: Int? = numeroIngeniero
    if (numeroIngeniero <= 0) idIngenieroCargo = null
    return Visita(
        idVisita = fila.entero("id_visita"),
        numeroOt = fila.texto("numero_ot"),
        idInstalacion = fila.entero("id_instalacion"),
        fecha = fila.texto("fecha"),
        tipoVisita = fila.texto("tipo_visita"),
        idIngenieroCargo = idIngenieroCargo,
        fechaHoraCierre = fila["fecha_hora_cierre"]
    )
}

fun aInstalacion(fila: Map<String, String?>): Instalacion = Instalacion(
    idInstalacion = fila.entero("id_instalacion"),
    idUsuarioCliente = fila.entero("id_usuario_cliente"),
    nombre = fila.texto("nombre"),
    ubicacion = fila.texto("ubicacion"),
    comuna = fila.texto("comuna"),
    observacionesAcceso = fila.texto("observaciones_acceso"),
    clienteNombre = fila.texto("cliente_nombre")
)

fun aRecepcion(fila: Map<String, String?>): RecepcionActa = RecepcionActa(
    receptorNombre = fila.texto("receptor_nombre"),
    receptorRut = fila.texto("receptor_rut"),
    receptorCargo = fila.texto("receptor_cargo"),
    fechaHoraRecepcion = fila.texto("fecha_hora_recepcion")
)

private fun rolDe(codigo: String): Rol {
    for (posible in Rol.values()) {
        if (posible.codigo == codigo) return posible
    }
    error("rol desconocido: $codigo")
}

private fun estadoDe(codigo: String): EstadoProtocolo {
    for (posible in EstadoProtocolo.values()) {
        if (posible.codigo == codigo) return posible
    }
    error("estado desconocido: $codigo")
}

private fun resultadoDe(codigo: String?): Resultado? {
    if (codigo == null) return null
    for (posible in Resultado.values()) {
        if (posible.codigo == codigo) return posible
    }
    error("resultado desconocido: $codigo")
}
