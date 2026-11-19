package cl.duoc.induztek.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duoc.induztek.data.*
import cl.duoc.induztek.model.EstadoProtocolo

class ClienteViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {
    private val baseDatos = InduztekDbHelper.obtener(aplicacion)

    fun equiposDe(idEmpresa: Int) = baseDatos.listarEquiposDeCliente(idEmpresa)
    fun protocolosVisitaCliente(idEmpresa: Int) = baseDatos.listarProtocolosVisitaCliente(idEmpresa)
    fun visitaPorId(id: Int) = baseDatos.visitaPorId(id)
    fun instalacionPorId(id: Int) = baseDatos.instalacionPorId(id)
    fun recepcionDeVisita(id: Int) = baseDatos.recepcionDeVisita(id)
    fun protocolosDeVisita(id: Int) = baseDatos.protocolosDeVisita(id)
    fun nombreDe(id: Int): String {
        val persona = baseDatos.buscarUsuarioPorId(id)
        if (persona == null) return ""
        return persona.nombre
    }
    fun equipo(id: Int) = baseDatos.equipoPorId(id)
    fun historial(idEquipo: Int) = baseDatos.historialAislacion(idEquipo)
    fun pruebasDe(idProtocolo: Int) = baseDatos.listarPruebas(idProtocolo)
    fun formalizadoPor(idProtocolo: Int) = baseDatos.formalizadoPor(idProtocolo)
    fun equipoDeCliente(idEquipo: Int, idEmpresa: Int) = baseDatos.equipoDeCliente(idEquipo, idEmpresa)

    fun ingenieroDe(idVisita: Int): String {
        val visita = baseDatos.visitaPorId(idVisita)
        if (visita == null) {
            return ""
        }
        val idIngeniero = visita.idIngenieroCargo
        if (idIngeniero == null) {
            return ""
        }
        return nombreDe(idIngeniero)
    }

    fun protocolosDeEquipo(idEquipo: Int, idEmpresa: Int): List<cl.duoc.induztek.model.Protocolo> {
        val todos = baseDatos.listarProtocolos()
        val propios = mutableListOf<cl.duoc.induztek.model.Protocolo>()
        for (protocolo in todos) {
            if (protocolo.idEquipo == idEquipo &&
                protocolo.estado != EstadoProtocolo.BORRADOR &&
                protocolo.estado != EstadoProtocolo.ANULADO &&
                esDeLaEmpresa(protocolo.idVisita, idEmpresa)
            ) {
                propios.add(protocolo)
            }
        }
        return propios.sortedByDescending { it.fechaHoraPrueba }
    }

    private fun esDeLaEmpresa(idVisita: Int, idEmpresa: Int): Boolean {
        val visita = baseDatos.visitaPorId(idVisita)
        if (visita == null) {
            return false
        }
        val instalacion = baseDatos.instalacionPorId(visita.idInstalacion)
        if (instalacion == null) {
            return false
        }
        return instalacion.idUsuarioCliente == idEmpresa
    }
}
