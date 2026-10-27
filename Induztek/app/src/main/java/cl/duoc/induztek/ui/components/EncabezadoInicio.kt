package cl.duoc.induztek.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import cl.duoc.induztek.R
import cl.duoc.induztek.ui.theme.InduztekTheme
import cl.duoc.induztek.ui.theme.TextoGris

@Composable
fun EncabezadoInicio(
    titulo: String,
    subtitulo: String,
    modifier: Modifier = Modifier,
    mostrarEmpresa: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (mostrarEmpresa) {
            Text(
                text = stringResource(R.string.nombre_empresa),
                style = MaterialTheme.typography.labelLarge,
                color = TextoGris()
            )
        }
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleLarge,
            color = TextoGris()
        )
        if (subtitulo.isNotEmpty()) {
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodyMedium,
                color = TextoGris()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EncabezadoInicioPreview(){
    InduztekTheme {
        EncabezadoInicio(
            titulo = "Protocolos de prueba en terreno",
            subtitulo = "Eslogan lindo tierno y adorable hola que tal."
        )
    }
}