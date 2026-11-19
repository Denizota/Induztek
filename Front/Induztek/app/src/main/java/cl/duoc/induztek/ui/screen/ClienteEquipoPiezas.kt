
package cl.duoc.induztek.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import cl.duoc.induztek.R
import cl.duoc.induztek.model.Calculos
import cl.duoc.induztek.ui.components.FilaVacia
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.TextoSecundario
import cl.duoc.induztek.ui.theme.BordeSuave
import cl.duoc.induztek.ui.theme.AzulAcero


@Composable
internal fun FilaHistorialCliente(
    fecha: String,
    numero: String,
    detalle: String,
    estado: String,
    formalizadoPor: String
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = Dimens.salto4),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("$fecha / $numero", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(detalle, style = MaterialTheme.typography.labelSmall, color = TextoSecundario())
            if (formalizadoPor.isNotBlank()) {
                Text(
                    stringResource(R.string.cliente_formalizado_por, formalizadoPor),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextoSecundario(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Box(
            Modifier
                .width(Dimens.grosorDivisor)
                .height(Dimens.altoDivisorFila)
                .background(BordeSuave())
        )
        Text(
            estado,
            Modifier
                .width(Dimens.anchoEstadoHistorial)
                .padding(start = Dimens.salto3),
            style = MaterialTheme.typography.labelSmall, color = TextoSecundario(), textAlign = TextAlign.End
        )
    }
}

@Composable
internal fun GraficoAislacion(series: List<Pair<String, Double>>) {
    if (series.isEmpty()) return
    val maximo = series.maxOf { it.second }
    val valorMaximo = if (maximo <= 0.0) 1.0 else maximo
    val altoUtil = Dimens.altoGrafico - Dimens.salto9
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(Dimens.altoGrafico),
            verticalAlignment = Alignment.Bottom
        ) {
            var indice = 0
            for (par in series) {
                val esLaUltima = indice == series.lastIndex
                Column(
                    Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        Calculos.formatear(par.second, 0),
                        style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(Dimens.salto1))
                    val alto: Dp = altoUtil * (par.second / valorMaximo).toFloat() * Dimens.factorAltoBarras
                    BarraAislacion(alto, solida = esLaUltima)
                }
                indice++
            }
        }
        Box(Modifier.fillMaxWidth().height(Dimens.grosorDivisor).background(BordeSuave()))
        FilaVacia(Dimens.salto2)
        Row(Modifier.fillMaxWidth()) {
            for (par in series) {
                Text(
                    par.first, Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun BarraAislacion(alto: Dp, solida: Boolean) {
    val colorBarra = AzulAcero()
    Canvas(
        Modifier.clearAndSetSemantics { }
            .fillMaxWidth(Dimens.anchoRelativoBarra)
            .height(alto)
    ) {
        val forma = Path()
        forma.addRoundRect(
            RoundRect(
                0f, 0f, size.width, size.height,
                CornerRadius(Dimens.radioBarras, Dimens.radioBarras),
                CornerRadius(Dimens.radioBarras, Dimens.radioBarras),
                CornerRadius.Zero, CornerRadius.Zero
            )
        )
        clipPath(forma) {
            drawRect(
                color = colorBarra.copy(
                    alpha = if (solida) Dimens.alfaBarraSolida else 0.28f
                )
            )
        }
    }
}
