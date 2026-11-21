package cl.induztek.protocolos_service.dto;

public record ProtocoloRequest(
        Integer idVisita,
        Integer idEquipo,
        Integer idUsuarioRegistra,
        Double temperaturaC,
        Double humedadPct,
        String observaciones
) {
}
