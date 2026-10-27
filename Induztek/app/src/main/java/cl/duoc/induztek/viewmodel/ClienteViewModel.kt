package cl.duoc.induztek.viewmodel




import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duoc.induztek.data.InduztekDbHelper
import cl.duoc.induztek.model.EstadoProtocolo



class ClienteViewModel(app: Application) : AndroidViewModel(app) {
    private val db = InduztekDbHelper.obtener(app)

    fun equiposDe(idEmpresa: Int) = db.listarEquiposDeCliente(idEmpresa)
    fun protocolosVisitaCliente(idEmpresa: Int) = db.listarProtocolosVisitaCliente(idEmpresa)
    fun visitaPorId(id: Int) = db.visitaPorId(id)
    fun instalacionPorId(id: Int) = db.instalacionPorId(id)
    fun recepcionDeVisita(id: Int) = db.recepcionDeVisita(id)
    fun protocolosDeVisita(id: Int) = db.protocolosDeVisita(id)
    fun nombreDe(id: Int) = db.buscarUsuarioPorId(id)?.nombre ?: ""
    fun ingenieroDe(idVisita: Int) =
        db.visitaPorId(idVisita)?.idIngenieroCargo?.let { nombreDe(it) } ?: ""
    fun equipo(id: Int) = db.equipoPorId(id)
    fun historial(idEquipo: Int) = db.historialAislacion(idEquipo)
    fun pruebasDe(idProtocolo: Int) = db.listarPruebas(idProtocolo)




    fun equipoDeCliente(idEquipo: Int, idEmpresa: Int) = db.equipoDeCliente(idEquipo, idEmpresa)



    fun protocolosDeEquipo(idEquipo: Int, idEmpresa: Int) = db.listarProtocolos()
        .filter {
            it.idEquipo == idEquipo && it.estado != EstadoProtocolo.BORRADOR &&
                    it.estado != EstadoProtocolo.ANULADO && esDeLaEmpresa(it.idVisita, idEmpresa)
        }
        .sortedByDescending { it.fechaHoraPrueba }

    private fun esDeLaEmpresa(idVisita: Int, idEmpresa: Int) =
        db.instalacionPorId(db.visitaPorId(idVisita)?.idInstalacion ?: -1)?.idUsuarioCliente == idEmpresa
}
