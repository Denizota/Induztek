package cl.duoc.induztek.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.LavandaBorde


@Composable
fun BotonBordeado(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(Dimens.radioTarjeta),
        border = BorderStroke(Dimens.bordeTarjeta, LavandaBorde()),
        modifier = modifier.fillMaxWidth().height(Dimens.alturaBotonGrande)
    ) { Text(
        texto,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.primary
    )
    }
}