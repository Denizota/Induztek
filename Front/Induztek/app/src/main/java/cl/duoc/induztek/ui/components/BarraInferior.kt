package cl.duoc.induztek.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import cl.duoc.induztek.R
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.TextoSecundario

enum class SeccionBarra { EQUIPOS, PERFIL }

@Composable
fun BarraInferior(
    activa: SeccionBarra,
    onIrEquipos: () -> Unit,
    onIrPerfil: () -> Unit
) {
    Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
        HorizontalDivider(
            thickness = Dimens.grosorDivisor,
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Row(Modifier.fillMaxWidth().height(Dimens.altoBarraNavegacion)) {
            ItemBarra(
                stringResource(R.string.barra_equipos), R.drawable.ic_equipos,
                activa == SeccionBarra.EQUIPOS, onIrEquipos, Modifier.weight(1f)
            )
            ItemBarra(
                stringResource(R.string.barra_perfil), R.drawable.ic_perfil,
                activa == SeccionBarra.PERFIL, onIrPerfil, Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ItemBarra(
    texto: String,
    icono: Int,
    activo: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (activo) MaterialTheme.colorScheme.primary else TextoSecundario()
    Column(
        modifier
            .semantics { contentDescription = texto; role = Role.Button; selected = activo }
            .clickable(enabled = !activo, role = Role.Button) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier
                .width(Dimens.salto9)
                .height(Dimens.grosorPestana)
                .background(if (activo) color else Color.Transparent)
        )
        Spacer(Modifier.height(Dimens.salto3))
        Icon(
            painter = painterResource(id = icono),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(Dimens.tamanoIconoNav)
        )
        Spacer(Modifier.height(Dimens.salto1))
        Text(
            texto,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
        Spacer(Modifier.height(Dimens.salto3))
    }
}
