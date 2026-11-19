
package cl.duoc.induztek.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import cl.duoc.induztek.model.Usuario
import cl.duoc.induztek.ui.screen.ActaScreen
import cl.duoc.induztek.ui.screen.ClienteEquipoScreen
import cl.duoc.induztek.ui.screen.ClienteHomeScreen
import cl.duoc.induztek.viewmodel.InicioViewModel

internal fun NavGraphBuilder.rutasCliente(
    nav: NavHostController,
    usuarioActual: () -> Usuario?,
    irA: (String) -> Unit,
    salir: () -> Unit
) {
    composable(
        route = "${Ruta.Acta.ruta}/{idVisita}/{cliente}",
        arguments = listOf(
            navArgument("idVisita") {
                type = NavType.IntType
            },
            navArgument("cliente") {
                type = NavType.IntType
            }
        )
    ) { backStack ->
        val actual = usuarioActual()
        if (actual == null) return@composable

        val argumentos = backStack.arguments
        if (argumentos == null) return@composable
        val idVisita = argumentos.getInt("idVisita")
        val esCliente = argumentos.getInt("cliente") == 1

        ActaScreen(
            idVisita = idVisita,
            usuario = actual,
            esCliente = esCliente,
            onAtras = {
                nav.popBackStack()
            }
        )
    }

    composable(Ruta.Cliente.ruta) {
        val actual = usuarioActual()
        if (actual == null) return@composable
        val inicioVm: InicioViewModel = viewModel()

        ClienteHomeScreen(
            usuario = actual,
            onIrEquipo = { idEquipo ->
                nav.navigate(rutaClienteEquipo(idEquipo))
            },
            onIrActa = { idVisita ->
                nav.navigate(rutaActa(idVisita, true))
            },
            onIrPerfil = { irA(Ruta.Perfil.ruta) },
            onSalir = {
                salir()
            },
            onReiniciar = { inicioVm.reiniciarTodo() }
        )
    }

    composable(
        route = "${Ruta.Cliente.ruta}/equipo/{idEquipo}",
        arguments = listOf(
            navArgument("idEquipo") {
                type = NavType.IntType
            }
        )
    ) { backStack ->
        val actual = usuarioActual()
        if (actual == null) return@composable
        val argumentos = backStack.arguments
        if (argumentos == null) return@composable
        val idEquipo = argumentos.getInt("idEquipo")

        ClienteEquipoScreen(
            idEquipo = idEquipo,
            usuario = actual,
            onAtras = {
                nav.popBackStack()
            }
        )
    }
}
