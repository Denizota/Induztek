package cl.duoc.induztek.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import cl.duoc.induztek.model.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



// y chile.... :c
fun fechaHoy(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
fun fechaHoraAhora(): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())

internal fun String.fechaCorta(): String {
    val partes = split("-")
    return if (partes.size == 3) partes[2] + "-" + partes[1] + "-" + partes[0] else this
}



class InduztekDbHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    companion object {
        private const val NOMBRE_BD = "induztek.db"
        private const val VERSION_BD = 5
        const val CLAVE_DEMO = "Induztek2026"


        private val HASH_SEMILLA = Seguridad.hashear(CLAVE_DEMO)

        @Volatile
        private var instancia: InduztekDbHelper? = null

        fun obtener(context: Context): InduztekDbHelper =
            instancia ?: synchronized(this) {
                instancia ?: InduztekDbHelper(context.applicationContext).also { instancia = it }
            }



        var generacion = 0L
            private set
    }



    fun reiniciarTodo() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            borrarTablas(db)
            onCreate(db)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        synchronized(this) { generacion++ }
    }


    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE rol (" +
                    "id_rol INTEGER PRIMARY KEY, codigo TEXT UNIQUE NOT NULL, nombre TEXT NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE usuario (" +
                    "id_usuario INTEGER PRIMARY KEY AUTOINCREMENT, id_rol INTEGER NOT NULL, " +
                    "rut TEXT UNIQUE NOT NULL, nombre TEXT NOT NULL, correo TEXT UNIQUE, " +
                    "telefono TEXT DEFAULT '', password_hash TEXT, activo INTEGER DEFAULT 1, " +
                    "id_empresa INTEGER, cargo TEXT DEFAULT '', giro TEXT DEFAULT '')"
        )
        db.execSQL(
            "CREATE TABLE solicitud_acceso (" +
                    "id_solicitud INTEGER PRIMARY KEY AUTOINCREMENT, sol_nombre TEXT NOT NULL, " +
                    "sol_apellido TEXT NOT NULL, sol_pais TEXT NOT NULL, sol_ciudad TEXT NOT NULL, " +
                    "sol_correo TEXT NOT NULL, sol_password_hash TEXT NOT NULL, " +
                    "sol_justificacion TEXT NOT NULL, sol_estado TEXT NOT NULL DEFAULT 'PENDIENTE')"
        )
        db.execSQL(
            "CREATE TABLE instalacion (" +
                    "id_instalacion INTEGER PRIMARY KEY AUTOINCREMENT, id_usuario_cliente INTEGER NOT NULL, " +
                    "nombre TEXT NOT NULL, ubicacion TEXT DEFAULT '', comuna TEXT DEFAULT '', " +
                    "observaciones_acceso TEXT DEFAULT '')"
        )
        db.execSQL(
            "CREATE TABLE tipo_equipo (" +
                    "id_tipo_equipo INTEGER PRIMARY KEY, nombre TEXT UNIQUE NOT NULL, " +
                    "clasificacion TEXT NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE equipo (" +
                    "id_equipo INTEGER PRIMARY KEY AUTOINCREMENT, id_instalacion INTEGER NOT NULL, " +
                    "tag TEXT NOT NULL, id_tipo_equipo INTEGER NOT NULL, marca TEXT DEFAULT '', " +
                    "modelo TEXT DEFAULT '', numero_serie TEXT DEFAULT '', anio_fabricacion INTEGER, " +
                    "potencia_kva REAL, tension_primaria_kv REAL, tension_secundaria_kv REAL, " +
                    "corriente_nominal_a REAL, max_contactos_uohm REAL, observaciones TEXT DEFAULT '', " +
                    "UNIQUE(id_instalacion, tag))"
        )
        db.execSQL(
            "CREATE TABLE instrumento (" +
                    "id_instrumento INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL, " +
                    "marca TEXT DEFAULT '', modelo TEXT DEFAULT '', numero_serie TEXT UNIQUE, " +
                    "fecha_ultima_cal TEXT, fecha_vence TEXT)"
        )
        db.execSQL(
            "CREATE TABLE visita (" +
                    "id_visita INTEGER PRIMARY KEY AUTOINCREMENT, numero_ot TEXT UNIQUE NOT NULL, " +
                    "id_instalacion INTEGER NOT NULL, fecha TEXT NOT NULL, tipo_visita TEXT DEFAULT 'SOLO_PRUEBAS', " +
                    "id_ingeniero_cargo INTEGER, fecha_hora_cierre TEXT, descripcion_mantenimiento TEXT DEFAULT '')"
        )
        db.execSQL(
            "CREATE TABLE visita_participante (id_visita INTEGER NOT NULL, id_usuario INTEGER NOT NULL, " +
                    "PRIMARY KEY(id_visita, id_usuario))"
        )
        db.execSQL(
            "CREATE TABLE recepcion_acta (" +
                    "id_visita INTEGER PRIMARY KEY, receptor_nombre TEXT NOT NULL, receptor_rut TEXT NOT NULL, " +
                    "receptor_cargo TEXT DEFAULT '', fecha_hora_recepcion TEXT NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE protocolo (" +
                    "id_protocolo INTEGER PRIMARY KEY AUTOINCREMENT, numero TEXT UNIQUE NOT NULL, " +
                    "id_visita INTEGER NOT NULL, id_equipo INTEGER NOT NULL, id_usuario_registra INTEGER NOT NULL, " +
                    "fecha_hora_prueba TEXT NOT NULL, temperatura_c REAL, humedad_pct REAL, " +
                    "estado TEXT NOT NULL DEFAULT 'BORRADOR', observaciones TEXT DEFAULT '', " +
                    "UNIQUE(id_visita, id_equipo))"
        )
        db.execSQL(
            "CREATE TABLE tipo_prueba (" +
                    "id_tipo_prueba INTEGER PRIMARY KEY, codigo TEXT UNIQUE NOT NULL, nombre TEXT NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE tipo_equipo_prueba (" +
                    "id_tipo_equipo INTEGER NOT NULL, id_tipo_prueba INTEGER NOT NULL, " +
                    "PRIMARY KEY(id_tipo_equipo, id_tipo_prueba))"
        )
        db.execSQL(
            "CREATE TABLE prueba (" +
                    "id_prueba INTEGER PRIMARY KEY AUTOINCREMENT, id_protocolo INTEGER NOT NULL, " +
                    "codigo_tipo_prueba TEXT NOT NULL, elemento TEXT DEFAULT '', momento TEXT DEFAULT 'UNICA', " +
                    "realizada INTEGER DEFAULT 1, motivo_no_realizada TEXT DEFAULT '', " +
                    "id_instrumento INTEGER, resultado TEXT, comentario TEXT DEFAULT '')"
        )
        db.execSQL(
            "CREATE TABLE medicion (" +
                    "id_medicion INTEGER PRIMARY KEY AUTOINCREMENT, id_prueba INTEGER NOT NULL, " +
                    "codigo_parametro TEXT NOT NULL, valor TEXT NOT NULL, UNIQUE(id_prueba, codigo_parametro))"
        )
        db.execSQL(
            "CREATE TABLE historial_estado (" +
                    "id_historial INTEGER PRIMARY KEY AUTOINCREMENT, id_protocolo INTEGER NOT NULL, " +
                    "estado_anterior TEXT, estado_nuevo TEXT NOT NULL, id_usuario INTEGER NOT NULL, " +
                    "fecha_hora TEXT NOT NULL, motivo TEXT DEFAULT '')"
        )
        precargar(db)
    }



    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        borrarTablas(db)
        onCreate(db)
    }

    private fun borrarTablas(db: SQLiteDatabase) {
        listOf(
            "solicitud_acceso", "medicion", "prueba", "protocolo", "visita_participante",
            "recepcion_acta", "visita", "instrumento", "equipo", "tipo_equipo_prueba",
            "tipo_equipo", "instalacion", "usuario", "rol", "tipo_prueba", "historial_estado"
        ).forEach { db.execSQL("DROP TABLE IF EXISTS $it") }
    }

    // ------------------------------------------------------------------ seed

    private fun u(db: SQLiteDatabase, idRol: Int, rut: String, nombre: String, correo: String,
                  empresa: Int? = null, cargo: String = "", giro: String = "") {
        db.execSQL(
            "INSERT INTO usuario(id_rol,rut,nombre,correo,password_hash,id_empresa,cargo,giro) " +
                    "VALUES(?,?,?,?,?,?,?,?)",
            arrayOf<Any>(idRol, rut, nombre, correo, HASH_SEMILLA, empresa ?: -1, cargo, giro)
                .map { if (it == -1) null else it }.toTypedArray()
        )
    }



    private fun precargar(db: SQLiteDatabase) {
        Rol.entries.forEachIndexed { i, r ->
            db.execSQL("INSERT INTO rol VALUES(?,?,?)", arrayOf(i + 1, r.codigo, r.nombre))
        }


        val empresas = listOf(
            Triple("76.482.915-8", "Envasadora del Pacífico S.A.", "Agroindustria"),
            Triple("96.731.540-0", "Sanitaria Valle Central S.A.", "Servicios sanitarios"),
            Triple("77.105.362-9", "Minera Cerro Alto SpA", "Minería"),
            Triple("76.903.218-5", "Clínica Los Aromos Ltda.", "Salud"),
            Triple("99.524.170-6", "Cementos Lomas Blancas S.A.", "Construcción"),
            Triple("78.214.635-1", "Data Center Andes Norte SpA", "Tecnología")
        )
        empresas.forEach { (rut, nombre, giro) ->
            db.execSQL(
                "INSERT INTO usuario(id_rol,rut,nombre,giro,activo) VALUES(4,?,?,?,1)",
                arrayOf(rut, nombre, giro)
            )
        }
        // Personal Induztek
        u(db, 2, "16.482.905-7", "Thomas Peralta S.", "tperalta@induztek.ejemplo")
        u(db, 3, "14.993.210-0", "Joaquín Dupre", "jdupre@induztek.ejemplo")
        u(db, 4, "17.650.348-3", "Víctor Isidro R.", "visidro@envpacifico.ejemplo", 1, "Jefe de Planta")
        u(db, 1, "17.284.519-3", "Matías Godoy Herrera", "mgodoy@induztek.ejemplo")
        u(db, 1, "19.412.087-7", "Javiera Tapia Muñoz", "jtapia@induztek.ejemplo")
        u(db, 1, "26.104.583-4", "Carlos Rondón Pérez", "crondon@induztek.ejemplo")
        u(db, 1, "20.137.648-3", "Diego Valenzuela Ríos", "dvalenzuela@induztek.ejemplo")
        u(db, 2, "15.906.334-8", "Andrea Contreras Silva", "acontreras@induztek.ejemplo")
        u(db, 2, "16.553.270-8", "Sebastián Morales Gutiérrez", "smorales@induztek.ejemplo")
        u(db, 3, "13.871.905-7", "Ricardo Espinoza Lagos", "respinoza@induztek.ejemplo")



        u(db, 4, "14.226.791-8", "Marcela Fuentes Rojas", "mfuentes@envpacifico.ejemplo", 1, "Jefa de Mantenimiento")
        u(db, 4, "18.390.452-3", "Tomás Leiva Araya", "tleiva@losaromos.ejemplo", 4, "Supervisor Eléctrico")
        u(db, 4, "12.645.308-6", "Patricio Navarro Soto", "pnavarro@cerroalto.ejemplo", 3, "Jefe de Mantenimiento Eléctrico")

        val instalaciones = listOf(
            listOf("1", "Planta Lampa", "Sector industrial", "Lampa", "Inducción obligatoria de 30 min"),
            listOf("2", "Planta Elevadora Sur", "Sector rural poniente", "San Bernardo", "Sala eléctrica subterránea, sin señal"),
            listOf("3", "Faena Til Til", "Acceso por Ruta 5 Norte", "Til Til", "Sin señal en toda la faena"),
            listOf("4", "Edificio Clínico", "Zona urbana", "La Florida", "Cortes solo domingos 06:00-10:00"),
            listOf("5", "Planta Molienda", "Sector precordillera", "Puente Alto", "Uso obligatorio de respirador"),
            listOf("6", "Site Huechuraba", "Parque empresarial", "Huechuraba", "Acompañamiento permanente del cliente")
        )
        instalaciones.forEach { (cli, nombre, ubic, comuna, obs) ->
            db.execSQL(
                "INSERT INTO instalacion(id_usuario_cliente,nombre,ubicacion,comuna,observaciones_acceso) VALUES(?,?,?,?,?)",
                arrayOf(cli, nombre, ubic, comuna, obs)
            )
        }

        db.execSQL("INSERT INTO tipo_equipo VALUES(1,'Transformador','PRIMARIO')")
        db.execSQL("INSERT INTO tipo_equipo VALUES(2,'Interruptor','PRIMARIO')")
        db.execSQL("INSERT INTO tipo_equipo VALUES(3,'Celda MT','PRIMARIO')")
        db.execSQL("INSERT INTO tipo_equipo VALUES(4,'Cable de poder','PRIMARIO')")
        db.execSQL("INSERT INTO tipo_equipo VALUES(5,'Relé de protección','SECUNDARIO')")

        db.execSQL("INSERT INTO tipo_prueba VALUES(1,'AISLACION','Resistencia de aislación')")
        db.execSQL("INSERT INTO tipo_prueba VALUES(2,'TTR','Relación de transformación')")
        db.execSQL("INSERT INTO tipo_prueba VALUES(3,'DEVANADOS','Resistencia de devanados')")
        db.execSQL("INSERT INTO tipo_prueba VALUES(4,'CONTACTOS','Resistencia de contactos')")
        db.execSQL("INSERT INTO tipo_prueba VALUES(5,'OPERACION_RELE','Prueba de operación (inyección secundaria)')")


        listOf(1 to 1, 1 to 2, 1 to 3, 2 to 1, 2 to 4, 3 to 1, 3 to 4, 4 to 1, 5 to 5).forEach { (te, tp) ->
            db.execSQL("INSERT INTO tipo_equipo_prueba VALUES(?,?)", arrayOf(te, tp))
        }

        // id, instalacion, tag, tipo, marca, serie, anio, potencia, Vprim, Vsec, corrientes, maxContactos
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
        equipos.forEach { f ->
            db.execSQL(
                "INSERT INTO equipo(id_instalacion,tag,id_tipo_equipo,marca,modelo,numero_serie,anio_fabricacion," +
                        "potencia_kva,tension_primaria_kv,tension_secundaria_kv,corriente_nominal_a,max_contactos_uohm,observaciones) " +
                        "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)",
                arrayOf(
                    f[0], f[1], f[2], f[3], f[4], f[4], f[5],
                    f[6].ifEmpty { null }, f[7].ifEmpty { null }, f[8].ifEmpty { null },
                    f[9].ifEmpty { null }, f[10].ifEmpty { null },
                    when (f[1]) {
                        "REL-50/51-A" -> "Sobrecorriente, ajuste 5 A"
                        "CAB-MT-02" -> "3x240 mm², 180 m"
                        else -> ""
                    }
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
        instrumentos.forEach { f ->
            db.execSQL(
                "INSERT INTO instrumento(nombre,marca,modelo,numero_serie,fecha_ultima_cal,fecha_vence) VALUES(?,?,?,?,?,?)",
                arrayOf(f[0], f[1], f[2], f[3], f[4], f[5])
            )
        }

        precargarHistorial(db)
    }


    private fun precargarHistorial(db: SQLiteDatabase) {
        fun visita(ot: String, inst: Int, fecha: String, ing: Int?, cierre: String?): Long {
            val valores = ContentValues().apply {
                put("numero_ot", ot)
                put("id_instalacion", inst)
                put("fecha", fecha)
                put("tipo_visita", "SOLO_PRUEBAS")
                ing?.let { put("id_ingeniero_cargo", it) }
                cierre?.let { put("fecha_hora_cierre", it) }
            }
            return db.insert("visita", null, valores)
        }

        fun protocolo(id: Long, num: String, idVisita: Int, idEquipo: Int, tec: Int, fh: String,
                      temp: Double, hum: Double, estado: String) {
            db.execSQL(
                "INSERT INTO protocolo(id_protocolo,numero,id_visita,id_equipo,id_usuario_registra,fecha_hora_prueba,temperatura_c,humedad_pct,estado) " +
                        "VALUES(?,?,?,?,?,?,?,?,?)",
                arrayOf(id, num, idVisita, idEquipo, tec, fh, temp, hum, estado)
            )
        }

        fun prueba(idProtocolo: Int, tipo: String, elemento: String, instrumento: Int?,
                   resultado: String, med: Map<String, String>) {
            val valores = ContentValues().apply {
                put("id_protocolo", idProtocolo)
                put("codigo_tipo_prueba", tipo)
                put("elemento", elemento)
                put("realizada", 1)
                instrumento?.let { put("id_instrumento", it) }
                put("resultado", resultado)
            }
            val idPrueba = db.insert("prueba", null, valores).toInt()
            med.forEach { (cod, valor) ->
                db.execSQL(
                    "INSERT INTO medicion(id_prueba,codigo_parametro,valor) VALUES(?,?,?)",
                    arrayOf(idPrueba, cod, valor)
                )
            }
        }


        val v24 = visita("OT-2024-1042", 1, "2024-09-14", 7, "2024-09-14 15:20").toInt()
        db.execSQL(
            "INSERT INTO recepcion_acta VALUES(?,?,?,?,'2024-09-14 15:20')",
            arrayOf(v24, "Marcela Fuentes Rojas", "14.226.791-8", "Jefa de Mantenimiento")
        )
        db.execSQL("INSERT INTO visita_participante VALUES(?,?)", arrayOf(v24, 10))
        protocolo(301, "PT-2024-0312", v24, 1, 10, "2024-09-14 10:30", 18.0, 55.0, "FORMALIZADO")
        prueba(301, "AISLACION", "AT-T", 1, "APROBADO",
            mapOf("TENSION_PRUEBA" to "5", "LECTURA_1MIN" to "12500", "LECTURA_10MIN" to "31250"))
        prueba(301, "TTR", "3 fases / tap 3", 3, "APROBADO",
            mapOf("POSICION_TAP" to "3", "RELACION_TEORICA" to "30", "RELACION_MEDIDA" to "30.08"))
        prueba(301, "DEVANADOS", "AT", 4, "APROBADO", mapOf("RESISTENCIA" to "45.2"))
        prueba(301, "DEVANADOS", "BT", 4, "APROBADO", mapOf("RESISTENCIA" to "2.15"))



        val v25 = visita("OT-2025-1101", 1, "2025-09-13", 7, "2025-09-13 16:05").toInt()
        db.execSQL(
            "INSERT INTO recepcion_acta VALUES(?,?,?,?,'2025-09-13 16:05')",
            arrayOf(v25, "Marcela Fuentes Rojas", "14.226.791-8", "Jefa de Mantenimiento")
        )
        db.execSQL("INSERT INTO visita_participante VALUES(?,?)", arrayOf(v25, 11))
        protocolo(302, "PT-2025-0288", v25, 1, 11, "2025-09-13 11:10", 21.0, 48.0, "FORMALIZADO")
        prueba(302, "AISLACION", "AT-T", 1, "APROBADO",
            mapOf("TENSION_PRUEBA" to "5", "LECTURA_1MIN" to "9800", "LECTURA_10MIN" to "21560"))
        prueba(302, "TTR", "3 fases / tap 3", 3, "APROBADO",
            mapOf("POSICION_TAP" to "3", "RELACION_TEORICA" to "30", "RELACION_MEDIDA" to "30.05"))
        prueba(302, "DEVANADOS", "AT", 4, "APROBADO", mapOf("RESISTENCIA" to "46.8"))
        prueba(302, "DEVANADOS", "BT", 4, "APROBADO", mapOf("RESISTENCIA" to "2.2"))




        val v26 = visita("OT-2026-1187", 1, "2026-09-26", 8, null).toInt()
        db.execSQL("INSERT INTO visita_participante VALUES(?,?)", arrayOf(v26, 7))





        fun cerrada(ot: String, num: String, idProtocolo: Long, inst: Int, idEquipo: Int,
                    tec: Int, dia: String, temp: Double, hum: Double): Int {
            val v = visita(ot, inst, dia, 7, "$dia 16:05").toInt()
            protocolo(idProtocolo, num, v, idEquipo, tec, "$dia 10:30", temp, hum, "FORMALIZADO")
            return v
        }




        cerrada("OT-2025-1112", "PT-2025-0290", 401, 2, 3, 11, "2025-08-05", 22.0, 44.0)
        prueba(401, "AISLACION", "AT-T", 1, "APROBADO",
            mapOf("TENSION_PRUEBA" to "5", "LECTURA_1MIN" to "8200", "LECTURA_10MIN" to "24600"))
        prueba(401, "TTR", "3 fases / cambiador de derivaciones", 3, "APROBADO",
            mapOf("POSICION_TAP" to "2", "RELACION_TEORICA" to "57.5", "RELACION_MEDIDA" to "57.56"))
        prueba(401, "DEVANADOS", "AT-R", 4, "APROBADO", mapOf("RESISTENCIA" to "12.4"))
        prueba(401, "DEVANADOS", "BT-r", 4, "APROBADO", mapOf("RESISTENCIA" to "0.85"))




        val v402 = cerrada("OT-2025-1118", "PT-2025-0291", 402, 3, 5, 12, "2025-08-19", 17.0, 52.0)
        prueba(402, "AISLACION", "MT-BT", 1, "APROBADO",
            mapOf("TENSION_PRUEBA" to "5", "LECTURA_1MIN" to "5200"))
        prueba(402, "CONTACTOS", "Polo 1", 4, "APROBADO", mapOf("RESISTENCIA_CONTACTO" to "38.5"))
        prueba(402, "CONTACTOS", "Polo 2", 4, "APROBADO", mapOf("RESISTENCIA_CONTACTO" to "41.0"))
        prueba(402, "CONTACTOS", "Polo 3", 4, "OBSERVADO", mapOf("RESISTENCIA_CONTACTO" to "52.0"))
        db.execSQL(
            "INSERT INTO recepcion_acta VALUES(?,?,?,?,'2025-08-19 16:05')",
            arrayOf(v402, "Patricio Navarro Soto", "12.645.308-6", "Jefe de Mantenimiento Eléctrico")
        )



        val v403 = cerrada("OT-2025-1124", "PT-2025-0292", 403, 4, 4, 13, "2025-09-02", 20.0, 47.0)
        prueba(403, "AISLACION", "MT-Tierra", 1, "APROBADO",
            mapOf("TENSION_PRUEBA" to "5", "LECTURA_1MIN" to "3100"))
        prueba(403, "CONTACTOS", "Polo 1", 4, "APROBADO", mapOf("RESISTENCIA_CONTACTO" to "44.0"))
        prueba(403, "CONTACTOS", "Polo 2", 4, "APROBADO", mapOf("RESISTENCIA_CONTACTO" to "48.5"))
        prueba(403, "CONTACTOS", "Polo 3", 4, "OBSERVADO", mapOf("RESISTENCIA_CONTACTO" to "67.0"))
        db.execSQL(
            "INSERT INTO recepcion_acta VALUES(?,?,?,?,'2025-09-02 16:05')",
            arrayOf(v403, "Tomás Leiva Araya", "18.390.452-3", "Supervisor Eléctrico")
        )




        cerrada("OT-2025-1131", "PT-2025-0293", 404, 5, 6, 10, "2025-09-09", 15.0, 61.0)
        prueba(404, "OPERACION_RELE", "Disparo de fase", 5, "APROBADO",
            mapOf(
                "CORRIENTE_AJUSTE" to "5", "CORRIENTE_OPERACION" to "5.1",
                "TIEMPO_ESPERADO" to "80", "TIEMPO_MEDIDO" to "82.4"
            ))
        prueba(404, "OPERACION_RELE", "Disparo de neutro", 5, "OBSERVADO",
            mapOf(
                "CORRIENTE_AJUSTE" to "5", "CORRIENTE_OPERACION" to "5.3",
                "TIEMPO_ESPERADO" to "80", "TIEMPO_MEDIDO" to "86.4"
            ))





        cerrada("OT-2025-1137", "PT-2025-0294", 405, 6, 7, 11, "2025-09-16", 23.0, 39.0)
        prueba(405, "AISLACION", "Fase R", 1, "APROBADO",
            mapOf("TENSION_PRUEBA" to "5", "LECTURA_1MIN" to "15200", "LECTURA_10MIN" to "45600"))
        prueba(405, "AISLACION", "Fase S", 1, "APROBADO",
            mapOf("TENSION_PRUEBA" to "5", "LECTURA_1MIN" to "12800", "LECTURA_10MIN" to "39680"))
        prueba(405, "AISLACION", "Fase T", 1, "OBSERVADO",
            mapOf("TENSION_PRUEBA" to "5", "LECTURA_1MIN" to "4200", "LECTURA_10MIN" to "6300"))




        cerrada("OT-2025-1144", "PT-2025-0295", 406, 3, 8, 12, "2025-09-23", 12.0, 66.0)
        prueba(406, "AISLACION", "AT-T", 1, "APROBADO",
            mapOf("TENSION_PRUEBA" to "5", "LECTURA_1MIN" to "21000", "LECTURA_10MIN" to "54600"))
        prueba(406, "TTR", "3 fases / cambiador de derivaciones", 3, "APROBADO",
            mapOf("POSICION_TAP" to "4", "RELACION_TEORICA" to "3.4848", "RELACION_MEDIDA" to "3.49"))
        prueba(406, "DEVANADOS", "AT-R", 4, "APROBADO", mapOf("RESISTENCIA" to "286.0"))
        prueba(406, "DEVANADOS", "BT-r", 4, "APROBADO", mapOf("RESISTENCIA" to "3.9"))




        listOf(2, 3, 4, 5, 6).forEachIndexed { i, inst ->
            visita("OT-2026-${1188 + i}", inst, "2026-09-26", 8, null)
        }
    }

    // ------------------------------------------------------------------ usuarios


    fun buscarUsuarioPorCorreo(correo: String): Usuario? =
        writableDatabase.rawQuery(
            "SELECT u.id_usuario,u.id_rol,u.rut,u.nombre,u.correo,u.telefono,u.activo,r.codigo,u.id_empresa,u.cargo,u.giro " +
                    "FROM usuario u JOIN rol r ON r.id_rol=u.id_rol WHERE lower(u.correo)=lower(?)",
            arrayOf(correo.trim())
        ).use { if (it.moveToFirst()) leerUsuario(it) else null }




    fun verificarClave(correo: String, clave: String): Boolean {
        val hash = writableDatabase.rawQuery(
            "SELECT password_hash FROM usuario WHERE lower(correo)=lower(?)", arrayOf(correo.trim())
        ).use { if (it.moveToFirst()) it.getString(0) else null } ?: return false
        return Seguridad.verificar(clave, hash)
    }



    fun buscarUsuarioPorId(id: Int): Usuario? =
        writableDatabase.rawQuery(
            "SELECT u.id_usuario,u.id_rol,u.rut,u.nombre,u.correo,u.telefono,u.activo,r.codigo,u.id_empresa,u.cargo,u.giro " +
                    "FROM usuario u JOIN rol r ON r.id_rol=u.id_rol WHERE u.id_usuario=?",
            arrayOf(id.toString())
        ).use { if (it.moveToFirst()) leerUsuario(it) else null }



    fun listarPerfilesRapidos(): List<Usuario> =
        writableDatabase.rawQuery(
            "SELECT u.id_usuario,u.id_rol,u.rut,u.nombre,u.correo,u.telefono,u.activo,r.codigo,u.id_empresa,u.cargo,u.giro " +
                    "FROM usuario u JOIN rol r ON r.id_rol=u.id_rol " +
                    "WHERE u.activo=1 AND r.codigo IN ('INGENIERO','JEFE_TECNICO','CLIENTE') " +
                    "AND u.correo IS NOT NULL " +
                    "AND u.id_usuario IN (SELECT MIN(id_usuario) FROM usuario WHERE activo=1 AND correo IS NOT NULL GROUP BY id_rol) " +
                    "ORDER BY u.id_rol",
            null
        ).use { c ->
            buildList { while (c.moveToNext()) add(leerUsuario(c)) }
        }

    // ------------------------------------------------------------------ solicitudes de acceso

    fun correoYaUsado(correo: String): Boolean =
        writableDatabase.rawQuery(
            "SELECT 1 FROM usuario WHERE LOWER(correo) = LOWER(?) " +
                    "UNION " +
                    "SELECT 1 FROM solicitud_acceso WHERE LOWER(sol_correo) = LOWER(?) AND sol_estado = 'PENDIENTE'",
            arrayOf(correo, correo)
        ).use { it.moveToFirst() }



    fun insertarSolicitud(
        nombre: String, apellido: String, pais: String, ciudad: String,
        correo: String, passwordHash: String, justificacion: String
    ): Long {
        val valores = ContentValues().apply {
            put("sol_nombre", nombre)
            put("sol_apellido", apellido)
            put("sol_pais", pais)
            put("sol_ciudad", ciudad)
            put("sol_correo", correo)
            put("sol_password_hash", passwordHash)
            put("sol_justificacion", justificacion)
        }
        return writableDatabase.insert("solicitud_acceso", null, valores)
    }



    private fun leerUsuario(c: Cursor) = Usuario(
        idUsuario = c.getInt(0), idRol = c.getInt(1), rut = c.getString(2), nombre = c.getString(3),
        correo = c.getString(4) ?: "", telefono = c.getString(5) ?: "", activo = c.getInt(6) == 1,
        rol = Rol.entries.first { it.codigo == c.getString(7) },
        idEmpresa = c.getInt(8).takeIf { it > 0 },
        cargo = c.getString(9) ?: "", giro = c.getString(10) ?: ""
    )

    // ------------------------------------------------------------------ equipos

    fun listarEquipos(): List<Equipo> =
        writableDatabase.rawQuery(
            "SELECT e.id_equipo,e.id_instalacion,e.tag,e.id_tipo_equipo,t.nombre,t.clasificacion,e.marca,e.modelo," +
                    "e.numero_serie,e.anio_fabricacion,e.potencia_kva,e.tension_primaria_kv,e.tension_secundaria_kv," +
                    "e.corriente_nominal_a,e.max_contactos_uohm,e.observaciones,i.nombre " +
                    "FROM equipo e JOIN tipo_equipo t ON t.id_tipo_equipo=e.id_tipo_equipo " +
                    "JOIN instalacion i ON i.id_instalacion=e.id_instalacion ORDER BY e.tag", null
        ).use { c -> buildList { while (c.moveToNext()) add(leerEquipo(c)) } }


    fun listarEquiposDeCliente(idEmpresa: Int): List<Equipo> =
        writableDatabase.rawQuery(
            "SELECT e.id_equipo,e.id_instalacion,e.tag,e.id_tipo_equipo,t.nombre,t.clasificacion,e.marca,e.modelo," +
                    "e.numero_serie,e.anio_fabricacion,e.potencia_kva,e.tension_primaria_kv,e.tension_secundaria_kv," +
                    "e.corriente_nominal_a,e.max_contactos_uohm,e.observaciones,i.nombre " +
                    "FROM equipo e JOIN tipo_equipo t ON t.id_tipo_equipo=e.id_tipo_equipo " +
                    "JOIN instalacion i ON i.id_instalacion=e.id_instalacion " +
                    "WHERE i.id_usuario_cliente=? ORDER BY e.tag",
            arrayOf(idEmpresa.toString())
        ).use { c -> buildList { while (c.moveToNext()) add(leerEquipo(c)) } }






    fun equipoPorId(id: Int): Equipo? =
        writableDatabase.rawQuery(
            "SELECT e.id_equipo,e.id_instalacion,e.tag,e.id_tipo_equipo,t.nombre,t.clasificacion,e.marca,e.modelo," +
                    "e.numero_serie,e.anio_fabricacion,e.potencia_kva,e.tension_primaria_kv,e.tension_secundaria_kv," +
                    "e.corriente_nominal_a,e.max_contactos_uohm,e.observaciones,i.nombre " +
                    "FROM equipo e JOIN tipo_equipo t ON t.id_tipo_equipo=e.id_tipo_equipo " +
                    "JOIN instalacion i ON i.id_instalacion=e.id_instalacion WHERE e.id_equipo=?",
            arrayOf(id.toString())
        ).use { if (it.moveToFirst()) leerEquipo(it) else null }



    private fun leerEquipo(c: Cursor) = Equipo(
        idEquipo = c.getInt(0), idInstalacion = c.getInt(1), tag = c.getString(2),
        idTipoEquipo = c.getInt(3), tipoEquipo = c.getString(4), clasificacion = c.getString(5),
        marca = c.getString(6) ?: "", modelo = c.getString(7) ?: "", numeroSerie = c.getString(8) ?: "",
        anioFabricacion = c.getInt(9),
        potenciaKva = c.getDouble(10).takeIf { !c.isNull(10) },
        tensionPrimariaKv = c.getDouble(11).takeIf { !c.isNull(11) },
        tensionSecundariaKv = c.getDouble(12).takeIf { !c.isNull(12) },
        corrienteNominalA = c.getDouble(13).takeIf { !c.isNull(13) },
        maxContactosUohm = c.getDouble(14).takeIf { !c.isNull(14) },
        observaciones = c.getString(15) ?: "", instalacionNombre = c.getString(16) ?: ""
    )


    fun tiposPruebaDeEquipo(idEquipo: Int): List<TipoPrueba> =
        writableDatabase.rawQuery(
            "SELECT tp.codigo FROM tipo_prueba tp " +
                    "JOIN tipo_equipo_prueba x ON x.id_tipo_prueba=tp.id_tipo_prueba " +
                    "JOIN equipo e ON e.id_tipo_equipo=x.id_tipo_equipo WHERE e.id_equipo=?",
            arrayOf(idEquipo.toString())
        ).use { c -> buildList { while (c.moveToNext()) add(TipoPrueba.valueOf(c.getString(0))) } }

    // ------------------------------------------------------------------ instrumentos

    fun listarInstrumentos(): List<Instrumento> =
        writableDatabase.rawQuery(
            "SELECT id_instrumento,nombre,marca,modelo,numero_serie,fecha_ultima_cal,fecha_vence FROM instrumento ORDER BY nombre",
            null
        ).use { c ->
            buildList {
                while (c.moveToNext()) add(
                    Instrumento(c.getInt(0), c.getString(1), c.getString(2), c.getString(3),
                        c.getString(4), c.getString(5), c.getString(6))
                )
            }
        }

    // ------------------------------------------------------------------ protocolos

    fun listarProtocolos(): List<Protocolo> =
        writableDatabase.rawQuery(
            "SELECT p.id_protocolo,p.numero,p.id_visita,p.id_equipo,p.id_usuario_registra,p.fecha_hora_prueba," +
                    "p.temperatura_c,p.humedad_pct,p.estado,p.observaciones,e.tag,t.nombre,i.nombre,u.nombre " +
                    "FROM protocolo p JOIN equipo e ON e.id_equipo=p.id_equipo " +
                    "JOIN tipo_equipo t ON t.id_tipo_equipo=e.id_tipo_equipo " +
                    "JOIN visita v ON v.id_visita=p.id_visita JOIN instalacion i ON i.id_instalacion=v.id_instalacion " +
                    "JOIN usuario u ON u.id_usuario=p.id_usuario_registra ORDER BY p.fecha_hora_prueba DESC", null
        ).use { c -> buildList { while (c.moveToNext()) add(leerProtocolo(c)) } }


    fun listarProtocolosVisitaCliente(idEmpresa: Int): List<Protocolo> =
        writableDatabase.rawQuery(
            "SELECT p.id_protocolo,p.numero,p.id_visita,p.id_equipo,p.id_usuario_registra,p.fecha_hora_prueba," +
                    "p.temperatura_c,p.humedad_pct,p.estado,p.observaciones,e.tag,t.nombre,i.nombre,u.nombre " +
                    "FROM protocolo p JOIN equipo e ON e.id_equipo=p.id_equipo " +
                    "JOIN tipo_equipo t ON t.id_tipo_equipo=e.id_tipo_equipo " +
                    "JOIN visita v ON v.id_visita=p.id_visita JOIN instalacion i ON i.id_instalacion=v.id_instalacion " +
                    "JOIN usuario u ON u.id_usuario=p.id_usuario_registra " +
                    "WHERE i.id_usuario_cliente=? AND p.estado NOT IN ('BORRADOR','ANULADO') " +
                    "ORDER BY p.fecha_hora_prueba DESC",
            arrayOf(idEmpresa.toString())
        ).use { c -> buildList { while (c.moveToNext()) add(leerProtocolo(c)) } }


    fun equipoDeCliente(idEquipo: Int, idEmpresa: Int): Equipo? =
        equipoPorId(idEquipo)?.takeIf { e ->
            writableDatabase.rawQuery(
                "SELECT 1 FROM instalacion WHERE id_instalacion=? AND id_usuario_cliente=?",
                arrayOf(e.idInstalacion.toString(), idEmpresa.toString())
            ).use { it.moveToFirst() }
        }


    fun protocoloPorId(id: Int): Protocolo? =
        listarProtocolos().firstOrNull { it.idProtocolo == id }


    private fun leerProtocolo(c: Cursor) = Protocolo(
        idProtocolo = c.getInt(0), numero = c.getString(1), idVisita = c.getInt(2), idEquipo = c.getInt(3),
        idUsuarioRegistra = c.getInt(4), fechaHoraPrueba = c.getString(5),
        temperaturaC = c.getDouble(6).takeIf { !c.isNull(6) },
        humedadPct = c.getDouble(7).takeIf { !c.isNull(7) },
        estado = EstadoProtocolo.entries.first { it.codigo == c.getString(8) },
        observaciones = c.getString(9) ?: "", equipoTag = c.getString(10),
        tipoEquipo = c.getString(11), instalacionNombre = c.getString(12), tecnicoNombre = c.getString(13) ?: ""
    )


    fun asegurarProtocoloEditable(idEquipo: Int, idUsuario: Int): Protocolo {
        val db = writableDatabase
        val equipo = equipoPorId(idEquipo) ?: error("El equipo no existe en los registros.")
        val rol = rolDe(idUsuario)
        val idVisita = visitaAbierta(db, equipo.idInstalacion) ?: nuevaVisita(db, equipo, idUsuario, rol)

        val existente = db.rawQuery(
            "SELECT id_protocolo, estado, id_usuario_registra FROM protocolo WHERE id_visita=? AND id_equipo=?",
            arrayOf(idVisita.toString(), idEquipo.toString())
        ).use { if (it.moveToFirst()) Triple(it.getInt(0), it.getString(1), it.getInt(2)) else null }
            ?: return nuevoProtocolo(db, idVisita, idEquipo, idUsuario)

        val (idProtocolo, estado, creador) = existente
        if (estado != "BORRADOR" && estado != "OBSERVADO") error(
            when (estado) {
                "REGISTRADO" ->
                    "El equipo ya tiene el protocolo ${numeroDe(idProtocolo)} registrado en esta visita. " +
                            "Para corregirlo, el ingeniero debe devolverlo con observación."
                "FORMALIZADO" ->
                    "El protocolo ${numeroDe(idProtocolo)} está formalizado y no se modifica."
                else ->
                    "El protocolo ${numeroDe(idProtocolo)} está anulado en esta visita."
            }
        )

        if (rol == Rol.TECNICO && creador != idUsuario) error(
            "Este protocolo lo abrió otra persona. Un técnico solo puede editar los protocolos que creó."
        )
        return protocoloPorId(idProtocolo)!!
    }


    fun visitaAbierta(idInstalacion: Int): Int? = visitaAbierta(writableDatabase, idInstalacion)

    private fun visitaAbierta(db: SQLiteDatabase, idInstalacion: Int): Int? = db.rawQuery(
        "SELECT id_visita FROM visita WHERE id_instalacion=? AND fecha_hora_cierre IS NULL " +
                "ORDER BY id_visita DESC LIMIT 1",
        arrayOf(idInstalacion.toString())
    ).use { if (it.moveToFirst()) it.getInt(0) else null }




    private fun nuevaVisita(db: SQLiteDatabase, equipo: Equipo, idUsuario: Int, rol: Rol?): Int {
        val anio = fechaHoy().substring(0, 4)
        val sec = db.rawQuery("SELECT COUNT(*) FROM visita", null)
            .use { if (it.moveToFirst()) it.getInt(0) else 0 } + 1
        return db.insert(
            "visita", null, ContentValues().apply {
                put("numero_ot", "OT-$anio-${1300 + sec}")
                put("id_instalacion", equipo.idInstalacion)
                put("fecha", fechaHoy())
                put("tipo_visita", "SOLO_PRUEBAS")
                if (rol == Rol.INGENIERO) put("id_ingeniero_cargo", idUsuario)
            }
        ).toInt()
    }



    private fun nuevoProtocolo(db: SQLiteDatabase, idVisita: Int, idEquipo: Int, idUsuario: Int): Protocolo {
        val numero = nuevoNumeroProtocolo(db)
        val id = db.insert(
            "protocolo", null, ContentValues().apply {
                put("numero", numero)
                put("id_visita", idVisita)
                put("id_equipo", idEquipo)
                put("id_usuario_registra", idUsuario)
                put("fecha_hora_prueba", fechaHoraAhora())
                put("estado", "BORRADOR")
            }
        ).toInt()
        db.execSQL(
            "INSERT INTO historial_estado(id_protocolo,estado_anterior,estado_nuevo,id_usuario,fecha_hora) VALUES(?,NULL,'BORRADOR',?,?)",
            arrayOf(id, idUsuario, fechaHoraAhora())
        )
        return protocoloPorId(id)!!
    }



    private fun numeroDe(idProtocolo: Int): String = writableDatabase.rawQuery(
        "SELECT numero FROM protocolo WHERE id_protocolo=?", arrayOf(idProtocolo.toString())
    ).use { if (it.moveToFirst()) it.getString(0) else "" }



    private fun rolDe(idUsuario: Int): Rol? =
        buscarUsuarioPorId(idUsuario)?.rol


    private fun nuevoNumeroProtocolo(db: SQLiteDatabase): String {
        val anio = fechaHoy().substring(0, 4)
        val max = db.rawQuery(
            "SELECT MAX(numero) FROM protocolo WHERE numero LIKE 'PT-$anio-%'", null
        ).use { if (it.moveToFirst()) it.getString(0) else null }
        val seq = max?.substringAfterLast("-")?.toIntOrNull() ?: 400
        return "PT-$anio-${seq + 1}"
    }


    // ------------------------------------------------------------------ pruebas

    fun listarPruebas(idProtocolo: Int): List<Prueba> =
        writableDatabase.rawQuery(
            "SELECT id_prueba,id_protocolo,codigo_tipo_prueba,elemento,realizada,motivo_no_realizada," +
                    "id_instrumento,resultado,comentario FROM prueba WHERE id_protocolo=? ORDER BY id_prueba",
            arrayOf(idProtocolo.toString())
        ).use { c ->
            buildList {
                while (c.moveToNext()) {
                    val idPrueba = c.getInt(0)
                    val meds = mutableMapOf<String, Double>()
                    writableDatabase.rawQuery(
                        "SELECT codigo_parametro,valor FROM medicion WHERE id_prueba=?",
                        arrayOf(idPrueba.toString())
                    ).use { m ->
                        while (m.moveToNext()) meds[m.getString(0)] = m.getString(1).toDoubleOrNull() ?: 0.0
                    }
                    add(
                        Prueba(
                            idPrueba = idPrueba, idProtocolo = idProtocolo,
                            tipoPrueba = TipoPrueba.valueOf(c.getString(2)), elemento = c.getString(3) ?: "",
                            realizada = c.getInt(4) == 1, motivoNoRealizada = c.getString(5) ?: "",
                            idInstrumento = c.getInt(6).takeIf { it > 0 },
                            resultado = c.getString(7)?.let { r -> Resultado.entries.first { it.codigo == r } },
                            comentario = c.getString(8) ?: "", mediciones = meds
                        )
                    )
                }
            }
        }



    fun guardarPrueba(idProtocolo: Int, tipo: TipoPrueba, elemento: String, realizada: Boolean,
                      motivo: String, idInstrumento: Int?, resultado: Resultado?, comentario: String,
                      mediciones: Map<String, Double>) {
        val db = writableDatabase
        val existente = db.rawQuery(
            "SELECT id_prueba FROM prueba WHERE id_protocolo=? AND codigo_tipo_prueba=? AND elemento=?",
            arrayOf(idProtocolo.toString(), tipo.name, elemento)
        ).use { if (it.moveToFirst()) it.getInt(0) else null }
        if (existente != null) {
            db.delete("medicion", "id_prueba=?", arrayOf(existente.toString()))
            db.delete("prueba", "id_prueba=?", arrayOf(existente.toString()))
        }
        val idPrueba = db.insert(
            "prueba", null, ContentValues().apply {
                put("id_protocolo", idProtocolo)
                put("codigo_tipo_prueba", tipo.name)
                put("elemento", elemento)
                put("realizada", if (realizada) 1 else 0)
                put("motivo_no_realizada", motivo)
                put("id_instrumento", idInstrumento)
                put("resultado", resultado?.codigo)
                put("comentario", comentario)
            }
        ).toInt()
        if (realizada) mediciones.forEach { (cod, valor) ->
            db.insert(
                "medicion", null, ContentValues().apply {
                    put("id_prueba", idPrueba)
                    put("codigo_parametro", cod)
                    put("valor", valor.toString())
                }
            )
        }
    }

    fun valorAnterior(idEquipo: Int, tipo: TipoPrueba, codigoParametro: String, excluirProtocolo: Int): Double? =
        writableDatabase.rawQuery(
            "SELECT m.valor FROM medicion m " +
                    "JOIN prueba p ON p.id_prueba=m.id_prueba " +
                    "JOIN protocolo pr ON pr.id_protocolo=p.id_protocolo " +
                    "WHERE pr.id_equipo=? AND p.codigo_tipo_prueba=? AND m.codigo_parametro=? " +
                    "AND p.realizada=1 AND pr.id_protocolo!=? AND pr.estado!='ANULADO' " +
                    "ORDER BY pr.fecha_hora_prueba DESC LIMIT 1",
            arrayOf(idEquipo.toString(), tipo.name, codigoParametro, excluirProtocolo.toString())
        ).use { if (it.moveToFirst()) it.getString(0).toDoubleOrNull() else null }


    fun valoresHistoricos(idEquipo: Int, tipo: TipoPrueba, codigoParametro: String, excluirProtocolo: Int): List<String> =
        writableDatabase.rawQuery(
            "SELECT DISTINCT m.valor FROM medicion m " +
                    "JOIN prueba p ON p.id_prueba=m.id_prueba " +
                    "JOIN protocolo pr ON pr.id_protocolo=p.id_protocolo " +
                    "WHERE pr.id_equipo=? AND p.codigo_tipo_prueba=? AND m.codigo_parametro=? " +
                    "AND p.realizada=1 AND pr.id_protocolo!=? AND pr.estado!='ANULADO' " +
                    "ORDER BY m.valor DESC LIMIT 6",
            arrayOf(idEquipo.toString(), tipo.name, codigoParametro, excluirProtocolo.toString())
        ).use { c -> buildList { while (c.moveToNext()) add(c.getString(0).removeSuffix(".0")) } }


    fun historialAislacion(idEquipo: Int): List<Triple<String, String, Double>> =
        writableDatabase.rawQuery(
            "SELECT pr.numero, substr(pr.fecha_hora_prueba,1,10), m.valor FROM medicion m " +
                    "JOIN prueba p ON p.id_prueba=m.id_prueba JOIN protocolo pr ON pr.id_protocolo=p.id_protocolo " +
                    "WHERE pr.id_equipo=? AND p.codigo_tipo_prueba='AISLACION' AND m.codigo_parametro='LECTURA_1MIN' " +
                    "AND pr.estado NOT IN ('BORRADOR','ANULADO') ORDER BY pr.fecha_hora_prueba",
            arrayOf(idEquipo.toString())
        ).use { c ->
            buildList { while (c.moveToNext()) add(Triple(c.getString(0), c.getString(1), c.getString(2).toDouble())) }
        }



    // ------------------------------------------------------------------ estados



    fun cambiarEstado(idProtocolo: Int, nuevo: EstadoProtocolo, idUsuario: Int, motivo: String = "") {
        val db = writableDatabase
        val actual = db.rawQuery(
            "SELECT estado FROM protocolo WHERE id_protocolo=?", arrayOf(idProtocolo.toString())
        ).use { if (it.moveToFirst()) it.getString(0) else null } ?: return
        val desde = EstadoProtocolo.entries.first { it.codigo == actual }
        val permiso = transicionPermitida(desde, nuevo, rolDe(idUsuario))
        if (!permiso.first) error(permiso.second)

        if (motivo.isBlank() && (nuevo == EstadoProtocolo.OBSERVADO || nuevo == EstadoProtocolo.ANULADO))
            error("El ${nuevo.nombre.lowercase()} requiere escribir un motivo.")
        db.execSQL(
            "UPDATE protocolo SET estado=? WHERE id_protocolo=?", arrayOf(nuevo.codigo, idProtocolo.toString())
        )
        db.execSQL(
            "INSERT INTO historial_estado(id_protocolo,estado_anterior,estado_nuevo,id_usuario,fecha_hora,motivo) VALUES(?,?,?,?,?,?)",
            arrayOf(idProtocolo, actual, nuevo.codigo, idUsuario, fechaHoraAhora(), motivo)
        )
    }




    private fun transicionPermitida(desde: EstadoProtocolo, hacia: EstadoProtocolo, rol: Rol?): Pair<Boolean, String> {
        val terreno = rol == Rol.TECNICO || rol == Rol.INGENIERO
        val oficina = rol == Rol.INGENIERO || rol == Rol.JEFE_TECNICO
        return when {
            desde == EstadoProtocolo.FORMALIZADO ->
                false to "Un protocolo formalizado no se modifica ni se anula."
            hacia == EstadoProtocolo.REGISTRADO &&
                    (desde == EstadoProtocolo.BORRADOR || desde == EstadoProtocolo.OBSERVADO) ->
                if (terreno) true to "" else false to "Solo un técnico o un ingeniero pueden registrar en terreno."
            hacia == EstadoProtocolo.FORMALIZADO && desde == EstadoProtocolo.REGISTRADO ->
                if (oficina) true to "" else false to "Solo el ingeniero o el jefe técnico pueden formalizar."
            hacia == EstadoProtocolo.OBSERVADO && desde == EstadoProtocolo.REGISTRADO ->
                if (oficina) true to "" else false to "Solo el ingeniero o el jefe técnico devuelven con observación."
            hacia == EstadoProtocolo.ANULADO ->
                if (oficina) true to "" else false to "Solo el ingeniero o el jefe técnico pueden anular."
            else -> false to "La transición ${desde.nombre} → ${hacia.nombre} no existe."
        }
    }


    fun ultimoMotivo(idProtocolo: Int): String =
        writableDatabase.rawQuery(
            "SELECT motivo FROM historial_estado WHERE id_protocolo=? AND estado_nuevo='OBSERVADO' " +
                    "ORDER BY id_historial DESC LIMIT 1",
            arrayOf(idProtocolo.toString())
        ).use { if (it.moveToFirst()) it.getString(0) ?: "" else "" }



    fun guardarAmbientes(idProtocolo: Int, temp: Double, hum: Double, observaciones: String = "") {
        writableDatabase.execSQL(
            "UPDATE protocolo SET temperatura_c=?, humedad_pct=?, observaciones=? WHERE id_protocolo=?",
            arrayOf(temp, hum, observaciones, idProtocolo.toString())
        )
    }


    fun guardarObservacionesProtocolo(idProtocolo: Int, texto: String) {
        writableDatabase.execSQL(
            "UPDATE protocolo SET observaciones=? WHERE id_protocolo=?", arrayOf(texto, idProtocolo.toString())
        )
    }

    // ------------------------------------------------------------------ visita / acta

    fun visitaPorId(id: Int): Visita? =
        writableDatabase.rawQuery(
            "SELECT id_visita,numero_ot,id_instalacion,fecha,tipo_visita,id_ingeniero_cargo,fecha_hora_cierre FROM visita WHERE id_visita=?",
            arrayOf(id.toString())
        ).use { c ->
            if (!c.moveToFirst()) null else Visita(
                c.getInt(0), c.getString(1), c.getInt(2), c.getString(3), c.getString(4),
                c.getInt(5).takeIf { it > 0 }, c.getString(6)
            )
        }



    fun instalacionPorId(id: Int): Instalacion? =
        writableDatabase.rawQuery(
            "SELECT i.id_instalacion,i.id_usuario_cliente,i.nombre,i.ubicacion,i.comuna,i.observaciones_acceso,u.nombre " +
                    "FROM instalacion i JOIN usuario u ON u.id_usuario=i.id_usuario_cliente WHERE i.id_instalacion=?",
            arrayOf(id.toString())
        ).use { c ->
            if (!c.moveToFirst()) null else Instalacion(
                c.getInt(0), c.getInt(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6)
            )
        }


    fun protocolosDeVisita(idVisita: Int): List<Protocolo> =
        listarProtocolos().filter { it.idVisita == idVisita }

    fun protocolosDeVisitaCliente(idVisita: Int, idEmpresa: Int): List<Protocolo> {
        val esDeLaEmpresa = writableDatabase.rawQuery(
            "SELECT 1 FROM visita v JOIN instalacion i ON i.id_instalacion=v.id_instalacion " +
                    "WHERE v.id_visita=? AND i.id_usuario_cliente=?",
            arrayOf(idVisita.toString(), idEmpresa.toString())
        ).use { it.moveToFirst() }
        return if (esDeLaEmpresa) protocolosDeVisita(idVisita) else emptyList()
    }


    fun recepcionDeVisita(idVisita: Int): RecepcionActa? =
        writableDatabase.rawQuery(
            "SELECT receptor_nombre," +
                    "receptor_rut," +
                    "receptor_cargo," +
                    "fecha_hora_recepcion FROM recepcion_acta " +
                    "WHERE id_visita=?",
            arrayOf(idVisita.toString())
        ).use { c ->
            if (!c.moveToFirst()) null
            else RecepcionActa(c.getString(0), c.getString(1), c.getString(2), c.getString(3))
        }


    fun cerrarVisita(idVisita: Int, nombre: String, rut: String, cargo: String) {
        val db = writableDatabase
        val pendientes = protocolosDeVisita(idVisita)
            .filter { it.estado != EstadoProtocolo.REGISTRADO && it.estado != EstadoProtocolo.ANULADO }
        if (pendientes.isNotEmpty()) error(
            "Faltan protocolos por registrar en terreno: " +
                    pendientes.joinToString { "${it.equipoTag} (${it.estado.nombre.lowercase()})" } + "."
        )
        val ahora = fechaHoraAhora()
        db.execSQL(
            "INSERT OR REPLACE INTO recepcion_acta VALUES(?,?,?,?,?)",
            arrayOf(idVisita, nombre, rut, cargo, ahora)
        )
        db.execSQL(
            "UPDATE visita SET fecha_hora_cierre=? WHERE id_visita=?", arrayOf(ahora, idVisita.toString())
        )
    }
}





/*

class InduztekDbHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    companion object {
        private const val NOMBRE_BD = "induztek.db"
        private const val VERSION_BD = 2

        //Tabla de los roles
        //El reloj
        private const val TABLA_ROLES = "rol"
        private const val ROL_ID = "id_rol"
        private const val ROL_CODIGO = "codigo_rol"
        private const val ROL_NOMBRE = "nombre_rol"

        //Tabla d usuarios
        private const val TABLA_USUARIOS = "usuario"
        private const val USU_ID = "id_usuario"
        private const val USU_ROL = "id_rol"
        private const val USU_RUT = "rut"
        private const val USU_NOMBRE = "nombre"
        private const val USU_CORREO = "correo"
        private const val USU_HASH = "password_hash"
        private const val USU_ACTIVO = "activo"

        //Tabla solicitud d acceso
        private const val TABLA_SOLICITUDES = "solicitud_acceso"
        private const val SOL_ID = "id_solicitud"
        private const val SOL_NOMBRE = "sol_nombre"
        private const val SOL_APELLIDO = "sol_apellido"
        private const val SOL_PAIS = "sol_pais"
        private const val SOL_CIUDAD = "sol_ciudad"
        private const val SOL_CORREO = "sol_correo"
        private const val SOL_HASH = "sol_password_hash"
        private const val SOL_JUSTIFICACION = "sol_justificacion"
        private const val SOL_ESTADO = "sol_estado"
        const val CLAVE_DEMO = "Induztek2026"

        private const val SQL_CREAR_ROLES =
            "CREATE TABLE $TABLA_ROLES (" +
                    "$ROL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$ROL_CODIGO TEXT NOT NULL UNIQUE, " +
                    "$ROL_NOMBRE TEXT NOT NULL)"

        private const val SQL_CREAR_USUARIOS =
            "CREATE TABLE $TABLA_USUARIOS (" +
                    "$USU_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$USU_ROL INTEGER NOT NULL REFERENCES $TABLA_ROLES($ROL_ID), " +
                    "$USU_RUT TEXT NOT NULL UNIQUE, " +
                    "$USU_NOMBRE TEXT NOT NULL, " +
                    "$USU_CORREO TEXT NOT NULL UNIQUE, " +
                    "$USU_HASH TEXT NOT NULL, " +
                    "$USU_ACTIVO INTEGER NOT NULL DEFAULT 1)"

        private const val SQL_CREAR_SOLICITUDES =
            "CREATE TABLE $TABLA_SOLICITUDES (" +
                    "$SOL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$SOL_NOMBRE TEXT NOT NULL, " +
                    "$SOL_APELLIDO TEXT NOT NULL, " +
                    "$SOL_PAIS TEXT NOT NULL, " +
                    "$SOL_CIUDAD TEXT NOT NULL, " +
                    "$SOL_CORREO TEXT NOT NULL, " +
                    "$SOL_HASH TEXT NOT NULL, " +
                    "$SOL_JUSTIFICACION TEXT NOT NULL, " +
                    "$SOL_ESTADO TEXT NOT NULL DEFAULT 'PENDIENTE')"




        private const val SQL_PRECARGAR_ROLES =
            "INSERT INTO $TABLA_ROLES ($ROL_ID, $ROL_CODIGO, $ROL_NOMBRE) VALUES " +
                    "(1, 'TECNICO', 'Técnico de terreno'), " +
                    "(2, 'INGENIERO', 'Ingeniero en pruebas'), " +      // CAMBIO: nombres como en tu diseño
                    "(3, 'JEFE_TECNICO', 'Jefe técnico'), " +
                    "(4, 'CLIENTE', 'Cliente / Visitante')"

        private const val SQL_SELECT_USUARIOS =
            "SELECT u.*, r.$ROL_CODIGO AS $ROL_CODIGO, r.$ROL_NOMBRE AS $ROL_NOMBRE " +   // CAMBIO: ahora trae también nombre_rol
                    "FROM $TABLA_USUARIOS u " +
                    "INNER JOIN $TABLA_ROLES r ON u.$USU_ROL = r.$ROL_ID"


        @Volatile
        private var instancia: InduztekDbHelper? = null

        fun obtener(context: Context): InduztekDbHelper =
            instancia ?: synchronized(this) {
                instancia ?: InduztekDbHelper(context.applicationContext).also { instancia = it }
            }
    }


    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREAR_ROLES)
        db.execSQL(SQL_CREAR_USUARIOS)
        db.execSQL(SQL_CREAR_SOLICITUDES)
        db.execSQL(SQL_PRECARGAR_ROLES)


        val hash = Seguridad.hashear(CLAVE_DEMO)


        precargarUsuario(db, 1, "17.284.519-3", "Matías Godoy Herrera", "mgodoy@induztek.ejemplo", hash)
        precargarUsuario(db, 2, "16.482.905-7", "Thomas Peralta S.", "tperalta@induztek.ejemplo", hash)
        precargarUsuario(db, 3, "14.993.210-0", "Joaquín Dupre", "jdupre@induztek.ejemplo", hash)
        precargarUsuario(db, 4, "17.650.348-3", "Víctor Isidro R.", "visidro@envpacifico.ejemplo", hash)
        precargarUsuario(db, 4, "14.226.791-8", "Marcela Fuentes Rojas", "mfuentes@envpacifico.ejemplo", hash)
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLA_SOLICITUDES")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_USUARIOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_ROLES")
        onCreate(db)
    }

    private fun precargarUsuario(
        db: SQLiteDatabase, rolId: Long, rut: String, nombre: String, correo: String, hash: String
    ) {
        val valores = ContentValues().apply {
            put(USU_ROL, rolId)
            put(USU_RUT, rut)
            put(USU_NOMBRE, nombre)
            put(USU_CORREO, correo)
            put(USU_HASH, hash)
            put(USU_ACTIVO, 1)
        }
        db.insert(TABLA_USUARIOS, null, valores)
    }

    // ---------------------------- USUARIOS ---------------------------


    fun buscarUsuarioPorCorreo(correo: String): Usuario? =
        readableDatabase.rawQuery(
            "$SQL_SELECT_USUARIOS WHERE LOWER(u.$USU_CORREO) = LOWER(?)", arrayOf(correo)
        ).use { cursor ->
            if (cursor.moveToFirst()) leerUsuario(cursor) else null
        }


    fun buscarUsuarioPorId(id: Long): Usuario? =
        readableDatabase.rawQuery(
            "$SQL_SELECT_USUARIOS WHERE u.$USU_ID = ?", arrayOf(id.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) leerUsuario(cursor) else null
        }


    fun listarPerfilesRapidos(): List<Usuario> {
        val lista = mutableListOf<Usuario>()
        readableDatabase.rawQuery(
            "$SQL_SELECT_USUARIOS WHERE u.$USU_ACTIVO = 1 " +
                    "AND r.$ROL_CODIGO IN ('INGENIERO', 'JEFE_TECNICO', 'CLIENTE') " +
                    "AND u.$USU_ID IN (SELECT MIN($USU_ID) FROM $TABLA_USUARIOS WHERE $USU_ACTIVO = 1 GROUP BY $USU_ROL) " +
                    "ORDER BY u.$USU_ROL",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) lista.add(leerUsuario(cursor))
        }
        return lista
    }

    // -------------------- SOLICITUDES ------------------------

    fun correoYaUsado(correo: String): Boolean {
        readableDatabase.rawQuery(
            "SELECT 1 FROM $TABLA_USUARIOS WHERE LOWER($USU_CORREO) = LOWER(?) " +
                    "UNION " +
                    "SELECT 1 FROM $TABLA_SOLICITUDES WHERE LOWER($SOL_CORREO) = LOWER(?) AND $SOL_ESTADO = 'PENDIENTE'",
            arrayOf(correo, correo)
        ).use { cursor -> return cursor.moveToFirst() }
    }


    fun insertarSolicitud(
        nombre: String, apellido: String, pais: String, ciudad: String,
        correo: String, passwordHash: String, justificacion: String
    ): Long {
        val valores = ContentValues().apply {
            put(SOL_NOMBRE, nombre)
            put(SOL_APELLIDO, apellido)
            put(SOL_PAIS, pais)
            put(SOL_CIUDAD, ciudad)
            put(SOL_CORREO, correo)
            put(SOL_HASH, passwordHash)
            put(SOL_JUSTIFICACION, justificacion)
        }
        return writableDatabase.insert(TABLA_SOLICITUDES, null, valores)
    }





    private fun leerUsuario(cursor: Cursor) = Usuario(
        id = cursor.getLong(cursor.getColumnIndexOrThrow(USU_ID)),
        rolId = cursor.getLong(cursor.getColumnIndexOrThrow(USU_ROL)),
        rolCodigo = cursor.getString(cursor.getColumnIndexOrThrow(ROL_CODIGO)),
        rolNombre = cursor.getString(cursor.getColumnIndexOrThrow(ROL_NOMBRE)),
        rut = cursor.getString(cursor.getColumnIndexOrThrow(USU_RUT)),
        nombre = cursor.getString(cursor.getColumnIndexOrThrow(USU_NOMBRE)),
        correo = cursor.getString(cursor.getColumnIndexOrThrow(USU_CORREO)),
        passwordHash = cursor.getString(cursor.getColumnIndexOrThrow(USU_HASH)),
        activo = cursor.getInt(cursor.getColumnIndexOrThrow(USU_ACTIVO)) == 1
    )


}

*/


