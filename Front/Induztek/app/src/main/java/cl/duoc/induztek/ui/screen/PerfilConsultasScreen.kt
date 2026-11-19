
package cl.duoc.induztek.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.induztek.R
import cl.duoc.induztek.ui.components.colorCaja
import cl.duoc.induztek.ui.components.fondoPantallaInterna
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.TextoSecundario
import cl.duoc.induztek.ui.theme.InduztekTheme
import cl.duoc.induztek.ui.theme.BordeSuave
import cl.duoc.induztek.ui.theme.TextoTitulo
import cl.duoc.induztek.ui.theme.TextoPrincipal
import cl.duoc.induztek.viewmodel.ColaboracionViewModel


@Composable
fun ConsultasFrecuentesScreen(
    onAtras: () -> Unit,
    modeloPantalla: ColaboracionViewModel = viewModel()
) {
    val usuarioActual = modeloPantalla.usuarioEnSesion()
    var nombre = ""
    if (usuarioActual != null) nombre = usuarioActual.nombre.substringBefore(" ")
    ConsultasContenido(
        nombre = nombre,
        onAtras = onAtras
    )
}

@Composable
private fun ConsultasContenido(nombre: String, onAtras: () -> Unit) {
    val preguntas = listOf(
        stringResource(R.string.faq_q1) to stringResource(R.string.faq_a1),
        stringResource(R.string.faq_q2) to stringResource(R.string.faq_a2),
        stringResource(R.string.faq_q3) to stringResource(R.string.faq_a3),
        stringResource(R.string.faq_q4) to stringResource(R.string.faq_a4),
        stringResource(R.string.faq_q5) to stringResource(R.string.faq_a5),
        stringResource(R.string.faq_q6) to stringResource(R.string.faq_a6),
        stringResource(R.string.faq_q7) to stringResource(R.string.faq_a7),
        stringResource(R.string.faq_q8) to stringResource(R.string.faq_a8)
    )
    var abierta by rememberSaveable { mutableIntStateOf(-1) }

    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .fondoPantallaInterna()
            .verticalScroll(rememberScrollState())
    ) {
        Column(Modifier.padding(horizontal = Dimens.margenHorizontalPantalla)) {
            CabeceraVolver(stringResource(R.string.faq_titulo), onAtras = onAtras)
            Text(
                if (nombre.isBlank()) stringResource(R.string.faq_intro)
                else stringResource(R.string.faq_intro) + " " + nombre + ".",
                style = MaterialTheme.typography.bodyMedium,
                color = TextoSecundario()
            )
            Spacer(Modifier.height(Dimens.espacioMedio))

            for (indice in preguntas.indices) {
                val pregunta = preguntas[indice].first
                val respuesta = preguntas[indice].second
                FilaPregunta(
                    pregunta = pregunta,
                    respuesta = respuesta,
                    abierta = abierta == indice,
                    descripcion = stringResource(R.string.faq_abrir),
                    onClic = { abierta = if (abierta == indice) -1 else indice }
                )
                Spacer(Modifier.height(Dimens.salto2))
            }
            Spacer(Modifier.height(Dimens.espacioGrande))
        }
    }
}

@Composable
private fun FilaPregunta(
    pregunta: String,
    respuesta: String,
    abierta: Boolean,
    descripcion: String,
    onClic: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.radioTarjeta))
            .border(Dimens.bordeTarjeta, BordeSuave(), RoundedCornerShape(Dimens.radioTarjeta))
            .background(colorCaja())
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClickLabel = descripcion) { onClic() }
                .semantics { contentDescription = pregunta }
                .padding(Dimens.rellenoTarjeta),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                pregunta,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextoTitulo(),
                modifier = Modifier.weight(1f)
            )
            Text(
                if (abierta) "⌄" else "›",
                style = MaterialTheme.typography.titleMedium,
                color = TextoSecundario()
            )
        }
        if (abierta) {
            Box(Modifier.fillMaxWidth().height(Dimens.grosorDivisor).background(BordeSuave()))
            Text(
                respuesta,
                style = MaterialTheme.typography.bodySmall,
                color = TextoPrincipal(),
                modifier = Modifier.padding(Dimens.rellenoTarjeta)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun ConsultasPreview() {
    InduztekTheme {
        ConsultasContenido(nombre = "Thomas", onAtras = {})
    }
}
