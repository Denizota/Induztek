
package cl.duoc.induztek.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import cl.duoc.induztek.R
import cl.duoc.induztek.model.Calculos
import cl.duoc.induztek.model.Equipo
import cl.duoc.induztek.model.EstadoProtocolo
import cl.duoc.induztek.model.Protocolo
import cl.duoc.induztek.model.Prueba
import cl.duoc.induztek.model.Resultado
import cl.duoc.induztek.model.Rol
import cl.duoc.induztek.model.TipoPrueba
import cl.duoc.induztek.model.Usuario
import cl.duoc.induztek.ui.components.BotonBordeado
import cl.duoc.induztek.ui.components.BotonChico
import cl.duoc.induztek.ui.components.BotonPrincipal
import cl.duoc.induztek.ui.components.CampoNumero
import cl.duoc.induztek.ui.components.CampoTexto
import cl.duoc.induztek.ui.components.CirculoPrueba
import cl.duoc.induztek.ui.components.EncabezadoPantalla
import cl.duoc.induztek.ui.components.FilaVacia
import cl.duoc.induztek.ui.components.fondoPantallaInterna
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.TextoSecundario
import cl.duoc.induztek.ui.theme.RojoRechazado


@Composable
internal fun EquipoContent(
    equipo: Equipo,
    usuario: Usuario,
    protocolo: Protocolo?,
    pruebas: List<Prueba>,
    tipos: List<TipoPrueba>,
    temp: String,
    humedad: String,
    onTemp: (String) -> Unit,
    onHumedad: (String) -> Unit,
    historial: List<Triple<String, String, Double>>,
    mensaje: String?,
    motivo: String?,
    onMotivo: (String) -> Unit,
    onAbrirMotivo: () -> Unit,
    onCerrarMotivo: () -> Unit,
    fechaCalibracion: (Prueba) -> String?,
    visitaAbierta: (Int) -> Boolean,
    ultimoMotivo: (Int) -> String,
    onAtras: () -> Unit,
    onRegistrarPrueba: (TipoPrueba) -> Unit,
    onIrActa: (Int) -> Unit,
    onMarcarRegistrado: (Protocolo, List<TipoPrueba>, List<Prueba>) -> Unit,
    onFormalizar: (Protocolo) -> Unit,
    onDevolver: (Protocolo, String) -> Unit
) {
    val editable = if (protocolo != null) {
        protocolo.estado == EstadoProtocolo.BORRADOR || protocolo.estado == EstadoProtocolo.OBSERVADO
    } else {
        true
    }

    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .fondoPantallaInterna()
            .verticalScroll(rememberScrollState())
    ) {
        Column(Modifier.padding(horizontal = Dimens.margenPantalla)) {
            var potenciaTexto = ""
            if (equipo.potenciaKva != null) {
                potenciaTexto = stringResource(R.string.ficha_valor_kva, Calculos.formatear(equipo.potenciaKva, 0))
            }
            EncabezadoPantalla(
                titulo = equipo.tag,
                subtitulo = "${equipo.tipoEquipo} ${equipo.marca} ${equipo.modelo} $potenciaTexto" +
                        "\n" + equipo.instalacionNombre,
                onAtras = onAtras
            )
            FilaVacia(Dimens.salto7)

            var potenciaFicha = "—"
            if (equipo.potenciaKva != null) {
                potenciaFicha = stringResource(R.string.ficha_valor_kva, Calculos.formatear(equipo.potenciaKva, 0))
            }
            var tensionesFicha = "—"
            if (equipo.tensionPrimariaKv != null) {
                tensionesFicha = stringResource(
                    R.string.ficha_valor_tensiones,
                    Calculos.formatear(equipo.tensionPrimariaKv, 1), Calculos.formatear(equipo.tensionSecundariaKv, 1)
                )
            }
            FilaSpec(
                stringResource(R.string.ficha_potencia) to potenciaFicha,
                stringResource(R.string.ficha_tensiones) to tensionesFicha,
                stringResource(R.string.ficha_anio) to equipo.anioFabricacion.toString()
            )
            FilaVacia(Dimens.salto8)
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.salto4)) {
                CampoNumero(
                    temp, onTemp,
                    stringResource(R.string.ficha_temperatura_ambiente),
                    stringResource(R.string.unidad_celsius), Modifier.weight(1f)
                )
                CampoNumero(
                    humedad, onHumedad,
                    stringResource(R.string.ficha_humedad),
                    stringResource(R.string.unidad_porcentaje), Modifier.weight(1f)
                )
            }
            FilaVacia(Dimens.salto7)
        }

        Divisor()

        Column(Modifier.padding(horizontal = Dimens.margenPantalla)) {
            Text(
                stringResource(R.string.ficha_pruebas_realizadas),
                style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold
            )

            for (indice in tipos.indices) {
                val tipo = tipos[indice]

                val hechas = mutableListOf<Prueba>()
                for (prueba in pruebas) {
                    if (prueba.tipoPrueba == tipo) {
                        hechas.add(prueba)
                    }
                }
                val resultados = mutableListOf<Resultado?>()
                for (prueba in hechas) {
                    resultados.add(prueba.resultado)
                }
                val resultado = Calculos.resultadoGlobal(resultados)

                var todasSinHacer = hechas.isNotEmpty()
                for (prueba in hechas) {
                    if (prueba.realizada) {
                        todasSinHacer = false
                    }
                }

                FilaVacia(Dimens.salto4)
                Row(
                    Modifier.fillMaxWidth().heightIn(min = Dimens.alturaFilaPrueba),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CirculoPrueba(
                        indice + 1, resultado,
                        noRealizada = todasSinHacer
                    )
                    Column(Modifier.weight(1f).padding(horizontal = Dimens.salto3)) {
                        Text(tipo.nombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(pistaPrueba(tipo, hechas, equipo), style = MaterialTheme.typography.bodySmall, color = TextoSecundario())
                        Text(estadoPrueba(hechas, resultado), style = MaterialTheme.typography.bodySmall, color = TextoSecundario())
                    }
                    if (editable) {
                        if (hechas.isEmpty()) {
                            BotonChico(stringResource(R.string.ficha_registrar), onClick = { onRegistrarPrueba(tipo) })
                        } else {
                            TextButton(onClick = { onRegistrarPrueba(tipo) }) {
                                Text(
                                    stringResource(R.string.ficha_editar),
                                    style = MaterialTheme.typography.labelMedium, color = TextoSecundario()
                                )
                            }
                        }
                    }
                }
                if (indice < tipos.lastIndex) {
                    FilaVacia(Dimens.salto4)
                    Divisor()
                }
            }
        }

        if (protocolo != null) {
            val medidas = mutableListOf<Prueba>()
            for (prueba in pruebas) {
                if (prueba.realizada) {
                    medidas.add(prueba)
                }
            }
            if (medidas.isNotEmpty()) {
                FilaVacia(Dimens.salto7)
                Divisor()
                for (medicion in medidas) {
                    Column(Modifier.padding(horizontal = Dimens.margenPantalla)) {
                        FilaVacia(Dimens.salto7)
                        Text(
                            tituloResumen(medicion, equipo),
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold
                        )
                        FilaVacia(Dimens.salto5)
                        val columnas = columnasResumen(medicion, equipo)
                        Row(Modifier.fillMaxWidth()) {
                            for (indice in columnas.indices) {
                                if (indice > 0) DivisorVertical()
                                val columna = columnas[indice]
                                ColumnaValor(columna.clave, columna.valor, columna.sufijo, Modifier.weight(1f))
                            }
                        }
                        FilaVacia(Dimens.salto5)

                        var fechaDelInstrumento = stringResource(R.string.ficha_fecha_sin_dato)
                        val fechaGuardada = fechaCalibracion(medicion)
                        if (fechaGuardada != null) {
                            fechaDelInstrumento = fechaGuardada
                        }
                        Text(
                            stringResource(R.string.ficha_resumen_calibrado, fechaDelInstrumento),
                            style = MaterialTheme.typography.bodySmall, color = TextoSecundario()
                        )
                        FilaVacia(Dimens.salto7)
                    }
                }
            }

            Divisor()
            Column(Modifier.padding(horizontal = Dimens.margenPantalla)) {
                FilaVacia(Dimens.salto7)

                ContraPruebasAnteriores(historial)

                val faltan = mutableListOf<TipoPrueba>()
                for (tipo in tipos) {
                    var tienePrueba = false
                    for (prueba in pruebas) {
                        if (prueba.tipoPrueba == tipo) {
                            tienePrueba = true
                        }
                    }
                    if (!tienePrueba) {
                        faltan.add(tipo)
                    }
                }
                val puedeCerrar = usuario.rol != Rol.CLIENTE

                FilaVacia(Dimens.salto8)

                if (puedeCerrar && editable) {
                    BotonPrincipal(
                        texto = stringResource(R.string.ficha_btn_registrado),
                        modifier = Modifier.fillMaxWidth(),
                        habilitado = faltan.isEmpty(),
                        onClick = { onMarcarRegistrado(protocolo, tipos, pruebas) }
                    )
                    var listaFaltan = ""
                    val conjuncion = stringResource(R.string.ficha_conjuncion)
                    for (tipo in faltan) {
                        if (listaFaltan.isNotEmpty()) {
                            listaFaltan = listaFaltan + conjuncion
                        }
                        listaFaltan = listaFaltan + tipo.nombre.substringBefore(" (")
                    }
                    if (faltan.isNotEmpty()) Text(
                        stringResource(R.string.ficha_faltan, listaFaltan),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario(),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (puedeCerrar && protocolo.estado == EstadoProtocolo.REGISTRADO &&
                    visitaAbierta(protocolo.idVisita) && usuario.rol == Rol.INGENIERO) {
                    BotonPrincipal(
                        texto = stringResource(R.string.ficha_btn_formalizar),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onFormalizar(protocolo) }
                    )
                    FilaVacia(Dimens.salto3)
                    BotonBordeado(
                        stringResource(R.string.ficha_btn_devolver),
                        onClick = onAbrirMotivo
                    )
                    if (motivo != null) {
                        CampoTexto(
                            valor = motivo,
                            onValorCambia = onMotivo,
                            etiqueta = stringResource(R.string.ficha_campo_motivo),
                            textoSugerido = stringResource(R.string.campo_placeholder_escribe)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.salto2)) {
                            TextButton(onClick = onCerrarMotivo) {
                                Text(stringResource(R.string.btn_cancelar))
                            }
                            TextButton(onClick = { if (motivo.isNotBlank()) onDevolver(protocolo, motivo) }) {
                                Text(stringResource(R.string.ficha_confirmar))
                            }
                        }
                    }
                }

                if (protocolo.estado == EstadoProtocolo.OBSERVADO) {
                    val motivoGuardado = ultimoMotivo(protocolo.idProtocolo)
                    if (motivoGuardado.isNotEmpty()) {
                        FilaVacia(Dimens.salto3)
                        Text(
                            stringResource(R.string.ficha_devuelto, motivoGuardado),
                            style = MaterialTheme.typography.bodySmall, color = RojoRechazado()
                        )
                    }
                }

                if (!editable) {
                    FilaVacia(Dimens.salto6)
                    Text(
                        stringResource(
                            R.string.ficha_no_editable,
                            protocolo.estado.nombre.lowercase()
                        ),
                        style = MaterialTheme.typography.bodySmall, color = TextoSecundario(),
                        textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                    )
                    FilaVacia(Dimens.salto4)
                    BotonBordeado(stringResource(R.string.ficha_ver_acta), onClick = { onIrActa(protocolo.idVisita) })
                }

                if (mensaje != null) {
                    FilaVacia(Dimens.salto3)
                    Text(mensaje, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                FilaVacia(Dimens.salto9)
            }
        }
    }
}
