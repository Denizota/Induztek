package cl.duoc.induztek.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.induztek.R
import cl.duoc.induztek.ui.components.BotonBordeado
import cl.duoc.induztek.ui.components.BotonPrincipal
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.viewmodel.PuenteViewModel

//pq NI IDEA QUE HIZO EL JOAQUIN.
//VERDE INDUZTEK MIS PATAS GRRRR
@Composable
fun PuenteScreen(
    onIrEquipo: (Int) -> Unit,
    onSalir: () -> Unit,
    vm: PuenteViewModel = viewModel()
) {
    val equipos = vm.listarEquipos()

    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(Dimens.margenPantalla),
        verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
    ) {
        // un botón simple por equipo: el título es el TAG, nada más
        equipos.forEach { e ->
            BotonPrincipal(texto = e.tag, onClick = { onIrEquipo(e.idEquipo) }, modifier = Modifier.fillMaxWidth())
        }

        // "Reiniciar" deja la base como recién instalada: todas las pruebas vuelven a estar pendientes
        BotonBordeado(texto = stringResource(R.string.puente_reiniciar), onClick = { vm.reiniciarDemo() })
        BotonBordeado(texto = stringResource(R.string.puente_salir), onClick = {
            vm.borrarSesion()
            onSalir()
        })
    }
}
