package cl.duoc.induztek.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.induztek.data.InduztekDbHelper
import cl.duoc.induztek.data.SesionPreferencias
import cl.duoc.induztek.data.buscarUsuarioPorCorreo
import cl.duoc.induztek.data.buscarUsuarioPorId
import cl.duoc.induztek.data.buscarUsuarioPorRut
import cl.duoc.induztek.data.guardarUsuarioRemoto
import cl.duoc.induztek.data.remote.Remoto
import cl.duoc.induztek.data.remote.ResultadoRemoto
import cl.duoc.induztek.data.remote.Sincronia
import cl.duoc.induztek.data.sincronizarUsuarios
import cl.duoc.induztek.data.verificarClave
import cl.duoc.induztek.model.LoginUiState
import cl.duoc.induztek.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class ResultadoGoogle { ENTRAR, NO_REGISTRADO, INACTIVO }

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val baseDatos = InduztekDbHelper.obtener(application)
    private val sesion = SesionPreferencias(application)
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private var ingresando = false

    fun onCorreoCambia(valor: String) {
        _uiState.value = _uiState.value.copy(correo = valor.trim(), errorCorreo = null)
    }

    fun onContrasenaCambia(valor: String) {
        _uiState.value = _uiState.value.copy(contrasena = valor, errorContrasena = null)
    }

    fun onAlternarVisibilidad() {
        _uiState.value = _uiState.value.copy(contrasenaVisible = !_uiState.value.contrasenaVisible)
    }

    fun onIngresar(alTerminar: (Boolean) -> Unit) {
        if (ingresando) return
        val estado = _uiState.value

        var errorCorreo: String? = null
        if (!estado.correo.contains("@") || !estado.correo.contains(".")) {
            errorCorreo = "Ingresa un correo válido !"
        }
        var errorContrasena: String? = null
        if (estado.contrasena.isBlank()) {
            errorContrasena = "Ingresa tu contraseña !"
        }

        if (errorCorreo != null || errorContrasena != null) {
            _uiState.value = _uiState.value.copy(errorCorreo = errorCorreo, errorContrasena = errorContrasena)
            alTerminar(false)
            return
        }

        ingresando = true
        viewModelScope.launch {
            val entro = withContext(Dispatchers.IO) { ingresar(estado.correo, estado.contrasena) }
            ingresando = false
            alTerminar(entro)
        }
    }

    private suspend fun ingresar(correo: String, contrasena: String): Boolean {
        when (val remoto = Remoto.ingresar(correo, contrasena)){
            is ResultadoRemoto.Ok -> {
                val usuario = remoto.usuario
                if (!usuario.activo){
                    _uiState.value = _uiState.value.copy(errorCorreo = "Su cuenta aun no esta activa !")
                    return false
                }
                baseDatos.guardarUsuarioRemoto(usuario, contrasena)
                baseDatos.sincronizarUsuarios(Remoto.usuarios())
                val local = baseDatos.buscarUsuarioPorCorreo(usuario.correo)
                    ?: baseDatos.buscarUsuarioPorRut(usuario.rut)
                sesion.guardar(local?.idUsuario ?: usuario.id)
                Sincronia.sincronizarAhora()
                return true

            }

            ResultadoRemoto.ClaveMala -> Unit
            ResultadoRemoto.Suspendido -> {
                _uiState.value = _uiState.value.copy(errorCorreo = "Tu cuenta no esta activa !")
                return false
            }
            ResultadoRemoto.SinConexion -> Unit
        }
        val usuario = baseDatos.buscarUsuarioPorCorreo(correo)
        if (usuario == null || !baseDatos.verificarClave(correo, contrasena)){
            _uiState.value = _uiState.value.copy(errorContrasena = "Correo o contraseña incorrectos !")
            return false
        }
        if (!usuario.activo){
            _uiState.value = _uiState.value.copy(errorCorreo = "Su cuenta no esta activa !")
            return false
        }

        sesion.guardar(usuario.idUsuario)
        Sincronia.sincronizarAhora()
        return true
    }

    fun onIngresarConGoogle(correo: String): ResultadoGoogle {
        val usuario = baseDatos.buscarUsuarioPorCorreo(correo)
        if (usuario == null) {
            return ResultadoGoogle.NO_REGISTRADO
        }
        if (!usuario.activo) {
            return ResultadoGoogle.INACTIVO
        }
        sesion.guardar(usuario.idUsuario)
        return ResultadoGoogle.ENTRAR
    }

    fun usuarioEnSesion(): Usuario? {
        val usuario = baseDatos.buscarUsuarioPorId(sesion.leer())
        if (usuario == null || !usuario.activo) {
            return null
        }
        return usuario
    }
}