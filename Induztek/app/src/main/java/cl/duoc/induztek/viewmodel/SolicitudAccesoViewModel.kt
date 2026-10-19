package cl.duoc.induztek.viewmodel

import androidx.lifecycle.ViewModel
import cl.duoc.induztek.model.SolicitudAccesoUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SolicitudAccesoViewModel : ViewModel() {
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


    //Para cuando haga la b a s e d e d a t o s, aqui se guardan
    //las solicitudes para que el jefe tecnico las revise y active la cuenta

    fun onRegistrarse(): Boolean{
        val estado = _uiState.value
        val errorNombre = if(estado.nombre.isBlank()) "El nombre es obligatorio !" else null
        val errorApellido = if (estado.apellido.isBlank()) "El apellido es obligatorio !" else null
        val errorCorreo = if (!estado.correo.contains("@") || !estado.correo.contains(".")) "Ingresa un correo valido !" else null
        val errorContrasena = if (estado.contrasena.length < 6)"Minimo 6 caracteres !" else null
        val errorRepetirContrasena = if(estado.repetirContrasena != estado.contrasena) "Las contrasenas no coinciden" else null

        _uiState.update {
            it.copy(
                errorNombre = errorNombre,
                errorApellido = errorApellido,
                errorCorreo = errorCorreo,
                errorContrasena = errorContrasena,
                errorRepetirContrasena = errorRepetirContrasena
            )
        }

        return listOf(errorNombre, errorApellido, errorCorreo, errorContrasena, errorRepetirContrasena)
            .all{ it == null}

    }
}