package cl.duoc.induztek.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import cl.duoc.induztek.ui.screen.LoginScreen


@Composable
fun AppNavegacion(){
    val navController = rememberNavController()

    NavHost( navController = navController, startDestination = Ruta.Login.ruta){
        composable(Ruta.Login.ruta){
            LoginScreen(
                onIngresoExitoso = { },
                onSolicitarAcceso = { navController.navigate(Ruta.SolicitarAcceso.ruta)}
            )
        }

        composable(Ruta.SolicitarAcceso.ruta){
            SolicitarAccesoScreen(
                onSolicitudEnviada = { navController.navigateUp() },
                onVolverAIngreso = { navController.navigateUp() }
            )
        }
    }
}