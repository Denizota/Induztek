package cl.duoc.induztek.data

import cl.duoc.induztek.data.remote.EquipoApi
import cl.duoc.induztek.data.remote.HistorialApi
import cl.duoc.induztek.data.remote.InstalacionApi
import cl.duoc.induztek.data.remote.InstrumentoApi
import cl.duoc.induztek.data.remote.ProtocoloApi
import cl.duoc.induztek.data.remote.ProtocoloDetalleApi
import cl.duoc.induztek.data.remote.PruebaApi
import cl.duoc.induztek.data.remote.SolicitudApi
import cl.duoc.induztek.data.remote.TipoEquipoApi
import cl.duoc.induztek.data.remote.TipoPruebaApi
import cl.duoc.induztek.data.remote.UsuarioRemoto
import cl.duoc.induztek.data.remote.VisitaApi
import cl.duoc.induztek.data.remote.VisitaDetalleApi

internal class PaqueteRemoto(
    val usuarios: List<UsuarioRemoto> = emptyList(),
    val solicitudes: List<SolicitudApi> = emptyList(),
    val visitas: List<VisitaApi> = emptyList(),
    val detallesVisita: Map<Int, VisitaDetalleApi> = emptyMap(),
    val protocolos: List<ProtocoloApi> = emptyList(),
    val detallesProtocolo: Map<Int, ProtocoloDetalleApi> = emptyMap(),
    val equipos: List<EquipoApi> = emptyList(),
    val instalaciones: List<InstalacionApi> = emptyList(),
    val instrumentos: List<InstrumentoApi> = emptyList(),
    val tiposEquipo: List<TipoEquipoApi> = emptyList(),
    val tiposPrueba: List<TipoPruebaApi> = emptyList()
)

internal fun InduztekDbHelper.mezclarPaquete(paquete: PaqueteRemoto) {
    sincronizarUsuarios(paquete.usuarios)
    mezclarCatalogo(paquete)
    mezclarSolicitudes(paquete.solicitudes)
    mezclarVisitas(paquete.visitas, paquete.detallesVisita)
    mezclarProtocolos(paquete.protocolos, paquete.detallesProtocolo)
}

private fun InduztekDbHelper.usuarioLocalDe(idRemoto: Int?): Int? {
    if (idRemoto == null) return null
    val encontrado = fila("SELECT id_usuario FROM usuario WHERE id_remoto=?", listOf(idRemoto))
    if (encontrado != null) return encontrado.entero("id_usuario")
    return idRemoto
}

private fun InduztekDbHelper.mezclarCatalogo(paquete: PaqueteRemoto) {
    for (tipo in paquete.tiposEquipo) {
        val id = tipo.id ?: continue
        ejecutar(
            "INSERT OR REPLACE INTO tipo_equipo(id_tipo_equipo,nombre,clasificacion) VALUES(?,?,?)",
            listOf(id, tipo.nombre ?: "", tipo.clasificacion ?: "")
        )
    }
    for (tipo in paquete.tiposPrueba) {
        val id = tipo.id ?: continue
        ejecutar(
            "INSERT OR REPLACE INTO tipo_prueba(id_tipo_prueba,codigo,nombre) VALUES(?,?,?)",
            listOf(id, tipo.codigo ?: "", tipo.nombre ?: "")
        )
    }
    for (instalacion in paquete.instalaciones) {
        val id = instalacion.id ?: continue
        val valores = mapOf(
            "id_usuario_cliente" to (instalacion.idUsuarioCliente ?: 0),
            "nombre" to (instalacion.nombre ?: ""),
            "ubicacion" to (instalacion.ubicacion ?: ""),
            "comuna" to (instalacion.comuna ?: ""),
            "observaciones_acceso" to (instalacion.observacionesAcceso ?: "")
        )
        if (!actualizar("instalacion", valores, "id_instalacion=?", listOf(id))) {
            val conId = valores.toMutableMap()
            conId["id_instalacion"] = id
            insertar("instalacion", conId)
        }
    }
    for (equipo in paquete.equipos) {
        val id = equipo.id ?: continue
        val valores = mapOf(
            "id_instalacion" to (equipo.idInstalacion ?: 0),
            "tag" to (equipo.tag ?: ""),
            "id_tipo_equipo" to (equipo.idTipoEquipo ?: 0),
            "marca" to (equipo.marca ?: ""),
            "modelo" to (equipo.modelo ?: ""),
            "numero_serie" to (equipo.numeroSerie ?: ""),
            "anio_fabricacion" to equipo.anioFabricacion,
            "potencia_kva" to equipo.potenciaKva,
            "tension_primaria_kv" to equipo.tensionPrimariaKv,
            "tension_secundaria_kv" to equipo.tensionSecundariaKv,
            "corriente_nominal_a" to equipo.corrienteNominalA,
            "max_contactos_uohm" to equipo.maxContactosUohm,
            "observaciones" to (equipo.observaciones ?: "")
        )
        if (!actualizar("equipo", valores, "id_equipo=?", listOf(id))) {
            val conId = valores.toMutableMap()
            conId["id_equipo"] = id
            insertar("equipo", conId)
        }
    }
    for (instrumento in paquete.instrumentos) {
        val id = instrumento.id ?: continue
        val valores = mapOf(
            "nombre" to (instrumento.nombre ?: ""),
            "marca" to (instrumento.marca ?: ""),
            "modelo" to (instrumento.modelo ?: ""),
            "numero_serie" to (instrumento.numeroSerie ?: ""),
            "fecha_ultima_cal" to instrumento.fechaUltimaCal,
            "fecha_vence" to instrumento.fechaVence
        )
        if (actualizar("instrumento", valores, "id_instrumento=?", listOf(id))) {
            continue
        }
        if (actualizar("instrumento", valores, "numero_serie=?", listOf(instrumento.numeroSerie ?: ""))) {
            continue
        }
        val conId = valores.toMutableMap<String, Any?>()
        conId["id_instrumento"] = id
        insertar("instrumento", conId)
    }
}

private fun InduztekDbHelper.mezclarSolicitudes(lista: List<SolicitudApi>) {
    for (solicitud in lista) {
        val idRemoto = solicitud.id ?: continue
        var local = fila(
            "SELECT id_solicitud,sol_estado,id_remoto FROM solicitud_acceso WHERE id_remoto=?",
            listOf(idRemoto)
        )
        if (local == null) {
            local = fila(
                "SELECT id_solicitud,sol_estado,id_remoto FROM solicitud_acceso " +
                        "WHERE sol_correo=? AND sol_tipo=? AND sol_campo=? AND id_remoto IS NULL",
                listOf(solicitud.correo ?: "", solicitud.tipo ?: "INGRESO", solicitud.campo ?: "")
            )
        }
        if (local == null) {
            insertar(
                "solicitud_acceso",
                mapOf(
                    "sol_nombre" to (solicitud.nombre ?: ""),
                    "sol_apellido" to (solicitud.apellido ?: ""),
                    "sol_pais" to (solicitud.pais ?: ""),
                    "sol_ciudad" to (solicitud.ciudad ?: ""),
                    "sol_correo" to (solicitud.correo ?: ""),
                    "sol_password_hash" to "",
                    "sol_justificacion" to (solicitud.justificacion ?: ""),
                    "sol_estado" to (solicitud.estado ?: "PENDIENTE"),
                    "sol_tipo" to (solicitud.tipo ?: "INGRESO"),
                    "sol_id_usuario" to usuarioLocalDe(solicitud.idUsuario),
                    "sol_campo" to (solicitud.campo ?: ""),
                    "sol_valor" to (solicitud.valor ?: ""),
                    "id_remoto" to idRemoto
                )
            )
            continue
        }

        val idLocal = local.entero("id_solicitud")
        if (local.enteroSiHay("id_remoto") == null) {
            actualizar(
                "solicitud_acceso",
                mapOf("id_remoto" to idRemoto),
                "id_solicitud=?",
                listOf(idLocal)
            )
        }
        val valores = mutableMapOf<String, Any?>(
            "sol_nombre" to (solicitud.nombre ?: ""),
            "sol_apellido" to (solicitud.apellido ?: ""),
            "sol_pais" to (solicitud.pais ?: ""),
            "sol_ciudad" to (solicitud.ciudad ?: ""),
            "sol_correo" to (solicitud.correo ?: ""),
            "sol_justificacion" to (solicitud.justificacion ?: ""),
            "sol_tipo" to (solicitud.tipo ?: "INGRESO"),
            "sol_id_usuario" to usuarioLocalDe(solicitud.idUsuario),
            "sol_campo" to (solicitud.campo ?: ""),
            "sol_valor" to (solicitud.valor ?: "")
        )
        if (!hayCambio(CAMBIO_SOLICITUD_ESTADO, idLocal) && !hayCambio(CAMBIO_SOLICITUD, idLocal)) {
            valores["sol_estado"] = solicitud.estado ?: "PENDIENTE"
        }
        actualizar("solicitud_acceso", valores, "id_solicitud=?", listOf(idLocal))
    }
}

private fun InduztekDbHelper.mezclarVisitas(
    lista: List<VisitaApi>,
    detalles: Map<Int, VisitaDetalleApi>
) {
    for (visita in lista) {
        val idRemoto = visita.id ?: continue
        var local = fila(
            "SELECT id_visita,id_remoto FROM visita WHERE id_remoto=?",
            listOf(idRemoto)
        )
        if (local == null) {
            local = fila(
                "SELECT id_visita,id_remoto FROM visita WHERE numero_ot=? AND id_remoto IS NULL",
                listOf(visita.numeroOt ?: "")
            )
        }
        if (local == null) {
            local = fila(
                "SELECT id_visita,id_remoto FROM visita " +
                        "WHERE id_instalacion=? AND fecha=? AND tipo_visita=? " +
                        "AND IFNULL(id_ingeniero_cargo,0)=? AND id_remoto IS NULL",
                listOf(
                    visita.idInstalacion ?: 0,
                    visita.fecha ?: "",
                    visita.tipoVisita ?: "SOLO_PRUEBAS",
                    visita.idIngenieroCargo ?: 0
                )
            )
        }

        var idLocal = 0
        if (local == null) {
            idLocal = insertar(
                "visita",
                mapOf(
                    "numero_ot" to (visita.numeroOt ?: "OT-${idRemoto}"),
                    "id_instalacion" to (visita.idInstalacion ?: 0),
                    "fecha" to (visita.fecha ?: fechaHoy()),
                    "tipo_visita" to (visita.tipoVisita ?: "SOLO_PRUEBAS"),
                    "id_ingeniero_cargo" to usuarioLocalDe(visita.idIngenieroCargo),
                    "fecha_hora_cierre" to visita.fechaHoraCierre,
                    "descripcion_mantenimiento" to (visita.descripcionMantenimiento ?: ""),
                    "id_remoto" to idRemoto
                )
            ).toInt()
            if (idLocal <= 0) continue
        } else {
            idLocal = local.entero("id_visita")
            if (local.enteroSiHay("id_remoto") == null) {
                actualizar("visita", mapOf("id_remoto" to idRemoto), "id_visita=?", listOf(idLocal))
            }
            if (!hayCambio(CAMBIO_VISITA, idLocal) && !hayCambio(CAMBIO_CIERRE, idLocal)) {
                actualizar(
                    "visita",
                    mapOf(
                        "id_ingeniero_cargo" to usuarioLocalDe(visita.idIngenieroCargo),
                        "fecha_hora_cierre" to visita.fechaHoraCierre,
                        "descripcion_mantenimiento" to (visita.descripcionMantenimiento ?: "")
                    ),
                    "id_visita=?",
                    listOf(idLocal)
                )
            }
        }

        val detalle = detalles[idRemoto] ?: continue
        for (idUsuario in detalle.participantes.orEmpty()) {
            ejecutar(
                "INSERT OR IGNORE INTO visita_participante(id_visita,id_usuario) VALUES(?,?)",
                listOf(idLocal, usuarioLocalDe(idUsuario) ?: idUsuario)
            )
        }
        val acta = detalle.acta
        if (acta != null && !hayCambio(CAMBIO_CIERRE, idLocal)) {
            ejecutar(
                "INSERT OR REPLACE INTO recepcion_acta VALUES(?,?,?,?,?)",
                listOf(
                    idLocal,
                    acta.receptorNombre ?: "",
                    acta.receptorRut ?: "",
                    acta.receptorCargo ?: "",
                    acta.fechaHoraRecepcion ?: fechaHoraAhora()
                )
            )
        }
    }
}

private fun InduztekDbHelper.mezclarProtocolos(
    lista: List<ProtocoloApi>,
    detalles: Map<Int, ProtocoloDetalleApi>
) {
    for (protocolo in lista) {
        val idRemoto = protocolo.id ?: continue
        val idVisitaRemota = protocolo.idVisita ?: continue
        val visitaLocal = fila("SELECT id_visita FROM visita WHERE id_remoto=?", listOf(idVisitaRemota))
        if (visitaLocal == null) continue
        val idVisita = visitaLocal.entero("id_visita")
        val idEquipo = protocolo.idEquipo ?: continue

        var local = fila("SELECT id_protocolo,id_remoto FROM protocolo WHERE id_remoto=?", listOf(idRemoto))
        if (local == null) {
            local = fila(
                "SELECT id_protocolo,id_remoto FROM protocolo WHERE id_visita=? AND id_equipo=? AND id_remoto IS NULL",
                listOf(idVisita, idEquipo)
            )
        }

        var idLocal = 0
        if (local == null) {
            val numero = protocolo.numero ?: continue
            val ocupado = fila("SELECT 1 FROM protocolo WHERE numero=?", listOf(numero))
            if (ocupado != null) continue
            idLocal = insertar(
                "protocolo",
                mapOf(
                    "numero" to numero,
                    "id_visita" to idVisita,
                    "id_equipo" to idEquipo,
                    "id_usuario_registra" to (usuarioLocalDe(protocolo.idUsuarioRegistra) ?: 0),
                    "fecha_hora_prueba" to (protocolo.fechaHoraPrueba ?: fechaHoraAhora()),
                    "temperatura_c" to protocolo.temperaturaC,
                    "humedad_pct" to protocolo.humedadPct,
                    "estado" to (protocolo.estado ?: "BORRADOR"),
                    "observaciones" to (protocolo.observaciones ?: ""),
                    "id_remoto" to idRemoto
                )
            ).toInt()
            if (idLocal <= 0) continue
        } else {
            idLocal = local.entero("id_protocolo")
            if (local.enteroSiHay("id_remoto") == null) {
                actualizar("protocolo", mapOf("id_remoto" to idRemoto), "id_protocolo=?", listOf(idLocal))
            }
            val valores = mutableMapOf<String, Any?>()
            if (!hayCambio(CAMBIO_PROTOCOLO, idLocal)) {
                valores["id_usuario_registra"] = usuarioLocalDe(protocolo.idUsuarioRegistra) ?: 0
                valores["fecha_hora_prueba"] = protocolo.fechaHoraPrueba ?: fechaHoraAhora()
            }
            if (!hayCambio(CAMBIO_ESTADO, idLocal)) {
                valores["estado"] = protocolo.estado ?: "BORRADOR"
            }
            if (!hayCambio(CAMBIO_AMBIENTES, idLocal)) {
                valores["temperatura_c"] = protocolo.temperaturaC
                valores["humedad_pct"] = protocolo.humedadPct
                valores["observaciones"] = protocolo.observaciones ?: ""
            }
            if (valores.isNotEmpty()) {
                actualizar("protocolo", valores, "id_protocolo=?", listOf(idLocal))
            }
        }

        val detalle = detalles[idRemoto] ?: continue
        mezclarPruebas(idLocal, detalle.pruebas.orEmpty())
        mezclarHistorial(idLocal, detalle.historial.orEmpty())
    }
}

private fun InduztekDbHelper.mezclarPruebas(idProtocolo: Int, pruebas: List<PruebaApi>) {
    if (hayCambio(CAMBIO_PRUEBA, idProtocolo)) return

    val locales = filas(
        "SELECT id_prueba,codigo_tipo_prueba,elemento FROM prueba WHERE id_protocolo=?",
        listOf(idProtocolo)
    )
    for (registro in locales) {
        val codigoLocal = registro.texto("codigo_tipo_prueba")
        val elementoLocal = registro.texto("elemento")
        var sigueEnServidor = false
        for (prueba in pruebas) {
            if ((prueba.codigoTipoPrueba ?: "") == codigoLocal && (prueba.elemento ?: "") == elementoLocal) {
                sigueEnServidor = true
            }
        }
        if (!sigueEnServidor) {
            val idPrueba = registro.entero("id_prueba")
            ejecutar("DELETE FROM medicion WHERE id_prueba=?", listOf(idPrueba))
            ejecutar("DELETE FROM prueba WHERE id_prueba=?", listOf(idPrueba))
        }
    }

    for (prueba in pruebas) {
        val codigo = prueba.codigoTipoPrueba ?: continue
        val elemento = prueba.elemento ?: ""
        val existente = fila(
            "SELECT id_prueba FROM prueba WHERE id_protocolo=? AND codigo_tipo_prueba=? AND elemento=?",
            listOf(idProtocolo, codigo, elemento)
        )
        var idPrueba = 0
        if (existente != null) {
            idPrueba = existente.entero("id_prueba")
            ejecutar("DELETE FROM medicion WHERE id_prueba=?", listOf(idPrueba))
            actualizar(
                "prueba",
                mapOf(
                    "realizada" to if (prueba.realizada == true) 1 else 0,
                    "motivo_no_realizada" to (prueba.motivoNoRealizada ?: ""),
                    "id_instrumento" to prueba.idInstrumento,
                    "resultado" to prueba.resultado,
                    "comentario" to (prueba.comentario ?: "")
                ),
                "id_prueba=?",
                listOf(idPrueba)
            )
        } else {
            idPrueba = insertar(
                "prueba",
                mapOf(
                    "id_protocolo" to idProtocolo,
                    "codigo_tipo_prueba" to codigo,
                    "elemento" to elemento,
                    "realizada" to if (prueba.realizada == true) 1 else 0,
                    "motivo_no_realizada" to (prueba.motivoNoRealizada ?: ""),
                    "id_instrumento" to prueba.idInstrumento,
                    "resultado" to prueba.resultado,
                    "comentario" to (prueba.comentario ?: "")
                )
            ).toInt()
            if (idPrueba <= 0) continue
        }
        if (prueba.realizada == true) {
            for ((parametro, valor) in prueba.mediciones.orEmpty()) {
                insertar(
                    "medicion",
                    mapOf(
                        "id_prueba" to idPrueba,
                        "codigo_parametro" to parametro,
                        "valor" to valor.toString()
                    )
                )
            }
        }
    }
}

private fun InduztekDbHelper.mezclarHistorial(idProtocolo: Int, historial: List<HistorialApi>) {
    for (paso in historial) {
        val existe = fila(
            "SELECT 1 FROM historial_estado WHERE id_protocolo=? AND estado_nuevo=? " +
                    "AND fecha_hora=? AND motivo=?",
            listOf(
                idProtocolo,
                paso.estadoNuevo ?: "",
                paso.fechaHora ?: "",
                paso.motivo ?: ""
            )
        )
        if (existe != null) continue
        ejecutar(
            "INSERT INTO historial_estado(id_protocolo,estado_anterior,estado_nuevo,id_usuario,fecha_hora,motivo) " +
                    "VALUES(?,?,?,?,?,?)",
            listOf(
                idProtocolo,
                paso.estadoAnterior,
                paso.estadoNuevo ?: "",
                usuarioLocalDe(paso.idUsuario) ?: 0,
                paso.fechaHora ?: "",
                paso.motivo ?: ""
            )
        )
    }
}
