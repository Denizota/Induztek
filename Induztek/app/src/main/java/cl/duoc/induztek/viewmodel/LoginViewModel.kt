package cl.duoc.induztek.viewmodel

import androidx.lifecycle.ViewModel

import cl.duoc.induztek.model.LoginUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor.trim(), errorCorreo = null) }
    }

    fun onContrasenaCambia(valor: String) {
        _uiState.update { it.copy(contrasena = valor, errorContrasena = null) }
    }

    fun onAlternarVisibilidad() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }



    fun onIngresar(): Boolean {
        val estado = _uiState.value
        val errorCorreo = if (!estado.correo.contains("@") || !estado.correo.contains("."))
            "Ingresa un correo válido" else null
        val errorContrasena = if (estado.contrasena.isBlank())
            "Ingresa tu contraseña" else null

        _uiState.update { it.copy(errorCorreo = errorCorreo, errorContrasena = errorContrasena) }
        return errorCorreo == null && errorContrasena == null
    }
}