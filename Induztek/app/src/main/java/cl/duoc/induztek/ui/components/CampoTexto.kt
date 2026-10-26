package cl.duoc.induztek.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.InduztekTheme

@Composable
fun CampoTexto(
    valor: String,
    onValorCambia: (String) -> Unit,
    etiqueta: String,
    textoSugerido: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    habilitado: Boolean = true,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    accionTeclado: ImeAction = ImeAction.Next,
    alto: Dp = Dimens.alturaCampo,
    unaSolaLinea: Boolean = true
) {
    CampoBase(
        valor = valor,
        onValorCambia = onValorCambia,
        etiqueta = etiqueta,
        textoSugerido = textoSugerido,
        modifier = modifier,
        error = error,
        habilitado = habilitado,
        tipoTeclado = tipoTeclado,
        accionTeclado = accionTeclado,
        alto = alto,
        unaSolaLinea = unaSolaLinea
    )
}

@Preview(showBackground = true)
@Composable
private fun CampoTextoPreview(){
    InduztekTheme {
        CampoTexto(
            valor = "",
            onValorCambia = {},
            etiqueta = "Correo",
            textoSugerido = "yisang@induztek.cl",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Con error")
@Composable
private fun CampoTextoErrorPreview(){
    InduztekTheme {
        CampoTexto(
            valor = "ana@",
            onValorCambia = {},
            etiqueta = "Correo",
            textoSugerido = "yisang@induztek.cl",
            error = "Ingresa un correo valido !!",
            modifier = Modifier.padding(16.dp)
        )
    }
}