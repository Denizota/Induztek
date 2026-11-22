package cl.induztek.usuarios_service.config;

import cl.induztek.usuarios_service.model.Usuario;
import cl.induztek.usuarios_service.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatosInicialesTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Test
    void siYaHayUsuariosNoGuardaNada() {
        when(usuarioRepository.count()).thenReturn(19L);

        new DatosIniciales(usuarioRepository).run();

        verify(usuarioRepository, never()).saveAll(any());
    }

    @Test
    void siLaTablaEstaVaciaGuardaSeisEmpresasYTrecePersonas() {
        when(usuarioRepository.count()).thenReturn(0L);
        simularBusquedas();

        new DatosIniciales(usuarioRepository).run();

        ArgumentCaptor<List<Usuario>> captor = ArgumentCaptor.forClass(List.class);
        verify(usuarioRepository).saveAll(captor.capture());
        List<Usuario> guardados = captor.getValue();
        assertEquals(19, guardados.size());
        assertEquals("Envasadora del Pacífico S.A.", guardados.get(0).getNombre());
        assertEquals("Data Center Andes Norte SpA", guardados.get(5).getNombre());
        assertEquals("Thomas Peralta S.", guardados.get(6).getNombre());
        assertEquals("tperalta@induztek.ejemplo", guardados.get(6).getCorreo());
        assertNotNull(guardados.get(6).getPasswordHash());
        assertTrue(guardados.get(6).getActivo());
        assertEquals("Patricio Navarro Soto", guardados.get(18).getNombre());
    }

    @Test
    void completaLaFichaDeLasPersonasDelStaffYDeLosContactosDeEmpresa() {
        when(usuarioRepository.count()).thenReturn(0L);
        Usuario victor = simularBusquedas();

        new DatosIniciales(usuarioRepository).run();

        assertEquals(1, victor.getIdEmpresa());
        assertEquals("Jefe de Planta", victor.getCargo());
        assertEquals(2021, victor.getDesde());
        assertEquals(4, victor.getCalificacion());
        verify(usuarioRepository, times(6)).save(any(Usuario.class));
    }

    private Usuario simularBusquedas() {
        Usuario victor = Usuario.builder().id(9).nombre("Víctor Isidro R.").build();
        when(usuarioRepository.findById(7)).thenReturn(Optional.of(Usuario.builder().id(7).build()));
        when(usuarioRepository.findById(8)).thenReturn(Optional.of(Usuario.builder().id(8).build()));
        when(usuarioRepository.findById(9)).thenReturn(Optional.of(victor));
        when(usuarioRepository.findById(17)).thenReturn(Optional.of(Usuario.builder().id(17).build()));
        when(usuarioRepository.findById(18)).thenReturn(Optional.of(Usuario.builder().id(18).build()));
        when(usuarioRepository.findById(19)).thenReturn(Optional.of(Usuario.builder().id(19).build()));
        return victor;
    }
}
