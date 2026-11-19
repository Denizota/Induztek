
package cl.duoc.induztek.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.induztek.model.Rol
import cl.duoc.induztek.model.Usuario
import cl.duoc.induztek.viewmodel.ColaboracionViewModel


@Composable
fun PerfilScreen(
    onIrEquipos: () -> Unit,
    onEditar: () -> Unit,
    onIrConsultas: () -> Unit,
    onSalir: () -> Unit,
    onReiniciar: () -> Unit,
    modeloPantalla: ColaboracionViewModel = viewModel()
) {
    val usuarioActual = modeloPantalla.usuarioEnSesion()
    if (usuarioActual == null) return
    val esCliente = usuarioActual.rol == Rol.CLIENTE

    var revision by rememberSaveable { mutableIntStateOf(0) }
    val numeros = remember(revision) { modeloPantalla.estadisticas(usuarioActual.idUsuario) }
    val equiposPlanta = remember(revision) { if (esCliente) modeloPantalla.equiposDeSuPlanta(usuarioActual.idEmpresa) else 0 }
    val actas = remember(revision) { if (esCliente) modeloPantalla.actasRecibidas(usuarioActual.idEmpresa) else 0 }
    val empresa = if (esCliente) modeloPantalla.empresaDel(usuarioActual.idEmpresa) else ""

    PerfilContenido(
        usuarioActual = usuarioActual,
        numeros = numeros,
        esCliente = esCliente,
        equiposPlanta = equiposPlanta,
        actas = actas,
        empresa = empresa,
        foto = imagenGuardada(usuarioActual.idUsuario, ImagenesPerfil.TIPO_FOTO),
        banner = imagenGuardada(usuarioActual.idUsuario, ImagenesPerfil.TIPO_BANNER),
        onEditar = onEditar,
        onIrConsultas = onIrConsultas,
        onSalir = onSalir,
        onReiniciar = {
            onReiniciar()
            revision++
        },
        onIrEquipos = onIrEquipos
    )
}

internal fun personaDePreview() = Usuario(
    idUsuario = 10,
    idRol = 2,
    rut = "16.482.905-7",
    nombre = "Thomas Peralta S.",
    correo = "tperalta@induztek.ejemplo",
    telefono = "+56 9 5551 2043",
    rol = Rol.INGENIERO,
    cargo = "Técnico de terreno",
    bio = "Técnico en pruebas eléctricas. Trabajo principalmente con transformadores y " +
            "celdas de media tensión en plantas industriales de la zona centro-sur.",
    especialidades = "Transformadores, Celdas MT, Reles, Interruptores",
    formacion = "Técnico en electricidad industrial",
    certificaciones = "SEC clase B",
    desde = 2019,
    calificacion = 5
)
