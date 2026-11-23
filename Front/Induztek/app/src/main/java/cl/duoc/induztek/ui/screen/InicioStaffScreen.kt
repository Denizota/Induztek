
package cl.duoc.induztek.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.induztek.R
import cl.duoc.induztek.model.GrupoInicio
import cl.duoc.induztek.model.InicioTarea
import cl.duoc.induztek.model.Rol
import cl.duoc.induztek.ui.components.BarraInferior
import cl.duoc.induztek.ui.components.SeccionTitulo
import cl.duoc.induztek.ui.components.fondoPantallaInterna
import cl.duoc.induztek.ui.theme.Blanco
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.TextoSecundario
import cl.duoc.induztek.viewmodel.InicioViewModel

@Composable
fun InicioStaffScreen(
    onIrEquipo: (Int) -> Unit,
    onIrActa: (Int) -> Unit,
    onIrPerfil: () -> Unit,
    onSalir: () -> Unit,
    onIrInstrumentos: () -> Unit = {},
    onIrPerfiles: () -> Unit = {},
    onIrSolicitudes: () -> Unit = {},
    modeloPantalla: InicioViewModel = viewModel()
) {
    val usuarioActual = modeloPantalla.usuarioEnSesion()
    if (usuarioActual == null) {
        return
    }

    val tAntiguos = stringResource(R.string.inicio_ver_antiguos)
    val tPendientes = stringResource(R.string.inicio_ver_pendientes)
    val tOtras = stringResource(R.string.inicio_otras_tareas)
    val tInstrumentos = stringResource(R.string.inicio_otras_instrumentos)
    val tPerfiles = stringResource(R.string.inicio_otras_perfiles)
    val tSolicitudes = stringResource(R.string.inicio_otras_solicitudes)

    var busqueda by rememberSaveable { mutableStateOf("") }
    var pestana by rememberSaveable { mutableIntStateOf(0) }
    var viendoAntiguos by rememberSaveable { mutableStateOf(false) }
    var revision by rememberSaveable { mutableIntStateOf(0) }
    val sincronia = versionSincronia()

    val todas = remember(revision, viendoAntiguos, sincronia) {
        if (viendoAntiguos) modeloPantalla.antiguos() else modeloPantalla.pendientes()
    }
    val texto = busqueda.trim()
    val filtradas = mutableListOf<InicioTarea>()
    for (tarea in todas) {
        var coincide = texto.isEmpty()
        if (tarea.tag.contains(texto, true)) {
            coincide = true
        }
        if (tarea.numeroSerie.contains(texto, true)) {
            coincide = true
        }
        if (tarea.numero.contains(texto, true)) {
            coincide = true
        }
        if (tarea.tipoEquipo.contains(texto, true)) {
            coincide = true
        }
        if (tarea.instalacion.contains(texto, true)) {
            coincide = true
        }
        if (tarea.cliente.contains(texto, true)) {
            coincide = true
        }
        if (tarea.contacto.contains(texto, true)) {
            coincide = true
        }
        if (coincide) {
            filtradas.add(tarea)
        }
    }

    val visibles = mutableListOf<InicioTarea>()
    for (tarea in filtradas) {
        var mostrar = true
        if (pestana == 1 && tarea.grupo != GrupoInicio.BORRADOR) {
            mostrar = false
        }
        if (pestana == 2 && tarea.grupo != GrupoInicio.POR_FORMALIZAR) {
            mostrar = false
        }
        if (pestana == 3 && tarea.grupo != GrupoInicio.DEVUELTO) {
            mostrar = false
        }
        if (mostrar) {
            visibles.add(tarea)
        }
    }

    val grupos = mutableMapOf<Int, MutableList<InicioTarea>>()
    for (tarea in visibles) {
        val listaDelCliente = grupos[tarea.idCliente]
        if (listaDelCliente == null) {
            grupos[tarea.idCliente] = mutableListOf(tarea)
        } else {
            listaDelCliente.add(tarea)
        }
    }
    val etiquetas = etiquetasPestanas(todas, viendoAntiguos)

    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .fondoPantallaInterna()
    ) {
        CabeceraInicio(
            nombre = usuarioActual.nombre,
            rol = usuarioActual.rol.nombre,
            idUsuario = usuarioActual.idUsuario,
            onSalir = onSalir,
            onReiniciar = {
                modeloPantalla.reiniciarTodo()
                busqueda = ""
                viendoAntiguos = false
                pestana = 0
                revision++
            }
        )

        Column(Modifier.weight(1f)) {
            Column(Modifier.padding(horizontal = Dimens.margenHorizontalPantalla)) {
                FilaTitulo(
                    viendoAntiguos = viendoAntiguos,
                    tAntiguos = tAntiguos,
                    tPendientes = tPendientes,
                    onCambiar = { viendoAntiguos = it; pestana = 0 }
                )
                BuscadorInicio(busqueda) { busqueda = it }
            }

            Spacer(Modifier.height(Dimens.salto2))
            PestanasInicio(etiquetas, pestana) { pestana = it }
            Spacer(Modifier.height(Dimens.salto2))

            LazyColumn(Modifier.weight(1f)) {
                for ((idCliente, tareas) in grupos) {
                    item(key = "cliente-$idCliente") {
                        TarjetaCliente(
                            tareas = tareas,
                            modifier = Modifier.padding(horizontal = Dimens.margenHorizontalPantalla),
                            onIrTarea = { tarea ->
                                if (viendoAntiguos) onIrActa(tarea.idVisita) else onIrEquipo(tarea.idEquipo)
                            }
                        )
                    }
                }
                if (visibles.isEmpty()) {
                    item {
                        Text(
                            textoVacio(busqueda, todas.isEmpty(), viendoAntiguos, modeloPantalla.soloMisEquipos()),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextoSecundario(),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = Dimens.margenHorizontalPantalla,
                                    vertical = Dimens.salto9
                                )
                        )
                    }
                }

                if (usuarioActual.rol == Rol.JEFE_TECNICO) {
                    item {
                        SeccionTitulo(
                            tOtras,
                            Modifier.padding(horizontal = Dimens.margenHorizontalPantalla)
                        )
                    }
                    item {
                        val vencidos = modeloPantalla.instrumentosVencidos()
                        var detalle = stringResource(R.string.inicio_otras_al_dia)
                        if (vencidos > 0) {
                            detalle = stringResource(R.string.inicio_otras_vencidos, vencidos)
                        }
                        FilaOtraTarea(
                            tInstrumentos,
                            detalle,
                            Modifier.padding(horizontal = Dimens.margenHorizontalPantalla),
                            onIrInstrumentos
                        )
                    }
                    item {
                        val total = modeloPantalla.personal().size + modeloPantalla.clientes().size
                        FilaOtraTarea(
                            tPerfiles,
                            stringResource(R.string.inicio_otras_perfiles_detalle, total),
                            Modifier.padding(horizontal = Dimens.margenHorizontalPantalla),
                            onIrPerfiles
                        )
                    }
                    item {
                        val pendientes = modeloPantalla.solicitudesPendientes()
                        var detalle = stringResource(R.string.inicio_otras_solicitudes_vacio)
                        if (pendientes > 0) {
                            detalle = stringResource(R.string.inicio_otras_solicitudes_detalle, pendientes)
                        }
                        FilaOtraTarea(
                            tSolicitudes,
                            detalle,
                            Modifier.padding(horizontal = Dimens.margenHorizontalPantalla),
                            onIrSolicitudes
                        )
                    }
                }
            }
        }

        BarraInferior(
            activa = cl.duoc.induztek.ui.components.SeccionBarra.EQUIPOS,
            onIrEquipos = {},
            onIrPerfil = onIrPerfil
        )
    }
}

@Composable
private fun CabeceraInicio(
    nombre: String,
    rol: String,
    idUsuario: Int,
    onSalir: () -> Unit,
    onReiniciar: () -> Unit
) {
    val banner = imagenGuardada(idUsuario, ImagenesPerfil.TIPO_BANNER)

    Box(
        Modifier
            .fillMaxWidth()
            .height(Dimens.altoCabeceraStaff)
    ) {
        BannerUsuario(banner, Modifier.fillMaxSize())

        Row(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = Dimens.margenHorizontalPantalla, end = Dimens.margenHorizontalPantalla, bottom = Dimens.salto5),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarInicio(idUsuario, nombre)
            Spacer(Modifier.width(Dimens.salto5))
            Column(Modifier.weight(1f)) {
                Text(
                    nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Blanco,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(Dimens.salto1))
                Text(
                    rol.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Blanco.copy(alpha = 0.72f),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        MenuLineas(
            onSalir = onSalir,
            onReiniciar = onReiniciar,
            modifier = Modifier.align(Alignment.TopEnd).padding(Dimens.salto3)
        )
    }
}
