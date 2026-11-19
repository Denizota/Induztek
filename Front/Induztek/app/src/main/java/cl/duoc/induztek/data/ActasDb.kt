
package cl.duoc.induztek.data

import cl.duoc.induztek.model.EstadoProtocolo
import cl.duoc.induztek.model.Instalacion
import cl.duoc.induztek.model.Protocolo
import cl.duoc.induztek.model.RecepcionActa
import cl.duoc.induztek.model.Visita

fun InduztekDbHelper.visitaPorId(id: Int): Visita? {
    val fila = fila(
        "SELECT id_visita,numero_ot,id_instalacion,fecha,tipo_visita,id_ingeniero_cargo,fecha_hora_cierre " +
                "FROM visita WHERE id_visita=?",
        listOf(id)
    )
    if (fila == null) return null
    return aVisita(fila)
}

fun InduztekDbHelper.instalacionPorId(id: Int): Instalacion? {
    val fila = fila(
        "SELECT i.id_instalacion,i.id_usuario_cliente,i.nombre,i.ubicacion,i.comuna,i.observaciones_acceso," +
                "u.nombre AS cliente_nombre " +
                "FROM instalacion i JOIN usuario u ON u.id_usuario=i.id_usuario_cliente WHERE i.id_instalacion=?",
        listOf(id)
    )
    if (fila == null) return null
    return aInstalacion(fila)
}

fun InduztekDbHelper.recepcionDeVisita(idVisita: Int): RecepcionActa? {
    val fila = fila(
        "SELECT receptor_nombre,receptor_rut,receptor_cargo,fecha_hora_recepcion " +
                "FROM recepcion_acta WHERE id_visita=?",
        listOf(idVisita)
    )
    if (fila == null) return null
    return aRecepcion(fila)
}

fun InduztekDbHelper.cerrarVisita(idVisita: Int, nombre: String, rut: String, cargo: String) {
    val pendientes = mutableListOf<Protocolo>()
    for (protocolo in protocolosDeVisita(idVisita)) {
        if (protocolo.estado != EstadoProtocolo.REGISTRADO &&
            protocolo.estado != EstadoProtocolo.ANULADO
        ) {
            pendientes.add(protocolo)
        }
    }
    if (pendientes.isNotEmpty()) {
        var detalle = ""
        for (protocolo in pendientes) {
            if (detalle.isNotEmpty()) detalle = detalle + ", "
            detalle = detalle + protocolo.equipoTag + " (" + protocolo.estado.nombre.lowercase() + ")"
        }
        error("Faltan protocolos por registrar en terreno !!: " + detalle + ".")
    }
    val ahora = fechaHoraAhora()
    ejecutar(
        "INSERT OR REPLACE INTO recepcion_acta VALUES(?,?,?,?,?)",
        listOf(idVisita, nombre, rut, cargo, ahora)
    )
    actualizar("visita", mapOf("fecha_hora_cierre" to ahora), "id_visita=?", listOf(idVisita))
}
