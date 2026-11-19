package cl.duoc.induztek.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.LavandaBorde

@Composable
fun Tarjeta(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.radioTarjeta),
        colors = CardDefaults.cardColors(containerColor = colorCaja()),
        border = BorderStroke(Dimens.bordeTarjeta, LavandaBorde()),
        content = { Column(Modifier.padding(Dimens.rellenoTarjeta)) { content() } }
    )
}
