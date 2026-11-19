
package cl.duoc.induztek.data

import android.database.sqlite.SQLiteDatabase

internal val TABLAS = listOf(
    "solicitud_acceso", "medicion", "prueba", "protocolo", "visita_participante",
    "recepcion_acta", "visita", "instrumento", "equipo", "tipo_equipo_prueba",
    "tipo_equipo", "instalacion", "usuario", "rol", "tipo_prueba", "historial_estado"
)

internal fun crearTablas(bd: SQLiteDatabase) {
    bd.execSQL(
        "CREATE TABLE rol (" +
                "id_rol INTEGER PRIMARY KEY, codigo TEXT UNIQUE NOT NULL, nombre TEXT NOT NULL)"
    )
    bd.execSQL(
        "CREATE TABLE usuario (" +
                "id_usuario INTEGER PRIMARY KEY AUTOINCREMENT, id_rol INTEGER NOT NULL, " +
                "rut TEXT UNIQUE NOT NULL, nombre TEXT NOT NULL, correo TEXT UNIQUE, " +
                "telefono TEXT DEFAULT '', password_hash TEXT, activo INTEGER DEFAULT 1, " +
                "id_empresa INTEGER, cargo TEXT DEFAULT '', giro TEXT DEFAULT '', " +
                "bio TEXT DEFAULT '', especialidades TEXT DEFAULT '', " +
                "formacion TEXT DEFAULT '', certificaciones TEXT DEFAULT '', " +
                "desde INTEGER, calificacion INTEGER)"
    )
    bd.execSQL(
        "CREATE TABLE solicitud_acceso (" +
                "id_solicitud INTEGER PRIMARY KEY AUTOINCREMENT, sol_nombre TEXT NOT NULL, " +
                "sol_apellido TEXT NOT NULL, sol_pais TEXT NOT NULL, sol_ciudad TEXT NOT NULL, " +
                "sol_correo TEXT NOT NULL, sol_password_hash TEXT NOT NULL, " +
                "sol_justificacion TEXT NOT NULL, sol_estado TEXT NOT NULL DEFAULT 'PENDIENTE', " +
                "sol_tipo TEXT NOT NULL DEFAULT 'INGRESO', sol_id_usuario INTEGER, " +
                "sol_campo TEXT NOT NULL DEFAULT '', sol_valor TEXT NOT NULL DEFAULT '')"
    )
    bd.execSQL(
        "CREATE TABLE instalacion (" +
                "id_instalacion INTEGER PRIMARY KEY AUTOINCREMENT, id_usuario_cliente INTEGER NOT NULL, " +
                "nombre TEXT NOT NULL, ubicacion TEXT DEFAULT '', comuna TEXT DEFAULT '', " +
                "observaciones_acceso TEXT DEFAULT '')"
    )
    bd.execSQL(
        "CREATE TABLE tipo_equipo (" +
                "id_tipo_equipo INTEGER PRIMARY KEY, nombre TEXT UNIQUE NOT NULL, " +
                "clasificacion TEXT NOT NULL)"
    )
    bd.execSQL(
        "CREATE TABLE equipo (" +
                "id_equipo INTEGER PRIMARY KEY AUTOINCREMENT, id_instalacion INTEGER NOT NULL, " +
                "tag TEXT NOT NULL, id_tipo_equipo INTEGER NOT NULL, marca TEXT DEFAULT '', " +
                "modelo TEXT DEFAULT '', numero_serie TEXT DEFAULT '', anio_fabricacion INTEGER, " +
                "potencia_kva REAL, tension_primaria_kv REAL, tension_secundaria_kv REAL, " +
                "corriente_nominal_a REAL, max_contactos_uohm REAL, observaciones TEXT DEFAULT '', " +
                "UNIQUE(id_instalacion, tag))"
    )
    bd.execSQL(
        "CREATE TABLE instrumento (" +
                "id_instrumento INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL, " +
                "marca TEXT DEFAULT '', modelo TEXT DEFAULT '', numero_serie TEXT UNIQUE, " +
                "fecha_ultima_cal TEXT, fecha_vence TEXT)"
    )
    bd.execSQL(
        "CREATE TABLE visita (" +
                "id_visita INTEGER PRIMARY KEY AUTOINCREMENT, numero_ot TEXT UNIQUE NOT NULL, " +
                "id_instalacion INTEGER NOT NULL, fecha TEXT NOT NULL, tipo_visita TEXT DEFAULT 'SOLO_PRUEBAS', " +
                "id_ingeniero_cargo INTEGER, fecha_hora_cierre TEXT, descripcion_mantenimiento TEXT DEFAULT '')"
    )
    bd.execSQL(
        "CREATE TABLE visita_participante (id_visita INTEGER NOT NULL, id_usuario INTEGER NOT NULL, " +
                "PRIMARY KEY(id_visita, id_usuario))"
    )
    bd.execSQL(
        "CREATE TABLE recepcion_acta (" +
                "id_visita INTEGER PRIMARY KEY, receptor_nombre TEXT NOT NULL, receptor_rut TEXT NOT NULL, " +
                "receptor_cargo TEXT DEFAULT '', fecha_hora_recepcion TEXT NOT NULL)"
    )
    bd.execSQL(
        "CREATE TABLE protocolo (" +
                "id_protocolo INTEGER PRIMARY KEY AUTOINCREMENT, numero TEXT UNIQUE NOT NULL, " +
                "id_visita INTEGER NOT NULL, id_equipo INTEGER NOT NULL, id_usuario_registra INTEGER NOT NULL, " +
                "fecha_hora_prueba TEXT NOT NULL, temperatura_c REAL, humedad_pct REAL, " +
                "estado TEXT NOT NULL DEFAULT 'BORRADOR', observaciones TEXT DEFAULT '', " +
                "UNIQUE(id_visita, id_equipo))"
    )
    bd.execSQL(
        "CREATE TABLE tipo_prueba (" +
                "id_tipo_prueba INTEGER PRIMARY KEY, codigo TEXT UNIQUE NOT NULL, nombre TEXT NOT NULL)"
    )
    bd.execSQL(
        "CREATE TABLE tipo_equipo_prueba (" +
                "id_tipo_equipo INTEGER NOT NULL, id_tipo_prueba INTEGER NOT NULL, " +
                "PRIMARY KEY(id_tipo_equipo, id_tipo_prueba))"
    )
    bd.execSQL(
        "CREATE TABLE prueba (" +
                "id_prueba INTEGER PRIMARY KEY AUTOINCREMENT, id_protocolo INTEGER NOT NULL, " +
                "codigo_tipo_prueba TEXT NOT NULL, elemento TEXT DEFAULT '', momento TEXT DEFAULT 'UNICA', " +
                "realizada INTEGER DEFAULT 1, motivo_no_realizada TEXT DEFAULT '', " +
                "id_instrumento INTEGER, resultado TEXT, comentario TEXT DEFAULT '')"
    )
    bd.execSQL(
        "CREATE TABLE medicion (" +
                "id_medicion INTEGER PRIMARY KEY AUTOINCREMENT, id_prueba INTEGER NOT NULL, " +
                "codigo_parametro TEXT NOT NULL, valor TEXT NOT NULL, UNIQUE(id_prueba, codigo_parametro))"
    )
    bd.execSQL(
        "CREATE TABLE historial_estado (" +
                "id_historial INTEGER PRIMARY KEY AUTOINCREMENT, id_protocolo INTEGER NOT NULL, " +
                "estado_anterior TEXT, estado_nuevo TEXT NOT NULL, id_usuario INTEGER NOT NULL, " +
                "fecha_hora TEXT NOT NULL, motivo TEXT DEFAULT '')"
    )
}
