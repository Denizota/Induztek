package cl.duoc.induztek.data

import android.content.Context

class SesionPreferencias(context: Context) {

    private val preferencias = context.getSharedPreferences("sesion", Context.MODE_PRIVATE)

    fun guardar(idUsuario: Int) {
        preferencias.edit().putInt("id_usuario", idUsuario).apply()
    }

    fun leer(): Int {
        return preferencias.getInt("id_usuario", -1)
    }

    fun borrar() {
        preferencias.edit().remove("id_usuario").apply()
    }
}
