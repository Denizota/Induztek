package cl.duoc.induztek.model

enum class Resultado(val codigo: String, val nombre: String) {
    APROBADO("APROBADO", "Aprobado"),
    OBSERVADO("OBSERVADO", "Observado"),
    RECHAZADO("RECHAZADO", "Rechazado");

    companion object {
        fun peor(a: Resultado?, b: Resultado?): Resultado? {
            if (a == null) {
                return b
            }
            if (b == null) {
                return a
            }
            if (a.ordinal >= b.ordinal) {
                return a
            }
            return b
        }
    }
}
