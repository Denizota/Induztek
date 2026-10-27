package cl.duoc.induztek.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.Context
import cl.duoc.induztek.R
import cl.duoc.induztek.data.fechaCorta
import cl.duoc.induztek.model.Equipo
import cl.duoc.induztek.model.EstadoProtocolo
import cl.duoc.induztek.model.Instrumento
import cl.duoc.induztek.model.Protocolo
import cl.duoc.induztek.model.Resultado
import cl.duoc.induztek.model.TipoPrueba
import cl.duoc.induztek.model.Usuario
import cl.duoc.induztek.model.Calculos
import cl.duoc.induztek.ui.components.BotonBordeado
import cl.duoc.induztek.ui.components.BotonPrincipal
import cl.duoc.induztek.ui.components.CajaBordeada
import cl.duoc.induztek.ui.components.CampoNumero
import cl.duoc.induztek.ui.components.CampoNumeroSugerido
import cl.duoc.induztek.ui.components.CampoSeleccion
import cl.duoc.induztek.ui.components.CampoTexto
import cl.duoc.induztek.ui.components.ControlSegmentado
import cl.duoc.induztek.ui.components.EncabezadoPantalla
import cl.duoc.induztek.ui.components.EtiquetaCampo
import cl.duoc.induztek.ui.components.FilaVacia
import cl.duoc.induztek.ui.components.Pestanas
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.GrisTexto
import cl.duoc.induztek.ui.theme.InduztekTheme
import cl.duoc.induztek.ui.theme.LavandaBorde
import cl.duoc.induztek.ui.theme.NaranjaObservado
import cl.duoc.induztek.ui.theme.RojoRechazado
import cl.duoc.induztek.ui.theme.VerdeAprobado
import cl.duoc.induztek.ui.components.fondoPantallaInterna
import cl.duoc.induztek.viewmodel.PruebaFormsViewModel
import cl.duoc.induztek.viewmodel.aMega
import cl.duoc.induztek.viewmodel.formatear
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.clearAndSetSemantics
import cl.duoc.induztek.viewmodel.parametrosDe


@Composable
fun PruebaFormScreen(
    idEquipo: Int,
    tipo: TipoPrueba,
    usuario: Usuario,
    onAtras: () -> Unit,
    onGuardado: () -> Unit,
    vm: PruebaFormsViewModel = viewModel()
) {
    val equipo = remember(idEquipo) { vm.equipo(idEquipo) } ?: return
    val bloqueo = remember { vm.bloqueo(idEquipo, usuario.idUsuario) }


    val p = if (bloqueo == null) remember(idEquipo) { vm.protocoloEditable(idEquipo, usuario.idUsuario) } else null
    val tipos = remember(idEquipo) { vm.tiposDe(idEquipo) }
    if (p != null) vm.cargar(p.idProtocolo)

    PruebaFormContent(
        equipo = equipo, protocolo = p, tipos = tipos, estado = vm.estado,
        tipoInicial = tipo, bloqueo = bloqueo,
        instrumentosPara = vm::instrumentosPara,
        sugerencias = vm::sugerencias,
        valorAnterior = vm::valorAnterior,
        onAtras = onAtras, onGuardado = onGuardado,
        onGuardar = vm::guardar,
        onGuardarNoRealizada = vm::guardarNoRealizada
    )
}



@Composable
private fun PruebaFormContent(
    equipo: Equipo,
    protocolo: Protocolo?,
    tipos: List<TipoPrueba>,
    estado: MutableMap<String, String>,
    tipoInicial: TipoPrueba,
    bloqueo: String?,
    instrumentosPara: (TipoPrueba) -> List<Instrumento>,
    sugerencias: (Int, TipoPrueba, String, Int) -> List<String>,
    valorAnterior: (Int, TipoPrueba, String, Int) -> Double?,
    onAtras: () -> Unit,
    onGuardado: () -> Unit,
    onGuardar: (
        Int, TipoPrueba, Equipo, String, List<Instrumento>, String, Map<String, String>,
        String, String, Boolean, String
    ) -> String?,
    onGuardarNoRealizada: (Int, TipoPrueba, String, String, String) -> Unit
) {
    if (bloqueo != null) {
        Column(
            Modifier.fillMaxSize().padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.Center
        ) {
            Text(bloqueo, fontWeight = FontWeight.Medium)
            FilaVacia(Dimens.salto5)
            BotonPrincipal(
                texto = stringResource(R.string.btn_volver),
                onClick = onAtras,
                modifier = Modifier.fillMaxWidth()
            )
        }
        return
    }
    val p = protocolo ?: return

    var tipoSel by remember { mutableStateOf(tipoInicial) }
    var unidad by remember { mutableStateOf("MΩ") }
    var errorGuardado by remember { mutableStateOf<String?>(null) }

    fun dato(t: TipoPrueba, campo: String) = estado["${t.name}|_$campo"] ?: ""
    fun poner(t: TipoPrueba, campo: String, valor: String) { estado["${t.name}|_$campo"] = valor }

    val opciones = elementosPosibles(equipo, tipoSel)
    val elemento = dato(tipoSel, "elemento").takeIf { it.isNotEmpty() } ?: opciones.first()
    fun texto(parametro: String) = estado["$elemento|$tipoSel|$parametro"] ?: ""
    fun setV(parametro: String, valor: String) { estado["$elemento|$tipoSel|$parametro"] = valor }

    val instrumentos = remember(tipoSel) { instrumentosPara(tipoSel) }
    val instrumentoNombre = dato(tipoSel, "instrumento")
    val comentario = dato(tipoSel, "comentario")
    val motivo = dato(tipoSel, "motivo")
    val noRealizada = dato(tipoSel, "noRealizada") == "1"
    val resultadoManual = dato(tipoSel, "resultado")
    val releOpera = dato(tipoSel, "opera") != "0"


    val contexto = LocalContext.current
    val errMotivo = stringResource(R.string.form_err_motivo)

    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .fondoPantallaInterna()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.espacioMedio)
    ) {
        EncabezadoPantalla(
            titulo = stringResource(R.string.form_titulo),
            subtitulo = stringResource(R.string.form_subtitulo, equipo.tag, p.numero, equipo.instalacionNombre),
            onAtras = onAtras
        )
        Pestanas(tipos.map { nombrePestana(contexto, it) }, nombrePestana(contexto, tipoSel)) { elegido ->
            tipoSel = tipos.first { nombrePestana(contexto, it) == elegido }
        }
        FilaVacia(Dimens.espacioPestanaAContent)

        Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioEntreCampos)) {
            if (tipoSel != TipoPrueba.AISLACION && opciones.size > 1) CampoSeleccion(
                valor = elemento,
                opciones = opciones,
                onSeleccionar = { poner(tipoSel, "elemento", it) },
                etiqueta = etiquetaElemento(tipoSel),
                textoSugerido = stringResource(R.string.placeholder_seleccion)
            )
            when (tipoSel) {
                TipoPrueba.AISLACION -> BloqueAislacion(
                    equipo, elemento, opciones, { poner(tipoSel, "elemento", it) },
                    ::texto, ::setV, unidad,
                    { nueva -> unidad = reescalar(tipoSel, elemento, estado, unidad, nueva) },
                    sugerencias, valorAnterior, p.idProtocolo
                )
                TipoPrueba.TTR -> BloqueTtr(equipo, ::texto, ::setV, valorAnterior, p.idProtocolo)
                TipoPrueba.DEVANADOS -> BloqueDevanados(
                    equipo, elemento, ::texto, ::setV, resultadoManual,
                    { poner(tipoSel, "resultado", it) }, valorAnterior, p.idProtocolo
                )
                TipoPrueba.CONTACTOS -> BloqueContactos(equipo, ::texto, ::setV, valorAnterior, p.idProtocolo)
                TipoPrueba.OPERACION_RELE -> BloqueRele(
                    equipo, ::texto, ::setV, releOpera, { poner(tipoSel, "opera", if (it) "1" else "0") },
                    valorAnterior, p.idProtocolo
                )
            }

            HorizontalDivider(thickness = Dimens.grosorDivisor, color = LavandaBorde())

            CampoSeleccion(
                valor = instrumentoNombre,
                opciones = instrumentos.map { "${it.nombre} - ${it.numeroSerie}" },
                onSeleccionar = { poner(tipoSel, "instrumento", it) },
                etiqueta = stringResource(R.string.form_instrumento),
                textoSugerido = stringResource(R.string.placeholder_seleccion),
                habilitado = !noRealizada
            )
            instrumentos.find { "${it.nombre} - ${it.numeroSerie}" == instrumentoNombre }?.let { ins ->
                Text(
                    stringResource(R.string.form_calibracion, ins.fechaVence.fechaCorta()),
                    style = MaterialTheme.typography.bodySmall, color = GrisTexto()
                )
            }

            CampoTexto(
                valor = comentario,
                onValorCambia = { poner(tipoSel, "comentario", it) },
                etiqueta = stringResource(R.string.form_comentarios),
                textoSugerido = stringResource(R.string.campo_placeholder_escribe),
                habilitado = !noRealizada,
                alto = Dimens.alturaCampoTextoLargo,
                unaSolaLinea = false,
                accionTeclado = ImeAction.Default
            )


            if (noRealizada) CampoTexto(
                valor = motivo,
                onValorCambia = { poner(tipoSel, "motivo", it); errorGuardado = null },
                etiqueta = stringResource(R.string.form_motivos),
                textoSugerido = stringResource(R.string.campo_placeholder_escribe),
                alto = Dimens.alturaCampoTextoLargo,
                unaSolaLinea = false,
                accionTeclado = ImeAction.Default
            )
        }

        FilaVacia(Dimens.salto5)
        Row(
            Modifier.semantics(mergeDescendants = true) { },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.form_no_realizada), Modifier.weight(1f))
            Switch(checked = noRealizada, onCheckedChange = { poner(tipoSel, "noRealizada", if (it) "1" else "0") })
        }

        FilaVacia(Dimens.salto4)
        errorGuardado?.let {
            Text(
                it, Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall, color = RojoRechazado(), textAlign = TextAlign.Center
            )
            FilaVacia(Dimens.salto3)
        }
        BotonPrincipal(
            texto = stringResource(R.string.form_guardar),
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (noRealizada) {
                    if (motivo.isBlank()) {
                        errorGuardado = errMotivo
                        return@BotonPrincipal
                    }
                    onGuardarNoRealizada(p.idProtocolo, tipoSel, elemento, motivo, comentario)
                    onGuardado()
                    return@BotonPrincipal
                }
                val err = onGuardar(
                    p.idProtocolo, tipoSel, equipo, elemento, instrumentos, instrumentoNombre,
                    parametrosDe(tipoSel).associateWith { texto(it) },
                    unidad, resultadoManual, releOpera, comentario
                )
                if (err != null) errorGuardado = err else onGuardado()
            })
        FilaVacia(Dimens.salto2)
        BotonBordeado(stringResource(R.string.btn_cancelar), onClick = onAtras)
        FilaVacia(Dimens.espacioGrande)
    }
}





@Composable
private fun BloqueAislacion(
    equipo: Equipo, elemento: String, opciones: List<String>, onElemento: (String) -> Unit,
    texto: (String) -> String, setV: (String, String) -> Unit,
    unidad: String, onUnidad: (String) -> Unit,
    sugerencias: (Int, TipoPrueba, String, Int) -> List<String>,
    valorAnterior: (Int, TipoPrueba, String, Int) -> Double?, idProtocolo: Int
) {
    val con10 = equipo.exigeLectura10Min
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioEntreCampos)) {
        if (opciones.size > 1) CampoSeleccion(
            valor = elemento,
            opciones = opciones,
            onSeleccionar = onElemento,
            etiqueta = etiquetaElemento(TipoPrueba.AISLACION),
            textoSugerido = stringResource(R.string.placeholder_seleccion),
            modifier = Modifier.weight(1f)
        )


        CampoNumeroSugerido(
            texto("TENSION_PRUEBA"), { setV("TENSION_PRUEBA", it) },
            stringResource(R.string.form_tension_prueba),
            sugerencias(equipo.idEquipo, TipoPrueba.AISLACION, "TENSION_PRUEBA", idProtocolo),
            Modifier.weight(1f), stringResource(R.string.unidad_kv)
        )
    }
    EtiquetaCampo(texto = stringResource(R.string.form_unidad_medida))
    ControlSegmentado(listOf("kΩ", "MΩ", "GΩ"), unidad, onUnidad)
    val sugeridas = sugerencias(equipo.idEquipo, TipoPrueba.AISLACION, "LECTURA_1MIN", idProtocolo)
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioEntreCampos)) {
        CampoNumeroSugerido(
            texto("LECTURA_1MIN"), { setV("LECTURA_1MIN", it) },
            stringResource(R.string.form_lectura_1min), sugeridas, Modifier.weight(1f), unidad
        )
        if (con10) CampoNumeroSugerido(
            texto("LECTURA_10MIN"), { setV("LECTURA_10MIN", it) },
            stringResource(R.string.form_lectura_10min), sugeridas, Modifier.weight(1f), unidad
        )
    }
    Text(
        stringResource(
            if (con10) R.string.form_rango_esperado else R.string.form_rechazo_minimo
        ),
        style = MaterialTheme.typography.bodySmall, color = GrisTexto()
    )


    val l1 = aMega(texto("LECTURA_1MIN").toDoubleOrNull(), unidad)
    val l10 = if (con10) aMega(texto("LECTURA_10MIN").toDoubleOrNull(), unidad) else null
    val ip = Calculos.indicePolarizacion(l1, l10)


    val resultado = when {
        con10 && l10 == null -> null
        else -> Calculos.resultadoAislacion(ip, l1)
    }
    val anterior = l1?.let {
        valorAnterior(equipo.idEquipo, TipoPrueba.AISLACION, "LECTURA_1MIN", idProtocolo)
    }

    if (con10) {
        FilaCalculo(
            stringResource(R.string.form_ip_calculado), Calculos.fmt(ip),
            stringResource(R.string.form_ip_detalle, Calculos.fmt(l10, 0), Calculos.fmt(l1, 0)),
            resultado
        )
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f))
            CajaResultado(resultado)
        }
    }
    ComparacionAnterior(l1, anterior, stringResource(R.string.unidad_mohm))
    if (Calculos.esVariacionSignificativa(Calculos.variacionPct(l1, anterior))) {
        NotaVariacion()
    }
}



@Composable
private fun FilaCalculo(etiqueta: String, valor: String, detalle: String, resultado: Resultado?) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.salto3)
    ) {
        CajaBordeada(Modifier.weight(3f)) {
            EtiquetaCampo(texto = etiqueta)
            Text(valor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(detalle, style = MaterialTheme.typography.bodySmall, color = GrisTexto())
        }
        Text("›", color = GrisTexto(), fontWeight = FontWeight.Bold, maxLines = 1,
            modifier = Modifier.clearAndSetSemantics { })
        CajaBordeada(Modifier.weight(2f).fillMaxHeight(), centrado = true) { CajaResultado(resultado) }
    }
}


@Composable
private fun CajaResultado(resultado: Resultado?) {
    val color = when (resultado) {
        Resultado.APROBADO -> VerdeAprobado()
        Resultado.OBSERVADO -> NaranjaObservado()
        Resultado.RECHAZADO -> RojoRechazado()
        null -> GrisTexto()
    }
    Text(
        resultado?.nombre ?: stringResource(R.string.resultado_pendiente),
        Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium, color = color,
        fontWeight = FontWeight.Medium, textAlign = TextAlign.Center
    )
}


@Composable
private fun ComparacionAnterior(
    actual: Double?, anterior: Double?, unidad: String, decimales: Int = 0
) {
    if (actual == null) return
    Text(
        anterior?.let {
            stringResource(
                R.string.form_anterior,
                Calculos.fmt(it, decimales), unidad,
                Calculos.fmt(Calculos.variacionPct(actual, it), 1)
            )
        } ?: stringResource(R.string.form_sin_anterior),
        style = MaterialTheme.typography.bodySmall, color = GrisTexto()
    )
}


@Composable
private fun BloqueTtr(
    equipo: Equipo, texto: (String) -> String, setV: (String, String) -> Unit,
    valorAnterior: (Int, TipoPrueba, String, Int) -> Double?, idProtocolo: Int
) {
    if (texto("RELACION_TEORICA").isBlank() && equipo.tensionPrimariaKv != null) {
        setV("RELACION_TEORICA", relacionTeoricaPlaca(equipo))
    }
    val medida = texto("RELACION_MEDIDA").toDoubleOrNull()
    val error = Calculos.errorTtr(
        texto("RELACION_TEORICA").toDoubleOrNull(), medida
    )
    val anterior = medida?.let {
        valorAnterior(equipo.idEquipo, TipoPrueba.TTR, "RELACION_MEDIDA", idProtocolo)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioEntreCampos)) {
        CampoNumero(
            texto("POSICION_TAP"), { setV("POSICION_TAP", it) },
            stringResource(R.string.form_posicion_tap), modifier = Modifier.weight(1f)
        )
        CampoNumero(
            texto("RELACION_TEORICA"), { setV("RELACION_TEORICA", it) },
            stringResource(R.string.form_relacion_teorica), "—", Modifier.weight(1f)
        )
    }
    CampoNumero(
        texto("RELACION_MEDIDA"), { setV("RELACION_MEDIDA", it) },
        stringResource(R.string.form_relacion_medida), "—"
    )
    FilaCalculo(
        stringResource(R.string.form_error_calculado),
        error?.let { stringResource(R.string.valor_porcentaje, Calculos.fmt(it)) } ?: "—",
        stringResource(R.string.form_criterio_ttr),
        Calculos.resultadoTtr(error)
    )
    ComparacionAnterior(medida, anterior, "—", decimales = 2)
    if (Calculos.esVariacionSignificativa(Calculos.variacionPct(medida, anterior))) NotaVariacion()
}


@Composable
private fun BloqueDevanados(
    equipo: Equipo, elemento: String, texto: (String) -> String, setV: (String, String) -> Unit,
    resultadoManual: String, onManual: (String) -> Unit,
    valorAnterior: (Int, TipoPrueba, String, Int) -> Double?, idProtocolo: Int
) {
    val resistencia = texto("RESISTENCIA").toDoubleOrNull()
    val anterior = resistencia?.let {
        valorAnterior(equipo.idEquipo, TipoPrueba.DEVANADOS, "RESISTENCIA", idProtocolo)
    }
    CampoNumero(
        texto("RESISTENCIA"), { setV("RESISTENCIA", it) },
        stringResource(R.string.form_resistencia_fase), stringResource(R.string.unidad_mmohm)
    )
    CampoSeleccion(
        valor = resultadoManual,
        opciones = Resultado.entries.map { it.nombre },
        onSeleccionar = onManual,
        etiqueta = stringResource(R.string.form_resultado_manual),
        textoSugerido = stringResource(R.string.placeholder_seleccion)
    )
    Text(
        stringResource(R.string.form_sin_criterio),
        style = MaterialTheme.typography.bodySmall, color = GrisTexto()
    )
    ComparacionAnterior(resistencia, anterior, stringResource(R.string.unidad_mmohm), decimales = 2)
    if (Calculos.esVariacionSignificativa(Calculos.variacionPct(resistencia, anterior))) NotaVariacion()
}


@Composable
private fun BloqueContactos(
    equipo: Equipo, texto: (String) -> String, setV: (String, String) -> Unit,
    valorAnterior: (Int, TipoPrueba, String, Int) -> Double?, idProtocolo: Int
) {
    val valor = texto("RESISTENCIA_CONTACTO").toDoubleOrNull()
    val pct = Calculos.porcentajeContactos(valor, equipo.maxContactosUohm)
    val anterior = valor?.let {
        valorAnterior(equipo.idEquipo, TipoPrueba.CONTACTOS, "RESISTENCIA_CONTACTO", idProtocolo)
    }
    CampoNumero(
        texto("RESISTENCIA_CONTACTO"), { setV("RESISTENCIA_CONTACTO", it) },
        stringResource(R.string.form_resistencia_polo), stringResource(R.string.unidad_umohm)
    )
    FilaCalculo(
        stringResource(R.string.form_pct_placa_calculado),
        pct?.let { stringResource(R.string.valor_porcentaje, Calculos.fmt(it, 1)) } ?: "—",
        stringResource(R.string.form_maximo_placa, Calculos.fmt(equipo.maxContactosUohm, 0)),
        Calculos.resultadoContactos(pct)
    )
    ComparacionAnterior(valor, anterior, stringResource(R.string.unidad_umohm), decimales = 1)
    if (Calculos.esVariacionSignificativa(Calculos.variacionPct(valor, anterior))) NotaVariacion()
}


@Composable
private fun BloqueRele(
    equipo: Equipo, texto: (String) -> String, setV: (String, String) -> Unit,
    opera: Boolean, onOpera: (Boolean) -> Unit,
    valorAnterior: (Int, TipoPrueba, String, Int) -> Double?, idProtocolo: Int
) {
    val medido = texto("TIEMPO_MEDIDO").toDoubleOrNull()
    val pct = Calculos.porcentajeDiferenciaTiempo(medido, texto("TIEMPO_ESPERADO").toDoubleOrNull())
    val anterior = medido?.let {
        valorAnterior(equipo.idEquipo, TipoPrueba.OPERACION_RELE, "TIEMPO_MEDIDO", idProtocolo)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioEntreCampos)) {
        CampoNumero(
            texto("CORRIENTE_AJUSTE"), { setV("CORRIENTE_AJUSTE", it) },
            stringResource(R.string.form_ajuste_corriente), stringResource(R.string.unidad_amper),
            Modifier.weight(1f)
        )
        CampoNumero(
            texto("CORRIENTE_OPERACION"), { setV("CORRIENTE_OPERACION", it) },
            stringResource(R.string.form_corriente_operacion), stringResource(R.string.unidad_amper),
            Modifier.weight(1f)
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioEntreCampos)) {
        CampoNumero(
            texto("TIEMPO_ESPERADO"), { setV("TIEMPO_ESPERADO", it) },
            stringResource(R.string.form_tiempo_esperado), stringResource(R.string.unidad_ms),
            Modifier.weight(1f)
        )
        CampoNumero(
            texto("TIEMPO_MEDIDO"), { setV("TIEMPO_MEDIDO", it) },
            stringResource(R.string.form_tiempo_medido), stringResource(R.string.unidad_ms),
            Modifier.weight(1f)
        )
    }
    Row(
        Modifier.semantics(mergeDescendants = true) { },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Switch(checked = opera, onCheckedChange = onOpera)
        Spacer(Modifier.width(Dimens.salto3))
        Text(stringResource(R.string.form_rele_opera))
    }
    FilaCalculo(
        stringResource(R.string.form_diferencia_calculada),
        pct?.let { stringResource(R.string.valor_porcentaje, Calculos.fmt(it, 1)) } ?: "—",
        stringResource(R.string.form_criterio_rele),
        Calculos.resultadoRele(opera, pct)
    )
    ComparacionAnterior(medido, anterior, stringResource(R.string.unidad_ms), decimales = 2)
    if (Calculos.esVariacionSignificativa(Calculos.variacionPct(medido, anterior))) NotaVariacion()
}



@Composable
private fun NotaVariacion() {
    Text(
        stringResource(R.string.alerta_variacion),
        Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodySmall, color = RojoRechazado(), textAlign = TextAlign.Center
    )
}




private fun nombrePestana(contexto: Context, tipo: TipoPrueba): String = when (tipo) {
    TipoPrueba.AISLACION -> contexto.getString(R.string.pestana_aislacion)
    TipoPrueba.TTR -> contexto.getString(R.string.pestana_relacion)
    TipoPrueba.DEVANADOS -> contexto.getString(R.string.pestana_devanados)
    TipoPrueba.CONTACTOS -> contexto.getString(R.string.pestana_contactos)
    TipoPrueba.OPERACION_RELE -> contexto.getString(R.string.pestana_rele)
}


@Composable
private fun etiquetaElemento(tipo: TipoPrueba) = when (tipo) {
    TipoPrueba.AISLACION -> stringResource(R.string.elem_devanado)
    TipoPrueba.DEVANADOS -> stringResource(R.string.elem_fase)
    TipoPrueba.CONTACTOS -> stringResource(R.string.elem_polo)
    TipoPrueba.OPERACION_RELE -> stringResource(R.string.elem_elemento_prueba)
    TipoPrueba.TTR -> stringResource(R.string.elem_conexion)
}


private fun reescalar(
    tipo: TipoPrueba, elemento: String, estado: MutableMap<String, String>, anterior: String, nueva: String
): String {
    if (anterior != nueva) listOf("LECTURA_1MIN", "LECTURA_10MIN").forEach { parametro ->
        val clave = "$elemento|${tipo.name}|$parametro"
        estado[clave]?.toDoubleOrNull()
            ?.let { aMega(it, anterior) }
            ?.let { enMega -> estado[clave] = formatear(fromMega(enMega, nueva)) }
    }
    return nueva
}


private fun fromMega(mega: Double, unidad: String): Double = when (unidad) {
    "kΩ" -> mega * 1000
    "GΩ" -> mega / 1000
    else -> mega
}



private fun elementosPosibles(equipo: Equipo, tipo: TipoPrueba): List<String> = when (tipo) {
    TipoPrueba.AISLACION -> when (equipo.tipoEquipo) {
        "Transformador" -> listOf("AT-T", "AT-BT", "BT-T")
        "Interruptor", "Celda MT" -> listOf("MT-BT", "MT-Tierra", "Fases-Tierra")
        "Cable de poder" -> listOf("Fase R", "Fase S", "Fase T")
        else -> listOf("Equipo completo")
    }
    TipoPrueba.DEVANADOS -> listOf("AT-R", "AT-S", "AT-T", "BT-r", "BT-s", "BT-t")
    TipoPrueba.CONTACTOS -> listOf("Polo 1", "Polo 2", "Polo 3")
    TipoPrueba.OPERACION_RELE -> listOf("Disparo de fase", "Disparo de neutro", "Disparo de tierra")
    TipoPrueba.TTR -> listOf("3 fases / cambiador de derivaciones")
}



private fun relacionTeoricaPlaca(equipo: Equipo): String =
    equipo.tensionPrimariaKv?.let { tp ->
        equipo.tensionSecundariaKv?.takeIf { it > 0.0 }?.let { ts -> formatear(tp / ts) }
    } ?: ""






private val TransformadorForm = Equipo(
    idEquipo = 1, idInstalacion = 1, tag = "TR-01", idTipoEquipo = 1, tipoEquipo = "Transformador",
    clasificacion = "PRIMARIO", marca = "Rhona", modelo = "RH-18733", numeroSerie = "RH-18733",
    anioFabricacion = 2012, potenciaKva = 1500.0, tensionPrimariaKv = 12.0, tensionSecundariaKv = 0.4,
    corrienteNominalA = null, maxContactosUohm = null, observaciones = "", instalacionNombre = "Planta Lampa"
)

private val ReleForm = Equipo(
    idEquipo = 6, idInstalacion = 5, tag = "REL-50/51-A", idTipoEquipo = 5, tipoEquipo = "Relé de protección",
    clasificacion = "SECUNDARIO", marca = "SEL", modelo = "SEL-751-30219", numeroSerie = "SEL-751-30219",
    anioFabricacion = 2020, potenciaKva = null, tensionPrimariaKv = null, tensionSecundariaKv = null,
    corrienteNominalA = null, maxContactosUohm = null, observaciones = "Sobrecorriente, ajuste 5 A",
    instalacionNombre = "Site Huechuraba"
)

private val ProtocoloForm = Protocolo(
    idProtocolo = 501, numero = "PT-2026-0143", idVisita = 26, idEquipo = 1, idUsuarioRegistra = 7,
    fechaHoraPrueba = "2026-09-26 10:30", temperaturaC = 21.0, humedadPct = 48.0,
    estado = EstadoProtocolo.BORRADOR, equipoTag = "TR-01", instalacionNombre = "Planta Lampa"
)

private val InstrumentosForm = listOf(
    Instrumento(1, "Megóhmetro 5 kV", "Megger", "MIT525", "MIT-0045821", "2026-03-15", "2027-03-15"),
    Instrumento(3, "Medidor de relación (TTR)", "Megger", "TTR300", "TTR-30044", "2026-06-02", "2027-06-02"),
    Instrumento(4, "Micro-ohmímetro", "Megger", "DLRO10X", "DLR-10882", "2026-08-20", "2027-08-20"),
    Instrumento(5, "Equipo de inyección secundaria", "Omicron", "CMC 356", "CMC-HB411", "2026-01-05", "2027-01-05")
)



private val AnterioresForm = mapOf(
    "AISLACION|LECTURA_1MIN" to 11800.0,
    "TTR|RELACION_MEDIDA" to 30.15,
    "OPERACION_RELE|TIEMPO_MEDIDO" to 40.0
)

@Composable
private fun FormularioPreview(
    equipo: Equipo,
    tipos: List<TipoPrueba>,
    tipoSel: TipoPrueba,
    estado: MutableMap<String, String>
) {
    InduztekTheme {
        PruebaFormContent(
            equipo = equipo, protocolo = ProtocoloForm, tipos = tipos, estado = estado,
            tipoInicial = tipoSel, bloqueo = null,
            instrumentosPara = { InstrumentosForm },
            sugerencias = { _, _, _, _ -> listOf("5", "10") },
            valorAnterior = { _, tipo, parametro, _ -> AnterioresForm["${tipo.name}|$parametro"] },
            onAtras = {}, onGuardado = {},
            onGuardar = { _, _, _, _, _, _, _, _, _, _, _ -> null },
            onGuardarNoRealizada = { _, _, _, _, _ -> }
        )
    }
}



@Preview(showBackground = true, showSystemUi = true, name = "Aislación")
@Composable
private fun FormAislacionPreview() {
    FormularioPreview(
        equipo = TransformadorForm,
        tipos = listOf(TipoPrueba.AISLACION, TipoPrueba.TTR, TipoPrueba.DEVANADOS),
        tipoSel = TipoPrueba.AISLACION,
        estado = mutableMapOf(
            "AISLACION|_instrumento" to "Megóhmetro 5 kV - MIT-0045821",
            "AISLACION|_comentario" to "Buje seco, sin condensación",
            "AT-T|AISLACION|TENSION_PRUEBA" to "5",
            "AT-T|AISLACION|LECTURA_1MIN" to "12500",
            "AT-T|AISLACION|LECTURA_10MIN" to "31250"
        )
    )
}



@Preview(showBackground = true, showSystemUi = true, name = "Relación")
@Composable
private fun FormTtrPreview() {
    FormularioPreview(
        equipo = TransformadorForm,
        tipos = listOf(TipoPrueba.AISLACION, TipoPrueba.TTR, TipoPrueba.DEVANADOS),
        tipoSel = TipoPrueba.TTR,
        estado = mutableMapOf(
            "TTR|_instrumento" to "Medidor de relación (TTR) - TTR-30044",
            "3 fases / cambiador de derivaciones|TTR|POSICION_TAP" to "3",
            "3 fases / cambiador de derivaciones|TTR|RELACION_TEORICA" to "30",
            "3 fases / cambiador de derivaciones|TTR|RELACION_MEDIDA" to "30,62"
        )
    )
}



@Preview(showBackground = true, showSystemUi = true, name = "Operación de relé")
@Composable
private fun FormRelePreview() {
    FormularioPreview(
        equipo = ReleForm,
        tipos = listOf(TipoPrueba.OPERACION_RELE),
        tipoSel = TipoPrueba.OPERACION_RELE,
        estado = mutableMapOf(
            "OPERACION_RELE|_instrumento" to "Equipo de inyección secundaria - CMC-HB411",
            "Disparo de fase|OPERACION_RELE|CORRIENTE_AJUSTE" to "5",
            "Disparo de fase|OPERACION_RELE|CORRIENTE_OPERACION" to "6,25",
            "Disparo de fase|OPERACION_RELE|TIEMPO_ESPERADO" to "40",
            "Disparo de fase|OPERACION_RELE|TIEMPO_MEDIDO" to "38,4"
        )
    )
}
