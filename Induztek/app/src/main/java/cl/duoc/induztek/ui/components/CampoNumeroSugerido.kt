package cl.duoc.induztek.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import cl.duoc.induztek.R
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.GrisTexto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoNumeroSugerido(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    sugerencias: List<String>,
    modifier: Modifier = Modifier,
    sufijo: String = ""
) {
    var expandido by remember {
        mutableStateOf(false)
    }
    ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = {
        expandido = it },
        modifier = modifier){
        CampoBase(
            valor = valor,
            onValorCambia = { nuevo ->
                if (nuevo.esNumero()) onCambio(nuevo.replace(',', '.'))
            },
            etiqueta = etiqueta,
            textoSugerido = stringResource(R.string.placeholder_numero),
            modifier = Modifier.menuAnchor(
                type = ExposedDropdownMenuAnchorType.PrimaryEditable,
                enabled = true
            ),
            tipoTeclado = KeyboardType.Decimal,
            contenidoFinal = {
                Column(
                    Modifier.padding(start = Dimens.salto2),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (sufijo.isNotEmpty()) {
                        Text(sufijo,
                            color = GrisTexto,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        "▾",
                        color = GrisTexto,
                        fontWeight = FontWeight.Bold)
                }
            }
        )
        ExposedDropdownMenu(expanded = expandido, onDismissRequest = {
            expandido = false
        }) {
            sugerencias.forEach {
                    sugerencia ->
                DropdownMenuItem(
                    text = { Text(sugerencia) },
                    onClick = { onCambio(sugerencia);expandido = false }
                )
            }
        }
    }
}
