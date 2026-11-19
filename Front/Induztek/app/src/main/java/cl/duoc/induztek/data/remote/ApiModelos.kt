
package cl.duoc.induztek.data.remote

import cl.duoc.induztek.model.Rol
import cl.duoc.induztek.model.Usuario

data class UsuarioRemoto(
    val id: Int = 0,
    val rut: String = "",
    val nombre: String = "",
    val correo: String = "",
    val telefono: String = "",
    val activo: Boolean = true,
    val idRol: Int = 4,
    val rolNombre: String = "",
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
    fun rol(): Rol {
        for (posible in Rol.entries) {
            if (posible.ordinal + 1 == idRol) return posible
        }
        return Rol.CLIENTE
    }

    fun aUsuario(): Usuario = Usuario(
        idUsuario = id,
        idRol = idRol,
        rut = rut,
        nombre = nombre,
        correo = correo,
        telefono = telefono,
        activo = activo,
        rol = rol(),
        idEmpresa = idEmpresa,
        cargo = cargo,
        giro = giro,
        bio = bio,
        especialidades = especialidades,
        formacion = formacion,
        certificaciones = certificaciones,
        desde = desde,
        calificacion = calificacion
    )
}

data class LoginRemoto(val correo: String, val contrasena: String)

data class SolicitudRemota(
    val nombre: String,
    val apellido: String,
    val pais: String,
    val ciudad: String,
    val correo: String,
    val contrasena: String,
    val justificacion: String,
    val tipo: String = "INGRESO",
    val idUsuario: Int? = null,
    val campo: String = "",
    val valor: String = ""
)
