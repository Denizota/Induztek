
package cl.duoc.induztek.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import cl.duoc.induztek.R
import cl.duoc.induztek.model.Calculos
import cl.duoc.induztek.model.Equipo
import cl.duoc.induztek.model.Prueba
import cl.duoc.induztek.model.Resultado
import cl.duoc.induztek.model.TipoPrueba
import cl.duoc.induztek.ui.components.FilaVacia
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.TextoSecundario
import cl.duoc.induztek.ui.theme.BordeSuave


@Composable
internal fun Divisor() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(Dimens.grosorDivisor)
            .background(BordeSuave())
    )
}

@Composable
internal fun DivisorVertical() {
    Box(
        Modifier
            .width(Dimens.grosorDivisor)
            .height(Dimens.altoDivisorColumna)
            .background(BordeSuave())
    )
}

@Composable
internal fun ColumnaValor(clave: String, valor: String, sufijo: String, modifier: Modifier = Modifier) {
    Column(modifier.padding(horizontal = Dimens.salto4)) {
        Text(clave, style = MaterialTheme.typography.labelSmall, color = TextoSecundario())
        FilaVacia(Dimens.salto1)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(valor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(Dimens.salto2))
            Text(sufijo, style = MaterialTheme.typography.labelSmall, color = TextoSecundario())
        }
    }
}

internal data class ColumnaResumen(val clave: String, val valor: String, val sufijo: String)

@Composable
internal fun tituloResumen(prueba: Prueba, equipo: Equipo): String {
    if (prueba.tipoPrueba == TipoPrueba.AISLACION) {
        val tension = Calculos.formatear(prueba.mediciones["TENSION_PRUEBA"], 0)
        if (esTransformador(equipo)) {
            return stringResource(R.string.ficha_titulo_aislacion_dev, prueba.elemento, tension)
        }
        return stringResource(R.string.ficha_titulo_aislacion, prueba.elemento, tension)
    }
    if (prueba.tipoPrueba == TipoPrueba.TTR) {
        return stringResource(R.string.ficha_titulo_ttr, Calculos.formatear(prueba.mediciones["POSICION_TAP"], 0))
    }
    if (prueba.tipoPrueba == TipoPrueba.DEVANADOS) {
        return stringResource(R.string.ficha_titulo_devanados, prueba.elemento)
    }
    if (prueba.tipoPrueba == TipoPrueba.CONTACTOS) {
        return stringResource(R.string.ficha_titulo_contactos, prueba.elemento)
    }
    return stringResource(R.string.ficha_titulo_rele, prueba.elemento)
}

@Composable
internal fun columnasResumen(prueba: Prueba, equipo: Equipo): List<ColumnaResumen> {
    val mediciones = prueba.mediciones
    val megaohm = stringResource(R.string.unidad_mohm)

    var textoResultado = stringResource(R.string.resultado_pendiente)
    if (prueba.resultado != null) {
        textoResultado = prueba.resultado.nombre
    }

    if (prueba.tipoPrueba == TipoPrueba.AISLACION) {
        val indicePolarizacion = Calculos.indicePolarizacion(mediciones["LECTURA_1MIN"], mediciones["LECTURA_10MIN"])
        val columnas = mutableListOf<ColumnaResumen>()
        columnas.add(
            ColumnaResumen(
                stringResource(R.string.ficha_col_1min),
                Calculos.formatear(mediciones["LECTURA_1MIN"], 0), megaohm
            )
        )
        if (mediciones["LECTURA_10MIN"] != null) {
            columnas.add(
                ColumnaResumen(
                    stringResource(R.string.ficha_col_10min),
                    Calculos.formatear(mediciones["LECTURA_10MIN"], 0), megaohm
                )
            )
        }
        if (indicePolarizacion != null) {
            columnas.add(
                ColumnaResumen(
                    stringResource(R.string.ficha_col_ip),
                    Calculos.formatear(indicePolarizacion), textoResultado
                )
            )
        } else {
            columnas.add(ColumnaResumen(stringResource(R.string.ficha_col_minimo), "100", megaohm))
        }
        return columnas
    }

    if (prueba.tipoPrueba == TipoPrueba.TTR) {
        val error = Calculos.errorTtr(mediciones["RELACION_TEORICA"], mediciones["RELACION_MEDIDA"])
        var textoError = "—"
        if (error != null) {
            textoError = stringResource(R.string.valor_porcentaje, Calculos.formatear(error, 2))
        }
        return listOf(
            ColumnaResumen(
                stringResource(R.string.ficha_col_teorica),
                Calculos.formatear(mediciones["RELACION_TEORICA"], 2), ""
            ),
            ColumnaResumen(
                stringResource(R.string.ficha_col_medida),
                Calculos.formatear(mediciones["RELACION_MEDIDA"], 2), ""
            ),
            ColumnaResumen(stringResource(R.string.ficha_col_error), textoError, textoResultado)
        )
    }

    if (prueba.tipoPrueba == TipoPrueba.DEVANADOS) {
        return listOf(
            ColumnaResumen(
                stringResource(R.string.ficha_col_resistencia),
                Calculos.formatear(mediciones["RESISTENCIA"], 2), stringResource(R.string.unidad_mmohm)
            ),
            ColumnaResumen(stringResource(R.string.ficha_col_resultado), textoResultado, "")
        )
    }

    if (prueba.tipoPrueba == TipoPrueba.CONTACTOS) {
        val porcentaje = Calculos.porcentajeContactos(mediciones["RESISTENCIA_CONTACTO"], equipo.maxContactosUohm)
        val microhm = stringResource(R.string.unidad_umohm)
        var textoSobreMax = "—"
        if (porcentaje != null) {
            textoSobreMax = stringResource(R.string.valor_porcentaje, Calculos.formatear(porcentaje, 1))
        }
        return listOf(
            ColumnaResumen(
                stringResource(R.string.ficha_col_contacto),
                Calculos.formatear(mediciones["RESISTENCIA_CONTACTO"], 1), microhm
            ),
            ColumnaResumen(
                stringResource(R.string.ficha_col_max_placa),
                Calculos.formatear(equipo.maxContactosUohm, 0), microhm
            ),
            ColumnaResumen(stringResource(R.string.ficha_col_sobre_max), textoSobreMax, textoResultado)
        )
    }

    val porcentaje = Calculos.porcentajeDiferenciaTiempo(mediciones["TIEMPO_MEDIDO"], mediciones["TIEMPO_ESPERADO"])
    val ms = stringResource(R.string.unidad_ms)
    var textoDiferencia = "—"
    if (porcentaje != null) {
        textoDiferencia = stringResource(R.string.valor_porcentaje, Calculos.formatear(porcentaje, 1))
    }
    return listOf(
        ColumnaResumen(
            stringResource(R.string.ficha_col_esperado),
            Calculos.formatear(mediciones["TIEMPO_ESPERADO"], 0), ms
        ),
        ColumnaResumen(
            stringResource(R.string.ficha_col_medido),
            Calculos.formatear(mediciones["TIEMPO_MEDIDO"], 1), ms
        ),
        ColumnaResumen(stringResource(R.string.ficha_col_diferencia), textoDiferencia, textoResultado)
    )
}

@Composable
internal fun FilaSpec(vararg columnas: Pair<String, String>) {
    Row(Modifier.fillMaxWidth()) {
        for ((clave, valor) in columnas) {
            Column(Modifier.weight(1f)) {
                Text(clave, style = MaterialTheme.typography.labelSmall, color = TextoSecundario())
                Text(valor, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
internal fun pistaPrueba(tipo: TipoPrueba, hechas: List<Prueba>, equipo: Equipo): String {

    val lecturas = stringResource(
        if (equipo.exigeLectura10Min) R.string.ficha_pista_aislacion_dos_lecturas
        else R.string.ficha_pista_aislacion_una_lectura
    )

    if (tipo == TipoPrueba.AISLACION) {
        if (hechas.isEmpty()) {
            return lecturas
        }
        val primera = hechas[0]
        return stringResource(
            if (esTransformador(equipo)) R.string.ficha_pista_aislacion_dev
            else R.string.ficha_pista_aislacion,
            primera.elemento, Calculos.formatear(primera.mediciones["TENSION_PRUEBA"], 0), lecturas
        )
    }

    if (tipo == TipoPrueba.TTR) {
        var posicionTap = "3"
        if (hechas.isNotEmpty()) {
            posicionTap = Calculos.formatear(hechas[0].mediciones["POSICION_TAP"], 0)
        }
        return stringResource(R.string.ficha_pista_ttr, posicionTap)
    }

    if (tipo == TipoPrueba.DEVANADOS) {
        return stringResource(R.string.ficha_pista_devanados)
    }
    if (tipo == TipoPrueba.CONTACTOS) {
        return stringResource(R.string.ficha_pista_contactos)
    }
    return stringResource(R.string.ficha_pista_rele)
}

internal fun esTransformador(equipo: Equipo) = equipo.tipoEquipo == "Transformador"

@Composable
internal fun estadoPrueba(hechas: List<Prueba>, resultado: Resultado?): String {
    if (hechas.isEmpty()) {
        return stringResource(R.string.resultado_pendiente)
    }

    var todasSinHacer = true
    for (prueba in hechas) {
        if (prueba.realizada) {
            todasSinHacer = false
        }
    }
    if (todasSinHacer) {
        return stringResource(R.string.ficha_estado_no_realizada)
    }

    var nombreResultado = stringResource(R.string.ficha_estado_sin_resultado)
    if (resultado != null) {
        nombreResultado = resultado.nombre.lowercase()
    }
    return stringResource(R.string.ficha_estado_hecha, nombreResultado)
}
