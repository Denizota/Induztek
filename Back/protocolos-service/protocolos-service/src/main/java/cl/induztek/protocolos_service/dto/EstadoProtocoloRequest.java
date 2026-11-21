package cl.induztek.protocolos_service.dto;

public record EstadoProtocoloRequest(
        String estado,
        Integer idUsuario,
        String motivo
) {
}
