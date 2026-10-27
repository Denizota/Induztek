package cl.duoc.induztek.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

//todavia no hago el modo claro OK

private val EsquemaClaro = lightColorScheme(
    primary = PrimarioClaro,
    onPrimary = Blanco,
    background = FondoPantallaClaro,
    onBackground = TextoOscuroClaro,
    surface = FondoPantallaClaro,
    onSurface = TextoOscuroClaro
)

private val EsquemaOscuro = darkColorScheme(
    primary = PrimarioOscuro,
    onPrimary = Blanco,
    background = FondoPantallaOscuro,
    onBackground = TextoOscuroOscuro,
    surface = FondoPantallaOscuro,
    onSurface = TextoOscuroOscuro
)

private val Formas = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp)
)

@Composable
fun InduztekTheme(
    content: @Composable () -> Unit
){


    val contexto = LocalContext.current
    AjustesTema.cargarSiFalta(contexto)
    val modoOscuro = AjustesTema.apariencia.esOscuro()

    val paleta = if (modoOscuro) PaletaOscura else PaletaClara
    CompositionLocalProvider(LocalPaleta provides paleta) {
        MaterialTheme(
            colorScheme = if (modoOscuro) EsquemaOscuro else EsquemaClaro,
            typography = Tipografia,
            shapes = Formas,
            content = content
        )
    }
}
