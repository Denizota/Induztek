package cl.induztek.roles_service.service;

import cl.induztek.roles_service.model.Rol;
import cl.induztek.roles_service.repository.RolRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RolServiceTest {

    @Mock
    private RolRepository rolRepository;

    @InjectMocks
    private RolService rolService;

    @Test
    void listarDevuelveLosRolesDelRepositorio() {
        List<Rol> roles = List.of(new Rol(1, "ADMIN", "Administrador"));
        when(rolRepository.findAll()).thenReturn(roles);

        assertEquals(roles, rolService.listar());
    }

    @Test
    void obtenerDevuelveElRolCuandoExiste() {
        Rol rol = new Rol(1, "ADMIN", "Administrador");
        when(rolRepository.findById(1)).thenReturn(Optional.of(rol));

        assertEquals(rol, rolService.obtener(1));
    }

    @Test
    void obtenerInformaCuandoElRolNoExiste() {
        when(rolRepository.findById(8)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> rolService.obtener(8));
    }

    @Test
    void porCodigoBuscaElRolIndicado() {
        Rol rol = new Rol(2, "TECNICO", "Técnico");
        when(rolRepository.findByCodigo("TECNICO")).thenReturn(Optional.of(rol));

        assertEquals(rol, rolService.porCodigo("TECNICO"));
    }

    @Test
    void porCodigoInformaCuandoNoEncuentraElRol() {
        when(rolRepository.findByCodigo("OTRO")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> rolService.porCodigo("OTRO"));
    }
}
