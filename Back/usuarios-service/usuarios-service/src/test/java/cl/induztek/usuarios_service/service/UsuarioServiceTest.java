package cl.induztek.usuarios_service.service;

import cl.induztek.usuarios_service.client.RolClient;
import cl.induztek.usuarios_service.dto.RegistroRequest;
import cl.induztek.usuarios_service.dto.RolDto;
import cl.induztek.usuarios_service.model.Usuario;
import cl.induztek.usuarios_service.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RolClient rolClient;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void registrarGuardaUnUsuarioConDatosValidos() {
        RegistroRequest datos = new RegistroRequest("12.345.678-5", "Ana", "ana@ejemplo.cl",
                "clave123", "912345678", 4, null, "Técnica", "");
        when(usuarioRepository.existsByRut(datos.rut())).thenReturn(false);
        when(usuarioRepository.existsByCorreo(datos.correo())).thenReturn(false);
        when(rolClient.obtener(4)).thenReturn(new RolDto(4, "CLIENTE", "Cliente"));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> {
            Usuario usuario = invocacion.getArgument(0);
            usuario.setId(1);
            return usuario;
        });

        assertEquals("Ana", usuarioService.registrar(datos).nombre());
    }

    @Test
    void registrarRechazaUnRutVacio() {
        RegistroRequest datos = new RegistroRequest(" ", "Ana", null, "clave123", null,
                4, null, null, null);

        assertThrows(ResponseStatusException.class, () -> usuarioService.registrar(datos));
    }

    @Test
    void obtenerDevuelveElUsuarioEncontrado() {
        Usuario usuario = Usuario.builder().id(3).idRol(4).rut("12.345.678-5")
                .nombre("Ana").activo(true).build();
        when(usuarioRepository.findById(3)).thenReturn(Optional.of(usuario));
        when(rolClient.obtener(4)).thenReturn(new RolDto(4, "CLIENTE", "Cliente"));

        assertEquals("Ana", usuarioService.obtener(3).nombre());
    }

    @Test
    void iniciarSesionRechazaDatosIncompletos() {
        assertThrows(ResponseStatusException.class,
                () -> usuarioService.iniciarSesion(new cl.induztek.usuarios_service.dto.LoginRequest("", "clave123")));
    }
}
