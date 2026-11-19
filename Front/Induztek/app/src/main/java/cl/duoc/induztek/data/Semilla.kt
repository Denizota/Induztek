
package cl.duoc.induztek.data

import android.database.sqlite.SQLiteDatabase
import cl.duoc.induztek.model.Rol

internal fun precargar(bd: SQLiteDatabase) {
    val hash = Seguridad.hashear(InduztekDbHelper.CLAVE_DEMO)

    for ((indice, rol) in Rol.values().withIndex()) {
        bd.execSQL(
            "INSERT INTO rol VALUES(?,?,?)",
            arrayOf(indice + 1, rol.codigo, rol.nombre)
        )
    }

    val empresas = listOf(
        Triple("76.482.915-8", "Envasadora del Pacífico S.A.", "Agroindustria"),
        Triple("96.731.540-0", "Sanitaria Valle Central S.A.", "Servicios sanitarios"),
        Triple("77.105.362-9", "Minera Cerro Alto SpA", "Minería"),
        Triple("76.903.218-5", "Clínica Los Aromos Ltda.", "Salud"),
        Triple("99.524.170-6", "Cementos Lomas Blancas S.A.", "Construcción"),
        Triple("78.214.635-1", "Data Center Andes Norte SpA", "Tecnología")
    )
    for ((rut, nombre, giro) in empresas) {
        bd.insert(
            "usuario", null, aValores(
                mapOf(
                    "id_rol" to 4, "rut" to rut, "nombre" to nombre,
                    "giro" to giro, "activo" to 1
                )
            )
        )
    }

    agregarPersona(bd, hash, 2, "16.482.905-7", "Thomas Peralta S.", "tperalta@induztek.ejemplo")
    agregarPersona(bd, hash, 3, "14.993.210-0", "Joaquín Dupre", "jdupre@induztek.ejemplo")
    agregarPersona(bd, hash, 4, "17.650.348-3", "Víctor Isidro R.", "visidro@envpacifico.ejemplo", 1, "Jefe de Planta")
    agregarPersona(bd, hash, 1, "17.284.519-3", "Matías Godoy Herrera", "mgodoy@induztek.ejemplo")
    agregarPersona(bd, hash, 1, "19.412.087-7", "Javiera Tapia Muñoz", "jtapia@induztek.ejemplo")
    agregarPersona(bd, hash, 1, "26.104.583-4", "Carlos Rondón Pérez", "crondon@induztek.ejemplo")
    agregarPersona(bd, hash, 1, "20.137.648-3", "Diego Valenzuela Ríos", "dvalenzuela@induztek.ejemplo")
    agregarPersona(bd, hash, 2, "15.906.334-8", "Andrea Contreras Silva", "acontreras@induztek.ejemplo")
    agregarPersona(bd, hash, 2, "16.553.270-8", "Sebastián Morales Gutiérrez", "smorales@induztek.ejemplo")
    agregarPersona(bd, hash, 3, "13.871.905-7", "Ricardo Espinoza Lagos", "respinoza@induztek.ejemplo")
    agregarPersona(bd, hash, 4, "14.226.791-8", "Marcela Fuentes Rojas", "mfuentes@envpacifico.ejemplo", 1, "Jefa de Mantenimiento")
    agregarPersona(bd, hash, 4, "18.390.452-3", "Tomás Leiva Araya", "tleiva@losaromos.ejemplo", 4, "Supervisor Eléctrico")
    agregarPersona(bd, hash, 4, "12.645.308-6", "Patricio Navarro Soto", "pnavarro@cerroalto.ejemplo", 3, "Jefe de Mantenimiento Eléctrico")

    bd.execSQL(
        "UPDATE usuario SET bio=?, especialidades=?, formacion=?, certificaciones=?, desde=?, calificacion=? " +
                "WHERE nombre='Thomas Peralta S.'",
        arrayOf(
            "Técnico en pruebas eléctricas. Trabajo principalmente con transformadores y celdas de " +
                    "media tensión en plantas industriales de la zona centro-sur.",
            "Transformadores, Celdas MT, Reles, Interruptores",
            "Técnico en electricidad industrial",
            "SEC clase B",
            2019, 5
        )
    )
    bd.execSQL(
        "UPDATE usuario SET bio=?, especialidades=?, formacion=?, certificaciones=?, desde=?, calificacion=? " +
                "WHERE nombre='Joaquín Dupre'",
        arrayOf(
            "Ingeniero a cargo de las pruebas de alto voltaje en terreno y de la revisión de los protocolos.",
            "Transformadores, Reles, Mediciones parciales",
            "Ingeniero eléctrico, mención potencia",
            "SEC clase A",
            2016, 5
        )
    )
    bd.execSQL(
        "UPDATE usuario SET bio=?, desde=?, calificacion=? WHERE nombre='Víctor Isidro R.'",
        arrayOf(
            "Jefe de planta. Reviso los protocolos y recibo las actas de cada visita técnica.",
            2021, 4
        )
    )

    val instalaciones = listOf(
        listOf("1", "Planta Lampa", "Sector industrial", "Lampa", "Inducción obligatoria de 30 min"),
        listOf("2", "Planta Elevadora Sur", "Sector rural poniente", "San Bernardo", "Sala eléctrica subterránea, sin señal"),
        listOf("3", "Faena Til Til", "Acceso por Ruta 5 Norte", "Til Til", "Sin señal en toda la faena"),
        listOf("4", "Edificio Clínico", "Zona urbana", "La Florida", "Cortes solo domingos 06:00-10:00"),
        listOf("5", "Planta Molienda", "Sector precordillera", "Puente Alto", "Uso obligatorio de respirador"),
        listOf("6", "Site Huechuraba", "Parque empresarial", "Huechuraba", "Acompañamiento permanente del cliente")
    )
    for ((cliente, nombre, ubicacion, comuna, observaciones) in instalaciones) {
        bd.execSQL(
            "INSERT INTO instalacion(id_usuario_cliente,nombre,ubicacion,comuna,observaciones_acceso) VALUES(?,?,?,?,?)",
            arrayOf(cliente, nombre, ubicacion, comuna, observaciones)
        )
    }

    bd.execSQL("INSERT INTO tipo_equipo VALUES(1,'Transformador','PRIMARIO')")
    bd.execSQL("INSERT INTO tipo_equipo VALUES(2,'Interruptor','PRIMARIO')")
    bd.execSQL("INSERT INTO tipo_equipo VALUES(3,'Celda MT','PRIMARIO')")
    bd.execSQL("INSERT INTO tipo_equipo VALUES(4,'Cable de poder','PRIMARIO')")
    bd.execSQL("INSERT INTO tipo_equipo VALUES(5,'Relé de protección','SECUNDARIO')")

    bd.execSQL("INSERT INTO tipo_prueba VALUES(1,'AISLACION','Resistencia de aislación')")
    bd.execSQL("INSERT INTO tipo_prueba VALUES(2,'TTR','Relación de transformación')")
    bd.execSQL("INSERT INTO tipo_prueba VALUES(3,'DEVANADOS','Resistencia de devanados')")
    bd.execSQL("INSERT INTO tipo_prueba VALUES(4,'CONTACTOS','Resistencia de contactos')")
    bd.execSQL("INSERT INTO tipo_prueba VALUES(5,'OPERACION_RELE','Prueba de operación (inyección secundaria)')")

    val paresEquipoPrueba = listOf(
        1 to 1, 1 to 2, 1 to 3, 2 to 1, 2 to 4,
        3 to 1, 3 to 4, 4 to 1, 5 to 5
    )
    for ((tipoEquipo, tipoPrueba) in paresEquipoPrueba) {
        bd.execSQL("INSERT INTO tipo_equipo_prueba VALUES(?,?)", arrayOf(tipoEquipo, tipoPrueba))
    }

    val equipos = listOf(
        listOf("1", "TR-01", "1", "Rhona", "RH-18733", "2012", "1500", "12", "0.4", "", ""),
        listOf("1", "TR-02", "1", "Rhona", "RH-21904", "2017", "1000", "12", "0.4", "", ""),
        listOf("2", "TR-A", "1", "Tusan", "TS-5521", "2009", "2500", "23", "0.4", "", ""),
        listOf("4", "CEL-MT-03", "3", "Ormazabal", "OR-77812", "2018", "", "12", "", "630", "60"),
        listOf("3", "INT-52-1", "2", "Schneider", "SE-449021", "2015", "", "23", "", "1250", "45"),
        listOf("5", "REL-50/51-A", "5", "SEL", "SEL-751-30219", "2020", "", "", "", "", ""),
        listOf("6", "CAB-MT-02", "4", "Nexans", "NX-2201-B", "2019", "", "15", "", "", ""),
        listOf("3", "TR-SS-01", "1", "ABB", "ABB-1180442", "2011", "5000", "23", "6.6", "", "")
    )
    for (fila in equipos) {
        var observaciones = ""
        if (fila[1] == "REL-50/51-A") observaciones = "Sobrecorriente, ajuste 5 A"
        if (fila[1] == "CAB-MT-02") observaciones = "3x240 mm², 180 m"
        bd.execSQL(
            "INSERT INTO equipo(id_instalacion,tag,id_tipo_equipo,marca,modelo,numero_serie,anio_fabricacion," +
                    "potencia_kva,tension_primaria_kv,tension_secundaria_kv,corriente_nominal_a,max_contactos_uohm,observaciones) " +
                    "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)",
            arrayOf(
                fila[0], fila[1], fila[2], fila[3], fila[4], fila[4], fila[5],
                textoONulo(fila[6]), textoONulo(fila[7]), textoONulo(fila[8]),
                textoONulo(fila[9]), textoONulo(fila[10]), observaciones
            )
        )
    }

    val instrumentos = listOf(
        listOf("Megóhmetro 5 kV", "Megger", "MIT525", "MIT-0045821", "2026-03-15", "2027-03-15"),
        listOf("Megóhmetro 10 kV", "Megger", "MIT1025", "MIT-0071233", "2025-10-10", "2026-10-10"),
        listOf("Medidor de relación (TTR)", "Megger", "TTR300", "TTR-30044", "2026-06-02", "2027-06-02"),
        listOf("Micro-ohmímetro", "Megger", "DLRO10X", "DLR-10882", "2026-08-20", "2027-08-20"),
        listOf("Equipo de inyección secundaria", "Omicron", "CMC 356", "CMC-HB411", "2026-01-05", "2027-01-05")
    )
    for (fila in instrumentos) {
        bd.execSQL(
            "INSERT INTO instrumento(nombre,marca,modelo,numero_serie,fecha_ultima_cal,fecha_vence) VALUES(?,?,?,?,?,?)",
            arrayOf(fila[0], fila[1], fila[2], fila[3], fila[4], fila[5])
        )
    }

    precargarHistorial(bd)
}

private fun agregarPersona(
    bd: SQLiteDatabase, hash: String, idRol: Int, rut: String, nombre: String, correo: String,
    empresa: Int? = null, cargo: String = "", giro: String = ""
) {
    val datos = mutableMapOf<String, Any?>(
        "id_rol" to idRol, "rut" to rut, "nombre" to nombre, "correo" to correo,
        "password_hash" to hash, "cargo" to cargo, "giro" to giro
    )
    if (empresa != null) {
        datos["id_empresa"] = empresa
    }
    bd.insert("usuario", null, aValores(datos))
}

private fun textoONulo(texto: String): String? {
    if (texto.isEmpty()) return null
    return texto
}
