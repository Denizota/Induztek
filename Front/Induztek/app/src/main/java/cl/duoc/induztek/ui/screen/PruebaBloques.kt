
package cl.duoc.induztek.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import cl.duoc.induztek.R
import cl.duoc.induztek.model.Calculos
import cl.duoc.induztek.model.Equipo
import cl.duoc.induztek.model.Resultado
import cl.duoc.induztek.model.TipoPrueba
import cl.duoc.induztek.ui.components.CajaBordeada
import cl.duoc.induztek.ui.components.CampoNumero
import cl.duoc.induztek.ui.components.CampoNumeroSugerido
import cl.duoc.induztek.ui.components.CampoSeleccion
import cl.duoc.induztek.ui.components.ControlSegmentado
import cl.duoc.induztek.ui.components.EtiquetaCampo
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.TextoSecundario
import cl.duoc.induztek.ui.theme.NaranjaObservado
import cl.duoc.induztek.ui.theme.RojoRechazado
import cl.duoc.induztek.ui.theme.VerdeAprobado
import cl.duoc.induztek.viewmodel.aMega
import cl.duoc.induztek.viewmodel.formatear

@Composable
internal fun BloqueAislacion(
    equipo: Equipo, elemento: String, opciones: List<String>, onElemento: (String) -> Unit,
    texto: (String) -> String, actualizarValor: (String, String) -> Unit,
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
            texto("TENSION_PRUEBA"), { actualizarValor("TENSION_PRUEBA", it) },
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
            texto("LECTURA_1MIN"), { actualizarValor("LECTURA_1MIN", it) },
            stringResource(R.string.form_lectura_1min), sugeridas, Modifier.weight(1f), unidad
        )
        if (con10) CampoNumeroSugerido(
            texto("LECTURA_10MIN"), { actualizarValor("LECTURA_10MIN", it) },
            stringResource(R.string.form_lectura_10min), sugeridas, Modifier.weight(1f), unidad
        )
    }
    Text(
        stringResource(
            if (con10) R.string.form_rango_esperado else R.string.form_rechazo_minimo
        ),
        style = MaterialTheme.typography.bodySmall, color = TextoSecundario()
    )

    val lecturaUnoMinuto = aMega(texto("LECTURA_1MIN").toDoubleOrNull(), unidad)
    val lecturaDiezMinutos = if (con10) aMega(texto("LECTURA_10MIN").toDoubleOrNull(), unidad) else null
    val indicePolarizacion = Calculos.indicePolarizacion(lecturaUnoMinuto, lecturaDiezMinutos)

    val resultado = when {
        con10 && lecturaDiezMinutos == null -> null
        else -> Calculos.resultadoAislacion(indicePolarizacion, lecturaUnoMinuto)
    }
    val anterior = if (lecturaUnoMinuto != null) {
        valorAnterior(equipo.idEquipo, TipoPrueba.AISLACION, "LECTURA_1MIN", idProtocolo)
    } else {
        null
    }

    if (con10) {
        FilaCalculo(
            stringResource(R.string.form_ip_calculado), Calculos.formatear(indicePolarizacion),
            stringResource(R.string.form_ip_detalle, Calculos.formatear(lecturaDiezMinutos, 0), Calculos.formatear(lecturaUnoMinuto, 0)),
            resultado
        )
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f))
            CajaResultado(resultado)
        }
    }
    ComparacionAnterior(lecturaUnoMinuto, anterior, stringResource(R.string.unidad_mohm))
    if (Calculos.esVariacionSignificativa(Calculos.variacionPct(lecturaUnoMinuto, anterior))) {
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
            Text(detalle, style = MaterialTheme.typography.bodySmall, color = TextoSecundario())
        }
        Text("›", color = TextoSecundario(), fontWeight = FontWeight.Bold, maxLines = 1,
            modifier = Modifier.clearAndSetSemantics { })
        CajaBordeada(Modifier.weight(2f).fillMaxHeight(), centrado = true) { CajaResultado(resultado) }
    }
}

@Composable
private fun CajaResultado(resultado: Resultado?) {
    var color = TextoSecundario()
    if (resultado == Resultado.APROBADO) color = VerdeAprobado()
    if (resultado == Resultado.OBSERVADO) color = NaranjaObservado()
    if (resultado == Resultado.RECHAZADO) color = RojoRechazado()

    var textoResultado = stringResource(R.string.resultado_pendiente)
    if (resultado != null) textoResultado = resultado.nombre
    Text(
        textoResultado,
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
    val linea = if (anterior == null) {
        stringResource(R.string.form_sin_anterior)
    } else {
        stringResource(
            R.string.form_anterior,
            Calculos.formatear(anterior, decimales), unidad,
            Calculos.formatear(Calculos.variacionPct(actual, anterior), 1)
        )
    }
    Text(
        linea,
        style = MaterialTheme.typography.bodySmall, color = TextoSecundario()
    )
}

@Composable
internal fun BloqueTtr(
    equipo: Equipo, texto: (String) -> String, actualizarValor: (String, String) -> Unit,
    valorAnterior: (Int, TipoPrueba, String, Int) -> Double?, idProtocolo: Int
) {
    if (texto("RELACION_TEORICA").isBlank() && equipo.tensionPrimariaKv != null) {
        actualizarValor("RELACION_TEORICA", relacionTeoricaPlaca(equipo))
    }
    val medida = texto("RELACION_MEDIDA").toDoubleOrNull()
    val error = Calculos.errorTtr(
        texto("RELACION_TEORICA").toDoubleOrNull(), medida
    )
    val anterior = if (medida != null) {
        valorAnterior(equipo.idEquipo, TipoPrueba.TTR, "RELACION_MEDIDA", idProtocolo)
    } else {
        null
    }
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioEntreCampos)) {
        CampoNumero(
            texto("POSICION_TAP"), { actualizarValor("POSICION_TAP", it) },
            stringResource(R.string.form_posicion_tap), modifier = Modifier.weight(1f)
        )
        CampoNumero(
            texto("RELACION_TEORICA"), { actualizarValor("RELACION_TEORICA", it) },
            stringResource(R.string.form_relacion_teorica), "—", Modifier.weight(1f)
        )
    }
    CampoNumero(
        texto("RELACION_MEDIDA"), { actualizarValor("RELACION_MEDIDA", it) },
        stringResource(R.string.form_relacion_medida), "—"
    )
    val textoError = if (error != null) {
        stringResource(R.string.valor_porcentaje, Calculos.formatear(error))
    } else {
        "—"
    }
    FilaCalculo(
        stringResource(R.string.form_error_calculado),
        textoError,
        stringResource(R.string.form_criterio_ttr),
        Calculos.resultadoTtr(error)
    )
    ComparacionAnterior(medida, anterior, "—", decimales = 2)
    if (Calculos.esVariacionSignificativa(Calculos.variacionPct(medida, anterior))) NotaVariacion()
}

@Composable
internal fun BloqueDevanados(
    equipo: Equipo, elemento: String, texto: (String) -> String, actualizarValor: (String, String) -> Unit,
    resultadoManual: String, onManual: (String) -> Unit,
    valorAnterior: (Int, TipoPrueba, String, Int) -> Double?, idProtocolo: Int
) {
    val resistencia = texto("RESISTENCIA").toDoubleOrNull()
    val anterior = if (resistencia != null) {
        valorAnterior(equipo.idEquipo, TipoPrueba.DEVANADOS, "RESISTENCIA", idProtocolo)
    } else {
        null
    }
    CampoNumero(
        texto("RESISTENCIA"), { actualizarValor("RESISTENCIA", it) },
        stringResource(R.string.form_resistencia_fase), stringResource(R.string.unidad_mmohm)
    )
    val nombresResultado = mutableListOf<String>()
    for (posible in Resultado.values()) {
        nombresResultado.add(posible.nombre)
    }
    CampoSeleccion(
        valor = resultadoManual,
        opciones = nombresResultado,
        onSeleccionar = onManual,
        etiqueta = stringResource(R.string.form_resultado_manual),
        textoSugerido = stringResource(R.string.placeholder_seleccion)
    )
    Text(
        stringResource(R.string.form_sin_criterio),
        style = MaterialTheme.typography.bodySmall, color = TextoSecundario()
    )
    ComparacionAnterior(resistencia, anterior, stringResource(R.string.unidad_mmohm), decimales = 2)
    if (Calculos.esVariacionSignificativa(Calculos.variacionPct(resistencia, anterior))) NotaVariacion()
}

@Composable
internal fun BloqueContactos(
    equipo: Equipo, texto: (String) -> String, actualizarValor: (String, String) -> Unit,
    valorAnterior: (Int, TipoPrueba, String, Int) -> Double?, idProtocolo: Int
) {
    val valor = texto("RESISTENCIA_CONTACTO").toDoubleOrNull()
    val porcentaje = Calculos.porcentajeContactos(valor, equipo.maxContactosUohm)
    val anterior = if (valor != null) {
        valorAnterior(equipo.idEquipo, TipoPrueba.CONTACTOS, "RESISTENCIA_CONTACTO", idProtocolo)
    } else {
        null
    }
    CampoNumero(
        texto("RESISTENCIA_CONTACTO"), { actualizarValor("RESISTENCIA_CONTACTO", it) },
        stringResource(R.string.form_resistencia_polo), stringResource(R.string.unidad_umohm)
    )
    val textoPorcentaje = if (porcentaje != null) {
        stringResource(R.string.valor_porcentaje, Calculos.formatear(porcentaje, 1))
    } else {
        "—"
    }
    FilaCalculo(
        stringResource(R.string.form_pct_placa_calculado),
        textoPorcentaje,
        stringResource(R.string.form_maximo_placa, Calculos.formatear(equipo.maxContactosUohm, 0)),
        Calculos.resultadoContactos(porcentaje)
    )
    ComparacionAnterior(valor, anterior, stringResource(R.string.unidad_umohm), decimales = 1)
    if (Calculos.esVariacionSignificativa(Calculos.variacionPct(valor, anterior))) NotaVariacion()
}

@Composable
internal fun BloqueRele(
    equipo: Equipo, texto: (String) -> String, actualizarValor: (String, String) -> Unit,
    opera: Boolean, onOpera: (Boolean) -> Unit,
    valorAnterior: (Int, TipoPrueba, String, Int) -> Double?, idProtocolo: Int
) {
    val medido = texto("TIEMPO_MEDIDO").toDoubleOrNull()
    val porcentaje = Calculos.porcentajeDiferenciaTiempo(medido, texto("TIEMPO_ESPERADO").toDoubleOrNull())
    val anterior = if (medido != null) {
        valorAnterior(equipo.idEquipo, TipoPrueba.OPERACION_RELE, "TIEMPO_MEDIDO", idProtocolo)
    } else {
        null
    }
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioEntreCampos)) {
        CampoNumero(
            texto("CORRIENTE_AJUSTE"), { actualizarValor("CORRIENTE_AJUSTE", it) },
            stringResource(R.string.form_ajuste_corriente), stringResource(R.string.unidad_amper),
            Modifier.weight(1f)
        )
        CampoNumero(
            texto("CORRIENTE_OPERACION"), { actualizarValor("CORRIENTE_OPERACION", it) },
            stringResource(R.string.form_corriente_operacion), stringResource(R.string.unidad_amper),
            Modifier.weight(1f)
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioEntreCampos)) {
        CampoNumero(
            texto("TIEMPO_ESPERADO"), { actualizarValor("TIEMPO_ESPERADO", it) },
            stringResource(R.string.form_tiempo_esperado), stringResource(R.string.unidad_ms),
            Modifier.weight(1f)
        )
        CampoNumero(
            texto("TIEMPO_MEDIDO"), { actualizarValor("TIEMPO_MEDIDO", it) },
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
    val textoDiferencia = if (porcentaje != null) {
        stringResource(R.string.valor_porcentaje, Calculos.formatear(porcentaje, 1))
    } else {
        "—"
    }
    FilaCalculo(
        stringResource(R.string.form_diferencia_calculada),
        textoDiferencia,
        stringResource(R.string.form_criterio_rele),
        Calculos.resultadoRele(opera, porcentaje)
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
