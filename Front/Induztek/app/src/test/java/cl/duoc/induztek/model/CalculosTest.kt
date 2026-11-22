package cl.duoc.induztek.model

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Assert
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculosTest {

    @Test
    fun indicePolarizacion_dividirEntreLasDosLecturas(){
        assertEquals(
            3.0,
            Calculos.indicePolarizacion(
                2.0,
                6.0)!!,
            0.0001)
    }

    @Test
    fun indicePolarizacion_sinLecturasDaUnNULL(){
        assertNull(Calculos.indicePolarizacion(
            null,
            6.0))
        assertNull(Calculos.indicePolarizacion(
            2.0,
            null))
        assertNull(Calculos.indicePolarizacion(
            0.0,
            6.0))
    }

    @Test
    fun aislacion_bajoCienMegaSeRechaza(){
        assertEquals(
            Resultado.RECHAZADO,
            Calculos.resultadoAislacion(
                3.0,
                80.0))
    }

    @Test
    fun aislacion_segunIndicePolarizacion() {
        Assert.assertEquals(
            Resultado.RECHAZADO,
            Calculos.resultadoAislacion(
                0.5,
                500.0))
        Assert.assertEquals(
            Resultado.OBSERVADO,
            Calculos.resultadoAislacion(
                1.5,
                500.0))
        Assert.assertEquals(
            Resultado.APROBADO,
            Calculos.resultadoAislacion(
                2.5,
                500.0))
        Assert.assertEquals(
            Resultado.APROBADO,
            Calculos.resultadoAislacion(
                null,
                500.0))
    }

    @Test
    fun errorTtr_calculaDiferenciaPorcentual() {
        Assert.assertEquals(2.0,
            Calculos.errorTtr(
            100.0,
            102.0)!!,
            0.0001)
        Assert.assertNull(Calculos.errorTtr(
            0.0,
            102.0))
        Assert.assertNull(Calculos.errorTtr(
            null,
            102.0))
    }

    @Test
    fun resultadoTtr_usaLosTramosDeLaPauta() {
        Assert.assertEquals(
            Resultado.APROBADO,
            Calculos.resultadoTtr(
                0.4))
        Assert.assertEquals(
            Resultado.OBSERVADO,
            Calculos.resultadoTtr(
                0.8))
        Assert.assertEquals(
            Resultado.RECHAZADO,
            Calculos.resultadoTtr(
                1.5))
    }

    @Test
    fun contactos_comparaContraLaPlaca() {
        Assert.assertEquals(
            100.0,
            Calculos.porcentajeContactos(
                50.0,
                50.0)!!,
            0.0001)
        Assert.assertNull(Calculos.porcentajeContactos(
            50.0,
            0.0))
        Assert.assertEquals(
            Resultado.APROBADO,
            Calculos.resultadoContactos(
                95.0))
        Assert.assertEquals(
            Resultado.OBSERVADO,
            Calculos.resultadoContactos(
                110.0))
        Assert.assertEquals(
            Resultado.RECHAZADO,
            Calculos.resultadoContactos(
                130.0))
    }

    @Test
    fun rele_consideraQueOpereYLaDiferencia() {
        Assert.assertEquals(
            Resultado.RECHAZADO,
            Calculos.resultadoRele(
                false,
                1.0))
        Assert.assertEquals(
            Resultado.APROBADO,
            Calculos.resultadoRele(
                true,
                3.0))
        Assert.assertEquals(
            Resultado.OBSERVADO,
            Calculos.resultadoRele(
                true,
                8.0))
        Assert.assertEquals(
            Resultado.RECHAZADO,
            Calculos.resultadoRele(
                true,
                12.0))
    }

    @Test
    fun variacion_marcaSoloSobre25PorCiento() {
        Assert.assertEquals(
            50.0,
            Calculos.variacionPct(
                150.0,
                100.0)!!,
            0.0001)
        Assert.assertNull(Calculos.variacionPct(
            150.0,
            0.0))
        assertTrue(Calculos.esVariacionSignificativa(30.0))
        assertTrue(Calculos.esVariacionSignificativa(-30.0))
        assertFalse(Calculos.esVariacionSignificativa(25.0))
        assertFalse(Calculos.esVariacionSignificativa(null))
    }

    @Test
    fun resultadoGlobal_tomaElPeor() {
        Assert.assertEquals(
            Resultado.RECHAZADO,
            Calculos.resultadoGlobal(
                listOf(
                    Resultado.APROBADO,
                    Resultado.RECHAZADO,
                    Resultado.OBSERVADO
                )
            )
        )
        Assert.assertEquals(
            Resultado.OBSERVADO,
            Calculos.resultadoGlobal(
                listOf(null, Resultado.APROBADO, Resultado.OBSERVADO))
        )
        Assert.assertNull(Calculos.resultadoGlobal(
            listOf(null, null)))
    }

    @Test
    fun rutValido_revisaElDigitoVerificador() {
        assertTrue(Calculos.rutValido("17.284.519-3"))
        assertTrue(Calculos.rutValido("12345678-5"))
        assertFalse(Calculos.rutValido("17.284.519-4"))
        assertFalse(Calculos.rutValido("abc"))
    }

    @Test
    fun formatear_usaElFormatoChileno() {
        Assert.assertEquals(
            "—",
            Calculos.formatear(
                null))
        Assert.assertEquals(
            "1.234,50",
            Calculos.formatear(
                1234.5))
        Assert.assertEquals(
            "0,500",
            Calculos.formatear(
            0.5,
            3))
    }
}
