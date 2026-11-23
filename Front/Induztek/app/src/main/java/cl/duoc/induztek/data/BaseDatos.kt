
package cl.duoc.induztek.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class InduztekDbHelper private constructor(contexto: Context) :
    SQLiteOpenHelper(contexto, NOMBRE_BD, null, VERSION_BD) {

    companion object {
        private const val NOMBRE_BD = "induztek.db"
        private const val VERSION_BD = 8

        const val CLAVE_DEMO = "Induztek2026"

        private var instancia: InduztekDbHelper? = null

        fun obtener(contexto: Context): InduztekDbHelper {
            var abierta = instancia
            if (abierta == null) {
                abierta = InduztekDbHelper(contexto.applicationContext)
                instancia = abierta
            }
            return abierta
        }

        var generacion = 0L
            private set
    }

    override fun onCreate(bd: SQLiteDatabase) {
        crearTablas(bd)
        precargar(bd)
    }

    override fun onUpgrade(bd: SQLiteDatabase, versionVieja: Int, versionNueva: Int) {
        if (versionVieja < 6) {
            borrarTablas(bd)
            onCreate(bd)
            return
        }
        if (versionVieja < 7) {
            bd.execSQL("ALTER TABLE solicitud_acceso ADD COLUMN sol_campo TEXT NOT NULL DEFAULT ''")
            bd.execSQL("ALTER TABLE solicitud_acceso ADD COLUMN sol_valor TEXT NOT NULL DEFAULT ''")
        }
        if (versionVieja < 8) {
            bd.execSQL("ALTER TABLE usuario ADD COLUMN id_remoto INTEGER")
            bd.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_usuario_remoto ON usuario(id_remoto)")
            bd.execSQL("ALTER TABLE solicitud_acceso ADD COLUMN id_remoto INTEGER")
            bd.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_solicitud_remoto ON solicitud_acceso(id_remoto)")
            bd.execSQL("ALTER TABLE visita ADD COLUMN id_remoto INTEGER")
            bd.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_visita_remoto ON visita(id_remoto)")
            bd.execSQL("ALTER TABLE protocolo ADD COLUMN id_remoto INTEGER")
            bd.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_protocolo_remoto ON protocolo(id_remoto)")
            bd.execSQL(
                "CREATE TABLE IF NOT EXISTS cambio_pendiente (" +
                        "id_cambio INTEGER PRIMARY KEY AUTOINCREMENT, entidad TEXT NOT NULL, " +
                        "id_local INTEGER NOT NULL, detalle TEXT NOT NULL DEFAULT '')"
            )
        }
    }

    fun reiniciarTodo() {
        val bd = writableDatabase
        bd.beginTransaction()
        try {
            borrarTablas(bd)
            onCreate(bd)
            borrarFormalizados(bd)
            bd.setTransactionSuccessful()
        } finally {
            bd.endTransaction()
        }
        generacion = generacion + 1
    }

    private fun borrarTablas(bd: SQLiteDatabase) {
        for (tabla in TABLAS) {
            bd.execSQL("DROP TABLE IF EXISTS $tabla")
        }
    }

    private fun borrarFormalizados(bd: SQLiteDatabase) {
        val registros = filas("SELECT id_protocolo FROM protocolo WHERE estado IN ('FORMALIZADO','ANULADO')")
        val protocolos = mutableListOf<Int>()
        for (registro in registros) {
            protocolos.add(registro.entero("id_protocolo"))
        }
        if (protocolos.isEmpty()) return
        val listaProtocolos = unirConComas(protocolos)

        bd.execSQL(
            "DELETE FROM medicion WHERE id_prueba IN " +
                    "(SELECT id_prueba FROM prueba WHERE id_protocolo IN ($listaProtocolos))"
        )
        bd.execSQL("DELETE FROM prueba WHERE id_protocolo IN ($listaProtocolos)")
        bd.execSQL("DELETE FROM historial_estado WHERE id_protocolo IN ($listaProtocolos)")

        val registrosVisita = filas(
            "SELECT DISTINCT id_visita FROM protocolo WHERE id_protocolo IN ($listaProtocolos)"
        )
        val visitas = mutableListOf<Int>()
        for (registro in registrosVisita) {
            visitas.add(registro.entero("id_visita"))
        }
        bd.execSQL("DELETE FROM protocolo WHERE id_protocolo IN ($listaProtocolos)")
        if (visitas.isEmpty()) return
        val listaVisitas = unirConComas(visitas)
        bd.execSQL("DELETE FROM recepcion_acta WHERE id_visita IN ($listaVisitas)")
        bd.execSQL("DELETE FROM visita_participante WHERE id_visita IN ($listaVisitas)")
        bd.execSQL(
            "DELETE FROM visita WHERE id_visita IN ($listaVisitas) AND fecha_hora_cierre IS NOT NULL " +
                    "AND id_visita NOT IN (SELECT id_visita FROM protocolo)"
        )
    }

    private fun unirConComas(numeros: List<Int>): String {
        var texto = ""
        for (numero in numeros) {
            if (texto.isNotEmpty()) texto = texto + ","
            texto = texto + numero
        }
        return texto
    }
}
