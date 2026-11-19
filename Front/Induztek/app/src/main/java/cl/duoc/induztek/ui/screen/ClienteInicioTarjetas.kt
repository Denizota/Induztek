
package cl.duoc.induztek.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import cl.duoc.induztek.R
import cl.duoc.induztek.ui.components.BarraInferior
import cl.duoc.induztek.ui.components.SeccionBarra
import cl.duoc.induztek.ui.components.Tarjeta
import cl.duoc.induztek.ui.components.colorCaja
import cl.duoc.induztek.ui.theme.DatoTecnico
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.TextoSecundario
import cl.duoc.induztek.ui.theme.BordeSuave
import cl.duoc.induztek.ui.theme.NaranjaObservado
import cl.duoc.induztek.ui.theme.TextoPrincipal
import cl.duoc.induztek.ui.theme.VerdeAprobado


@Composable
internal fun BuscadorCliente(valor: String, onCambio: (String) -> Unit) {
    var enfocado by remember { mutableStateOf(false) }
    val forma = RoundedCornerShape(Dimens.radioBotonChico)

    Row(
        Modifier
            .fillMaxWidth()
            .height(Dimens.altoBuscadorInicio)
            .clip(forma)
            .background(colorCaja())
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
                    stringResource(R.string.cliente_buscar),
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
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { enfocado = it.isFocused }
            )
        }
    }
}

@Composable
internal fun TarjetaEquipoCliente(
    tag: String,
    tipoEquipo: String,
    instalacion: String,
    ultimaPrueba: String,
    pendiente: Boolean,
    formalizadoPor: String,
    onClick: () -> Unit
) {
    val color = if (pendiente) NaranjaObservado() else VerdeAprobado()
    Tarjeta(
        Modifier
            .clickable(role = Role.Button, onClickLabel = stringResource(R.string.cd_abrir_equipo)) { onClick() }
            .semantics(mergeDescendants = true) { }
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    tag,
                    style = DatoTecnico.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                    color = TextoPrincipal()
                )
                Spacer(Modifier.height(Dimens.salto1))
                Text(tipoEquipo, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario())
            }
            EtiquetaEstado(
                stringResource(
                    if (pendiente) R.string.estado_pendiente_formalizacion
                    else R.string.estado_formalizado
                ),
                color
            )
        }
        Spacer(Modifier.height(Dimens.salto4))
        HorizontalDivider(thickness = Dimens.grosorDivisor, color = BordeSuave())
        Spacer(Modifier.height(Dimens.salto4))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.cliente_fila_ultima_prueba, instalacion, ultimaPrueba),
                    style = DatoTecnico,
                    color = TextoSecundario(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (formalizadoPor.isNotBlank()) {
                    Spacer(Modifier.height(Dimens.salto1))
                    Text(
                        stringResource(R.string.cliente_formalizado_por, formalizadoPor),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextoSecundario(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            CirculoFlecha()
        }
    }
}

@Composable
internal fun EtiquetaEstado(texto: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(Dimens.marcadorEstado)
                .background(color)
        )
        Spacer(Modifier.width(Dimens.salto2))
        Text(
            texto.uppercase(),
            style = DatoTecnico.copy(
                fontSize = 10.sp,
                letterSpacing = 0.8.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = color,
            maxLines = 1
        )
    }
}

@Composable
private fun CirculoFlecha() {
    Text(
        "›",
        style = MaterialTheme.typography.headlineSmall,
        color = TextoSecundario()
    )
}

@Composable
internal fun BarraNavegacionCliente(onIrPerfil: () -> Unit) {
    BarraInferior(
        activa = SeccionBarra.EQUIPOS,
        onIrEquipos = {},
        onIrPerfil = onIrPerfil
    )
}
