package cl.duoc.induztek.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import cl.duoc.induztek.model.Resultado
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.GrisTexto
import cl.duoc.induztek.ui.theme.NaranjaObservado
import cl.duoc.induztek.ui.theme.RojoRechazado
import cl.duoc.induztek.ui.theme.VerdeAprobado

@Composable
fun CirculoPrueba(numero: Int, resultado: Resultado?, noRealizada: Boolean = false) {
    val (color, glifo) = when {
        noRealizada -> NaranjaObservado to "!"
        else -> when (resultado) {
            Resultado.APROBADO -> VerdeAprobado to "✓"
            Resultado.OBSERVADO -> NaranjaObservado to "!"
            Resultado.RECHAZADO -> RojoRechazado to "✕"
            null -> GrisTexto to numero.toString()
        }
    }
    Box(
        Modifier.size(Dimens.tamanoIconoResultado).background(color.copy(alpha = Dimens.iconoAlpha), CircleShape),
        contentAlignment = Alignment.Center
    ) { Text(glifo, color = color, fontWeight = FontWeight.Bold) }
}
