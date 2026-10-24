package cl.duoc.induztek.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import cl.duoc.induztek.model.Usuario




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
        rolNombre = cursor.getString(cursor.getColumnIndexOrThrow(ROL_NOMBRE)),   // CAMBIO
        rut = cursor.getString(cursor.getColumnIndexOrThrow(USU_RUT)),
        nombre = cursor.getString(cursor.getColumnIndexOrThrow(USU_NOMBRE)),
        correo = cursor.getString(cursor.getColumnIndexOrThrow(USU_CORREO)),
        passwordHash = cursor.getString(cursor.getColumnIndexOrThrow(USU_HASH)),
        activo = cursor.getInt(cursor.getColumnIndexOrThrow(USU_ACTIVO)) == 1
    )

}