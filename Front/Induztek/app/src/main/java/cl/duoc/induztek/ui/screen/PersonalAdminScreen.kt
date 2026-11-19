
package cl.duoc.induztek.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.induztek.R
import cl.duoc.induztek.model.Rol
import cl.duoc.induztek.model.Usuario
import cl.duoc.induztek.ui.components.BotonPrincipal
import cl.duoc.induztek.ui.components.SeccionTitulo
import cl.duoc.induztek.ui.components.fondoPantallaInterna
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.TextoSecundario
import cl.duoc.induztek.ui.theme.InduztekTheme
import cl.duoc.induztek.ui.theme.BordeSuave
import cl.duoc.induztek.ui.theme.RojoRechazado
import cl.duoc.induztek.ui.theme.TextoTitulo
import cl.duoc.induztek.viewmodel.InicioViewModel


@Composable
fun PersonalScreen(
    onIrSuspension: (Int) -> Unit,
    onIrAsignar: (Int) -> Unit,
    onIrSolicitudes: () -> Unit,
    onAtras: () -> Unit,
    modeloPantalla: InicioViewModel = viewModel()
) {
    var seleccion by rememberSaveable { mutableIntStateOf(-1) }

    PersonalContenido(
        solicitudes = modeloPantalla.solicitudesPendientes(),
        personal = modeloPantalla.personal(),
        clientes = modeloPantalla.clientes(),
        seleccionado = seleccion,
        onSeleccionar = { seleccion = it },
        onSuspender = { if (seleccion > 0) onIrSuspension(seleccion) },
        onAsignar = { if (seleccion > 0) onIrAsignar(seleccion) },
        onIrSolicitudes = onIrSolicitudes,
        onAtras = onAtras
    )
}

@Composable
private fun PersonalContenido(
    solicitudes: Int,
    personal: List<Usuario>,
    clientes: List<Usuario>,
    seleccionado: Int,
    onSeleccionar: (Int) -> Unit,
    onSuspender: () -> Unit,
    onAsignar: () -> Unit,
    onIrSolicitudes: () -> Unit,
    onAtras: () -> Unit
) {
    val tElegir = stringResource(R.string.admin_elegir_persona)
    val tAsignarElegir = stringResource(R.string.admin_asignar_elegir)
    val tAsignarNoAplica = stringResource(R.string.admin_asignar_no_aplica)
    val tAsignarSuspendido = stringResource(R.string.admin_asignar_suspendido)
    var personaElegida: Usuario? = null
    for (persona in personal) {
        if (personaElegida == null && persona.idUsuario == seleccionado) personaElegida = persona
    }
    for (persona in clientes) {
        if (personaElegida == null && persona.idUsuario == seleccionado) personaElegida = persona
    }
    val elegida = personaElegida
    var suspendidoElegido = false
    if (elegida != null && !elegida.activo) suspendidoElegido = true

    var asignable = false
    if (elegida != null && elegida.activo) {
        if (elegida.rol == Rol.INGENIERO || elegida.rol == Rol.CLIENTE) asignable = true
    }

    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .fondoPantallaInterna()
    ) {
        Column(Modifier.padding(horizontal = Dimens.margenHorizontalPantalla)) {
            CabeceraVolver(
                stringResource(R.string.admin_personal),
                stringResource(R.string.admin_solicitudes, solicitudes),
                onAtras,
                onEtiqueta = onIrSolicitudes
            )
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.admin_col_nombre),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextoTitulo(),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    stringResource(R.string.admin_col_cargo),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextoTitulo(),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    stringResource(R.string.admin_col_contacto),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextoTitulo(),
                    modifier = Modifier.weight(1.5f)
                )
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(Dimens.grosorDivisor)
                    .background(BordeSuave())
            )
            Spacer(Modifier.height(Dimens.salto2))
        }

        LazyColumn(Modifier.weight(1f)) {
            items(personal.size) { indice ->
                FilaPersona(personal[indice], seleccionado == personal[indice].idUsuario) {
                    onSeleccionar(personal[indice].idUsuario)
                }
            }
            if (clientes.isNotEmpty()) {
                item {
                    SeccionTitulo(
                        stringResource(R.string.admin_clientes),
                        Modifier.padding(horizontal = Dimens.margenHorizontalPantalla)
                    )
                }
                items(clientes.size) { indice ->
                    FilaPersona(clientes[indice], seleccionado == clientes[indice].idUsuario) {
                        onSeleccionar(clientes[indice].idUsuario)
                    }
                }
            }
            item { Spacer(Modifier.height(Dimens.espacioMedio)) }
        }

        Column(Modifier.padding(horizontal = Dimens.margenHorizontalPantalla)) {
            BotonPrincipal(
                texto = stringResource(R.string.admin_agregar),
                onClick = onIrSolicitudes,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(Dimens.salto2))
            Text(
                stringResource(R.string.admin_agregar_motivo),
                style = MaterialTheme.typography.labelSmall,
                color = TextoSecundario()
            )
            Spacer(Modifier.height(Dimens.salto2))
            BotonBordeadoPropio(
                texto = stringResource(
                    if (suspendidoElegido) R.string.admin_reactivar else R.string.admin_suspender
                ),
                habilitado = seleccionado > 0,
                onClick = onSuspender,
                modifier = Modifier.fillMaxWidth()
            )
            if (seleccionado <= 0) {
                Spacer(Modifier.height(Dimens.salto2))
                Text(
                    tElegir,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario()
                )
            }

            Spacer(Modifier.height(Dimens.salto2))
            BotonBordeadoPropio(
                texto = stringResource(R.string.admin_asignar),
                habilitado = asignable,
                onClick = onAsignar,
                modifier = Modifier.fillMaxWidth()
            )
            if (!asignable) {
                Spacer(Modifier.height(Dimens.salto2))
                Text(
                    if (elegida == null) tAsignarElegir
                    else if (elegida.activo) tAsignarNoAplica
                    else tAsignarSuspendido,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario()
                )
            }
            Spacer(Modifier.height(Dimens.espacioGrande))
        }
    }
}

@Composable
private fun FilaPersona(persona: Usuario, elegido: Boolean, onElegir: () -> Unit) {
    val fondo = if (elegido) MaterialTheme.colorScheme.primary.copy(alpha = Dimens.chipAlpha)
    else Color.Transparent

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.margenHorizontalPantalla, vertical = Dimens.salto2)
            .clip(RoundedCornerShape(Dimens.radioCaja))
            .background(fondo)
            .border(
                Dimens.bordeCampo,
                if (elegido) BordeSuave() else Color.Transparent,
                RoundedCornerShape(Dimens.radioCaja)
            )
            .clickable(role = Role.Button) { onElegir() }
            .semantics { selected = elegido }
            .padding(horizontal = Dimens.salto3, vertical = Dimens.salto2),
        verticalAlignment = Alignment.Top
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                persona.nombre,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextoTitulo(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (!persona.activo) {
                Text(
                    stringResource(R.string.admin_suspendido),
                    style = MaterialTheme.typography.labelSmall,
                    color = RojoRechazado()
                )
            }
        }
        Text(
            persona.cargo.ifBlank { persona.rol.nombre },
            style = MaterialTheme.typography.bodySmall,
            color = TextoSecundario(),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            persona.correo.ifBlank { "—" },
            style = MaterialTheme.typography.bodySmall,
            color = TextoSecundario(),
            maxLines = 2,
            modifier = Modifier.weight(1.5f)
        )
    }
}


private val PreviewPersonal = listOf(
    Usuario(10, 1, "17.284.519-3", "Matías Godoy Herrera", "mgodoy@induztek.ejemplo", rol = Rol.TECNICO),
    Usuario(7, 2, "16.482.905-7", "Thomas Peralta S.", "tperalta@induztek.ejemplo", rol = Rol.INGENIERO),
    Usuario(8, 3, "14.993.210-0", "Joaquín Dupre", "jdupre@induztek.ejemplo", rol = Rol.JEFE_TECNICO),
    Usuario(11, 4, "17.650.348-3", "Víctor Isidro R.", "visidro@envpacifico.ejemplo", rol = Rol.CLIENTE,
        cargo = "Jefe de Planta")
)

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun PersonalPreview() {
    InduztekTheme {
        val soloPersonal = mutableListOf<Usuario>()
        val soloClientes = mutableListOf<Usuario>()
        for (persona in PreviewPersonal) {
            if (persona.rol != Rol.CLIENTE) soloPersonal.add(persona)
            if (persona.rol == Rol.CLIENTE) soloClientes.add(persona)
        }
        PersonalContenido(
            solicitudes = 0,
            personal = soloPersonal,
            clientes = soloClientes,
            seleccionado = -1,
            onSeleccionar = {},
            onSuspender = {},
            onAsignar = {},
            onIrSolicitudes = {},
            onAtras = {}
        )
    }
}
