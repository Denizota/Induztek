
package cl.duoc.induztek.data

import cl.duoc.induztek.model.Rol

fun InduztekDbHelper.asignarTarea(idEquipo: Int, idIngeniero: Int, idJefe: Int): String {
    val jefe = buscarUsuarioPorId(idJefe)
    if (jefe == null || jefe.rol != Rol.JEFE_TECNICO) {
        return "Solo el jefe técnico asigna tareas."
    }

    val ingeniero = buscarUsuarioPorId(idIngeniero)
    if (ingeniero == null || ingeniero.rol != Rol.INGENIERO) {
        return "La tarea se asigna a un ingeniero."
    }
    if (!ingeniero.activo) {
        return "Ese ingeniero tiene la cuenta suspendida."
    }

    val equipo = equipoPorId(idEquipo)
    if (equipo == null) {
        return "El equipo no existe en los registros."
    }

    val idVisita = visitaDeLaTarea(equipo.idInstalacion, idIngeniero)

    val yaTiene = fila(
        "SELECT numero FROM protocolo WHERE id_visita=? AND id_equipo=?",
        listOf(idVisita, idEquipo)
    )
    if (yaTiene != null) {
        return "Ese equipo ya tiene el protocolo ${yaTiene.texto("numero")} en esta visita."
    }

    val idProtocolo = insertar(
        "protocolo",
        mapOf(
            "numero" to nuevoNumeroProtocolo(),
            "id_visita" to idVisita,
            "id_equipo" to idEquipo,
            "id_usuario_registra" to idIngeniero,
            "fecha_hora_prueba" to fechaHoraAhora(),
            "estado" to "BORRADOR"
        )
    ).toInt()
    ejecutar(
        "INSERT INTO historial_estado(id_protocolo,estado_anterior,estado_nuevo,id_usuario,fecha_hora) VALUES(?,NULL,'BORRADOR',?,?)",
        listOf(idProtocolo, idJefe, fechaHoraAhora())
    )
    encolarCambio(CAMBIO_PROTOCOLO, idProtocolo)
    return ""
}

private fun InduztekDbHelper.visitaDeLaTarea(idInstalacion: Int, idIngeniero: Int): Int {
    val abierta = visitaAbierta(idInstalacion)
    if (abierta == null) {
        val anio = fechaHoy().substring(0, 4)
        val cantidad = contar("SELECT COUNT(*) FROM visita") + 1
        val idNueva = insertar(
            "visita",
            mapOf(
                "numero_ot" to "OT-$anio-${1300 + cantidad}",
                "id_instalacion" to idInstalacion,
                "fecha" to fechaHoy(),
                "tipo_visita" to "SOLO_PRUEBAS",
                "id_ingeniero_cargo" to idIngeniero
            )
        ).toInt()
        encolarCambio(CAMBIO_VISITA, idNueva)
        return idNueva
    }

    val registro = fila("SELECT id_ingeniero_cargo FROM visita WHERE id_visita=?", listOf(abierta))
    val aCargo = registro?.enteroSiHay("id_ingeniero_cargo")
    if (aCargo == null) {
        actualizar("visita", mapOf("id_ingeniero_cargo" to idIngeniero), "id_visita=?", listOf(abierta))
    } else if (aCargo != idIngeniero) {
        ejecutar(
            "INSERT OR IGNORE INTO visita_participante(id_visita,id_usuario) VALUES(?,?)",
            listOf(abierta, idIngeniero)
        )
        val detalle = gsonCambios.toJson(DetalleParticipante(idVisita = abierta, idUsuario = idIngeniero))
        encolarCambio(CAMBIO_PARTICIPANTE, abierta, detalle)
    }
    return abierta
}
