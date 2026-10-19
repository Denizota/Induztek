package cl.duoc.induztek.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.TextoGris

@Composable
fun EtiquetaCampo (
    texto: String,
    modifier : Modifier = Modifier
){
    Text(
        text = texto,
        style = MaterialTheme.typography.labelMedium,
        color = TextoGris,
        modifier = modifier
    )
}

@Composable
fun MensajeErrorCampo(error: String?, modifier: Modifier = Modifier){
    if(error != null){
        Text(
            text = error,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = modifier.padding(top = Dimens.espacioMini)
        )
    }
}