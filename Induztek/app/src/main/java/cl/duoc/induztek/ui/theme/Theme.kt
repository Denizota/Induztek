package cl.duoc.induztek.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

//todavia no hago el modo claro OK

private val EsquemaClaro = lightColorScheme(
    primary = Primario,
    onPrimary = Blanco,
    background = FondoPantalla,
    onBackground = TextoOscuro,
    surface = FondoPantalla,
    onSurface = TextoOscuro
)

private val Formas = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp)
)

@Composable
fun InduztekTheme(content: @Composable () -> Unit){
    MaterialTheme(
        colorScheme = EsquemaClaro,
        typography = Tipografia,
        shapes = Formas,
        content = content
    )
}