package cl.duoc.induztek.ui.screen

import android.widget.Toast
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.induztek.R
import cl.duoc.induztek.model.SolicitudAccesoUiState
import cl.duoc.induztek.ui.components.BotonPrincipal
import cl.duoc.induztek.ui.components.CampoContrasena
import cl.duoc.induztek.ui.components.CampoSeleccion
import cl.duoc.induztek.ui.components.CampoTexto
import cl.duoc.induztek.ui.components.EncabezadoInicio
import cl.duoc.induztek.ui.components.FondoInicio
import cl.duoc.induztek.ui.components.TextoConEnlace
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.InduztekTheme
import cl.duoc.induztek.viewmodel.SolicitudAccesoViewModel

@Composable
fun SolicitarAccesoScreen(
    onSolicitudEnviada: () -> Unit,
    onVolverAIngreso: () -> Unit,
    viewModel: SolicitudAccesoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    SolicitarAccesoContent(
        uiState = uiState,
        onNombreCambia = viewModel::onNombreCambia,
        onApellidoCambia = viewModel::onApellidoCambia,
        onPaisSeleccionado = viewModel::onPaisSeleccionado,
        onCiudadSeleccionada = viewModel::onCiudadSeleccionada,
        onCorreoCambia = viewModel::onCorreoCambia,
        onContrasenaCambia = viewModel::onContrasenaCambia,
        onRepetirContrasenaCambia = viewModel::onRepetirContrasenaCambia,
        onJustificacionCambia = viewModel::onJustificacionCambia,
        onAlternarVisibilidadContrasena = viewModel::onAlternarVisibilidadContrasena,
        onAlternarVisibilidadRepetirContrasena = viewModel::onAlternarVisibilidadRepetirContrasena,
        onRegistrarse = {
            if (viewModel.onRegistrarse()) {
                Toast.makeText(context, "¡Tu solicitud ha sido enviada ! ", Toast.LENGTH_LONG).show()
                onSolicitudEnviada()
            }
        },
        onVolverAIngreso = onVolverAIngreso

    )
}

@Composable
fun SolicitarAccesoContent(
    uiState: SolicitudAccesoUiState,
    onNombreCambia: (String) -> Unit,
    onApellidoCambia: (String) -> Unit,
    onPaisSeleccionado: (String) -> Unit,
    onCiudadSeleccionada: (String) -> Unit,
    onCorreoCambia: (String) -> Unit,
    onContrasenaCambia: (String) -> Unit,
    onRepetirContrasenaCambia: (String) -> Unit,
    onJustificacionCambia: (String) -> Unit,
    onAlternarVisibilidadContrasena: () -> Unit,
    onAlternarVisibilidadRepetirContrasena: () -> Unit,
    onRegistrarse: () -> Unit,
    onVolverAIngreso: () -> Unit
) {
    FondoInicio {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding() // respeta barra de estado, barra de abajo y teclado
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.margenHorizontalPantalla)
        ) {
            Spacer(Modifier.height(Dimens.espacioSuperiorencabezado))

            EncabezadoInicio(
                titulo = stringResource(R.string.titulo_solicitar_acceso),
                subtitulo = stringResource(R.string.subtitulo_solicitar_acceso)
            )

            Spacer(Modifier.height(Dimens.espacioEncabezadoACuerpo))



            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioNombreApellido)) {
                CampoTexto(
                    valor = uiState.nombre,
                    onValorCambia = onNombreCambia,
                    etiqueta = stringResource(R.string.campo_nombre),
                    textoSugerido = stringResource(R.string.placeholder_nombre),
                    error = uiState.errorNombre,
                    modifier = Modifier.weight(1f)
                )
                CampoTexto(
                    valor = uiState.apellido,
                    onValorCambia = onApellidoCambia,
                    etiqueta = stringResource(R.string.campo_apellido),
                    textoSugerido = stringResource(R.string.placeholder_apellido),
                    error = uiState.errorApellido,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(Dimens.espacioEntreCampos))

            CampoSeleccion(
                valor = uiState.pais,
                opciones = uiState.paises,
                onSeleccionar = onPaisSeleccionado,
                etiqueta = stringResource(R.string.campo_pais),
                textoSugerido = stringResource(R.string.placeholder_seleccion)
            )

            Spacer(Modifier.height(Dimens.espacioEntreCampos))

            CampoSeleccion(
                valor = uiState.ciudad,
                opciones = uiState.ciudades,
                onSeleccionar = onCiudadSeleccionada,
                etiqueta = stringResource(R.string.campo_ciudad),
                textoSugerido = stringResource(R.string.placeholder_seleccion)
            )

            Spacer(Modifier.height(Dimens.espacioEntreCampos))

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
                onAlternarVisible = onAlternarVisibilidadContrasena,
                error = uiState.errorContrasena
            )

            Spacer(Modifier.height(Dimens.espacioEntreCampos))

            CampoContrasena(
                valor = uiState.repetirContrasena,
                onValorCambia = onRepetirContrasenaCambia,
                etiqueta = stringResource(R.string.campo_repetir_contrasena),
                textoSugerido = stringResource(R.string.placeholder_contrasena),
                visible = uiState.repetirContrasenaVisible,
                onAlternarVisible = onAlternarVisibilidadRepetirContrasena,
                error = uiState.errorRepetirContrasena
            )

            Spacer(Modifier.height(Dimens.espacioEntreCampos))

            CampoTexto(
                valor = uiState.justificacion,
                onValorCambia = onJustificacionCambia,
                etiqueta = stringResource(R.string.campo_justificacion),
                textoSugerido = stringResource(R.string.placeholder_justificacion),
                alto = Dimens.alturaCampoTextoLargo,
                unaSolaLinea = false,
                accionTeclado = ImeAction.Default
            )

            Spacer(Modifier.height(Dimens.espacioMedio))

            BotonPrincipal(
                texto = stringResource(R.string.btn_registrarse),
                onClick = onRegistrarse,
                habilitado = uiState.registrarseHabilitado,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(Dimens.espacioMedio))

            TextoConEnlace(
                textoNormal = stringResource(R.string.texto_con_cuenta),
                textoEnlace = stringResource(R.string.enlace_inicia_sesion),
                onClickEnlace = onVolverAIngreso
            )

            Spacer(Modifier.height(Dimens.espacioGrande))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Vacío")
@Composable
private fun SolicitarAccesoVacioPreview() {
    InduztekTheme {
        SolicitarAccesoContent(
            uiState = SolicitudAccesoUiState(),
            onNombreCambia = {}, onApellidoCambia = {}, onPaisSeleccionado = {},
            onCiudadSeleccionada = {}, onCorreoCambia = {}, onContrasenaCambia = {},
            onRepetirContrasenaCambia = {}, onJustificacionCambia = {},
            onAlternarVisibilidadContrasena = {}, onAlternarVisibilidadRepetirContrasena = {},
            onRegistrarse = {}, onVolverAIngreso = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 1100, name = "Con errores")
@Composable
private fun SolicitarAccesoConErroresPreview() {
    InduztekTheme {
        SolicitarAccesoContent(
            uiState = SolicitudAccesoUiState(
                nombre = "Ana",
                correo = "ana@",
                errorApellido = "El apellido es obligatorio",
                errorCorreo = "Ingresa un correo válido",
                errorContrasena = "Mínimo 6 caracteres"
            ),
            onNombreCambia = {}, onApellidoCambia = {}, onPaisSeleccionado = {},
            onCiudadSeleccionada = {}, onCorreoCambia = {}, onContrasenaCambia = {},
            onRepetirContrasenaCambia = {}, onJustificacionCambia = {},
            onAlternarVisibilidadContrasena = {}, onAlternarVisibilidadRepetirContrasena = {},
            onRegistrarse = {}, onVolverAIngreso = {}
        )
    }
}