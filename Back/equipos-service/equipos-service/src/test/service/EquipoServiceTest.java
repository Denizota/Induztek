package cl.induztek.equipos_service.service;

import cl.induztek.equipos_service.dto.EquipoRequest;
import cl.induztek.equipos_service.model.Instalacion;
import cl.induztek.equipos_service.model.TipoEquipo;
import cl.induztek.equipos_service.repository.EquipoRepository;
import cl.induztek.equipos_service.repository.InstalacionRepository;
import cl.induztek.equipos_service.repository.InstrumentoRepository;
import cl.induztek.equipos_service.repository.TipoEquipoRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipoServiceTest {

    @Mock private EquipoRepository equipoRepository;
    @Mock private InstalacionRepository instalacionRepository;
    @Mock private TipoEquipoRepository tipoEquipoRepository;
    @Mock private InstrumentoRepository instrumentoRepository;

    @InjectMocks
    private EquipoService equipoService;

    @Test
    void listarInstalacionesSinClienteDevuelveTodas() {
        when(instalacionRepository.findAll()).thenReturn(List.of());

        assertEquals(0, equipoService.listarInstalaciones(null).size());
    }

    @Test
    void listarInstalacionesDeUnClienteUsaSuIdentificador() {
        when(instalacionRepository.findByIdUsuarioClienteOrderByNombreAsc(7)).thenReturn(List.of());

        assertEquals(0, equipoService.listarInstalaciones(7).size());
    }

    @Test
    void instrumentosVencidosRechazaUnaFechaVacia() {
        assertThrows(ResponseStatusException.class, () -> equipoService.instrumentosVencidos(" "));
    }

    @Test
    void listarSinFiltrosDevuelveUnaListaVaciaCuandoNoHayEquipos() {
        when(equipoRepository.findAll()).thenReturn(List.of());
        when(tipoEquipoRepository.findAll()).thenReturn(List.of());
        when(instalacionRepository.findAll()).thenReturn(List.of());

        assertEquals(0, equipoService.listar(null, null).size());
    }

    @Test
    void crearRechazaUnEquipoSinInstalacion() {
        EquipoRequest datos = new EquipoRequest(
            null, 
            "TR-01", 
            1, 
            "Marca", 
            "Modelo", 
            "Serie",
            2020, 
            100.0, 
            12.0, 
            0.4, 
            5.0, 
            100.0, 
            "");

        assertThrows(ResponseStatusException.class, () -> equipoService.crear(datos));
    }

    @Test
    void listarTiposDevuelveLosTiposDelRepositorio() {
        TipoEquipo tipo = new TipoEquipo();
        when(tipoEquipoRepository.findAll()).thenReturn(List.of(tipo));

        assertEquals(1, equipoService.listarTipos().size());
        verify(tipoEquipoRepository).findAll();
    }
}
