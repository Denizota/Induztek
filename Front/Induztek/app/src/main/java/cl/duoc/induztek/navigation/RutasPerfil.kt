
package cl.duoc.induztek.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import cl.duoc.induztek.ui.screen.ConsultasFrecuentesScreen
import cl.duoc.induztek.ui.screen.EditarPerfilScreen
import cl.duoc.induztek.ui.screen.PerfilScreen
import cl.duoc.induztek.viewmodel.InicioViewModel

internal fun NavGraphBuilder.rutasPerfil(
    nav: NavHostController,
    irAEquipos: () -> Unit,
    salir: () -> Unit
) {
    composable(Ruta.Perfil.ruta) {
        val inicioVm: InicioViewModel = viewModel()
        PerfilScreen(
            onIrEquipos = { irAEquipos() },
            onEditar = { nav.navigate(Ruta.EditarPerfil.ruta) },
            onIrConsultas = { nav.navigate(Ruta.Consultas.ruta) },
            onSalir = { salir() },
            onReiniciar = { inicioVm.reiniciarTodo() }
        )
    }

    composable(Ruta.EditarPerfil.ruta) {
        EditarPerfilScreen(
            onAtras = {
                nav.popBackStack()
            },
            onIrEquipos = { irAEquipos() },
            onIrConsultas = { nav.navigate(Ruta.Consultas.ruta) }
        )
    }

    composable(Ruta.Consultas.ruta) {
        ConsultasFrecuentesScreen(
            onAtras = { nav.popBackStack() }
        )
    }
}
