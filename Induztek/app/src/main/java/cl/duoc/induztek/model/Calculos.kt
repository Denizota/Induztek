package cl.duoc.induztek.model

import kotlin.math.abs


object Calculos {

    fun indicePolarizacion(lectura1Min: Double?, lectura10Min: Double?): Double? {
        val a = lectura1Min ?: return null
        val b = lectura10Min ?: return null
        if (a <= 0.0) return null
        return b / a
    }

    fun resultadoAislacion(ip: Double?, lectura1Min: Double?): Resultado? {
        if (lectura1Min == null) return null
        if (lectura1Min < 100.0) return Resultado.RECHAZADO
        if (ip == null) return Resultado.APROBADO
        return when {
            ip < 1.0 -> Resultado.RECHAZADO
            ip < 2.0 -> Resultado.OBSERVADO
            else -> Resultado.APROBADO
        }
    }

    fun errorTtr(teorica: Double?, medida: Double?): Double? {
        val t = teorica ?: return null
        val m = medida ?: return null
        if (t == 0.0) return null
        return abs(m - t) / t * 100.0
    }

    fun resultadoTtr(errorPct: Double?): Resultado? = errorPct?.let {
        when {
            it <= 0.5 -> Resultado.APROBADO
            it <= 1.0 -> Resultado.OBSERVADO
            else -> Resultado.RECHAZADO
        }
    }

    fun porcentajeContactos(valorUohm: Double?, maxPlacaUohm: Double?): Double? {
        val v = valorUohm ?: return null
        val m = maxPlacaUohm ?: return null
        if (m == 0.0) return null
        return v / m * 100.0
    }

    fun resultadoContactos(pct: Double?): Resultado? = pct?.let {
        when {
            it <= 100.0 -> Resultado.APROBADO
            it <= 120.0 -> Resultado.OBSERVADO
            else -> Resultado.RECHAZADO
        }
    }

    fun porcentajeDiferenciaTiempo(medidoMs: Double?, esperadoMs: Double?): Double? {
        val m = medidoMs ?: return null
        val e = esperadoMs ?: return null
        if (e == 0.0) return null
        return abs(m - e) / e * 100.0
    }

    fun resultadoRele(opera: Boolean, pctDiferencia: Double?): Resultado? {
        if (!opera) return Resultado.RECHAZADO
        return pctDiferencia?.let {
            when {
                it <= 5.0 -> Resultado.APROBADO
                it <= 10.0 -> Resultado.OBSERVADO
                else -> Resultado.RECHAZADO
            }
        }
    }

    fun variacionPct(actual: Double?, anterior: Double?): Double? {
        val a = actual ?: return null
        val p = anterior ?: return null
        if (p == 0.0) return null
        return (a - p) / p * 100.0
    }

    fun esVariacionSignificativa(variacionPct: Double?): Boolean =
        variacionPct != null && abs(variacionPct) > 25.0

    fun resultadoGlobal(resultados: List<Resultado?>): Resultado? =
        resultados.filterNotNull().maxByOrNull { it.ordinal }

    fun rutValido(rut: String): Boolean {
        val limpio = rut.replace(".", "").
        replace("-", "")
            .replace(" ", "")
            .uppercase()
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


    fun fmt(v: Double?, decimales: Int = 2): String = v?.let {
        String.format(java.util.Locale.US, "%,.${decimales}f", it)
            .replace(',', '¤').replace('.', ',').replace('¤', '.')
    } ?: "—"

}
