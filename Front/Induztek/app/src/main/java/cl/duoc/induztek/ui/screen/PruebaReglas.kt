
package cl.duoc.induztek.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import cl.duoc.induztek.R
import cl.duoc.induztek.model.Equipo
import cl.duoc.induztek.model.TipoPrueba
import cl.duoc.induztek.viewmodel.aMega
import cl.duoc.induztek.viewmodel.formatear
import android.content.Context


internal fun nombrePestana(contexto: Context, tipo: TipoPrueba): String {
    if (tipo == TipoPrueba.AISLACION) {
        return contexto.getString(R.string.pestana_aislacion)
    }
    if (tipo == TipoPrueba.TTR) {
        return contexto.getString(R.string.pestana_relacion)
    }
    if (tipo == TipoPrueba.DEVANADOS) {
        return contexto.getString(R.string.pestana_devanados)
    }
    if (tipo == TipoPrueba.CONTACTOS) {
        return contexto.getString(R.string.pestana_contactos)
    }
    return contexto.getString(R.string.pestana_rele)
}

@Composable
internal fun etiquetaElemento(tipo: TipoPrueba): String {
    if (tipo == TipoPrueba.AISLACION) {
        return stringResource(R.string.elem_devanado)
    }
    if (tipo == TipoPrueba.DEVANADOS) {
        return stringResource(R.string.elem_fase)
    }
    if (tipo == TipoPrueba.CONTACTOS) {
        return stringResource(R.string.elem_polo)
    }
    if (tipo == TipoPrueba.OPERACION_RELE) {
        return stringResource(R.string.elem_elemento_prueba)
    }
    return stringResource(R.string.elem_conexion)
}

internal fun reescalar(
    tipo: TipoPrueba, elemento: String, estado: MutableMap<String, String>, anterior: String, nueva: String
): String {
    if (anterior != nueva) {
        for (parametro in listOf("LECTURA_1MIN", "LECTURA_10MIN")) {
            val clave = "$elemento|${tipo.name}|$parametro"
            var guardado: Double? = null
            val escrito = estado[clave]
            if (escrito != null) guardado = escrito.toDoubleOrNull()
            val enMega = aMega(guardado, anterior)
            if (enMega != null) {
                estado[clave] = formatear(fromMega(enMega, nueva))
            }
        }
    }
    return nueva
}

private fun fromMega(valorMega: Double, unidad: String): Double {
    if (unidad == "kΩ") {
        return valorMega * 1000
    }
    if (unidad == "GΩ") {
        return valorMega / 1000
    }
    return valorMega
}

internal fun elementosPosibles(equipo: Equipo, tipo: TipoPrueba): List<String> {
    if (tipo == TipoPrueba.AISLACION) {
        if (equipo.tipoEquipo == "Transformador") {
            return listOf("AT-T", "AT-BT", "BT-T")
        }
        if (equipo.tipoEquipo == "Interruptor" || equipo.tipoEquipo == "Celda MT") {
            return listOf("MT-BT", "MT-Tierra", "Fases-Tierra")
        }
        if (equipo.tipoEquipo == "Cable de poder") {
            return listOf("Fase R", "Fase S", "Fase T")
        }
        return listOf("Equipo completo")
    }
    if (tipo == TipoPrueba.DEVANADOS) {
        return listOf("AT-R", "AT-S", "AT-T", "BT-r", "BT-s", "BT-t")
    }
    if (tipo == TipoPrueba.CONTACTOS) {
        return listOf("Polo 1", "Polo 2", "Polo 3")
    }
    if (tipo == TipoPrueba.OPERACION_RELE) {
        return listOf("Disparo de fase", "Disparo de neutro", "Disparo de tierra")
    }
    return listOf("3 fases / cambiador de derivaciones")
}

internal fun relacionTeoricaPlaca(equipo: Equipo): String {
    val primaria = equipo.tensionPrimariaKv
    val secundaria = equipo.tensionSecundariaKv
    if (primaria == null) {
        return ""
    }
    if (secundaria == null) {
        return ""
    }
    if (secundaria <= 0.0) {
        return ""
    }
    return formatear(primaria / secundaria)
}
