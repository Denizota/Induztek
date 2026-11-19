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

    val idEmpresa: Int? = null,
    val cargo: String = "",
    val giro: String = ""
)

