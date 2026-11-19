
package cl.duoc.induztek.ui.screen

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import cl.duoc.induztek.R
import cl.duoc.induztek.model.Calculos
import cl.duoc.induztek.ui.components.FilaVacia
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.TextoSecundario
import cl.duoc.induztek.ui.theme.RojoRechazado

@Composable
internal fun ContraPruebasAnteriores(historial: List<Triple<String, String, Double>>) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            stringResource(R.string.ficha_contra_titulo), Modifier.weight(1f),
            fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall
        )
        TextButton(onClick = { }) {
            Text(
                stringResource(R.string.ficha_contra_ver),
                style = MaterialTheme.typography.labelSmall, color = TextoSecundario()
            )
        }
    }
    FilaVacia(Dimens.salto3)
    if (historial.isEmpty()) {
        Text(
            stringResource(R.string.ficha_contra_sin_historial),
            style = MaterialTheme.typography.bodySmall, color = TextoSecundario()
        )
        return
    }
    Row(Modifier.fillMaxWidth().padding(vertical = Dimens.salto3)) {
        Text(
            stringResource(R.string.ficha_anio), Modifier.weight(1f),
            style = MaterialTheme.typography.labelSmall, color = TextoSecundario()
        )
        Text(
            stringResource(R.string.ficha_contra_col_1min), Modifier.weight(1f),
            style = MaterialTheme.typography.labelSmall, color = TextoSecundario()
        )
        Text(
            stringResource(R.string.ficha_contra_col_variacion), Modifier.weight(1f),
            style = MaterialTheme.typography.labelSmall, color = TextoSecundario(), textAlign = TextAlign.End
        )
    }
    Divisor()
    var anterior: Double? = null
    var algunaSignificativa = false
    for (indice in historial.indices) {
        val (_, fecha, valor) = historial[indice]
        val varPct = if (indice == 0) null else Calculos.variacionPct(valor, anterior)
        val significativa = Calculos.esVariacionSignificativa(varPct)
        if (significativa) algunaSignificativa = true
        Row(Modifier.fillMaxWidth().padding(vertical = Dimens.salto4)) {
            Text(fecha.take(4), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(Calculos.formatear(valor, 0), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text(
                if (varPct != null) stringResource(R.string.valor_porcentaje, Calculos.formatear(varPct, 1)) else "-",
                Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = if (significativa) RojoRechazado() else Color.Unspecified,
                fontWeight = if (significativa) FontWeight.SemiBold else FontWeight.Normal,
                textAlign = TextAlign.End
            )
        }
        if (indice < historial.lastIndex) Divisor()
        anterior = valor
    }
    if (algunaSignificativa) {
        FilaVacia(Dimens.salto4)
        Text(
            stringResource(R.string.alerta_variacion),
            style = MaterialTheme.typography.bodySmall, color = RojoRechazado(),
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
        )
    }
}
