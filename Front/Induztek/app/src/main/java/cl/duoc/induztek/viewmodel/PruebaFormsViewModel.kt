package cl.duoc.induztek.viewmodel

import android.app.Application
import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.AndroidViewModel
import cl.duoc.induztek.R
import cl.duoc.induztek.data.*
import cl.duoc.induztek.model.Calculos
import cl.duoc.induztek.model.Equipo
import cl.duoc.induztek.model.Instrumento
import cl.duoc.induztek.model.Protocolo
import cl.duoc.induztek.model.Resultado
import cl.duoc.induztek.model.TipoPrueba
import cl.duoc.induztek.model.Usuario

class PruebaFormsViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {
    private val baseDatos = InduztekDbHelper.obtener(aplicacion)
    private val borradores = aplicacion.getSharedPreferences("borradores_pruebas", Context.MODE_PRIVATE)

    val estado = mutableStateMapOf<String, String>()
    private var cargadoDe: Int? = null
    private var generacionCargada = InduztekDbHelper.generacion

    fun equipo(id: Int) = baseDatos.equipoPorId(id)
    fun tiposDe(idEquipo: Int) = baseDatos.tiposPruebaDeEquipo(idEquipo)

    fun bloqueo(idEquipo: Int, idUsuario: Int): String? {
        try {
            baseDatos.asegurarProtocoloEditable(idEquipo, idUsuario)
            return null
        } catch (error: Exception) {
            return error.message
        }
    }

    fun protocoloEditable(idEquipo: Int, idUsuario: Int) = baseDatos.asegurarProtocoloEditable(idEquipo, idUsuario)

    fun instrumentosPara(tipo: TipoPrueba): List<Instrumento> {
        val lista = mutableListOf<Instrumento>()
        for (instrumento in baseDatos.listarInstrumentos()) {
            if (aplicaInstrumento(tipo, instrumento.nombre)) {
                lista.add(instrumento)
            }
        }
        return lista
    }

    fun valorAnterior(idEquipo: Int, tipo: TipoPrueba, parametro: String, excluirProtocolo: Int) =
        baseDatos.valorAnterior(idEquipo, tipo, parametro, excluirProtocolo)

    fun sugerencias(idEquipo: Int, tipo: TipoPrueba, parametro: String, excluirProtocolo: Int) =
        baseDatos.valoresHistoricos(idEquipo, tipo, parametro, excluirProtocolo)

    fun cargar(idProtocolo: Int) {
        if (generacionCargada != InduztekDbHelper.generacion) {
            estado.clear()
            cargadoDe = null
            generacionCargada = InduztekDbHelper.generacion
        }
        if (cargadoDe == idProtocolo) {
            return
        }
        cargadoDe = idProtocolo

        val pruebas = baseDatos.listarPruebas(idProtocolo)
        for (prueba in pruebas) {
            val tipoPrueba = prueba.tipoPrueba.name
            estado["$tipoPrueba|_elemento"] = prueba.elemento
            if (!prueba.realizada) {
                estado["$tipoPrueba|_noRealizada"] = "1"
            }
            if (prueba.motivoNoRealizada.isNotEmpty()) {
                estado["$tipoPrueba|_motivo"] = prueba.motivoNoRealizada
            }
            if (prueba.comentario.isNotEmpty()) {
                estado["$tipoPrueba|_comentario"] = prueba.comentario
            }
            val resultado = prueba.resultado
            if (resultado != null) {
                estado["$tipoPrueba|_resultado"] = resultado.nombre
            }
            val idInstrumento = prueba.idInstrumento
            if (idInstrumento != null) {
                val instrumentos = baseDatos.listarInstrumentos()
                for (instrumento in instrumentos) {
                    if (instrumento.idInstrumento == idInstrumento) {
                        estado["$tipoPrueba|_instrumento"] = "${instrumento.nombre} - ${instrumento.numeroSerie}"
                    }
                }
            }
            for (medicion in prueba.mediciones) {
                val parametro = medicion.key
                val valor = medicion.value
                estado["${prueba.elemento}|$tipoPrueba|$parametro"] = formatear(valor)
            }
        }
        val prefijo = "protocolo_${idProtocolo}_"
        for ((clave, valor) in borradores.all) {
            if (clave.startsWith(prefijo) && valor is String) {
                estado[clave.removePrefix(prefijo)] = valor
            }
        }
    }

    fun guardarBorrador(idProtocolo: Int, campo: String, valor: String) {
        estado[campo] = valor
        borradores.edit().putString("protocolo_${idProtocolo}_$campo", valor).apply()
    }

    fun limpiarBorrador(idProtocolo: Int, tipo: TipoPrueba) {
        val prefijoProtocolo = "protocolo_${idProtocolo}_"
        val editor = borradores.edit()
        for (clave in borradores.all.keys) {
            if (clave.startsWith(prefijoProtocolo)) {
                val campo = clave.removePrefix(prefijoProtocolo)
                if (campo.startsWith("${tipo.name}|_") || campo.contains("|${tipo.name}|")) editor.remove(clave)
            }
        }
        editor.apply()
    }

    fun guardarNoRealizada(
        idProtocolo: Int, tipo: TipoPrueba, elemento: String, motivo: String, comentario: String
    ) = baseDatos.guardarPrueba(idProtocolo, tipo, elemento, false, motivo, null, null, comentario, emptyMap())

    fun guardar(
        idProtocolo: Int, tipo: TipoPrueba, equipo: Equipo, elemento: String,
        instrumentos: List<Instrumento>, instrumentoNombre: String, valores: Map<String, String>,
        unidad: String, resultadoManual: String, releOpera: Boolean, comentario: String
    ): String? {
        var instrumentoElegido: Instrumento? = null
        for (instrumento in instrumentos) {
            if ("${instrumento.nombre} - ${instrumento.numeroSerie}" == instrumentoNombre) {
                instrumentoElegido = instrumento
            }
        }
        if (instrumentoElegido == null) {
            return texto(R.string.vm_rf05)
        }
        if (!instrumentoElegido.vigenteEn(fechaHoy())) {
            return texto(R.string.vm_rn11, instrumentoElegido.nombre, instrumentoElegido.fechaVence)
        }

        val problema = validarValores(getApplication(), tipo, valores, equipo.exigeLectura10Min)
        if (problema != null) {
            return problema
        }

        var resultado: Resultado? = null
        if (tipo == TipoPrueba.DEVANADOS) {
            val resultados = Resultado.values()
            for (posible in resultados) {
                if (posible.nombre == resultadoManual) {
                    resultado = posible
                }
            }
        } else {
            resultado = calcularResultadoAutomatico(tipo, equipo, valores, releOpera, unidad)
        }

        baseDatos.guardarPrueba(
            idProtocolo, tipo, elemento, true, "", instrumentoElegido.idInstrumento, resultado, comentario,
            medicionesDe(tipo, valores, unidad)
        )
        return null
    }

    private fun texto(@StringRes id: Int, vararg args: Any): String =
        getApplication<Application>().getString(id, *args)
}

internal fun formatear(valor: Double): String {
    if (valor == valor.toLong().toDouble()) {
        return valor.toLong().toString()
    }
    return valor.toString()
}

internal fun aMega(x: Double?, unidad: String): Double? {
    if (x == null) {
        return null
    }
    if (unidad == "kΩ") {
        return x / 1000
    }
    if (unidad == "GΩ") {
        return x * 1000
    }
    return x
}

internal fun parametrosDe(tipo: TipoPrueba): List<String> {
    if (tipo == TipoPrueba.AISLACION) {
        return listOf("TENSION_PRUEBA", "LECTURA_1MIN", "LECTURA_10MIN")
    }
    if (tipo == TipoPrueba.TTR) {
        return listOf("POSICION_TAP", "RELACION_TEORICA", "RELACION_MEDIDA")
    }
    if (tipo == TipoPrueba.DEVANADOS) {
        return listOf("RESISTENCIA")
    }
    if (tipo == TipoPrueba.CONTACTOS) {
        return listOf("RESISTENCIA_CONTACTO")
    }
    return listOf("CORRIENTE_AJUSTE", "CORRIENTE_OPERACION", "TIEMPO_ESPERADO", "TIEMPO_MEDIDO")
}

private fun aplicaInstrumento(tipo: TipoPrueba, nombre: String): Boolean {
    if (tipo == TipoPrueba.AISLACION) {
        return nombre.contains("Megóhmetro")
    }
    if (tipo == TipoPrueba.TTR) {
        return nombre.contains("relación")
    }
    if (tipo == TipoPrueba.DEVANADOS || tipo == TipoPrueba.CONTACTOS) {
        return nombre.contains("Micro-ohmímetro")
    }
    return nombre.contains("inyección")
}

private fun validarValores(
    contexto: Context, tipo: TipoPrueba, valores: Map<String, String>, conLectura10Min: Boolean
): String? {
    fun nombres(codigos: List<String>): String {
        var textos = ""
        for (codigo in codigos) {
            if (textos.isNotEmpty()) textos = textos + ", "
            textos = textos + contexto.getString(nombreParametro(codigo))
        }
        return textos
    }

    val malos = mutableListOf<String>()
    for (codigo in parametrosDe(tipo)) {
        if (codigo == "POSICION_TAP") {
            continue
        }
        val valor = numeroDe(valores, codigo)
        if (valor != null && valor <= 0.0) {
            malos.add(codigo)
        }
    }
    if (malos.isNotEmpty()) {
        return contexto.getString(R.string.vm_hu03, nombres(malos))
    }

    val obligatorios: List<String>
    if (tipo == TipoPrueba.AISLACION) {
        if (conLectura10Min) {
            obligatorios = listOf("TENSION_PRUEBA", "LECTURA_1MIN", "LECTURA_10MIN")
        } else {
            obligatorios = listOf("TENSION_PRUEBA", "LECTURA_1MIN")
        }
    } else if (tipo == TipoPrueba.TTR) {
        obligatorios = listOf("POSICION_TAP", "RELACION_TEORICA", "RELACION_MEDIDA")
    } else if (tipo == TipoPrueba.DEVANADOS) {
        obligatorios = listOf("RESISTENCIA")
    } else if (tipo == TipoPrueba.CONTACTOS) {
        obligatorios = listOf("RESISTENCIA_CONTACTO")
    } else {
        obligatorios = listOf("CORRIENTE_AJUSTE", "CORRIENTE_OPERACION", "TIEMPO_ESPERADO", "TIEMPO_MEDIDO")
    }

    val faltan = mutableListOf<String>()
    for (codigo in obligatorios) {
        if (numeroDe(valores, codigo) == null) {
            faltan.add(codigo)
        }
    }
    if (faltan.isNotEmpty()) {
        return contexto.getString(R.string.vm_faltan_valores, nombres(faltan))
    }
    return null
}

@StringRes
private fun nombreParametro(codigo: String): Int {
    if (codigo == "TENSION_PRUEBA") return R.string.form_tension_prueba
    if (codigo == "LECTURA_1MIN") return R.string.form_lectura_1min
    if (codigo == "LECTURA_10MIN") return R.string.form_lectura_10min
    if (codigo == "POSICION_TAP") return R.string.form_posicion_tap
    if (codigo == "RELACION_TEORICA") return R.string.form_relacion_teorica
    if (codigo == "RELACION_MEDIDA") return R.string.form_relacion_medida
    if (codigo == "RESISTENCIA") return R.string.form_resistencia_fase
    if (codigo == "RESISTENCIA_CONTACTO") return R.string.form_resistencia_polo
    if (codigo == "CORRIENTE_AJUSTE") return R.string.form_ajuste_corriente
    if (codigo == "CORRIENTE_OPERACION") return R.string.form_corriente_operacion
    if (codigo == "TIEMPO_ESPERADO") return R.string.form_tiempo_esperado
    if (codigo == "TIEMPO_MEDIDO") return R.string.form_tiempo_medido
    return R.string.form_resistencia_fase
}

private fun numeroDe(valores: Map<String, String>, codigo: String): Double? {
    val escrito = valores[codigo]
    if (escrito == null) return null
    return escrito.toDoubleOrNull()
}

private fun medicionesDe(tipo: TipoPrueba, valores: Map<String, String>, unidad: String): Map<String, Double> {
    val mediciones = mutableMapOf<String, Double>()
    for (key in parametrosDe(tipo)) {
        val numero = numeroDe(valores, key)
        if (numero == null) {
            continue
        }
        var valor = numero
        if (tipo == TipoPrueba.AISLACION && (key == "LECTURA_1MIN" || key == "LECTURA_10MIN")) {
            val convertido = aMega(numero, unidad)
            if (convertido == null) {
                continue
            }
            valor = convertido
        }
        mediciones[key] = valor
    }
    return mediciones
}

private fun calcularResultadoAutomatico(
    tipo: TipoPrueba, equipo: Equipo, valores: Map<String, String>, releOpera: Boolean, unidad: String
): Resultado? {
    fun numero(codigoParametro: String): Double? = numeroDe(valores, codigoParametro)

    if (tipo == TipoPrueba.AISLACION) {
        val lecturaUno = aMega(numero("LECTURA_1MIN"), unidad)
        val lecturaDiez = aMega(numero("LECTURA_10MIN"), unidad)
        val indice = Calculos.indicePolarizacion(lecturaUno, lecturaDiez)
        return Calculos.resultadoAislacion(indice, lecturaUno)
    }
    if (tipo == TipoPrueba.TTR) {
        val error = Calculos.errorTtr(numero("RELACION_TEORICA"), numero("RELACION_MEDIDA"))
        return Calculos.resultadoTtr(error)
    }
    if (tipo == TipoPrueba.DEVANADOS) {
        return null
    }
    if (tipo == TipoPrueba.CONTACTOS) {
        val porcentaje = Calculos.porcentajeContactos(numero("RESISTENCIA_CONTACTO"), equipo.maxContactosUohm)
        return Calculos.resultadoContactos(porcentaje)
    }
    val diferencia = Calculos.porcentajeDiferenciaTiempo(numero("TIEMPO_MEDIDO"), numero("TIEMPO_ESPERADO"))
    return Calculos.resultadoRele(releOpera, diferencia)
}
