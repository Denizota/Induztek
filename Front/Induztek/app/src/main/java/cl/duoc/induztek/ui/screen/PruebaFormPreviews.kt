
package cl.duoc.induztek.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import cl.duoc.induztek.model.Equipo
import cl.duoc.induztek.model.EstadoProtocolo
import cl.duoc.induztek.model.Instrumento
import cl.duoc.induztek.model.Protocolo
import cl.duoc.induztek.model.TipoPrueba
import cl.duoc.induztek.ui.theme.InduztekTheme


private val TransformadorForm = Equipo(
    idEquipo = 1, idInstalacion = 1, tag = "TR-01", idTipoEquipo = 1, tipoEquipo = "Transformador",
    clasificacion = "PRIMARIO", marca = "Rhona", modelo = "RH-18733", numeroSerie = "RH-18733",
    anioFabricacion = 2012, potenciaKva = 1500.0, tensionPrimariaKv = 12.0, tensionSecundariaKv = 0.4,
    corrienteNominalA = null, maxContactosUohm = null, observaciones = "", instalacionNombre = "Planta Lampa"
)

private val ReleForm = Equipo(
    idEquipo = 6, idInstalacion = 5, tag = "REL-50/51-A", idTipoEquipo = 5, tipoEquipo = "Relé de protección",
    clasificacion = "SECUNDARIO", marca = "SEL", modelo = "SEL-751-30219", numeroSerie = "SEL-751-30219",
    anioFabricacion = 2020, potenciaKva = null, tensionPrimariaKv = null, tensionSecundariaKv = null,
    corrienteNominalA = null, maxContactosUohm = null, observaciones = "Sobrecorriente, ajuste 5 A",
    instalacionNombre = "Site Huechuraba"
)

private val ProtocoloForm = Protocolo(
    idProtocolo = 501, numero = "PT-2026-0143", idVisita = 26, idEquipo = 1, idUsuarioRegistra = 7,
    fechaHoraPrueba = "2026-09-26 10:30", temperaturaC = 21.0, humedadPct = 48.0,
    estado = EstadoProtocolo.BORRADOR, equipoTag = "TR-01", instalacionNombre = "Planta Lampa"
)

private val InstrumentosForm = listOf(
    Instrumento(1, "Megóhmetro 5 kV", "Megger", "MIT525", "MIT-0045821", "2026-03-15", "2027-03-15"),
    Instrumento(3, "Medidor de relación (TTR)", "Megger", "TTR300", "TTR-30044", "2026-06-02", "2027-06-02"),
    Instrumento(4, "Micro-ohmímetro", "Megger", "DLRO10X", "DLR-10882", "2026-08-20", "2027-08-20"),
    Instrumento(5, "Equipo de inyección secundaria", "Omicron", "CMC 356", "CMC-HB411", "2026-01-05", "2027-01-05")
)

private val AnterioresForm = mapOf(
    "AISLACION|LECTURA_1MIN" to 11800.0,
    "TTR|RELACION_MEDIDA" to 30.15,
    "OPERACION_RELE|TIEMPO_MEDIDO" to 40.0
)

@Composable
private fun FormularioPreview(
    equipo: Equipo,
    tipos: List<TipoPrueba>,
    tipoSeleccionado: TipoPrueba,
    estado: MutableMap<String, String>
) {
    InduztekTheme {
        PruebaFormContent(
            equipo = equipo, protocolo = ProtocoloForm, tipos = tipos, estado = estado,
            tipoInicial = tipoSeleccionado, bloqueo = null,
            instrumentosPara = { InstrumentosForm },
            sugerencias = { _, _, _, _ -> listOf("5", "10") },
            valorAnterior = { _, tipo, parametro, _ -> AnterioresForm["${tipo.name}|$parametro"] },
            onAtras = {}, onGuardado = {},
            onGuardar = { _, _, _, _, _, _, _, _, _, _, _ -> null },
            onGuardarNoRealizada = { _, _, _, _, _ -> }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Aislación")
@Composable
private fun FormAislacionPreview() {
    FormularioPreview(
        equipo = TransformadorForm,
        tipos = listOf(TipoPrueba.AISLACION, TipoPrueba.TTR, TipoPrueba.DEVANADOS),
        tipoSeleccionado = TipoPrueba.AISLACION,
        estado = mutableMapOf(
            "AISLACION|_instrumento" to "Megóhmetro 5 kV - MIT-0045821",
            "AISLACION|_comentario" to "Buje seco, sin condensación",
            "AT-T|AISLACION|TENSION_PRUEBA" to "5",
            "AT-T|AISLACION|LECTURA_1MIN" to "12500",
            "AT-T|AISLACION|LECTURA_10MIN" to "31250"
        )
    )
}

@Preview(showBackground = true, showSystemUi = true, name = "Relación")
@Composable
private fun FormTtrPreview() {
    FormularioPreview(
        equipo = TransformadorForm,
        tipos = listOf(TipoPrueba.AISLACION, TipoPrueba.TTR, TipoPrueba.DEVANADOS),
        tipoSeleccionado = TipoPrueba.TTR,
        estado = mutableMapOf(
            "TTR|_instrumento" to "Medidor de relación (TTR) - TTR-30044",
            "3 fases / cambiador de derivaciones|TTR|POSICION_TAP" to "3",
            "3 fases / cambiador de derivaciones|TTR|RELACION_TEORICA" to "30",
            "3 fases / cambiador de derivaciones|TTR|RELACION_MEDIDA" to "30,62"
        )
    )
}

@Preview(showBackground = true, showSystemUi = true, name = "Operación de relé")
@Composable
private fun FormRelePreview() {
    FormularioPreview(
        equipo = ReleForm,
        tipos = listOf(TipoPrueba.OPERACION_RELE),
        tipoSeleccionado = TipoPrueba.OPERACION_RELE,
        estado = mutableMapOf(
            "OPERACION_RELE|_instrumento" to "Equipo de inyección secundaria - CMC-HB411",
            "Disparo de fase|OPERACION_RELE|CORRIENTE_AJUSTE" to "5",
            "Disparo de fase|OPERACION_RELE|CORRIENTE_OPERACION" to "6,25",
            "Disparo de fase|OPERACION_RELE|TIEMPO_ESPERADO" to "40",
            "Disparo de fase|OPERACION_RELE|TIEMPO_MEDIDO" to "38,4"
        )
    )
}
