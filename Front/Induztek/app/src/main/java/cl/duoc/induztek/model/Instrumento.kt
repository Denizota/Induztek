package cl.duoc.induztek.model

data class Instrumento(
    val idInstrumento: Int,
    val nombre: String,
    val marca: String,
    val modelo: String,
    val numeroSerie: String,
    val fechaUltimaCal: String,
    val fechaVence: String
) {
    fun vigenteEn(fecha: String): Boolean {
        val vence = fechaVence.replace("-", "").toInt()
        val hoy = fecha.replace("-", "").toInt()
        return vence >= hoy
    }
}
