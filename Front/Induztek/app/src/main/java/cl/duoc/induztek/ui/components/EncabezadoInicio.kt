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
import cl.duoc.induztek.ui.theme.TextoTitulo

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
                color = TextoTitulo()
            )
        }
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleLarge,
            color = TextoTitulo()
        )
        if (subtitulo.isNotEmpty()) {
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodyMedium,
                color = TextoTitulo()
            )
        }
    }
}
