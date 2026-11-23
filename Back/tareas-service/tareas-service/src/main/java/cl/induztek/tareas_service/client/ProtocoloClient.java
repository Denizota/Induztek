package cl.induztek.tareas_service.client;

import cl.induztek.tareas_service.dto.ProtocoloDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
public class ProtocoloClient {

    private final WebClient webClient;

    public ProtocoloClient(@Value("${protocolos.service.url}") String protocolosUrl) {
        this.webClient = WebClient.builder().baseUrl(protocolosUrl).build();
    }

    public List<ProtocoloDto> listarPorVisita(Integer idVisita) {
        try {
            List<ProtocoloDto> protocolos = webClient.get()
                    .uri("/api/protocolos?visita={id}", idVisita)
                    .retrieve()
                    .bodyToFlux(ProtocoloDto.class)
                    .collectList()
                    .block();
            if (protocolos == null) {
                return List.of();
            }
            return protocolos;
        } catch (WebClientRequestException e) {
            throw servicioNoDisponible();
        }
    }

    private ResponseStatusException servicioNoDisponible() {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "El servicio de protocolos no está disponible");
    }
}
