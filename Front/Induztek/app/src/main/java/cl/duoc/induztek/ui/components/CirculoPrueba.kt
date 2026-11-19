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
import cl.duoc.induztek.ui.theme.TextoSecundario
import cl.duoc.induztek.ui.theme.NaranjaObservado
import cl.duoc.induztek.ui.theme.RojoRechazado
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import cl.duoc.induztek.R
import cl.duoc.induztek.ui.theme.VerdeAprobado

@Composable
fun CirculoPrueba(numero: Int, resultado: Resultado?, noRealizada: Boolean = false) {
    val textoAccesible = stringResource(R.string.cd_prueba_numero, numero)
    var color = TextoSecundario()
    var glifo = numero.toString()
    if (noRealizada) {
        color = NaranjaObservado()
        glifo = "!"
    } else if (resultado == Resultado.APROBADO) {
        color = VerdeAprobado()
        glifo = "✓"
    } else if (resultado == Resultado.OBSERVADO) {
        color = NaranjaObservado()
        glifo = "!"
    } else if (resultado == Resultado.RECHAZADO) {
        color = RojoRechazado()
        glifo = "✕"
    }
    Box(
        Modifier
            .size(Dimens.tamanoIconoResultado)
            .background(color.copy(alpha = Dimens.iconoAlpha), CircleShape)
            .clearAndSetSemantics { contentDescription = textoAccesible },
        contentAlignment = Alignment.Center
    ) { Text(glifo, color = color, fontWeight = FontWeight.Bold) }
}
