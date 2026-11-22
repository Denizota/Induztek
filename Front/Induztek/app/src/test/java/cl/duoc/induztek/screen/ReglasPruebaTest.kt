package cl.duoc.induztek.screen

import cl.duoc.induztek.model.Equipo
import cl.duoc.induztek.model.TipoPrueba
import cl.duoc.induztek.ui.screen.elementosPosibles
import cl.duoc.induztek.ui.screen.reescalar
import cl.duoc.induztek.ui.screen.relacionTeoricaPlaca
import org.junit.Assert.assertEquals
import org.junit.Test

class ReglasPruebaTest {

    private fun equipo(
        tipo: String,
        primaria: Double? = null,
        secundaria: Double? = null
    ) = Equipo(
        idEquipo = 1,
        idInstalacion = 1,
        tag = "TR-01",
        idTipoEquipo = 1,
        tipoEquipo = tipo,
        clasificacion = "PRIMARIO",
        marca = "Rhona",
        modelo = "RH-18733",
        numeroSerie = "RH-18733",
        anioFabricacion = 2012,
        potenciaKva = 1500.0,
        tensionPrimariaKv = primaria,
        tensionSecundariaKv = secundaria,
        corrienteNominalA = null,
        maxContactosUohm = null,
        observaciones = ""
    )

    @Test
    fun aislacion_transformadorPideSusTresElementos() {
        assertEquals(
            listOf("AT-T", "AT-BT", "BT-T"),
            elementosPosibles(equipo(
                "Transformador"),
                TipoPrueba.AISLACION)
        )
    }

    @Test
    fun aislacion_interruptorYCeldaPidenMediaTension() {
        val esperado = listOf("MT-BT", "MT-Tierra", "Fases-Tierra")
        assertEquals(
            esperado,
            elementosPosibles(equipo(
                "Interruptor"),
                TipoPrueba.AISLACION))
        assertEquals(
            esperado,
            elementosPosibles(equipo(
                "Celda MT"),
                TipoPrueba.AISLACION))
    }

    @Test
    fun aislacion_cableTrabajaPorFase() {
        assertEquals(
            listOf("Fase R", "Fase S", "Fase T"),
            elementosPosibles(equipo(
                "Cable de poder"),
                TipoPrueba.AISLACION)
        )
    }

    @Test
    fun aislacion_otroEquipoVaCompleto() {
        assertEquals(
            listOf("Equipo completo"),
            elementosPosibles(equipo(
                "Seccionador"),
                TipoPrueba.AISLACION)
        )
    }

    @Test
    fun devanados_tieneSeisBobinas() {
        assertEquals(
            6,
            elementosPosibles(equipo(
                "Transformador"),
                TipoPrueba.DEVANADOS).size)
    }

    @Test
    fun contactos_tieneTresPolos() {
        assertEquals(
            listOf("Polo 1", "Polo 2", "Polo 3"),
            elementosPosibles(equipo(
                "Interruptor"),
                TipoPrueba.CONTACTOS)
        )
    }

    @Test
    fun rele_tieneTresDisparos() {
        assertEquals(
            3,
            elementosPosibles(equipo(
                "Rele"),
                TipoPrueba.OPERACION_RELE).size)
    }

    @Test
    fun ttr_usaUnaSolaFila() {
        assertEquals(
            listOf("3 fases / cambiador de derivaciones"),
            elementosPosibles(equipo(
                "Transformador"),
                TipoPrueba.TTR)
        )
    }

    @Test
    fun relacionTeorica_dividelasTensionesDeLaPlaca() {
        assertEquals(
            "30",
            relacionTeoricaPlaca(equipo(
                "Transformador",
                12.0,
                0.4))
        )
    }

    @Test
    fun relacionTeorica_sinDatosDePlacaQuedaVacia() {
        assertEquals(
            "",
            relacionTeoricaPlaca(equipo(
                "Transformador")))
        assertEquals(
            "",
            relacionTeoricaPlaca(equipo(
                "Transformador",
                12.0,
                0.0)))
    }

    @Test
    fun reescalar_pasaLasLecturasAlCambiarDeUnidad() {
        val estado = mutableMapOf(
            "AT-T|AISLACION|LECTURA_1MIN" to "2",
            "AT-T|AISLACION|LECTURA_10MIN" to "6"
        )
        val unidad = reescalar(
            TipoPrueba.AISLACION,
            "AT-T", estado,
            "MΩ",
            "kΩ")
        assertEquals(
            "kΩ",
            unidad)
        assertEquals(
            "2000",
            estado["AT-T|AISLACION|LECTURA_1MIN"])
        assertEquals(
            "6000",
            estado["AT-T|AISLACION|LECTURA_10MIN"])
    }

    @Test
    fun reescalar_conLaMismaUnidadNoTocaLasLecturas() {
        val estado = mutableMapOf("AT-T|AISLACION|LECTURA_1MIN" to "2")
        reescalar(TipoPrueba.AISLACION,
            "AT-T", estado,
            "MΩ",
            "MΩ")
        assertEquals(
            "2",
            estado["AT-T|AISLACION|LECTURA_1MIN"])
    }
}
