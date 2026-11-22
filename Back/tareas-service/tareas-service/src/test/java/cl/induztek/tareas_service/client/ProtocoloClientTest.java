package cl.induztek.tareas_service.client;

import cl.induztek.tareas_service.dto.ProtocoloDto;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProtocoloClientTest {

    private HttpServer servidor;
    private String url;

    private void levantar(int estado, String cuerpo) throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext("/api/protocolos", intercambio -> {
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
    void listarPorVisitaDevuelveLosProtocolos() throws IOException {
        levantar(200, "[{\"id\":1,\"numero\":\"PT-2026-401\",\"idEquipo\":10,\"estado\":\"BORRADOR\"}]");

        List<ProtocoloDto> protocolos = new ProtocoloClient(url).listarPorVisita(1);

        assertEquals(1, protocolos.size());
        assertEquals("BORRADOR", protocolos.get(0).estado());
    }

    @Test
    void listarPorVisitaDevuelveListaVaciaCuandoNoHayProtocolos() throws IOException {
        levantar(200, "[]");

        assertTrue(new ProtocoloClient(url).listarPorVisita(1).isEmpty());
    }

    @Test
    void listarPorVisitaAvisaCuandoElServicioNoResponde() throws IOException {
        levantar(200, "[]");
        servidor.stop(0);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> new ProtocoloClient(url).listarPorVisita(1));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatusCode());
    }
}
