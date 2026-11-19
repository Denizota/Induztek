
package cl.duoc.induztek.navigation

import cl.duoc.induztek.model.Rol
import cl.duoc.induztek.model.TipoPrueba
import cl.duoc.induztek.model.Usuario


internal fun rutaEquipo(id: Int): String =
    "${Ruta.Equipo.ruta}/$id"

internal fun rutaPrueba(id: Int, tipo: TipoPrueba): String =
    "${Ruta.Prueba.ruta}/$id/${tipo.name}"

internal fun rutaActa(idVisita: Int, cliente: Boolean): String =
    "${Ruta.Acta.ruta}/$idVisita/${if (cliente) 1 else 0}"

internal fun rutaClienteEquipo(id: Int): String =
    "${Ruta.Cliente.ruta}/equipo/$id"

internal fun rutaTrasLogin(u: Usuario): String =
    if (u.rol == Rol.CLIENTE) Ruta.Cliente.ruta else Ruta.Inicio.ruta

internal fun rutaSuspension(idUsuario: Int): String =
    "${Ruta.Suspension.ruta}/$idUsuario"

internal fun rutaInstrumento(idInstrumento: Int): String =
    "${Ruta.Instrumento.ruta}/$idInstrumento"
