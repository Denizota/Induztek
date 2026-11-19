package cl.duoc.induztek.ui.screen

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.induztek.R
import cl.duoc.induztek.ui.components.BotonBordeado
import cl.duoc.induztek.ui.components.BotonPrincipal
import cl.duoc.induztek.ui.components.ControlSegmentado
import cl.duoc.induztek.ui.components.Tarjeta
import cl.duoc.induztek.ui.theme.AjustesTema
import cl.duoc.induztek.ui.theme.Apariencia
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.TextoSecundario
import cl.duoc.induztek.viewmodel.PuenteViewModel

@Composable
fun PuenteScreen(
    onIrEquipo: (Int) -> Unit,
    onSalir: () -> Unit,
    modeloPantalla: PuenteViewModel = viewModel()
) {
    val equipos = modeloPantalla.listarEquipos()

    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(Dimens.margenPantalla),
        verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
    ) {
        AjustesAccesibilidad()

        for (equipo in equipos) {
            BotonPrincipal(texto = equipo.tag, onClick = { onIrEquipo(equipo.idEquipo) }, modifier = Modifier.fillMaxWidth())
        }

        BotonBordeado(texto = stringResource(R.string.puente_reiniciar), onClick = { modeloPantalla.reiniciarDemo() })
        BotonBordeado(texto = stringResource(R.string.puente_salir), onClick = {
            modeloPantalla.borrarSesion()
            onSalir()
        })
    }
}

@Composable
private fun AjustesAccesibilidad() {
    val contexto = LocalContext.current
    val manager = contexto.getSystemService(AccessibilityManager::class.java)

    var lectorPrendido = false
    if (manager != null && manager.isTouchExplorationEnabled) lectorPrendido = true
    var locutor by remember { mutableStateOf(lectorPrendido) }
    DisposableEffect(manager) {
        if (manager == null) return@DisposableEffect onDispose { }
        val oyente = AccessibilityManager.TouchExplorationStateChangeListener { locutor = it }
        manager.addTouchExplorationStateChangeListener(oyente)
        onDispose { manager.removeTouchExplorationStateChangeListener(oyente) }
    }

    val etiquetas = listOf(
        stringResource(R.string.apariencia_sistema),
        stringResource(R.string.apariencia_claro),
        stringResource(R.string.apariencia_oscuro)
    )
    val estadoLocutor = stringResource(
        if (locutor) R.string.puente_locutor_activado else R.string.puente_locutor_desactivado
    )
    val titulo = stringResource(R.string.puente_locutor)

    Tarjeta {
        Text(stringResource(R.string.puente_ajustes_titulo), style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(Dimens.salto2))
        Text(stringResource(R.string.puente_apariencia), style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(Dimens.salto2))
        ControlSegmentado(
            opciones = etiquetas,
            seleccionado = etiquetas[AjustesTema.apariencia.ordinal]
        ) { elegida ->
            AjustesTema.guardar(contexto, Apariencia.values()[etiquetas.indexOf(elegida)])
        }
        Spacer(Modifier.height(Dimens.espacioChico))
        Row(
            Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {
                    contentDescription = titulo
                    stateDescription = estadoLocutor
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(titulo, Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
            Switch(checked = locutor, onCheckedChange = { abrirAjustesAccesibilidad(contexto) })
        }
        Text(
            stringResource(R.string.puente_locutor_nota),
            style = MaterialTheme.typography.labelSmall,
            color = TextoSecundario()
        )
    }
}

private fun abrirAjustesAccesibilidad(contexto: Context) {
    contexto.startActivity(
        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}
