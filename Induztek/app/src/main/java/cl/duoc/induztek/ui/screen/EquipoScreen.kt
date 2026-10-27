package cl.duoc.induztek.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.induztek.R
import cl.duoc.induztek.model.Equipo
import cl.duoc.induztek.model.EstadoProtocolo
import cl.duoc.induztek.model.Protocolo
import cl.duoc.induztek.model.Prueba
import cl.duoc.induztek.model.Resultado
import cl.duoc.induztek.model.Rol
import cl.duoc.induztek.model.TipoPrueba
import cl.duoc.induztek.model.Usuario
import cl.duoc.induztek.model.Calculos
import cl.duoc.induztek.ui.components.BotonBordeado
import cl.duoc.induztek.ui.components.BotonChico
import cl.duoc.induztek.ui.components.BotonPrincipal
import cl.duoc.induztek.ui.components.CampoNumero
import cl.duoc.induztek.ui.components.CampoTexto
import cl.duoc.induztek.ui.components.CirculoPrueba
import cl.duoc.induztek.ui.components.EncabezadoPantalla
import cl.duoc.induztek.ui.components.FilaVacia
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.GrisTexto
import cl.duoc.induztek.ui.theme.InduztekTheme
import cl.duoc.induztek.ui.theme.LavandaBorde
import cl.duoc.induztek.ui.theme.RojoRechazado
import cl.duoc.induztek.ui.components.fondoPantallaInterna
import cl.duoc.induztek.viewmodel.EquipoViewModel



@Composable
fun EquipoScreen(
    idEquipo: Int,
    usuario: Usuario,
    onAtras: () -> Unit,
    onRegistrarPrueba: (TipoPrueba) -> Unit,
    onIrActa: (Int) -> Unit,
    vm: EquipoViewModel = viewModel()
) {
    val equipo = remember(idEquipo) { vm.equipo(idEquipo) } ?: return
    LaunchedEffect(idEquipo) { vm.refrescar(idEquipo) }

    vm.prepararBorrador(vm.protocoloActual)

    EquipoContent(
        equipo = equipo, usuario = usuario, protocolo = vm.protocoloActual,
        pruebas = vm.pruebas, tipos = vm.tipos,
        temp = vm.temp.value, humedad = vm.humedad.value,
        onTemp = { vm.temp.value = it }, onHumedad = { vm.humedad.value = it },
        historial = vm.historial, mensaje = vm.mensaje, motivo = vm.motivoDevolucion.value,
        onMotivo = { vm.motivoDevolucion.value = it },
        onAbrirMotivo = { vm.motivoDevolucion.value = "" },
        onCerrarMotivo = { vm.motivoDevolucion.value = null },
        fechaCalibracion = { p -> vm.calibracionDe(p)?.fechaVence },
        visitaAbierta = { id -> vm.visitaAbierta(id) },
        ultimoMotivo = { id -> vm.motivoDevolucionGuardada(id) },
        onAtras = onAtras, onRegistrarPrueba = onRegistrarPrueba, onIrActa = onIrActa,
        onMarcarRegistrado = { p, tipos, pruebas -> vm.marcarRegistrado(p, usuario, tipos, pruebas) },
        onFormalizar = { p -> vm.formalizar(p, usuario) },
        onDevolver = { p, m -> vm.devolverConObservacion(p, usuario, m) }
    )
}



@Composable
private fun EquipoContent(
    equipo: Equipo,
    usuario: Usuario,
    protocolo: Protocolo?,
    pruebas: List<Prueba>,
    tipos: List<TipoPrueba>,
    temp: String,
    humedad: String,
    onTemp: (String) -> Unit,
    onHumedad: (String) -> Unit,
    historial: List<Triple<String, String, Double>>,
    mensaje: String?,
    motivo: String?,
    onMotivo: (String) -> Unit,
    onAbrirMotivo: () -> Unit,
    onCerrarMotivo: () -> Unit,
    fechaCalibracion: (Prueba) -> String?,
    visitaAbierta: (Int) -> Boolean,
    ultimoMotivo: (Int) -> String,
    onAtras: () -> Unit,
    onRegistrarPrueba: (TipoPrueba) -> Unit,
    onIrActa: (Int) -> Unit,
    onMarcarRegistrado: (Protocolo, List<TipoPrueba>, List<Prueba>) -> Unit,
    onFormalizar: (Protocolo) -> Unit,
    onDevolver: (Protocolo, String) -> Unit
) {


    val editable = protocolo?.let {
        it.estado == EstadoProtocolo.BORRADOR || it.estado == EstadoProtocolo.OBSERVADO
    } ?: true

    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .fondoPantallaInterna()
            .verticalScroll(rememberScrollState())
    ) {
        Column(Modifier.padding(horizontal = Dimens.margenPantalla)) {
            EncabezadoPantalla(
                titulo = equipo.tag,
                subtitulo = "${equipo.tipoEquipo} ${equipo.marca} ${equipo.modelo} " +
                        (equipo.potenciaKva?.let {
                            stringResource(R.string.ficha_valor_kva, Calculos.fmt(it, 0))
                        } ?: "") + "\n" +
                        equipo.instalacionNombre,
                onAtras = onAtras
            )
            FilaVacia(Dimens.salto7)
            FilaSpec(
                stringResource(R.string.ficha_potencia) to
                        (equipo.potenciaKva?.let {
                            stringResource(R.string.ficha_valor_kva, Calculos.fmt(it, 0))
                        } ?: "—"),
                stringResource(R.string.ficha_tensiones) to if (equipo.tensionPrimariaKv != null)
                    stringResource(
                        R.string.ficha_valor_tensiones,
                        Calculos.fmt(equipo.tensionPrimariaKv, 1), Calculos.fmt(equipo.tensionSecundariaKv, 1)
                    ) else "—",
                stringResource(R.string.ficha_anio) to equipo.anioFabricacion.toString()
            )
            FilaVacia(Dimens.salto8)
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.salto4)) {
                CampoNumero(
                    temp, onTemp,
                    stringResource(R.string.ficha_temperatura_ambiente),
                    stringResource(R.string.unidad_celsius), Modifier.weight(1f)
                )
                CampoNumero(
                    humedad, onHumedad,
                    stringResource(R.string.ficha_humedad),
                    stringResource(R.string.unidad_porcentaje), Modifier.weight(1f)
                )
            }
            FilaVacia(Dimens.salto7)
        }

        Divisor()

        Column(Modifier.padding(horizontal = Dimens.margenPantalla)) {
            Text(
                stringResource(R.string.ficha_pruebas_realizadas),
                style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold
            )



            tipos.forEachIndexed { indice, tipo ->
                val hechas = pruebas.filter { it.tipoPrueba == tipo }
                val resultado = Calculos.resultadoGlobal(hechas.map { it.resultado })
                FilaVacia(Dimens.salto4)
                Row(
                    Modifier.fillMaxWidth().height(Dimens.alturaFilaPrueba),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CirculoPrueba(
                        indice + 1, resultado,
                        noRealizada = hechas.isNotEmpty() && hechas.all { !it.realizada }
                    )
                    Column(Modifier.weight(1f).padding(horizontal = Dimens.salto3)) {
                        Text(tipo.nombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(pistaPrueba(tipo, hechas, equipo), style = MaterialTheme.typography.bodySmall, color = GrisTexto())
                        Text(estadoPrueba(hechas, resultado), style = MaterialTheme.typography.bodySmall, color = GrisTexto())
                    }
                    if (editable) {
                        if (hechas.isEmpty()) {
                            BotonChico(stringResource(R.string.ficha_registrar), onClick = { onRegistrarPrueba(tipo) })
                        } else {
                            TextButton(onClick = { onRegistrarPrueba(tipo) }) {
                                Text(
                                    stringResource(R.string.ficha_editar),
                                    style = MaterialTheme.typography.labelMedium, color = GrisTexto()
                                )
                            }
                        }
                    }
                }
                if (indice < tipos.lastIndex) {
                    FilaVacia(Dimens.salto4)
                    Divisor()
                }
            }
        }




        if (protocolo != null) {


            val medidas = pruebas.filter { it.realizada }
            if (medidas.isNotEmpty()) {
                FilaVacia(Dimens.salto7)
                Divisor()
                medidas.forEach { p ->
                    Column(Modifier.padding(horizontal = Dimens.margenPantalla)) {
                        FilaVacia(Dimens.salto7)
                        Text(
                            tituloResumen(p, equipo),
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold
                        )
                        FilaVacia(Dimens.salto5)
                        Row(Modifier.fillMaxWidth()) {
                            columnasResumen(p, equipo).forEachIndexed { i, c ->
                                if (i > 0) DivisorVertical()
                                ColumnaValor(c.clave, c.valor, c.sufijo, Modifier.weight(1f))
                            }
                        }
                        FilaVacia(Dimens.salto5)
                        Text(
                            stringResource(
                                R.string.ficha_resumen_calibrado,
                                fechaCalibracion(p) ?: stringResource(R.string.ficha_fecha_sin_dato)
                            ),
                            style = MaterialTheme.typography.bodySmall, color = GrisTexto()
                        )
                        FilaVacia(Dimens.salto7)
                    }
                }
            }

            Divisor()
            Column(Modifier.padding(horizontal = Dimens.margenPantalla)) {
                FilaVacia(Dimens.salto7)



                ContraPruebasAnteriores(historial)

                val faltan = tipos.filter { t -> pruebas.none { it.tipoPrueba == t } }
                val puedeCerrar = usuario.rol != Rol.CLIENTE

                FilaVacia(Dimens.salto8)



                if (puedeCerrar && editable) {
                    BotonPrincipal(
                        texto = stringResource(R.string.ficha_btn_registrado),
                        modifier = Modifier.fillMaxWidth(),
                        habilitado = faltan.isEmpty(),
                        onClick = { onMarcarRegistrado(protocolo, tipos, pruebas) }
                    )
                    if (faltan.isNotEmpty()) Text(
                        stringResource(
                            R.string.ficha_faltan,
                            faltan.joinToString(stringResource(R.string.ficha_conjuncion)) {
                                it.nombre.substringBefore(" (")
                            }
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = GrisTexto(),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }




                if (puedeCerrar && protocolo.estado == EstadoProtocolo.REGISTRADO &&
                    visitaAbierta(protocolo.idVisita) && usuario.rol == Rol.INGENIERO) {
                    BotonPrincipal(
                        texto = stringResource(R.string.ficha_btn_formalizar),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onFormalizar(protocolo) }
                    )
                    FilaVacia(Dimens.salto3)
                    BotonBordeado(
                        stringResource(R.string.ficha_btn_devolver),
                        onClick = onAbrirMotivo
                    )
                    motivo?.let { m ->
                        CampoTexto(
                            valor = m,
                            onValorCambia = onMotivo,
                            etiqueta = stringResource(R.string.ficha_campo_motivo),
                            textoSugerido = stringResource(R.string.campo_placeholder_escribe)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.salto2)) {
                            TextButton(onClick = onCerrarMotivo) {
                                Text(stringResource(R.string.btn_cancelar))
                            }
                            TextButton(onClick = { if (m.isNotBlank()) onDevolver(protocolo, m) }) {
                                Text(stringResource(R.string.ficha_confirmar))
                            }
                        }
                    }
                }



                if (protocolo.estado == EstadoProtocolo.OBSERVADO) {
                    val motivoGuardado = ultimoMotivo(protocolo.idProtocolo)
                    if (motivoGuardado.isNotEmpty()) {
                        FilaVacia(Dimens.salto3)
                        Text(
                            stringResource(R.string.ficha_devuelto, motivoGuardado),
                            style = MaterialTheme.typography.bodySmall, color = RojoRechazado()
                        )
                    }
                }


                if (!editable) {
                    FilaVacia(Dimens.salto6)
                    Text(
                        stringResource(
                            R.string.ficha_no_editable,
                            protocolo.estado.nombre.lowercase()
                        ),
                        style = MaterialTheme.typography.bodySmall, color = GrisTexto(),
                        textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                    )
                    FilaVacia(Dimens.salto4)
                    BotonBordeado(stringResource(R.string.ficha_ver_acta), onClick = { onIrActa(protocolo.idVisita) })
                }

                mensaje?.let {
                    FilaVacia(Dimens.salto3)
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                FilaVacia(Dimens.salto9)
            }
        }
    }
}




@Composable
private fun Divisor() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(Dimens.grosorDivisor)
            .background(LavandaBorde())
    )
}




@Composable
private fun DivisorVertical() {
    Box(
        Modifier
            .width(Dimens.grosorDivisor)
            .height(Dimens.altoDivisorColumna)
            .background(LavandaBorde())
    )
}



@Composable
private fun ColumnaValor(clave: String, valor: String, sufijo: String, modifier: Modifier = Modifier) {
    Column(modifier.padding(horizontal = Dimens.salto4)) {
        Text(clave, style = MaterialTheme.typography.labelSmall, color = GrisTexto())
        FilaVacia(Dimens.salto1)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(valor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(Dimens.salto2))
            Text(sufijo, style = MaterialTheme.typography.labelSmall, color = GrisTexto())
        }
    }
}




private data class ColumnaResumen(val clave: String, val valor: String, val sufijo: String)


@Composable
private fun tituloResumen(p: Prueba, equipo: Equipo): String = when (p.tipoPrueba) {
    // el transformador sí tiene devanados; el cable, la celda y el interruptor no
    TipoPrueba.AISLACION -> if (esTransformador(equipo))
        stringResource(
            R.string.ficha_titulo_aislacion_dev, p.elemento,
            Calculos.fmt(p.mediciones["TENSION_PRUEBA"], 0)
        )
    else
        stringResource(
            R.string.ficha_titulo_aislacion, p.elemento,
            Calculos.fmt(p.mediciones["TENSION_PRUEBA"], 0)
        )
    TipoPrueba.TTR ->
        stringResource(R.string.ficha_titulo_ttr, Calculos.fmt(p.mediciones["POSICION_TAP"], 0))
    TipoPrueba.DEVANADOS -> stringResource(R.string.ficha_titulo_devanados, p.elemento)
    TipoPrueba.CONTACTOS -> stringResource(R.string.ficha_titulo_contactos, p.elemento)
    TipoPrueba.OPERACION_RELE -> stringResource(R.string.ficha_titulo_rele, p.elemento)
}




@Composable
private fun columnasResumen(p: Prueba, equipo: Equipo): List<ColumnaResumen> {
    val m = p.mediciones
    val megaohm = stringResource(R.string.unidad_mohm)
    val resultado = p.resultado?.nombre ?: stringResource(R.string.resultado_pendiente)
    return when (p.tipoPrueba) {


        TipoPrueba.AISLACION -> {
            val ip = Calculos.indicePolarizacion(m["LECTURA_1MIN"], m["LECTURA_10MIN"])
            buildList {
                add(ColumnaResumen(stringResource(R.string.ficha_col_1min), Calculos.fmt(m["LECTURA_1MIN"], 0), megaohm))
                if (m["LECTURA_10MIN"] != null)
                    add(ColumnaResumen(stringResource(R.string.ficha_col_10min), Calculos.fmt(m["LECTURA_10MIN"], 0), megaohm))
                add(
                    if (ip != null) ColumnaResumen(stringResource(R.string.ficha_col_ip), Calculos.fmt(ip), resultado)
                    else ColumnaResumen(stringResource(R.string.ficha_col_minimo), "100", megaohm)
                )
            }
        }



        TipoPrueba.TTR -> {
            val error = Calculos.errorTtr(m["RELACION_TEORICA"], m["RELACION_MEDIDA"])
            listOf(
                ColumnaResumen(
                    stringResource(R.string.ficha_col_teorica),
                    Calculos.fmt(m["RELACION_TEORICA"], 2), ""
                ),
                ColumnaResumen(
                    stringResource(R.string.ficha_col_medida),
                    Calculos.fmt(m["RELACION_MEDIDA"], 2), ""
                ),
                ColumnaResumen(
                    stringResource(R.string.ficha_col_error),
                    error?.let { stringResource(R.string.valor_porcentaje, Calculos.fmt(it, 2)) } ?: "—",
                    resultado
                )
            )
        }

        TipoPrueba.DEVANADOS -> listOf(
            ColumnaResumen(
                stringResource(R.string.ficha_col_resistencia),
                Calculos.fmt(m["RESISTENCIA"], 2), stringResource(R.string.unidad_mmohm)
            ),
            ColumnaResumen(stringResource(R.string.ficha_col_resultado), resultado, "")
        )


        TipoPrueba.CONTACTOS -> {
            val pct = Calculos.porcentajeContactos(m["RESISTENCIA_CONTACTO"], equipo.maxContactosUohm)
            val microhm = stringResource(R.string.unidad_umohm)
            listOf(
                ColumnaResumen(
                    stringResource(R.string.ficha_col_contacto),
                    Calculos.fmt(m["RESISTENCIA_CONTACTO"], 1), microhm
                ),
                ColumnaResumen(
                    stringResource(R.string.ficha_col_max_placa),
                    Calculos.fmt(equipo.maxContactosUohm, 0), microhm
                ),
                ColumnaResumen(
                    stringResource(R.string.ficha_col_sobre_max),
                    pct?.let { stringResource(R.string.valor_porcentaje, Calculos.fmt(it, 1)) } ?: "—",
                    resultado
                )
            )
        }


        TipoPrueba.OPERACION_RELE -> {
            val pct = Calculos.porcentajeDiferenciaTiempo(m["TIEMPO_MEDIDO"], m["TIEMPO_ESPERADO"])
            val ms = stringResource(R.string.unidad_ms)
            listOf(
                ColumnaResumen(
                    stringResource(R.string.ficha_col_esperado),
                    Calculos.fmt(m["TIEMPO_ESPERADO"], 0), ms
                ),
                ColumnaResumen(
                    stringResource(R.string.ficha_col_medido),
                    Calculos.fmt(m["TIEMPO_MEDIDO"], 1), ms
                ),
                ColumnaResumen(
                    stringResource(R.string.ficha_col_diferencia),
                    pct?.let { stringResource(R.string.valor_porcentaje, Calculos.fmt(it, 1)) } ?: "—",
                    resultado
                )
            )
        }
    }
}




@Composable
private fun FilaSpec(vararg columnas: Pair<String, String>) {
    Row(Modifier.fillMaxWidth()) {
        columnas.forEach { (clave, valor) ->
            Column(Modifier.weight(1f)) {
                Text(clave, style = MaterialTheme.typography.labelSmall, color = GrisTexto())
                Text(valor, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}



@Composable
private fun pistaPrueba(tipo: TipoPrueba, hechas: List<Prueba>, equipo: Equipo): String {

    val lecturas = stringResource(
        if (equipo.exigeLectura10Min) R.string.ficha_pista_aislacion_dos_lecturas
        else R.string.ficha_pista_aislacion_una_lectura
    )
    return when (tipo) {
        TipoPrueba.AISLACION -> hechas.firstOrNull()?.let {
            stringResource(
                if (esTransformador(equipo)) R.string.ficha_pista_aislacion_dev
                else R.string.ficha_pista_aislacion,
                it.elemento, Calculos.fmt(it.mediciones["TENSION_PRUEBA"], 0), lecturas
            )
        } ?: lecturas
        TipoPrueba.TTR -> stringResource(
            R.string.ficha_pista_ttr,
            hechas.firstOrNull()?.let { Calculos.fmt(it.mediciones["POSICION_TAP"], 0) } ?: "3"
        )
        TipoPrueba.DEVANADOS -> stringResource(R.string.ficha_pista_devanados)
        TipoPrueba.CONTACTOS -> stringResource(R.string.ficha_pista_contactos)
        TipoPrueba.OPERACION_RELE -> stringResource(R.string.ficha_pista_rele)
    }
}


private fun esTransformador(equipo: Equipo) = equipo.tipoEquipo == "Transformador"


@Composable
private fun estadoPrueba(hechas: List<Prueba>, resultado: Resultado?): String = when {
    hechas.isEmpty() -> stringResource(R.string.resultado_pendiente)
    hechas.all { !it.realizada } -> stringResource(R.string.ficha_estado_no_realizada)
    else -> stringResource(
        R.string.ficha_estado_hecha,
        resultado?.nombre?.lowercase() ?: stringResource(R.string.ficha_estado_sin_resultado)
    )
}



@Composable
fun ContraPruebasAnteriores(historial: List<Triple<String, String, Double>>) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            stringResource(R.string.ficha_contra_titulo), Modifier.weight(1f),
            fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall
        )
        TextButton(onClick = { }) {
            Text(
                stringResource(R.string.ficha_contra_ver),
                style = MaterialTheme.typography.labelSmall, color = GrisTexto()
            )
        }
    }
    FilaVacia(Dimens.salto3)
    if (historial.isEmpty()) {
        Text(
            stringResource(R.string.ficha_contra_sin_historial),
            style = MaterialTheme.typography.bodySmall, color = GrisTexto()
        )
        return
    }
    Row(Modifier.fillMaxWidth().padding(vertical = Dimens.salto3)) {
        Text(
            stringResource(R.string.ficha_anio), Modifier.weight(1f),
            style = MaterialTheme.typography.labelSmall, color = GrisTexto()
        )
        Text(
            stringResource(R.string.ficha_contra_col_1min), Modifier.weight(1f),
            style = MaterialTheme.typography.labelSmall, color = GrisTexto()
        )
        Text(
            stringResource(R.string.ficha_contra_col_variacion), Modifier.weight(1f),
            style = MaterialTheme.typography.labelSmall, color = GrisTexto(), textAlign = TextAlign.End
        )
    }
    Divisor()
    var anterior: Double? = null
    var algunaSignificativa = false
    historial.forEachIndexed { indice, (_, fecha, valor) ->
        val varPct = if (indice == 0) null else Calculos.variacionPct(valor, anterior)
        val significativa = Calculos.esVariacionSignificativa(varPct)
        if (significativa) algunaSignificativa = true
        Row(Modifier.fillMaxWidth().padding(vertical = Dimens.salto4)) {
            Text(fecha.take(4), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(Calculos.fmt(valor, 0), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text(
                varPct?.let { stringResource(R.string.valor_porcentaje, Calculos.fmt(it, 1)) } ?: "-",
                Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = if (significativa) RojoRechazado() else Color.Unspecified,
                fontWeight = if (significativa) FontWeight.SemiBold else FontWeight.Normal,
                textAlign = TextAlign.End
            )
        }
        if (indice < historial.lastIndex) Divisor()
        anterior = valor
    }
    if (algunaSignificativa) {
        FilaVacia(Dimens.salto4)
        Text(
            stringResource(R.string.alerta_variacion),
            style = MaterialTheme.typography.bodySmall, color = RojoRechazado(),
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
        )
    }
}




private val UsuarioPreview = Usuario(
    idUsuario = 7, idRol = 1, rut = "17.284.519-3", nombre = "Matías Godoy Herrera",
    correo = "mgodoy@induztek.ejemplo", rol = Rol.INGENIERO
)

private val TransformadorPreview = Equipo(
    idEquipo = 1, idInstalacion = 1, tag = "TR-01", idTipoEquipo = 1, tipoEquipo = "Transformador",
    clasificacion = "PRIMARIO", marca = "Rhona", modelo = "RH-18733", numeroSerie = "RH-18733",
    anioFabricacion = 2012, potenciaKva = 1500.0, tensionPrimariaKv = 12.0, tensionSecundariaKv = 0.4,
    corrienteNominalA = null, maxContactosUohm = null, observaciones = "", instalacionNombre = "Planta Lampa"
)

private val CeldaPreview = Equipo(
    idEquipo = 4, idInstalacion = 4, tag = "CEL-MT-03", idTipoEquipo = 3, tipoEquipo = "Celda MT",
    clasificacion = "PRIMARIO", marca = "Ormazabal", modelo = "OR-77812", numeroSerie = "OR-77812",
    anioFabricacion = 2018, potenciaKva = null, tensionPrimariaKv = 12.0, tensionSecundariaKv = null,
    corrienteNominalA = 630.0, maxContactosUohm = 60.0, observaciones = "", instalacionNombre = "Edificio Clínico"
)

private fun protocoloPreview(estado: EstadoProtocolo) = Protocolo(
    idProtocolo = 501, numero = "PT-2026-0143", idVisita = 26, idEquipo = 1, idUsuarioRegistra = 7,
    fechaHoraPrueba = "2026-09-26 10:30", temperaturaC = 21.0, humedadPct = 48.0, estado = estado
)

private fun aislacion(valor1Min: Double, valor10Min: Double?, resultado: Resultado) = Prueba(
    idPrueba = 900, idProtocolo = 501, tipoPrueba = TipoPrueba.AISLACION, elemento = "AT-T",
    realizada = true, motivoNoRealizada = "", idInstrumento = 1, resultado = resultado, comentario = "",
    mediciones = buildMap {
        put("TENSION_PRUEBA", 5.0); put("LECTURA_1MIN", valor1Min)
        valor10Min?.let { put("LECTURA_10MIN", it) }
    }
)




@Preview(showBackground = true, showSystemUi = true, name = "TR-01 en curso")
@Composable
private fun FichaEquipoPreview() {
    InduztekTheme {
        EquipoContent(
            equipo = TransformadorPreview, usuario = UsuarioPreview,
            protocolo = protocoloPreview(EstadoProtocolo.BORRADOR),
            pruebas = listOf(
                aislacion(12500.0, 31250.0, Resultado.APROBADO),
                Prueba(
                    idPrueba = 901, idProtocolo = 501, tipoPrueba = TipoPrueba.TTR, elemento = "3 fases / tap 3",
                    realizada = true, motivoNoRealizada = "", idInstrumento = 3, resultado = Resultado.OBSERVADO,
                    comentario = "",
                    mediciones = mapOf("POSICION_TAP" to 3.0, "RELACION_TEORICA" to 30.0, "RELACION_MEDIDA" to 30.62)
                )
            ),
            tipos = listOf(TipoPrueba.AISLACION, TipoPrueba.TTR, TipoPrueba.DEVANADOS),
            temp = "21", humedad = "48", onTemp = {}, onHumedad = {},
            historial = listOf(
                Triple("PT-2025-0288", "2025-09-13", 9800.0),
                Triple("PT-2024-0312", "2024-09-14", 12500.0)
            ),
            mensaje = null, motivo = null, onMotivo = {}, onAbrirMotivo = {}, onCerrarMotivo = {},
            fechaCalibracion = { "2027-03-15" }, visitaAbierta = { true }, ultimoMotivo = { "" },
            onAtras = {}, onRegistrarPrueba = {}, onIrActa = {},
            onMarcarRegistrado = { _, _, _ -> }, onFormalizar = {}, onDevolver = { _, _ -> }
        )
    }
}




@Preview(showBackground = true, showSystemUi = true, name = "Celda formalizada")
@Composable
private fun FichaCeldaPreview() {
    InduztekTheme {
        EquipoContent(
            equipo = CeldaPreview, usuario = UsuarioPreview,
            protocolo = protocoloPreview(EstadoProtocolo.FORMALIZADO),
            pruebas = listOf(
                aislacion(3100.0, null, Resultado.APROBADO),
                Prueba(
                    idPrueba = 903, idProtocolo = 501, tipoPrueba = TipoPrueba.CONTACTOS, elemento = "Polo 3",
                    realizada = true, motivoNoRealizada = "", idInstrumento = 4, resultado = Resultado.OBSERVADO,
                    comentario = "", mediciones = mapOf("RESISTENCIA_CONTACTO" to 67.0)
                )
            ),
            tipos = listOf(TipoPrueba.AISLACION, TipoPrueba.CONTACTOS),
            temp = "20", humedad = "47", onTemp = {}, onHumedad = {},
            historial = listOf(Triple("PT-2025-0292", "2025-09-02", 3100.0)),
            mensaje = null, motivo = null, onMotivo = {}, onAbrirMotivo = {}, onCerrarMotivo = {},
            fechaCalibracion = { "2027-08-20" }, visitaAbierta = { false }, ultimoMotivo = { "" },
            onAtras = {}, onRegistrarPrueba = {}, onIrActa = {},
            onMarcarRegistrado = { _, _, _ -> }, onFormalizar = {}, onDevolver = { _, _ -> }
        )
    }
}