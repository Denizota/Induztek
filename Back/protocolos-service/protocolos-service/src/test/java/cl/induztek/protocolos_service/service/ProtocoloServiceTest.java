package cl.induztek.protocolos_service.service;

import cl.induztek.protocolos_service.dto.ProtocoloRequest;
import cl.induztek.protocolos_service.model.Protocolo;
import cl.induztek.protocolos_service.repository.HistorialEstadoRepository;
import cl.induztek.protocolos_service.repository.MedicionRepository;
import cl.induztek.protocolos_service.repository.ProtocoloRepository;
import cl.induztek.protocolos_service.repository.PruebaRepository;
import cl.induztek.protocolos_service.repository.TipoEquipoPruebaRepository;
import cl.induztek.protocolos_service.repository.TipoPruebaRepository;
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
class ProtocoloServiceTest {

    @Mock private ProtocoloRepository protocoloRepository;
    @Mock private PruebaRepository pruebaRepository;
    @Mock private MedicionRepository medicionRepository;
    @Mock private HistorialEstadoRepository historialRepository;
    @Mock private TipoPruebaRepository tipoPruebaRepository;
    @Mock private TipoEquipoPruebaRepository tipoEquipoPruebaRepository;

    @InjectMocks
    private ProtocoloService protocoloService;

    @Test
    void listarSinFiltrosDevuelveLosProtocolos() {
        when(protocoloRepository.findAllByOrderByFechaHoraPruebaDesc()).thenReturn(List.of());

        assertEquals(0, protocoloService.listar(null, null).size());
    }

    @Test
    void crearRechazaUnaVisitaFaltante() {
        ProtocoloRequest datos = new ProtocoloRequest(null, 2, 3, 20.0, 50.0, "");

        assertThrows(ResponseStatusException.class, () -> protocoloService.crear(datos));
    }

    @Test
    void crearGuardaUnProtocoloNuevo() {
        ProtocoloRequest datos = new ProtocoloRequest(4, 2, 3, 20.0, 50.0, "Revisión inicial");
        when(protocoloRepository.findByIdVisitaAndIdEquipo(4, 2)).thenReturn(Optional.empty());
        when(protocoloRepository.findFirstByNumeroStartingWithOrderByNumeroDesc(any()))
                .thenReturn(Optional.empty());
        when(protocoloRepository.save(any(Protocolo.class))).thenAnswer(invocacion -> {
            Protocolo protocolo = invocacion.getArgument(0);
            protocolo.setId(10);
            return protocolo;
        });

        assertEquals("BORRADOR", protocoloService.crear(datos).estado());
        verify(historialRepository).save(any());
    }

    @Test
    void obtenerInformaCuandoElProtocoloNoExiste() {
        when(protocoloRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> protocoloService.obtener(99));
    }
}
