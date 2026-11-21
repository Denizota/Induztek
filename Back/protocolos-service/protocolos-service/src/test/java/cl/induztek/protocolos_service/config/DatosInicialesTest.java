package cl.induztek.protocolos_service.config;

import cl.induztek.protocolos_service.model.TipoEquipoPrueba;
import cl.induztek.protocolos_service.model.TipoPrueba;
import cl.induztek.protocolos_service.repository.TipoEquipoPruebaRepository;
import cl.induztek.protocolos_service.repository.TipoPruebaRepository;
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
    private TipoPruebaRepository tipoPruebaRepository;

    @Mock
    private TipoEquipoPruebaRepository tipoEquipoPruebaRepository;

    @Test
    void siYaHayTiposNoGuardaNada() {
        when(tipoPruebaRepository.count()).thenReturn(5L);

        new DatosIniciales(tipoPruebaRepository, tipoEquipoPruebaRepository).run();

        verify(tipoEquipoPruebaRepository, never()).saveAll(any());
    }

    @Test
    void siLaTablaEstaVaciaCargaLosTiposYLasRelaciones() {
        when(tipoPruebaRepository.count()).thenReturn(0L);

        new DatosIniciales(tipoPruebaRepository, tipoEquipoPruebaRepository).run();

        verify(tipoPruebaRepository).saveAll(org.mockito.ArgumentMatchers.<List<TipoPrueba>>any());
        verify(tipoEquipoPruebaRepository).saveAll(org.mockito.ArgumentMatchers.<List<TipoEquipoPrueba>>any());
    }
}
