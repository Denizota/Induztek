package cl.duoc.induztek.model

data class FilaHistorial (
    val numero: String, //XDDDDDDDD
    val fecha: String,
    val valor: String,
    val resultado: String,
    val estado: String,
    val variacionPct: Double?
)
