package cl.duoc.induztek.data

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Assert
import org.junit.Test

class FechasTest {

    @Test
    fun fechaCorta_cambiaElORdenParaLaPantalla(){
        assertEquals("13-09-2025","2025-09-13".fechaCorta())
    }

    @Test
    fun fechaCorta_dejaIgualUnTextoRarisimo(){
        assertEquals("sin fecha", "sin fecha".fechaCorta())
    }

    @Test
    fun fechaHoy_saleEnElFormatoDeLaBase() {
        Assert.assertTrue(Regex("\\d{4}-\\d{2}-\\d{2}").matches(fechaHoy()))
    }

    @Test
    fun fechaHoraAhorita_traeFechaConLaHora(){
        assertTrue(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}").matches(fechaHoraAhora()))
    }


}