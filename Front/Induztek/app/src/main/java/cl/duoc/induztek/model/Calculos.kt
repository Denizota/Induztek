
package cl.duoc.induztek.model

import kotlin.math.abs

object Calculos {

    fun indicePolarizacion(lecturaUnoMinuto: Double?, lecturaDiezMinutos: Double?): Double? {
        if (lecturaUnoMinuto == null) {
            return null
        }
        if (lecturaDiezMinutos == null) {
            return null
        }
        if (lecturaUnoMinuto <= 0.0) {
            return null
        }
        return lecturaDiezMinutos / lecturaUnoMinuto
    }

    fun resultadoAislacion(indicePolarizacion: Double?, lecturaUnoMinuto: Double?): Resultado? {
        if (lecturaUnoMinuto == null) {
            return null
        }
        if (lecturaUnoMinuto < 100.0) {
            return Resultado.RECHAZADO
        }
        if (indicePolarizacion == null) {
            return Resultado.APROBADO
        }
        if (indicePolarizacion < 1.0) {
            return Resultado.RECHAZADO
        }
        if (indicePolarizacion < 2.0) {
            return Resultado.OBSERVADO
        }
        return Resultado.APROBADO
    }

    fun errorTtr(relacionTeorica: Double?, relacionMedida: Double?): Double? {
        if (relacionTeorica == null) {
            return null
        }
        if (relacionMedida == null) {
            return null
        }
        if (relacionTeorica == 0.0) {
            return null
        }
        return abs(relacionMedida - relacionTeorica) / relacionTeorica * 100.0
    }

    fun resultadoTtr(errorPorcentaje: Double?): Resultado? {
        if (errorPorcentaje == null) {
            return null
        }
        if (errorPorcentaje <= 0.5) {
            return Resultado.APROBADO
        }
        if (errorPorcentaje <= 1.0) {
            return Resultado.OBSERVADO
        }
        return Resultado.RECHAZADO
    }

    fun porcentajeContactos(valorMicroohm: Double?, maximoPlacaMicroohm: Double?): Double? {
        if (valorMicroohm == null) {
            return null
        }
        if (maximoPlacaMicroohm == null) {
            return null
        }
        if (maximoPlacaMicroohm == 0.0) {
            return null
        }
        return valorMicroohm / maximoPlacaMicroohm * 100.0
    }

    fun resultadoContactos(porcentaje: Double?): Resultado? {
        if (porcentaje == null) {
            return null
        }
        if (porcentaje <= 100.0) {
            return Resultado.APROBADO
        }
        if (porcentaje <= 120.0) {
            return Resultado.OBSERVADO
        }
        return Resultado.RECHAZADO
    }

    fun porcentajeDiferenciaTiempo(tiempoMedidoMilisegundos: Double?, tiempoEsperadoMilisegundos: Double?): Double? {
        if (tiempoMedidoMilisegundos == null) {
            return null
        }
        if (tiempoEsperadoMilisegundos == null) {
            return null
        }
        if (tiempoEsperadoMilisegundos == 0.0) {
            return null
        }
        return abs(tiempoMedidoMilisegundos - tiempoEsperadoMilisegundos) / tiempoEsperadoMilisegundos * 100.0
    }

    fun resultadoRele(opera: Boolean, diferenciaPorcentaje: Double?): Resultado? {
        if (!opera) {
            return Resultado.RECHAZADO
        }
        if (diferenciaPorcentaje == null) {
            return null
        }
        if (diferenciaPorcentaje <= 5.0) {
            return Resultado.APROBADO
        }
        if (diferenciaPorcentaje <= 10.0) {
            return Resultado.OBSERVADO
        }
        return Resultado.RECHAZADO
    }

    fun variacionPct(actual: Double?, anterior: Double?): Double? {
        if (actual == null) {
            return null
        }
        if (anterior == null) {
            return null
        }
        if (anterior == 0.0) {
            return null
        }
        return (actual - anterior) / anterior * 100.0
    }

    fun esVariacionSignificativa(variacionPct: Double?): Boolean {
        if (variacionPct == null) {
            return false
        }
        return abs(variacionPct) > 25.0
    }

    fun resultadoGlobal(resultados: List<Resultado?>): Resultado? {
        var peor: Resultado? = null
        for (resultado in resultados) {
            if (resultado != null) {
                if (peor == null || resultado.ordinal > peor.ordinal) {
                    peor = resultado
                }
            }
        }
        return peor
    }

    fun rutValido(rut: String): Boolean {
        var limpio = rut.replace(".", "")
        limpio = limpio.replace("-", "")
        limpio = limpio.replace(" ", "")
        limpio = limpio.uppercase()

        if (limpio.length < 7 || limpio.length > 10) {
            return false
        }

        var cuerpo = ""
        for (indice in 0 until limpio.length - 1) {
            cuerpo = cuerpo + limpio[indice]
        }
        val digitoVerificador = limpio[limpio.length - 1]

        for (letra in cuerpo) {
            if (!letra.isDigit()) {
                return false
            }
        }
        if (!digitoVerificador.isDigit() && digitoVerificador != 'K') {
            return false
        }

        var suma = 0
        var factor = 2
        for (i in cuerpo.length - 1 downTo 0) {
            suma += (cuerpo[i] - '0') * factor
            if (factor == 7) {
                factor = 2
            } else {
                factor = factor + 1
            }
        }

        val resto = 11 - (suma % 11)
        val digitoEsperado: Char
        if (resto == 11) {
            digitoEsperado = '0'
        } else if (resto == 10) {
            digitoEsperado = 'K'
        } else {
            digitoEsperado = resto.toString()[0]
        }
        return digitoVerificador == digitoEsperado
    }

    fun formatear(valor: Double?, decimales: Int = 2): String {
        if (valor == null) {
            return "—"
        }
        val conFormato = String.format(java.util.Locale.US, "%,.${decimales}f", valor)
        val conMarca = conFormato.replace(',', '¤')
        val conComa = conMarca.replace('.', ',')
        return conComa.replace('¤', '.')
    }
}
