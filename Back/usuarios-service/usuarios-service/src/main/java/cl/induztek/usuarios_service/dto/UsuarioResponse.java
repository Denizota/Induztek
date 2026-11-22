package cl.induztek.usuarios_service.dto;

public record UsuarioResponse(
        Integer id,
        String rut,
        String nombre,
        String correo,
        String telefono,
        Boolean activo,
        Integer idRol,
        String rolNombre,
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
