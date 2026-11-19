package cl.duoc.induztek.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.induztek.R
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.IconoCampo
import cl.duoc.induztek.ui.theme.InduztekTheme

@Composable
fun CampoContrasena(
    valor: String,
    onValorCambia: (String) -> Unit,
    etiqueta: String,
    textoSugerido: String,
    visible: Boolean,
    onAlternarVisible: ()-> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    accionTeclado: ImeAction = ImeAction.Next,
    iconoInicial: Painter? = null
) {
    CampoBase(
        valor = valor,
        onValorCambia = onValorCambia,
        etiqueta = etiqueta,
        textoSugerido = textoSugerido,
        modifier = modifier,
        error = error,
        tipoTeclado = KeyboardType.Password,
        accionTeclado = accionTeclado,
        transformacionVisual = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        iconoInicial = iconoInicial,
        contenidoFinal = {
            Box(
                modifier = Modifier.size(Dimens.alturaCampo)
                    .clickable(role = Role.Button, onClick = onAlternarVisible),
                contentAlignment = Alignment.Center
            ){
                Icon(
                    painter = painterResource(
                        if (visible) R.drawable.ic_ojo_cerrado else R.drawable.ic_ojo_abierto
                    ),
                    contentDescription = stringResource(
                        if (visible) R.string.cd_ocultar_contrasena else R.string.cd_mostrar_contrasena
                    ),
                    tint = IconoCampo(),
                    modifier = Modifier.size(Dimens.tamanoIconoCampo)
                )
            }
        }
    )
}
