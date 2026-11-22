package cl.induztek.usuarios_service.dto;

public record ActualizarRequest(
        String nombre,
        String correo,
        String telefono,
        Integer idRol,
        Integer idEmpresa,
        String cargo,
        String giro,
        String bio,
        String especialidades,
        String formacion,
        String certificaciones,
        Integer desde,
        Integer calificacion
) {
}
