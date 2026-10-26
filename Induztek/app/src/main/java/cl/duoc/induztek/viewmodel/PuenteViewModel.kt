package cl.duoc.induztek.viewmodel


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duoc.induztek.data.InduztekDbHelper
import cl.duoc.induztek.data.SesionPreferencias


class PuenteViewModel(app: Application) : AndroidViewModel(app) {
    private val db = InduztekDbHelper.obtener(app)

    fun listarEquipos() = db.listarEquipos()
    fun borrarSesion() = SesionPreferencias(getApplication()).borrar()


    fun reiniciarDemo() = db.reiniciarTodo()
}
