package cl.duoc.induztek.model

enum class EstadoProtocolo(val codigo: String, val nombre: String) {
    BORRADOR("BORRADOR", "Borrador"),
    REGISTRADO("REGISTRADO_TERRENO", "Registrado en terreno"),
    OBSERVADO("OBSERVADO", "Observado"),
    FORMALIZADO("FORMALIZADO", "Formalizado"),
    ANULADO("ANULADO", "Anulado")
}
