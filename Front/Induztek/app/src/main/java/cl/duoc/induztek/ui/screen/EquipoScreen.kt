
package cl.duoc.induztek.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.induztek.model.TipoPrueba
import cl.duoc.induztek.model.Usuario
import cl.duoc.induztek.viewmodel.EquipoViewModel

@Composable
fun EquipoScreen(
    idEquipo: Int,
    usuario: Usuario,
    onAtras: () -> Unit,
    onRegistrarPrueba: (TipoPrueba) -> Unit,
    onIrActa: (Int) -> Unit,
    modeloPantalla: EquipoViewModel = viewModel()
) {
    val sincronia = versionSincronia()
    val equipo = remember(idEquipo, sincronia) { modeloPantalla.equipo(idEquipo) }
    if (equipo == null) return
    LaunchedEffect(idEquipo, sincronia) { modeloPantalla.refrescar(idEquipo) }

    modeloPantalla.prepararBorrador(modeloPantalla.protocoloActual)

    EquipoContent(
        equipo = equipo, usuario = usuario, protocolo = modeloPantalla.protocoloActual,
        pruebas = modeloPantalla.pruebas, tipos = modeloPantalla.tipos,
        temp = modeloPantalla.temp.value, humedad = modeloPantalla.humedad.value,
        onTemp = { modeloPantalla.actualizarTemperatura(modeloPantalla.protocoloActual?.idProtocolo, it) },
        onHumedad = { modeloPantalla.actualizarHumedad(modeloPantalla.protocoloActual?.idProtocolo, it) },
        historial = modeloPantalla.historial, mensaje = modeloPantalla.mensaje, motivo = modeloPantalla.motivoDevolucion.value,
        onMotivo = { modeloPantalla.motivoDevolucion.value = it },
        onAbrirMotivo = { modeloPantalla.motivoDevolucion.value = "" },
        onCerrarMotivo = { modeloPantalla.motivoDevolucion.value = null },
        fechaCalibracion = { protocolo ->
            val instrumento = modeloPantalla.calibracionDe(protocolo)
            if (instrumento == null) null else instrumento.fechaVence
        },
        visitaAbierta = { id -> modeloPantalla.visitaAbierta(id) },
        ultimoMotivo = { id -> modeloPantalla.motivoDevolucionGuardada(id) },
        onAtras = onAtras, onRegistrarPrueba = onRegistrarPrueba, onIrActa = onIrActa,
        onMarcarRegistrado = { p, tipos, pruebas -> modeloPantalla.marcarRegistrado(p, usuario, tipos, pruebas) },
        onFormalizar = { protocolo -> modeloPantalla.formalizar(protocolo, usuario) },
        onDevolver = { p, m -> modeloPantalla.devolverConObservacion(p, usuario, m) }
    )
}
