package cl.duoc.induztek.viewmodel

// ViewModel del formulario de prueba (MVVM): estado de lo escrito, validaciones y cálculo
// del resultado antes de guardar. Las reglas viven aquí, no en la pantalla.

import android.app.Application
import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.AndroidViewModel
import cl.duoc.induztek.R
import cl.duoc.induztek.data.InduztekDbHelper
import cl.duoc.induztek.data.fechaHoy
import cl.duoc.induztek.model.Equipo
import cl.duoc.induztek.model.Instrumento
import cl.duoc.induztek.model.Protocolo
import cl.duoc.induztek.model.Resultado
import cl.duoc.induztek.model.TipoPrueba
import cl.duoc.induztek.model.Usuario
import cl.duoc.induztek.model.Calculos



class PruebaFormsViewModel(app: Application) : AndroidViewModel(app) {
    private val db = InduztekDbHelper.obtener(app)


    val estado = mutableStateMapOf<String, String>()
    private var cargadoDe: Int? = null
    private var generacionCargada = InduztekDbHelper.generacion

    fun equipo(id: Int) = db.equipoPorId(id)
    fun tiposDe(idEquipo: Int) = db.tiposPruebaDeEquipo(idEquipo)

    fun bloqueo(idEquipo: Int, idUsuario: Int): String? =
        runCatching { db.asegurarProtocoloEditable(idEquipo, idUsuario) }.exceptionOrNull()?.message

    fun protocoloEditable(idEquipo: Int, idUsuario: Int) = db.asegurarProtocoloEditable(idEquipo, idUsuario)

    fun instrumentosPara(tipo: TipoPrueba) =
        db.listarInstrumentos().filter { aplicaInstrumento(tipo, it.nombre) }

    fun valorAnterior(idEquipo: Int, tipo: TipoPrueba, parametro: String, excluirProtocolo: Int) =
        db.valorAnterior(idEquipo, tipo, parametro, excluirProtocolo)

    fun sugerencias(idEquipo: Int, tipo: TipoPrueba, parametro: String, excluirProtocolo: Int) =
        db.valoresHistoricos(idEquipo, tipo, parametro, excluirProtocolo)



    fun cargar(idProtocolo: Int) {

        if (generacionCargada != InduztekDbHelper.generacion) {
            estado.clear()
            cargadoDe = null
            generacionCargada = InduztekDbHelper.generacion
        }
        if (cargadoDe == idProtocolo) return
        cargadoDe = idProtocolo
        db.listarPruebas(idProtocolo).forEach { pr ->
            val t = pr.tipoPrueba.name
            estado["$t|_elemento"] = pr.elemento
            if (!pr.realizada) estado["$t|_noRealizada"] = "1"
            if (pr.motivoNoRealizada.isNotEmpty()) estado["$t|_motivo"] = pr.motivoNoRealizada
            if (pr.comentario.isNotEmpty()) estado["$t|_comentario"] = pr.comentario
            pr.resultado?.let { estado["$t|_resultado"] = it.nombre }
            pr.idInstrumento?.let { id ->
                db.listarInstrumentos().firstOrNull { it.idInstrumento == id }?.let {
                    estado["$t|_instrumento"] = "${it.nombre} - ${it.numeroSerie}"
                }
            }
            pr.mediciones.forEach { (parametro, valor) ->
                estado["${pr.elemento}|$t|$parametro"] = formatear(valor)
            }
        }
    }

    fun guardarNoRealizada(
        idProtocolo: Int, tipo: TipoPrueba, elemento: String, motivo: String, comentario: String
    ) = db.guardarPrueba(idProtocolo, tipo, elemento, false, motivo, null, null, comentario, emptyMap())



    fun guardar(
        idProtocolo: Int, tipo: TipoPrueba, equipo: Equipo, elemento: String,
        instrumentos: List<Instrumento>, instrumentoNombre: String, valores: Map<String, String>,
        unidad: String, resultadoManual: String, releOpera: Boolean, comentario: String
    ): String? {
        val ins = instrumentos.firstOrNull { "${it.nombre} - ${it.numeroSerie}" == instrumentoNombre }
            ?: return txt(R.string.vm_rf05)
        if (!ins.vigenteEn(fechaHoy()))
            return txt(R.string.vm_rn11, ins.nombre, ins.fechaVence)
        validarValores(getApplication(), tipo, valores, equipo.exigeLectura10Min)?.let { return it }
        val resultado = when (tipo) {
            TipoPrueba.DEVANADOS -> Resultado.entries.firstOrNull { r -> r.nombre == resultadoManual }
            else -> calcularResultadoAutomatico(tipo, equipo, valores, releOpera, unidad)
        }
        db.guardarPrueba(
            idProtocolo, tipo, elemento, true, "", ins.idInstrumento, resultado, comentario,
            medicionesDe(tipo, valores, unidad)
        )
        return null
    }



    private fun txt(@StringRes id: Int, vararg args: Any): String =
        getApplication<Application>().getString(id, *args)
}



internal fun formatear(valor: Double) =
    if (valor == valor.toLong().toDouble()) valor.toLong().toString() else valor.toString()



internal fun aMega(x: Double?, unidad: String): Double? = x?.let {
    when (unidad) {
        "kΩ" -> it / 1000
        "GΩ" -> it * 1000
        else -> it
    }
}



internal fun parametrosDe(tipo: TipoPrueba): List<String> = when (tipo) {
    TipoPrueba.AISLACION -> listOf("TENSION_PRUEBA", "LECTURA_1MIN", "LECTURA_10MIN")
    TipoPrueba.TTR -> listOf("POSICION_TAP", "RELACION_TEORICA", "RELACION_MEDIDA")
    TipoPrueba.DEVANADOS -> listOf("RESISTENCIA")
    TipoPrueba.CONTACTOS -> listOf("RESISTENCIA_CONTACTO")
    TipoPrueba.OPERACION_RELE -> listOf("CORRIENTE_AJUSTE", "CORRIENTE_OPERACION", "TIEMPO_ESPERADO", "TIEMPO_MEDIDO")
}



private fun aplicaInstrumento(tipo: TipoPrueba, nombre: String): Boolean = when (tipo) {
    TipoPrueba.AISLACION -> nombre.contains("Megóhmetro")
    TipoPrueba.TTR -> nombre.contains("relación")
    TipoPrueba.DEVANADOS, TipoPrueba.CONTACTOS -> nombre.contains("Micro-ohmímetro")
    TipoPrueba.OPERACION_RELE -> nombre.contains("inyección")
}



private fun validarValores(
    contexto: Context, tipo: TipoPrueba, m: Map<String, String>, conLectura10Min: Boolean
): String? {
    fun nombres(codigos: List<String>) =
        codigos.joinToString { contexto.getString(nombreParametro(it)) }
    val malos = parametrosDe(tipo)
        .filter { it != "POSICION_TAP" }
        .filter { k -> m[k]?.toDoubleOrNull()?.let { it <= 0.0 } == true }
    if (malos.isNotEmpty())
        return contexto.getString(R.string.vm_hu03, nombres(malos))
    val obligatorios = when (tipo) {
        TipoPrueba.AISLACION -> if (conLectura10Min) {
            listOf("TENSION_PRUEBA", "LECTURA_1MIN", "LECTURA_10MIN")
        } else {
            listOf("TENSION_PRUEBA", "LECTURA_1MIN")
        }
        TipoPrueba.TTR -> listOf("POSICION_TAP", "RELACION_TEORICA", "RELACION_MEDIDA")
        TipoPrueba.DEVANADOS -> listOf("RESISTENCIA")
        TipoPrueba.CONTACTOS -> listOf("RESISTENCIA_CONTACTO")
        TipoPrueba.OPERACION_RELE ->
            listOf("CORRIENTE_AJUSTE", "CORRIENTE_OPERACION", "TIEMPO_ESPERADO", "TIEMPO_MEDIDO")
    }
    val faltan = obligatorios.filter { m[it]?.toDoubleOrNull() == null }
    return faltan.takeIf { it.isNotEmpty() }
        ?.let { contexto.getString(R.string.vm_faltan_valores, nombres(it)) }
}


@StringRes
private fun nombreParametro(codigo: String): Int = when (codigo) {
    "TENSION_PRUEBA" -> R.string.form_tension_prueba
    "LECTURA_1MIN" -> R.string.form_lectura_1min
    "LECTURA_10MIN" -> R.string.form_lectura_10min
    "POSICION_TAP" -> R.string.form_posicion_tap
    "RELACION_TEORICA" -> R.string.form_relacion_teorica
    "RELACION_MEDIDA" -> R.string.form_relacion_medida
    "RESISTENCIA" -> R.string.form_resistencia_fase
    "RESISTENCIA_CONTACTO" -> R.string.form_resistencia_polo
    "CORRIENTE_AJUSTE" -> R.string.form_ajuste_corriente
    "CORRIENTE_OPERACION" -> R.string.form_corriente_operacion
    "TIEMPO_ESPERADO" -> R.string.form_tiempo_esperado
    "TIEMPO_MEDIDO" -> R.string.form_tiempo_medido
    else -> R.string.form_resistencia_fase
}


private fun medicionesDe(tipo: TipoPrueba, m: Map<String, String>, unidad: String): Map<String, Double> =
    parametrosDe(tipo).mapNotNull { key ->
        val raw = m[key]?.toDoubleOrNull() ?: return@mapNotNull null
        val valor = if (tipo == TipoPrueba.AISLACION && (key == "LECTURA_1MIN" || key == "LECTURA_10MIN"))
            aMega(raw, unidad) ?: return@mapNotNull null
        else raw
        key to valor
    }.toMap()



private fun calcularResultadoAutomatico(
    tipo: TipoPrueba, equipo: Equipo, m: Map<String, String>, releOpera: Boolean, unidad: String
): Resultado? {
    fun num(k: String) = m[k]?.toDoubleOrNull()
    return when (tipo) {
        TipoPrueba.AISLACION -> Calculos.resultadoAislacion(
            Calculos.indicePolarizacion(aMega(num("LECTURA_1MIN"), unidad), aMega(num("LECTURA_10MIN"), unidad)),
            aMega(num("LECTURA_1MIN"), unidad)
        )
        TipoPrueba.TTR -> Calculos.resultadoTtr(Calculos.errorTtr(num("RELACION_TEORICA"), num("RELACION_MEDIDA")))
        TipoPrueba.DEVANADOS -> null
        TipoPrueba.CONTACTOS -> Calculos.resultadoContactos(
            Calculos.porcentajeContactos(num("RESISTENCIA_CONTACTO"), equipo.maxContactosUohm)
        )
        TipoPrueba.OPERACION_RELE -> Calculos.resultadoRele(
            releOpera, Calculos.porcentajeDiferenciaTiempo(num("TIEMPO_MEDIDO"), num("TIEMPO_ESPERADO"))
        )
    }
}
