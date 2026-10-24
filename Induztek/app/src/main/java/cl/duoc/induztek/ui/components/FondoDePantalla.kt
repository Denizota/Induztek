package cl.duoc.induztek.ui.components

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import cl.duoc.induztek.ui.theme.Blanco
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.MoraditoClaro
import cl.duoc.induztek.ui.theme.fonditoMorado

//bueno como mas lo ponia bro. deje el buling
@Composable
fun Modifier.fondoPantallaInterna(): Modifier = background(
    Brush.verticalGradient(
        colorStops = arrayOf(
            0f to Blanco,
            Dimens.paradaDifuminado to fonditoMorado,
            1f to MoraditoClaro
        )
    )
)