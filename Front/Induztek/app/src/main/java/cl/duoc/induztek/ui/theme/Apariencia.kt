package cl.duoc.induztek.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit

enum class Apariencia { SISTEMA, CLARO, OSCURO }

object AjustesTema {

    private const val ARCHIVO = "apariencia"
    private const val CLAVE = "modo"

    var apariencia by mutableStateOf(Apariencia.SISTEMA)
        private set

    private var leida = false

    fun cargarSiFalta(contexto: Context) {
        if (leida) return
        leida = true
        val guardado = contexto.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
            .getString(CLAVE, Apariencia.SISTEMA.name)
        var elegida = Apariencia.SISTEMA
        for (posible in Apariencia.entries) {
            if (posible.name == guardado) elegida = posible
        }
        apariencia = elegida
    }

    fun guardar(contexto: Context, nueva: Apariencia) {
        leida = true
        apariencia = nueva
        contexto.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
            .edit { putString(CLAVE, nueva.name) }
    }
}

@Composable
fun Apariencia.esOscuro(): Boolean {
    if (this == Apariencia.CLARO) return false
    if (this == Apariencia.OSCURO) return true
    return isSystemInDarkTheme()
}
