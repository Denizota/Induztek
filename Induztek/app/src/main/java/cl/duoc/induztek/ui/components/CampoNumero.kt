package cl.duoc.induztek.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import cl.duoc.induztek.R
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.GrisTexto

@Composable
fun CampoNumero(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    sufijo: String = "",
    modifier: Modifier = Modifier,
    error: String? = null,
    habilitado: Boolean = true
) {
    CampoBase(
        valor = valor,
        onValorCambia = {
                nuevo -> if (nuevo.esNumero()) onCambio(nuevo)
        },
        etiqueta = etiqueta,
        textoSugerido = stringResource(R.string.placeholder_numero),
        modifier = modifier,
        error = error,
        habilitado = habilitado,
        tipoTeclado = KeyboardType.Decimal,
        contenidoFinal = if (sufijo.isNotEmpty()) {
            {
                Text(
                    sufijo, color = GrisTexto, style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = Dimens.salto2)
                )
            }
        } else null
    )
}

internal fun String.esNumero() = all {
    it.isDigit() || it == '.' || it == ',' || it == '-'
}
