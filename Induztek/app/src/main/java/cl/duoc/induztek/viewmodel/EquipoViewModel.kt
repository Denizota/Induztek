package cl.duoc.induztek.viewmodel



import android.app.Application
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import cl.duoc.induztek.R
import cl.duoc.induztek.data.InduztekDbHelper
import cl.duoc.induztek.model.EstadoProtocolo
import cl.duoc.induztek.model.Instrumento
import cl.duoc.induztek.model.Prueba
import cl.duoc.induztek.model.Protocolo
import cl.duoc.induztek.model.TipoPrueba
import cl.duoc.induztek.model.Usuario



class EquipoViewModel(app: Application) : AndroidViewModel(app) {
    private val db = InduztekDbHelper.obtener(app)

    val temp = mutableStateOf("")
    val humedad = mutableStateOf("")
    val observaciones = mutableStateOf("")
    val motivoDevolucion = mutableStateOf<String?>(null)
    var mensaje: String? by mutableStateOf(null)
        private set

    var protocoloActual: Protocolo? by mutableStateOf(null)
        private set
    var pruebas: List<Prueba> by mutableStateOf(emptyList())
        private set
    var tipos: List<TipoPrueba> by mutableStateOf(emptyList())
        private set
    var historial: List<Triple<String, String, Double>> by mutableStateOf(emptyList())
        private set

    private var borradorDe: Int? = null

    fun equipo(id: Int) = db.equipoPorId(id)
    fun calibracionDe(prueba: Prueba): Instrumento? =
        prueba.idInstrumento?.let { id -> db.listarInstrumentos().firstOrNull { it.idInstrumento == id } }

    fun motivoDevolucionGuardada(idProtocolo: Int) = db.ultimoMotivo(idProtocolo)



    fun visitaAbierta(idVisita: Int) = db.visitaPorId(idVisita)?.fechaHoraCierre == null

    fun refrescar(idEquipo: Int) {
        protocoloActual = protocoloEnCurso(idEquipo)
        pruebas = protocoloActual?.let { db.listarPruebas(it.idProtocolo) } ?: emptyList()
        tipos = db.tiposPruebaDeEquipo(idEquipo)
        historial = db.historialAislacion(idEquipo)
        mensaje = null
    }



    fun prepararBorrador(p: Protocolo?) {
        val id = p?.idProtocolo
        if (borradorDe == id) return
        borradorDe = id
        temp.value = p?.temperaturaC.trimDecimal()
        humedad.value = p?.humedadPct.trimDecimal()
        observaciones.value = p?.observaciones ?: ""
    }

    private fun Double?.trimDecimal(): String =
        this?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: ""

    fun marcarRegistrado(
        p: Protocolo, usuario: Usuario, tipos: List<TipoPrueba>, pruebas: List<Prueba>
    ) {
        val faltan = tipos.filter { t -> pruebas.none { it.tipoPrueba == t } }
        val t = temp.value.toDoubleOrNull()
        val h = humedad.value.toDoubleOrNull()
        when {
            faltan.isNotEmpty() ->
                mensaje = txt(R.string.vm_faltan, faltan.joinToString { it.nombre.substringBefore(" (") })
            t == null || h == null ->
                mensaje = txt(R.string.vm_hu02)
            else -> {
                db.guardarAmbientes(p.idProtocolo, t, h, observaciones.value)
                db.cambiarEstado(p.idProtocolo, EstadoProtocolo.REGISTRADO, usuario.idUsuario)
                mensaje = txt(R.string.vm_registrado)
                refrescar(p.idEquipo)
            }
        }
    }

    fun formalizar(p: Protocolo, usuario: Usuario) {
        db.cambiarEstado(p.idProtocolo, EstadoProtocolo.FORMALIZADO, usuario.idUsuario)
        mensaje = txt(R.string.vm_formalizado)
        refrescar(p.idEquipo)
    }

    fun devolverConObservacion(p: Protocolo, usuario: Usuario, motivo: String) {
        db.cambiarEstado(p.idProtocolo, EstadoProtocolo.OBSERVADO, usuario.idUsuario, motivo)
        motivoDevolucion.value = null
        mensaje = txt(R.string.vm_devuelto)
        refrescar(p.idEquipo)
    }

    private fun txt(@StringRes id: Int, vararg args: Any): String =
        getApplication<Application>().getString(id, *args)


    private fun protocoloEnCurso(idEquipo: Int): Protocolo? {
        val equipo = db.equipoPorId(idEquipo) ?: return null
        val idVisita = db.visitaAbierta(equipo.idInstalacion)
            ?: return db.listarProtocolos().filter { it.idEquipo == idEquipo }.maxByOrNull { it.fechaHoraPrueba }
        return db.protocolosDeVisita(idVisita).firstOrNull { it.idEquipo == idEquipo }
    }
}
