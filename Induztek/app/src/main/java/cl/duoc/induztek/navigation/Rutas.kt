
package cl.duoc.induztek.navigation

sealed class Ruta(val ruta: String) {
    data object Login : Ruta("login")
    data object SolicitarAcceso : Ruta("solicitar_acceso")
    data object Inicio : Ruta("inicio")
    data object Protocolos : Ruta("protocolos")
    data object Mensajes : Ruta("mensajes")
    data object Perfil : Ruta("perfil")
    data object Cliente : Ruta("cliente")
    data object Equipo : Ruta("equipo")
    data object Acta : Ruta("acta")
    data object Prueba : Ruta("prueba")
    data object Puente : Ruta("puente")
}
