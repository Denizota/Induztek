package cl.duoc.induztek.ui.components


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.FondoPantalla
import cl.duoc.induztek.ui.theme.GrisTexto
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import cl.duoc.induztek.ui.theme.LavandaBorde
import cl.duoc.induztek.ui.theme.LavandaSuave
import cl.duoc.induztek.ui.theme.esModoOscuro

@Composable
fun ControlSegmentado(opciones: List<String>, seleccionado: String, onSeleccion: (String) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(LavandaSuave(), RoundedCornerShape(Dimens.radioSegmento))
            .padding(Dimens.salto1),
        horizontalArrangement = Arrangement.spacedBy(Dimens.salto1)
    ) {
        opciones.forEach { op ->
            val activo = op == seleccionado
            Box(
                Modifier
                    .weight(1f)
                    .height(Dimens.alturaSegmento)
                    .background(

                        if (activo) {
                            if (esModoOscuro()) LavandaBorde() else FondoPantalla()
                        } else Color.Transparent,
                        RoundedCornerShape(Dimens.radioSegmento)
                    )
                    .clickable(role = Role.RadioButton) { onSeleccion(op) }
                    .semantics { selected = activo },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    op,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (activo) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (activo) MaterialTheme.colorScheme.primary else GrisTexto()
                )
            }
        }
    }
}
