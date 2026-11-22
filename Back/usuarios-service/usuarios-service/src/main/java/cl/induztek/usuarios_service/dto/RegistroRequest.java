package cl.induztek.usuarios_service.dto;

public record RegistroRequest(
        String rut,
        String nombre,
        String correo,
        String contrasena,
        String telefono,
        Integer idRol,
        Integer idEmpresa,
        String cargo,
        String giro
) {
}
