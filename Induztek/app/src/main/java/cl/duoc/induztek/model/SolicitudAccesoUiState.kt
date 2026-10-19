package cl.duoc.induztek.model

data class SolicitudAccesoUiState(
    val nombre: String = "",
    val apellido: String = "",
    val pais: String = "",
    val ciudad: String = "",

    //Listas ejemplares para el desplegable
    val paises: List<String> = listOf("Chile", "Argentina", "Peru"),
    val ciudades: List<String> = listOf("Santiago", "Valparaiso", "Concepcion", "Temuco"),

    val correo: String = "",
    val contrasena: String = "",
    val repetirContrasena: String = "",
    val justificacion: String = "",
    val contrasenaVisible: Boolean = false,
    val repetirContrasenaVisible: Boolean = false,
    val errorNombre: String? = null,
    val errorApellido: String? = null,
    val errorCorreo: String? = null,
    val errorContrasena: String? = null,
    val errorRepetirContrasena: String? = null
    //val errorJustificacion: String? = null
) {
    val registrarseHabilitado: Boolean
        get()= nombre.isNotBlank() && apellido.isNotBlank() && correo.isNotBlank()
                && contrasena.isNotBlank() && repetirContrasena.isNotBlank()
}
