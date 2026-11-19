package cl.duoc.induztek.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.induztek.R
import cl.duoc.induztek.data.fechaCorta
import cl.duoc.induztek.model.EstadoProtocolo
import cl.duoc.induztek.model.Protocolo
import cl.duoc.induztek.model.TipoPrueba
import cl.duoc.induztek.model.Usuario
import cl.duoc.induztek.model.Calculos
import cl.duoc.induztek.ui.components.FilaVacia
import cl.duoc.induztek.ui.components.Tarjeta
import cl.duoc.induztek.ui.theme.Blanco
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.GrisTexto
import cl.duoc.induztek.ui.theme.LavandaBorde
import cl.duoc.induztek.ui.theme.LavandaPrimario
import cl.duoc.induztek.ui.components.colorCaja
import cl.duoc.induztek.ui.components.fondoPantallaInterna
import cl.duoc.induztek.viewmodel.ClienteViewModel



@Composable
fun ClienteHomeScreen(
    usuario: Usuario,
    onIrEquipo: (Int) -> Unit,
    onIrActa: (Int) -> Unit,
    onSalir: () -> Unit,
    vm: ClienteViewModel = viewModel()
) {
    val empresaId = usuario.idEmpresa ?: usuario.idUsuario
    val equipos = remember { vm.equiposDe(empresaId) }
    val protocolos = remember { vm.protocolosVisitaCliente(empresaId) }
    val ultimaVisita = remember {
        protocolos.mapNotNull { vm.visitaPorId(it.idVisita) }
            .filter { it.fechaHoraCierre != null }
            .maxByOrNull { it.fechaHoraCierre ?: "" }
    }
    val recepcion = remember(ultimaVisita) { ultimaVisita?.let { vm.recepcionDeVisita(it.idVisita) } }
    var busqueda by remember { mutableStateOf("") }
    val visibles = equipos.filter {
        busqueda.isBlank() ||
                it.tag.contains(busqueda, true) || it.instalacionNombre.contains(busqueda, true) ||
                it.numeroSerie.contains(busqueda, true)
    }

    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .fondoPantallaInterna()
    ) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            CabeceraCliente(
                nombre = usuario.nombre,
                subtitulo = "${usuario.cargo} ${vm.nombreDe(empresaId)}",
                onSalir = onSalir
            )
            Column(Modifier.padding(Dimens.espacioMedio)) {
                if (ultimaVisita != null) {
                    val deLaVisita = protocolos.filter { it.idVisita == ultimaVisita.idVisita }
                    CajaUltimaVisita(
                        instalacion = vm.instalacionPorId(ultimaVisita.idInstalacion)?.nombre ?: "",
                        numeroOt = ultimaVisita.numeroOt,
                        fecha = ultimaVisita.fecha.fechaCorta(),
                        pendiente = deLaVisita.any { it.estado != EstadoProtocolo.FORMALIZADO },
                        hora = recepcion?.fechaHoraRecepcion?.takeLast(5) ?: "",
                        mismaFecha = recepcion?.fechaHoraRecepcion?.take(10) == ultimaVisita.fecha,
                        fechaEntrega = recepcion?.fechaHoraRecepcion?.take(10)?.fechaCorta() ?: "",
                        equiposProbados = deLaVisita.map { it.idEquipo }.distinct().size,
                        onVerActa = { onIrActa(ultimaVisita.idVisita) }
                    )
                    FilaVacia(Dimens.salto6)
                }

                BuscadorCliente(busqueda) { busqueda = it }
                FilaVacia(Dimens.salto5)

                Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    visibles.forEach { e ->
                        val ult = protocolos.filter { it.idEquipo == e.idEquipo }
                            .maxByOrNull { it.fechaHoraPrueba }
                        TarjetaEquipoCliente(
                            tag = e.tag,
                            tipoEquipo = e.tipoEquipo,
                            instalacion = e.instalacionNombre,
                            ultimaPrueba = ult?.fechaHoraPrueba?.take(10)?.fechaCorta() ?: "—",
                            pendiente = ult == null || ult.estado != EstadoProtocolo.FORMALIZADO,
                            onClick = { onIrEquipo(e.idEquipo) }
                        )
                    }
                }

                FilaVacia(Dimens.salto6)
                Text(
                    stringResource(R.string.cliente_solo_visualizar),
                    style = MaterialTheme.typography.labelSmall, color = GrisTexto()
                )
                FilaVacia(Dimens.salto9)
            }
        }
        BarraNavegacionCliente()
    }
}


@Composable
private fun CabeceraCliente(nombre: String, subtitulo: String, onSalir: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(Dimens.altoCabeceraCliente)) {
        Image(
            painter = painterResource(id = R.drawable.banner_inicio),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
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
            Image(
                painter = painterResource(id = R.drawable.foto_perfil),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(Dimens.tamanoAvatar)
                    .clip(CircleShape)
                    .border(Dimens.grosorBordeAvatar, Blanco, CircleShape)
            )
            Spacer(Modifier.width(Dimens.espacioEntreCampos))
            Column(Modifier.weight(1f)) {
                Text(nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    subtitulo, style = MaterialTheme.typography.bodySmall, color = GrisTexto()
                )
            }
            Box(
                Modifier
                    .background(Blanco.copy(alpha = Dimens.chipAlpha), RoundedCornerShape(Dimens.radioSegmento))
                    .clickable(role = Role.Button) { onSalir() }
                    .padding(
                        horizontal = Dimens.rellenoBotonChicoH,
                        vertical = Dimens.rellenoBotonChicoV
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.cliente_cerrar_sesion),
                    style = MaterialTheme.typography.labelMedium, color = GrisTexto()
                )
            }
        }
    }
}



@Composable
private fun CajaUltimaVisita(
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
                style = MaterialTheme.typography.labelSmall, color = GrisTexto()
            )
            FilaVacia(Dimens.salto2)
            BarraAcento(
                titulo = instalacion,
                etiqueta = stringResource(
                    if (pendiente) R.string.cliente_por_formalizar else R.string.cliente_formalizada
                ),
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
                .background(colorCaja(), RoundedCornerShape(Dimens.radioTarjeta))
                .border(
                    Dimens.bordeTarjeta, LavandaBorde(),
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
                    tint = LavandaPrimario(),
                    modifier = Modifier.size(Dimens.tamanoIconoNav)
                )
                Spacer(Modifier.width(Dimens.salto1))
                Column {
                    Text(
                        stringResource(R.string.cliente_ver_acta),
                        style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium
                    )
                    Text(
                        stringResource(R.string.cliente_pdf),
                        style = MaterialTheme.typography.labelMedium, color = GrisTexto()
                    )
                }
            }
        }
    }
}

@Composable
private fun BarraAcento(titulo: String, etiqueta: String, detalle: String, entrega: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            Modifier
                .width(Dimens.anchoBarraAcento)
                .height(Dimens.altoBarraAcento)
                .background(LavandaPrimario())
        )
        Spacer(Modifier.width(Dimens.salto3))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(Dimens.salto2))
                Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = GrisTexto())
            }
            Text(detalle, style = MaterialTheme.typography.bodySmall)
            Text(
                entrega, style = MaterialTheme.typography.labelSmall, color = GrisTexto()
            )
        }
    }
}

@Composable
private fun BuscadorCliente(valor: String, onCambio: (String) -> Unit) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        placeholder = {
            Text(stringResource(R.string.cliente_buscar), color = GrisTexto())
        },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_buscar),
                contentDescription = null,
                tint = GrisTexto(),
                modifier = Modifier.size(Dimens.tamanoIconoCampo)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(percent = 50),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LavandaBorde(),
            unfocusedBorderColor = LavandaBorde(),
            focusedContainerColor = colorCaja(),
            unfocusedContainerColor = colorCaja()
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.alturaBuscador)
    )
}


@Composable
private fun TarjetaEquipoCliente(
    tag: String,
    tipoEquipo: String,
    instalacion: String,
    ultimaPrueba: String,
    pendiente: Boolean,
    onClick: () -> Unit
) {
    Tarjeta(
        Modifier
            .clickable(role = Role.Button, onClickLabel = stringResource(R.string.cd_abrir_equipo)) { onClick() }
            .semantics(mergeDescendants = true) { }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(tag, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(Dimens.salto2))
                    Text(tipoEquipo, style = MaterialTheme.typography.bodyMedium, color = GrisTexto())
                }
                FilaVacia(Dimens.salto2)
                FilaBarra(
                    stringResource(R.string.cliente_fila_ultima_prueba, instalacion, ultimaPrueba), GrisTexto()
                )
                FilaBarra(
                    stringResource(
                        if (pendiente) R.string.estado_pendiente_formalizacion
                        else R.string.estado_formalizado
                    ),
                    MaterialTheme.colorScheme.onSurface
                )
            }
            CirculoFlecha()
        }
    }
}

@Composable
private fun FilaBarra(texto: String, color: androidx.compose.ui.graphics.Color) {
    Row(Modifier.padding(top = Dimens.salto1), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .width(Dimens.anchoBarraAcento)
                .height(Dimens.altoBarraAcento)
                .background(LavandaPrimario())
        )
        Spacer(Modifier.width(Dimens.salto3))
        Text(texto, style = MaterialTheme.typography.bodySmall, color = color)
    }
}

@Composable
private fun CirculoFlecha() {
    Box(
        Modifier
            .size(Dimens.tamanoCirculoFlecha)
            .border(Dimens.bordeTarjeta, LavandaBorde(), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text("›", style = MaterialTheme.typography.titleMedium, color = GrisTexto(), fontWeight = FontWeight.Bold)
    }
}


@Composable
private fun BarraNavegacionCliente() {
    Column(Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(Dimens.grosorDivisor).background(LavandaBorde()))
        Row(Modifier.fillMaxWidth().height(Dimens.altoBarraNavegacion)) {
            ItemNavegacion(
                stringResource(R.string.cliente_nav_equipos), R.drawable.ic_equipos,
                activo = true, modifier = Modifier.weight(1f)
            )
            ItemNavegacion(
                stringResource(R.string.cliente_nav_chat), R.drawable.ic_chat,
                activo = false, modifier = Modifier.weight(1f)
            )
            ItemNavegacion(
                stringResource(R.string.cliente_nav_perfil), R.drawable.ic_perfil,
                activo = false, modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable

private fun ItemNavegacion(texto: String, icono: Int, activo: Boolean, modifier: Modifier = Modifier) {
    val color = if (activo) LavandaPrimario() else GrisTexto()
    Column(
        modifier.clickable { },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = icono),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(Dimens.tamanoIconoNav)
        )
        Spacer(Modifier.height(Dimens.salto1))
        Text(texto, style = MaterialTheme.typography.labelMedium, color = color)
    }
}


@Composable
fun ClienteEquipoScreen(
    idEquipo: Int,
    usuario: Usuario,
    onAtras: () -> Unit,
    onConsultar: () -> Unit,
    vm: ClienteViewModel = viewModel()
) {

    val equipo = remember(idEquipo) {
        usuario.idEmpresa?.let { vm.equipoDeCliente(idEquipo, it) }
    } ?: return
    val historial = remember { vm.historial(idEquipo) }
    val protocolos = remember { vm.protocolosDeEquipo(idEquipo, usuario.idEmpresa ?: -1) }
    val ultimo = protocolos.firstOrNull()
    val pendiente = ultimo != null && ultimo.estado != EstadoProtocolo.FORMALIZADO


    val contexto = LocalContext.current
    val sinAislacion = stringResource(R.string.cliente_sin_aislacion)
    val textoPendiente = stringResource(R.string.resultado_pendiente)

    @Composable
    fun detalleDe(pr: Protocolo): String {
        val i = historial.indexOfFirst { it.first == pr.numero }
        if (i < 0) return sinAislacion
        val valor = historial[i].third
        val previa = historial.getOrNull(i - 1)
        val resultado = vm.pruebasDe(pr.idProtocolo)
            .firstOrNull { it.tipoPrueba == TipoPrueba.AISLACION && it.realizada }?.resultado
            ?.nombre ?: textoPendiente
        return if (previa == null)
            stringResource(R.string.cliente_detalle_primero, Calculos.fmt(valor, 0), resultado)
        else
            stringResource(
                R.string.cliente_detalle_vs,
                Calculos.fmt(valor, 0), previa, resultado
            )
    }

    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .fondoPantallaInterna()
    ) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.espacioMedio, vertical = Dimens.salto5),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    Modifier.clickable(role = Role.Button) { onAtras() }
                        .semantics(mergeDescendants = true) { },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("‹", style = MaterialTheme.typography.headlineSmall, color = GrisTexto())
                    Spacer(Modifier.width(Dimens.salto1))
                    Text(
                        stringResource(R.string.cliente_mis_equipos),
                        style = MaterialTheme.typography.bodyMedium, color = GrisTexto()
                    )
                }
                Spacer(Modifier.weight(1f))
                if (pendiente) {
                    Text(
                        stringResource(R.string.cliente_aviso_pendiente),
                        style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    )
                }
            }

            Column(Modifier.padding(horizontal = Dimens.espacioMedio)) {
                Text(equipo.tag, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "${equipo.tipoEquipo} ",
                        style = MaterialTheme.typography.bodyMedium, color = GrisTexto()
                    )
                    Text(
                        "${equipo.instalacionNombre} ${equipo.potenciaKva?.let { "${Calculos.fmt(it, 0)} kVA" } ?: ""}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                FilaVacia(Dimens.salto2)
                Text(
                    stringResource(R.string.cliente_preliminares),
                    style = MaterialTheme.typography.labelSmall, color = GrisTexto()
                )
                FilaVacia(Dimens.salto5)

                Tarjeta {
                    Text(
                        stringResource(R.string.cliente_como_viene),
                        style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        stringResource(R.string.cliente_lectura_at_t),
                        style = MaterialTheme.typography.labelSmall, color = GrisTexto()
                    )
                    FilaVacia(Dimens.salto4)
                    if (historial.isEmpty()) {
                        Text(
                            stringResource(R.string.cliente_sin_datos),
                            style = MaterialTheme.typography.bodySmall, color = GrisTexto()
                        )
                    } else {
                        GraficoAislacion(historial.map { it.second.take(4) to it.third })
                    }
                }

                val ultima = historial.lastOrNull()?.third
                val anterior = historial.getOrNull(historial.size - 2)
                val variacion = Calculos.variacionPct(ultima, anterior?.third)
                if (variacion != null) {
                    val ingeniero = ultimo?.let { vm.ingenieroDe(it.idVisita) } ?: ""
                    FilaVacia(Dimens.salto3)
                    Text(
                        stringResource(
                            if (variacion < 0) R.string.cliente_lectura_bajo else R.string.cliente_lectura_subio,
                            Calculos.fmt(if (variacion < 0) -variacion else variacion, 1),
                            anterior?.second?.take(4) ?: ""
                        ) +
                                if (ingeniero.isNotEmpty())
                                    " " + stringResource(R.string.cliente_ingeniero_revisa, ingeniero)
                                else "",
                        style = MaterialTheme.typography.labelSmall, color = GrisTexto()
                    )
                }

                FilaVacia(Dimens.salto6)
                if (protocolos.isNotEmpty()) {
                    Tarjeta {
                        Text(
                            stringResource(R.string.cliente_historial),
                            style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold
                        )
                        protocolos.forEachIndexed { i, pr ->
                            if (i > 0) {
                                HorizontalDivider(thickness = Dimens.grosorDivisor, color = LavandaBorde())
                            }
                            FilaHistorialCliente(
                                fecha = pr.fechaHoraPrueba.take(10).fechaCorta(),
                                numero = pr.numero,
                                detalle = detalleDe(pr),
                                estado = stringResource(
                                    if (pr.estado == EstadoProtocolo.FORMALIZADO) R.string.estado_formalizado
                                    else R.string.estado_pendiente_formalizacion
                                )
                            )
                        }
                    }
                }
                FilaVacia(Dimens.salto9)
            }
        }
        BotonConsultarChat(onConsultar)
    }
}

@Composable

private fun FilaHistorialCliente(fecha: String, numero: String, detalle: String, estado: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = Dimens.salto4),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("$fecha / $numero", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(detalle, style = MaterialTheme.typography.labelSmall, color = GrisTexto())
        }
        Box(
            Modifier
                .width(Dimens.grosorDivisor)
                .height(Dimens.altoDivisorFila)
                .background(LavandaBorde())
        )
        Text(
            estado,
            Modifier
                .width(Dimens.anchoEstadoHistorial)
                .padding(start = Dimens.salto3),
            style = MaterialTheme.typography.labelSmall, color = GrisTexto(), textAlign = TextAlign.End
        )
    }
}




@Composable
private fun GraficoAislacion(series: List<Pair<String, Double>>) {
    if (series.isEmpty()) return
    val max = series.maxOf { it.second }.takeIf { it > 0 } ?: 1.0
    val altoUtil = Dimens.altoGrafico - Dimens.salto9
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(Dimens.altoGrafico),
            verticalAlignment = Alignment.Bottom
        ) {
            series.forEachIndexed { i, par ->
                Column(
                    Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        Calculos.fmt(par.second, 0),
                        style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(Dimens.salto1))
                    val alto: Dp = altoUtil * (par.second / max).toFloat() * Dimens.factorAltoBarras
                    BarraAislacion(alto, solida = i == series.lastIndex)
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(Dimens.grosorDivisor).background(LavandaBorde()))
        FilaVacia(Dimens.salto2)
        Row(Modifier.fillMaxWidth()) {
            series.forEach { par ->
                Text(
                    par.first, Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun BarraAislacion(alto: Dp, solida: Boolean) {
    val colorBarra = LavandaPrimario()
    Canvas(
        Modifier.clearAndSetSemantics { }
            .fillMaxWidth(Dimens.anchoRelativoBarra)
            .height(alto)
    ) {
        val forma = Path().apply {
            addRoundRect(
                RoundRect(
                    0f, 0f, size.width, size.height,
                    CornerRadius(Dimens.radioBarras, Dimens.radioBarras),
                    CornerRadius(Dimens.radioBarras, Dimens.radioBarras),
                    CornerRadius.Zero, CornerRadius.Zero
                )
            )
        }
        clipPath(forma) {
            drawRect(
                color = colorBarra.copy(
                    alpha = if (solida) Dimens.alfaBarraSolida else Dimens.alfaBarraFondo
                )
            )
            if (!solida) {
                var x = -size.height
                while (x < size.width) {
                    drawLine(
                        color = colorBarra.copy(alpha = Dimens.alfaTrazoHatch),
                        start = Offset(x, size.height),
                        end = Offset(x + size.height, 0f),
                        strokeWidth = Dimens.grosorTrazoHatch
                    )
                    x += Dimens.espacioTrazoHatch
                }
            }
        }
    }
}


@Composable
private fun BotonConsultarChat(onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(
                horizontal = Dimens.espacioMedio,
                vertical = Dimens.salto5
            )
            .height(Dimens.alturaBotonGrande)
            .background(
                LavandaPrimario().copy(alpha = Dimens.alfaBarraSolida),
                RoundedCornerShape(Dimens.radioTarjeta)
            )
            .clickable { onClick() },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_chat),
            contentDescription = null,
            tint = Blanco,
            modifier = Modifier.size(Dimens.tamanoIconoNav)
        )
        Spacer(Modifier.width(Dimens.espacioEntreCampos))
        Text(
            stringResource(R.string.cliente_consultar_chat),
            style = MaterialTheme.typography.titleSmall,
            color = Blanco, fontWeight = FontWeight.SemiBold
        )
    }
}
