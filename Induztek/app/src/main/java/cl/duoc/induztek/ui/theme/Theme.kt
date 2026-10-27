package cl.duoc.induztek.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.unit.dp

/** El valor se guarda en el teléfono y se comparte con todas las pantallas Compose. */
data class ConfiguracionTema(
    val modoOscuro: Boolean,
    val cambiarModoOscuro: (Boolean) -> Unit
)

val LocalConfiguracionTema = compositionLocalOf {
    ConfiguracionTema(modoOscuro = false, cambiarModoOscuro = {})
}

private val EsquemaClaro = lightColorScheme(
    primary = Primario,
    onPrimary = Blanco,
    background = Color(0xFFF7F8FB),
    onBackground = TextoOscuro,
    surface = Blanco,
    onSurface = TextoOscuro,
    onSurfaceVariant = Color(0xFF50586A),
    surfaceVariant = Color(0xFFEFF0F5),
    secondaryContainer = Color(0xFFEDEAF8),
    onSecondaryContainer = Color(0xFF2F264C),
    tertiary = Color(0xFF3D7628),
    onTertiary = Blanco,
    tertiaryContainer = Color(0xFFE5F3DC),
    onTertiaryContainer = Color(0xFF24451D),
    outline = Color(0xFF72788A)
)

private val EsquemaOscuro = darkColorScheme(
    primary = Color(0xFFC3B3FF),
    onPrimary = Color(0xFF281B61),
    background = Color(0xFF12151B),
    onBackground = Color(0xFFE9EAF0),
    surface = Color(0xFF1E232C),
    onSurface = Color(0xFFE9EAF0),
    surfaceVariant = Color(0xFF2A303A),
    onSurfaceVariant = Color(0xFFC3C8D3),
    secondaryContainer = Color(0xFF35304A),
    onSecondaryContainer = Color(0xFFF0EAFF),
    tertiary = Color(0xFFA6E58F),
    onTertiary = Color(0xFF1D421B),
    tertiaryContainer = Color(0xFF263D2A),
    onTertiaryContainer = Color(0xFFC7EFC0),
    outline = Color(0xFF939AA8)
)

private val Formas = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp)
)

@Composable
fun InduztekTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current
    val preferencias = remember(context) {
        context.applicationContext.getSharedPreferences(
            "preferencias_apariencia", Context.MODE_PRIVATE
        )
    }
    // Si no se ha seleccionado un tema, seguimos la configuración del teléfono.
    var seleccionManual by remember(preferencias) {
        mutableStateOf<Boolean?>(
            if (preferencias.contains("modo_oscuro")) {
                preferencias.getBoolean("modo_oscuro", false)
            } else {
                null
            }
        )
    }
    val modoOscuro = seleccionManual ?: isSystemInDarkTheme()
    val configuracion = ConfiguracionTema(
        modoOscuro = modoOscuro,
        cambiarModoOscuro = { nuevoValor ->
            seleccionManual = nuevoValor
            preferencias.edit().putBoolean("modo_oscuro", nuevoValor).apply()
        }
    )

    // Actualiza también los iconos de las barras del sistema si se cambia el tema manualmente.
    SideEffect {
        var actual: Context = context
        while (actual is ContextWrapper && actual !is Activity) {
            actual = actual.baseContext
        }
        if (actual is Activity) {
            val controller = WindowCompat.getInsetsController(actual.window, view)
            controller.isAppearanceLightStatusBars = !modoOscuro
            controller.isAppearanceLightNavigationBars = !modoOscuro
        }
    }

    CompositionLocalProvider(LocalConfiguracionTema provides configuracion) {
        MaterialTheme(
            colorScheme = if (modoOscuro) EsquemaOscuro else EsquemaClaro,
            typography = Tipografia,
            shapes = Formas,
            content = content
        )
    }
}
