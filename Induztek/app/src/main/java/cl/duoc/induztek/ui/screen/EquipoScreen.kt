package cl.duoc.induztek.ui.screen

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.induztek.model.TipoPrueba
import cl.duoc.induztek.model.Usuario
import cl.duoc.induztek.viewmodel.EquipoViewModel

//fichas del equipo
//protocolos
//pruebas historicas

@Composable
fun EquipoScreen(
    idEquipos: Int,
    usuario: Usuario,
    onAtras: () -> Unit,
    onRegistrarPrueba: (TipoPrueba) -> Unit,
    onIrActa: (Int) -> Unit,
    vm: EquipoViewModel = viewModel()
) {
}