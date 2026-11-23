package cl.duoc.induztek.data

import cl.duoc.induztek.data.remote.ActivoApiRequest
import cl.duoc.induztek.data.remote.ActualizarApiRequest
import cl.duoc.induztek.data.remote.AmbientesApiRequest
import cl.duoc.induztek.data.remote.ApiSincronia
import cl.duoc.induztek.data.remote.CierreApiRequest
import cl.duoc.induztek.data.remote.EstadoProtocoloApiRequest
import cl.duoc.induztek.data.remote.EstadoSolicitudApiRequest
import cl.duoc.induztek.data.remote.ParticipanteApiRequest
import cl.duoc.induztek.data.remote.ProtocoloApi
import cl.duoc.induztek.data.remote.ProtocoloApiRequest
import cl.duoc.induztek.data.remote.PruebaApiRequest
import cl.duoc.induztek.data.remote.RegistroApiRequest
import cl.duoc.induztek.data.remote.SolicitudApiRequest
import cl.duoc.induztek.data.remote.UsuarioRemoto
import cl.duoc.induztek.data.remote.VisitaApi
import cl.duoc.induztek.data.remote.VisitaApiRequest
import retrofit2.Call
import retrofit2.HttpException
import java.io.IOException

internal class SubidaRemota(
    private val baseDatos: InduztekDbHelper,
    private val api: ApiSincronia
) {

    private var visitasDelServidor: List<VisitaApi>? = null
    private var usuariosDelServidor: List<UsuarioRemoto>? = null

    private class CorteRed : Exception()

    fun drenar(): Boolean {
        usuariosDelServidor = null
        for (cambio in baseDatos.cambiosPendientes()) {
            try {
                aplicar(cambio)
            } catch (error: CorteRed) {
                return false
            }
            baseDatos.borrarCambio(cambio.idCambio)
        }
        return true
    }

    private fun aplicar(cambio: CambioPendiente) {
        when (cambio.entidad) {
            CAMBIO_VISITA -> subirVisita(cambio.idLocal)
            CAMBIO_PARTICIPANTE -> subirParticipante(cambio.detalle)
            CAMBIO_PROTOCOLO -> subirProtocolo(cambio.idLocal)
            CAMBIO_PRUEBA -> subirPruebas(cambio.idLocal)
            CAMBIO_AMBIENTES -> subirAmbientes(cambio.idLocal)
            CAMBIO_ESTADO -> subirEstado(cambio.idLocal, cambio.detalle)
            CAMBIO_CIERRE -> subirCierre(cambio.idLocal)
            CAMBIO_USUARIO -> subirCuenta(cambio.detalle)
            CAMBIO_PERFIL -> subirPerfil(cambio.idLocal)
            CAMBIO_ACTIVO -> subirActivo(cambio.idLocal)
            CAMBIO_SOLICITUD -> subirSolicitud(cambio.idLocal, cambio.detalle)
            CAMBIO_SOLICITUD_ESTADO -> subirEstadoSolicitud(cambio.idLocal)
        }
    }

    private fun <T> pedir(llamada: Call<T>): T? {
        try {
            return llamada.execute().body()
        } catch (error: IOException) {
            throw CorteRed()
        } catch (error: HttpException) {
            if (error.code() >= 500) {
                throw CorteRed()
            }
            return null
        }
    }

    private fun subirVisita(idVisita: Int) {
        visitaRemota(idVisita)
    }

    private fun visitaRemota(idVisita: Int): Int? {
        val guardado = baseDatos.idRemotoDe("visita", "id_visita", idVisita)
        if (guardado != null) return guardado

        val local = baseDatos.fila(
            "SELECT numero_ot,id_instalacion,fecha,tipo_visita,id_ingeniero_cargo,descripcion_mantenimiento " +
                    "FROM visita WHERE id_visita=?",
            listOf(idVisita)
        ) ?: return null

        var remotas = visitasDelServidor
        if (remotas == null) {
            remotas = pedir(api.visitas()) ?: emptyList()
            visitasDelServidor = remotas
        }
        for (remota in remotas) {
            if (remota.idInstalacion == local.entero("id_instalacion") &&
                remota.fecha == local.texto("fecha") &&
                (remota.tipoVisita ?: "") == local.texto("tipo_visita") &&
                remota.idIngenieroCargo == local.enteroSiHay("id_ingeniero_cargo")
            ) {
                val id = remota.id ?: continue
                baseDatos.fijarIdRemoto("visita", "id_visita", idVisita, id)
                return id
            }
        }
        for (remota in remotas) {
            if (remota.numeroOt == local.texto("numero_ot")) {
                val id = remota.id ?: continue
                baseDatos.fijarIdRemoto("visita", "id_visita", idVisita, id)
                return id
            }
        }

        val creada = pedir(
            api.crearVisita(
                VisitaApiRequest(
                    idInstalacion = local.entero("id_instalacion"),
                    fecha = local.texto("fecha"),
                    tipoVisita = local.texto("tipo_visita"),
                    idIngenieroCargo = local.enteroSiHay("id_ingeniero_cargo"),
                    descripcionMantenimiento = local.texto("descripcion_mantenimiento")
                )
            )
        ) ?: return null
        val idCreado = creada.id ?: return null
        baseDatos.fijarIdRemoto("visita", "id_visita", idVisita, idCreado)
        val numero = creada.numeroOt
        if (!numero.isNullOrBlank()) {
            val ocupado = baseDatos.fila(
                "SELECT 1 FROM visita WHERE numero_ot=? AND id_visita<>?",
                listOf(numero, idVisita)
            )
            if (ocupado == null) {
                baseDatos.actualizar("visita", mapOf("numero_ot" to numero), "id_visita=?", listOf(idVisita))
            }
        }
        visitasDelServidor = null
        return idCreado
    }

    private fun subirParticipante(detalle: String) {
        val datos = gsonCambios.fromJson(detalle, DetalleParticipante::class.java)
        if (datos.idUsuario <= 0) return
        val idRemoto = visitaRemota(datos.idVisita) ?: return
        pedir(api.participante(idRemoto, ParticipanteApiRequest(idUsuarioRemoto(datos.idUsuario))))
    }

    private fun idUsuarioRemoto(idLocal: Int): Int {
        val mapeado = usuarioRemoto(idLocal)
        if (mapeado != null) return mapeado
        return idLocal
    }

    private fun subirProtocolo(idProtocolo: Int) {
        protocoloRemoto(idProtocolo)
    }

    private fun protocoloRemoto(idProtocolo: Int): Int? {
        val guardado = baseDatos.idRemotoDe("protocolo", "id_protocolo", idProtocolo)
        if (guardado != null) return guardado

        val local = baseDatos.fila(
            "SELECT id_visita,id_equipo,id_usuario_registra,temperatura_c,humedad_pct,observaciones " +
                    "FROM protocolo WHERE id_protocolo=?",
            listOf(idProtocolo)
        ) ?: return null
        val idVisitaRemota = visitaRemota(local.entero("id_visita")) ?: return null
        val idEquipo = local.entero("id_equipo")

        val deLaVisita = pedir(api.protocolos(idVisitaRemota, null)) ?: emptyList()
        for (remoto in deLaVisita) {
            if (remoto.idEquipo == idEquipo) {
                fijarProtocolo(idProtocolo, remoto)
                return remoto.id
            }
        }

        val respuesta = try {
            api.crearProtocolo(
                ProtocoloApiRequest(
                    idVisita = idVisitaRemota,
                    idEquipo = idEquipo,
                    idUsuarioRegistra = idUsuarioRemoto(local.entero("id_usuario_registra")),
                    temperaturaC = local.decimal("temperatura_c"),
                    humedadPct = local.decimal("humedad_pct"),
                    observaciones = local.texto("observaciones")
                )
            ).execute().body()
        } catch (error: IOException) {
            throw CorteRed()
        } catch (error: HttpException) {
            if (error.code() >= 500) {
                throw CorteRed()
            }
            if (error.code() == 409) {
                val repetidos = pedir(api.protocolos(idVisitaRemota, null)) ?: return null
                for (remoto in repetidos) {
                    if (remoto.idEquipo == idEquipo) {
                        fijarProtocolo(idProtocolo, remoto)
                        return remoto.id
                    }
                }
            }
            return null
        }

        val creado = respuesta ?: return null
        fijarProtocolo(idProtocolo, creado)
        return creado.id
    }

    private fun fijarProtocolo(idProtocolo: Int, remoto: ProtocoloApi) {
        val idRemoto = remoto.id ?: return
        val valores = mutableMapOf<String, Any?>("id_remoto" to idRemoto)
        val numero = remoto.numero
        if (!numero.isNullOrBlank()) {
            val local = baseDatos.fila(
                "SELECT numero FROM protocolo WHERE id_protocolo=?",
                listOf(idProtocolo)
            )
            if (local != null && local.texto("numero") != numero) {
                val ocupado = baseDatos.fila(
                    "SELECT 1 FROM protocolo WHERE numero=? AND id_protocolo<>?",
                    listOf(numero, idProtocolo)
                )
                if (ocupado == null) valores["numero"] = numero
            }
        }
        baseDatos.actualizar("protocolo", valores, "id_protocolo=?", listOf(idProtocolo))
    }

    private fun subirPruebas(idProtocolo: Int) {
        val idRemoto = protocoloRemoto(idProtocolo) ?: return
        val pruebas = baseDatos.filas(
            "SELECT id_prueba,codigo_tipo_prueba,elemento,realizada,motivo_no_realizada," +
                    "id_instrumento,resultado,comentario FROM prueba WHERE id_protocolo=?",
            listOf(idProtocolo)
        )
        for (prueba in pruebas) {
            val realizada = prueba.siNo("realizada")
            val mediciones = mutableMapOf<String, Double>()
            if (realizada) {
                val valores = baseDatos.filas(
                    "SELECT codigo_parametro,valor FROM medicion WHERE id_prueba=?",
                    listOf(prueba.entero("id_prueba"))
                )
                for (valor in valores) {
                    val numero = valor.texto("valor").toDoubleOrNull() ?: 0.0
                    mediciones[valor.texto("codigo_parametro")] = numero
                }
            }
            pedir(
                api.guardarPrueba(
                    idRemoto,
                    PruebaApiRequest(
                        codigoTipoPrueba = prueba.texto("codigo_tipo_prueba"),
                        elemento = prueba.texto("elemento"),
                        realizada = realizada,
                        motivoNoRealizada = prueba.texto("motivo_no_realizada"),
                        idInstrumento = prueba.enteroSiHay("id_instrumento"),
                        resultado = prueba["resultado"],
                        comentario = prueba.texto("comentario"),
                        mediciones = mediciones
                    )
                )
            )
        }
    }

    private fun subirAmbientes(idProtocolo: Int) {
        val idRemoto = protocoloRemoto(idProtocolo) ?: return
        val local = baseDatos.fila(
            "SELECT temperatura_c,humedad_pct,observaciones FROM protocolo WHERE id_protocolo=?",
            listOf(idProtocolo)
        ) ?: return
        pedir(
            api.ambientes(
                idRemoto,
                AmbientesApiRequest(
                    temperaturaC = local.decimal("temperatura_c"),
                    humedadPct = local.decimal("humedad_pct"),
                    observaciones = local.texto("observaciones")
                )
            )
        )
    }

    private fun subirEstado(idProtocolo: Int, detalle: String) {
        if (detalle.isBlank()) return
        val datos = gsonCambios.fromJson(detalle, DetalleEstado::class.java)
        if (datos.estado.isBlank() || datos.idUsuario <= 0) return
        val idRemoto = protocoloRemoto(idProtocolo) ?: return
        pedir(
            api.estadoProtocolo(
                idRemoto,
                EstadoProtocoloApiRequest(datos.estado, idUsuarioRemoto(datos.idUsuario), datos.motivo)
            )
        )
    }

    private fun subirCierre(idVisita: Int) {
        val idRemoto = visitaRemota(idVisita) ?: return
        val acta = baseDatos.fila(
            "SELECT receptor_nombre,receptor_rut,receptor_cargo FROM recepcion_acta WHERE id_visita=?",
            listOf(idVisita)
        ) ?: return
        pedir(
            api.cerrarVisita(
                idRemoto,
                CierreApiRequest(
                    receptorNombre = acta.texto("receptor_nombre"),
                    receptorRut = acta.texto("receptor_rut"),
                    receptorCargo = acta.texto("receptor_cargo")
                )
            )
        )
    }

    private fun subirCuenta(detalle: String) {
        if (detalle.isBlank()) return
        val datos = gsonCambios.fromJson(detalle, DetalleCuenta::class.java)
        if (datos.idUsuario <= 0) return
        if (baseDatos.idRemotoDe("usuario", "id_usuario", datos.idUsuario) != null) return
        val usuario = baseDatos.buscarUsuarioPorId(datos.idUsuario) ?: return
        val registrado = try {
            api.registrar(
                RegistroApiRequest(
                    rut = usuario.rut,
                    nombre = usuario.nombre,
                    correo = usuario.correo.ifBlank { null },
                    contrasena = datos.clave,
                    telefono = usuario.telefono,
                    idRol = usuario.idRol,
                    idEmpresa = usuario.idEmpresa,
                    cargo = usuario.cargo,
                    giro = usuario.giro
                )
            ).execute().body()
        } catch (error: IOException) {
            throw CorteRed()
        } catch (error: HttpException) {
            if (error.code() >= 500) throw CorteRed()
            null
        }
        if (registrado != null) {
            baseDatos.fijarIdRemoto("usuario", "id_usuario", datos.idUsuario, registrado.id)
            usuariosDelServidor = null
        } else {
            usuarioRemoto(datos.idUsuario)
        }
    }

    private fun usuarioRemoto(idLocal: Int): Int? {
        val guardado = baseDatos.idRemotoDe("usuario", "id_usuario", idLocal)
        if (guardado != null) return guardado
        val usuario = baseDatos.buscarUsuarioPorId(idLocal) ?: return null
        var remotos = usuariosDelServidor
        if (remotos == null) {
            remotos = pedir(api.usuarios()) ?: emptyList()
            usuariosDelServidor = remotos
        }
        for (remoto in remotos) {
            if (remoto.rut == usuario.rut) {
                baseDatos.fijarIdRemoto("usuario", "id_usuario", idLocal, remoto.id)
                return remoto.id
            }
        }
        if (usuario.correo.isBlank()) return null
        for (remoto in remotos) {
            if (remoto.correo == usuario.correo) {
                baseDatos.fijarIdRemoto("usuario", "id_usuario", idLocal, remoto.id)
                return remoto.id
            }
        }
        return null
    }

    private fun subirPerfil(idUsuario: Int) {
        val usuario = baseDatos.buscarUsuarioPorId(idUsuario) ?: return
        val idRemoto = usuarioRemoto(idUsuario) ?: return
        pedir(
            api.actualizar(
                idRemoto,
                ActualizarApiRequest(
                    nombre = usuario.nombre,
                    correo = usuario.correo.ifBlank { null },
                    telefono = usuario.telefono,
                    idRol = usuario.idRol,
                    idEmpresa = usuario.idEmpresa,
                    cargo = usuario.cargo,
                    giro = usuario.giro,
                    bio = usuario.bio,
                    especialidades = usuario.especialidades,
                    formacion = usuario.formacion,
                    certificaciones = usuario.certificaciones,
                    desde = usuario.desde,
                    calificacion = usuario.calificacion
                )
            )
        )
    }

    private fun subirActivo(idUsuario: Int) {
        val usuario = baseDatos.buscarUsuarioPorId(idUsuario) ?: return
        val idRemoto = usuarioRemoto(idUsuario) ?: return
        pedir(api.activo(idRemoto, ActivoApiRequest(usuario.activo)))
    }

    private fun subirSolicitud(idSolicitud: Int, detalle: String): Int? {
        val guardado = baseDatos.idRemotoDe("solicitud_acceso", "id_solicitud", idSolicitud)
        if (guardado != null) return guardado

        val local = baseDatos.fila(
            "SELECT sol_nombre,sol_apellido,sol_pais,sol_ciudad,sol_correo,sol_justificacion," +
                    "sol_tipo,sol_id_usuario,sol_campo,sol_valor FROM solicitud_acceso WHERE id_solicitud=?",
            listOf(idSolicitud)
        ) ?: return null
        var clave = "induztek123"
        if (detalle.isNotBlank()) {
            val datos = gsonCambios.fromJson(detalle, DetalleSolicitud::class.java)
            if (datos.clave.isNotBlank()) clave = datos.clave
        }

        val tipoSolicitud = local.texto("sol_tipo")
        var apellido = local.texto("sol_apellido")
        if (apellido.isBlank()) apellido = "—"
        var correoSolicitud = local.texto("sol_correo")
        if (correoSolicitud.isBlank()) correoSolicitud = "usuario$idSolicitud@induztek.local"
        var justificacion = local.texto("sol_justificacion")
        if (justificacion.isBlank()) {
            justificacion = "Solicitud de " + tipoSolicitud.lowercase() + " registrada desde la aplicación"
        }

        var idUsuarioSolicitud = local.enteroSiHay("sol_id_usuario")
        if (idUsuarioSolicitud != null && idUsuarioSolicitud > 0) {
            idUsuarioSolicitud = idUsuarioRemoto(idUsuarioSolicitud)
        }

        val creada = try {
            api.crearSolicitud(
                SolicitudApiRequest(
                    nombre = local.texto("sol_nombre"),
                    apellido = apellido,
                    pais = local.texto("sol_pais"),
                    ciudad = local.texto("sol_ciudad"),
                    correo = correoSolicitud,
                    contrasena = clave,
                    justificacion = justificacion,
                    tipo = tipoSolicitud,
                    idUsuario = idUsuarioSolicitud,
                    campo = local.texto("sol_campo"),
                    valor = local.texto("sol_valor")
                )
            ).execute().body()
        } catch (error: IOException) {
            throw CorteRed()
        } catch (error: HttpException) {
            if (error.code() >= 500) throw CorteRed()
            if (error.code() == 409) {
                return buscarSolicitudRemota(
                    idSolicitud,
                    correoSolicitud,
                    tipoSolicitud,
                    local.texto("sol_campo")
                )
            }
            return null
        }

        val idRemoto = creada?.id ?: return null
        baseDatos.fijarIdRemoto("solicitud_acceso", "id_solicitud", idSolicitud, idRemoto)
        return idRemoto
    }

    private fun buscarSolicitudRemota(
        idSolicitud: Int,
        correo: String,
        tipo: String,
        campo: String
    ): Int? {
        val remotas = pedir(api.solicitudes()) ?: return null
        for (remota in remotas) {
            if ((remota.correo ?: "") == correo &&
                (remota.tipo ?: "") == tipo &&
                (remota.campo ?: "") == campo
            ) {
                val id = remota.id ?: continue
                baseDatos.fijarIdRemoto("solicitud_acceso", "id_solicitud", idSolicitud, id)
                return id
            }
        }
        return null
    }

    private fun subirEstadoSolicitud(idSolicitud: Int) {
        val local = baseDatos.fila(
            "SELECT sol_estado FROM solicitud_acceso WHERE id_solicitud=?",
            listOf(idSolicitud)
        ) ?: return
        val idRemoto = baseDatos.idRemotoDe("solicitud_acceso", "id_solicitud", idSolicitud)
        var destino = idRemoto
        if (destino == null) {
            destino = subirSolicitud(idSolicitud, "") ?: return
        }
        pedir(api.estadoSolicitud(destino, EstadoSolicitudApiRequest(local.texto("sol_estado"))))
    }
}
