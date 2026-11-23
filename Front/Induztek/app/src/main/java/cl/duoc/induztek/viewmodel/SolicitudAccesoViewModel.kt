package cl.duoc.induztek.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duoc.induztek.data.*
import cl.duoc.induztek.data.remote.Remoto
import cl.duoc.induztek.data.remote.ResultadoSolicitud
import cl.duoc.induztek.data.remote.SolicitudRemota
import cl.duoc.induztek.model.SolicitudAccesoUiState
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SolicitudAccesoViewModel(application: Application) : AndroidViewModel(application) {

    private val baseDatos = InduztekDbHelper.obtener(application)
    private val _uiState = MutableStateFlow(SolicitudAccesoUiState())
    val uiState: StateFlow<SolicitudAccesoUiState> = _uiState.asStateFlow()

    private var enviando = false

    fun onNombreCambia(valor: String) {
        _uiState.value = _uiState.value.copy(nombre = valor, errorNombre = null)
    }

    fun onApellidoCambia(valor: String) {
        _uiState.value = _uiState.value.copy(apellido = valor, errorApellido = null)
    }

    fun onPaisSeleccionado(valor: String) {
        _uiState.value = _uiState.value.copy(pais = valor)
    }

    fun onCiudadSeleccionada(valor: String) {
        _uiState.value = _uiState.value.copy(ciudad = valor)
    }

    fun onCorreoCambia(valor: String) {
        _uiState.value = _uiState.value.copy(correo = valor, errorCorreo = null)
    }

    fun onContrasenaCambia(valor: String) {
        _uiState.value = _uiState.value.copy(contrasena = valor, errorContrasena = null)
    }

    fun onRepetirContrasenaCambia(valor: String) {
        _uiState.value = _uiState.value.copy(repetirContrasena = valor, errorRepetirContrasena = null)
    }

    fun onJustificacionCambia(valor: String) {
        _uiState.value = _uiState.value.copy(justificacion = valor)
    }

    fun onAlternarVisibilidadContrasena() {
        _uiState.value = _uiState.value.copy(contrasenaVisible = !_uiState.value.contrasenaVisible)
    }

    fun onAlternarVisibilidadRepetirContrasena() {
        _uiState.value = _uiState.value.copy(repetirContrasenaVisible = !_uiState.value.repetirContrasenaVisible)
    }

    fun onRegistrarse(alTerminar: (Boolean) -> Unit) {
        if (enviando) return
        val estado = _uiState.value
        val correo = estado.correo.trim().lowercase()

        var errorNombre: String? = null
        if (estado.nombre.isBlank()) {
            errorNombre = "El nombre es obligatorio !"
        }
        var errorApellido: String? = null
        if (estado.apellido.isBlank()) {
            errorApellido = "El apellido es obligatorio !"
        }
        var errorCorreo: String? = null
        if (!correo.contains("@") || !correo.contains(".")) {
            errorCorreo = "Ingresa un correo valido !"
        } else if (baseDatos.correoYaUsado(correo)) {
            errorCorreo = "Este correo ya tiene cuenta o una solicitud pendiente !"
        }
        var errorContrasena: String? = null
        if (estado.contrasena.length < 6) {
            errorContrasena = "Minimo 6 caracteres !"
        }
        var errorRepetirContrasena: String? = null
        if (estado.repetirContrasena != estado.contrasena) {
            errorRepetirContrasena = "Las contrasenas no coinciden"
        }

        _uiState.value = _uiState.value.copy(
            errorNombre = errorNombre,
            errorApellido = errorApellido,
            errorCorreo = errorCorreo,
            errorContrasena = errorContrasena,
            errorRepetirContrasena = errorRepetirContrasena
        )

        if (errorNombre != null || errorApellido != null || errorCorreo != null ||
            errorContrasena != null || errorRepetirContrasena != null
        ) {
            alTerminar(false)
            return
        }

        enviando = true
        viewModelScope.launch {
            val datos = SolicitudRemota(
                nombre = estado.nombre.trim(),
                apellido = estado.apellido.trim(),
                pais = estado.pais,
                ciudad = estado.ciudad,
                correo = correo,
                contrasena = estado.contrasena,
                justificacion = estado.justificacion.trim()
            )

            when (val respuesta = Remoto.crearSolicitud(datos)) {
                is ResultadoSolicitud.Ok -> {
                    withContext(Dispatchers.IO) { guardarSolicitudLocal(estado, correo, respuesta.id) }
                    _uiState.value = SolicitudAccesoUiState()
                    enviando = false
                    alTerminar(true)
                }

                ResultadoSolicitud.CorreoUsado -> {
                    _uiState.value = _uiState.value.copy(
                        errorCorreo = "Este correo ya tiene cuenta o una solicitud pendiente !"
                    )
                    enviando = false
                    alTerminar(false)
                }

                ResultadoSolicitud.SinConexion -> {
                    val guardada = withContext(Dispatchers.IO) { guardarSolicitudLocal(estado, correo) }
                    if (guardada) _uiState.value = SolicitudAccesoUiState()
                    enviando = false
                    alTerminar(guardada)
                }
            }
        }
    }

    private fun guardarSolicitudLocal(
        estado: SolicitudAccesoUiState, correo: String, idRemoto: Int = 0
    ): Boolean {
        val id = baseDatos.insertarSolicitud(
            nombre = estado.nombre.trim(),
            apellido = estado.apellido.trim(),
            pais = estado.pais,
            ciudad = estado.ciudad,
            correo = correo,
            passwordHash = Seguridad.hashear(estado.contrasena),
            justificacion = estado.justificacion.trim(),
            contrasena = estado.contrasena
        )
        if (id > 0 && idRemoto > 0) {
            baseDatos.actualizar(
                "solicitud_acceso",
                mapOf("id_remoto" to idRemoto),
                "id_solicitud=?",
                listOf(id.toInt())
            )
        }
        return id != -1L
    }
}
