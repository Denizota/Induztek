
package cl.duoc.induztek.ui.screen

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.induztek.R
import cl.duoc.induztek.permisos.Permisos
import cl.duoc.induztek.ui.components.CampoBase
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.TextoSecundario
import cl.duoc.induztek.viewmodel.ColaboracionViewModel


@Composable
fun EditarPerfilScreen(
    onAtras: () -> Unit,
    onIrEquipos: () -> Unit,
    onIrConsultas: () -> Unit,
    modeloPantalla: ColaboracionViewModel = viewModel()
) {
    val usuarioActual = modeloPantalla.usuarioEnSesion()
    if (usuarioActual == null) return
    val contexto = LocalContext.current
    val imagenes = remember(contexto) { ImagenesPerfil(contexto) }
    val hayCamara = remember {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).resolveActivity(contexto.packageManager) != null
    }

    val tFotoArt = stringResource(R.string.perfil_foto_art)
    val tBannerArt = stringResource(R.string.perfil_banner_art)
    val tErrorImagen = stringResource(R.string.perfil_imagen_error)
    val tSinCamara = stringResource(R.string.perfil_sin_camara)
    val tPermisoCamara = stringResource(R.string.perfil_permiso_camara)
    val tSolicitudOk = stringResource(R.string.perfil_solicitud_ok)
    val tBajaPendiente = stringResource(R.string.perfil_baja_pendiente)
    val tNombreCorto = stringResource(R.string.perfil_nombre_corto)
    val tBajaCorta = stringResource(R.string.perfil_baja_corta)
    val tBajaEnviada = stringResource(R.string.perfil_baja_enviada)

    var nombre by rememberSaveable { mutableStateOf(usuarioActual.nombre) }
    var textoBiografia by rememberSaveable { mutableStateOf(usuarioActual.bio) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }
    var version by rememberSaveable { mutableIntStateOf(0) }
    var pidiendo by rememberSaveable { mutableStateOf<String?>(null) }
    var permisoNegado by rememberSaveable { mutableStateOf(false) }
    var pidiendoBaja by rememberSaveable { mutableStateOf(false) }
    var motivoBaja by rememberSaveable { mutableStateOf("") }
    var errorBaja by rememberSaveable { mutableStateOf<String?>(null) }

    var tipoPendiente by rememberSaveable { mutableStateOf<String?>(null) }

    val elegirDelTelefono = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val tipo = tipoPendiente
        tipoPendiente = null
        if (uri != null && tipo != null) {
            if (imagenes.guardarDesdeTelefono(usuarioActual.idUsuario, tipo, uri)) version++
            else aviso = tErrorImagen
        }
    }

    val tomarFoto = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        val tipo = tipoPendiente
        tipoPendiente = null
        if (bitmap != null && tipo != null) {
            if (imagenes.guardarDesdeCamara(usuarioActual.idUsuario, tipo, bitmap)) version++
            else aviso = tErrorImagen
        }
    }

    fun abrirCamara() {
        try {
            tomarFoto.launch(null)
        } catch (e: ActivityNotFoundException) {
            aviso = tSinCamara
        }
    }

    val pedirPermisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) abrirCamara() else permisoNegado = true
    }

    val foto = remember(version) { imagenes.archivo(usuarioActual.idUsuario, ImagenesPerfil.TIPO_FOTO) }
    val banner = remember(version) { imagenes.archivo(usuarioActual.idUsuario, ImagenesPerfil.TIPO_BANNER) }

    EditarPerfilContenido(
        usuarioActual = usuarioActual,
        foto = foto,
        banner = banner,
        nombre = nombre,
        onNombre = { nombre = it; error = null },
        error = error,
        biografia = textoBiografia,
        onBio = { textoBiografia = it },
        onElegirImagen = { tipo -> pidiendo = tipo; tipoPendiente = tipo },
        onSolicitar = { campo, valor, motivo ->
            val solicitudAceptada = modeloPantalla.solicitarCambio(usuarioActual, campo, valor, motivo)
            if (solicitudAceptada) aviso = tSolicitudOk
            solicitudAceptada
        },
        cambioPendiente = { campo -> modeloPantalla.cambioPendiente(usuarioActual.idUsuario, campo) },
        aviso = aviso,
        bajaPendiente = modeloPantalla.bajaPendiente(usuarioActual.idUsuario),
        onBajaPendiente = { aviso = tBajaPendiente },
        onBorrarCuenta = { pidiendoBaja = true; errorBaja = null },
        onConsultas = onIrConsultas,
        onVolver = {
            if (modeloPantalla.guardarPerfil(usuarioActual.idUsuario, nombre, textoBiografia)) onAtras()
            else error = tNombreCorto
        },
        onIrEquipos = onIrEquipos
    )

    val tipo = pidiendo
    if (tipo != null) {
        val articulo = if (tipo == ImagenesPerfil.TIPO_FOTO) tFotoArt else tBannerArt
        AlertDialog(
            onDismissRequest = { pidiendo = null },
            title = { Text(stringResource(R.string.perfil_imagen_titulo, articulo)) },
            text = {
                Column {
                    FilaEleccion(stringResource(R.string.perfil_desde_telefono)) {
                        pidiendo = null
                        elegirDelTelefono.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                    FilaEleccion(stringResource(R.string.perfil_desde_camara), enabled = hayCamara) {
                        if (!hayCamara) aviso = tSinCamara
                        else {
                            pidiendo = null
                            if (Permisos.tiene(contexto, Permisos.CAMARA)) abrirCamara()
                            else pedirPermisoCamara.launch(Permisos.CAMARA)
                        }
                    }
                    FilaEleccion(
                        stringResource(R.string.perfil_quitar_imagen, articulo),
                        enabled = imagenes.archivo(usuarioActual.idUsuario, tipo) != null
                    ) {
                        imagenes.borrar(usuarioActual.idUsuario, tipo)
                        version++
                        pidiendo = null
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { pidiendo = null }) {
                    Text(stringResource(R.string.inicio_reiniciar_no))
                }
            }
        )
    }

    if (permisoNegado) {
        AlertDialog(
            onDismissRequest = { permisoNegado = false },
            title = { Text(stringResource(R.string.perfil_permiso_camara_titulo)) },
            text = { Text(tPermisoCamara) },
            confirmButton = {
                TextButton(onClick = {
                    permisoNegado = false
                    Permisos.abrirAjustes(contexto)
                }) {
                    Text(stringResource(R.string.inicio_menu_abrir_ajustes))
                }
            },
            dismissButton = {
                TextButton(onClick = { permisoNegado = false }) {
                    Text(stringResource(R.string.inicio_reiniciar_no))
                }
            }
        )
    }

    if (pidiendoBaja) {
        AlertDialog(
            onDismissRequest = { pidiendoBaja = false },
            title = { Text(stringResource(R.string.perfil_baja_titulo)) },
            text = {
                Column {
                    Text(
                        stringResource(R.string.perfil_baja_texto),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario()
                    )
                    Spacer(Modifier.height(Dimens.salto4))
                    CampoBase(
                        valor = motivoBaja,
                        onValorCambia = { motivoBaja = it; errorBaja = null },
                        etiqueta = stringResource(R.string.perfil_baja_motivo),
                        textoSugerido = stringResource(R.string.perfil_baja_motivo_sugerido),
                        error = errorBaja,
                        unaSolaLinea = false,
                        alto = Dimens.alturaCampoTextoLargo,
                        accionTeclado = androidx.compose.ui.text.input.ImeAction.Done
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val limpio = motivoBaja.trim()
                    if (limpio.length < 5) {
                        errorBaja = tBajaCorta
                    } else if (modeloPantalla.solicitarBaja(usuarioActual, limpio)) {
                        pidiendoBaja = false
                        motivoBaja = ""
                        aviso = tBajaEnviada
                    } else {
                        pidiendoBaja = false
                        aviso = tBajaPendiente
                    }
                }) {
                    Text(stringResource(R.string.perfil_baja_enviar))
                }
            },
            dismissButton = {
                TextButton(onClick = { pidiendoBaja = false }) {
                    Text(stringResource(R.string.inicio_reiniciar_no))
                }
            }
        )
    }
}
