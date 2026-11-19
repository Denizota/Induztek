
package cl.duoc.induztek.data

import cl.duoc.induztek.data.remote.UsuarioRemoto

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
        "calificacion" to datos.calificacion
    )

    if (buscarUsuarioPorId(datos.id) != null) {
        return actualizar("usuario", valores, "id_usuario=?", listOf(datos.id))
    }

    val porRut = buscarUsuarioPorRut(datos.rut)
    if (porRut != null) {
        return actualizar("usuario", valores, "id_usuario=?", listOf(porRut.idUsuario))
    }

    if (!clave.isNullOrBlank()) {
        valores["password_hash"] = Seguridad.hashear(clave)
    }

    val conId = mutableMapOf<String, Any?>("id_usuario" to datos.id)
    conId.putAll(valores)
    return insertar("usuario", conId) != -1L
}

fun InduztekDbHelper.sincronizarUsuarios(lista: List<UsuarioRemoto>): Int {
    var guardados = 0
    for (datos in lista) {
        if (guardarUsuarioRemoto(datos)) guardados++
    }
    return guardados
}
