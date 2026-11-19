package cl.duoc.induztek.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import cl.duoc.induztek.data.InduztekDbHelper
import cl.duoc.induztek.data.Seguridad
import cl.duoc.induztek.data.SesionPreferencias

import cl.duoc.induztek.model.LoginUiState
import cl.duoc.induztek.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel(application: Application) : AndroidViewModel(application) {


    private val bd = InduztekDbHelper.obtener(application)
    private val sesion = SesionPreferencias(application)
    private val _uiState = MutableStateFlow(LoginUiState(perfiles = bd.listarPerfilesRapidos()))
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
            "Ingresa un correo válido !" else null
        val errorContrasena = if (estado.contrasena.isBlank())
            "Ingresa tu contraseña !" else null

        if (errorCorreo != null || errorContrasena != null) {
            _uiState.update { it.copy(errorCorreo = errorCorreo, errorContrasena = errorContrasena) }
            return false
        }


        val usuario = bd.buscarUsuarioPorCorreo(estado.correo)
        if (usuario == null || !bd.verificarClave(estado.correo, estado.contrasena)) {

            _uiState.update { it.copy(errorContrasena = "Correo o contraseña incorrectos !") }
            return false
        }


        if (!usuario.activo) {
            _uiState.update { it.copy(errorCorreo = "Tu cuenta aún no está activa") }
            return false
        }

        sesion.guardar(usuario.idUsuario)
        return true
    }

    fun onEntrarComo(idUsuario: Int): Boolean {
        val usuario = bd.buscarUsuarioPorId(idUsuario)
        if (usuario == null || !usuario.activo) return false
        sesion.guardar(usuario.idUsuario)
        return true
    }

    /** El usuario que quedó guardado en la sesión; la navegación lo pide al saltar de pantalla. */
    fun usuarioEnSesion(): Usuario? = bd.buscarUsuarioPorId(sesion.leer())?.takeIf { it.activo }
}