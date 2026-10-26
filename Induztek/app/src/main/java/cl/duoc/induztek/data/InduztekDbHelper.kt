package cl.duoc.induztek.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import cl.duoc.induztek.model.*



class InduztekDbHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    companion object {
        private const val NOMBRE_BD = "induztek.db"
        private const val VERSION_BD = 2
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

    //porsiaca
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
            "CREATE TABLE tipo_prueba (" +
                    "id_tipo_prueba INTEGER PRIMARY KEY, codigo TEXT UNIQUE NOT NULL, nombre TEXT NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE tipo_equipo_prueba (" +
                    "id_tipo_equipo INTEGER NOT NULL, id_tipo_prueba INTEGER NOT NULL, " +
                    "PRIMARY KEY(id_tipo_equipo, id_tipo_prueba))"
        )
        precargar(db)
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        borrarTablas(db)
        onCreate(db)
    }


    private fun borrarTablas(db: SQLiteDatabase) {
        listOf(
            "tipo_equipo_prueba", "tipo_prueba", "instrumento", "equipo",
            "tipo_equipo", "instalacion", "solicitud_acceso", "usuario", "rol"
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
        // Empresas cliente
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
        // client
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


