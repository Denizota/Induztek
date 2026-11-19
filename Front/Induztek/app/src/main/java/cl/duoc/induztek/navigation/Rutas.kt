
package cl.duoc.induztek.navigation

sealed class Ruta(val ruta: String) {

    // ACCESO
    data object Login : Ruta("login")
    data object SolicitarAcceso : Ruta("solicitar_acceso")

    // PANTALLA PRINCIPAL
    data object Inicio : Ruta("inicio")
    data object Protocolos : Ruta("protocolos")

    // MENSAJES
    data object Mensajes : Ruta("mensajes")
    data object Chat : Ruta("chat")
    data object DetallesChat : Ruta("detalles_chat")

    // PERFIL
    data object Perfil : Ruta("perfil")
    data object EditarPerfil : Ruta("editar_perfil")

    // PERSONAL Y VALIDACION
    data object Personal : Ruta("personal")
    data object Validacion : Ruta("validacion")

    // EQUIPOS Y PROTOCOLOS
    data object Cliente : Ruta("cliente")
    data object Equipo : Ruta("equipo")
    data object Acta : Ruta("acta")
    data object Prueba : Ruta("prueba")
    data object Puente : Ruta("puente")
}

