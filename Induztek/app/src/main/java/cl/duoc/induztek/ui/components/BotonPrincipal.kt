package cl.duoc.induztek.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.induztek.ui.styles.EstilosBoton
import cl.duoc.induztek.ui.styles.estiloAltoBoton
import cl.duoc.induztek.ui.theme.InduztekTheme

//ingresar y registrarse. falsity = gris verity = el azul ese ajshfajs como el primary de bootstrap (horrible)

@Composable
fun BotonPrincipal (
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
    ){
    Button(
        onClick = onClick,
        modifier = modifier.estiloAltoBoton(),
        enabled = habilitado,
        shape = EstilosBoton.forma,
        colors = EstilosBoton.coloresPrincipal(),
        elevation = EstilosBoton.elevacion(),
        contentPadding = EstilosBoton.relleno
    ) {
        Text(text = texto, style = MaterialTheme.typography.titleMedium)
    }
}

@Preview(showBackground = true)
@Composable
private fun BotonPrincipalPreview(){
    InduztekTheme {
        Column(Modifier.padding(16.dp)) {
            BotonPrincipal(texto = "Ingresar", onClick = {}, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            BotonPrincipal(texto = "Ingresar", onClick = {}, habilitado = false, modifier = Modifier.fillMaxWidth())
        }
    }
}