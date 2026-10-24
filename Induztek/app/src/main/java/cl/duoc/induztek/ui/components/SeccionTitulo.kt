package cl.duoc.induztek.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import cl.duoc.induztek.ui.theme.Dimens

@Composable
fun SeccionTitulo(texto: String, modifier: Modifier = Modifier) {
    Text(
        texto, modifier.padding(vertical = Dimens.salto2),
        style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold
    )
}