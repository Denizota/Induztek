package cl.duoc.induztek.model

data class Instrumento(
    val idInstrmento: Int,
    val nombre: String,
    val marca: String,
    val modelo: String,
    val numeroSerie: String,
    val fechaUltimaCal: String,
    val fechaVence: String
){
    fun vigenteEn(fecha: String): Boolean = fechaVence
        .replace("-", "")
        .toInt() >= fecha
            .replace("-", "")
            .toInt()
}