package cl.induztek.protocolos_service.dto;

import java.util.List;

public record ProtocoloDetalleResponse(
        ProtocoloResponse protocolo,
        List<PruebaResponse> pruebas,
        List<HistorialResponse> historial
) {
}
