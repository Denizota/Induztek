package cl.duoc.induztek.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeguridadTest {

    @Test
    fun hashear_guardarContraseniaHasheadisima(){
        val cifrada = Seguridad.hashear("Induztek2026")
        assertNotEquals("Induztek2026", cifrada)
        assertTrue(cifrada.startsWith("$2"))
    }

    @Test
    fun verificar_claveCorrecta(){
        val cifrada = Seguridad.hashear("Induztek2026")
        assertTrue(Seguridad.verificar("Induztek2026", cifrada))
    }

    @Test
    fun verificar_rechazaOtraClave() {
        val cifrada = Seguridad.hashear("Induztek2026")
        assertFalse(Seguridad.verificar("induztek2026", cifrada))
    }

    @Test
    fun hashear_generarUnValorDistintoCadaVezParaQNoHayaNRepetidosOk(){
        val primera = Seguridad.hashear("Induztek2026")
        val segunda = Seguridad.hashear("Induztek2026")
        assertNotEquals(primera, segunda)
        assertTrue(Seguridad.verificar("Induztek2026", primera))
        assertTrue(Seguridad.verificar("Induztek2026", segunda  ))
    }
}