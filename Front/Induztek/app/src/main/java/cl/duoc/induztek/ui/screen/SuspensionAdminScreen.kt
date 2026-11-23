
package cl.duoc.induztek.ui.screen

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.induztek.R
import cl.duoc.induztek.ui.components.BotonPrincipal
import cl.duoc.induztek.ui.components.CajaBordeada
import cl.duoc.induztek.ui.components.CampoBase
import cl.duoc.induztek.ui.components.fondoPantallaInterna
import cl.duoc.induztek.ui.theme.Blanco
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.TextoSecundario
import cl.duoc.induztek.ui.theme.InduztekTheme
import cl.duoc.induztek.ui.theme.RojoRechazado
import cl.duoc.induztek.ui.theme.TextoTitulo
import cl.duoc.induztek.viewmodel.InicioViewModel


@Composable
fun SuspensionScreen(
    idUsuario: Int,
    onAtras: () -> Unit,
    modeloPantalla: InicioViewModel = viewModel()
) {
    versionSincronia()
    val persona = modeloPantalla.usuario(idUsuario)
    if (persona == null) return

    var motivo by rememberSaveable { mutableStateOf("") }
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }
    val tSinMotivo = stringResource(R.string.admin_sin_motivo)

    val enSesion = modeloPantalla.usuarioEnSesion()
    var esPropia = false
    if (enSesion != null && enSesion.idUsuario == idUsuario) esPropia = true

    SuspensionContenido(
        nombre = persona.nombre,
        idUsuario = idUsuario,
        telefono = persona.telefono,
        correo = persona.correo,
        suspendida = !persona.activo,
        propia = esPropia,
        motivoGuardado = modeloPantalla.motivoDe(idUsuario),
        motivo = motivo,
        aviso = aviso,
        onMotivo = { motivo = it; aviso = null },
        onConfirmar = {
            val limpio = motivo.trim()
            if (persona.activo) {
                if (limpio.length < 5) aviso = tSinMotivo
                else if (modeloPantalla.suspender(idUsuario, limpio)) onAtras()
            } else if (modeloPantalla.reactivar(idUsuario)) {
                onAtras()
            }
        },
        onAtras = onAtras
    )
}

@Composable
private fun SuspensionContenido(
    nombre: String,
    telefono: String,
    correo: String,
    suspendida: Boolean,
    propia: Boolean,
    motivoGuardado: String,
    motivo: String,
    aviso: String?,
    onMotivo: (String) -> Unit,
    onConfirmar: () -> Unit,
    onAtras: () -> Unit,
    idUsuario: Int = 0
) {
    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .fondoPantallaInterna()
    ) {
        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = Dimens.margenHorizontalPantalla)
        ) {
            Text(
                stringResource(R.string.nombre_empresa),
                style = MaterialTheme.typography.labelLarge,
                color = TextoTitulo()
            )
            Spacer(Modifier.height(Dimens.salto1))
            CabeceraVolver(
                stringResource(R.string.admin_suspension_titulo),
                onAtras = onAtras
            )
            Spacer(Modifier.height(Dimens.espacioMedio))

            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarCuenta(idUsuario, nombre)
                Spacer(Modifier.width(Dimens.espacioEntreCampos))
                Column(Modifier.weight(1f)) {
                    Text(nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        telefono.ifBlank { "—" },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario()
                    )
                    Text(
                        correo.ifBlank { "—" },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario()
                    )
                }
            }

            Spacer(Modifier.height(Dimens.espacioMedio))

            if (suspendida) {
                CajaBordeada(Modifier.fillMaxWidth()) {
                    Text(
                        stringResource(R.string.admin_estado_suspendida),
                        style = MaterialTheme.typography.bodyMedium,
                        color = RojoRechazado(),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(Dimens.salto2))
                    Text(
                        stringResource(R.string.admin_motivo_guardado, motivoGuardado.ifBlank { "—" }),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario()
                    )
                }
                Spacer(Modifier.height(Dimens.espacioMedio))
                BotonPrincipal(
                    texto = stringResource(R.string.admin_reactivar),
                    onClick = onConfirmar,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                CampoBase(
                    valor = motivo,
                    onValorCambia = onMotivo,
                    etiqueta = stringResource(R.string.admin_justificativo),
                    textoSugerido = stringResource(R.string.admin_justificativo_sugerido),
                    error = aviso,
                    unaSolaLinea = false,
                    alto = Dimens.alturaCampoTextoLargo
                )
                Spacer(Modifier.height(Dimens.espacioMedio))
                BotonPrincipal(
                    texto = stringResource(R.string.admin_suspender_cuenta),
                    onClick = onConfirmar,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(Dimens.salto3))
            BotonBordeadoPropio(
                texto = stringResource(R.string.inicio_reiniciar_no),
                habilitado = true,
                onClick = onAtras,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(Dimens.espacioMedio))
            Text(
                stringResource(R.string.admin_suspension_nota),
                style = MaterialTheme.typography.labelSmall,
                color = TextoSecundario()
            )
            Spacer(Modifier.height(Dimens.espacioGrande))
        }
    }
}

@Composable
private fun AvatarCuenta(idUsuario: Int, nombre: String) {
    AvatarPersona(
        nombre = nombre,
        foto = imagenGuardada(idUsuario, ImagenesPerfil.TIPO_FOTO),
        lado = Dimens.tamanoAvatar,
        borde = Dimens.grosorBordeAvatar,
        colorBorde = Blanco
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun SuspensionPreview() {
    InduztekTheme {
        SuspensionContenido(
            nombre = "Víctor Isidro R.",
            telefono = "",
            correo = "visidro@envpacifico.ejemplo",
            suspendida = false,
            propia = false,
            motivoGuardado = "",
            motivo = "",
            aviso = null,
            onMotivo = {},
            onConfirmar = {},
            onAtras = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun SuspensionYaSuspendidaPreview() {
    InduztekTheme {
        SuspensionContenido(
            nombre = "Marcela Fuentes Rojas",
            telefono = "",
            correo = "mfuentes@envpacifico.ejemplo",
            suspendida = true,
            propia = false,
            motivoGuardado = "Pidió acceso fuera de hora y no respondió la validación.",
            motivo = "",
            aviso = null,
            onMotivo = {},
            onConfirmar = {},
            onAtras = {}
        )
    }
}
