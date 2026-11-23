
package cl.duoc.induztek.data

import cl.duoc.induztek.data.remote.UsuarioRemoto
import cl.duoc.induztek.model.Usuario

fun InduztekDbHelper.guardarUsuarioRemoto(datos: UsuarioRemoto, clave: String? = null): Boolean {
    val valores = mutableMapOf<String, Any?>(
        "id_rol" to datos.idRol,
        "rut" to datos.rut,
        "nombre" to datos.nombre,
        "correo" to datos.correo,
        "telefono" to datos.telefono,
        "activo" to if (datos.activo) 1 else 0,
        "id_empresa" to datos.idEmpresa,
        "cargo" to datos.cargo,
        "giro" to datos.giro,
        "bio" to datos.bio,
        "especialidades" to datos.especialidades,
        "formacion" to datos.formacion,
        "certificaciones" to datos.certificaciones,
        "desde" to datos.desde,
        "calificacion" to datos.calificacion,
        "id_remoto" to datos.id
    )

    if (!clave.isNullOrBlank()) {
        valores["password_hash"] = Seguridad.hashear(clave)
    }

    val porRemoto = fila(
        "SELECT id_usuario FROM usuario WHERE id_remoto=?",
        listOf(datos.id)
    )
    if (porRemoto != null) {
        return actualizar("usuario", valores, "id_usuario=?", listOf(porRemoto.entero("id_usuario")))
    }

    val porRut = buscarUsuarioPorRut(datos.rut)
    if (porRut != null) {
        return actualizar("usuario", valores, "id_usuario=?", listOf(porRut.idUsuario))
    }

    var porCorreo: Usuario? = null
    if (datos.correo.isNotBlank()) {
        porCorreo = buscarUsuarioPorCorreo(datos.correo)
    }
    if (porCorreo != null) {
        return actualizar("usuario", valores, "id_usuario=?", listOf(porCorreo.idUsuario))
    }

    val conId = mutableMapOf<String, Any?>("id_usuario" to datos.id)
    conId.putAll(valores)
    if (insertar("usuario", conId) != -1L) {
        return true
    }
    return insertar("usuario", valores) != -1L
}

fun InduztekDbHelper.sincronizarUsuarios(lista: List<UsuarioRemoto>): Int {
    var guardados = 0
    for (datos in lista) {
        if (guardarUsuarioRemoto(datos)) guardados++
    }
    return guardados
}
