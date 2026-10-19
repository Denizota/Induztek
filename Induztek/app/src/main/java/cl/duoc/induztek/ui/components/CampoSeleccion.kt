package cl.duoc.induztek.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.induztek.ui.styles.estiloCajaCampo
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.IconoCampo
import cl.duoc.induztek.ui.theme.InduztekTheme
import cl.duoc.induztek.ui.theme.TextoOscuro
import cl.duoc.induztek.ui.theme.TextoPlaceholder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoSeleccion (
    valor: String,
    opciones: List<String>,
    onSeleccionar: (String)-> Unit,
    etiqueta: String,
    textoSugerido: String,
    modifier: Modifier = Modifier

){
    var expandido by remember {
        mutableStateOf(false)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        EtiquetaCampo(texto = etiqueta)
        Spacer(Modifier.height(Dimens.espacioEtiquetaACampo))

        ExposedDropdownMenuBox(
            expanded = expandido,
            onExpandedChange = { expandido = it }
        ) {
            Row(
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .estiloCajaCampo(enfocado = expandido, hayError = false, alto = Dimens.alturaCampo)
                    .padding(start = Dimens.espacioCampoHorizontal),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = valor.ifEmpty { textoSugerido },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (valor.isEmpty()) TextoPlaceholder else TextoOscuro,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                CompositionLocalProvider(LocalContentColor provides IconoCampo) {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido)
                }
            }

            ExposedDropdownMenu(
                expanded = expandido,
                onDismissRequest = {expandido = false}
            ) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = {Text(opcion)},
                        onClick = {
                            onSeleccionar(opcion)
                            expandido = false
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CampoSeleccionPreview(){
    InduztekTheme {
        CampoSeleccion(
            valor = "",
            opciones = listOf("Chile", "Peru", "Argentina"),
            onSeleccionar = {},
            etiqueta = "Pais",
            textoSugerido = "No seleccionado",
            modifier = Modifier.padding(16.dp)
        )
    }
}