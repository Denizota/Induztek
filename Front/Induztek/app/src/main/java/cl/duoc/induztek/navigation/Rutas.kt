
package cl.duoc.induztek.navigation

sealed class Ruta(val ruta: String) {

    data object Login : Ruta("login")
    data object SolicitarAcceso : Ruta("solicitar_acceso")

    data object Inicio : Ruta("inicio")
    data object Protocolos : Ruta("protocolos")


    data object Perfil : Ruta("perfil")
    data object EditarPerfil : Ruta("editar_perfil")

    data object Personal : Ruta("personal")
    data object AsignarTarea : Ruta("asignar_tarea")
    data object Validacion : Ruta("validacion")

    data object Instrumentos : Ruta("instrumentos")
    data object Suspension : Ruta("suspension")

    data object Consultas : Ruta("consultas")
    data object Solicitudes : Ruta("solicitudes")
    data object Instrumento : Ruta("instrumento")

    data object Cliente : Ruta("cliente")
    data object Equipo : Ruta("equipo")
    data object Acta : Ruta("acta")
    data object Prueba : Ruta("prueba")
    data object Puente : Ruta("puente")
}

