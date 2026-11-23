
package cl.duoc.induztek.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.induztek.R
import cl.duoc.induztek.data.fechaCorta
import cl.duoc.induztek.model.Equipo
import cl.duoc.induztek.model.EstadoProtocolo
import cl.duoc.induztek.model.Protocolo
import cl.duoc.induztek.model.Usuario
import cl.duoc.induztek.model.Visita
import cl.duoc.induztek.ui.components.FilaVacia
import cl.duoc.induztek.ui.components.fondoPantallaInterna
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.TextoSecundario
import cl.duoc.induztek.viewmodel.ClienteViewModel


@Composable
fun ClienteHomeScreen(
    usuario: Usuario,
    onIrEquipo: (Int) -> Unit,
    onIrActa: (Int) -> Unit,
    onIrPerfil: () -> Unit,
    onSalir: () -> Unit,
    onReiniciar: () -> Unit,
    modeloPantalla: ClienteViewModel = viewModel()
) {
    var empresaId = usuario.idUsuario
    val idDeLaEmpresa = usuario.idEmpresa
    if (idDeLaEmpresa != null) {
        empresaId = idDeLaEmpresa
    }

    var revision by rememberSaveable { mutableIntStateOf(0) }
    val sincronia = versionSincronia()
    val equipos = remember(revision, sincronia) { modeloPantalla.equiposDe(empresaId) }
    val protocolos = remember(revision, sincronia) { modeloPantalla.protocolosVisitaCliente(empresaId) }

    val formalizadores = remember(revision, protocolos, sincronia) {
        val nombres = mutableMapOf<Int, String>()
        for (protocolo in protocolos) {
            if (protocolo.estado == EstadoProtocolo.FORMALIZADO) {
                nombres[protocolo.idProtocolo] = modeloPantalla.formalizadoPor(protocolo.idProtocolo)
            }
        }
        nombres
    }

    val ultimaVisita = remember(revision, sincronia) {
        var laUltima: Visita? = null
        var ultimaFecha = ""
        for (protocolo in protocolos) {
            val visita = modeloPantalla.visitaPorId(protocolo.idVisita)
            if (visita != null) {
                val cierre = visita.fechaHoraCierre
                if (cierre != null) {
                    if (laUltima == null || cierre > ultimaFecha) {
                        laUltima = visita
                        ultimaFecha = cierre
                    }
                }
            }
        }
        laUltima
    }

    val recepcion = remember(ultimaVisita) {
        if (ultimaVisita == null) {
            null
        } else {
            modeloPantalla.recepcionDeVisita(ultimaVisita.idVisita)
        }
    }

    var busqueda by remember { mutableStateOf("") }

    val visibles = mutableListOf<Equipo>()
    for (equipo in equipos) {
        var coincide = busqueda.isBlank()
        if (equipo.tag.contains(busqueda, true)) {
            coincide = true
        }
        if (equipo.instalacionNombre.contains(busqueda, true)) {
            coincide = true
        }
        if (equipo.numeroSerie.contains(busqueda, true)) {
            coincide = true
        }
        if (coincide) {
            visibles.add(equipo)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .fondoPantallaInterna()
    ) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            CabeceraCliente(
                nombre = usuario.nombre,
                subtitulo = "${usuario.cargo} ${modeloPantalla.nombreDe(empresaId)}",
                idUsuario = usuario.idUsuario,
                onSalir = onSalir,
                onReiniciar = {
                    onReiniciar()
                    revision++
                }
            )
            Column(
                Modifier.padding(
                    horizontal = Dimens.margenPantalla,
                    vertical = Dimens.espacioMedio
                )
            ) {
                if (ultimaVisita != null) {
                    val deLaVisita = mutableListOf<Protocolo>()
                    for (protocolo in protocolos) {
                        if (protocolo.idVisita == ultimaVisita.idVisita) {
                            deLaVisita.add(protocolo)
                        }
                    }

                    var pendiente = false
                    for (protocolo in deLaVisita) {
                        if (protocolo.estado != EstadoProtocolo.FORMALIZADO) {
                            pendiente = true
                        }
                    }

                    var hora = ""
                    var mismaFecha = false
                    var fechaEntrega = ""
                    if (recepcion != null) {
                        hora = recepcion.fechaHoraRecepcion.takeLast(5)
                        val diaRecepcion = recepcion.fechaHoraRecepcion.take(10)
                        if (diaRecepcion == ultimaVisita.fecha) {
                            mismaFecha = true
                        }
                        fechaEntrega = diaRecepcion.fechaCorta()
                    }

                    val idsProbados = mutableListOf<Int>()
                    for (protocolo in deLaVisita) {
                        if (!idsProbados.contains(protocolo.idEquipo)) {
                            idsProbados.add(protocolo.idEquipo)
                        }
                    }
                    val equiposProbados = idsProbados.size

                    var instalacion = ""
                    val lugar = modeloPantalla.instalacionPorId(ultimaVisita.idInstalacion)
                    if (lugar != null) {
                        instalacion = lugar.nombre
                    }

                    CajaUltimaVisita(
                        instalacion = instalacion,
                        numeroOt = ultimaVisita.numeroOt,
                        fecha = ultimaVisita.fecha.fechaCorta(),
                        pendiente = pendiente,
                        hora = hora,
                        mismaFecha = mismaFecha,
                        fechaEntrega = fechaEntrega,
                        equiposProbados = equiposProbados,
                        onVerActa = { onIrActa(ultimaVisita.idVisita) }
                    )
                    FilaVacia(Dimens.salto6)
                }

                BuscadorCliente(busqueda) { busqueda = it }
                FilaVacia(Dimens.salto5)

                Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    for (equipo in visibles) {
                        var protocoloDelEquipo: Protocolo? = null
                        for (protocolo in protocolos) {
                            if (protocolo.idEquipo == equipo.idEquipo) {
                                if (protocoloDelEquipo == null ||
                                    protocolo.fechaHoraPrueba > protocoloDelEquipo.fechaHoraPrueba
                                ) {
                                    protocoloDelEquipo = protocolo
                                }
                            }
                        }

                        var ultimaPrueba = "—"
                        var pendienteEquipo = true
                        var firmadoPor = ""
                        if (protocoloDelEquipo != null) {
                            ultimaPrueba = protocoloDelEquipo.fechaHoraPrueba.take(10).fechaCorta()
                            if (protocoloDelEquipo.estado == EstadoProtocolo.FORMALIZADO) {
                                pendienteEquipo = false
                            }
                            val nombreFirmante = formalizadores[protocoloDelEquipo.idProtocolo]
                            if (nombreFirmante != null) {
                                firmadoPor = nombreFirmante
                            }
                        }

                        TarjetaEquipoCliente(
                            tag = equipo.tag,
                            tipoEquipo = equipo.tipoEquipo,
                            instalacion = equipo.instalacionNombre,
                            ultimaPrueba = ultimaPrueba,
                            pendiente = pendienteEquipo,
                            formalizadoPor = firmadoPor,
                            onClick = { onIrEquipo(equipo.idEquipo) }
                        )
                    }
                }

                FilaVacia(Dimens.salto6)
                Text(
                    stringResource(R.string.cliente_solo_visualizar),
                    style = MaterialTheme.typography.labelSmall, color = TextoSecundario()
                )
                FilaVacia(Dimens.salto9)
            }
        }
        BarraNavegacionCliente(onIrPerfil = onIrPerfil)
    }
}
