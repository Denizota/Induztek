package cl.induztek.protocolos_service.dto;

public record HistorialResponse(
        Integer id,
        String estadoAnterior,
        String estadoNuevo,
        Integer idUsuario,
        String fechaHora,
        String motivo
) {
}
