
package cl.duoc.induztek.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import cl.duoc.induztek.R
import cl.duoc.induztek.model.Usuario
import cl.duoc.induztek.ui.components.BarraInferior
import cl.duoc.induztek.ui.components.CampoBase
import cl.duoc.induztek.ui.components.SeccionBarra
import cl.duoc.induztek.ui.components.SeccionTitulo
import cl.duoc.induztek.ui.components.fondoPantallaInterna
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.TextoSecundario
import cl.duoc.induztek.ui.theme.InduztekTheme
import cl.duoc.induztek.ui.theme.BordeSuave
import java.io.File

@Composable
internal fun EditarPerfilContenido(
    usuarioActual: Usuario,
    foto: File?,
    banner: File?,
    nombre: String,
    onNombre: (String) -> Unit,
    error: String?,
    biografia: String,
    onBio: (String) -> Unit,
    onElegirImagen: (String) -> Unit,
    onSolicitar: (String, String, String) -> Boolean,
    cambioPendiente: (String) -> Boolean,
    aviso: String?,
    bajaPendiente: Boolean,
    onBajaPendiente: () -> Unit,
    onBorrarCuenta: () -> Unit,
    onConsultas: () -> Unit,
    onVolver: () -> Unit,
    onIrEquipos: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .fondoPantallaInterna()
    ) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Column(Modifier.padding(horizontal = Dimens.margenHorizontalPantalla)) {
                CabeceraVolver(
                    titulo = stringResource(R.string.perfil_editar),
                    onAtras = onVolver
                )
            }

            Box(Modifier.fillMaxWidth()) {
                BannerUsuario(
                    banner,
                    Modifier
                        .fillMaxWidth()
                        .height(Dimens.altoBannerEdicion)
                )
                BotonConIcono(
                    texto = stringResource(R.string.perfil_cambiar_banner),
                    icono = R.drawable.ic_camara,
                    onClick = { onElegirImagen(ImagenesPerfil.TIPO_BANNER) },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(Dimens.salto4)
                )
            }
            Column(Modifier.padding(horizontal = Dimens.margenHorizontalPantalla)) {
                Spacer(Modifier.height(Dimens.salto2))
                Text(
                    stringResource(R.string.perfil_banner_medida),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextoSecundario()
                )

                Spacer(Modifier.height(Dimens.espacioMedio))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarPersona(
                        nombre = usuarioActual.nombre,
                        foto = foto,
                        lado = Dimens.ladoFotoEdicion,
                        radio = Dimens.radioFotoEdicion,
                        borde = Dimens.bordeCampo,
                        colorBorde = BordeSuave()
                    )
                    Spacer(Modifier.width(Dimens.espacioEntreCampos))
                    BotonConIcono(
                        texto = stringResource(R.string.perfil_cambiar_foto),
                        icono = R.drawable.ic_camara,
                        onClick = { onElegirImagen(ImagenesPerfil.TIPO_FOTO) }
                    )
                }

                Spacer(Modifier.height(Dimens.espacioMedio))
                CampoBase(
                    valor = nombre,
                    onValorCambia = onNombre,
                    etiqueta = stringResource(R.string.perfil_nombre),
                    textoSugerido = usuarioActual.nombre,
                    error = error
                )

                Spacer(Modifier.height(Dimens.espacioEntreCampos))
                CamposBloqueados(usuarioActual, onSolicitar, cambioPendiente)

                Spacer(Modifier.height(Dimens.espacioMedio))
                SeccionTitulo(stringResource(R.string.perfil_acerca))
                CampoBase(
                    valor = biografia,
                    onValorCambia = onBio,
                    etiqueta = "",
                    textoSugerido = stringResource(R.string.perfil_bio_sugerido),
                    unaSolaLinea = false,
                    alto = Dimens.alturaCampoTextoLargo,
                    accionTeclado = androidx.compose.ui.text.input.ImeAction.Done
                )

                val textoAviso = aviso
                if (textoAviso != null) {
                    Spacer(Modifier.height(Dimens.salto3))
                    Text(textoAviso, style = MaterialTheme.typography.bodySmall, color = TextoSecundario())
                }

                Spacer(Modifier.height(Dimens.salto3))
                Text(
                    stringResource(R.string.perfil_guardar_aviso),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextoSecundario(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(Dimens.espacioMedio))
                EnlaceCentrado(stringResource(R.string.perfil_consultas)) { onConsultas() }
                Spacer(Modifier.height(Dimens.salto2))
                EnlaceCentrado(
                    stringResource(
                        if (bajaPendiente) R.string.perfil_baja_pendiente else R.string.perfil_borrar_cuenta
                    )
                ) {
                    if (bajaPendiente) onBajaPendiente() else onBorrarCuenta()
                }

                Spacer(Modifier.height(Dimens.espacioGrande))
            }
        }

        BarraInferior(
            activa = SeccionBarra.PERFIL,
            onIrEquipos = onIrEquipos,
            onIrPerfil = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun EditarPerfilPreview() {
    InduztekTheme {
        EditarPerfilContenido(
            usuarioActual = personaDePreview(),
            foto = null,
            banner = null,
            nombre = personaDePreview().nombre,
            onNombre = {},
            error = null,
            biografia = personaDePreview().bio,
            onBio = {},
            onElegirImagen = {},
            onSolicitar = { _, _, _ -> true },
            cambioPendiente = { false },
            aviso = null,
            bajaPendiente = false,
            onBajaPendiente = {},
            onBorrarCuenta = {},
            onConsultas = {},
            onVolver = {},
            onIrEquipos = {}
        )
    }
}
