package cl.duoc.induztek.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duoc.induztek.data.*
import cl.duoc.induztek.model.Usuario

data class ResumenPerfil(
    val protocolos: Int,
    val equiposProbados: Int,
    val visitasCerradas: Int
)

class ColaboracionViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {

    private val baseDatos = InduztekDbHelper.obtener(aplicacion)

    fun usuarioEnSesion(): Usuario? =
        baseDatos.buscarUsuarioPorId(SesionPreferencias(getApplication()).leer())

    fun empresaDel(idEmpresa: Int?): String {
        if (idEmpresa == null) {
            return ""
        }
        val empresa = baseDatos.buscarUsuarioPorId(idEmpresa)
        if (empresa == null) {
            return ""
        }
        return empresa.nombre
    }

    fun estadisticas(id: Int): ResumenPerfil {
        val protocolos = contar(
            "SELECT COUNT(*) FROM protocolo WHERE id_usuario_registra=?", id
        )
        val equipos = contar(
            "SELECT COUNT(DISTINCT id_equipo) FROM protocolo WHERE id_usuario_registra=?", id
        )
        val visitas = contar(
            "SELECT COUNT(*) FROM visita WHERE id_ingeniero_cargo=? AND fecha_hora_cierre IS NOT NULL", id
        )
        return ResumenPerfil(protocolos, equipos, visitas)
    }

    fun equiposDeSuPlanta(idEmpresa: Int?): Int {
        if (idEmpresa == null) {
            return 0
        }
        return baseDatos.listarEquiposDeCliente(idEmpresa).size
    }

    fun actasRecibidas(idEmpresa: Int?): Int {
        if (idEmpresa == null) {
            return 0
        }
        val cursor = baseDatos.readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM recepcion_acta ra " +
                    "JOIN visita v ON v.id_visita=ra.id_visita " +
                    "JOIN instalacion i ON i.id_instalacion=v.id_instalacion " +
                    "WHERE i.id_usuario_cliente=?",
            arrayOf(idEmpresa.toString())
        )
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0)
            }
            return 0
        } finally {
            cursor.close()
        }
    }

    fun guardarPerfil(id: Int, nombre: String, bio: String): Boolean {
        val limpio = nombre.trim()
        if (limpio.length < 3) {
            return false
        }
        return baseDatos.guardarPerfil(id, limpio, bio.trim())
    }

    fun solicitarCambio(persona: Usuario, campo: String, valor: String, motivo: String): Boolean {
        return baseDatos.insertarSolicitudCambio(persona, campo, valor.trim(), motivo.trim())
    }

    fun cambioPendiente(idUsuario: Int, campo: String): Boolean {
        val pendientes = baseDatos.listarSolicitudesPendientes()
        for (solicitud in pendientes) {
            if (solicitud.esCambio && solicitud.idUsuario == idUsuario && solicitud.campo == campo) {
                return true
            }
        }
        return false
    }

    fun solicitarBaja(persona: Usuario, motivo: String): Boolean {
        return baseDatos.insertarSolicitudBaja(persona, motivo)
    }

    fun bajaPendiente(idUsuario: Int): Boolean {
        return baseDatos.tieneSolicitudBajaPendiente(idUsuario)
    }

    private fun contar(consulta: String, id: Int): Int {
        val cursor = baseDatos.readableDatabase.rawQuery(consulta, arrayOf(id.toString()))
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0)
            }
            return 0
        } finally {
            cursor.close()
        }
    }
}
