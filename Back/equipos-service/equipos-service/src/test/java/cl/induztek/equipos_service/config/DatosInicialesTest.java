package cl.induztek.equipos_service.config;

import cl.induztek.equipos_service.model.Equipo;
import cl.induztek.equipos_service.model.Instalacion;
import cl.induztek.equipos_service.model.Instrumento;
import cl.induztek.equipos_service.model.TipoEquipo;
import cl.induztek.equipos_service.repository.EquipoRepository;
import cl.induztek.equipos_service.repository.InstalacionRepository;
import cl.induztek.equipos_service.repository.InstrumentoRepository;
import cl.induztek.equipos_service.repository.TipoEquipoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatosInicialesTest {

    @Mock
    private InstalacionRepository instalacionRepository;

    @Mock
    private TipoEquipoRepository tipoEquipoRepository;

    @Mock
    private EquipoRepository equipoRepository;

    @Mock
    private InstrumentoRepository instrumentoRepository;

    private DatosIniciales datos() {
        return new DatosIniciales(instalacionRepository, tipoEquipoRepository, equipoRepository, instrumentoRepository);
    }

    @Test
    void siYaHayInstalacionesNoGuardaNada() {
        when(instalacionRepository.count()).thenReturn(6L);

        datos().run();

        verify(tipoEquipoRepository, never()).saveAll(any());
    }

    @Test
    void siLaTablaEstaVaciaCargaLaSemillaCompleta() {
        when(instalacionRepository.count()).thenReturn(0L);

        datos().run();

        verify(instalacionRepository).saveAll(org.mockito.ArgumentMatchers.<List<Instalacion>>any());
        verify(tipoEquipoRepository).saveAll(org.mockito.ArgumentMatchers.<List<TipoEquipo>>any());
        verify(equipoRepository).saveAll(org.mockito.ArgumentMatchers.<List<Equipo>>any());
        verify(instrumentoRepository).saveAll(org.mockito.ArgumentMatchers.<List<Instrumento>>any());
    }
}
