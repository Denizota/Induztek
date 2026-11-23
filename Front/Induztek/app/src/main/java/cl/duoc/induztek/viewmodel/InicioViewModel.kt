package cl.duoc.induztek.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import cl.duoc.induztek.R
import cl.duoc.induztek.data.*
import cl.duoc.induztek.model.Calculos
import cl.duoc.induztek.model.Equipo
import cl.duoc.induztek.model.InicioTarea
import cl.duoc.induztek.model.Instrumento
import cl.duoc.induztek.model.Rol
import cl.duoc.induztek.model.Solicitud
import cl.duoc.induztek.model.Usuario

class InicioViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {
    private val baseDatos = InduztekDbHelper.obtener(aplicacion)

    fun usuarioEnSesion(): Usuario? {
        val id = SesionPreferencias(getApplication()).leer()
        val usuario = baseDatos.buscarUsuarioPorId(id)
        if (usuario == null || !usuario.activo) {
            return null
        }
        return usuario
    }

    private fun idIngenieroFiltro(): Int? {
        val usuario = usuarioEnSesion()
        if (usuario == null || usuario.rol != Rol.INGENIERO) {
            return null
        }
        return usuario.idUsuario
    }

    fun soloMisEquipos(): Boolean {
        return idIngenieroFiltro() != null
    }

    fun pendientes(): List<InicioTarea> {
        return baseDatos.listarTareasInicio(antiguos = false, idIngeniero = idIngenieroFiltro())
    }

    fun antiguos(): List<InicioTarea> {
        return baseDatos.listarTareasInicio(antiguos = true, idIngeniero = idIngenieroFiltro())
    }

    fun reiniciarTodo() {
        baseDatos.reiniciarTodo()
        suspensiones().edit().clear().apply()
    }

    fun esJefeTecnico(): Boolean {
        val usuario = usuarioEnSesion()
        if (usuario == null) return false
        return usuario.rol == Rol.JEFE_TECNICO
    }

    fun instrumentos(): List<Instrumento> = baseDatos.listarInstrumentos()

    fun instrumento(id: Int): Instrumento? {
        val instrumentos = baseDatos.listarInstrumentos()
        for (instrumento in instrumentos) {
            if (instrumento.idInstrumento == id) {
                return instrumento
            }
        }
        return null
    }

    fun instrumentosVencidos(): Int {
        val instrumentos = baseDatos.listarInstrumentos()
        var vencidos = 0
        for (instrumento in instrumentos) {
            if (!instrumento.vigenteEn(fechaHoy())) {
                vencidos = vencidos + 1
            }
        }
        return vencidos
    }

    fun personal(): List<Usuario> = baseDatos.listarPersonal()

    fun clientes(): List<Usuario> = baseDatos.listarClientesPersona()

    fun ingenieros(): List<Usuario> {
        val lista = mutableListOf<Usuario>()
        for (persona in baseDatos.listarPersonal()) {
            if (persona.rol == Rol.INGENIERO && persona.activo) lista.add(persona)
        }
        return lista
    }

    fun equiposDeCliente(idCliente: Int): List<Equipo> {
        val cliente = baseDatos.buscarUsuarioPorId(idCliente)
        if (cliente == null) return emptyList()
        var idEmpresa = cliente.idUsuario
        if (cliente.idEmpresa != null) idEmpresa = cliente.idEmpresa
        return baseDatos.listarEquiposDeCliente(idEmpresa)
    }

    fun asignarTarea(idEquipo: Int, idIngeniero: Int): String {
        val jefe = usuarioEnSesion()
        if (jefe == null) return "No hay una sesión abierta."
        return baseDatos.asignarTarea(idEquipo, idIngeniero, jefe.idUsuario)
    }

    fun solicitudesPendientes(): Int = baseDatos.contarSolicitudesPendientes()

    fun usuario(id: Int): Usuario? = baseDatos.buscarUsuarioPorId(id)


    fun solicitudes(): List<Solicitud> = baseDatos.listarSolicitudesPendientes()

    fun solicitudesAprobadas(): List<Solicitud> = baseDatos.listarSolicitudesAprobadas()

    fun resolver(s: Solicitud, aprobar: Boolean): String? {
        val quien = s.nombreCompleto.ifBlank { s.correo }

        if (s.esCambio && aprobar) {
            val problema = validarCambio(s)
            if (problema != null) {
                return problema
            }
            if (!baseDatos.aplicarCambioSolicitud(s)) {
                return texto(R.string.sol_error)
            }
        }

        val estado = if (aprobar) "APROBADA" else "RECHAZADA"
        if (!baseDatos.cambiarEstadoSolicitud(s.idSolicitud, estado)) {
            return null
        }

        if (s.esCambio) {
            if (aprobar) {
                return texto(R.string.sol_cambio_aprobada, s.campo, quien)
            }
            return texto(R.string.sol_cambio_rechazada, s.campo, quien)
        }

        if (!s.esBaja) {
            if (aprobar) {
                return texto(R.string.sol_resuelta, texto(R.string.sol_aprobada))
            }
            return texto(R.string.sol_resuelta, texto(R.string.sol_rechazada))
        }

        if (aprobar) {
            val idUsuario = s.idUsuario
            if (idUsuario != null) {
                baseDatos.marcarActivo(idUsuario, activo = false)
            }
            return texto(R.string.sol_baja_aprobada, quien)
        }
        return texto(R.string.sol_baja_rechazada, quien)
    }

    private fun validarCambio(s: Solicitud): String? {
        if (s.valor.isBlank() || s.idUsuario == null) {
            return texto(R.string.sol_cambio_vacio)
        }
        val campo = s.campo.trim().lowercase()

        if (campo == "rut") {
            if (!Calculos.rutValido(s.valor)) {
                return texto(R.string.sol_rut_invalido)
            }
            val usuarioConEseRut = baseDatos.buscarUsuarioPorRut(s.valor)
            if (usuarioConEseRut != null && usuarioConEseRut.idUsuario != s.idUsuario) {
                return texto(R.string.sol_rut_repetido)
            }
            return null
        }

        if (campo == "correo") {
            val usuarioConEseCorreo = baseDatos.buscarUsuarioPorCorreo(s.valor)
            if (usuarioConEseCorreo != null && usuarioConEseCorreo.idUsuario != s.idUsuario) {
                return texto(R.string.sol_correo_repetido)
            }
            return null
        }

        return null
    }

    fun crearCuenta(s: Solicitud, rut: String, rol: Rol, clave: String, empresa: Int?): String? {
        val limpio = rut.trim()
        if (!Calculos.rutValido(limpio)) {
            return texto(R.string.sol_rut_invalido)
        }
        if (baseDatos.buscarUsuarioPorRut(limpio) != null) {
            return texto(R.string.sol_rut_repetido)
        }
        if (s.correo.isNotBlank() && baseDatos.buscarUsuarioPorCorreo(s.correo) != null) {
            return texto(R.string.sol_correo_repetido)
        }
        if (clave.length < 6) {
            return texto(R.string.sol_clave_corta)
        }
        if (rol == Rol.CLIENTE && empresa == null) {
            return texto(R.string.sol_empresa_falta)
        }

        var empresaParaLaCuenta: Int? = null
        if (rol == Rol.CLIENTE) {
            empresaParaLaCuenta = empresa
        }
        val idCuentaCreada = baseDatos.crearCuentaDesdeSolicitud(
            solicitud = s,
            rut = limpio,
            rol = rol,
            clave = clave,
            empresa = empresaParaLaCuenta
        )
        if (idCuentaCreada <= 0) {
            return texto(R.string.sol_error)
        }
        return null
    }

    fun empresas(): List<Usuario> = baseDatos.listarEmpresas()

    private fun texto(recurso: Int, arg: String = ""): String {
        val aplicacion = getApplication<Application>()
        if (arg.isEmpty()) {
            return aplicacion.getString(recurso)
        }
        return aplicacion.getString(recurso, arg)
    }

    private fun texto(recurso: Int, arg1: String, arg2: String): String {
        return getApplication<Application>().getString(recurso, arg1, arg2)
    }

    fun motivoDe(id: Int): String {
        val guardado = suspensiones().getString("motivo_$id", "")
        if (guardado == null) return ""
        return guardado
    }

    fun suspender(id: Int, motivo: String): Boolean {
        if (!baseDatos.marcarActivo(id, activo = false)) {
            return false
        }
        suspensiones().edit().putString("motivo_$id", motivo.trim()).apply()
        return true
    }

    fun reactivar(id: Int): Boolean {
        if (!baseDatos.marcarActivo(id, activo = true)) {
            return false
        }
        suspensiones().edit().putString("motivo_$id", null).apply()
        return true
    }

    private fun suspensiones() =
        getApplication<Application>().getSharedPreferences("cuentas_suspendidas", Context.MODE_PRIVATE)
}
