
package cl.duoc.induztek.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import cl.duoc.induztek.R
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.FondoCampo
import cl.duoc.induztek.ui.theme.TextoSecundario
import cl.duoc.induztek.ui.theme.BordeSuave
import cl.duoc.induztek.ui.theme.TextoPrincipal


@Composable
internal fun BuscadorInicio(valor: String, onCambio: (String) -> Unit) {
    var enfocado by remember { mutableStateOf(false) }
    val forma = RoundedCornerShape(Dimens.radioBotonChico)

    Row(
        Modifier
            .fillMaxWidth()
            .height(Dimens.altoBuscadorInicio)
            .clip(forma)
            .background(FondoCampo())
            .border(
                if (enfocado) Dimens.grosorBordeEnfocado else Dimens.bordeCampo,
                if (enfocado) MaterialTheme.colorScheme.primary else BordeSuave(),
                forma
            )
            .padding(horizontal = Dimens.salto5),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_buscar),
            contentDescription = null,
            tint = TextoSecundario(),
            modifier = Modifier.size(Dimens.tamanoIconoCampo)
        )
        Spacer(Modifier.width(Dimens.salto3))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (valor.isEmpty()) {
                Text(
                    stringResource(R.string.inicio_buscar),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSecundario(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            BasicTextField(
                value = valor,
                onValueChange = onCambio,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextoPrincipal()),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { enfocado = it.isFocused }
            )
        }
    }
}

@Composable
internal fun PestanasInicio(etiquetas: List<String>, seleccionado: Int, onSeleccion: (Int) -> Unit) {
    val acento = MaterialTheme.colorScheme.primary
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(Dimens.altoPestanaInicio)
                .horizontalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.width(Dimens.margenHorizontalPantalla))
            for (indice in etiquetas.indices) {
                val etiqueta = etiquetas[indice]
                val activo = indice == seleccionado
                Box(
                    Modifier
                        .fillMaxHeight()
                        .clickable(role = Role.Tab) { onSeleccion(indice) }
                        .semantics { selected = activo }
                        .padding(horizontal = Dimens.salto5),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        etiqueta,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (activo) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (activo) acento else TextoSecundario(),
                        maxLines = 1,
                        modifier = Modifier
                            .padding(bottom = Dimens.salto2)
                            .drawBehind {
                                if (activo) {
                                    val grosor = Dimens.grosorPestana.toPx()
                                    drawRect(
                                        color = acento,
                                        topLeft = Offset(0f, size.height),
                                        size = Size(size.width, grosor)
                                    )
                                }
                            }
                    )
                }
            }
            Spacer(Modifier.width(Dimens.margenHorizontalPantalla))
        }
        Box(Modifier.fillMaxWidth().height(Dimens.bordeCampo).background(BordeSuave()))
    }
}
