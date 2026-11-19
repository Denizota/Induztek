package cl.duoc.induztek.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.TextoSecundario
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import cl.duoc.induztek.ui.theme.BordeSuave

@Composable
fun Pestanas(opciones: List<String>, seleccionado: String, onSeleccion: (String) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().height(Dimens.alturaPestana)) {
            for (op in opciones) {
                val activo = op == seleccionado
                Column(
                    Modifier.weight(1f)
                        .clickable(role = Role.Tab) { onSeleccion(op) }
                        .semantics { selected = activo },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            op,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (activo) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (activo) MaterialTheme.colorScheme.primary else TextoSecundario()
                        )
                    }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(Dimens.grosorPestana)
                            .background(if (activo) MaterialTheme.colorScheme.primary else Color.Transparent)
                    )
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(Dimens.bordeCampo).background(BordeSuave()))
    }
}
