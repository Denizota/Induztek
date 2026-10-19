package cl.duoc.induztek.model

data class ChatResumen(
    val id: Int,
    val nombre: String,
    val ultimoMensaje: String,
    val hora: String,
    val sinLeer: Int = 0
)