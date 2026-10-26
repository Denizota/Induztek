package cl.duoc.induztek.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.induztek.model.Usuario
import cl.duoc.induztek.ui.screen.EquipoScreen
import cl.duoc.induztek.ui.screen.LoginScreen
import cl.duoc.induztek.ui.screen.PuenteScreen
import cl.duoc.induztek.ui.screen.SolicitarAccesoScreen
import cl.duoc.induztek.viewmodel.LoginViewModel


private fun rutaEquipo(id: Int) = "${Ruta.Equipo.ruta}/$id"


@Composable
fun AppNavegacion() {
    val navController = rememberNavController()


    var usuario by remember { mutableStateOf<Usuario?>(null) }

    fun entrar(u: Usuario) {
        usuario = u


        navController.navigate(Ruta.Puente.ruta) { popUpTo(Ruta.Login.ruta) { inclusive = true } }
    }

    NavHost(navController = navController, startDestination = Ruta.Login.ruta) {
        composable(Ruta.Login.ruta) {
            val vm: LoginViewModel = viewModel()
            LoginScreen(
                onIngresoExitoso = { vm.usuarioEnSesion()?.let(::entrar) },
                onSolicitarAcceso = { navController.navigate(Ruta.SolicitarAcceso.ruta) },
                viewModel = vm
            )
        }

        composable(Ruta.SolicitarAcceso.ruta) {
            SolicitarAccesoScreen(
                onSolicitudEnviada = { navController.navigateUp() },
                onVolverAIngreso = { navController.navigateUp() }
            )
        }

        composable(Ruta.Puente.ruta) {
            PuenteScreen(
                onIrEquipo = { navController.navigate(rutaEquipo(it)) },
                onSalir = {
                    usuario = null
                    navController.navigate(Ruta.Login.ruta) { popUpTo(Ruta.Puente.ruta) { inclusive = true } }
                }
            )
        }

        composable(
            "${Ruta.Equipo.ruta}/{idEquipo}",
            arguments = listOf(navArgument("idEquipo") { type = NavType.IntType })
        ) { backStack ->
            val id = backStack.arguments?.getInt("idEquipo") ?: return@composable
            EquipoScreen(
                idEquipo = id,
                usuario = usuario ?: return@composable,
                onAtras = { navController.popBackStack() },


                onRegistrarPrueba = { },
                onIrActa = { }
            )
        }
    }
}