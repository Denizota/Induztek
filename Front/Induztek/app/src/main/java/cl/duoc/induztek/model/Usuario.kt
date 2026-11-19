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
    val giro: String = "",

    val bio: String = "",
    val especialidades: String = "",
    val formacion: String = "",
    val certificaciones: String = "",
    val desde: Int? = null,
    val calificacion: Int? = null
) {
    fun listaDeEspecialidades(): List<String> {
        val lista = mutableListOf<String>()
        for (parte in especialidades.split(',')) {
            val limpia = parte.trim()
            if (limpia.isNotEmpty()) lista.add(limpia)
        }
        return lista
    }
}

data class Solicitud(
    val idSolicitud: Int,
    val nombre: String,
    val apellido: String,
    val pais: String,
    val ciudad: String,
    val correo: String,
    val justificacion: String,
    val estado: String,
    val tipo: String,
    val idUsuario: Int?,
    val campo: String = "",
    val valor: String = ""
) {
    val nombreCompleto: String get() = "$nombre $apellido".trim()
    val esBaja: Boolean get() = tipo == "BAJA"
    val esCambio: Boolean get() = tipo == "CAMBIO"
}
