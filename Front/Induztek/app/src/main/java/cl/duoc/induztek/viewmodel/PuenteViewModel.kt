package cl.duoc.induztek.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duoc.induztek.data.*

class PuenteViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {
    private val baseDatos = InduztekDbHelper.obtener(aplicacion)

    fun listarEquipos() = baseDatos.listarEquipos()
    fun borrarSesion() = SesionPreferencias(getApplication()).borrar()
    fun reiniciarDemo() = baseDatos.reiniciarTodo()
}
