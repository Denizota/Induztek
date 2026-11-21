package cl.induztek.solicitudes_service.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeguridadTest {

    @Test
    void laClaveSeGuardaCifrada() {
        String cifrada = Seguridad.hashear("secreta");

        assertNotEquals("secreta", cifrada);
        assertTrue(cifrada.startsWith("$2"));
    }

    @Test
    void cadaCifradoUsaUnaSalDistinta() {
        assertNotEquals(Seguridad.hashear("secreta"), Seguridad.hashear("secreta"));
    }

    @Test
    void unaClaveNulaSeCifraIgual() {
        String cifrada = Seguridad.hashear(null);

        assertFalse(cifrada.isBlank());
    }
}
