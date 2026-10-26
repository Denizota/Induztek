package cl.duoc.induztek.model




data class Usuario(
    val idUsuario: Int,
    val idRol: Int,
    val rut: String,
    val nombre: String,
    val correo: String,
    val telefono: String = "",
    val activo: Boolean = true,
    val rol: Rol,

    // CLIENTE
    val idEmpresa: Int? = null,
    val cargo: String = "",
    val giro: String = ""
)


/*
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

*/