package cl.duoc.induztek.data

import com.google.gson.Gson

internal const val CAMBIO_VISITA = "VISITA"
internal const val CAMBIO_PARTICIPANTE = "PARTICIPANTE"
internal const val CAMBIO_PROTOCOLO = "PROTOCOLO"
internal const val CAMBIO_PRUEBA = "PRUEBA"
internal const val CAMBIO_AMBIENTES = "AMBIENTES"
internal const val CAMBIO_ESTADO = "ESTADO"
internal const val CAMBIO_CIERRE = "CIERRE"
internal const val CAMBIO_USUARIO = "USUARIO"
internal const val CAMBIO_PERFIL = "PERFIL"
internal const val CAMBIO_ACTIVO = "ACTIVO"
internal const val CAMBIO_SOLICITUD = "SOLICITUD"
internal const val CAMBIO_SOLICITUD_ESTADO = "SOLICITUD_ESTADO"

internal val gsonCambios = Gson()

internal data class CambioPendiente(
    val idCambio: Int,
    val entidad: String,
    val idLocal: Int,
    val detalle: String
)

internal data class DetalleEstado(
    val estado: String = "",
    val idUsuario: Int = 0,
    val motivo: String = ""
)

internal data class DetalleParticipante(
    val idVisita: Int = 0,
    val idUsuario: Int = 0
)

internal data class DetalleCuenta(
    val idUsuario: Int = 0,
    val clave: String = ""
)

internal data class DetalleSolicitud(
    val clave: String = ""
)

internal fun InduztekDbHelper.encolarCambio(entidad: String, idLocal: Int, detalle: String = "") {
    insertar(
        "cambio_pendiente",
        mapOf("entidad" to entidad, "id_local" to idLocal, "detalle" to detalle)
    )
}

internal fun InduztekDbHelper.cambiosPendientes(): List<CambioPendiente> {
    val lista = mutableListOf<CambioPendiente>()
    val registros = filas(
        "SELECT id_cambio,entidad,id_local,detalle FROM cambio_pendiente ORDER BY id_cambio"
    )
    for (registro in registros) {
        lista.add(
            CambioPendiente(
                idCambio = registro.entero("id_cambio"),
                entidad = registro.texto("entidad"),
                idLocal = registro.entero("id_local"),
                detalle = registro.texto("detalle")
            )
        )
    }
    return lista
}

internal fun InduztekDbHelper.borrarCambio(idCambio: Int) {
    ejecutar("DELETE FROM cambio_pendiente WHERE id_cambio=?", listOf(idCambio))
}

internal fun InduztekDbHelper.hayCambio(entidad: String, idLocal: Int): Boolean {
    val registro = fila(
        "SELECT 1 FROM cambio_pendiente WHERE entidad=? AND id_local=?",
        listOf(entidad, idLocal)
    )
    return registro != null
}

internal fun InduztekDbHelper.idRemotoDe(tabla: String, clave: String, idLocal: Int): Int? {
    val registro = fila("SELECT id_remoto FROM $tabla WHERE $clave=?", listOf(idLocal))
    if (registro == null) return null
    return registro.enteroSiHay("id_remoto")
}

internal fun InduztekDbHelper.fijarIdRemoto(tabla: String, clave: String, idLocal: Int, idRemoto: Int) {
    actualizar(tabla, mapOf("id_remoto" to idRemoto), "$clave=?", listOf(idLocal))
}
