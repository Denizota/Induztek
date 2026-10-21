package cl.duoc.induztek.model


data class Usuario(
    val id: Long,
    val rolId: Long,
    val rolCodigo: String,
    val rolNombre: String,
    val rut: String,
    val nombre: String,
    val correo: String,
    val passwordHash: String,
    val activo: Boolean
)