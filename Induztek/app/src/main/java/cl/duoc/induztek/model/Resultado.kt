package cl.duoc.induztek.model

enum class Resultado {
    APROVADO,
    OBSERVADO,
    RECHAZADO;

    companion object {
        fun peor(a: Resultado?, b: Resultado?): Resultado? {
            if (a == null) return b
            if (b == null) return a
            return if (a.ordinal >= b.ordinal) a else b
        }

    }
}