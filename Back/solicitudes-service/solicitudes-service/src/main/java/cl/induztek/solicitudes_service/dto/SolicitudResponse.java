package cl.induztek.solicitudes_service.dto;

public record SolicitudResponse(
        Integer id,
        String nombre,
        String apellido,
        String pais,
        String ciudad,
        String correo,
        String justificacion,
        String estado,
        String tipo,
        Integer idUsuario,
        String campo,
        String valor
) {
}
