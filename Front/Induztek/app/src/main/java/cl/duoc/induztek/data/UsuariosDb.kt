
package cl.duoc.induztek.data

import cl.duoc.induztek.model.Rol
import cl.duoc.induztek.model.Usuario

internal const val COLUMNAS_USUARIO =
    "u.id_usuario,u.id_rol,u.rut,u.nombre,u.correo,u.telefono,u.activo,r.codigo," +
            "u.id_empresa,u.cargo,u.giro,u.bio,u.especialidades,u.formacion," +
            "u.certificaciones,u.desde,u.calificacion"

internal const val DESDE_USUARIO = " FROM usuario u JOIN rol r ON r.id_rol=u.id_rol "

fun InduztekDbHelper.buscarUsuarioPorCorreo(correo: String): Usuario? {
    val fila = fila(
        "SELECT $COLUMNAS_USUARIO $DESDE_USUARIO WHERE lower(u.correo)=lower(?)",
        listOf(correo.trim())
    )
    if (fila == null) return null
    return aUsuario(fila)
}

fun InduztekDbHelper.verificarClave(correo: String, clave: String): Boolean {
    val registro = fila(
        "SELECT password_hash FROM usuario WHERE lower(correo)=lower(?)",
        listOf(correo.trim())
    )
    if (registro == null) return false
    val cifrada = registro["password_hash"]
    if (cifrada == null) return false
    return Seguridad.verificar(clave, cifrada)
}

fun InduztekDbHelper.buscarUsuarioPorId(id: Int): Usuario? {
    val fila = fila(
        "SELECT $COLUMNAS_USUARIO $DESDE_USUARIO WHERE u.id_usuario=?",
        listOf(id)
    )
    if (fila == null) return null
    return aUsuario(fila)
}

fun InduztekDbHelper.correoYaUsado(correo: String): Boolean {
    val fila = fila(
        "SELECT 1 FROM usuario WHERE LOWER(correo) = LOWER(?) " +
                "UNION " +
                "SELECT 1 FROM solicitud_acceso WHERE LOWER(sol_correo) = LOWER(?) AND sol_estado = 'PENDIENTE'",
        listOf(correo, correo)
    )
    return fila != null
}

fun InduztekDbHelper.insertarSolicitud(
    nombre: String, apellido: String, pais: String, ciudad: String,
    correo: String, passwordHash: String, justificacion: String
): Long = insertar(
    "solicitud_acceso",
    mapOf(
        "sol_nombre" to nombre,
        "sol_apellido" to apellido,
        "sol_pais" to pais,
        "sol_ciudad" to ciudad,
        "sol_correo" to correo,
        "sol_password_hash" to passwordHash,
        "sol_justificacion" to justificacion
    )
)

fun InduztekDbHelper.listarPersonal(): List<Usuario> {
    val lista = mutableListOf<Usuario>()
    val registros = filas(
        "SELECT $COLUMNAS_USUARIO $DESDE_USUARIO " +
                "WHERE r.codigo <> 'CLIENTE' ORDER BY r.codigo,u.nombre"
    )
    for (registro in registros) {
        val persona = aUsuario(registro)
        lista.add(persona)
    }
    return lista
}

fun InduztekDbHelper.listarClientesPersona(): List<Usuario> {
    val lista = mutableListOf<Usuario>()
    val registros = filas(
        "SELECT $COLUMNAS_USUARIO $DESDE_USUARIO " +
                "WHERE r.codigo = 'CLIENTE' AND u.correo IS NOT NULL ORDER BY u.nombre"
    )
    for (registro in registros) {
        val persona = aUsuario(registro)
        lista.add(persona)
    }
    return lista
}

fun InduztekDbHelper.listarEmpresas(): List<Usuario> {
    val lista = mutableListOf<Usuario>()
    val registros = filas(
        "SELECT $COLUMNAS_USUARIO $DESDE_USUARIO " +
                "WHERE r.codigo = 'CLIENTE' AND u.correo IS NULL ORDER BY u.nombre"
    )
    for (registro in registros) {
        val empresa = aUsuario(registro)
        lista.add(empresa)
    }
    return lista
}

fun InduztekDbHelper.buscarUsuarioPorRut(rut: String): Usuario? {
    val fila = fila(
        "SELECT $COLUMNAS_USUARIO $DESDE_USUARIO WHERE u.rut=?",
        listOf(rut.trim())
    )
    if (fila == null) return null
    return aUsuario(fila)
}

fun InduztekDbHelper.guardarPerfil(idUsuario: Int, nombre: String, bio: String): Boolean =
    actualizar(
        "usuario",
        mapOf("nombre" to nombre, "bio" to bio),
        "id_usuario=?",
        listOf(idUsuario)
    )

fun InduztekDbHelper.marcarActivo(idUsuario: Int, activo: Boolean): Boolean {
    var numero = 0
    if (activo) numero = 1
    return actualizar(
        "usuario",
        mapOf("activo" to numero),
        "id_usuario=?",
        listOf(idUsuario)
    )
}

internal fun InduztekDbHelper.buscarIdRol(rol: Rol): Int? {
    val fila = fila("SELECT id_rol FROM rol WHERE codigo=?", listOf(rol.codigo))
    if (fila == null) return null
    return fila.enteroSiHay("id_rol")
}
