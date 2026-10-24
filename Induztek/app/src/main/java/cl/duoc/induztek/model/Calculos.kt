package cl.duoc.induztek.model

import kotlin.math.abs


class Calculos {

    //Índice de polarización
    // lectura 10 min y lectura 1 min
    fun indicePolarizacion(lectura1Min: Double?, lectura10Min: Double?): Double? {
        val a = lectura1Min ?: return null
        val b = lectura10Min ?: return null
        if (a <= 0.0) return null
        return b / a
    }

    //Rechazado si IP < 1,0 o lectura 1 min < 100 MΩ
    fun resultadoAislacion(ip: Double?, lectura1Min: Double?): Resultado? {
        if (lectura1Min == null) return null
        if (lectura1Min < 100.0) return Resultado.RECHAZADO
        if (ip == null) return Resultado.APROVADO
        return when {
            ip < 1.0 -> Resultado.RECHAZADO
            ip < 2.0 -> Resultado.OBSERVADO
            else -> Resultado.APROVADO
        }
    }

    // Error TTR = (medida - teorica) / teorica * 100 :C
    fun errorTtr(teorica: Double?, medida: Double?): Double? {
        val t = teorica ?: return null
        val m = medida ?: return null
        if (t == 0.0) return null
        return abs(m - t) / t * 100.0
    }

    // Aprobado si <= 0,5 %, sobservado si <= 1,0 %, Rechazado si > 1,0 %%%
    fun resultadoTtr(errorPct: Double?): Resultado? = errorPct?.let {
        when {
            it <= 0.5 -> Resultado.APROVADO
            it <= 1.0 -> Resultado.OBSERVADO
            else -> Resultado.RECHAZADO
        }
    }

    //Contactos: % sobre el máximo de placa
    fun porcentajeContactos(valorUohm: Double?, maxPlacaUohm: Double?): Double? {
        val v = valorUohm ?: return null
        val m = maxPlacaUohm ?: return null
        if (m == 0.0) return null
        return v / m * 100.0
    }

    // <= 100 % del máximo de placa = Aprobado|| <= 120 % = Observado, sobre eso rechazaod
    fun resultadoContactos(pct: Double?): Resultado? = pct?.let {
        when {
            it <= 100.0 -> Resultado.APROVADO
            it <= 120.0 -> Resultado.OBSERVADO
            else -> Resultado.RECHAZADO
        }
    }

    // Relé: diferencia de tiempo medido vs esperado. sino opera = Rechazooooooooooooooo
    fun porcentajeDiferenciaTiempo(medidoMs: Double?, esperadoMs: Double?): Double? {
        val m = medidoMs ?: return null
        val e = esperadoMs ?: return null
        if (e == 0.0) return null
        return abs(m - e) / e * 100.0
    }

    // <= 5 % de diferencia = Aprobado, <= 10 % = Observado, sobre eso Rechazado
    fun resultadoRele(opera: Boolean, pctDiferencia: Double?): Resultado? {
        if (!opera) return Resultado.RECHAZADO
        return pctDiferencia?.let {
            when {
                it <= 5.0 -> Resultado.APROVADO
                it <= 10.0 -> Resultado.OBSERVADO
                else -> Resultado.RECHAZADO
            }
        }
    }

    // Variación respecto de la prueba anterior del mismo tipo
    fun variacionPct(actual: Double?, anterior: Double?): Double? {
        val a = actual ?: return null
        val p = anterior ?: return null
        if (p == 0.0) return null
        return (a - p) / p * 100.0
    }

    //Sobre el 25 % de variación el ingeniero tiene que dejar un comentario !!!!!
    fun esVariacionSignificativa(variacionPct: Double?): Boolean =
        variacionPct != null && abs(variacionPct) > 25.0

    // Resultado global = peor resultado de las pruebas realizadas :c
    fun resultadoGlobal(resultados: List<Resultado?>): Resultado? =
        resultados.filterNotNull().maxByOrNull { it.ordinal }

    // RUT con dígito verificador válido
    // gracias base de datos por tanto perdon por tan poco
    fun rutValido(rut: String): Boolean {
        val limpio = rut.replace(".", "").replace("-", "").replace(" ", "").uppercase()
        if (limpio.length < 7 || limpio.length > 10) return false
        val cuerpo = limpio.dropLast(1)
        val dv = limpio.last()
        if (!cuerpo.all { it.isDigit() }) return false
        if (!dv.isDigit() && dv != 'K') return false
        var suma = 0
        var factor = 2
        for (i in cuerpo.length - 1 downTo 0) {
            suma += (cuerpo[i] - '0') * factor
            factor = if (factor == 7) 2 else factor + 1
        }
        val resto = 11 - (suma % 11)
        val dvEsperado = when (resto) {
            11 -> '0'
            10 -> 'K'
            else -> resto.toString()[0]
        }
        return dv == dvEsperado
    }
}
