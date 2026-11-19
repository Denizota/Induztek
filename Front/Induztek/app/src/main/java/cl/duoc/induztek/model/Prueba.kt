package cl.duoc.induztek.model

data class Prueba (
    val idPrueba: Int,
    val idProtocolo: Int,
    val tipoPrueba: TipoPrueba,
    val elemento: String,
    val realizada: Boolean,
    val motivoNoRealizada: String,
    val idInstrumento: Int?,
    val resultado: Resultado?,
    val comentario: String,
    val mediciones: Map<String, Double>
)
