
package cl.duoc.induztek.ui.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.TextoSecundario
import cl.duoc.induztek.ui.theme.TextoTitulo
import cl.duoc.induztek.ui.theme.TextoPrincipal


@Composable
internal fun TituloRaya(texto: String) {
    Row(Modifier.fillMaxWidth().padding(bottom = Dimens.salto2), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .width(Dimens.altoBarraNombre)
                .height(Dimens.grosorDivisor)
                .background(TextoSecundario())
        )
        Spacer(Modifier.width(Dimens.salto3))
        Text(
            texto,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = TextoTitulo()
        )
    }
}

@Composable
internal fun FilaEstadistica(etiqueta: String, valor: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = Dimens.salto3),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = TextoSecundario(),
            modifier = Modifier.weight(1f)
        )
        Text(
            valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextoPrincipal(),
            textAlign = TextAlign.End
        )
    }
}

@Composable
internal fun FilaNota(@StringRes recurso: Int, cantidad: Int) {
    Text(
        stringResource(recurso, cantidad),
        style = MaterialTheme.typography.bodySmall,
        color = TextoSecundario(),
        textAlign = TextAlign.End,
        modifier = Modifier.fillMaxWidth().padding(bottom = Dimens.salto3)
    )
}

@Composable
internal fun FilaEspecialidad(texto: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = Dimens.salto3),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .width(Dimens.altoBarraNombre)
                .height(Dimens.grosorDivisor)
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(Modifier.width(Dimens.salto3))
        Text(
            texto,
            style = MaterialTheme.typography.bodyMedium,
            color = TextoPrincipal()
        )
    }
}

@Composable
internal fun FilaDato(etiqueta: String, valor: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = Dimens.salto2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            etiqueta,
            style = MaterialTheme.typography.bodySmall,
            color = TextoSecundario(),
            modifier = Modifier.weight(1f)
        )
        Text(
            valor,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End
        )
    }
}
