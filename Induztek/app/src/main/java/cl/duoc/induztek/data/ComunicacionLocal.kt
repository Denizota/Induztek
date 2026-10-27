
package cl.duoc.induztek.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class MensajeLocal(
    val id: Long,
    val remitenteId: Int,
    val destinatarioId: Int,
    val texto: String,
    val fecha: String
)

data class ValidacionLocal(
    val id: Long,
    val usuarioId: Int,
    val responsable: String,
    val observaciones: String,
    val fecha: String
)

/** Datos SOLO en el teléfono. No es un chat en línea ni envía notificaciones. */
class ComunicacionLocal(context: Context) :
    SQLiteOpenHelper(
        context.applicationContext,
        "colaboracion_local.db",
        null,
        1
    ) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE mensaje (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                remitente_id INTEGER NOT NULL,
                destinatario_id INTEGER NOT NULL,
                texto TEXT NOT NULL,
                fecha TEXT NOT NULL
            )"""
        )

        db.execSQL(
            """CREATE TABLE validacion_local (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                usuario_id INTEGER NOT NULL,
                responsable TEXT NOT NULL,
                observaciones TEXT NOT NULL,
                fecha TEXT NOT NULL
            )"""
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        // No se elimina información existente.
    }

    fun mensajes(
        usuarioId: Int,
        contactoId: Int
    ): List<MensajeLocal> =
        readableDatabase.rawQuery(
            """SELECT id, remitente_id, destinatario_id, texto, fecha
               FROM mensaje
               WHERE (remitente_id = ? AND destinatario_id = ?)
                  OR (remitente_id = ? AND destinatario_id = ?)
               ORDER BY id""",
            arrayOf(
                usuarioId.toString(),
                contactoId.toString(),
                contactoId.toString(),
                usuarioId.toString()
            )
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        MensajeLocal(
                            id = cursor.getLong(0),
                            remitenteId = cursor.getInt(1),
                            destinatarioId = cursor.getInt(2),
                            texto = cursor.getString(3),
                            fecha = cursor.getString(4)
                        )
                    )
                }
            }
        }

    fun enviar(
        usuarioId: Int,
        contactoId: Int,
        texto: String
    ): Boolean {
        val limpio = texto.trim()

        if (
            usuarioId == contactoId ||
            limpio.isEmpty() ||
            limpio.length > 1000
        ) {
            return false
        }

        val datos = ContentValues().apply {
            put("remitente_id", usuarioId)
            put("destinatario_id", contactoId)
            put("texto", limpio)
            put("fecha", fechaHoraAhora())
        }

        return writableDatabase.insert(
            "mensaje",
            null,
            datos
        ) > 0
    }

    fun ultimaConversacion(
        usuarioId: Int,
        contactoId: Int
    ): MensajeLocal? =
        mensajes(usuarioId, contactoId).lastOrNull()

    fun guardarValidacion(
        usuarioId: Int,
        responsable: String,
        observaciones: String
    ): Boolean {
        if (responsable.isBlank()) return false

        val datos = ContentValues().apply {
            put("usuario_id", usuarioId)
            put("responsable", responsable.trim())
            put("observaciones", observaciones.trim())
            put("fecha", fechaHoraAhora())
        }

        return writableDatabase.insert(
            "validacion_local",
            null,
            datos
        ) > 0
    }

    fun validaciones(usuarioId: Int): List<ValidacionLocal> =
        readableDatabase.rawQuery(
            """SELECT id, usuario_id, responsable, observaciones, fecha
               FROM validacion_local
               WHERE usuario_id = ?
               ORDER BY id DESC""",
            arrayOf(usuarioId.toString())
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        ValidacionLocal(
                            id = cursor.getLong(0),
                            usuarioId = cursor.getInt(1),
                            responsable = cursor.getString(2),
                            observaciones = cursor.getString(3),
                            fecha = cursor.getString(4)
                        )
                    )
                }
            }
        }
}
