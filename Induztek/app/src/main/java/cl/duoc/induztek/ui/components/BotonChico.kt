package cl.duoc.induztek.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.LavandaBorde

@Composable
fun BotonChico(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(Dimens.radioBotonChico),
        border = BorderStroke(Dimens.bordeTarjeta, LavandaBorde()),
        contentPadding = PaddingValues(
            horizontal = Dimens.rellenoBotonChicoH, vertical = Dimens.rellenoBotonChicoV
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = colorCaja(),
            contentColor = MaterialTheme.colorScheme.primary
        ),
        modifier = modifier
    ) { Text(texto, style = MaterialTheme.typography.labelMedium) }
}

