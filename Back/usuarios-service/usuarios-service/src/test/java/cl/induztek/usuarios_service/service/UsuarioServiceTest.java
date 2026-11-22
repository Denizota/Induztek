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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolClient rolClient;

    private UsuarioService servicio() {
        return new UsuarioService(usuarioRepository, rolClient);
    }

    private Usuario usuario() {
        return Usuario.builder()
                .id(5).idRol(2).rut("17.284.519-3").nombre("Camila Soto")
                .correo("camila@duoc.cl").telefono("+56911111111")
                .passwordHash(Seguridad.hashear("secreta")).activo(true)
                .cargo("").giro("").bio("").especialidades("").formacion("").certificaciones("")
                .build();
    }

    private RegistroRequest registro(String rut, String correo, String clave, Integer idRol) {
        return new RegistroRequest(rut, "Camila Soto", correo, clave, "+56911111111", idRol, null, "", "");
    }

    private ResponseStatusException errorDe(Runnable accion) {
        return assertThrows(ResponseStatusException.class, accion::run);
    }

    @Test
    void listarSinBusquedaUsaElOrdenPorNombre() {
        when(usuarioRepository.findAllByOrderByNombreAsc()).thenReturn(List.of(usuario()));
        when(rolClient.listar()).thenReturn(List.of(new RolDto(2, "INGENIERO", "Ingeniero en pruebas")));

        List<UsuarioResponse> lista = servicio().listar(null);

        assertEquals(1, lista.size());
        assertEquals("Ingeniero en pruebas", lista.get(0).rolNombre());
    }

    @Test
    void listarConBusquedaFiltraPorNombreRutOCorreo() {
        when(usuarioRepository
                .findByNombreContainingIgnoreCaseOrRutContainingIgnoreCaseOrCorreoContainingIgnoreCaseOrderByNombreAsc(
                        "camila", "camila", "camila"))
                .thenReturn(List.of(usuario()));
        when(rolClient.listar()).thenReturn(List.of(new RolDto(2, "INGENIERO", "Ingeniero en pruebas")));

        assertEquals(1, servicio().listar("camila").size());
    }

    @Test
    void listarDejaElNombreDelRolVacioSiNoLlega() {
        when(usuarioRepository.findAllByOrderByNombreAsc()).thenReturn(List.of(usuario()));
        when(rolClient.listar()).thenReturn(List.of());

        assertEquals("", servicio().listar(" ").get(0).rolNombre());
    }

    @Test
    void obtenerDevuelveElUsuarioConSuRol() {
        when(usuarioRepository.findById(5)).thenReturn(Optional.of(usuario()));
        when(rolClient.obtener(2)).thenReturn(new RolDto(2, "INGENIERO", "Ingeniero en pruebas"));

        assertEquals("Camila Soto", servicio().obtener(5).nombre());
    }

    @Test
    void obtenerFallaSiNoExiste() {
        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, errorDe(() -> servicio().obtener(99)).getStatusCode());
    }

    @Test
    void listarPorRolFiltraConElRolPedido() {
        when(usuarioRepository.findByIdRolOrderByNombreAsc(2)).thenReturn(List.of(usuario()));
        when(rolClient.obtener(2)).thenReturn(new RolDto(2, "INGENIERO", "Ingeniero en pruebas"));

        assertEquals(1, servicio().listarPorRol(2).size());
    }

    @Test
    void listarPorActivoFiltraPorLaColumnaActivo() {
        when(usuarioRepository.findByActivoOrderByNombreAsc(false)).thenReturn(List.of(usuario()));
        when(rolClient.obtener(2)).thenReturn(new RolDto(2, "INGENIERO", "Ingeniero en pruebas"));

        assertEquals(1, servicio().listarPorActivo(false).size());
    }

    @Test
    void registrarExigeRutYNombre() {
        assertEquals(HttpStatus.BAD_REQUEST,
                errorDe(() -> servicio().registrar(registro("", "camila@duoc.cl", "secreta", 2))).getStatusCode());
    }

    @Test
    void registrarExigeClaveDeMinimoSeis() {
        assertEquals(HttpStatus.BAD_REQUEST,
                errorDe(() -> servicio().registrar(registro("17.284.519-3", "camila@duoc.cl", "123", 2))).getStatusCode());
    }

    @Test
    void registrarRechazaRutRepetido() {
        when(usuarioRepository.existsByRut("17.284.519-3")).thenReturn(true);

        assertEquals(HttpStatus.CONFLICT,
                errorDe(() -> servicio().registrar(registro("17.284.519-3", "camila@duoc.cl", "secreta", 2))).getStatusCode());
    }

    @Test
    void registrarRechazaCorreoRepetido() {
        when(usuarioRepository.existsByRut("17.284.519-3")).thenReturn(false);
        when(usuarioRepository.existsByCorreo("camila@duoc.cl")).thenReturn(true);

        assertEquals(HttpStatus.CONFLICT,
                errorDe(() -> servicio().registrar(registro("17.284.519-3", "camila@duoc.cl", "secreta", 2))).getStatusCode());
    }

    @Test
    void registrarRechazaUnRolInexistente() {
        when(usuarioRepository.existsByRut("17.284.519-3")).thenReturn(false);
        when(usuarioRepository.existsByCorreo("camila@duoc.cl")).thenReturn(false);
        when(rolClient.obtener(9)).thenReturn(null);

        assertEquals(HttpStatus.BAD_REQUEST,
                errorDe(() -> servicio().registrar(registro("17.284.519-3", "camila@duoc.cl", "secreta", 9))).getStatusCode());
    }

    @Test
    void registrarGuardaAlUsuarioConSuClaveCifrada() {
        when(usuarioRepository.existsByRut("17.284.519-3")).thenReturn(false);
        when(usuarioRepository.existsByCorreo("camila@duoc.cl")).thenReturn(false);
        when(rolClient.obtener(2)).thenReturn(new RolDto(2, "INGENIERO", "Ingeniero en pruebas"));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse respuesta = servicio().registrar(registro("17.284.519-3", "camila@duoc.cl", "secreta", 2));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();
        assertTrue(guardado.getActivo());
        assertTrue(guardado.getPasswordHash().startsWith("$2"));
        assertEquals("", guardado.getCargo());
        assertEquals("", guardado.getGiro());
        assertEquals("Ingeniero en pruebas", respuesta.rolNombre());
    }

    @Test
    void registrarSinRolAsumeClienteYDejaElCorreoVacioEnNulo() {
        when(usuarioRepository.existsByRut("17.284.519-3")).thenReturn(false);
        when(rolClient.obtener(4)).thenReturn(new RolDto(4, "CLIENTE", "Cliente / Visitante"));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        servicio().registrar(new RegistroRequest("17.284.519-3", "Camila Soto", "  ", "secreta",
                null, null, null, "", ""));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals(4, captor.getValue().getIdRol());
        assertNull(captor.getValue().getCorreo());
        assertEquals("", captor.getValue().getTelefono());
    }

    @Test
    void iniciarSesionExigeCorreoYClave() {
        assertEquals(HttpStatus.BAD_REQUEST,
                errorDe(() -> servicio().iniciarSesion(new LoginRequest("", "secreta"))).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST,
                errorDe(() -> servicio().iniciarSesion(new LoginRequest("camila@duoc.cl", null))).getStatusCode());
    }

    @Test
    void iniciarSesionRechazaCorreoDesconocido() {
        when(usuarioRepository.findByCorreo("nadie@duoc.cl")).thenReturn(Optional.empty());

        assertEquals(HttpStatus.UNAUTHORIZED,
                errorDe(() -> servicio().iniciarSesion(new LoginRequest("nadie@duoc.cl", "secreta"))).getStatusCode());
    }

    @Test
    void iniciarSesionRechazaClaveIncorrecta() {
        when(usuarioRepository.findByCorreo("camila@duoc.cl")).thenReturn(Optional.of(usuario()));

        assertEquals(HttpStatus.UNAUTHORIZED,
                errorDe(() -> servicio().iniciarSesion(new LoginRequest("camila@duoc.cl", "otra"))).getStatusCode());
    }

    @Test
    void iniciarSesionRechazaUnaCuentaSuspendida() {
        Usuario suspendido = usuario();
        suspendido.setActivo(false);
        when(usuarioRepository.findByCorreo("camila@duoc.cl")).thenReturn(Optional.of(suspendido));

        assertEquals(HttpStatus.FORBIDDEN,
                errorDe(() -> servicio().iniciarSesion(new LoginRequest("camila@duoc.cl", "secreta"))).getStatusCode());
    }

    @Test
    void iniciarSesionDevuelveElUsuarioCuandoTodoCoincide() {
        when(usuarioRepository.findByCorreo("camila@duoc.cl")).thenReturn(Optional.of(usuario()));
        when(rolClient.obtener(2)).thenReturn(new RolDto(2, "INGENIERO", "Ingeniero en pruebas"));

        assertEquals(5, servicio().iniciarSesion(new LoginRequest("camila@duoc.cl", "secreta")).id());
    }

    @Test
    void actualizarExigeNombre() {
        when(usuarioRepository.findById(5)).thenReturn(Optional.of(usuario()));

        ActualizarRequest datos = new ActualizarRequest("", "", "", null, null, "", "", "", "", "", "", null, null);

        assertEquals(HttpStatus.BAD_REQUEST, errorDe(() -> servicio().actualizar(5, datos)).getStatusCode());
    }

    @Test
    void actualizarRechazaCorreoDeOtroUsuario() {
        when(usuarioRepository.findById(5)).thenReturn(Optional.of(usuario()));
        when(usuarioRepository.existsByCorreoAndIdNot("otro@duoc.cl", 5)).thenReturn(true);

        ActualizarRequest datos = new ActualizarRequest("Camila Soto", "otro@duoc.cl", "", null,
                null, "", "", "", "", "", "", null, null);

        assertEquals(HttpStatus.CONFLICT, errorDe(() -> servicio().actualizar(5, datos)).getStatusCode());
    }

    @Test
    void actualizarRechazaUnRolInexistente() {
        when(usuarioRepository.findById(5)).thenReturn(Optional.of(usuario()));
        when(rolClient.obtener(9)).thenReturn(null);

        ActualizarRequest datos = new ActualizarRequest("Camila Soto", "", "", 9,
                null, "", "", "", "", "", "", null, null);

        assertEquals(HttpStatus.BAD_REQUEST, errorDe(() -> servicio().actualizar(5, datos)).getStatusCode());
    }

    @Test
    void actualizarGuardaTodosLosCamposDelPerfil() {
        Usuario encontrado = usuario();
        when(usuarioRepository.findById(5)).thenReturn(Optional.of(encontrado));
        when(usuarioRepository.existsByCorreoAndIdNot("new@duoc.cl", 5)).thenReturn(false);
        when(rolClient.obtener(3)).thenReturn(new RolDto(3, "JEFE_TECNICO", "Jefe técnico"));
        when(usuarioRepository.save(encontrado)).thenReturn(encontrado);

        ActualizarRequest datos = new ActualizarRequest("Camila Soto", "new@duoc.cl", "+56922222222", 3,
                2, "Ingeniera", "Energía", "Bio", "Alta tensión", "UTFSM", "SEC", 2019, 5);

        UsuarioResponse respuesta = servicio().actualizar(5, datos);

        assertEquals("Ingeniera", respuesta.cargo());
        assertEquals("Energía", respuesta.giro());
        assertEquals(2019, respuesta.desde());
        assertEquals(5, respuesta.calificacion());
        assertEquals("Jefe técnico", respuesta.rolNombre());
    }

    @Test
    void actualizarLimpiaLosCamposNulos() {
        Usuario encontrado = usuario();
        when(usuarioRepository.findById(5)).thenReturn(Optional.of(encontrado));
        when(rolClient.obtener(2)).thenReturn(new RolDto(2, "INGENIERO", "Ingeniero en pruebas"));
        when(usuarioRepository.save(encontrado)).thenReturn(encontrado);

        ActualizarRequest datos = new ActualizarRequest("Camila Soto", " ", null, null,
                null, null, null, null, null, null, null, null, null);

        UsuarioResponse respuesta = servicio().actualizar(5, datos);

        assertEquals("", respuesta.telefono());
        assertEquals("", respuesta.bio());
        assertNull(respuesta.idEmpresa());
    }

    @Test
    void actualizarFallaSiNoExiste() {
        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

        ActualizarRequest datos = new ActualizarRequest("Camila", "", "", null,
                null, "", "", "", "", "", "", null, null);

        assertEquals(HttpStatus.NOT_FOUND, errorDe(() -> servicio().actualizar(99, datos)).getStatusCode());
    }

    @Test
    void cambiarContrasenaRechazaLaClaveActualIncorrecta() {
        when(usuarioRepository.findById(5)).thenReturn(Optional.of(usuario()));

        assertEquals(HttpStatus.UNAUTHORIZED, errorDe(() ->
                servicio().cambiarContrasena(5, new CambiarContrasenaRequest("mala", "nuevaClave"))).getStatusCode());
    }

    @Test
    void cambiarContrasenaRechazaUnaClaveNuevaCorta() {
        when(usuarioRepository.findById(5)).thenReturn(Optional.of(usuario()));

        assertEquals(HttpStatus.BAD_REQUEST, errorDe(() ->
                servicio().cambiarContrasena(5, new CambiarContrasenaRequest("secreta", "123"))).getStatusCode());
    }

    @Test
    void cambiarContrasenaGuardaLaNuevaClave() {
        Usuario encontrado = usuario();
        when(usuarioRepository.findById(5)).thenReturn(Optional.of(encontrado));

        servicio().cambiarContrasena(5, new CambiarContrasenaRequest("secreta", "nuevaClave"));

        assertTrue(Seguridad.verificar("nuevaClave", encontrado.getPasswordHash()));
        verify(usuarioRepository).save(encontrado);
    }

    @Test
    void cambiarContrasenaFallaSiNoExiste() {
        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, errorDe(() ->
                servicio().cambiarContrasena(99, new CambiarContrasenaRequest("secreta", "nuevaClave"))).getStatusCode());
    }

    @Test
    void marcarActivoExigeElValor() {
        assertEquals(HttpStatus.BAD_REQUEST, errorDe(() -> servicio().marcarActivo(5, null)).getStatusCode());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void marcarActivoSuspendeLaCuenta() {
        Usuario encontrado = usuario();
        when(usuarioRepository.findById(5)).thenReturn(Optional.of(encontrado));
        when(rolClient.obtener(2)).thenReturn(new RolDto(2, "INGENIERO", "Ingeniero en pruebas"));
        when(usuarioRepository.save(encontrado)).thenReturn(encontrado);

        assertEquals(false, servicio().marcarActivo(5, false).activo());
    }

    @Test
    void eliminarBorraAlUsuario() {
        Usuario encontrado = usuario();
        when(usuarioRepository.findById(5)).thenReturn(Optional.of(encontrado));

        servicio().eliminar(5);

        verify(usuarioRepository).delete(encontrado);
    }

    @Test
    void eliminarFallaSiNoExiste() {
        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, errorDe(() -> servicio().eliminar(99)).getStatusCode());
    }
}
