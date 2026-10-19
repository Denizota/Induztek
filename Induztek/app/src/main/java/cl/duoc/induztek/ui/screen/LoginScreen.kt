package cl.duoc.induztek.ui.screen



import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.induztek.R
import cl.duoc.induztek.ui.components.BotonPrincipal
import cl.duoc.induztek.ui.components.BotonRedSocial
import cl.duoc.induztek.ui.components.CampoContrasena
import cl.duoc.induztek.ui.components.CampoTexto
import cl.duoc.induztek.ui.components.EncabezadoInicio
import cl.duoc.induztek.ui.components.TextoConEnlace
import cl.duoc.induztek.ui.styles.EstilosBotonRedSocial
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.InduztekTheme
import cl.duoc.induztek.ui.theme.TextoGris
import cl.duoc.induztek.viewmodel.LoginViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import cl.duoc.induztek.model.LoginUiState
import cl.duoc.induztek.ui.components.FondoInicio
import cl.duoc.induztek.ui.theme.TextoOlvido


@Composable
fun LoginScreen(
    onIngresoExitoso: () -> Unit,
    onSolicitarAcceso: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LoginContent(
        uiState = uiState,
        onCorreoCambia = viewModel::onCorreoCambia,
        onContrasenaCambia = viewModel::onContrasenaCambia,
        onAlternarVisibilidad = viewModel::onAlternarVisibilidad,
        onIngresar = { if (viewModel.onIngresar()) onIngresoExitoso},
        onSolicitarAcceso = onSolicitarAcceso
    )
}

@Composable
fun LoginContent(
    uiState: LoginUiState,
    onCorreoCambia: (String) -> Unit,
    onContrasenaCambia: (String) -> Unit,
    onAlternarVisibilidad: () -> Unit,
    onIngresar: ()-> Unit,
    onSolicitarAcceso: () -> Unit,
    onOlvideContrasena: () -> Unit = {}
){
    FondoInicio{
        Column(
            modifier = Modifier.fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.margenHorizontalPantalla)
        ) {
            Spacer(Modifier.height(Dimens.espacioSuperiorencabezado))
            EncabezadoInicio(
                titulo = stringResource(R.string.titulo_login),
                subtitulo = stringResource(R.string.eslogan)
            )
            Spacer(Modifier.height(Dimens.espacioEncabezadoACuerpo))

            //espacio para el entrar como.
            //todavia no existe...
            Spacer(Modifier.height(Dimens.alturaEntrarcomo))

            Spacer(Modifier.height(Dimens.espacioAntescorreo))
            Text(
                text = stringResource(R.string.texto_o_correo),
                style = MaterialTheme.typography.labelSmall,
                color = TextoGris
            )

            Spacer(Modifier.height(Dimens.espacioDespuesTextoCorreo))

            CampoTexto(
                valor = uiState.correo,
                onValorCambia = onCorreoCambia,
                etiqueta = stringResource(R.string.campo_correo),
                textoSugerido = stringResource(R.string.placeholder_correo),
                error = uiState.errorCorreo,
                tipoTeclado = KeyboardType.Email
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
                accionTeclado = ImeAction.Done
            )

            Text(
                text = stringResource(R.string.enlace_olvido),
                style = MaterialTheme.typography.bodySmall,
                color = TextoOlvido,
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
                color = TextoGris,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(Dimens.espacioTextoRedes))

            // Los 3 cuadros todavía no hacen nada (onClick vacío).
            Row (
                modifier = Modifier
                    .fillMaxWidth(EstilosBotonRedSocial.anchoRelativoDelGrupo)
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.spacedBy(Dimens.espacioRedes)
            ) {
                BotonRedSocial(
                    icono = R.drawable.ic_google,
                    descripcion = stringResource(R.string.cd_google),
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                BotonRedSocial(
                    icono = R.drawable.ic_linkedin,
                    descripcion = stringResource(R.string.cd_linkedin),
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                BotonRedSocial(
                    icono = R.drawable.ic_facebook,
                    descripcion = stringResource(R.string.cd_facebook),
                    onClick = {},
                    modifier = Modifier.weight(1f)
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
                errorCorreo = "Ingresa un correo válido !!",
                errorContrasena = "Ingresa una contrasenia valida !!"
            ),
            onCorreoCambia = {}, onContrasenaCambia = {}, onAlternarVisibilidad = {},
            onIngresar = {}, onSolicitarAcceso = {}
        )
    }
}