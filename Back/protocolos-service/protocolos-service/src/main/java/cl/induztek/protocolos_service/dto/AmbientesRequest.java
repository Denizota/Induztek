package cl.induztek.protocolos_service.dto;

public record AmbientesRequest(
        Double temperaturaC,
        Double humedadPct,
        String observaciones
) {
}
