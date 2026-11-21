package cl.induztek.protocolos_service.dto;

public record ProtocoloResponse(
        Integer id,
        String numero,
        Integer idVisita,
        Integer idEquipo,
        Integer idUsuarioRegistra,
        String fechaHoraPrueba,
        Double temperaturaC,
        Double humedadPct,
        String estado,
        String observaciones
) {
}
