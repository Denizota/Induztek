
package cl.duoc.induztek.data

import cl.duoc.induztek.model.Rol
import cl.duoc.induztek.model.Solicitud
import cl.duoc.induztek.model.Usuario

internal const val COLUMNAS_SOLICITUD =
    "id_solicitud,sol_nombre,sol_apellido,sol_pais,sol_ciudad,sol_correo," +
            "sol_justificacion,sol_estado,sol_tipo,sol_id_usuario,sol_campo,sol_valor"

fun InduztekDbHelper.contarSolicitudesPendientes(): Int =
    contar("SELECT COUNT(*) FROM solicitud_acceso WHERE sol_estado='PENDIENTE'")

fun InduztekDbHelper.listarSolicitudesPendientes(): List<Solicitud> {
    val lista = mutableListOf<Solicitud>()
    val registros = filas(
        "SELECT $COLUMNAS_SOLICITUD " +
                "FROM solicitud_acceso WHERE sol_estado='PENDIENTE' ORDER BY id_solicitud DESC"
    )
    for (registro in registros) {
        val solicitud = aSolicitud(registro)
        lista.add(solicitud)
    }
    return lista
}

fun InduztekDbHelper.listarSolicitudesAprobadas(): List<Solicitud> {
    val lista = mutableListOf<Solicitud>()
    val registros = filas(
        "SELECT $COLUMNAS_SOLICITUD " +
                "FROM solicitud_acceso WHERE sol_estado='APROBADA' AND sol_tipo='INGRESO' " +
                "ORDER BY id_solicitud DESC"
    )
    for (registro in registros) {
        val solicitud = aSolicitud(registro)
        lista.add(solicitud)
    }
    return lista
}

fun InduztekDbHelper.insertarSolicitudBaja(persona: Usuario, motivo: String): Boolean {
    if (tieneSolicitudBajaPendiente(persona.idUsuario)) return false
    val id = insertar(
        "solicitud_acceso",
        mapOf(
            "sol_nombre" to persona.nombre,
            "sol_apellido" to "",
            "sol_pais" to "",
            "sol_ciudad" to "",
            "sol_correo" to persona.correo,
            "sol_password_hash" to "",
            "sol_justificacion" to motivo,
            "sol_estado" to "PENDIENTE",
            "sol_tipo" to "BAJA",
            "sol_id_usuario" to persona.idUsuario
        )
    )
    if (id > 0) encolarCambio(CAMBIO_SOLICITUD, id.toInt())
    return id > 0
}

fun InduztekDbHelper.tieneSolicitudBajaPendiente(idUsuario: Int): Boolean {
    val fila = fila(
        "SELECT 1 FROM solicitud_acceso WHERE sol_tipo='BAJA' AND sol_id_usuario=? AND sol_estado='PENDIENTE'",
        listOf(idUsuario)
    )
    return fila != null
}

fun InduztekDbHelper.insertarSolicitudCambio(
    persona: Usuario, campo: String, valor: String, motivo: String
): Boolean {
    if (columnaDeCambio(campo) == null) return false
    val repetida = fila(
        "SELECT 1 FROM solicitud_acceso WHERE sol_tipo='CAMBIO' AND sol_id_usuario=? " +
                "AND sol_campo=? AND sol_estado='PENDIENTE'",
        listOf(persona.idUsuario, campo)
    )
    if (repetida != null) return false
    val id = insertar(
        "solicitud_acceso",
        mapOf(
            "sol_nombre" to persona.nombre,
            "sol_apellido" to "",
            "sol_pais" to "",
            "sol_ciudad" to "",
            "sol_correo" to persona.correo,
            "sol_password_hash" to "",
            "sol_justificacion" to motivo,
            "sol_estado" to "PENDIENTE",
            "sol_tipo" to "CAMBIO",
            "sol_id_usuario" to persona.idUsuario,
            "sol_campo" to campo,
            "sol_valor" to valor
        )
    )
    if (id > 0) encolarCambio(CAMBIO_SOLICITUD, id.toInt())
    return id > 0
}

fun InduztekDbHelper.aplicarCambioSolicitud(solicitud: Solicitud): Boolean {
    val columna = columnaDeCambio(solicitud.campo)
    if (columna == null) return false
    val idUsuario = solicitud.idUsuario
    if (idUsuario == null) return false
    val cambiado = actualizar("usuario", mapOf(columna to solicitud.valor), "id_usuario=?", listOf(idUsuario))
    if (cambiado) encolarCambio(CAMBIO_PERFIL, idUsuario)
    return cambiado
}

private fun columnaDeCambio(campo: String): String? {
    val nombre = campo.trim().lowercase()
    if (nombre == "cargo") return "cargo"
    if (nombre == "correo") return "correo"
    if (nombre == "telefono") return "telefono"
    if (nombre == "teléfono") return "telefono"
    if (nombre == "rut") return "rut"
    return null
}

fun InduztekDbHelper.cambiarEstadoSolicitud(id: Int, estado: String): Boolean {
    val cambiado = actualizar("solicitud_acceso", mapOf("sol_estado" to estado), "id_solicitud=?", listOf(id))
    if (cambiado) encolarCambio(CAMBIO_SOLICITUD_ESTADO, id)
    return cambiado
}

fun InduztekDbHelper.usuarioDeSolicitudBaja(idSolicitud: Int): Usuario? {
    val registro = fila(
        "SELECT sol_id_usuario FROM solicitud_acceso WHERE id_solicitud=?",
        listOf(idSolicitud)
    )
    if (registro == null) return null
    val idUsuario = registro.enteroSiHay("sol_id_usuario")
    if (idUsuario == null) return null
    return buscarUsuarioPorId(idUsuario)
}

fun InduztekDbHelper.crearCuentaDesdeSolicitud(
    solicitud: Solicitud, rut: String, rol: Rol, clave: String, empresa: Int? = null
): Int {
    val idRol = buscarIdRol(rol)
    if (idRol == null) return -1
    var nombreCompleto = solicitud.nombre + " " + solicitud.apellido
    nombreCompleto = nombreCompleto.trim()
    val datos = mutableMapOf<String, Any?>(
        "id_rol" to idRol,
        "rut" to rut,
        "nombre" to nombreCompleto,
        "correo" to solicitud.correo,
        "password_hash" to Seguridad.hashear(clave),
        "activo" to 1,
        "giro" to solicitud.ciudad
    )
    if (empresa != null) datos["id_empresa"] = empresa
    val idCuentaCreada = insertar("usuario", datos).toInt()
    if (idCuentaCreada > 0) {
        cambiarEstadoSolicitud(solicitud.idSolicitud, "CREADA")
        val detalle = gsonCambios.toJson(DetalleCuenta(idUsuario = idCuentaCreada, clave = clave))
        encolarCambio(CAMBIO_USUARIO, idCuentaCreada, detalle)
    }
    return idCuentaCreada
}
