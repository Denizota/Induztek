package cl.induztek.tareas_service.dto;

public record ProtocoloDto(
        Integer id,
        String numero,
        Integer idEquipo,
        String estado
) {
}
