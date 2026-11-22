package cl.induztek.usuarios_service.service;

import cl.induztek.usuarios_service.client.RolClient;
import cl.induztek.usuarios_service.config.Seguridad;
import cl.induztek.usuarios_service.dto.ActualizarRequest;
import cl.induztek.usuarios_service.dto.CambiarContrasenaRequest;
import cl.induztek.usuarios_service.dto.LoginRequest;
import cl.induztek.usuarios_service.dto.RegistroRequest;
import cl.induztek.usuarios_service.dto.RolDto;
import cl.induztek.usuarios_service.dto.UsuarioResponse;
import cl.induztek.usuarios_service.model.Usuario;
import cl.induztek.usuarios_service.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private static final Integer ROL_CLIENTE = 4;

    private final UsuarioRepository usuarioRepository;
    private final RolClient rolClient;

    public List<UsuarioResponse> listar(String buscar) {
        List<Usuario> usuarios = (buscar == null || buscar.isBlank())
                ? usuarioRepository.findAllByOrderByNombreAsc()
                : usuarioRepository
                        .findByNombreContainingIgnoreCaseOrRutContainingIgnoreCaseOrCorreoContainingIgnoreCaseOrderByNombreAsc(
                                buscar, buscar, buscar);

        Map<Integer, String> roles = rolClient.listar().stream()
                .collect(Collectors.toMap(RolDto::id, RolDto::nombre));

        return usuarios.stream()
                .map(usuario -> aRespuesta(usuario, roles.getOrDefault(usuario.getIdRol(), "")))
                .toList();
    }

    public UsuarioResponse obtener(Integer id) {
        return aRespuesta(buscar(id));
    }

    public List<UsuarioResponse> listarPorRol(Integer idRol) {
        return usuarioRepository.findByIdRolOrderByNombreAsc(idRol).stream()
                .map(this::aRespuesta)
                .toList();
    }

    public List<UsuarioResponse> listarPorActivo(Boolean activo) {
        return usuarioRepository.findByActivoOrderByNombreAsc(activo).stream()
                .map(this::aRespuesta)
                .toList();
    }

    public UsuarioResponse registrar(RegistroRequest datos) {
        if (vacio(datos.rut()) || vacio(datos.nombre())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rut y nombre son obligatorios");
        }
        if (datos.contrasena() == null || datos.contrasena().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña debe tener mínimo 6 caracteres");
        }
        if (usuarioRepository.existsByRut(datos.rut())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese rut ya está registrado");
        }
        if (!vacio(datos.correo()) && usuarioRepository.existsByCorreo(datos.correo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese correo ya está registrado");
        }

        Integer idRol = datos.idRol() != null ? datos.idRol() : ROL_CLIENTE;
        validarRol(idRol);

        Usuario nuevo = Usuario.builder()
                .idRol(idRol)
                .rut(datos.rut())
                .nombre(datos.nombre())
                .correo(textoONulo(datos.correo()))
                .telefono(datos.telefono() != null ? datos.telefono() : "")
                .passwordHash(Seguridad.hashear(datos.contrasena()))
                .activo(true)
                .idEmpresa(datos.idEmpresa())
                .cargo(datos.cargo() != null ? datos.cargo() : "")
                .giro(datos.giro() != null ? datos.giro() : "")
                .build();
        return aRespuesta(usuarioRepository.save(nuevo));
    }

    public UsuarioResponse iniciarSesion(LoginRequest datos) {
        if (vacio(datos.correo()) || datos.contrasena() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Correo y contraseña son obligatorios");
        }

        Usuario usuario = usuarioRepository.findByCorreo(datos.correo())
                .filter(u -> u.getPasswordHash() != null && Seguridad.verificar(datos.contrasena(), u.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos"));

        if (!usuario.getActivo()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La cuenta está suspendida");
        }
        return aRespuesta(usuario);
    }

    public UsuarioResponse actualizar(Integer id, ActualizarRequest datos) {
        Usuario usuario = buscar(id);

        if (vacio(datos.nombre())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio");
        }
        if (!vacio(datos.correo()) && usuarioRepository.existsByCorreoAndIdNot(datos.correo(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese correo ya está registrado");
        }
        if (datos.idRol() != null) {
            validarRol(datos.idRol());
            usuario.setIdRol(datos.idRol());
        }

        usuario.setNombre(datos.nombre());
        usuario.setCorreo(textoONulo(datos.correo()));
        usuario.setTelefono(datos.telefono() != null ? datos.telefono() : "");
        usuario.setIdEmpresa(datos.idEmpresa());
        usuario.setCargo(datos.cargo() != null ? datos.cargo() : "");
        usuario.setGiro(datos.giro() != null ? datos.giro() : "");
        usuario.setBio(datos.bio() != null ? datos.bio() : "");
        usuario.setEspecialidades(datos.especialidades() != null ? datos.especialidades() : "");
        usuario.setFormacion(datos.formacion() != null ? datos.formacion() : "");
        usuario.setCertificaciones(datos.certificaciones() != null ? datos.certificaciones() : "");
        usuario.setDesde(datos.desde());
        usuario.setCalificacion(datos.calificacion());
        return aRespuesta(usuarioRepository.save(usuario));
    }

    public void cambiarContrasena(Integer id, CambiarContrasenaRequest datos) {
        Usuario usuario = buscar(id);

        if (usuario.getPasswordHash() == null || !Seguridad.verificar(datos.actual(), usuario.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La contraseña actual es incorrecta");
        }
        if (datos.nueva() == null || datos.nueva().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña debe tener mínimo 6 caracteres");
        }

        usuario.setPasswordHash(Seguridad.hashear(datos.nueva()));
        usuarioRepository.save(usuario);
    }

    public UsuarioResponse marcarActivo(Integer id, Boolean activo) {
        if (activo == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indica si la cuenta queda activa o suspendida");
        }
        Usuario usuario = buscar(id);
        usuario.setActivo(activo);
        return aRespuesta(usuarioRepository.save(usuario));
    }

    public void eliminar(Integer id) {
        usuarioRepository.delete(buscar(id));
    }

    private Usuario buscar(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private void validarRol(Integer idRol) {
        if (rolClient.obtener(idRol) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol indicado no existe");
        }
    }

    private UsuarioResponse aRespuesta(Usuario usuario) {
        RolDto rol = rolClient.obtener(usuario.getIdRol());
        return aRespuesta(usuario, rol != null ? rol.nombre() : "");
    }

    private UsuarioResponse aRespuesta(Usuario usuario, String rolNombre) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getRut(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getTelefono(),
                usuario.getActivo(),
                usuario.getIdRol(),
                rolNombre,
                usuario.getIdEmpresa(),
                usuario.getCargo(),
                usuario.getGiro(),
                usuario.getBio(),
                usuario.getEspecialidades(),
                usuario.getFormacion(),
                usuario.getCertificaciones(),
                usuario.getDesde(),
                usuario.getCalificacion()
        );
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private String textoONulo(String texto) {
        if (vacio(texto)) {
            return null;
        }
        return texto;
    }
}
