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
import cl.duoc.induztek.ui.theme.GrisTexto

@Composable
fun EncabezadoPantalla(titulo: String, subtitulo: String = "", onAtras: (() -> Unit)? = null) {
    Column(Modifier.fillMaxWidth().padding(bottom = Dimens.espacioEncabezadoCuerpo)) {
        if (onAtras != null) {


            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(Dimens.tamanoBotonAtras)
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClick = onAtras),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "‹", color = GrisTexto, fontWeight = FontWeight.Bold, maxLines = 1,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                Spacer(Modifier.width(Dimens.salto1))
                Text(stringResource(R.string.atras_equipos), color = GrisTexto, style = MaterialTheme.typography.labelMedium)
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
