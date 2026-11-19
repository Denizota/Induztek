
package cl.duoc.induztek.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import cl.duoc.induztek.R
import cl.duoc.induztek.model.GrupoInicio
import cl.duoc.induztek.model.InicioTarea
import cl.duoc.induztek.ui.components.Tarjeta
import cl.duoc.induztek.ui.theme.Acento
import cl.duoc.induztek.ui.theme.DatoTecnico
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.TextoSecundario
import cl.duoc.induztek.ui.theme.BordeSuave
import cl.duoc.induztek.ui.theme.AzulAcero
import cl.duoc.induztek.ui.theme.NaranjaObservado
import cl.duoc.induztek.ui.theme.RojoRechazado
import cl.duoc.induztek.ui.theme.TextoPrincipal
import cl.duoc.induztek.ui.theme.VerdeAprobado


@Composable
internal fun TarjetaCliente(tareas: List<InicioTarea>, modifier: Modifier = Modifier, onIrEquipo: (Int) -> Unit) {
    val primera = tareas[0]
    Tarjeta(modifier.padding(vertical = Dimens.salto1)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .width(Dimens.salto1)
                    .height(Dimens.salto5)
                    .background(Acento())
            )
            Spacer(Modifier.width(Dimens.salto3))
            Column(Modifier.weight(1f)) {
                Text(
                    primera.cliente.uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.2.sp),
                    fontWeight = FontWeight.Bold,
                    color = TextoPrincipal(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (primera.contacto.isNotBlank()) {
                    Text(
                        primera.contacto,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.width(Dimens.salto3))
            Text(
                if (tareas.size == 1) stringResource(R.string.inicio_grupo_un_equipo)
                else stringResource(R.string.inicio_grupo_equipos, tareas.size),
                style = DatoTecnico.copy(fontSize = 10.sp),
                color = TextoSecundario(),
                maxLines = 1
            )
        }
        Spacer(Modifier.height(Dimens.salto4))
        HorizontalDivider(thickness = Dimens.grosorDivisor, color = BordeSuave())
        for (indice in tareas.indices) {
            val tarea = tareas[indice]
            if (indice > 0) HorizontalDivider(thickness = Dimens.grosorDivisor, color = BordeSuave())
            FilaEquipo(tarea) { onIrEquipo(tarea.idEquipo) }
        }
    }
}

@Composable
private fun FilaEquipo(tarea: InicioTarea, onClick: () -> Unit) {
    val serie = stringResource(R.string.inicio_serie, tarea.numeroSerie.ifBlank { "N/A" })
    val folio = if (tarea.estado == null) "" else " · ${tarea.numero}"
    val progreso = if (tarea.estado != null && tarea.pruebasTotales > 0) {
        val hecho = tarea.pruebasTotales - tarea.pruebasPendientes
        if (tarea.pruebasTotales == 1) {
            stringResource(R.string.inicio_prueba_una, hecho, 1)
        } else {
            stringResource(R.string.inicio_pruebas, hecho, tarea.pruebasTotales)
        }
    } else {
        ""
    }
    var color = NaranjaObservado()
    if (tarea.grupo == GrupoInicio.POR_FORMALIZAR) color = AzulAcero()
    if (tarea.grupo == GrupoInicio.DEVUELTO) color = RojoRechazado()
    if (tarea.grupo == GrupoInicio.CERRADO) color = VerdeAprobado()

    Column(
        Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClickLabel = stringResource(R.string.inicio_abrir)) { onClick() }
            .semantics(mergeDescendants = true) { }
            .padding(vertical = Dimens.salto4)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    tarea.tag,
                    style = DatoTecnico.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                    color = TextoPrincipal(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(Dimens.salto1))
                Text(
                    tarea.tipoEquipo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoPrincipal(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    tarea.instalacion,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (tarea.atrasado) {
                Spacer(Modifier.width(Dimens.salto3))
                ChipEstado(stringResource(R.string.inicio_atrasado), RojoRechazado())
            }
        }
        Spacer(Modifier.height(Dimens.salto4))
        Text(
            "$serie$folio / ${tarea.fecha}",
            style = DatoTecnico,
            color = TextoSecundario(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(Dimens.salto4))
        HorizontalDivider(thickness = Dimens.grosorDivisor, color = BordeSuave())
        Spacer(Modifier.height(Dimens.salto4))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            ChipEstado(tarea.estadoLinea, color)
            Spacer(Modifier.weight(1f))
            if (progreso.isNotEmpty()) {
                Text(
                    progreso,
                    style = DatoTecnico,
                    color = TextoSecundario()
                )
                Spacer(Modifier.width(Dimens.salto3))
            }
            FlechaFila()
        }
    }
}

@Composable
private fun ChipEstado(texto: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(Dimens.marcadorEstado)
                .background(color)
        )
        Spacer(Modifier.width(Dimens.salto2))
        Text(
            texto.uppercase(),
            style = DatoTecnico.copy(
                fontSize = 10.sp,
                letterSpacing = 0.8.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
