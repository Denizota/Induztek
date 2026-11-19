
package cl.duoc.induztek.data

import android.database.sqlite.SQLiteDatabase

internal fun precargarHistorial(bd: SQLiteDatabase) {
    fun visita(ordenTrabajo: String, instalacion: Int, fecha: String, ingeniero: Int?, cierre: String?): Long {
        val datos = mutableMapOf<String, Any?>(
            "numero_ot" to ordenTrabajo,
            "id_instalacion" to instalacion,
            "fecha" to fecha,
            "tipo_visita" to "SOLO_PRUEBAS"
        )
        if (ingeniero != null) datos["id_ingeniero_cargo"] = ingeniero
        if (cierre != null) datos["fecha_hora_cierre"] = cierre
        return bd.insert("visita", null, aValores(datos))
    }

    fun protocolo(
        id: Long, numero: String, idVisita: Int, idEquipo: Int, tecnico: Int,
        fechaHora: String, temp: Double, humedad: Double, estado: String
    ) {
        bd.execSQL(
            "INSERT INTO protocolo(id_protocolo,numero,id_visita,id_equipo,id_usuario_registra,fecha_hora_prueba,temperatura_c,humedad_pct,estado) " +
                    "VALUES(?,?,?,?,?,?,?,?,?)",
            arrayOf(id, numero, idVisita, idEquipo, tecnico, fechaHora, temp, humedad, estado)
        )
    }

    fun prueba(
        idProtocolo: Int, tipo: String, elemento: String, instrumento: Int?,
        resultado: String, valoresMedidos: Map<String, String>
    ) {
        val datos = mutableMapOf<String, Any?>(
            "id_protocolo" to idProtocolo,
            "codigo_tipo_prueba" to tipo,
            "elemento" to elemento,
            "realizada" to 1,
            "resultado" to resultado
        )
        if (instrumento != null) datos["id_instrumento"] = instrumento
        val idPrueba = bd.insert("prueba", null, aValores(datos)).toInt()
        for ((codigo, valor) in valoresMedidos) {
            bd.insert(
                "medicion", null, aValores(
                    mapOf("id_prueba" to idPrueba, "codigo_parametro" to codigo, "valor" to valor)
                )
            )
        }
    }

    val visita2024 = visita("OT-2024-1042", 1, "2024-09-14", 7, "2024-09-14 15:20").toInt()
    bd.execSQL(
        "INSERT INTO recepcion_acta VALUES(?,?,?,?,'2024-09-14 15:20')",
        arrayOf(visita2024, "Marcela Fuentes Rojas", "14.226.791-8", "Jefa de Mantenimiento")
    )
    bd.execSQL("INSERT INTO visita_participante VALUES(?,?)", arrayOf(visita2024, 10))
    protocolo(301, "PT-2024-0312", visita2024, 1, 10, "2024-09-14 10:30", 18.0, 55.0, "FORMALIZADO")
    prueba(
        301, "AISLACION", "AT-T", 1, "APROBADO",
        mapOf("TENSION_PRUEBA" to "5", "LECTURA_1MIN" to "12500", "LECTURA_10MIN" to "31250")
    )
    prueba(
        301, "TTR", "3 fases / tap 3", 3, "APROBADO",
        mapOf("POSICION_TAP" to "3", "RELACION_TEORICA" to "30", "RELACION_MEDIDA" to "30.08")
    )
    prueba(301, "DEVANADOS", "AT", 4, "APROBADO", mapOf("RESISTENCIA" to "45.2"))
    prueba(301, "DEVANADOS", "BT", 4, "APROBADO", mapOf("RESISTENCIA" to "2.15"))

    val visita2025 = visita("OT-2025-1101", 1, "2025-09-13", 7, "2025-09-13 16:05").toInt()
    bd.execSQL(
        "INSERT INTO recepcion_acta VALUES(?,?,?,?,'2025-09-13 16:05')",
        arrayOf(visita2025, "Marcela Fuentes Rojas", "14.226.791-8", "Jefa de Mantenimiento")
    )
    bd.execSQL("INSERT INTO visita_participante VALUES(?,?)", arrayOf(visita2025, 11))
    protocolo(302, "PT-2025-0288", visita2025, 1, 11, "2025-09-13 11:10", 21.0, 48.0, "FORMALIZADO")
    prueba(
        302, "AISLACION", "AT-T", 1, "APROBADO",
        mapOf("TENSION_PRUEBA" to "5", "LECTURA_1MIN" to "9800", "LECTURA_10MIN" to "21560")
    )
    prueba(
        302, "TTR", "3 fases / tap 3", 3, "APROBADO",
        mapOf("POSICION_TAP" to "3", "RELACION_TEORICA" to "30", "RELACION_MEDIDA" to "30.05")
    )
    prueba(302, "DEVANADOS", "AT", 4, "APROBADO", mapOf("RESISTENCIA" to "46.8"))
    prueba(302, "DEVANADOS", "BT", 4, "APROBADO", mapOf("RESISTENCIA" to "2.2"))

    val visita2026 = visita("OT-2026-1187", 1, "2026-09-26", 8, null).toInt()
    bd.execSQL("INSERT INTO visita_participante VALUES(?,?)", arrayOf(visita2026, 7))

    fun cerrada(
        ordenTrabajo: String, numero: String, idProtocolo: Long, instalacion: Int, idEquipo: Int,
        tecnico: Int, dia: String, temp: Double, humedad: Double
    ): Int {
        val idVisita = visita(ordenTrabajo, instalacion, dia, 7, "$dia 16:05").toInt()
        protocolo(idProtocolo, numero, idVisita, idEquipo, tecnico, "$dia 10:30", temp, humedad, "FORMALIZADO")
        return idVisita
    }

    cerrada("OT-2025-1112", "PT-2025-0290", 401, 2, 3, 11, "2025-08-05", 22.0, 44.0)
    prueba(
        401, "AISLACION", "AT-T", 1, "APROBADO",
        mapOf("TENSION_PRUEBA" to "5", "LECTURA_1MIN" to "8200", "LECTURA_10MIN" to "24600")
    )
    prueba(
        401, "TTR", "3 fases / cambiador de derivaciones", 3, "APROBADO",
        mapOf("POSICION_TAP" to "2", "RELACION_TEORICA" to "57.5", "RELACION_MEDIDA" to "57.56")
    )
    prueba(401, "DEVANADOS", "AT-R", 4, "APROBADO", mapOf("RESISTENCIA" to "12.4"))
    prueba(401, "DEVANADOS", "BT-r", 4, "APROBADO", mapOf("RESISTENCIA" to "0.85"))

    val version402 = cerrada("OT-2025-1118", "PT-2025-0291", 402, 3, 5, 12, "2025-08-19", 17.0, 52.0)
    prueba(
        402, "AISLACION", "MT-BT", 1, "APROBADO",
        mapOf("TENSION_PRUEBA" to "5", "LECTURA_1MIN" to "5200")
    )
    prueba(402, "CONTACTOS", "Polo 1", 4, "APROBADO", mapOf("RESISTENCIA_CONTACTO" to "38.5"))
    prueba(402, "CONTACTOS", "Polo 2", 4, "APROBADO", mapOf("RESISTENCIA_CONTACTO" to "41.0"))
    prueba(402, "CONTACTOS", "Polo 3", 4, "OBSERVADO", mapOf("RESISTENCIA_CONTACTO" to "52.0"))
    bd.execSQL(
        "INSERT INTO recepcion_acta VALUES(?,?,?,?,'2025-08-19 16:05')",
        arrayOf(version402, "Patricio Navarro Soto", "12.645.308-6", "Jefe de Mantenimiento Eléctrico")
    )

    val version403 = cerrada("OT-2025-1124", "PT-2025-0292", 403, 4, 4, 13, "2025-09-02", 20.0, 47.0)
    prueba(
        403, "AISLACION", "MT-Tierra", 1, "APROBADO",
        mapOf("TENSION_PRUEBA" to "5", "LECTURA_1MIN" to "3100")
    )
    prueba(403, "CONTACTOS", "Polo 1", 4, "APROBADO", mapOf("RESISTENCIA_CONTACTO" to "44.0"))
    prueba(403, "CONTACTOS", "Polo 2", 4, "APROBADO", mapOf("RESISTENCIA_CONTACTO" to "48.5"))
    prueba(403, "CONTACTOS", "Polo 3", 4, "OBSERVADO", mapOf("RESISTENCIA_CONTACTO" to "67.0"))
    bd.execSQL(
        "INSERT INTO recepcion_acta VALUES(?,?,?,?,'2025-09-02 16:05')",
        arrayOf(version403, "Tomás Leiva Araya", "18.390.452-3", "Supervisor Eléctrico")
    )

    cerrada("OT-2025-1131", "PT-2025-0293", 404, 5, 6, 10, "2025-09-09", 15.0, 61.0)
    prueba(
        404, "OPERACION_RELE", "Disparo de fase", 5, "APROBADO",
        mapOf(
            "CORRIENTE_AJUSTE" to "5", "CORRIENTE_OPERACION" to "5.1",
            "TIEMPO_ESPERADO" to "80", "TIEMPO_MEDIDO" to "82.4"
        )
    )
    prueba(
        404, "OPERACION_RELE", "Disparo de neutro", 5, "OBSERVADO",
        mapOf(
            "CORRIENTE_AJUSTE" to "5", "CORRIENTE_OPERACION" to "5.3",
            "TIEMPO_ESPERADO" to "80", "TIEMPO_MEDIDO" to "86.4"
        )
    )

    cerrada("OT-2025-1137", "PT-2025-0294", 405, 6, 7, 11, "2025-09-16", 23.0, 39.0)
    prueba(
        405, "AISLACION", "Fase R", 1, "APROBADO",
        mapOf("TENSION_PRUEBA" to "5", "LECTURA_1MIN" to "15200", "LECTURA_10MIN" to "45600")
    )
    prueba(
        405, "AISLACION", "Fase S", 1, "APROBADO",
        mapOf("TENSION_PRUEBA" to "5", "LECTURA_1MIN" to "12800", "LECTURA_10MIN" to "39680")
    )
    prueba(
        405, "AISLACION", "Fase T", 1, "OBSERVADO",
        mapOf("TENSION_PRUEBA" to "5", "LECTURA_1MIN" to "4200", "LECTURA_10MIN" to "6300")
    )

    cerrada("OT-2025-1144", "PT-2025-0295", 406, 3, 8, 12, "2025-09-23", 12.0, 66.0)
    prueba(
        406, "AISLACION", "AT-T", 1, "APROBADO",
        mapOf("TENSION_PRUEBA" to "5", "LECTURA_1MIN" to "21000", "LECTURA_10MIN" to "54600")
    )
    prueba(
        406, "TTR", "3 fases / cambiador de derivaciones", 3, "APROBADO",
        mapOf("POSICION_TAP" to "4", "RELACION_TEORICA" to "3.4848", "RELACION_MEDIDA" to "3.49")
    )
    prueba(406, "DEVANADOS", "AT-R", 4, "APROBADO", mapOf("RESISTENCIA" to "286.0"))
    prueba(406, "DEVANADOS", "BT-r", 4, "APROBADO", mapOf("RESISTENCIA" to "3.9"))

    val instalacionesAbiertas = listOf(2, 3, 4, 5, 6)
    for (indice in instalacionesAbiertas.indices) {
        val instalacion = instalacionesAbiertas[indice]
        visita("OT-2026-${1188 + indice}", instalacion, "2026-09-26", 8, null)
    }
}
