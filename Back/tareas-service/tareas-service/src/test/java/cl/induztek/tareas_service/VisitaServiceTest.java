package cl.induztek.tareas_service;

import cl.induztek.tareas_service.client.ProtocoloClient;
import cl.induztek.tareas_service.dto.CierreRequest;
import cl.induztek.tareas_service.dto.ProtocoloDto;
import cl.induztek.tareas_service.dto.VisitaRequest;
import cl.induztek.tareas_service.model.Visita;
import cl.induztek.tareas_service.repository.RecepcionActaRepository;
import cl.induztek.tareas_service.repository.VisitaParticipanteRepository;
import cl.induztek.tareas_service.repository.VisitaRepository;
import cl.induztek.tareas_service.service.VisitaService;

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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisitaServiceTest {

    @Mock
    private VisitaRepository visitaRepository;

    @Mock
    private VisitaParticipanteRepository participanteRepository;

    @Mock
    private RecepcionActaRepository actaRepository;

    @Mock
    private ProtocoloClient protocoloClient;

    @InjectMocks
    private VisitaService visitaService;

    @Test
    void obtenerDevuelveLaVisitaEncontrada() {
        Visita visita = Visita.builder()
                .id(1)
                .numeroOt("OT-2026-1301")
                .idInstalacion(3)
                .fecha("2026-11-19")
                .tipoVisita("SOLO_PRUEBAS")
                .idIngenieroCargo(2)
                .descripcionMantenimiento("")
                .build();

        when(visitaRepository.findById(1)).thenReturn(Optional.of(visita));

        assertEquals(3, visitaService.obtener(1).idInstalacion());
    }

    @Test
    void obtenerFallaSiLaVisitaNoExiste() {
        when(visitaRepository.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> visitaService.obtener(99)
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    @Test
    void crearFallaSiNoSeIndicaInstalacion() {
        VisitaRequest datos = new VisitaRequest(null, null, null, null, null);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> visitaService.crear(datos)
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }

    @Test
    void crearGuardaLaVisitaConValoresPorDefecto() {
        when(visitaRepository.count()).thenReturn(0L);
        when(visitaRepository.save(any(Visita.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        var respuesta = visitaService.crear(
                new VisitaRequest(3, null, null, 2, null)
        );

        assertEquals("SOLO_PRUEBAS", respuesta.tipoVisita());
        assertEquals("", respuesta.descripcionMantenimiento());
        verify(visitaRepository).save(any(Visita.class));
    }

    @Test
    void cerrarFallaSiQuedanProtocolosSinRegistrar() {
        Visita visita = Visita.builder()
                .id(1)
                .numeroOt("OT-2026-1301")
                .idInstalacion(3)
                .fecha("2026-11-19")
                .tipoVisita("SOLO_PRUEBAS")
                .idIngenieroCargo(2)
                .descripcionMantenimiento("")
                .build();

        when(visitaRepository.findById(1)).thenReturn(Optional.of(visita));
        when(protocoloClient.listarPorVisita(1)).thenReturn(List.of(
                new ProtocoloDto(1, "PT-2026-401", 10, "BORRADOR")
        ));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> visitaService.cerrar(
                        1,
                        new CierreRequest("Ana Pérez", "17.284.519-3", "Administradora")
                )
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
    }
}