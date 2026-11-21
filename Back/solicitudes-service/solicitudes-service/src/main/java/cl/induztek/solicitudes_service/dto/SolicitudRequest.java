package cl.induztek.solicitudes_service.dto;

public record SolicitudRequest(
        String nombre,
        String apellido,
        String pais,
        String ciudad,
        String correo,
        String contrasena,
        String justificacion,
        String tipo,
        Integer idUsuario,
        String campo,
        String valor
) {
}
