package cl.duoc.induztek.ui.styles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import cl.duoc.induztek.ui.theme.BordeCampo
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.FondoCampo
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Shape

object EstilosCampo {
    val forma: Shape = RoundedCornerShape(Dimens.radioBoton)

    @Composable
    fun colorBorde(enfocado: Boolean, hayError: Boolean): Color = when{
        hayError -> MaterialTheme.colorScheme.error
        enfocado -> MaterialTheme.colorScheme.primary
        else -> BordeCampo()
    }
}

@Composable
fun Modifier.estiloCajaCampo(enfocado: Boolean, hayError: Boolean, alto: Dp) : Modifier =
    this.fillMaxWidth()
        .height(alto)
        .background(FondoCampo(), EstilosCampo.forma)
        .border(
            if (enfocado || hayError) Dimens.grosorBordeEnfocado else Dimens.bordeCampo,
            EstilosCampo.colorBorde(enfocado, hayError),
            EstilosCampo.forma
        )