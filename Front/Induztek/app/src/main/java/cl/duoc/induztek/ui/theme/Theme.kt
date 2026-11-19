package cl.duoc.induztek.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.unit.dp


data class ConfiguracionTema(
    val modoOscuro: Boolean,
    val cambiarModoOscuro: (Boolean) -> Unit
)

val LocalConfiguracionTema = compositionLocalOf {
    ConfiguracionTema(modoOscuro = false, cambiarModoOscuro = {})
}

private val EsquemaClaro = lightColorScheme(
    primary = AzulMarcaClaro,
    onPrimary = Blanco,
    primaryContainer = Color(0xFFE3EBF1),
    onPrimaryContainer = Color(0xFF0B2436),
    secondary = AcentoClaro,
    onSecondary = Color(0xFF2B1A00),
    secondaryContainer = Color(0xFFFFF1D6),
    onSecondaryContainer = Color(0xFF4A2E00),
    tertiary = VerdeAprobadoClaro,
    onTertiary = Blanco,
    tertiaryContainer = Color(0xFFE2F1E8),
    onTertiaryContainer = Color(0xFF0D3D24),
    error = RojoRechazadoClaro,
    onError = Blanco,
    errorContainer = Color(0xFFFBE9E7),
    onErrorContainer = Color(0xFF7A1F16),
    background = FondoPantallaClaro,
    onBackground = TextoPrincipalClaro,
    surface = FondoCampoClaro,
    onSurface = TextoPrincipalClaro,
    surfaceVariant = FondoFilaTablaClaro,
    onSurfaceVariant = Color(0xFF4C5865),
    outline = Color(0xFFB7C1CB),
    outlineVariant = BordeSuaveClaro,
    scrim = Negro,
    inverseSurface = Color(0xFF1B242C),
    inverseOnSurface = Color(0xFFF1F4F7),
    inversePrimary = AzulMarcaOscuro
)

private val EsquemaOscuro = darkColorScheme(
    primary = AzulMarcaOscuro,
    onPrimary = Color(0xFF0B1B28),
    primaryContainer = Color(0xFF1C3A52),
    onPrimaryContainer = Color(0xFFD6E6F3),
    secondary = AcentoOscuro,
    onSecondary = Color(0xFF2B1A00),
    secondaryContainer = Color(0xFF3D2C08),
    onSecondaryContainer = Color(0xFFFFE7BC),
    tertiary = VerdeAprobadoOscuro,
    onTertiary = Color(0xFF0D3D24),
    tertiaryContainer = Color(0xFF1B3A2A),
    onTertiaryContainer = Color(0xFFC6EFD8),
    error = RojoRechazadoOscuro,
    onError = Color(0xFF3B0A05),
    errorContainer = Color(0xFF5A201A),
    onErrorContainer = Color(0xFFFFDAD4),
    background = FondoPantallaOscuro,
    onBackground = Color(0xFFE6EBF0),
    surface = Color(0xFF131B22),
    onSurface = Color(0xFFE6EBF0),
    surfaceVariant = FondoSuaveOscuro,
    onSurfaceVariant = Color(0xFFB3BEC9),
    outline = Color(0xFF5B6772),
    outlineVariant = BordeSuaveOscuro,
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE6EBF0),
    inverseOnSurface = Color(0xFF16212B),
    inversePrimary = AzulMarcaClaro
)

private val Formas = Shapes(
    extraSmall = RoundedCornerShape(3.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(10.dp),
    extraLarge = RoundedCornerShape(12.dp)
)

@Composable
fun InduztekTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val vista = LocalView.current

    AjustesTema.cargarSiFalta(context)
    val modoOscuro = AjustesTema.apariencia.esOscuro()
    val paleta = if (modoOscuro) PaletaOscura else PaletaClara

    val configuracion = ConfiguracionTema(
        modoOscuro = modoOscuro,
        cambiarModoOscuro = { nuevoValor ->
            AjustesTema.guardar(
                context,
                if (nuevoValor) Apariencia.OSCURO else Apariencia.CLARO
            )
        }
    )

    SideEffect {
        var actual: Context = context
        while (actual is ContextWrapper && actual !is Activity) {
            actual = actual.baseContext
        }
        if (actual is Activity) {
            val controller = WindowCompat.getInsetsController(actual.window, vista)
            controller.isAppearanceLightStatusBars = !modoOscuro
            controller.isAppearanceLightNavigationBars = !modoOscuro
            actual.window.decorView.setBackgroundColor(paleta.fondoPantalla.toArgb())
        }
    }

    CompositionLocalProvider(
        LocalConfiguracionTema provides configuracion,
        LocalPaleta provides paleta
    ) {
        MaterialTheme(
            colorScheme = if (modoOscuro) EsquemaOscuro else EsquemaClaro,
            typography = Tipografia,
            shapes = Formas
        ) {
            CompositionLocalProvider(LocalContentColor provides paleta.textoPrincipal) {
                content()
            }
        }
    }
}
