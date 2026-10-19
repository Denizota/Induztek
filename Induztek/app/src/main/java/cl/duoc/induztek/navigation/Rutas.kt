package cl.duoc.induztek.navigation

sealed class Ruta(
    val ruta: String
){
    data object Login: Ruta("login")
    data object SolicitarAcceso: Ruta("solicitar_acceso")
}