
package cl.duoc.induztek.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.induztek.R
import cl.duoc.induztek.data.fechaCorta
import cl.duoc.induztek.model.Calculos
import cl.duoc.induztek.model.EstadoProtocolo
import cl.duoc.induztek.model.Protocolo
import cl.duoc.induztek.model.TipoPrueba
import cl.duoc.induztek.model.Usuario
import cl.duoc.induztek.ui.components.FilaVacia
import cl.duoc.induztek.ui.components.Tarjeta
import cl.duoc.induztek.ui.components.fondoPantallaInterna
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.TextoSecundario
import cl.duoc.induztek.ui.theme.BordeSuave
import cl.duoc.induztek.viewmodel.ClienteViewModel


@Composable
fun ClienteEquipoScreen(
    idEquipo: Int,
    usuario: Usuario,
    onAtras: () -> Unit,
    modeloPantalla: ClienteViewModel = viewModel()
) {

    val empresa = usuario.idEmpresa
    val equipo = remember(idEquipo) {
        if (empresa == null) {
            null
        } else {
            modeloPantalla.equipoDeCliente(idEquipo, empresa)
        }
    }
    if (equipo == null) {
        return
    }

    val historial = remember { modeloPantalla.historial(idEquipo) }

    var empresaDelCliente = -1
    if (empresa != null) {
        empresaDelCliente = empresa
    }
    val protocolos = remember { modeloPantalla.protocolosDeEquipo(idEquipo, empresaDelCliente) }

    val formalizadores = remember(protocolos) {
        val nombres = mutableMapOf<Int, String>()
        for (protocolo in protocolos) {
            if (protocolo.estado == EstadoProtocolo.FORMALIZADO) {
                nombres[protocolo.idProtocolo] = modeloPantalla.formalizadoPor(protocolo.idProtocolo)
            }
        }
        nombres
    }

    var ultimo: Protocolo? = null
    if (protocolos.isNotEmpty()) {
        ultimo = protocolos[0]
    }
    var pendiente = false
    if (ultimo != null && ultimo.estado != EstadoProtocolo.FORMALIZADO) {
        pendiente = true
    }

    val sinAislacion = stringResource(R.string.cliente_sin_aislacion)
    val textoPendiente = stringResource(R.string.resultado_pendiente)

    @Composable
    fun detalleDe(protocolo: Protocolo): String {
        var posicion = -1
        for (i in historial.indices) {
            if (historial[i].first == protocolo.numero) {
                posicion = i
            }
        }
        if (posicion < 0) {
            return sinAislacion
        }

        val valor = historial[posicion].third

        var previa: String? = null
        if (posicion > 0) {
            previa = historial[posicion - 1].second
        }

        var nombreResultado = textoPendiente
        val pruebas = modeloPantalla.pruebasDe(protocolo.idProtocolo)
        for (prueba in pruebas) {
            if (prueba.tipoPrueba == TipoPrueba.AISLACION && prueba.realizada) {
                if (prueba.resultado != null) {
                    nombreResultado = prueba.resultado.nombre
                }
                break
            }
        }

        if (previa == null) {
            return stringResource(R.string.cliente_detalle_primero, Calculos.formatear(valor, 0), nombreResultado)
        }
        return stringResource(
            R.string.cliente_detalle_vs,
            Calculos.formatear(valor, 0), previa, nombreResultado
        )
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
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.margenPantalla, vertical = Dimens.salto5),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    Modifier.clickable(role = Role.Button) { onAtras() }
                        .semantics(mergeDescendants = true) { },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("‹", style = MaterialTheme.typography.headlineSmall, color = TextoSecundario())
                    Spacer(Modifier.width(Dimens.salto1))
                    Text(
                        stringResource(R.string.cliente_mis_equipos),
                        style = MaterialTheme.typography.bodyMedium, color = TextoSecundario()
                    )
                }
                Spacer(Modifier.weight(1f))
                if (pendiente) {
                    Text(
                        stringResource(R.string.cliente_aviso_pendiente),
                        style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    )
                }
            }

            Column(Modifier.padding(horizontal = Dimens.margenPantalla)) {
                Text(equipo.tag, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.Bottom) {
                    val potencia = equipo.potenciaKva
                    val textoPotencia = if (potencia != null) "${Calculos.formatear(potencia, 0)} kVA" else ""
                    Text(
                        "${equipo.tipoEquipo} ",
                        style = MaterialTheme.typography.bodyMedium, color = TextoSecundario()
                    )
                    Text(
                        "${equipo.instalacionNombre} $textoPotencia",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                FilaVacia(Dimens.salto2)
                Text(
                    stringResource(R.string.cliente_preliminares),
                    style = MaterialTheme.typography.labelSmall, color = TextoSecundario()
                )
                FilaVacia(Dimens.salto5)

                Tarjeta {
                    Text(
                        stringResource(R.string.cliente_como_viene),
                        style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        stringResource(R.string.cliente_lectura_at_t),
                        style = MaterialTheme.typography.labelSmall, color = TextoSecundario()
                    )
                    FilaVacia(Dimens.salto4)
                    if (historial.isEmpty()) {
                        Text(
                            stringResource(R.string.cliente_sin_datos),
                            style = MaterialTheme.typography.bodySmall, color = TextoSecundario()
                        )
                    } else {
                        val series = mutableListOf<Pair<String, Double>>()
                        for (registro in historial) {
                            series.add(registro.second.take(4) to registro.third)
                        }
                        GraficoAislacion(series)
                    }
                }

                var ultima: Double? = null
                if (historial.isNotEmpty()) {
                    ultima = historial[historial.size - 1].third
                }
                var anterior: Triple<String, String, Double>? = null
                if (historial.size >= 2) {
                    anterior = historial[historial.size - 2]
                }
                var valorAnterior: Double? = null
                if (anterior != null) {
                    valorAnterior = anterior.third
                }
                val variacion = Calculos.variacionPct(ultima, valorAnterior)

                if (variacion != null) {
                    var ingeniero = ""
                    if (ultimo != null) {
                        ingeniero = modeloPantalla.ingenieroDe(ultimo.idVisita)
                    }

                    var fechaAnterior = ""
                    if (anterior != null) {
                        fechaAnterior = anterior.second.take(4)
                    }

                    var textoVariacion = R.string.cliente_lectura_subio
                    var valorMostrado = variacion
                    if (variacion < 0) {
                        textoVariacion = R.string.cliente_lectura_bajo
                        valorMostrado = -variacion
                    }

                    var aviso = stringResource(
                        textoVariacion,
                        Calculos.formatear(valorMostrado, 1),
                        fechaAnterior
                    )
                    if (ingeniero.isNotEmpty()) {
                        aviso = aviso + " " + stringResource(R.string.cliente_ingeniero_revisa, ingeniero)
                    }

                    FilaVacia(Dimens.salto3)
                    Text(
                        aviso,
                        style = MaterialTheme.typography.labelSmall, color = TextoSecundario()
                    )
                }

                FilaVacia(Dimens.salto6)
                if (protocolos.isNotEmpty()) {
                    Tarjeta {
                        Text(
                            stringResource(R.string.cliente_historial),
                            style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold
                        )
                        var posicion = 0
                        for (protocolo in protocolos) {
                            if (posicion > 0) {
                                HorizontalDivider(thickness = Dimens.grosorDivisor, color = BordeSuave())
                            }

                            var firmadoPor = ""
                            val nombreFirmante = formalizadores[protocolo.idProtocolo]
                            if (nombreFirmante != null) {
                                firmadoPor = nombreFirmante
                            }

                            FilaHistorialCliente(
                                fecha = protocolo.fechaHoraPrueba.take(10).fechaCorta(),
                                numero = protocolo.numero,
                                detalle = detalleDe(protocolo),
                                estado = stringResource(
                                    if (protocolo.estado == EstadoProtocolo.FORMALIZADO) R.string.estado_formalizado
                                    else R.string.estado_pendiente_formalizacion
                                ),
                                formalizadoPor = firmadoPor
                            )
                            posicion++
                        }
                    }
                }
                FilaVacia(Dimens.salto9)
            }
        }
    }
}
