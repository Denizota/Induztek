
package cl.duoc.induztek.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import cl.duoc.induztek.R
import cl.duoc.induztek.model.Calculos
import cl.duoc.induztek.model.Prueba
import cl.duoc.induztek.model.Resultado
import cl.duoc.induztek.model.TipoPrueba
import cl.duoc.induztek.ui.components.CirculoPrueba
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.FondoFilaTabla
import cl.duoc.induztek.ui.theme.TextoSecundario
import cl.duoc.induztek.ui.theme.NaranjaObservado
import cl.duoc.induztek.ui.theme.RojoRechazado
import cl.duoc.induztek.ui.theme.VerdeAprobado


@Composable
internal fun ColumnaDato(clave: String, valor: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(clave, style = MaterialTheme.typography.labelSmall, color = TextoSecundario())
        Text(valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun FilaActa(
    numero: Int, titulo: String, detalle: String, estado: String,
    resultado: Resultado? = null, noRealizada: Boolean = false
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(FondoFilaTabla(), RoundedCornerShape(Dimens.radioSegmento))
            .padding(horizontal = Dimens.salto3, vertical = Dimens.salto2)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically
    ) {
        CirculoPrueba(numero, resultado, noRealizada)
        Spacer(Modifier.width(Dimens.salto3))
        Column(Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            Text(detalle, style = MaterialTheme.typography.labelSmall, color = TextoSecundario())
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
internal fun colorResultado(resultado: Resultado?): Color {
    if (resultado == Resultado.APROBADO) {
        return VerdeAprobado()
    }
    if (resultado == Resultado.OBSERVADO) {
        return NaranjaObservado()
    }
    if (resultado == Resultado.RECHAZADO) {
        return RojoRechazado()
    }
    return TextoSecundario()
}

@Composable
internal fun estadoPrueba(prueba: Prueba): String {
    if (!prueba.realizada) {
        return stringResource(R.string.acta_registrada)
    }
    val resultado = prueba.resultado
    if (resultado == null) {
        return stringResource(R.string.acta_registrada)
    }
    return resultado.nombre
}

@Composable
internal fun detallePrueba(prueba: Prueba): String {
    if (!prueba.realizada) {
        var motivo = prueba.motivoNoRealizada
        if (motivo.isBlank()) {
            motivo = stringResource(R.string.acta_sin_motivo)
        }
        return stringResource(R.string.acta_no_realizada, motivo)
    }
    return resumenCorto(prueba.tipoPrueba, prueba.elemento, prueba.mediciones)
}

internal fun limpiarRut(entrada: String): String {
    var limpio = ""
    for (letra in entrada) {
        if (letra.isDigit() || letra.equals('K', ignoreCase = true)) {
            limpio = limpio + letra
        }
    }
    limpio = limpio.uppercase()
    return limpio.take(9)
}

internal object TransformacionRut : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val limpio = text.text
        val conDigitoVerificador = limpio.length == 9 || limpio.endsWith("K")

        var cuerpoOriginal = limpio
        if (conDigitoVerificador) {
            cuerpoOriginal = ""
            for (indice in 0 until limpio.length - 1) {
                cuerpoOriginal = cuerpoOriginal + limpio[indice]
            }
        }
        var cuerpo = ""
        for (letra in cuerpoOriginal) {
            if (letra.isDigit()) {
                cuerpo = cuerpo + letra
            }
        }
        var digitoVerificador = ""
        if (conDigitoVerificador) {
            digitoVerificador = limpio[limpio.length - 1].toString()
        }

        val pares = mutableListOf<Pair<Char, Int?>>()
        var restante = cuerpo.length % 3
        if (restante == 0) {
            restante = 3
        }
        for (i in cuerpo.indices) {
            if (restante == 0) {
                pares.add('.' to null)
                restante = 3
            }
            pares.add(cuerpo[i] to i)
            restante--
        }
        if (digitoVerificador.isNotEmpty()) {
            pares.add('-' to null)
            pares.add(digitoVerificador[0] to cuerpo.length)
        }

        var textoVisual = ""
        for (par in pares) {
            textoVisual = textoVisual + par.first
        }

        val mapaDelCursor = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                for (i in pares.indices) {
                    val posicionOriginal = pares[i].second
                    if (posicionOriginal != null && posicionOriginal >= offset) {
                        return i
                    }
                }
                return pares.size
            }

            override fun transformedToOriginal(offset: Int): Int {
                var cuenta = 0
                val limite = minOf(offset, pares.size)
                for (i in 0 until limite) {
                    if (pares[i].second != null) {
                        cuenta++
                    }
                }
                return cuenta
            }
        }
        return TransformedText(AnnotatedString(textoVisual), mapaDelCursor)
    }
}

internal fun String.horaCorta(): String {
    val partes = split(" ")

    var dia = ""
    val fecha = partes[0].split("-")
    if (fecha.size == 3) {
        dia = "${fecha[2]}-${fecha[1]}"
    }

    var hora = ""
    if (partes.size > 1) {
        val trozosHora = partes[1].split(":")
        hora = trozosHora[0]
        if (trozosHora.size > 1) {
            hora = hora + ":" + trozosHora[1]
        }
    }

    if (dia.isEmpty()) {
        return hora
    }
    if (hora.isEmpty()) {
        return dia
    }
    return dia + " " + hora
}

@Composable
private fun resumenCorto(tipo: TipoPrueba, elemento: String, mediciones: Map<String, Double>): String {
    if (tipo == TipoPrueba.AISLACION) {
        val indicePolarizacion = Calculos.indicePolarizacion(mediciones["LECTURA_1MIN"], mediciones["LECTURA_10MIN"])
        return stringResource(
            R.string.acta_det_aislacion, elemento,
            Calculos.formatear(mediciones["LECTURA_1MIN"], 0), Calculos.formatear(mediciones["LECTURA_10MIN"], 0), Calculos.formatear(indicePolarizacion)
        )
    }
    if (tipo == TipoPrueba.TTR) {
        val error = Calculos.errorTtr(mediciones["RELACION_TEORICA"], mediciones["RELACION_MEDIDA"])
        return stringResource(
            R.string.acta_det_ttr,
            Calculos.formatear(mediciones["POSICION_TAP"], 0),
            Calculos.formatear(mediciones["RELACION_TEORICA"]),
            Calculos.formatear(mediciones["RELACION_MEDIDA"]),
            Calculos.formatear(error)
        )
    }
    if (tipo == TipoPrueba.DEVANADOS) {
        return stringResource(R.string.acta_det_devanados, elemento, Calculos.formatear(mediciones["RESISTENCIA"]))
    }
    if (tipo == TipoPrueba.CONTACTOS) {
        return stringResource(R.string.acta_det_contactos, elemento, Calculos.formatear(mediciones["RESISTENCIA_CONTACTO"], 1))
    }
    return stringResource(
        R.string.acta_det_rele, elemento,
        Calculos.formatear(mediciones["TIEMPO_MEDIDO"], 1), Calculos.formatear(mediciones["TIEMPO_ESPERADO"], 1),
        Calculos.formatear(mediciones["CORRIENTE_OPERACION"])
    )
}
