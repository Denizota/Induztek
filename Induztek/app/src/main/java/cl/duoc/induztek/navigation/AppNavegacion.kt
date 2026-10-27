
package cl.duoc.induztek.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import cl.duoc.induztek.data.InduztekDbHelper
import cl.duoc.induztek.data.SesionPreferencias
import cl.duoc.induztek.model.TipoPrueba
import cl.duoc.induztek.model.Usuario

import cl.duoc.induztek.ui.screen.LoginScreen
import cl.duoc.induztek.ui.screen.SolicitarAccesoScreen
import cl.duoc.induztek.ui.screen.InicioScreen
import cl.duoc.induztek.ui.screen.PuenteScreen
import cl.duoc.induztek.ui.screen.EquipoScreen
import cl.duoc.induztek.ui.screen.PruebaFormScreen

// Pantallas nuevas
import cl.duoc.induztek.ui.screen.ListaMensajesScreen
import cl.duoc.induztek.ui.screen.ChatIndividualScreen
import cl.duoc.induztek.ui.screen.DetallesChatScreen
import cl.duoc.induztek.ui.screen.PerfilScreen
import cl.duoc.induztek.ui.screen.EditarPerfilScreen
import cl.duoc.induztek.ui.screen.PersonalHistorialScreen
import cl.duoc.induztek.ui.screen.ValidacionRegistroScreen

import cl.duoc.induztek.viewmodel.LoginViewModel

private fun rutaEquipo(id: Int): String =
    "${Ruta.Equipo.ruta}/$id"

private fun rutaPrueba(
    id: Int,
    tipo: TipoPrueba
): String =
    "${Ruta.Prueba.ruta}/$id/${tipo.name}"

private fun rutaChat(idContacto: Int): String =
    "${Ruta.Chat.ruta}/$idContacto"

private fun rutaDetallesChat(idContacto: Int): String =
    "${Ruta.DetallesChat.ruta}/$idContacto"


@Composable
fun AppNavegacion() {

    val navController = rememberNavController()
    val context = LocalContext.current

    val sesion = remember {
        SesionPreferencias(context)
    }

    val bd = remember {
        InduztekDbHelper.obtener(context)
    }

    var usuario by remember {
        mutableStateOf<Usuario?>(
            bd.buscarUsuarioPorId(sesion.leer())
                ?.takeIf { it.activo }
        )
    }

    // INICIAR SESION
    fun entrar(nuevoUsuario: Usuario) {
        usuario = nuevoUsuario
        sesion.guardar(nuevoUsuario.idUsuario)

        navController.navigate(Ruta.Inicio.ruta) {
            popUpTo(Ruta.Login.ruta) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    // CERRAR SESION
    fun salir() {
        sesion.borrar()
        usuario = null

        navController.navigate(Ruta.Login.ruta) {
            popUpTo(navController.graph.id) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination =
            if (usuario != null) {
                Ruta.Inicio.ruta
            } else {
                Ruta.Login.ruta
            }
    ) {

        // 1. LOGIN
        composable(Ruta.Login.ruta) {

            val vm: LoginViewModel = viewModel()

            LoginScreen(
                viewModel = vm,
                onIngresoExitoso = {
                    vm.usuarioEnSesion()?.let { nuevo ->
                        entrar(nuevo)
                    }
                },
                onSolicitarAcceso = {
                    navController.navigate(
                        Ruta.SolicitarAcceso.ruta
                    )
                }
            )
        }

        // 2. SOLICITAR ACCESO
        composable(Ruta.SolicitarAcceso.ruta) {

            SolicitarAccesoScreen(
                onSolicitudEnviada = {
                    navController.popBackStack()
                },
                onVolverAIngreso = {
                    navController.popBackStack()
                }
            )
        }

        // 3. INICIO
        composable(Ruta.Inicio.ruta) {

            InicioScreen(
                onProtocolosClick = {
                    navController.navigate(
                        Ruta.Puente.ruta
                    )
                },
                onChatClick = {
                    navController.navigate(
                        Ruta.Mensajes.ruta
                    )
                },
                onPerfilClick = {
                    navController.navigate(
                        Ruta.Perfil.ruta
                    )
                },
                onCerrarSesion = {
                    salir()
                }
            )
        }

        // 4. LISTADO DE EQUIPOS
        composable(Ruta.Puente.ruta) {

            PuenteScreen(
                onIrEquipo = { idEquipo ->
                    navController.navigate(
                        rutaEquipo(idEquipo)
                    )
                },
                onSalir = {
                    salir()
                }
            )
        }

        // 5. LISTA DE MENSAJES
        composable(Ruta.Mensajes.ruta) {

            val actual = usuario ?: return@composable

            ListaMensajesScreen(
                usuario = actual,
                onAbrirChat = { idContacto ->
                    navController.navigate(
                        rutaChat(idContacto)
                    )
                },
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        // 6. CHAT INDIVIDUAL
        composable(
            route = "${Ruta.Chat.ruta}/{idContacto}",
            arguments = listOf(
                navArgument("idContacto") {
                    type = NavType.IntType
                }
            )
        ) { backStack ->

            val actual = usuario ?: return@composable

            val idContacto =
                backStack.arguments?.getInt("idContacto")
                    ?: return@composable

            ChatIndividualScreen(
                usuario = actual,
                idContacto = idContacto,
                onDetalles = {
                    navController.navigate(
                        rutaDetallesChat(idContacto)
                    )
                },
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        // 7. DETALLES DEL CHAT
        composable(
            route = "${Ruta.DetallesChat.ruta}/{idContacto}",
            arguments = listOf(
                navArgument("idContacto") {
                    type = NavType.IntType
                }
            )
        ) { backStack ->

            val actual = usuario ?: return@composable

            val idContacto =
                backStack.arguments?.getInt("idContacto")
                    ?: return@composable

            DetallesChatScreen(
                usuario = actual,
                idContacto = idContacto,
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        // 8. PERFIL
        composable(Ruta.Perfil.ruta) {

            val actual = usuario ?: return@composable

            PerfilScreen(
                usuarioId = actual.idUsuario,
                onEditar = {
                    navController.navigate(
                        Ruta.EditarPerfil.ruta
                    )
                },
                onPersonal = {
                    navController.navigate(
                        Ruta.Personal.ruta
                    )
                },
                onValidar = {
                    navController.navigate(
                        Ruta.Validacion.ruta
                    )
                },
                onSalir = {
                    salir()
                },
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        // 9. EDITAR PERFIL
        composable(Ruta.EditarPerfil.ruta) {

            val actual = usuario ?: return@composable

            EditarPerfilScreen(
                usuarioId = actual.idUsuario,
                onGuardado = {
                    usuario = bd.buscarUsuarioPorId(
                        actual.idUsuario
                    )
                    navController.popBackStack()
                },
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        // 10. PERSONAL E HISTORIAL
        composable(Ruta.Personal.ruta) {

            val actual = usuario ?: return@composable

            PersonalHistorialScreen(
                usuarioId = actual.idUsuario,
                onValidar = {
                    navController.navigate(
                        Ruta.Validacion.ruta
                    )
                },
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        // 11. VALIDACION Y REGISTRO
        composable(Ruta.Validacion.ruta) {

            val actual = usuario ?: return@composable

            ValidacionRegistroScreen(
                usuarioId = actual.idUsuario,
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        // 12. DETALLE DE EQUIPO
        composable(
            route = "${Ruta.Equipo.ruta}/{idEquipo}",
            arguments = listOf(
                navArgument("idEquipo") {
                    type = NavType.IntType
                }
            )
        ) { backStack ->

            val actual = usuario ?: return@composable

            val idEquipo =
                backStack.arguments?.getInt("idEquipo")
                    ?: return@composable

            EquipoScreen(
                idEquipo = idEquipo,
                usuario = actual,
                onAtras = {
                    navController.popBackStack()
                },
                onRegistrarPrueba = { tipo ->
                    navController.navigate(
                        rutaPrueba(idEquipo, tipo)
                    )
                },
                onIrActa = {
                    // Pendiente: conectar ActaScreen
                    // cuando esté implementada.
                }
            )
        }

        // 13. FORMULARIO DE PRUEBAS
        composable(
            route = "${Ruta.Prueba.ruta}/{idEquipo}/{tipo}",
            arguments = listOf(
                navArgument("idEquipo") {
                    type = NavType.IntType
                },
                navArgument("tipo") {
                    type = NavType.StringType
                }
            )
        ) { backStack ->

            val actual = usuario ?: return@composable

            val idEquipo =
                backStack.arguments?.getInt("idEquipo")
                    ?: return@composable

            val tipoNombre =
                backStack.arguments?.getString("tipo")
                    ?: return@composable

            val tipo = TipoPrueba.entries.find {
                it.name == tipoNombre
            } ?: return@composable

            PruebaFormScreen(
                idEquipo = idEquipo,
                tipo = tipo,
                usuario = actual,
                onAtras = {
                    navController.popBackStack()
                },
                onGuardado = {
                    navController.popBackStack()
                }
            )
        }
    }
}
