package cl.duoc.induztek.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.induztek.R
import cl.duoc.induztek.data.fechaCorta
import cl.duoc.induztek.data.fechaHoraAhora
import cl.duoc.induztek.model.EstadoProtocolo
import cl.duoc.induztek.model.Prueba
import cl.duoc.induztek.model.Resultado
import cl.duoc.induztek.model.TipoPrueba
import cl.duoc.induztek.model.Usuario
import cl.duoc.induztek.model.Calculos
import cl.duoc.induztek.ui.components.BotonBordeado
import cl.duoc.induztek.ui.components.BotonPrincipal
import cl.duoc.induztek.ui.components.CampoBase
import cl.duoc.induztek.ui.components.CampoTexto
import cl.duoc.induztek.ui.components.CirculoPrueba
import cl.duoc.induztek.ui.components.EncabezadoPantalla
import cl.duoc.induztek.ui.components.FilaVacia
import cl.duoc.induztek.ui.theme.Blanco
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.FondoFilaTabla
import cl.duoc.induztek.ui.theme.GrisTexto
import cl.duoc.induztek.ui.theme.LavandaActa
import cl.duoc.induztek.ui.theme.LavandaBorde
import cl.duoc.induztek.ui.theme.NaranjaObservado
import cl.duoc.induztek.ui.theme.RojoRechazado
import cl.duoc.induztek.ui.theme.VerdeAprobado
import cl.duoc.induztek.ui.theme.esModoOscuro
import cl.duoc.induztek.ui.components.colorCaja
import cl.duoc.induztek.ui.components.fondoPantallaInterna
import cl.duoc.induztek.viewmodel.ActaViewModel
import kotlinx.coroutines.launch


@Composable
fun ActaScreen(
    idVisita: Int,
    usuario: Usuario,
    esCliente: Boolean,
    onAtras: () -> Unit,
    vm: ActaViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var tick by remember { mutableStateOf(0) }

    val visita = remember(tick) { vm.visitaPorId(idVisita) } ?: return
    val instalacion = remember { vm.instalacionPorId(visita.idInstalacion) }
    val protocolos = remember(tick) {
        if (esCliente) vm.protocolosCliente(idVisita, usuario.idEmpresa ?: -1)
        else vm.protocolosDeVisita(idVisita)
    }
    val recepcion = remember(tick) { vm.recepcionDeVisita(idVisita) }
    vm.preparar(idVisita, recepcion)

    val cerrada = visita.fechaHoraCierre != null || esCliente
    val primerProtocolo = protocolos.firstOrNull()
    val pendientes = protocolos.filter {
        it.estado != EstadoProtocolo.REGISTRADO && it.estado != EstadoProtocolo.ANULADO
    }

    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .fondoPantallaInterna()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.espacioMedio)
    ) {
        EncabezadoPantalla(
            titulo = stringResource(
                if (esCliente) R.string.acta_titulo_acta else R.string.acta_titulo_cerrar
            ),
            subtitulo = "${visita.numeroOt} ${instalacion?.nombre ?: ""}",
            onAtras = onAtras
        )
        Text(stringResource(R.string.acta_recibe_cliente), fontWeight = FontWeight.SemiBold)
        FilaVacia(Dimens.salto4)

        val conAmbiente = protocolos.firstOrNull { it.temperaturaC != null } ?: primerProtocolo

        Column(
            Modifier
                .fillMaxWidth()
                .background(colorCaja(), RoundedCornerShape(Dimens.radioActa))
                .border(Dimens.bordeCampo, LavandaBorde(), RoundedCornerShape(Dimens.radioActa))
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(LavandaActa(), RoundedCornerShape(topStart = Dimens.radioActa, topEnd = Dimens.radioActa))
                    .padding(Dimens.rellenoCaja)
            ) {
                Text(
                    stringResource(R.string.acta_empresa),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (esModoOscuro()) Blanco.copy(alpha = 0.72f) else GrisTexto()
                )
                Text(
                    stringResource(R.string.acta_titulo_acta),
                    fontWeight = FontWeight.Bold, color = Color.White
                )
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(FondoFilaTabla())
                    .padding(vertical = Dimens.salto2),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(
                        if (protocolos.isNotEmpty() && protocolos.all { it.estado == EstadoProtocolo.FORMALIZADO })
                            R.string.acta_formalizada
                        else R.string.estado_pendiente_formalizacion
                    ),
                    style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium
                )
            }

            Column(Modifier.fillMaxWidth().padding(Dimens.rellenoCaja)) {
                ColumnaDato(
                    stringResource(R.string.acta_cliente),
                    instalacion?.clienteNombre ?: "—", Modifier.fillMaxWidth()
                )
                FilaVacia(Dimens.salto2)
                Row(Modifier.fillMaxWidth()) {
                    ColumnaDato(
                        stringResource(R.string.acta_fecha), visita.fecha.fechaCorta(), Modifier.weight(1f)
                    )
                    ColumnaDato(
                        stringResource(R.string.acta_tecnico), conAmbiente?.tecnicoNombre ?: "—",
                        Modifier.weight(1f)
                    )
                }
                FilaVacia(Dimens.salto2)
                Row(Modifier.fillMaxWidth()) {
                    ColumnaDato(
                        stringResource(R.string.acta_temperatura),
                        conAmbiente?.temperaturaC?.let {
                            stringResource(R.string.acta_valor_celsius, Calculos.fmt(it, 0))
                        } ?: "—",
                        Modifier.weight(1f)
                    )
                    ColumnaDato(
                        stringResource(R.string.acta_humedad),
                        conAmbiente?.humedadPct?.let {
                            stringResource(R.string.acta_valor_porcentaje, Calculos.fmt(it, 0))
                        } ?: "—",
                        Modifier.weight(1f)
                    )
                }
                HorizontalDivider(Modifier.padding(vertical = Dimens.salto3))

                protocolos.forEach { pr ->
                    val pruebas = vm.pruebasDe(pr.idProtocolo)
                    val global = Calculos.resultadoGlobal(pruebas.map { it.resultado })
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "${pr.equipoTag} ${pr.tipoEquipo}",
                                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                stringResource(R.string.acta_protocolo, pr.numero),
                                style = MaterialTheme.typography.bodySmall, color = GrisTexto()
                            )
                        }
                        Text(
                            stringResource(
                                R.string.acta_global,
                                global?.nombre ?: stringResource(R.string.acta_registrada)
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = colorResultado(global),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    FilaVacia(Dimens.salto2)
                    pruebas.forEachIndexed { i, p ->
                        FilaActa(i + 1, p.tipoPrueba.nombre, detallePrueba(p), estadoPrueba(p), p.resultado, !p.realizada)
                        FilaVacia(Dimens.salto1)
                    }
                    FilaVacia(Dimens.salto3)
                }
            }
        }

        if (!esCliente) {
            FilaVacia(Dimens.salto6)
            Text(stringResource(R.string.acta_quien_recibe), fontWeight = FontWeight.SemiBold)
            FilaVacia(Dimens.salto3)
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioEntreCampos)) {
                CampoTexto(
                    valor = vm.nombre.value,
                    onValorCambia = { vm.nombre.value = it; vm.errNombre.value = null },
                    etiqueta = stringResource(R.string.acta_campo_nombre),
                    textoSugerido = stringResource(R.string.campo_placeholder_escribe),
                    habilitado = !cerrada,
                    error = vm.errNombre.value
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioEntreCampos)) {
                    CampoBase(
                        valor = vm.rut.value,
                        onValorCambia = { vm.rut.value = limpiarRut(it); vm.errRut.value = null },
                        etiqueta = stringResource(R.string.acta_campo_rut),
                        textoSugerido = stringResource(R.string.campo_placeholder_escribe),
                        habilitado = !cerrada,
                        error = vm.errRut.value,
                        modifier = Modifier.weight(1f),
                        transformacionVisual = TransformacionRut
                    )

                    CampoBase(
                        valor = recepcion?.fechaHoraRecepcion ?: fechaHoraAhora().horaCorta(),
                        onValorCambia = {},
                        etiqueta = stringResource(R.string.acta_campo_fecha),
                        textoSugerido = stringResource(R.string.campo_placeholder_escribe),
                        habilitado = false,
                        modifier = Modifier.weight(1f),
                        contenidoFinal = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_candado),
                                contentDescription = stringResource(R.string.cd_fecha_bloqueada),
                                tint = GrisTexto(),
                                modifier = Modifier.size(Dimens.tamanoIconoCampo)
                            )
                        }
                    )
                }
                CampoTexto(
                    valor = vm.cargo.value,
                    onValorCambia = { vm.cargo.value = it; vm.errCargo.value = null },
                    etiqueta = stringResource(R.string.acta_campo_cargo),
                    textoSugerido = stringResource(R.string.campo_placeholder_escribe),
                    habilitado = !cerrada,
                    error = vm.errCargo.value
                )
            }
            FilaVacia(Dimens.salto6)

            val errNombre = stringResource(R.string.acta_err_nombre)
            val errRut = stringResource(R.string.acta_err_rut)
            val errCargo = stringResource(R.string.acta_err_cargo)
            val actaCerrada = stringResource(R.string.acta_ok)
            val avisoPendientes = if (pendientes.isEmpty()) null else stringResource(
                R.string.acta_err_pendientes,
                pendientes.joinToString { "${it.equipoTag} (${it.estado.nombre.lowercase()})" }
            )
            BotonPrincipal(
                texto = stringResource(R.string.acta_btn_cerrar),
                modifier = Modifier.fillMaxWidth(),
                habilitado = !cerrada,
                onClick = {
                    val e1 = if (vm.nombre.value.isBlank()) errNombre else null
                    val e2 = if (!Calculos.rutValido(vm.rut.value)) errRut else null
                    val e3 = if (vm.cargo.value.isBlank()) errCargo else null
                    vm.errNombre.value = e1; vm.errRut.value = e2; vm.errCargo.value = e3
                    if (e1 != null || e2 != null || e3 != null) return@BotonPrincipal
                    if (avisoPendientes != null) {
                        scope.launch { snackbar.showSnackbar(avisoPendientes) }
                        return@BotonPrincipal
                    }
                    val error = runCatching {
                        vm.cerrarVisita(idVisita, vm.nombre.value, vm.rut.value, vm.cargo.value)
                    }.exceptionOrNull()?.message
                    tick++
                    scope.launch {
                        snackbar.showSnackbar(error ?: actaCerrada)
                    }
                })
            FilaVacia(Dimens.salto2)
            val sinPdf = stringResource(R.string.acta_no_pdf)
            BotonBordeado(stringResource(R.string.acta_btn_pdf), onClick = {
                scope.launch { snackbar.showSnackbar(sinPdf) }
            })
        }
        FilaVacia(Dimens.espacioGrande)
        SnackbarHost(snackbar)
    }
}

@Composable
private fun ColumnaDato(clave: String, valor: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(clave, style = MaterialTheme.typography.labelSmall, color = GrisTexto())
        Text(valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}



@Composable
private fun FilaActa(
    numero: Int, titulo: String, detalle: String, estado: String,
    resultado: Resultado? = null, noRealizada: Boolean = false
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(FondoFilaTabla(), RoundedCornerShape(Dimens.radioSegmento))
            .padding(horizontal = Dimens.salto3, vertical = Dimens.salto2)
            // la fila se lee de una sola vez: número, prueba, detalle y estado juntos
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically
    ) {
        CirculoPrueba(numero, resultado, noRealizada)
        Spacer(Modifier.width(Dimens.salto3))
        Column(Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            Text(detalle, style = MaterialTheme.typography.labelSmall, color = GrisTexto())
        }
        Text(
            estado,
            style = MaterialTheme.typography.labelMedium,
            color = colorResultado(resultado),
            textAlign = TextAlign.End
        )
    }
}



@Composable
private fun colorResultado(resultado: Resultado?): Color = when (resultado) {
    Resultado.APROBADO -> VerdeAprobado()
    Resultado.OBSERVADO -> NaranjaObservado()
    Resultado.RECHAZADO -> RojoRechazado()
    null -> GrisTexto()
}



@Composable
private fun estadoPrueba(p: Prueba): String =
    if (!p.realizada) stringResource(R.string.acta_registrada)
    else p.resultado?.nombre ?: stringResource(R.string.acta_registrada)




@Composable
private fun detallePrueba(p: Prueba): String =
    if (!p.realizada)
        stringResource(
            R.string.acta_no_realizada,
            p.motivoNoRealizada.ifBlank { stringResource(R.string.acta_sin_motivo) }
        )
    else resumenCorto(p.tipoPrueba, p.elemento, p.mediciones)




private fun limpiarRut(entrada: String): String =
    entrada.filter { it.isDigit() || it.equals('K', ignoreCase = true) }.uppercase().take(9)

private object TransformacionRut : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val limpio = text.text
        val conDigitoVerificador = limpio.length == 9 || limpio.endsWith("K")
        val cuerpo = (if (conDigitoVerificador) limpio.dropLast(1) else limpio).filter { it.isDigit() }
        val dv = if (conDigitoVerificador) limpio.last().toString() else ""
        val pares = mutableListOf<Pair<Char, Int?>>()
        var restante = if (cuerpo.length % 3 == 0) 3 else cuerpo.length % 3
        cuerpo.forEachIndexed { i, caracter ->
            if (restante == 0) {
                pares += '.' to null
                restante = 3
            }
            pares += caracter to i
            restante--
        }
        if (dv.isNotEmpty()) {
            pares += '-' to null
            pares += dv[0] to cuerpo.length
        }
        return TransformedText(
            AnnotatedString(pares.map { it.first }.joinToString("")),
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    val posicion = pares.indexOfFirst { it.second != null && it.second!! >= offset }
                    return if (posicion < 0) pares.size else posicion
                }

                override fun transformedToOriginal(offset: Int): Int =
                    (0 until minOf(offset, pares.size)).count { pares[it].second != null }
            }
        )
    }
}

private fun String.horaCorta(): String {
    val partes = split(" ")
    val fecha = partes.getOrElse(0) { "" }.split("-")
    val dia = if (fecha.size == 3) "${fecha[2]}-${fecha[1]}" else ""
    val hora = partes.getOrElse(1) { "" }.split(":").take(2).joinToString(":")
    return listOf(dia, hora).filter { it.isNotEmpty() }.joinToString(" ")
}

@Composable
private fun resumenCorto(tipo: TipoPrueba, elemento: String, m: Map<String, Double>): String = when (tipo) {
    TipoPrueba.AISLACION -> {
        val ip = Calculos.indicePolarizacion(m["LECTURA_1MIN"], m["LECTURA_10MIN"])
        stringResource(
            R.string.acta_det_aislacion, elemento,
            Calculos.fmt(m["LECTURA_1MIN"], 0), Calculos.fmt(m["LECTURA_10MIN"], 0), Calculos.fmt(ip)
        )
    }
    TipoPrueba.TTR -> stringResource(
        R.string.acta_det_ttr,
        Calculos.fmt(m["POSICION_TAP"], 0),
        Calculos.fmt(m["RELACION_TEORICA"]),
        Calculos.fmt(m["RELACION_MEDIDA"]),
        Calculos.fmt(Calculos.errorTtr(m["RELACION_TEORICA"],
            m["RELACION_MEDIDA"]))
    )
    TipoPrueba.DEVANADOS ->
        stringResource(R.string.acta_det_devanados, elemento, Calculos.fmt(m["RESISTENCIA"]))
    TipoPrueba.CONTACTOS ->
        stringResource(R.string.acta_det_contactos, elemento, Calculos.fmt(m["RESISTENCIA_CONTACTO"], 1))
    TipoPrueba.OPERACION_RELE -> stringResource(
        R.string.acta_det_rele, elemento,
        Calculos.fmt(m["TIEMPO_MEDIDO"], 1), Calculos.fmt(m["TIEMPO_ESPERADO"], 1),
        Calculos.fmt(m["CORRIENTE_OPERACION"])
    )
}
