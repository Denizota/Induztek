
package cl.duoc.induztek.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import cl.duoc.induztek.R
import cl.duoc.induztek.ui.components.FilaVacia
import cl.duoc.induztek.ui.components.Tarjeta
import cl.duoc.induztek.ui.theme.Acento
import cl.duoc.induztek.ui.theme.Blanco
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.TextoSecundario
import cl.duoc.induztek.ui.theme.NaranjaObservado
import cl.duoc.induztek.ui.theme.VerdeAprobado


@Composable
internal fun CabeceraCliente(
    nombre: String,
    subtitulo: String,
    idUsuario: Int,
    onSalir: () -> Unit,
    onReiniciar: () -> Unit
) {
    Box(Modifier.fillMaxWidth().height(Dimens.altoCabeceraCliente)) {
        BannerUsuario(
            imagenGuardada(idUsuario, ImagenesPerfil.TIPO_BANNER),
            Modifier.fillMaxSize()
        )
        Row(
            Modifier
                .fillMaxSize()
                .padding(
                    start = Dimens.espacioMedio,
                    top = Dimens.desplomeCabeceraCliente,
                    end = Dimens.espacioMedio
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val foto = imagenGuardada(idUsuario, ImagenesPerfil.TIPO_FOTO)
            AvatarPersona(
                nombre = nombre,
                foto = foto,
                lado = Dimens.tamanoAvatar,
                borde = Dimens.grosorBordeAvatar,
                colorBorde = Blanco
            )
            Spacer(Modifier.width(Dimens.espacioEntreCampos))
            Column(Modifier.weight(1f)) {
                Text(
                    nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Blanco
                )
                Text(
                    subtitulo, style = MaterialTheme.typography.bodySmall,
                    color = Blanco.copy(alpha = 0.72f)
                )
            }
            MenuLineas(onSalir = onSalir, onReiniciar = onReiniciar)
        }
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(Dimens.grosorFirmaMarca)
                .background(Acento())
        )
    }
}

@Composable
internal fun CajaUltimaVisita(
    instalacion: String,
    numeroOt: String,
    fecha: String,
    pendiente: Boolean,
    hora: String,
    mismaFecha: Boolean,
    fechaEntrega: String,
    equiposProbados: Int,
    onVerActa: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Tarjeta(Modifier.weight(1f)) {
            Text(
                stringResource(R.string.cliente_ultima_visita),
                style = MaterialTheme.typography.labelSmall, color = TextoSecundario()
            )
            FilaVacia(Dimens.salto2)
            BarraAcento(
                titulo = instalacion,
                etiqueta = stringResource(
                    if (pendiente) R.string.cliente_por_formalizar else R.string.cliente_formalizada
                ),
                colorEstado = if (pendiente) NaranjaObservado() else VerdeAprobado(),
                detalle = "$numeroOt $fecha",
                entrega = if (mismaFecha) {
                    stringResource(R.string.cliente_entrega_mismo_dia, hora, equiposProbados)
                } else {
                    stringResource(R.string.cliente_entrega_fecha, fechaEntrega, hora, equiposProbados)
                }
            )
        }
        Spacer(Modifier.width(Dimens.salto3))
        Column(
            Modifier
                .width(Dimens.anchoCajaActa)
                .height(Dimens.altoCajaActa)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = Dimens.chipAlpha),
                    RoundedCornerShape(Dimens.radioTarjeta)
                )
                .border(
                    Dimens.bordeTarjeta,
                    MaterialTheme.colorScheme.primary.copy(alpha = Dimens.iconoAlpha * 2),
                    RoundedCornerShape(Dimens.radioTarjeta)
                )
                .clickable(role = Role.Button) { onVerActa() }
                .semantics(mergeDescendants = true) { }
                .padding(horizontal = Dimens.salto2),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_acta),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(Dimens.tamanoIconoNav)
                )
                Spacer(Modifier.width(Dimens.salto1))
                Column {
                    Text(
                        stringResource(R.string.cliente_ver_acta),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        stringResource(R.string.cliente_pdf),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextoSecundario()
                    )
                }
            }
        }
    }
}

@Composable
private fun BarraAcento(titulo: String, etiqueta: String, colorEstado: Color, detalle: String, entrega: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            Modifier
                .width(Dimens.anchoBarraAcento)
                .height(Dimens.altoBarraAcento)
                .background(colorEstado)
        )
        Spacer(Modifier.width(Dimens.salto3))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(Dimens.salto2))
                EtiquetaEstado(etiqueta, colorEstado)
            }
            Text(detalle, style = MaterialTheme.typography.bodySmall)
            Text(
                entrega, style = MaterialTheme.typography.labelSmall, color = TextoSecundario()
            )
        }
    }
}
