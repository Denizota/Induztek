package cl.induztek.usuarios_service.client;

import cl.induztek.usuarios_service.dto.RolDto;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RolClientTest {

    private HttpServer servidor;
    private String url;

    private void levantar(String ruta, int estado, String cuerpo) throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext(ruta, intercambio -> {
            byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);
            intercambio.getResponseHeaders().add("Content-Type", "application/json");
            intercambio.sendResponseHeaders(estado, bytes.length);
            intercambio.getResponseBody().write(bytes);
            intercambio.close();
        });
        servidor.start();
        url = "http://localhost:" + servidor.getAddress().getPort();
    }

    @AfterEach
    void bajar() {
        if (servidor != null) {
            servidor.stop(0);
        }
    }

    @Test
    void listarDevuelveLosRolesDelServicio() throws IOException {
        levantar("/api/roles", 200,
                "[{\"id\":1,\"codigo\":\"TECNICO\",\"nombre\":\"Técnico de terreno\"}," +
                        "{\"id\":2,\"codigo\":\"INGENIERO\",\"nombre\":\"Ingeniero en pruebas\"}]");

        List<RolDto> roles = new RolClient(url).listar();

        assertEquals(2, roles.size());
        assertEquals("INGENIERO", roles.get(1).codigo());
    }

    @Test
    void obtenerDevuelveElRolPedido() throws IOException {
        levantar("/api/roles/3", 200, "{\"id\":3,\"codigo\":\"JEFE_TECNICO\",\"nombre\":\"Jefe técnico\"}");

        assertEquals("Jefe técnico", new RolClient(url).obtener(3).nombre());
    }

    @Test
    void obtenerDevuelveNuloCuandoElRolNoExiste() throws IOException {
        levantar("/api/roles/9", 404, "{}");

        assertNull(new RolClient(url).obtener(9));
    }

    @Test
    void listarAvisaCuandoElServicioNoResponde() throws IOException {
        levantar("/api/roles", 200, "[]");
        servidor.stop(0);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> new RolClient(url).listar());

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatusCode());
    }

    @Test
    void obtenerAvisaCuandoElServicioNoResponde() throws IOException {
        levantar("/api/roles/2", 200, "{}");
        servidor.stop(0);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> new RolClient(url).obtener(2));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatusCode());
    }
}
