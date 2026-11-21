package cl.induztek.roles_service.service;

import cl.induztek.roles_service.model.Rol;
import cl.induztek.roles_service.repository.RolRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RolServiceTest {

    @Mock
    private RolRepository rolRepository;

    @InjectMocks
    private RolService rolService;

    private Rol rol() {
        return Rol.builder().id(2).codigo("INGENIERO").nombre("Ingeniero en pruebas").build();
    }

    @Test
    void listarDevuelveTodosLosRoles() {
        when(rolRepository.findAll()).thenReturn(List.of(rol()));

        List<Rol> lista = rolService.listar();

        assertEquals(1, lista.size());
        assertEquals("INGENIERO", lista.get(0).getCodigo());
    }

    @Test
    void obtenerDevuelveElRolCuandoExiste() {
        when(rolRepository.findById(2)).thenReturn(Optional.of(rol()));

        assertSame("Ingeniero en pruebas", rolService.obtener(2).getNombre());
    }

    @Test
    void obtenerFallaCuandoNoExiste() {
        when(rolRepository.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> rolService.obtener(99));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    @Test
    void porCodigoDevuelveElRolCuandoExiste() {
        when(rolRepository.findByCodigo("INGENIERO")).thenReturn(Optional.of(rol()));

        assertEquals(2, rolService.porCodigo("INGENIERO").getId());
    }

    @Test
    void porCodigoFallaCuandoNoExiste() {
        when(rolRepository.findByCodigo("OTRO")).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> rolService.porCodigo("OTRO"));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }
}
