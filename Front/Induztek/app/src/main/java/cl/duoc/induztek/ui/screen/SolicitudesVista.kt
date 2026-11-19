
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import cl.duoc.induztek.R
import cl.duoc.induztek.model.Solicitud
import cl.duoc.induztek.ui.components.BotonChico
import cl.duoc.induztek.ui.components.CajaBordeada
import cl.duoc.induztek.ui.components.SeccionTitulo
import cl.duoc.induztek.ui.components.Tarjeta
import cl.duoc.induztek.ui.components.fondoPantallaInterna
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.TextoSecundario
import cl.duoc.induztek.ui.theme.InduztekTheme
import cl.duoc.induztek.ui.theme.RojoRechazado
import cl.duoc.induztek.ui.theme.TextoTitulo


@Composable
internal fun SolicitudesContenido(
    pendientes: List<Solicitud>,
    aprobadas: List<Solicitud>,
    aviso: String?,
    onResolver: (Solicitud, Boolean) -> Unit,
    onCrear: (Solicitud) -> Unit,
    onAtras: () -> Unit
) {
    val cambios = mutableListOf<Solicitud>()
    val accesos = mutableListOf<Solicitud>()
    val bajas = mutableListOf<Solicitud>()
    for (solicitud in pendientes) {
        if (solicitud.esCambio) {
            cambios.add(solicitud)
        } else if (solicitud.esBaja) {
            bajas.add(solicitud)
        } else {
            accesos.add(solicitud)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .fondoPantallaInterna()
    ) {
        Column(Modifier.padding(horizontal = Dimens.margenHorizontalPantalla)) {
            CabeceraVolver(stringResource(R.string.sol_titulo), onAtras = onAtras)

            val avisoMostrado = aviso
            if (avisoMostrado != null) {
                CajaBordeada(Modifier.fillMaxWidth()) {
                    Text(
                        avisoMostrado,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoTitulo()
                    )
                }
                Spacer(Modifier.height(Dimens.salto2))
            }
        }

        LazyColumn(Modifier.weight(1f)) {
            item {
                SeccionTitulo(
                    stringResource(R.string.sol_pendientes),
                    Modifier.padding(horizontal = Dimens.margenHorizontalPantalla)
                )
            }
            if (accesos.isEmpty() && bajas.isEmpty() && cambios.isEmpty()) {
                item { FilaSolicitudVacia(stringResource(R.string.sol_vacio)) }
            }
            items(cambios.size) { indice ->
                FilaSolicitud(
                    cambios[indice],
                    detalle = stringResource(R.string.sol_cambio_nota),
                    onResolver = { aprobar -> onResolver(cambios[indice], aprobar) }
                )
            }
            items(accesos.size) { indice ->
                FilaSolicitud(
                    accesos[indice],
                    onResolver = { aprobar -> onResolver(accesos[indice], aprobar) }
                )
            }
            if (bajas.isNotEmpty()) {
                items(bajas.size) { indice ->
                    FilaSolicitud(
                        bajas[indice],
                        detalle = stringResource(R.string.sol_baja_nota),
                        onResolver = { aprobar -> onResolver(bajas[indice], aprobar) }
                    )
                }
            }

            item {
                SeccionTitulo(
                    stringResource(R.string.sol_aprobadas),
                    Modifier.padding(horizontal = Dimens.margenHorizontalPantalla)
                )
            }
            if (aprobadas.isEmpty()) {
                item { FilaSolicitudVacia(stringResource(R.string.sol_aprobadas_vacias)) }
            }
            items(aprobadas.size) { indice ->
                FilaSolicitud(
                    aprobadas[indice],
                    crear = { onCrear(aprobadas[indice]) },
                    onResolver = { }
                )
            }
            item { Spacer(Modifier.height(Dimens.espacioGrande)) }
        }
    }
}

@Composable
private fun FilaSolicitudVacia(texto: String) {
    Text(
        texto,
        style = MaterialTheme.typography.bodySmall,
        color = TextoSecundario(),
        modifier = Modifier.padding(
            horizontal = Dimens.margenHorizontalPantalla, vertical = Dimens.salto2
        )
    )
}

@Composable
private fun FilaSolicitud(
    sol: Solicitud,
    detalle: String = "",
    crear: (() -> Unit)? = null,
    onResolver: (Boolean) -> Unit
) {
    Tarjeta(Modifier.padding(horizontal = Dimens.margenHorizontalPantalla, vertical = Dimens.salto1)) {
        var tipoTexto = R.string.sol_tipo_ingreso
        if (sol.esCambio) {
            tipoTexto = R.string.sol_tipo_cambio
        }
        if (sol.esBaja) {
            tipoTexto = R.string.sol_tipo_baja
        }
        Text(
            stringResource(tipoTexto),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (sol.esBaja) RojoRechazado() else MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(Dimens.salto1))
        Text(
            sol.nombreCompleto,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextoTitulo()
        )

        var correoMostrado = sol.correo
        if (correoMostrado.isBlank()) {
            correoMostrado = "—"
        }
        Text(
            correoMostrado,
            style = MaterialTheme.typography.bodySmall,
            color = TextoSecundario()
        )
        if (sol.esCambio) {
            Spacer(Modifier.height(Dimens.salto1))
            Text(
                "${stringResource(R.string.sol_cambio_dato)}: ${sol.campo}",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario()
            )
            Text(
                "${stringResource(R.string.sol_cambio_valor)}: ${sol.valor}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = TextoTitulo()
            )
        }
        if (!sol.esBaja && (sol.pais.isNotBlank() || sol.ciudad.isNotBlank())) {
            Spacer(Modifier.height(Dimens.salto1))
            Text(
                "${stringResource(R.string.sol_desde)}: ${sol.pais}, ${sol.ciudad}",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario()
            )
        }
        if (sol.justificacion.isNotBlank()) {
            Spacer(Modifier.height(Dimens.salto2))
            Text(
                stringResource(R.string.sol_justificacion),
                style = MaterialTheme.typography.labelSmall,
                color = TextoSecundario()
            )
            Text(
                sol.justificacion,
                style = MaterialTheme.typography.bodySmall,
                color = TextoTitulo()
            )
        }
        if (detalle.isNotBlank()) {
            Spacer(Modifier.height(Dimens.salto2))
            Text(detalle, style = MaterialTheme.typography.labelSmall, color = TextoSecundario())
        }

        Spacer(Modifier.height(Dimens.salto3))
        Row {
            if (crear != null) {
                BotonChico(stringResource(R.string.sol_crear), onClick = crear)
            } else {
                BotonChico(stringResource(R.string.sol_aprobar), onClick = { onResolver(true) })
                Spacer(Modifier.width(Dimens.salto3))
                BotonChico(stringResource(R.string.sol_rechazar), onClick = { onResolver(false) })
            }
        }
    }
}


private val PreviewSolicitudes = listOf(
    Solicitud(5, "Camila", "Núñez Prado", "Chile", "Rancagua", "cnunez@envpacifico.ejemplo",
        "Necesito ver los actas de la planta para el informe mensual.", "PENDIENTE", "INGRESO", null),
    Solicitud(6, "Thomas", "Peralta S.", "", "", "tperalta@induztek.ejemplo",
        "Dejo la empresa, pido que cierren mi cuenta.", "PENDIENTE", "BAJA", 7),
    Solicitud(3, "Esteban", "Rivas Molina", "Chile", "Talca", "erivas@clinica.ejemplo",
        "Soy el nuevo jefe de mantenimiento del edificio clínico.", "APROBADA", "INGRESO", null)
)

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun SolicitudesPreview() {
    InduztekTheme {
        val pendientes = mutableListOf<Solicitud>()
        val aprobadas = mutableListOf<Solicitud>()
        for (solicitud in PreviewSolicitudes) {
            if (solicitud.estado == "PENDIENTE") pendientes.add(solicitud)
            if (solicitud.estado == "APROBADA") aprobadas.add(solicitud)
        }
        SolicitudesContenido(
            pendientes = pendientes,
            aprobadas = aprobadas,
            aviso = "La cuenta de Thomas Peralta S. quedó inactiva y ya no aparece en el ingreso rápido.",
            onResolver = { _, _ -> },
            onCrear = { },
            onAtras = {}
        )
    }
}
