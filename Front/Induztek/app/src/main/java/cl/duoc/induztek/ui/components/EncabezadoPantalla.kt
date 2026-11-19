package cl.duoc.induztek.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import cl.duoc.induztek.R
import cl.duoc.induztek.ui.theme.Dimens
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import cl.duoc.induztek.ui.theme.TextoSecundario

@Composable
fun EncabezadoPantalla(titulo: String, subtitulo: String = "", onAtras: (() -> Unit)? = null) {
    val atras = stringResource(R.string.cd_atras_equipos)
    Column(Modifier.fillMaxWidth().padding(bottom = Dimens.espacioEncabezadoCuerpo)) {
        if (onAtras != null) {

            Row(
                Modifier
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onAtras)
                    .semantics(mergeDescendants = true) { contentDescription = atras }
                    .padding(end = Dimens.salto3, top = Dimens.salto1, bottom = Dimens.salto1),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(Dimens.tamanoBotonAtras),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "‹", color = TextoSecundario(), fontWeight = FontWeight.Bold, maxLines = 1,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                Text(
                    stringResource(R.string.atras_equipos),
                    color = TextoSecundario(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.height(Dimens.espacioTituloAtras))
        }
        Row(verticalAlignment = Alignment.Top) {
            if (subtitulo.isNotEmpty()) {
                Box(
                    Modifier.width(Dimens.anchoBarraAcento).height(Dimens.altoBarraAcento)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(Modifier.width(Dimens.salto3))
            }
            EncabezadoInicio(
                titulo = titulo,
                subtitulo = subtitulo,
                modifier = Modifier.weight(1f),
                mostrarEmpresa = false
            )
        }
    }
}
