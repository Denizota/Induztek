
package cl.duoc.induztek.ui.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import cl.duoc.induztek.R
import cl.duoc.induztek.model.Usuario
import cl.duoc.induztek.ui.components.CampoBase
import cl.duoc.induztek.ui.components.colorCaja
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.TextoSecundario
import cl.duoc.induztek.ui.theme.BordeSuave
import cl.duoc.induztek.ui.theme.TextoPrincipal


@Composable
internal fun CamposBloqueados(
    usuarioActual: Usuario,
    onSolicitar: (String, String, String) -> Boolean,
    cambioPendiente: (String) -> Boolean
) {
    val tCargo = stringResource(R.string.perfil_cargo)
    val tCorreo = stringResource(R.string.perfil_correo)
    val tTelefono = stringResource(R.string.perfil_telefono)
    val textoRut = stringResource(R.string.perfil_rut)
    val tValorCorto = stringResource(R.string.perfil_valor_corto)

    var campoPedido by rememberSaveable { mutableStateOf<String?>(null) }
    var valor by rememberSaveable { mutableStateOf("") }
    var motivo by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var enviada by rememberSaveable { mutableStateOf<String?>(null) }

    fun etiquetaDe(campo: String) =
        if (cambioPendiente(campo)) R.string.perfil_cambio_pendiente else R.string.perfil_solicitar

    CampoBloqueado(tCargo, usuarioActual.cargo.ifBlank { usuarioActual.rol.nombre }, etiquetaDe(tCargo)) {
        campoPedido = tCargo; valor = ""; motivo = ""; enviada = null; error = null
    }
    Spacer(Modifier.height(Dimens.espacioEntreCampos))
    CampoBloqueado(tCorreo, usuarioActual.correo, etiquetaDe(tCorreo)) {
        campoPedido = tCorreo; valor = ""; motivo = ""; enviada = null; error = null
    }
    Spacer(Modifier.height(Dimens.espacioEntreCampos))
    CampoBloqueado(tTelefono, usuarioActual.telefono.ifBlank { "—" }, etiquetaDe(tTelefono)) {
        campoPedido = tTelefono; valor = ""; motivo = ""; enviada = null; error = null
    }
    Spacer(Modifier.height(Dimens.espacioEntreCampos))
    CampoBloqueado(textoRut, usuarioActual.rut, etiquetaDe(textoRut)) {
        campoPedido = textoRut; valor = ""; motivo = ""; enviada = null; error = null
    }

    val campo = campoPedido
    if (campo != null) {
        val textoEnviada = stringResource(R.string.perfil_cambio_enviada, campo)
        val textoYaPedida = stringResource(R.string.perfil_cambio_ya_pedida)
        Spacer(Modifier.height(Dimens.salto3))
        CampoBase(
            valor = valor,
            onValorCambia = { valor = it; error = null },
            etiqueta = stringResource(R.string.perfil_valor_nuevo, campo),
            textoSugerido = stringResource(R.string.perfil_valor_sugerido),
            error = error
        )
        Spacer(Modifier.height(Dimens.espacioEntreCampos))
        CampoBase(
            valor = motivo,
            onValorCambia = { motivo = it; error = null },
            etiqueta = stringResource(R.string.perfil_motivo),
            textoSugerido = stringResource(R.string.perfil_motivo_sugerido),
            unaSolaLinea = false,
            alto = Dimens.alturaCampoTextoLargo,
            accionTeclado = androidx.compose.ui.text.input.ImeAction.Done
        )
        Spacer(Modifier.height(Dimens.salto3))
        BotonConIcono(
            texto = stringResource(R.string.perfil_baja_enviar),
            icono = R.drawable.ic_candado,
            onClick = {
                if (valor.trim().length < 2) {
                    error = tValorCorto
                } else if (onSolicitar(campo, valor, motivo)) {
                    enviada = textoEnviada
                    campoPedido = null
                    valor = ""
                    motivo = ""
                } else {
                    error = textoYaPedida
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }

    val avisoEnviado = enviada
    if (avisoEnviado != null) {
        Spacer(Modifier.height(Dimens.salto2))
        Text(
            stringResource(R.string.perfil_solicitud_ok) + " " + avisoEnviado,
            style = MaterialTheme.typography.bodySmall,
            color = TextoSecundario()
        )
    }
}

@Composable
private fun CampoBloqueado(
    etiqueta: String,
    valor: String,
    @StringRes etiquetaSolicitud: Int,
    onSolicitar: () -> Unit
) {
    CampoBase(
        valor = valor,
        onValorCambia = { },
        etiqueta = etiqueta,
        textoSugerido = valor,
        habilitado = false,
        contenidoFinal = {
            Icon(
                painter = painterResource(id = R.drawable.ic_candado),
                contentDescription = stringResource(R.string.perfil_bloqueado),
                tint = TextoSecundario(),
                modifier = Modifier.size(Dimens.tamanoIconoCampo)
            )
        }
    )
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            stringResource(etiquetaSolicitud, etiqueta),
            style = MaterialTheme.typography.labelSmall,
            color = TextoPrincipal(),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(RoundedCornerShape(Dimens.radioSegmento))
                .clickable(role = Role.Button) { onSolicitar() }
                .padding(vertical = Dimens.salto2, horizontal = Dimens.salto1)
        )
    }
}

@Composable
internal fun BotonConIcono(
    texto: String,
    icono: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(Dimens.radioBotonChico),
        border = BorderStroke(Dimens.bordeTarjeta, BordeSuave()),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = colorCaja(),
            contentColor = MaterialTheme.colorScheme.primary
        ),
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = icono),
            contentDescription = null,
            modifier = Modifier.size(Dimens.tamanoIconoCampo)
        )
        Spacer(Modifier.width(Dimens.salto2))
        Text(texto, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
internal fun EnlaceCentrado(texto: String, onClick: () -> Unit) {
    Text(
        texto,
        style = MaterialTheme.typography.bodyMedium.copy(
            textDecoration = TextDecoration.Underline,
            fontWeight = FontWeight.Medium
        ),
        color = TextoPrincipal(),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.radioSegmento))
            .clickable(role = Role.Button) { onClick() }
            .padding(vertical = Dimens.salto2)
    )
}

@Composable
internal fun FilaEleccion(texto: String, enabled: Boolean = true, onClick: () -> Unit) {
    Text(
        texto,
        style = MaterialTheme.typography.bodyMedium,
        color = if (enabled) TextoPrincipal() else TextoSecundario(),
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.radioSegmento))
            .clickable(role = Role.Button, enabled = enabled) { onClick() }
            .padding(vertical = Dimens.salto3)
    )
}
