package cl.induztek.usuarios_service.config;

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
    void verificarAceptaLaClaveCorrecta() {
        assertTrue(Seguridad.verificar("secreta", Seguridad.hashear("secreta")));
    }

    @Test
    void verificarRechazaUnaClaveDistinta() {
        assertFalse(Seguridad.verificar("otra", Seguridad.hashear("secreta")));
    }
}
