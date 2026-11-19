
package cl.duoc.induztek.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.induztek.R
import cl.duoc.induztek.model.ROLES_CREABLES
import cl.duoc.induztek.model.Rol
import cl.duoc.induztek.model.Solicitud
import cl.duoc.induztek.ui.components.CampoBase
import cl.duoc.induztek.ui.components.CampoContrasena
import cl.duoc.induztek.ui.components.CampoSeleccion
import cl.duoc.induztek.ui.components.MensajeErrorCampo
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.viewmodel.InicioViewModel


@Composable
fun SolicitudesScreen(
    onAtras: () -> Unit,
    modeloPantalla: InicioViewModel = viewModel()
) {
    var version by rememberSaveable { mutableIntStateOf(0) }
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }
    var creando by rememberSaveable { mutableStateOf<Int?>(null) }
    var rut by rememberSaveable { mutableStateOf("") }
    var rolNombre by rememberSaveable { mutableStateOf("") }
    var empresaNombre by rememberSaveable { mutableStateOf("") }
    var clave by rememberSaveable { mutableStateOf("") }
    var claveVisible by rememberSaveable { mutableStateOf(false) }
    var errorRut by rememberSaveable { mutableStateOf<String?>(null) }
    var errorClave by rememberSaveable { mutableStateOf<String?>(null) }
    var errorGeneral by rememberSaveable { mutableStateOf<String?>(null) }

    val pendientes = remember(version) { modeloPantalla.solicitudes() }
    val aprobadas = remember(version) { modeloPantalla.solicitudesAprobadas() }
    val empresas = remember(version) { modeloPantalla.empresas() }
    val tErrorBase = stringResource(R.string.sol_error)
    val tRolFalta = stringResource(R.string.sol_rol_falta)
    val tRutInvalido = stringResource(R.string.sol_rut_invalido)
    val tRutRepetido = stringResource(R.string.sol_rut_repetido)
    val tClaveCorta = stringResource(R.string.sol_clave_corta)

    val nombresRoles = mutableListOf<String>()
    for (rol in ROLES_CREABLES) {
        nombresRoles.add(rol.nombre)
    }
    val nombresEmpresas = mutableListOf<String>()
    for (empresa in empresas) {
        nombresEmpresas.add(empresa.nombre)
    }

    SolicitudesContenido(
        pendientes = pendientes,
        aprobadas = aprobadas,
        aviso = aviso,
        onResolver = { sol, aprobar ->
            var resultado = modeloPantalla.resolver(sol, aprobar)
            if (resultado == null) {
                resultado = tErrorBase
            }
            aviso = resultado
            version++
        },
        onCrear = { sol ->
            creando = sol.idSolicitud
            rut = ""; rolNombre = ""; empresaNombre = ""
            clave = ""; claveVisible = false
            errorRut = null; errorClave = null; errorGeneral = null
        },
        onAtras = onAtras
    )

    var encontrada: Solicitud? = null
    for (solicitud in aprobadas) {
        if (solicitud.idSolicitud == creando) {
            encontrada = solicitud
        }
    }
    val elegida = encontrada
    if (elegida != null) {
        val textoCreado = stringResource(R.string.sol_creado, elegida.nombreCompleto)
        AlertDialog(
            onDismissRequest = { creando = null },
            title = { Text(stringResource(R.string.sol_crear_titulo, elegida.nombreCompleto)) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    CampoBase(
                        valor = rut,
                        onValorCambia = { rut = it; errorRut = null },
                        etiqueta = stringResource(R.string.sol_rut),
                        textoSugerido = stringResource(R.string.sol_rut_sugerido),
                        error = errorRut
                    )
                    Spacer(Modifier.height(Dimens.espacioEntreCampos))
                    CampoSeleccion(
                        valor = rolNombre,
                        opciones = nombresRoles,
                        onSeleccionar = { rolNombre = it; errorGeneral = null },
                        etiqueta = stringResource(R.string.sol_rol),
                        textoSugerido = stringResource(R.string.sol_rol_sugerido)
                    )
                    if (rolNombre == Rol.CLIENTE.nombre) {
                        Spacer(Modifier.height(Dimens.espacioEntreCampos))
                        CampoSeleccion(
                            valor = empresaNombre,
                            opciones = nombresEmpresas,
                            onSeleccionar = { empresaNombre = it; errorGeneral = null },
                            etiqueta = stringResource(R.string.sol_empresa),
                            textoSugerido = stringResource(R.string.sol_empresa_sugerida)
                        )
                    }
                    Spacer(Modifier.height(Dimens.espacioEntreCampos))
                    CampoContrasena(
                        valor = clave,
                        onValorCambia = { clave = it; errorClave = null },
                        etiqueta = stringResource(R.string.sol_clave),
                        textoSugerido = stringResource(R.string.sol_clave_sugerido),
                        visible = claveVisible,
                        onAlternarVisible = { claveVisible = !claveVisible },
                        error = errorClave,
                        accionTeclado = ImeAction.Done
                    )
                    MensajeErrorCampo(errorGeneral, modifier = Modifier.padding(top = Dimens.espacioEntreCampos))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    errorRut = null; errorClave = null; errorGeneral = null
                    var rol: Rol? = null
                    for (posible in ROLES_CREABLES) {
                        if (posible.nombre == rolNombre) {
                            rol = posible
                        }
                    }
                    if (rol == null) {
                        errorGeneral = tRolFalta
                    } else {
                        var empresa: Int? = null
                        for (posible in empresas) {
                            if (posible.nombre == empresaNombre) {
                                empresa = posible.idUsuario
                            }
                        }
                        val error = modeloPantalla.crearCuenta(elegida, rut, rol, clave, empresa)
                        if (error == null) {
                            aviso = textoCreado
                            creando = null
                            version++
                        } else if (error == tRutInvalido || error == tRutRepetido) {
                            errorRut = error
                        } else if (error == tClaveCorta) {
                            errorClave = error
                        } else {
                            errorGeneral = error
                        }
                    }
                }) {
                    Text(stringResource(R.string.sol_crear_boton))
                }
            },
            dismissButton = {
                TextButton(onClick = { creando = null }) {
                    Text(stringResource(R.string.sol_crear_no))
                }
            }
        )
    }
}
