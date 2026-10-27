package cl.duoc.induztek.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cl.duoc.induztek.R
import cl.duoc.induztek.ui.styles.estiloCajaCampo
import cl.duoc.induztek.ui.theme.Blanco
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.Negro
import cl.duoc.induztek.ui.theme.TextoOscuro
import cl.duoc.induztek.ui.theme.TextoPlaceholder
import cl.duoc.induztek.ui.theme.esModoOscuro

@Composable
internal fun CampoBase(
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
    unaSolaLinea: Boolean = true,
    transformacionVisual: VisualTransformation = VisualTransformation.None,
    contenidoFinal: (@Composable () -> Unit)? = null
){
    var enfocado by remember { mutableStateOf(false) }

    val bloqueado = stringResource(R.string.cd_campo_bloqueado)
    val estadoCampo = error ?: if (!habilitado) bloqueado else null
    val modificadorColumna = modifier
        .fillMaxWidth()
        .then(
            if (estadoCampo == null) Modifier
            else Modifier.semantics { stateDescription = estadoCampo }
        )

    val modificadorCampo = Modifier
        .estiloCajaCampo(enfocado = enfocado, hayError = error != null, alto = alto)
        .onFocusChanged { enfocado = it.isFocused }
        .semantics {
            contentDescription = if (error != null) "$etiqueta, $error"
            else if (!habilitado) "$etiqueta, $bloqueado" else etiqueta
        }

    Column(modifier = modificadorColumna) {
        EtiquetaCampo(texto = etiqueta)
        Spacer(Modifier.height(Dimens.espacioEtiquetaACampo))

        BasicTextField(
            value = valor,
            onValueChange = onValorCambia,
            modifier = modificadorCampo,
            singleLine = unaSolaLinea,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextoOscuro()),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado, imeAction = accionTeclado),
            visualTransformation = transformacionVisual,
            decorationBox = { campoInterno ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            horizontal = Dimens.espacioCampoHorizontal,
                            vertical = if (unaSolaLinea) 0.dp else Dimens.espacioChico
                        ),
                    verticalAlignment = if (unaSolaLinea) Alignment.CenterVertically else Alignment.Top
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = if (unaSolaLinea) Alignment.CenterStart else Alignment.TopStart
                    ) {

                        if (valor.isEmpty()) {
                            Text(
                                text = textoSugerido,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextoPlaceholder()
                            )
                        }
                        campoInterno()
                    }
                    contenidoFinal?.invoke()
                }
            }
        )

        MensajeErrorCampo(
            error,
            modifier = if (error == null) Modifier
            else Modifier.semantics { liveRegion = LiveRegionMode.Polite }
        )
    }
}


@Composable
fun colorCaja(): Color =
    (if (esModoOscuro()) Negro else Blanco).copy(alpha = Dimens.alfaCajaBlanca)
