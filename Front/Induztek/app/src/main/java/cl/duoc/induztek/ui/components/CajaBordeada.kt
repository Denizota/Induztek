package cl.duoc.induztek.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.BordeSuave

@Composable
fun CajaBordeada(
    modifier: Modifier = Modifier,
    centrado: Boolean = false,
    content: @Composable () -> Unit
) {
    Column(
        modifier
            .background(colorCaja(), RoundedCornerShape(Dimens.radioCaja))
            .border(Dimens.bordeCampo, BordeSuave(), RoundedCornerShape(Dimens.radioCaja))
            .padding(Dimens.rellenoCaja),
        verticalArrangement = if (centrado) Arrangement.Center else Arrangement.Top
    ) { content() }
}
