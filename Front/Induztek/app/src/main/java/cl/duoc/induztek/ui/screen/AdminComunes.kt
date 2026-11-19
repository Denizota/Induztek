
package cl.duoc.induztek.ui.screen

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import cl.duoc.induztek.R
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.TextoSecundario
import cl.duoc.induztek.ui.theme.BordeSuave
import cl.duoc.induztek.ui.theme.TextoTitulo
import cl.duoc.induztek.ui.theme.TextoPrincipal


@Composable
internal fun CabeceraVolver(
    titulo: String,
    etiqueta: String = "",
    onAtras: () -> Unit,
    onEtiqueta: (() -> Unit)? = null
) {
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(Dimens.tamanoBotonAtras)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onAtras)
                    .semantics { contentDescription = titulo },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "‹",
                    color = TextoSecundario(),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            Spacer(Modifier.width(Dimens.salto1))
            Text(
                stringResource(R.string.admin_volver),
                style = MaterialTheme.typography.labelMedium,
                color = TextoSecundario()
            )
        }
        Spacer(Modifier.height(Dimens.espacioTituloAtras))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Text(
                titulo,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextoTitulo(),
                modifier = Modifier.weight(1f)
            )
            if (etiqueta.isNotEmpty()) {
                var modificadorEtiqueta: Modifier = Modifier
                if (onEtiqueta != null) {
                    modificadorEtiqueta = modificadorEtiqueta
                        .clip(RoundedCornerShape(Dimens.radioSegmento))
                        .clickable(role = Role.Button) { onEtiqueta() }
                }
                Text(
                    etiqueta,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (onEtiqueta == null) TextoSecundario() else TextoPrincipal(),
                    modifier = modificadorEtiqueta
                        .padding(horizontal = Dimens.salto2, vertical = Dimens.salto1)
                )
            }
        }
        Spacer(Modifier.height(Dimens.espacioEncabezadoCuerpo))
    }
}

@Composable
internal fun BotonBordeadoPropio(
    texto: String,
    habilitado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        enabled = habilitado,
        shape = RoundedCornerShape(Dimens.radioTarjeta),
        border = BorderStroke(Dimens.bordeTarjeta, BordeSuave()),
        modifier = modifier.fillMaxWidth().height(Dimens.alturaBotonGrande)
    ) {
        Text(
            texto,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
