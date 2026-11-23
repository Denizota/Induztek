
package cl.duoc.induztek.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

import cl.duoc.induztek.data.*
import cl.duoc.induztek.data.remote.Sincronia
import cl.duoc.induztek.model.Rol
import cl.duoc.induztek.model.TipoPrueba
import cl.duoc.induztek.model.Usuario

import cl.duoc.induztek.ui.screen.LoginScreen
import cl.duoc.induztek.ui.screen.SolicitarAccesoScreen
import cl.duoc.induztek.ui.screen.PuenteScreen
import cl.duoc.induztek.ui.screen.EquipoScreen
import cl.duoc.induztek.ui.screen.PruebaFormScreen

import cl.duoc.induztek.viewmodel.LoginViewModel

@Composable
fun AppNavegacion() {

    val navController = rememberNavController()
    val context = LocalContext.current

    val sesion = remember {
        SesionPreferencias(context)
    }

    val baseDatos = remember {
        InduztekDbHelper.obtener(context)
    }

    var usuario by remember {
        val guardado = baseDatos.buscarUsuarioPorId(sesion.leer())
        var inicial: Usuario? = null
        if (guardado != null && guardado.activo) {
            inicial = guardado
        }
        mutableStateOf(inicial)
    }

    val versionSincronia = Sincronia.version

    fun limpiarPila() {
        navController.popBackStack(navController.graph.startDestinationId, inclusive = true)
    }

    fun entrar(nuevoUsuario: Usuario) {
        usuario = nuevoUsuario
        sesion.guardar(nuevoUsuario.idUsuario)
        limpiarPila()

        navController.navigate(rutaTrasLogin(nuevoUsuario)) {
            launchSingleTop = true
        }
    }

    fun salir() {
        sesion.borrar()
        usuario = null
        limpiarPila()

        navController.navigate(Ruta.Login.ruta) {
            launchSingleTop = true
        }
    }

    LaunchedEffect(versionSincronia) {
        val actual = usuario
        if (actual != null) {
            val fresco = baseDatos.buscarUsuarioPorId(actual.idUsuario)
            if (fresco == null || !fresco.activo) {
                salir()
            } else {
                usuario = fresco
            }
        }
    }

    fun irA(destino: String) {
        var raiz = Ruta.Inicio.ruta
        val actual = usuario
        if (actual != null && actual.rol == Rol.CLIENTE) {
            raiz = Ruta.Cliente.ruta
        }
        navController.navigate(destino) {
            popUpTo(raiz) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun irAEquipos() {
        val actual = usuario
        if (actual != null && actual.rol == Rol.CLIENTE) {
            navController.navigate(Ruta.Cliente.ruta) {
                popUpTo(Ruta.Cliente.ruta) { inclusive = false }
                launchSingleTop = true
            }
        } else {
            irA(Ruta.Inicio.ruta)
        }
    }

    val sesionActiva = usuario
    val pantallaArranque = if (sesionActiva == null) Ruta.Login.ruta else rutaTrasLogin(sesionActiva)

    NavHost(
        navController = navController,
        startDestination = pantallaArranque
    ) {

        composable(Ruta.Login.ruta) {

            val modeloPantalla: LoginViewModel = viewModel()

            LoginScreen(
                viewModel = modeloPantalla,
                onIngresoExitoso = {
                    val nuevo = modeloPantalla.usuarioEnSesion()
                    if (nuevo != null) {
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

        rutasStaff(navController, { irA(it) }, { salir() })

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

        composable(
            route = "${Ruta.Equipo.ruta}/{idEquipo}",
            arguments = listOf(
                navArgument("idEquipo") {
                    type = NavType.IntType
                }
            )
        ) { backStack ->

            val actual = usuario
            if (actual == null) {
                return@composable
            }

            val argumentos = backStack.arguments
            if (argumentos == null) {
                return@composable
            }
            val idEquipo = argumentos.getInt("idEquipo")

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
                onIrActa = { idVisita ->
                    navController.navigate(rutaActa(idVisita, false))
                }
            )
        }

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

            val actual = usuario
            if (actual == null) {
                return@composable
            }

            val argumentos = backStack.arguments
            if (argumentos == null) {
                return@composable
            }
            val idEquipo = argumentos.getInt("idEquipo")

            val tipoNombre = argumentos.getString("tipo")
            if (tipoNombre == null) {
                return@composable
            }

            var tipo: TipoPrueba? = null
            for (posible in TipoPrueba.values()) {
                if (posible.name == tipoNombre) {
                    tipo = posible
                }
            }
            if (tipo == null) {
                return@composable
            }

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

        rutasCliente(navController, { usuario }, { irA(it) }, { salir() })
        rutasPerfil(navController, { irAEquipos() }, { salir() })
    }
}
