package cl.duoc.induztek.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import cl.duoc.induztek.data.*
import cl.duoc.induztek.model.RecepcionActa

class ActaViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {
    private val baseDatos = InduztekDbHelper.obtener(aplicacion)

    val nombre = mutableStateOf("")
    val rut = mutableStateOf("")
    val cargo = mutableStateOf("")
    val errNombre = mutableStateOf<String?>(null)
    val errRut = mutableStateOf<String?>(null)
    val errCargo = mutableStateOf<String?>(null)
    private var preparadoDe: Int? = null

    fun visitaPorId(id: Int) = baseDatos.visitaPorId(id)
    fun instalacionPorId(id: Int) = baseDatos.instalacionPorId(id)
    fun protocolosDeVisita(id: Int) = baseDatos.protocolosDeVisita(id)
    fun protocolosCliente(idVisita: Int, idEmpresa: Int) = baseDatos.protocolosDeVisitaCliente(idVisita, idEmpresa)
    fun recepcionDeVisita(id: Int) = baseDatos.recepcionDeVisita(id)
    fun pruebasDe(idProtocolo: Int) = baseDatos.listarPruebas(idProtocolo)
    fun cerrarVisita(idVisita: Int, nombre: String, rut: String, cargo: String) =
        baseDatos.cerrarVisita(idVisita, nombre, rut, cargo)

    fun preparar(idVisita: Int, recepcion: RecepcionActa?) {
        if (preparadoDe == idVisita || recepcion == null) {
            return
        }
        preparadoDe = idVisita
        nombre.value = recepcion.receptorNombre
        rut.value = recepcion.receptorRut
        cargo.value = recepcion.receptorCargo
    }
}
