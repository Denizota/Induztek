
package cl.duoc.induztek.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.induztek.R
import cl.duoc.induztek.data.fechaCorta
import cl.duoc.induztek.data.fechaHoy
import cl.duoc.induztek.model.Instrumento
import cl.duoc.induztek.ui.components.CajaBordeada
import cl.duoc.induztek.ui.components.Tarjeta
import cl.duoc.induztek.ui.components.fondoPantallaInterna
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.TextoSecundario
import cl.duoc.induztek.ui.theme.InduztekTheme
import cl.duoc.induztek.ui.theme.RojoRechazado
import cl.duoc.induztek.ui.theme.TextoTitulo
import cl.duoc.induztek.ui.theme.VerdeAprobado
import cl.duoc.induztek.viewmodel.InicioViewModel


@Composable
fun InstrumentosScreen(
    onIrInstrumento: (Int) -> Unit,
    onAtras: () -> Unit,
    modeloPantalla: InicioViewModel = viewModel()
) {
    versionSincronia()
    InstrumentosContenido(
        instrumentos = modeloPantalla.instrumentos(),
        hoy = fechaHoy(),
        onIrInstrumento = onIrInstrumento,
        onAtras = onAtras
    )
}

@Composable
private fun InstrumentosContenido(
    instrumentos: List<Instrumento>,
    hoy: String,
    onIrInstrumento: (Int) -> Unit,
    onAtras: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .fondoPantallaInterna()
    ) {
        Column(Modifier.padding(horizontal = Dimens.margenHorizontalPantalla)) {
            CabeceraVolver(stringResource(R.string.admin_instrumentos), onAtras = onAtras)
        }

        LazyColumn(Modifier.weight(1f)) {
            items(instrumentos.size) { indice ->
                FilaInstrumento(
                    instrumentos[indice],
                    vigente = instrumentos[indice].vigenteEn(hoy),
                    onAbrir = { onIrInstrumento(instrumentos[indice].idInstrumento) },
                    modifier = Modifier.padding(horizontal = Dimens.margenHorizontalPantalla)
                )
            }
            item { Spacer(Modifier.height(Dimens.espacioGrande)) }
        }
    }
}

@Composable
private fun FilaInstrumento(
    instrumento: Instrumento,
    vigente: Boolean,
    onAbrir: () -> Unit,
    modifier: Modifier = Modifier
) {
    val abrir = stringResource(R.string.admin_ficha_instrumento)
    Tarjeta(
        modifier
            .padding(vertical = Dimens.salto1)
            .clickable(role = Role.Button, onClick = onAbrir)
            .semantics { contentDescription = "${instrumento.nombre}, $abrir" }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    instrumento.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextoTitulo()
                )
                Text(
                    "${instrumento.marca} ${instrumento.modelo} · ${instrumento.numeroSerie}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario()
                )
                Spacer(Modifier.height(Dimens.salto1))
                Text(
                    stringResource(
                        R.string.admin_calibracion,
                        instrumento.fechaUltimaCal.fechaCorta(),
                        instrumento.fechaVence.fechaCorta()
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario()
                )
            }
            Spacer(Modifier.width(Dimens.salto3))
            EstadoCalibracion(vigente)
            Spacer(Modifier.width(Dimens.salto3))
            Text(
                "›",
                style = MaterialTheme.typography.titleMedium,
                color = TextoSecundario(),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.clearAndSetSemantics { }
            )
        }
    }
}

@Composable
fun InstrumentoScreen(
    idInstrumento: Int,
    onAtras: () -> Unit,
    modeloPantalla: InicioViewModel = viewModel()
) {
    versionSincronia()
    val instrumentoElegido = modeloPantalla.instrumento(idInstrumento)
    if (instrumentoElegido == null) return
    InstrumentoContenido(
        instrumento = instrumentoElegido,
        vigente = instrumentoElegido.vigenteEn(fechaHoy()),
        onAtras = onAtras
    )
}

@Composable
private fun InstrumentoContenido(
    instrumento: Instrumento,
    vigente: Boolean,
    onAtras: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .fondoPantallaInterna()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.margenHorizontalPantalla)
    ) {
        CabeceraVolver(stringResource(R.string.admin_ficha_instrumento), onAtras = onAtras)
        Text(
            instrumento.nombre,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextoTitulo()
        )
        Spacer(Modifier.height(Dimens.salto2))
        Row(verticalAlignment = Alignment.CenterVertically) {
            EstadoCalibracion(vigente)
            Spacer(Modifier.width(Dimens.espacioChico))
            Text(
                "${instrumento.marca} ${instrumento.modelo}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextoSecundario()
            )
        }

        Spacer(Modifier.height(Dimens.espacioMedio))
        CajaBordeada(Modifier.fillMaxWidth()) {
            DatoInstrumento(stringResource(R.string.admin_marca), instrumento.marca)
            DatoInstrumento(stringResource(R.string.admin_modelo), instrumento.modelo)
            DatoInstrumento(stringResource(R.string.admin_serie), instrumento.numeroSerie)
            DatoInstrumento(
                stringResource(R.string.admin_ultima_cal),
                instrumento.fechaUltimaCal.fechaCorta()
            )
            DatoInstrumento(stringResource(R.string.admin_vence), instrumento.fechaVence.fechaCorta())
            DatoInstrumento(
                stringResource(R.string.admin_estado_cal),
                stringResource(if (vigente) R.string.admin_vigente else R.string.admin_vencido)
            )
        }

        Spacer(Modifier.height(Dimens.espacioMedio))
        Text(
            stringResource(R.string.admin_instrumento_uso),
            style = MaterialTheme.typography.bodySmall,
            color = TextoSecundario()
        )

        Spacer(Modifier.height(Dimens.espacioGrande))
    }
}

@Composable
private fun DatoInstrumento(etiqueta: String, valor: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = Dimens.salto1)) {
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
            color = TextoTitulo(),
            modifier = Modifier.weight(1.2f)
        )
    }
}

@Composable
private fun EstadoCalibracion(vigente: Boolean) {
    val color = if (vigente) VerdeAprobado() else RojoRechazado()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .width(Dimens.altoBarraNombre)
                .height(Dimens.grosorDivisor)
                .background(color)
        )
        Spacer(Modifier.width(Dimens.salto3))
        Text(
            stringResource(if (vigente) R.string.admin_vigente else R.string.admin_vencido),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}


private val PreviewInstrumentos = listOf(
    Instrumento(4, "Micro-ohmímetro", "Megger", "DLRO10X", "DLR-10882", "2026-08-20", "2026-08-20"),
    Instrumento(2, "Megóhmetro 10 kV", "Megger", "MIT1025", "MIT-0071233", "2025-10-10", "2026-10-10"),
    Instrumento(1, "Megóhmetro 5 kV", "Megger", "MIT525", "MIT-0045821", "2026-03-15", "2027-03-15"),
    Instrumento(3, "Medidor de relación (TTR)", "Megger", "TTR300", "TTR-30044", "2026-06-02", "2027-06-02")
)

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun InstrumentosPreview() {
    InduztekTheme {
        InstrumentosContenido(
            instrumentos = PreviewInstrumentos,
            hoy = "2026-10-28",
            onIrInstrumento = {},
            onAtras = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun FichaInstrumentoPreview() {
    InduztekTheme {
        InstrumentoContenido(
            instrumento = PreviewInstrumentos[1],
            vigente = false,
            onAtras = {}
        )
    }
}
