
package cl.duoc.induztek.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.induztek.R
import cl.duoc.induztek.data.fechaCorta
import cl.duoc.induztek.data.fechaHoraAhora
import cl.duoc.induztek.model.Calculos
import cl.duoc.induztek.model.EstadoProtocolo
import cl.duoc.induztek.model.Protocolo
import cl.duoc.induztek.model.Resultado
import cl.duoc.induztek.model.Usuario
import cl.duoc.induztek.ui.components.BotonBordeado
import cl.duoc.induztek.ui.components.BotonPrincipal
import cl.duoc.induztek.ui.components.CampoBase
import cl.duoc.induztek.ui.components.CampoTexto
import cl.duoc.induztek.ui.components.EncabezadoPantalla
import cl.duoc.induztek.ui.components.FilaVacia
import cl.duoc.induztek.ui.components.colorCaja
import cl.duoc.induztek.ui.components.fondoPantallaInterna
import cl.duoc.induztek.ui.theme.Blanco
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.FondoFilaTabla
import cl.duoc.induztek.ui.theme.TextoSecundario
import cl.duoc.induztek.ui.theme.FondoActa
import cl.duoc.induztek.ui.theme.BordeSuave
import cl.duoc.induztek.ui.theme.esModoOscuro
import cl.duoc.induztek.viewmodel.ActaViewModel
import kotlinx.coroutines.launch

@Composable
fun ActaScreen(
    idVisita: Int,
    usuario: Usuario,
    esCliente: Boolean,
    onAtras: () -> Unit,
    modeloPantalla: ActaViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var recarga by remember { mutableStateOf(0) }

    val visita = remember(recarga) { modeloPantalla.visitaPorId(idVisita) }
    if (visita == null) {
        return
    }

    val instalacion = remember { modeloPantalla.instalacionPorId(visita.idInstalacion) }

    var empresaDelCliente = -1
    val empresa = usuario.idEmpresa
    if (empresa != null) {
        empresaDelCliente = empresa
    }
    val protocolos = remember(recarga) {
        if (esCliente) {
            modeloPantalla.protocolosCliente(idVisita, empresaDelCliente)
        } else {
            modeloPantalla.protocolosDeVisita(idVisita)
        }
    }

    val recepcion = remember(recarga) { modeloPantalla.recepcionDeVisita(idVisita) }
    modeloPantalla.preparar(idVisita, recepcion)

    val cerrada = visita.fechaHoraCierre != null || esCliente

    var primerProtocolo: Protocolo? = null
    if (protocolos.isNotEmpty()) {
        primerProtocolo = protocolos[0]
    }

    val pendientes = mutableListOf<Protocolo>()
    for (protocolo in protocolos) {
        if (protocolo.estado != EstadoProtocolo.REGISTRADO && protocolo.estado != EstadoProtocolo.ANULADO) {
            pendientes.add(protocolo)
        }
    }

    var todasFormalizadas = protocolos.isNotEmpty()
    for (protocolo in protocolos) {
        if (protocolo.estado != EstadoProtocolo.FORMALIZADO) {
            todasFormalizadas = false
        }
    }

    var nombreInstalacion = ""
    if (instalacion != null) {
        nombreInstalacion = instalacion.nombre
    }
    var nombreCliente = "—"
    if (instalacion != null) {
        nombreCliente = instalacion.clienteNombre
    }

    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .fondoPantallaInterna()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.margenPantalla)
    ) {
        EncabezadoPantalla(
            titulo = stringResource(
                if (esCliente) R.string.acta_titulo_acta else R.string.acta_titulo_cerrar
            ),
            subtitulo = "${visita.numeroOt} $nombreInstalacion",
            onAtras = onAtras
        )
        Text(stringResource(R.string.acta_recibe_cliente), fontWeight = FontWeight.SemiBold)
        FilaVacia(Dimens.salto4)

        var conAmbiente: Protocolo? = null
        for (protocolo in protocolos) {
            if (conAmbiente == null && protocolo.temperaturaC != null) {
                conAmbiente = protocolo
            }
        }
        if (conAmbiente == null) {
            conAmbiente = primerProtocolo
        }

        Column(
            Modifier
                .fillMaxWidth()
                .background(colorCaja(), RoundedCornerShape(Dimens.radioActa))
                .border(Dimens.bordeCampo, BordeSuave(), RoundedCornerShape(Dimens.radioActa))
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(FondoActa(), RoundedCornerShape(topStart = Dimens.radioActa, topEnd = Dimens.radioActa))
                    .padding(Dimens.rellenoCaja)
            ) {
                Text(
                    stringResource(R.string.acta_empresa),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (esModoOscuro()) Blanco.copy(alpha = 0.72f) else TextoSecundario()
                )
                Text(
                    stringResource(R.string.acta_titulo_acta),
                    fontWeight = FontWeight.Bold, color = Color.White
                )
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(FondoFilaTabla())
                    .padding(vertical = Dimens.salto2),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(
                        if (todasFormalizadas) R.string.acta_formalizada
                        else R.string.estado_pendiente_formalizacion
                    ),
                    style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium
                )
            }

            Column(Modifier.fillMaxWidth().padding(Dimens.rellenoCaja)) {
                ColumnaDato(
                    stringResource(R.string.acta_cliente),
                    nombreCliente, Modifier.fillMaxWidth()
                )
                FilaVacia(Dimens.salto2)

                var nombreTecnico = "—"
                if (conAmbiente != null) {
                    nombreTecnico = conAmbiente.tecnicoNombre
                }

                Row(Modifier.fillMaxWidth()) {
                    ColumnaDato(
                        stringResource(R.string.acta_fecha), visita.fecha.fechaCorta(), Modifier.weight(1f)
                    )
                    ColumnaDato(
                        stringResource(R.string.acta_tecnico), nombreTecnico,
                        Modifier.weight(1f)
                    )
                }
                FilaVacia(Dimens.salto2)
                Row(Modifier.fillMaxWidth()) {
                    var textoTemperatura = "—"
                    var textoHumedad = "—"
                    if (conAmbiente != null) {
                        val temperatura = conAmbiente.temperaturaC
                        if (temperatura != null) {
                            textoTemperatura = stringResource(
                                R.string.acta_valor_celsius, Calculos.formatear(temperatura, 0)
                            )
                        }
                        val humedad = conAmbiente.humedadPct
                        if (humedad != null) {
                            textoHumedad = stringResource(
                                R.string.acta_valor_porcentaje, Calculos.formatear(humedad, 0)
                            )
                        }
                    }
                    ColumnaDato(
                        stringResource(R.string.acta_temperatura),
                        textoTemperatura,
                        Modifier.weight(1f)
                    )
                    ColumnaDato(
                        stringResource(R.string.acta_humedad),
                        textoHumedad,
                        Modifier.weight(1f)
                    )
                }
                HorizontalDivider(Modifier.padding(vertical = Dimens.salto3))

                for (pr in protocolos) {
                    val pruebas = modeloPantalla.pruebasDe(pr.idProtocolo)

                    val resultados = mutableListOf<Resultado?>()
                    for (prueba in pruebas) {
                        resultados.add(prueba.resultado)
                    }
                    val global = Calculos.resultadoGlobal(resultados)

                    var nombreGlobal = stringResource(R.string.acta_registrada)
                    if (global != null) {
                        nombreGlobal = global.nombre
                    }

                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "${pr.equipoTag} ${pr.tipoEquipo}",
                                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                stringResource(R.string.acta_protocolo, pr.numero),
                                style = MaterialTheme.typography.bodySmall, color = TextoSecundario()
                            )
                        }
                        Text(
                            stringResource(R.string.acta_global, nombreGlobal),
                            style = MaterialTheme.typography.labelMedium,
                            color = colorResultado(global),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    FilaVacia(Dimens.salto2)
                    var numeroFila = 1
                    for (prueba in pruebas) {
                        FilaActa(numeroFila, prueba.tipoPrueba.nombre, detallePrueba(prueba), estadoPrueba(prueba), prueba.resultado, !prueba.realizada)
                        FilaVacia(Dimens.salto1)
                        numeroFila++
                    }
                    FilaVacia(Dimens.salto3)
                }
            }
        }

        if (!esCliente) {
            FilaVacia(Dimens.salto6)
            Text(stringResource(R.string.acta_quien_recibe), fontWeight = FontWeight.SemiBold)
            FilaVacia(Dimens.salto3)
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioEntreCampos)) {
                CampoTexto(
                    valor = modeloPantalla.nombre.value,
                    onValorCambia = { modeloPantalla.nombre.value = it; modeloPantalla.errNombre.value = null },
                    etiqueta = stringResource(R.string.acta_campo_nombre),
                    textoSugerido = stringResource(R.string.campo_placeholder_escribe),
                    habilitado = !cerrada,
                    error = modeloPantalla.errNombre.value
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioEntreCampos)) {
                    CampoBase(
                        valor = modeloPantalla.rut.value,
                        onValorCambia = { modeloPantalla.rut.value = limpiarRut(it); modeloPantalla.errRut.value = null },
                        etiqueta = stringResource(R.string.acta_campo_rut),
                        textoSugerido = stringResource(R.string.campo_placeholder_escribe),
                        habilitado = !cerrada,
                        error = modeloPantalla.errRut.value,
                        modifier = Modifier.weight(1f),
                        transformacionVisual = TransformacionRut
                    )

                    var fechaDelActa = fechaHoraAhora().horaCorta()
                    if (recepcion != null) {
                        fechaDelActa = recepcion.fechaHoraRecepcion
                    }

                    CampoBase(
                        valor = fechaDelActa,
                        onValorCambia = {},
                        etiqueta = stringResource(R.string.acta_campo_fecha),
                        textoSugerido = stringResource(R.string.campo_placeholder_escribe),
                        habilitado = false,
                        modifier = Modifier.weight(1f),
                        contenidoFinal = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_candado),
                                contentDescription = stringResource(R.string.cd_fecha_bloqueada),
                                tint = TextoSecundario(),
                                modifier = Modifier.size(Dimens.tamanoIconoCampo)
                            )
                        }
                    )
                }
                CampoTexto(
                    valor = modeloPantalla.cargo.value,
                    onValorCambia = { modeloPantalla.cargo.value = it; modeloPantalla.errCargo.value = null },
                    etiqueta = stringResource(R.string.acta_campo_cargo),
                    textoSugerido = stringResource(R.string.campo_placeholder_escribe),
                    habilitado = !cerrada,
                    error = modeloPantalla.errCargo.value
                )
            }
            FilaVacia(Dimens.salto6)

            val errNombre = stringResource(R.string.acta_err_nombre)
            val errRut = stringResource(R.string.acta_err_rut)
            val errCargo = stringResource(R.string.acta_err_cargo)
            val actaCerrada = stringResource(R.string.acta_ok)

            var listaPendientes = ""
            for (protocolo in pendientes) {
                if (listaPendientes.isNotEmpty()) {
                    listaPendientes = listaPendientes + ", "
                }
                listaPendientes = listaPendientes + "${protocolo.equipoTag} (${protocolo.estado.nombre.lowercase()})"
            }
            var avisoPendientes: String? = null
            if (pendientes.isNotEmpty()) {
                avisoPendientes = stringResource(R.string.acta_err_pendientes, listaPendientes)
            }

            BotonPrincipal(
                texto = stringResource(R.string.acta_btn_cerrar),
                modifier = Modifier.fillMaxWidth(),
                habilitado = !cerrada,
                onClick = {
                    var errorNombre: String? = null
                    if (modeloPantalla.nombre.value.isBlank()) {
                        errorNombre = errNombre
                    }
                    var errorRut: String? = null
                    if (!Calculos.rutValido(modeloPantalla.rut.value)) {
                        errorRut = errRut
                    }
                    var errorCargo: String? = null
                    if (modeloPantalla.cargo.value.isBlank()) {
                        errorCargo = errCargo
                    }
                    modeloPantalla.errNombre.value = errorNombre
                    modeloPantalla.errRut.value = errorRut
                    modeloPantalla.errCargo.value = errorCargo

                    if (errorNombre != null || errorRut != null || errorCargo != null) {
                        return@BotonPrincipal
                    }
                    if (avisoPendientes != null) {
                        scope.launch { snackbar.showSnackbar(avisoPendientes) }
                        return@BotonPrincipal
                    }

                    var error: String? = null
                    try {
                        modeloPantalla.cerrarVisita(idVisita, modeloPantalla.nombre.value, modeloPantalla.rut.value, modeloPantalla.cargo.value)
                    } catch (e: Exception) {
                        error = e.message
                    }
                    recarga++

                    var mensajeFinal = actaCerrada
                    if (error != null) {
                        mensajeFinal = error
                    }
                    scope.launch {
                        snackbar.showSnackbar(mensajeFinal)
                    }
                })
            FilaVacia(Dimens.salto2)
            val sinPdf = stringResource(R.string.acta_no_pdf)
            BotonBordeado(stringResource(R.string.acta_btn_pdf), onClick = {
                scope.launch { snackbar.showSnackbar(sinPdf) }
            })
        }
        FilaVacia(Dimens.espacioGrande)
        SnackbarHost(snackbar)
    }
}
