package cl.induztek.tareas_service.service;

import cl.induztek.tareas_service.client.ProtocoloClient;
import cl.induztek.tareas_service.dto.CierreRequest;
import cl.induztek.tareas_service.dto.ProtocoloDto;
import cl.induztek.tareas_service.dto.RecepcionResponse;
import cl.induztek.tareas_service.dto.VisitaDetalleResponse;
import cl.induztek.tareas_service.dto.VisitaRequest;
import cl.induztek.tareas_service.dto.VisitaResponse;
import cl.induztek.tareas_service.model.RecepcionActa;
import cl.induztek.tareas_service.model.Visita;
import cl.induztek.tareas_service.model.VisitaParticipante;
import cl.induztek.tareas_service.model.VisitaParticipanteId;
import cl.induztek.tareas_service.repository.RecepcionActaRepository;
import cl.induztek.tareas_service.repository.VisitaParticipanteRepository;
import cl.induztek.tareas_service.repository.VisitaRepository;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

    private VisitaService servicio() {
        return new VisitaService(visitaRepository, participanteRepository, actaRepository, protocoloClient);
    }

    private Visita visita() {
        return Visita.builder().id(1).numeroOt("OT-2026-1301").idInstalacion(3).fecha("2026-11-19")
                .tipoVisita("SOLO_PRUEBAS").idIngenieroCargo(2).descripcionMantenimiento("").build();
    }

    private VisitaParticipante participante(Integer idVisita, Integer idUsuario) {
        return VisitaParticipante.builder().id(new VisitaParticipanteId(idVisita, idUsuario)).build();
    }

    private CierreRequest cierre() {
        return new CierreRequest("Ana Pérez", "17.284.519-3", "Administradora");
    }

    private ResponseStatusException errorDe(Runnable accion) {
        return assertThrows(ResponseStatusException.class, accion::run);
    }

    @Test
    void listarPorIngenieroYInstalacionCruzaLosFiltros() {
        Visita otra = visita();
        otra.setIdInstalacion(9);
        when(visitaRepository.buscarPorIngeniero(2)).thenReturn(List.of(visita(), otra));

        assertEquals(1, servicio().listar(3, 2).size());
    }

    @Test
    void listarPorIngenieroDevuelveSusVisitas() {
        when(visitaRepository.buscarPorIngeniero(2)).thenReturn(List.of(visita()));

        assertEquals("OT-2026-1301", servicio().listar(null, 2).get(0).numeroOt());
    }

    @Test
    void listarPorInstalacionDevuelveLasDeEsaInstalacion() {
        when(visitaRepository.findByIdInstalacionOrderByFechaDesc(3)).thenReturn(List.of(visita()));

        assertEquals(1, servicio().listar(3, null).size());
    }

    @Test
    void listarSinFiltrosDevuelveTodas() {
        when(visitaRepository.findAllByOrderByFechaDesc()).thenReturn(List.of(visita()));

        assertEquals(1, servicio().listar(null, null).size());
    }

    @Test
    void listarAbiertasFiltraPorInstalacion() {
        when(visitaRepository.findByIdInstalacionAndFechaHoraCierreIsNullOrderByIdAsc(3))
                .thenReturn(List.of(visita()));

        assertEquals(1, servicio().listarAbiertas(3).size());
    }

    @Test
    void listarAbiertasSinInstalacionDevuelveTodasLasAbiertas() {
        when(visitaRepository.findByFechaHoraCierreIsNullOrderByIdAsc()).thenReturn(List.of(visita()));

        assertEquals(1, servicio().listarAbiertas(null).size());
    }

    @Test
    void obtenerDevuelveLaVisita() {
        when(visitaRepository.findById(1)).thenReturn(Optional.of(visita()));

        assertEquals(3, servicio().obtener(1).idInstalacion());
    }

    @Test
    void obtenerFallaSiNoExiste() {
        when(visitaRepository.findById(9)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, errorDe(() -> servicio().obtener(9)).getStatusCode());
    }

    @Test
    void detalleIncluyeParticipantesYActa() {
        when(visitaRepository.findById(1)).thenReturn(Optional.of(visita()));
        when(participanteRepository.findByIdIdVisitaOrderByIdIdUsuarioAsc(1))
                .thenReturn(List.of(participante(1, 5), participante(1, 6)));
        when(actaRepository.findById(1)).thenReturn(Optional.of(RecepcionActa.builder()
                .idVisita(1).receptorNombre("Ana").receptorRut("17.284.519-3").receptorCargo("Adm")
                .fechaHoraRecepcion("2026-11-19 10:30").build()));

        VisitaDetalleResponse detalle = servicio().detalle(1);

        assertEquals(List.of(5, 6), detalle.participantes());
        assertNotNull(detalle.acta());
    }

    @Test
    void detalleSinActaLaDejaEnNulo() {
        when(visitaRepository.findById(1)).thenReturn(Optional.of(visita()));
        when(participanteRepository.findByIdIdVisitaOrderByIdIdUsuarioAsc(1)).thenReturn(List.of());
        when(actaRepository.findById(1)).thenReturn(Optional.empty());

        VisitaDetalleResponse detalle = servicio().detalle(1);

        assertTrue(detalle.participantes().isEmpty());
        assertNull(detalle.acta());
    }

    @Test
    void crearExigeInstalacion() {
        VisitaRequest datos = new VisitaRequest(null, null, null, null, null);

        assertEquals(HttpStatus.BAD_REQUEST, errorDe(() -> servicio().crear(datos)).getStatusCode());
        verify(visitaRepository, never()).save(any());
    }

    @Test
    void crearCompletaLosValoresPorDefecto() {
        when(visitaRepository.count()).thenReturn(0L);
        when(visitaRepository.save(any(Visita.class))).thenAnswer(inv -> inv.getArgument(0));

        VisitaResponse respuesta = servicio().crear(new VisitaRequest(3, null, null, 2, null));

        assertTrue(respuesta.numeroOt().startsWith("OT-"));
        assertTrue(respuesta.numeroOt().endsWith("-1301"));
        assertEquals("SOLO_PRUEBAS", respuesta.tipoVisita());
        assertEquals("", respuesta.descripcionMantenimiento());
        assertNotNull(respuesta.fecha());
    }

    @Test
    void crearRespetaLosDatosEntregados() {
        when(visitaRepository.count()).thenReturn(4L);
        when(visitaRepository.save(any(Visita.class))).thenAnswer(inv -> inv.getArgument(0));

        VisitaResponse respuesta = servicio().crear(
                new VisitaRequest(3, "2026-11-20", "MANTENIMIENTO", 2, "Cambio de aceite"));

        assertEquals("OT-" + java.time.LocalDate.now().getYear() + "-1305", respuesta.numeroOt());
        assertEquals("2026-11-20", respuesta.fecha());
        assertEquals("MANTENIMIENTO", respuesta.tipoVisita());
        assertEquals("Cambio de aceite", respuesta.descripcionMantenimiento());
    }

    @Test
    void agregarParticipanteExigeUsuario() {
        when(visitaRepository.findById(1)).thenReturn(Optional.of(visita()));

        assertEquals(HttpStatus.BAD_REQUEST, errorDe(() -> servicio().agregarParticipante(1, null)).getStatusCode());
    }

    @Test
    void agregarParticipanteNoRepiteAlQueYaEsta() {
        when(visitaRepository.findById(1)).thenReturn(Optional.of(visita()));
        when(participanteRepository.existsByIdIdVisitaAndIdIdUsuario(1, 5)).thenReturn(true);
        when(participanteRepository.findByIdIdVisitaOrderByIdIdUsuarioAsc(1))
                .thenReturn(List.of(participante(1, 5)));

        assertEquals(List.of(5), servicio().agregarParticipante(1, 5));
        verify(participanteRepository, never()).save(any());
    }

    @Test
    void agregarParticipanteGuardaAlNuevo() {
        when(visitaRepository.findById(1)).thenReturn(Optional.of(visita()));
        when(participanteRepository.existsByIdIdVisitaAndIdIdUsuario(1, 7)).thenReturn(false);
        when(participanteRepository.findByIdIdVisitaOrderByIdIdUsuarioAsc(1))
                .thenReturn(List.of(participante(1, 5), participante(1, 7)));

        assertEquals(List.of(5, 7), servicio().agregarParticipante(1, 7));

        ArgumentCaptor<VisitaParticipante> captor = ArgumentCaptor.forClass(VisitaParticipante.class);
        verify(participanteRepository).save(captor.capture());
        assertEquals(7, captor.getValue().getId().getIdUsuario());
    }

    @Test
    void agregarParticipanteFallaSiLaVisitaNoExiste() {
        when(visitaRepository.findById(9)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND,
                errorDe(() -> servicio().agregarParticipante(9, 5)).getStatusCode());
    }

    @Test
    void quitarParticipanteLoBorraYDevuelveLaLista() {
        when(visitaRepository.findById(1)).thenReturn(Optional.of(visita()));
        when(participanteRepository.findByIdIdVisitaOrderByIdIdUsuarioAsc(1))
                .thenReturn(List.of(participante(1, 6)));

        assertEquals(List.of(6), servicio().quitarParticipante(1, 5));
        verify(participanteRepository).deleteByIdIdVisitaAndIdIdUsuario(1, 5);
    }

    @Test
    void participantesDevuelveSoloLosIds() {
        when(participanteRepository.findByIdIdVisitaOrderByIdIdUsuarioAsc(1))
                .thenReturn(List.of(participante(1, 5), participante(1, 9)));

        assertEquals(List.of(5, 9), servicio().participantes(1));
    }

    @Test
    void cerrarExigeQuienRecibeElActa() {
        when(visitaRepository.findById(1)).thenReturn(Optional.of(visita()));

        assertEquals(HttpStatus.BAD_REQUEST, errorDe(() ->
                servicio().cerrar(1, new CierreRequest("", "17.284.519-3", "Adm"))).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, errorDe(() ->
                servicio().cerrar(1, new CierreRequest("Ana", "", "Adm"))).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, errorDe(() ->
                servicio().cerrar(1, new CierreRequest("Ana", "17.284.519-3", " "))).getStatusCode());
    }

    @Test
    void cerrarRechazaSiQuedanProtocolosPendientes() {
        when(visitaRepository.findById(1)).thenReturn(Optional.of(visita()));
        when(protocoloClient.listarPorVisita(1)).thenReturn(List.of(
                new ProtocoloDto(1, "PT-2026-401", 10, "BORRADOR"),
                new ProtocoloDto(2, "PT-2026-402", 11, "OBSERVADO"),
                new ProtocoloDto(3, "PT-2026-403", 12, "FORMALIZADO"),
                new ProtocoloDto(4, "PT-2026-404", 13, "ANULADO"),
                new ProtocoloDto(5, "PT-2026-405", 14, "RARO")));

        ResponseStatusException error = errorDe(() -> servicio().cerrar(1, cierre()));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        assertTrue(error.getReason().contains("borrador"));
        assertTrue(error.getReason().contains("observado"));
        assertTrue(error.getReason().contains("formalizado"));
        assertTrue(error.getReason().contains("RARO"));
        assertTrue(!error.getReason().contains("anulado"));
    }

    @Test
    void cerrarDejaElActaYLaVisitaCerrada() {
        Visita abierta = visita();
        when(visitaRepository.findById(1)).thenReturn(Optional.of(abierta));
        when(protocoloClient.listarPorVisita(1)).thenReturn(List.of(
                new ProtocoloDto(1, "PT-2026-401", 10, "REGISTRADO_TERRENO")));
        when(actaRepository.findById(1)).thenReturn(Optional.empty());
        when(actaRepository.save(any(RecepcionActa.class))).thenAnswer(inv -> inv.getArgument(0));

        RecepcionResponse acta = servicio().cerrar(1, cierre());

        assertEquals("Ana Pérez", acta.receptorNombre());
        assertTrue(acta.fechaHoraRecepcion().matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}"));
        assertNotNull(abierta.getFechaHoraCierre());
        verify(visitaRepository).save(abierta);
    }

    @Test
    void cerrarReutilizaElActaSiYaExistia() {
        Visita abierta = visita();
        RecepcionActa existente = RecepcionActa.builder().idVisita(1).receptorNombre("Antiguo").build();
        when(visitaRepository.findById(1)).thenReturn(Optional.of(abierta));
        when(protocoloClient.listarPorVisita(1)).thenReturn(List.of());
        when(actaRepository.findById(1)).thenReturn(Optional.of(existente));
        when(actaRepository.save(existente)).thenAnswer(inv -> inv.getArgument(0));

        assertEquals("Ana Pérez", servicio().cerrar(1, cierre()).receptorNombre());
        verify(actaRepository).save(existente);
    }

    @Test
    void actaFallaSiLaVisitaNoExiste() {
        when(visitaRepository.findById(9)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, errorDe(() -> servicio().acta(9)).getStatusCode());
    }

    @Test
    void actaFallaSiLaVisitaNoTieneActa() {
        when(visitaRepository.findById(1)).thenReturn(Optional.of(visita()));
        when(actaRepository.findById(1)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, errorDe(() -> servicio().acta(1)).getStatusCode());
    }

    @Test
    void actaDevuelveLaRecepcionGuardada() {
        when(visitaRepository.findById(1)).thenReturn(Optional.of(visita()));
        when(actaRepository.findById(1)).thenReturn(Optional.of(RecepcionActa.builder()
                .idVisita(1).receptorNombre("Ana").receptorRut("17.284.519-3").receptorCargo("Adm")
                .fechaHoraRecepcion("2026-11-19 10:30").build()));

        assertEquals("2026-11-19 10:30", servicio().acta(1).fechaHoraRecepcion());
    }
}
