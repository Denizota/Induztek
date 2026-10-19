package cl.duoc.induztek.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import cl.duoc.induztek.ui.theme.InduztekTheme
import cl.duoc.induztek.ui.theme.TextoGris

@Composable
fun TextoConEnlace(
    textoNormal: String,
    textoEnlace: String,
    onClickEnlace: () -> Unit,
    modifier: Modifier = Modifier
){
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$textoNormal ",
            style = MaterialTheme.typography.bodySmall,
            color = TextoGris
        )
        Text(
            text = textoEnlace,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = TextoGris,
            modifier = Modifier.clickable(role = Role.Button, onClick = onClickEnlace)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TextoConEnlacePreview(){
    InduztekTheme {
        TextoConEnlace(
            textoNormal = "¿No tienes una cuenta?",
            textoEnlace = "¡Registrate aquí!",
            onClickEnlace = {}
        )
    }
}
