
package cl.duoc.induztek.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import cl.duoc.induztek.model.Equipo
import cl.duoc.induztek.model.EstadoProtocolo
import cl.duoc.induztek.model.Protocolo
import cl.duoc.induztek.model.Prueba
import cl.duoc.induztek.model.Resultado
import cl.duoc.induztek.model.Rol
import cl.duoc.induztek.model.TipoPrueba
import cl.duoc.induztek.model.Usuario
import cl.duoc.induztek.ui.theme.InduztekTheme


private val UsuarioPreview = Usuario(
    idUsuario = 7, idRol = 1, rut = "17.284.519-3", nombre = "Matías Godoy Herrera",
    correo = "mgodoy@induztek.ejemplo", rol = Rol.INGENIERO
)

private val TransformadorPreview = Equipo(
    idEquipo = 1, idInstalacion = 1, tag = "TR-01", idTipoEquipo = 1, tipoEquipo = "Transformador",
    clasificacion = "PRIMARIO", marca = "Rhona", modelo = "RH-18733", numeroSerie = "RH-18733",
    anioFabricacion = 2012, potenciaKva = 1500.0, tensionPrimariaKv = 12.0, tensionSecundariaKv = 0.4,
    corrienteNominalA = null, maxContactosUohm = null, observaciones = "", instalacionNombre = "Planta Lampa"
)

private val CeldaPreview = Equipo(
    idEquipo = 4, idInstalacion = 4, tag = "CEL-MT-03", idTipoEquipo = 3, tipoEquipo = "Celda MT",
    clasificacion = "PRIMARIO", marca = "Ormazabal", modelo = "OR-77812", numeroSerie = "OR-77812",
    anioFabricacion = 2018, potenciaKva = null, tensionPrimariaKv = 12.0, tensionSecundariaKv = null,
    corrienteNominalA = 630.0, maxContactosUohm = 60.0, observaciones = "", instalacionNombre = "Edificio Clínico"
)

private fun protocoloPreview(estado: EstadoProtocolo) = Protocolo(
    idProtocolo = 501, numero = "PT-2026-0143", idVisita = 26, idEquipo = 1, idUsuarioRegistra = 7,
    fechaHoraPrueba = "2026-09-26 10:30", temperaturaC = 21.0, humedadPct = 48.0, estado = estado
)

private fun aislacion(valor1Min: Double, valor10Min: Double?, resultado: Resultado): Prueba {
    val mediciones = mutableMapOf<String, Double>()
    mediciones["TENSION_PRUEBA"] = 5.0
    mediciones["LECTURA_1MIN"] = valor1Min
    if (valor10Min != null) mediciones["LECTURA_10MIN"] = valor10Min
    return Prueba(
        idPrueba = 900, idProtocolo = 501, tipoPrueba = TipoPrueba.AISLACION, elemento = "AT-T",
        realizada = true, motivoNoRealizada = "", idInstrumento = 1, resultado = resultado, comentario = "",
        mediciones = mediciones
    )
}

@Preview(showBackground = true, showSystemUi = true, name = "TR-01 en curso")
@Composable
private fun FichaEquipoPreview() {
    InduztekTheme {
        EquipoContent(
            equipo = TransformadorPreview, usuario = UsuarioPreview,
            protocolo = protocoloPreview(EstadoProtocolo.BORRADOR),
            pruebas = listOf(
                aislacion(12500.0, 31250.0, Resultado.APROBADO),
                Prueba(
                    idPrueba = 901, idProtocolo = 501, tipoPrueba = TipoPrueba.TTR, elemento = "3 fases / tap 3",
                    realizada = true, motivoNoRealizada = "", idInstrumento = 3, resultado = Resultado.OBSERVADO,
                    comentario = "",
                    mediciones = mapOf("POSICION_TAP" to 3.0, "RELACION_TEORICA" to 30.0, "RELACION_MEDIDA" to 30.62)
                )
            ),
            tipos = listOf(TipoPrueba.AISLACION, TipoPrueba.TTR, TipoPrueba.DEVANADOS),
            temp = "21", humedad = "48", onTemp = {}, onHumedad = {},
            historial = listOf(
                Triple("PT-2025-0288", "2025-09-13", 9800.0),
                Triple("PT-2024-0312", "2024-09-14", 12500.0)
            ),
            mensaje = null, motivo = null, onMotivo = {}, onAbrirMotivo = {}, onCerrarMotivo = {},
            fechaCalibracion = { "2027-03-15" }, visitaAbierta = { true }, ultimoMotivo = { "" },
            onAtras = {}, onRegistrarPrueba = {}, onIrActa = {},
            onMarcarRegistrado = { _, _, _ -> }, onFormalizar = {}, onDevolver = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Celda formalizada")
@Composable
private fun FichaCeldaPreview() {
    InduztekTheme {
        EquipoContent(
            equipo = CeldaPreview, usuario = UsuarioPreview,
            protocolo = protocoloPreview(EstadoProtocolo.FORMALIZADO),
            pruebas = listOf(
                aislacion(3100.0, null, Resultado.APROBADO),
                Prueba(
                    idPrueba = 903, idProtocolo = 501, tipoPrueba = TipoPrueba.CONTACTOS, elemento = "Polo 3",
                    realizada = true, motivoNoRealizada = "", idInstrumento = 4, resultado = Resultado.OBSERVADO,
                    comentario = "", mediciones = mapOf("RESISTENCIA_CONTACTO" to 67.0)
                )
            ),
            tipos = listOf(TipoPrueba.AISLACION, TipoPrueba.CONTACTOS),
            temp = "20", humedad = "47", onTemp = {}, onHumedad = {},
            historial = listOf(Triple("PT-2025-0292", "2025-09-02", 3100.0)),
            mensaje = null, motivo = null, onMotivo = {}, onAbrirMotivo = {}, onCerrarMotivo = {},
            fechaCalibracion = { "2027-08-20" }, visitaAbierta = { false }, ultimoMotivo = { "" },
            onAtras = {}, onRegistrarPrueba = {}, onIrActa = {},
            onMarcarRegistrado = { _, _, _ -> }, onFormalizar = {}, onDevolver = { _, _ -> }
        )
    }
}
