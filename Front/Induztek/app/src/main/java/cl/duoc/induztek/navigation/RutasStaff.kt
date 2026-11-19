
package cl.duoc.induztek.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import cl.duoc.induztek.ui.screen.AsignarTareaScreen
import cl.duoc.induztek.ui.screen.InicioStaffScreen
import cl.duoc.induztek.ui.screen.InstrumentoScreen
import cl.duoc.induztek.ui.screen.InstrumentosScreen
import cl.duoc.induztek.ui.screen.PersonalScreen
import cl.duoc.induztek.ui.screen.SolicitudesScreen
import cl.duoc.induztek.ui.screen.SuspensionScreen

@Composable
internal fun RutaInicio(
    nav: NavHostController,
    onIrPerfil: () -> Unit,
    onSalir: () -> Unit
) {
    InicioStaffScreen(
        onIrEquipo = { idEquipo -> nav.navigate(rutaEquipo(idEquipo)) },
        onIrPerfil = onIrPerfil,
        onSalir = onSalir,
        onIrInstrumentos = { nav.navigate(Ruta.Instrumentos.ruta) },
        onIrPerfiles = { nav.navigate(Ruta.Personal.ruta) },
        onIrSolicitudes = { nav.navigate(Ruta.Solicitudes.ruta) }
    )
}

internal fun NavGraphBuilder.rutasStaff(
    nav: NavHostController,
    irA: (String) -> Unit,
    salir: () -> Unit
) {
    composable(Ruta.Inicio.ruta) { RutaInicio(nav, { irA(Ruta.Perfil.ruta) }) { salir() } }
    composable(Ruta.Protocolos.ruta) { RutaInicio(nav, { irA(Ruta.Perfil.ruta) }) { salir() } }

    composable(Ruta.Instrumentos.ruta) {
        InstrumentosScreen(
            onIrInstrumento = { idInstrumento ->
                nav.navigate(rutaInstrumento(idInstrumento))
            },
            onAtras = { nav.popBackStack() }
        )
    }

    composable(
        route = "${Ruta.Instrumento.ruta}/{idInstrumento}",
        arguments = listOf(
            navArgument("idInstrumento") {
                type = NavType.IntType
            }
        )
    ) { backStack ->
        val argumentos = backStack.arguments
        if (argumentos == null) return@composable
        val idInstrumento = argumentos.getInt("idInstrumento")

        InstrumentoScreen(
            idInstrumento = idInstrumento,
            onAtras = { nav.popBackStack() }
        )
    }

    composable(Ruta.Personal.ruta) {
        PersonalScreen(
            onIrSuspension = { idUsuario ->
                nav.navigate(rutaSuspension(idUsuario))
            },
            onIrAsignar = { idPersona ->
                nav.navigate("${Ruta.AsignarTarea.ruta}/$idPersona")
            },
            onIrSolicitudes = { nav.navigate(Ruta.Solicitudes.ruta) },
            onAtras = { nav.popBackStack() }
        )
    }

    composable(
        route = "${Ruta.AsignarTarea.ruta}/{idPersona}",
        arguments = listOf(
            navArgument("idPersona") {
                type = NavType.IntType
            }
        )
    ) { backStack ->
        val argumentos = backStack.arguments
        if (argumentos == null) return@composable
        val idPersona = argumentos.getInt("idPersona")

        AsignarTareaScreen(
            idPersona = idPersona,
            onAtras = { nav.popBackStack() }
        )
    }

    composable(Ruta.Solicitudes.ruta) {
        SolicitudesScreen(
            onAtras = { nav.popBackStack() }
        )
    }

    composable(
        route = "${Ruta.Suspension.ruta}/{idUsuario}",
        arguments = listOf(
            navArgument("idUsuario") {
                type = NavType.IntType
            }
        )
    ) { backStack ->
        val argumentos = backStack.arguments
        if (argumentos == null) return@composable
        val idUsuario = argumentos.getInt("idUsuario")

        SuspensionScreen(
            idUsuario = idUsuario,
            onAtras = {
                nav.popBackStack()
            }
        )
    }
}
