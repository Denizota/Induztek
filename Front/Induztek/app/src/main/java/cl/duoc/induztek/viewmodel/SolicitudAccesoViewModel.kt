package cl.duoc.induztek.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import cl.duoc.induztek.data.InduztekDbHelper
import cl.duoc.induztek.data.Seguridad
import cl.duoc.induztek.model.SolicitudAccesoUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SolicitudAccesoViewModel(application: Application) : AndroidViewModel(application) {

    private val bd = InduztekDbHelper.obtener(application)
    private val _uiState = MutableStateFlow(SolicitudAccesoUiState())
    val uiState: StateFlow<SolicitudAccesoUiState> = _uiState.asStateFlow()

    fun onNombreCambia(valor: String){
        _uiState.update { it.copy(nombre = valor, errorNombre = null) }
    }

    fun onApellidoCambia(valor: String){
        _uiState.update { it.copy(apellido = valor, errorApellido = null) }
    }

    fun onPaisSeleccionado(valor: String){
        _uiState.update { it.copy(pais = valor) }
    }

    fun onCiudadSeleccionada(valor: String){
        _uiState.update { it.copy(ciudad = valor) }
    }

    fun onCorreoCambia(valor: String){
        _uiState.update { it.copy(correo = valor, errorCorreo = null) }
    }

    fun onContrasenaCambia(valor: String) {
        _uiState.update { it.copy(contrasena = valor, errorContrasena = null) }
    }

    fun onRepetirContrasenaCambia(valor: String) {
        _uiState.update { it.copy(repetirContrasena = valor, errorRepetirContrasena = null) }
    }

    fun onJustificacionCambia(valor: String) {
        _uiState.update { it.copy(justificacion = valor) }
    }

    fun onAlternarVisibilidadContrasena() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }

    fun onAlternarVisibilidadRepetirContrasena() {
        _uiState.update { it.copy(repetirContrasenaVisible = !it.repetirContrasenaVisible) }
    }


    fun onRegistrarse(): Boolean {
        val estado = _uiState.value
        val correo = estado.correo.trim().lowercase()

        val errorNombre = if (estado.nombre.isBlank()) "El nombre es obligatorio !" else null
        val errorApellido = if (estado.apellido.isBlank()) "El apellido es obligatorio !" else null
        val errorCorreo = when {
            !correo.contains("@") || !correo.contains(".") -> "Ingresa un correo valido !"
            bd.correoYaUsado(correo) -> "Este correo ya tiene cuenta o una solicitud pendiente !"
            else -> null
        }
        val errorContrasena = if (estado.contrasena.length < 6) "Minimo 6 caracteres !" else null
        val errorRepetirContrasena =
            if (estado.repetirContrasena != estado.contrasena) "Las contrasenas no coinciden" else null

        _uiState.update {
            it.copy(
                errorNombre = errorNombre,
                errorApellido = errorApellido,
                errorCorreo = errorCorreo,
                errorContrasena = errorContrasena,
                errorRepetirContrasena = errorRepetirContrasena
            )
        }

        val todoOk = listOf(errorNombre, errorApellido, errorCorreo, errorContrasena, errorRepetirContrasena)
            .all { it == null }
        if (!todoOk) return false

        val id = bd.insertarSolicitud(
            nombre = estado.nombre.trim(),
            apellido = estado.apellido.trim(),
            pais = estado.pais,
            ciudad = estado.ciudad,
            correo = correo,
            passwordHash = Seguridad.hashear(estado.contrasena),
            justificacion = estado.justificacion.trim()
        )
        if (id == -1L) return false

        // limpiacion d campos
        _uiState.value = SolicitudAccesoUiState()
        return true
    }
}
