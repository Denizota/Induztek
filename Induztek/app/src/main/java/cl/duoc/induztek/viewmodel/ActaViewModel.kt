package cl.duoc.induztek.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import cl.duoc.induztek.data.InduztekDbHelper
import cl.duoc.induztek.model.RecepcionActa

class ActaViewModel(app: Application) : AndroidViewModel(app) {
    private val db = InduztekDbHelper.obtener(app)

    val nombre = mutableStateOf("")
    val rut = mutableStateOf("")
    val cargo = mutableStateOf("")
    val errNombre = mutableStateOf<String?>(null)
    val errRut = mutableStateOf<String?>(null)
    val errCargo = mutableStateOf<String?>(null)
    private var preparadoDe: Int? = null

    fun visitaPorId(id: Int) = db.visitaPorId(id)
    fun instalacionPorId(id: Int) = db.instalacionPorId(id)
    fun protocolosDeVisita(id: Int) = db.protocolosDeVisita(id)
    fun protocolosCliente(idVisita: Int, idEmpresa: Int) = db.protocolosDeVisitaCliente(idVisita, idEmpresa)
    fun recepcionDeVisita(id: Int) = db.recepcionDeVisita(id)
    fun pruebasDe(idProtocolo: Int) = db.listarPruebas(idProtocolo)
    fun cerrarVisita(idVisita: Int, nombre: String, rut: String, cargo: String) =
        db.cerrarVisita(idVisita, nombre, rut, cargo)


    fun preparar(idVisita: Int, recepcion: RecepcionActa?) {
        if (preparadoDe == idVisita || recepcion == null) return
        preparadoDe = idVisita
        nombre.value = recepcion.receptorNombre
        rut.value = recepcion.receptorRut
        cargo.value = recepcion.receptorCargo
    }
}
