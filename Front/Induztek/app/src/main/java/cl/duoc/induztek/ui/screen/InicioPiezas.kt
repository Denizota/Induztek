
package cl.duoc.induztek.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import cl.duoc.induztek.R
import cl.duoc.induztek.model.GrupoInicio
import cl.duoc.induztek.model.InicioTarea
import cl.duoc.induztek.ui.components.SeccionTitulo
import cl.duoc.induztek.ui.components.Tarjeta
import cl.duoc.induztek.ui.theme.Blanco
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.TextoSecundario
import cl.duoc.induztek.ui.theme.InduztekTheme
import cl.duoc.induztek.ui.theme.TextoTitulo


@Composable
internal fun AvatarInicio(idUsuario: Int, nombre: String) {
    AvatarPersona(
        nombre = nombre,
        foto = imagenGuardada(idUsuario, ImagenesPerfil.TIPO_FOTO),
        lado = Dimens.tamanoAvatar,
        borde = Dimens.grosorBordeAvatar,
        colorBorde = Blanco
    )
}

@Composable
internal fun FilaTitulo(viendoAntiguos: Boolean, tAntiguos: String, tPendientes: String, onCambiar: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        SeccionTitulo(
            stringResource(R.string.inicio_equipos_por_revisar),
            Modifier.weight(1f)
        )
        Text(
            if (viendoAntiguos) tPendientes else tAntiguos,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clickable(role = Role.Button) { onCambiar(!viendoAntiguos) }
                .padding(start = Dimens.espacioChico, top = Dimens.salto4, bottom = Dimens.salto3)
        )
    }
}

@Composable
internal fun FilaOtraTarea(titulo: String, detalle: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Tarjeta(
        modifier
            .padding(vertical = Dimens.salto1)
            .clickable(role = Role.Button, onClickLabel = titulo) { onClick() }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextoTitulo()
                )
                Text(detalle, style = MaterialTheme.typography.bodySmall, color = TextoSecundario())
            }
            FlechaFila()
        }
    }
}

@Composable
internal fun FlechaFila() {
    Text(
        "›",
        style = MaterialTheme.typography.headlineSmall,
        color = TextoSecundario()
    )
}

@Composable
internal fun etiquetasPestanas(todas: List<InicioTarea>, viendoAntiguos: Boolean): List<String> {
    if (viendoAntiguos) return listOf(stringResource(R.string.inicio_formalizados, todas.size))
    var borradores = 0
    var porFormalizar = 0
    var devueltos = 0
    for (tarea in todas) {
        if (tarea.grupo == GrupoInicio.BORRADOR) borradores = borradores + 1
        if (tarea.grupo == GrupoInicio.POR_FORMALIZAR) porFormalizar = porFormalizar + 1
        if (tarea.grupo == GrupoInicio.DEVUELTO) devueltos = devueltos + 1
    }
    return listOf(
        stringResource(R.string.inicio_todos, todas.size),
        stringResource(R.string.inicio_borrador, borradores),
        stringResource(R.string.inicio_por_formalizar, porFormalizar),
        stringResource(R.string.inicio_devueltos, devueltos)
    )
}

@Composable
internal fun textoVacio(
    busqueda: String,
    sinEquipos: Boolean,
    viendoAntiguos: Boolean,
    soloMisEquipos: Boolean
): String = when {
    busqueda.isNotBlank() -> stringResource(R.string.inicio_vacio_busqueda)
    sinEquipos && soloMisEquipos && viendoAntiguos -> stringResource(R.string.inicio_vacio_mios_antiguos)
    sinEquipos && soloMisEquipos -> stringResource(R.string.inicio_vacio_mios)
    sinEquipos && viendoAntiguos -> stringResource(R.string.inicio_vacio_antiguos)
    sinEquipos -> stringResource(R.string.inicio_vacio_pendientes)
    else -> stringResource(R.string.inicio_vacio_grupo)
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun OtrasTareasPreview() {
    InduztekTheme {
        Column(Modifier.padding(horizontal = Dimens.margenHorizontalPantalla)) {
            SeccionTitulo(stringResource(R.string.inicio_otras_tareas))
            FilaOtraTarea(
                stringResource(R.string.inicio_otras_instrumentos),
                stringResource(R.string.inicio_otras_vencidos, 1),
                onClick = {}
            )
            FilaOtraTarea(
                stringResource(R.string.inicio_otras_perfiles),
                stringResource(R.string.inicio_otras_perfiles_detalle, 13),
                onClick = {}
            )
            FilaOtraTarea(
                stringResource(R.string.inicio_otras_solicitudes),
                stringResource(R.string.inicio_otras_solicitudes_detalle, 2),
                onClick = {}
            )
        }
    }
}
