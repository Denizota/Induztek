
package cl.duoc.induztek.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import cl.duoc.induztek.R
import cl.duoc.induztek.model.LoginUiState
import cl.duoc.induztek.ui.components.BotonPrincipal
import cl.duoc.induztek.ui.components.BotonRedSocial
import cl.duoc.induztek.ui.components.CampoContrasena
import cl.duoc.induztek.ui.components.CampoTexto
import cl.duoc.induztek.ui.components.FondoInicio
import cl.duoc.induztek.ui.components.TextoConEnlace
import cl.duoc.induztek.ui.styles.EstilosBotonRedSocial
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.TextoSecundario
import cl.duoc.induztek.ui.theme.InduztekTheme
import cl.duoc.induztek.ui.theme.RojoRechazado

@Composable
fun LoginContent(
    uiState: LoginUiState,
    onCorreoCambia: (String) -> Unit,
    onContrasenaCambia: (String) -> Unit,
    onAlternarVisibilidad: () -> Unit,
    onIngresar: ()-> Unit,
    onSolicitarAcceso: () -> Unit,
    onOlvideContrasena: () -> Unit = {},
    onGoogle: () -> Unit = {},
    mensajeGoogle: String? = null,
    googleEnCurso: Boolean = false
){
    var avisoRed by remember { mutableStateOf<String?>(null) }

    FondoInicio(
        cabecera = {
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = Dimens.margenHorizontalPantalla)
                    .padding(bottom = Dimens.salto7)
            ) {
                Text(
                    text = stringResource(R.string.titulo_login).trim(),
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White
                )
                Spacer(Modifier.height(Dimens.salto2))
                Text(
                    text = stringResource(R.string.eslogan).trim(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.78f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    ) {
        Column(
            Modifier
                .navigationBarsPadding()
                .padding(horizontal = Dimens.margenHorizontalPantalla)
        ) {
            Spacer(Modifier.height(Dimens.espacioAntescorreo))

            CampoTexto(
                valor = uiState.correo,
                onValorCambia = onCorreoCambia,
                etiqueta = stringResource(R.string.campo_correo),
                textoSugerido = stringResource(R.string.placeholder_correo),
                error = uiState.errorCorreo,
                tipoTeclado = KeyboardType.Email,
                iconoInicial = painterResource(R.drawable.ic_correo)
            )
            Spacer(Modifier.height(Dimens.espacioEntreCampos))
            CampoContrasena(
                valor = uiState.contrasena,
                onValorCambia = onContrasenaCambia,
                etiqueta = stringResource(R.string.campo_contrasena),
                textoSugerido = stringResource(R.string.placeholder_contrasena),
                visible = uiState.contrasenaVisible,
                onAlternarVisible = onAlternarVisibilidad,
                error = uiState.errorContrasena,
                accionTeclado = ImeAction.Done,
                iconoInicial = painterResource(R.drawable.ic_candado_claro)
            )

            Text(
                text = stringResource(R.string.enlace_olvido),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = Dimens.espacioMini)
                    .clickable(role = Role.Button, onClick = onOlvideContrasena)
            )

            Spacer(Modifier.height(Dimens.espacioAntesBoton))

            BotonPrincipal(
                texto = stringResource(R.string.btn_ingresar),
                onClick = onIngresar,
                habilitado = uiState.ingresarHabilitado,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(Dimens.espacioAntesRedes))

            Text(
                text = stringResource(R.string.texto_o_redes),
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario(),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(Dimens.espacioTextoRedes))

            Row (
                modifier = Modifier
                    .fillMaxWidth(EstilosBotonRedSocial.anchoRelativoDelGrupo)
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.spacedBy(Dimens.espacioRedes)
            ) {
                BotonRedSocial(
                    icono = R.drawable.ic_google,
                    descripcion = stringResource(R.string.cd_google),
                    onClick = onGoogle,
                    modifier = Modifier.weight(1f)
                )
                BotonRedSocial(
                    icono = R.drawable.ic_linkedin,
                    descripcion = stringResource(R.string.cd_linkedin),
                    onClick = { avisoRed = "LinkedIn" },
                    modifier = Modifier.weight(1f)
                )
                BotonRedSocial(
                    icono = R.drawable.ic_facebook,
                    descripcion = stringResource(R.string.cd_facebook),
                    onClick = { avisoRed = "Facebook" },
                    modifier = Modifier.weight(1f)
                )
            }

            if (mensajeGoogle != null) {
                Spacer(Modifier.height(Dimens.salto3))
                Text(
                    text = mensajeGoogle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (googleEnCurso) TextoSecundario() else RojoRechazado(),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(Dimens.espacioAntesSinCuenta))

            TextoConEnlace(
                textoNormal = stringResource(R.string.texto_sin_cuenta),
                textoEnlace = stringResource(R.string.enlace_registrate),
                onClickEnlace = onSolicitarAcceso
            )

            Spacer(Modifier.height(Dimens.espacioGrande))
        }
    }

    val red = avisoRed
    if (red != null) {
        AlertDialog(
            onDismissRequest = { avisoRed = null },
            title = { Text(stringResource(R.string.red_no_disponible_titulo, red)) },
            text = { Text(stringResource(R.string.red_no_disponible_texto, red)) },
            confirmButton = {
                TextButton(onClick = { avisoRed = null }) {
                    Text(stringResource(R.string.btn_entendido))
                }
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Vacío")
@Composable
private fun LoginVacioPreview() {
    InduztekTheme {
        LoginContent(
            uiState = LoginUiState(),
            onCorreoCambia = {}, onContrasenaCambia = {}, onAlternarVisibilidad = {},
            onIngresar = {}, onSolicitarAcceso = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Con datos y error")
@Composable
private fun LoginConDatosPreview() {
    InduztekTheme {
        LoginContent(
            uiState = LoginUiState(
                correo = "ana@",
                contrasena = "123456",
                errorCorreo = "Ingresa un correo válido.",
                errorContrasena = "Ingresa una contraseña válida."
            ),
            onCorreoCambia = {}, onContrasenaCambia = {}, onAlternarVisibilidad = {},
            onIngresar = {}, onSolicitarAcceso = {}
        )
    }
}
