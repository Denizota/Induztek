package cl.duoc.induztek.viewmodel

import android.app.Application
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import cl.duoc.induztek.R
import cl.duoc.induztek.data.*
import cl.duoc.induztek.model.EstadoProtocolo
import cl.duoc.induztek.model.Instrumento
import cl.duoc.induztek.model.Prueba
import cl.duoc.induztek.model.Protocolo
import cl.duoc.induztek.model.TipoPrueba
import cl.duoc.induztek.model.Usuario

class EquipoViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {
    private val baseDatos = InduztekDbHelper.obtener(aplicacion)

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

    fun equipo(id: Int) = baseDatos.equipoPorId(id)

    fun calibracionDe(prueba: Prueba): Instrumento? {
        val idInstrumento = prueba.idInstrumento
        if (idInstrumento == null) {
            return null
        }
        val instrumentos = baseDatos.listarInstrumentos()
        for (instrumento in instrumentos) {
            if (instrumento.idInstrumento == idInstrumento) {
                return instrumento
            }
        }
        return null
    }

    fun motivoDevolucionGuardada(idProtocolo: Int) = baseDatos.ultimoMotivo(idProtocolo)

    fun visitaAbierta(idVisita: Int): Boolean {
        val visita = baseDatos.visitaPorId(idVisita)
        if (visita == null) return true
        return visita.fechaHoraCierre == null
    }

    fun refrescar(idEquipo: Int) {
        protocoloActual = protocoloEnCurso(idEquipo)
        val protocolo = protocoloActual
        if (protocolo == null) {
            pruebas = emptyList()
        } else {
            pruebas = baseDatos.listarPruebas(protocolo.idProtocolo)
        }
        tipos = baseDatos.tiposPruebaDeEquipo(idEquipo)
        historial = baseDatos.historialAislacion(idEquipo)
        mensaje = null
    }

    fun prepararBorrador(protocolo: Protocolo?) {
        var idProtocolo: Int? = null
        if (protocolo != null) idProtocolo = protocolo.idProtocolo
        if (borradorDe == idProtocolo) {
            return
        }
        borradorDe = idProtocolo
        var temperatura: Double? = null
        var humedadGuardada: Double? = null
        var textoObservaciones = ""
        if (protocolo != null) {
            temperatura = protocolo.temperaturaC
            humedadGuardada = protocolo.humedadPct
            textoObservaciones = protocolo.observaciones
        }
        temp.value = trimDecimal(temperatura)
        humedad.value = trimDecimal(humedadGuardada)
        observaciones.value = textoObservaciones
    }

    private fun trimDecimal(numero: Double?): String {
        if (numero == null) {
            return ""
        }
        if (numero % 1.0 == 0.0) {
            return numero.toInt().toString()
        }
        return numero.toString()
    }

    fun marcarRegistrado(
        protocolo: Protocolo, usuario: Usuario, tipos: List<TipoPrueba>, pruebas: List<Prueba>
    ) {
        val faltan = mutableListOf<String>()
        for (tipo in tipos) {
            var encontrada = false
            for (prueba in pruebas) {
                if (prueba.tipoPrueba == tipo) {
                    encontrada = true
                }
            }
            if (!encontrada) {
                faltan.add(tipo.nombre.substringBefore(" ("))
            }
        }
        val temperaturaAmbiente = temp.value.toDoubleOrNull()
        val humedadAmbiente = humedad.value.toDoubleOrNull()

        if (faltan.isNotEmpty()) {
            var listaFaltan = ""
            for (nombre in faltan) {
                if (listaFaltan.isNotEmpty()) listaFaltan = listaFaltan + ", "
                listaFaltan = listaFaltan + nombre
            }
            mensaje = texto(R.string.vm_faltan, listaFaltan)
            return
        }
        if (temperaturaAmbiente == null || humedadAmbiente == null) {
            mensaje = texto(R.string.vm_hu02)
            return
        }
        baseDatos.guardarAmbientes(protocolo.idProtocolo, temperaturaAmbiente, humedadAmbiente, observaciones.value)
        baseDatos.cambiarEstado(protocolo.idProtocolo, EstadoProtocolo.REGISTRADO, usuario.idUsuario)
        mensaje = texto(R.string.vm_registrado)
        refrescar(protocolo.idEquipo)
    }

    fun formalizar(protocolo: Protocolo, usuario: Usuario) {
        baseDatos.cambiarEstado(protocolo.idProtocolo, EstadoProtocolo.FORMALIZADO, usuario.idUsuario)
        mensaje = texto(R.string.vm_formalizado)
        refrescar(protocolo.idEquipo)
    }

    fun devolverConObservacion(protocolo: Protocolo, usuario: Usuario, motivo: String) {
        baseDatos.cambiarEstado(protocolo.idProtocolo, EstadoProtocolo.OBSERVADO, usuario.idUsuario, motivo)
        motivoDevolucion.value = null
        mensaje = texto(R.string.vm_devuelto)
        refrescar(protocolo.idEquipo)
    }

    private fun texto(@StringRes id: Int, vararg args: Any): String =
        getApplication<Application>().getString(id, *args)

    private fun protocoloEnCurso(idEquipo: Int): Protocolo? {
        val equipo = baseDatos.equipoPorId(idEquipo)
        if (equipo == null) {
            return null
        }
        val idVisita = baseDatos.visitaAbierta(equipo.idInstalacion)
        if (idVisita == null) {
            val todos = baseDatos.listarProtocolos()
            val delEquipo = mutableListOf<Protocolo>()
            for (protocolo in todos) {
                if (protocolo.idEquipo == idEquipo) {
                    delEquipo.add(protocolo)
                }
            }
            var elUltimo: Protocolo? = null
            for (protocolo in delEquipo) {
                if (elUltimo == null) {
                    elUltimo = protocolo
                } else if (protocolo.fechaHoraPrueba > elUltimo.fechaHoraPrueba) {
                    elUltimo = protocolo
                }
            }
            return elUltimo
        }
        val deLaVisita = baseDatos.protocolosDeVisita(idVisita)
        for (protocolo in deLaVisita) {
            if (protocolo.idEquipo == idEquipo) {
                return protocolo
            }
        }
        return null
    }
}
