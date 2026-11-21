package cl.induztek.roles_service.config;

import cl.induztek.roles_service.model.Rol;
import cl.induztek.roles_service.repository.RolRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatosInicialesTest {

    @Mock
    private RolRepository rolRepository;

    @Test
    void siYaHayRolesNoGuardaNada() {
        when(rolRepository.count()).thenReturn(4L);

        new DatosIniciales(rolRepository).run();

        verify(rolRepository, never()).saveAll(any());
    }

    @Test
    void siLaTablaEstaVaciaGuardaLosCuatroRoles() {
        when(rolRepository.count()).thenReturn(0L);

        new DatosIniciales(rolRepository).run();

        ArgumentCaptor<List<Rol>> captor = ArgumentCaptor.forClass(List.class);
        verify(rolRepository).saveAll(captor.capture());
        List<Rol> guardados = captor.getValue();
        assertEquals(4, guardados.size());
        assertEquals("TECNICO", guardados.get(0).getCodigo());
        assertEquals("CLIENTE", guardados.get(3).getCodigo());
    }
}
