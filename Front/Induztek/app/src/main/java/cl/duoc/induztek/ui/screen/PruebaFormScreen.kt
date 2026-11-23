
package cl.duoc.induztek.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.induztek.R
import cl.duoc.induztek.data.fechaCorta
import cl.duoc.induztek.model.Equipo
import cl.duoc.induztek.model.Instrumento
import cl.duoc.induztek.model.Protocolo
import cl.duoc.induztek.model.TipoPrueba
import cl.duoc.induztek.model.Usuario
import cl.duoc.induztek.ui.components.BotonBordeado
import cl.duoc.induztek.ui.components.BotonPrincipal
import cl.duoc.induztek.ui.components.CampoSeleccion
import cl.duoc.induztek.ui.components.CampoTexto
import cl.duoc.induztek.ui.components.EncabezadoPantalla
import cl.duoc.induztek.ui.components.FilaVacia
import cl.duoc.induztek.ui.components.Pestanas
import cl.duoc.induztek.ui.components.fondoPantallaInterna
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.TextoSecundario
import cl.duoc.induztek.ui.theme.BordeSuave
import cl.duoc.induztek.ui.theme.RojoRechazado
import cl.duoc.induztek.viewmodel.PruebaFormsViewModel
import cl.duoc.induztek.viewmodel.parametrosDe

@Composable
fun PruebaFormScreen(
    idEquipo: Int,
    tipo: TipoPrueba,
    usuario: Usuario,
    onAtras: () -> Unit,
    onGuardado: () -> Unit,
    modeloPantalla: PruebaFormsViewModel = viewModel()
) {
    val equipo = remember(idEquipo) { modeloPantalla.equipo(idEquipo) }
    if (equipo == null) {
        return
    }

    val bloqueo = remember { modeloPantalla.bloqueo(idEquipo, usuario.idUsuario) }

    var protocoloAbierto: Protocolo? = null
    if (bloqueo == null) {
        protocoloAbierto = remember(idEquipo) { modeloPantalla.protocoloEditable(idEquipo, usuario.idUsuario) }
    }
    val tipos = remember(idEquipo) { modeloPantalla.tiposDe(idEquipo) }
    if (protocoloAbierto != null) {
        modeloPantalla.cargar(protocoloAbierto.idProtocolo)
    }

    PruebaFormContent(
        equipo = equipo, protocolo = protocoloAbierto, tipos = tipos, estado = modeloPantalla.estado,
        tipoInicial = tipo, bloqueo = bloqueo,
        instrumentosPara = modeloPantalla::instrumentosPara,
        sugerencias = modeloPantalla::sugerencias,
        valorAnterior = modeloPantalla::valorAnterior,
        onAtras = onAtras, onGuardado = onGuardado,
        onCambioEstado = { clave, valor ->
            protocoloAbierto?.let { modeloPantalla.guardarBorrador(it.idProtocolo, clave, valor) }
        },
        onLimpiarBorrador = { tipoGuardado ->
            protocoloAbierto?.let { modeloPantalla.limpiarBorrador(it.idProtocolo, tipoGuardado) }
        },
        onGuardar = modeloPantalla::guardar,
        onGuardarNoRealizada = modeloPantalla::guardarNoRealizada
    )
}

@Composable
internal fun PruebaFormContent(
    equipo: Equipo,
    protocolo: Protocolo?,
    tipos: List<TipoPrueba>,
    estado: MutableMap<String, String>,
    tipoInicial: TipoPrueba,
    bloqueo: String?,
    instrumentosPara: (TipoPrueba) -> List<Instrumento>,
    sugerencias: (Int, TipoPrueba, String, Int) -> List<String>,
    valorAnterior: (Int, TipoPrueba, String, Int) -> Double?,
    onAtras: () -> Unit,
    onGuardado: () -> Unit,
    onCambioEstado: (String, String) -> Unit = { _, _ -> },
    onLimpiarBorrador: (TipoPrueba) -> Unit = {},
    onGuardar: (
        Int, TipoPrueba, Equipo, String, List<Instrumento>, String, Map<String, String>,
        String, String, Boolean, String
    ) -> String?,
    onGuardarNoRealizada: (Int, TipoPrueba, String, String, String) -> Unit
) {
    if (bloqueo != null) {
        Column(
            Modifier.fillMaxSize().padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.Center
        ) {
            Text(bloqueo, fontWeight = FontWeight.Medium)
            FilaVacia(Dimens.salto5)
            BotonPrincipal(
                texto = stringResource(R.string.btn_volver),
                onClick = onAtras,
                modifier = Modifier.fillMaxWidth()
            )
        }
        return
    }

    if (protocolo == null) {
        return
    }
    val datosProtocolo = protocolo

    var tipoSeleccionado by remember { mutableStateOf(tipoInicial) }
    var unidad by remember { mutableStateOf("MΩ") }
    var errorGuardado by remember { mutableStateOf<String?>(null) }

    fun dato(tipo: TipoPrueba, campo: String): String {
        val valor = estado["${tipo.name}|_$campo"]
        if (valor == null) {
            return ""
        }
        return valor
    }
    fun poner(tipo: TipoPrueba, campo: String, valor: String) {
        val clave = "${tipo.name}|_$campo"
        estado[clave] = valor
        onCambioEstado(clave, valor)
    }

    val opciones = elementosPosibles(equipo, tipoSeleccionado)

    var elemento = opciones[0]
    if (dato(tipoSeleccionado, "elemento").isNotEmpty()) {
        elemento = dato(tipoSeleccionado, "elemento")
    }

    fun texto(parametro: String): String {
        val valor = estado["$elemento|$tipoSeleccionado|$parametro"]
        if (valor == null) {
            return ""
        }
        return valor
    }
    fun actualizarValor(parametro: String, valor: String) {
        val clave = "$elemento|$tipoSeleccionado|$parametro"
        estado[clave] = valor
        onCambioEstado(clave, valor)
    }

    val instrumentos = remember(tipoSeleccionado) { instrumentosPara(tipoSeleccionado) }
    val instrumentoNombre = dato(tipoSeleccionado, "instrumento")
    val comentario = dato(tipoSeleccionado, "comentario")
    val motivo = dato(tipoSeleccionado, "motivo")
    val noRealizada = dato(tipoSeleccionado, "noRealizada") == "1"
    val resultadoManual = dato(tipoSeleccionado, "resultado")
    val releOpera = dato(tipoSeleccionado, "opera") != "0"

    val contexto = LocalContext.current
    val errMotivo = stringResource(R.string.form_err_motivo)

    val etiquetas = mutableListOf<String>()
    for (t in tipos) {
        etiquetas.add(nombrePestana(contexto, t))
    }

    val etiquetasInstrumento = mutableListOf<String>()
    for (instrumento in instrumentos) {
        etiquetasInstrumento.add("${instrumento.nombre} - ${instrumento.numeroSerie}")
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
            titulo = stringResource(R.string.form_titulo),
            subtitulo = stringResource(R.string.form_subtitulo, equipo.tag, datosProtocolo.numero, equipo.instalacionNombre),
            onAtras = onAtras
        )
        Pestanas(etiquetas, nombrePestana(contexto, tipoSeleccionado)) { elegido ->
            for (t in tipos) {
                if (nombrePestana(contexto, t) == elegido) {
                    tipoSeleccionado = t
                }
            }
        }
        FilaVacia(Dimens.espacioPestanaAContent)

        Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioEntreCampos)) {
            if (tipoSeleccionado != TipoPrueba.AISLACION && opciones.size > 1) CampoSeleccion(
                valor = elemento,
                opciones = opciones,
                onSeleccionar = { poner(tipoSeleccionado, "elemento", it) },
                etiqueta = etiquetaElemento(tipoSeleccionado),
                textoSugerido = stringResource(R.string.placeholder_seleccion)
            )
            if (tipoSeleccionado == TipoPrueba.AISLACION) {
                BloqueAislacion(
                    equipo, elemento, opciones, { poner(tipoSeleccionado, "elemento", it) },
                    ::texto, ::actualizarValor, unidad,
                    { nueva -> unidad = reescalar(tipoSeleccionado, elemento, estado, unidad, nueva) },
                    sugerencias, valorAnterior, datosProtocolo.idProtocolo
                )
            } else if (tipoSeleccionado == TipoPrueba.TTR) {
                BloqueTtr(equipo, ::texto, ::actualizarValor, valorAnterior, datosProtocolo.idProtocolo)
            } else if (tipoSeleccionado == TipoPrueba.DEVANADOS) {
                BloqueDevanados(
                    equipo, elemento, ::texto, ::actualizarValor, resultadoManual,
                    { poner(tipoSeleccionado, "resultado", it) }, valorAnterior, datosProtocolo.idProtocolo
                )
            } else if (tipoSeleccionado == TipoPrueba.CONTACTOS) {
                BloqueContactos(equipo, ::texto, ::actualizarValor, valorAnterior, datosProtocolo.idProtocolo)
            } else {
                BloqueRele(
                    equipo, ::texto, ::actualizarValor, releOpera, { poner(tipoSeleccionado, "opera", if (it) "1" else "0") },
                    valorAnterior, datosProtocolo.idProtocolo
                )
            }

            HorizontalDivider(thickness = Dimens.grosorDivisor, color = BordeSuave())

            CampoSeleccion(
                valor = instrumentoNombre,
                opciones = etiquetasInstrumento,
                onSeleccionar = { poner(tipoSeleccionado, "instrumento", it) },
                etiqueta = stringResource(R.string.form_instrumento),
                textoSugerido = stringResource(R.string.placeholder_seleccion),
                habilitado = !noRealizada
            )

            var instrumentoElegido: Instrumento? = null
            for (instrumento in instrumentos) {
                if ("${instrumento.nombre} - ${instrumento.numeroSerie}" == instrumentoNombre) {
                    instrumentoElegido = instrumento
                }
            }
            if (instrumentoElegido != null) {
                Text(
                    stringResource(R.string.form_calibracion, instrumentoElegido.fechaVence.fechaCorta()),
                    style = MaterialTheme.typography.bodySmall, color = TextoSecundario()
                )
            }

            CampoTexto(
                valor = comentario,
                onValorCambia = { poner(tipoSeleccionado, "comentario", it) },
                etiqueta = stringResource(R.string.form_comentarios),
                textoSugerido = stringResource(R.string.campo_placeholder_escribe),
                habilitado = !noRealizada,
                alto = Dimens.alturaCampoTextoLargo,
                unaSolaLinea = false,
                accionTeclado = ImeAction.Default
            )

            if (noRealizada) CampoTexto(
                valor = motivo,
                onValorCambia = { poner(tipoSeleccionado, "motivo", it); errorGuardado = null },
                etiqueta = stringResource(R.string.form_motivos),
                textoSugerido = stringResource(R.string.campo_placeholder_escribe),
                alto = Dimens.alturaCampoTextoLargo,
                unaSolaLinea = false,
                accionTeclado = ImeAction.Default
            )
        }

        FilaVacia(Dimens.salto5)
        Row(
            Modifier.semantics(mergeDescendants = true) { },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.form_no_realizada), Modifier.weight(1f))
            Switch(checked = noRealizada, onCheckedChange = { poner(tipoSeleccionado, "noRealizada", if (it) "1" else "0") })
        }

        FilaVacia(Dimens.salto4)
        val avisoError = errorGuardado
        if (avisoError != null) {
            Text(
                avisoError, Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall, color = RojoRechazado(), textAlign = TextAlign.Center
            )
            FilaVacia(Dimens.salto3)
        }
        BotonPrincipal(
            texto = stringResource(R.string.form_guardar),
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (noRealizada) {
                    if (motivo.isBlank()) {
                        errorGuardado = errMotivo
                        return@BotonPrincipal
                    }
                    onGuardarNoRealizada(datosProtocolo.idProtocolo, tipoSeleccionado, elemento, motivo, comentario)
                    onLimpiarBorrador(tipoSeleccionado)
                    onGuardado()
                    return@BotonPrincipal
                }

                val valores = mutableMapOf<String, String>()
                for (parametro in parametrosDe(tipoSeleccionado)) {
                    valores[parametro] = texto(parametro)
                }

                val resultadoGuardado = onGuardar(
                    datosProtocolo.idProtocolo, tipoSeleccionado, equipo, elemento, instrumentos, instrumentoNombre,
                    valores,
                    unidad, resultadoManual, releOpera, comentario
                )
                if (resultadoGuardado != null) {
                    errorGuardado = resultadoGuardado
                } else {
                    onLimpiarBorrador(tipoSeleccionado)
                    onGuardado()
                }
            })
        FilaVacia(Dimens.salto2)
        BotonBordeado(stringResource(R.string.btn_cancelar), onClick = onAtras)
        FilaVacia(Dimens.espacioGrande)
    }
}
