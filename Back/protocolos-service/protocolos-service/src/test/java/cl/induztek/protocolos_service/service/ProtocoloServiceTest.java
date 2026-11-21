package cl.induztek.protocolos_service.service;

import cl.induztek.protocolos_service.dto.AmbientesRequest;
import cl.induztek.protocolos_service.dto.EstadoProtocoloRequest;
import cl.induztek.protocolos_service.dto.HistorialResponse;
import cl.induztek.protocolos_service.dto.ProtocoloDetalleResponse;
import cl.induztek.protocolos_service.dto.ProtocoloRequest;
import cl.induztek.protocolos_service.dto.ProtocoloResponse;
import cl.induztek.protocolos_service.dto.PruebaRequest;
import cl.induztek.protocolos_service.dto.PruebaResponse;
import cl.induztek.protocolos_service.model.HistorialEstado;
import cl.induztek.protocolos_service.model.Medicion;
import cl.induztek.protocolos_service.model.Protocolo;
import cl.induztek.protocolos_service.model.Prueba;
import cl.induztek.protocolos_service.model.TipoEquipoPrueba;
import cl.induztek.protocolos_service.model.TipoEquipoPruebaId;
import cl.induztek.protocolos_service.model.TipoPrueba;
import cl.induztek.protocolos_service.repository.HistorialEstadoRepository;
import cl.induztek.protocolos_service.repository.MedicionRepository;
import cl.induztek.protocolos_service.repository.ProtocoloRepository;
import cl.induztek.protocolos_service.repository.PruebaRepository;
import cl.induztek.protocolos_service.repository.TipoEquipoPruebaRepository;
import cl.induztek.protocolos_service.repository.TipoPruebaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProtocoloServiceTest {

    @Mock
    private ProtocoloRepository protocoloRepository;

    @Mock
    private PruebaRepository pruebaRepository;

    @Mock
    private MedicionRepository medicionRepository;

    @Mock
    private HistorialEstadoRepository historialRepository;

    @Mock
    private TipoPruebaRepository tipoPruebaRepository;

    @Mock
    private TipoEquipoPruebaRepository tipoEquipoPruebaRepository;

    private ProtocoloService servicio() {
        return new ProtocoloService(protocoloRepository, pruebaRepository, medicionRepository,
                historialRepository, tipoPruebaRepository, tipoEquipoPruebaRepository);
    }

    private Protocolo protocolo(String estado) {
        return Protocolo.builder().id(1).numero("PT-2026-401").idVisita(3).idEquipo(10)
                .idUsuarioRegistra(2).fechaHoraPrueba("2026-11-19 10:00")
                .temperaturaC(22.5).humedadPct(48.0).estado(estado).observaciones("").build();
    }

    private Prueba prueba() {
        return Prueba.builder().id(20).idProtocolo(1).codigoTipoPrueba("AISLACION").elemento("")
                .momento("UNICA").realizada(true).motivoNoRealizada("").resultado("APROBADO")
                .comentario("").build();
    }

    private PruebaRequest pruebaAprobada() {
        return new PruebaRequest("AISLACION", "", true, "", 1, "APROBADO", "", Map.of("R60", 2500.0));
    }

    private ResponseStatusException errorDe(Runnable accion) {
        return assertThrows(ResponseStatusException.class, accion::run);
    }

    private void simularGuardado() {
        when(protocoloRepository.save(any(Protocolo.class))).thenAnswer(inv -> {
            Protocolo guardado = inv.getArgument(0);
            if (guardado.getId() == null) guardado.setId(1);
            return guardado;
        });
        when(pruebaRepository.save(any(Prueba.class))).thenAnswer(inv -> {
            Prueba guardada = inv.getArgument(0);
            if (guardada.getId() == null) guardada.setId(20);
            return guardada;
        });
    }

    @Test
    void listarTiposDevuelveLosTiposDePrueba() {
        when(tipoPruebaRepository.findAll()).thenReturn(List.of(new TipoPrueba(1, "AISLACION", "Aislación")));

        assertEquals(1, servicio().listarTipos().size());
    }

    @Test
    void listarTiposDeEquipoSoloDevuelveLosQueExisten() {
        when(tipoEquipoPruebaRepository.findByIdIdTipoEquipo(1)).thenReturn(List.of(
                TipoEquipoPrueba.builder().id(new TipoEquipoPruebaId(1, 1)).build(),
                TipoEquipoPrueba.builder().id(new TipoEquipoPruebaId(1, 9)).build()));
        when(tipoPruebaRepository.findById(1))
                .thenReturn(Optional.of(new TipoPrueba(1, "AISLACION", "Aislación")));
        when(tipoPruebaRepository.findById(9)).thenReturn(Optional.empty());

        List<TipoPrueba> tipos = servicio().listarTiposDeEquipo(1);

        assertEquals(1, tipos.size());
    }

    @Test
    void listarPorVisitaFiltraConLaVisita() {
        when(protocoloRepository.findByIdVisitaOrderByFechaHoraPruebaDesc(3))
                .thenReturn(List.of(protocolo("BORRADOR")));

        assertEquals(1, servicio().listar(3, null).size());
    }

    @Test
    void listarPorEquipoFiltraConElEquipo() {
        when(protocoloRepository.findByIdEquipoOrderByFechaHoraPruebaDesc(10))
                .thenReturn(List.of(protocolo("BORRADOR")));

        assertEquals(1, servicio().listar(null, 10).size());
    }

    @Test
    void listarSinFiltrosDevuelveTodos() {
        when(protocoloRepository.findAllByOrderByFechaHoraPruebaDesc())
                .thenReturn(List.of(protocolo("BORRADOR")));

        assertEquals(1, servicio().listar(null, null).size());
    }

    @Test
    void obtenerDevuelveElProtocolo() {
        when(protocoloRepository.findById(1)).thenReturn(Optional.of(protocolo("BORRADOR")));

        assertEquals("PT-2026-401", servicio().obtener(1).numero());
    }

    @Test
    void obtenerFallaSiNoExiste() {
        when(protocoloRepository.findById(9)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, errorDe(() -> servicio().obtener(9)).getStatusCode());
    }

    @Test
    void detalleJuntaPruebasEHistorial() {
        when(protocoloRepository.findById(1)).thenReturn(Optional.of(protocolo("BORRADOR")));
        when(pruebaRepository.findByIdProtocoloOrderByIdAsc(1)).thenReturn(List.of(prueba()));
        when(medicionRepository.findByIdPruebaOrderByIdAsc(20))
                .thenReturn(List.of(Medicion.builder().idPrueba(20).codigoParametro("R60").valor("2500").build()));
        when(historialRepository.findByIdProtocoloOrderByIdAsc(1)).thenReturn(List.of(
                HistorialEstado.builder().id(1).idProtocolo(1).estadoNuevo("BORRADOR").idUsuario(2)
                        .fechaHora("2026-11-19 10:00").motivo("").build()));

        ProtocoloDetalleResponse detalle = servicio().detalle(1);

        assertEquals(1, detalle.pruebas().size());
        assertEquals(2500.0, detalle.pruebas().get(0).mediciones().get("R60"));
        assertEquals(1, detalle.historial().size());
    }

    @Test
    void historialFallaSiElProtocoloNoExiste() {
        when(protocoloRepository.findById(9)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, errorDe(() -> servicio().historial(9)).getStatusCode());
    }

    @Test
    void historialDevuelveLosPasos() {
        when(protocoloRepository.findById(1)).thenReturn(Optional.of(protocolo("BORRADOR")));
        when(historialRepository.findByIdProtocoloOrderByIdAsc(1)).thenReturn(List.of(
                HistorialEstado.builder().id(1).idProtocolo(1).estadoAnterior(null).estadoNuevo("BORRADOR")
                        .idUsuario(2).fechaHora("2026-11-19 10:00").motivo("").build()));

        List<HistorialResponse> pasos = servicio().historial(1);

        assertEquals("BORRADOR", pasos.get(0).estadoNuevo());
    }

    @Test
    void crearExigeVisitaEquipoYUsuario() {
        assertEquals(HttpStatus.BAD_REQUEST, errorDe(() -> servicio().crear(
                new ProtocoloRequest(null, 10, 2, null, null, null))).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, errorDe(() -> servicio().crear(
                new ProtocoloRequest(3, null, 2, null, null, null))).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, errorDe(() -> servicio().crear(
                new ProtocoloRequest(3, 10, null, null, null, null))).getStatusCode());
        verify(protocoloRepository, never()).save(any());
    }

    @Test
    void crearRechazaUnEquipoQueYaTieneProtocoloEnLaVisita() {
        when(protocoloRepository.findByIdVisitaAndIdEquipo(3, 10)).thenReturn(Optional.of(protocolo("BORRADOR")));

        assertEquals(HttpStatus.CONFLICT, errorDe(() -> servicio().crear(
                new ProtocoloRequest(3, 10, 2, null, null, null))).getStatusCode());
    }

    @Test
    void crearGeneraElPrimerNumeroDelAnio() {
        simularGuardado();
        when(protocoloRepository.findFirstByNumeroStartingWithOrderByNumeroDesc("PT-" + LocalDate.now().getYear() + "-"))
                .thenReturn(Optional.empty());

        ProtocoloResponse respuesta = servicio().crear(
                new ProtocoloRequest(3, 10, 2, 22.5, 48.0, null));

        assertEquals("PT-" + LocalDate.now().getYear() + "-401", respuesta.numero());
        assertEquals("BORRADOR", respuesta.estado());
        assertEquals("", respuesta.observaciones());
        verify(historialRepository).save(any(HistorialEstado.class));
    }

    @Test
    void crearSigueElCorrelativoDelUltimoProtocolo() {
        simularGuardado();
        when(protocoloRepository.findFirstByNumeroStartingWithOrderByNumeroDesc("PT-" + LocalDate.now().getYear() + "-"))
                .thenReturn(Optional.of(Protocolo.builder().numero("PT-" + LocalDate.now().getYear() + "-405").build()));

        assertEquals("PT-" + LocalDate.now().getYear() + "-406",
                servicio().crear(new ProtocoloRequest(3, 10, 2, null, null, "Revisar")).numero());
    }

    @Test
    void crearVuelveAlCuatrocientosSiElNumeroEstaMalEscrito() {
        simularGuardado();
        when(protocoloRepository.findFirstByNumeroStartingWithOrderByNumeroDesc("PT-" + LocalDate.now().getYear() + "-"))
                .thenReturn(Optional.of(Protocolo.builder().numero("PT-" + LocalDate.now().getYear() + "-ABC").build()));

        assertEquals("PT-" + LocalDate.now().getYear() + "-401",
                servicio().crear(new ProtocoloRequest(3, 10, 2, null, null, null)).numero());
    }

    @Test
    void actualizarAmbientesSePermiteEnBorradorYObservado() {
        Protocolo borrador = protocolo("BORRADOR");
        when(protocoloRepository.findById(1)).thenReturn(Optional.of(borrador));
        when(protocoloRepository.save(borrador)).thenReturn(borrador);

        ProtocoloResponse respuesta = servicio().actualizarAmbientes(1,
                new AmbientesRequest(30.0, 55.0, "Cambio de sala"));

        assertEquals(30.0, respuesta.temperaturaC());
        assertEquals("Cambio de sala", respuesta.observaciones());

        Protocolo observado = protocolo("OBSERVADO");
        observado.setObservaciones("Cambio de sala");
        when(protocoloRepository.findById(1)).thenReturn(Optional.of(observado));

        servicio().actualizarAmbientes(1, new AmbientesRequest(25.0, 50.0, null));

        assertEquals("Cambio de sala", observado.getObservaciones());
    }

    @Test
    void actualizarAmbientesRechazaProtocolosRegistradosFormalizadosOAnulados() {
        when(protocoloRepository.findById(1)).thenReturn(Optional.of(protocolo("REGISTRADO_TERRENO")));
        assertEquals(HttpStatus.CONFLICT, errorDe(() ->
                servicio().actualizarAmbientes(1, new AmbientesRequest(22.0, 48.0, null))).getStatusCode());

        when(protocoloRepository.findById(1)).thenReturn(Optional.of(protocolo("FORMALIZADO")));
        assertEquals(HttpStatus.CONFLICT, errorDe(() ->
                servicio().actualizarAmbientes(1, new AmbientesRequest(22.0, 48.0, null))).getStatusCode());

        when(protocoloRepository.findById(1)).thenReturn(Optional.of(protocolo("ANULADO")));
        assertEquals(HttpStatus.CONFLICT, errorDe(() ->
                servicio().actualizarAmbientes(1, new AmbientesRequest(22.0, 48.0, null))).getStatusCode());
    }

    @Test
    void guardarPruebaExigeUnTipoValido() {
        when(protocoloRepository.findById(1)).thenReturn(Optional.of(protocolo("BORRADOR")));

        assertEquals(HttpStatus.BAD_REQUEST, errorDe(() -> servicio().guardarPrueba(1,
                new PruebaRequest(" ", "", true, "", 1, "APROBADO", "", null))).getStatusCode());

        when(tipoPruebaRepository.findByCodigo("OTRO")).thenReturn(Optional.empty());
        assertEquals(HttpStatus.BAD_REQUEST, errorDe(() -> servicio().guardarPrueba(1,
                new PruebaRequest("OTRO", "", true, "", 1, "APROBADO", "", null))).getStatusCode());
    }

    @Test
    void guardarPruebaRealizadaExigeResultado() {
        when(protocoloRepository.findById(1)).thenReturn(Optional.of(protocolo("BORRADOR")));
        when(tipoPruebaRepository.findByCodigo("AISLACION"))
                .thenReturn(Optional.of(new TipoPrueba(1, "AISLACION", "Aislación")));

        assertEquals(HttpStatus.BAD_REQUEST, errorDe(() -> servicio().guardarPrueba(1,
                new PruebaRequest("AISLACION", "", true, "", 1, null, "", null))).getStatusCode());
    }

    @Test
    void guardarPruebaRechazaUnResultadoDesconocido() {
        when(protocoloRepository.findById(1)).thenReturn(Optional.of(protocolo("BORRADOR")));
        when(tipoPruebaRepository.findByCodigo("AISLACION"))
                .thenReturn(Optional.of(new TipoPrueba(1, "AISLACION", "Aislación")));

        assertEquals(HttpStatus.BAD_REQUEST, errorDe(() -> servicio().guardarPrueba(1,
                new PruebaRequest("AISLACION", "", true, "", 1, "REGULAR", "", null))).getStatusCode());
    }

    @Test
    void guardarPruebaNoRealizadaExigeMotivo() {
        when(protocoloRepository.findById(1)).thenReturn(Optional.of(protocolo("BORRADOR")));
        when(tipoPruebaRepository.findByCodigo("AISLACION"))
                .thenReturn(Optional.of(new TipoPrueba(1, "AISLACION", "Aislación")));

        assertEquals(HttpStatus.BAD_REQUEST, errorDe(() -> servicio().guardarPrueba(1,
                new PruebaRequest("AISLACION", "", false, " ", 1, null, "", null))).getStatusCode());
    }

    @Test
    void guardarPruebaReemplazaLaVersionAnterior() {
        Protocolo borrador = protocolo("BORRADOR");
        when(protocoloRepository.findById(1)).thenReturn(Optional.of(borrador));
        when(tipoPruebaRepository.findByCodigo("AISLACION"))
                .thenReturn(Optional.of(new TipoPrueba(1, "AISLACION", "Aislación")));
        when(pruebaRepository.findFirstByIdProtocoloAndCodigoTipoPruebaAndElementoOrderByIdDesc(
                1, "AISLACION", ""))
                .thenReturn(Optional.of(Prueba.builder().id(15).build()));
        cuandoGuardaLaPrueba();

        servicio().guardarPrueba(1, pruebaAprobada());

        verify(medicionRepository).deleteByIdPrueba(15);
        verify(pruebaRepository).delete(any(Prueba.class));
    }

    private void cuandoGuardaLaPrueba() {
        when(pruebaRepository.save(any(Prueba.class))).thenAnswer(inv -> {
            Prueba guardada = inv.getArgument(0);
            if (guardada.getId() == null) guardada.setId(20);
            return guardada;
        });
        when(medicionRepository.findByIdPruebaOrderByIdAsc(20)).thenReturn(List.of(
                Medicion.builder().idPrueba(20).codigoParametro("R60").valor("2500.0").build(),
                Medicion.builder().idPrueba(20).codigoParametro("PI").valor("no-numero").build()));
    }

    @Test
    void guardarPruebaRealizadaGuardaSusMediciones() {
        when(protocoloRepository.findById(1)).thenReturn(Optional.of(protocolo("BORRADOR")));
        when(tipoPruebaRepository.findByCodigo("AISLACION"))
                .thenReturn(Optional.of(new TipoPrueba(1, "AISLACION", "Aislación")));
        when(pruebaRepository.findFirstByIdProtocoloAndCodigoTipoPruebaAndElementoOrderByIdDesc(
                1, "AISLACION", ""))
                .thenReturn(Optional.empty());
        cuandoGuardaLaPrueba();

        PruebaResponse respuesta = servicio().guardarPrueba(1, pruebaAprobada());

        assertEquals("APROBADO", respuesta.resultado());
        assertEquals(2500.0, respuesta.mediciones().get("R60"));
        assertEquals(0.0, respuesta.mediciones().get("PI"));
        verify(medicionRepository, org.mockito.Mockito.times(1)).save(any(Medicion.class));
    }

    @Test
    void guardarPruebaNoRealizadaDejaElMotivo() {
        when(protocoloRepository.findById(1)).thenReturn(Optional.of(protocolo("BORRADOR")));
        when(tipoPruebaRepository.findByCodigo("AISLACION"))
                .thenReturn(Optional.of(new TipoPrueba(1, "AISLACION", "Aislación")));
        when(pruebaRepository.findFirstByIdProtocoloAndCodigoTipoPruebaAndElementoOrderByIdDesc(
                1, "AISLACION", "Lado MT"))
                .thenReturn(Optional.empty());
        cuandoGuardaLaPrueba();
        when(medicionRepository.findByIdPruebaOrderByIdAsc(20)).thenReturn(List.of());

        PruebaResponse respuesta = servicio().guardarPrueba(1,
                new PruebaRequest("AISLACION", "Lado MT", false, "Sin acceso", null, null, null, null));

        assertEquals("Sin acceso", respuesta.motivoNoRealizada());
        assertTrue(respuesta.mediciones().isEmpty());
    }

    @Test
    void guardarPruebaRechazaUnProtocoloYaRegistrado() {
        when(protocoloRepository.findById(1)).thenReturn(Optional.of(protocolo("REGISTRADO_TERRENO")));

        assertEquals(HttpStatus.CONFLICT, errorDe(() -> servicio().guardarPrueba(1, pruebaAprobada())).getStatusCode());
    }

    @Test
    void cambiarEstadoRechazaUnEstadoQueNoExiste() {
        when(protocoloRepository.findById(1)).thenReturn(Optional.of(protocolo("BORRADOR")));

        assertEquals(HttpStatus.BAD_REQUEST, errorDe(() -> servicio().cambiarEstado(1,
                new EstadoProtocoloRequest("OTRO", 2, ""))).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, errorDe(() -> servicio().cambiarEstado(1,
                new EstadoProtocoloRequest(null, 2, ""))).getStatusCode());
    }

    @Test
    void cambiarEstadoNoTocaUnProtocoloFormalizado() {
        when(protocoloRepository.findById(1)).thenReturn(Optional.of(protocolo("FORMALIZADO")));

        assertEquals(HttpStatus.BAD_REQUEST, errorDe(() -> servicio().cambiarEstado(1,
                new EstadoProtocoloRequest("ANULADO", 2, "motivo"))).getStatusCode());
    }

    @Test
    void cambiarEstadoRechazaUnaTransicionInexistente() {
        when(protocoloRepository.findById(1)).thenReturn(Optional.of(protocolo("BORRADOR")));

        assertEquals(HttpStatus.BAD_REQUEST, errorDe(() -> servicio().cambiarEstado(1,
                new EstadoProtocoloRequest("FORMALIZADO", 2, ""))).getStatusCode());
    }

    @Test
    void cambiarEstadoPideMotivoAlObservarOAnular() {
        when(protocoloRepository.findById(1)).thenReturn(Optional.of(protocolo("REGISTRADO_TERRENO")));

        assertEquals(HttpStatus.BAD_REQUEST, errorDe(() -> servicio().cambiarEstado(1,
                new EstadoProtocoloRequest("OBSERVADO", 2, " "))).getStatusCode());

        when(protocoloRepository.findById(1)).thenReturn(Optional.of(protocolo("BORRADOR")));
        assertEquals(HttpStatus.BAD_REQUEST, errorDe(() -> servicio().cambiarEstado(1,
                new EstadoProtocoloRequest("ANULADO", 2, null))).getStatusCode());
    }

    @Test
    void cambiarEstadoPideElUsuario() {
        when(protocoloRepository.findById(1)).thenReturn(Optional.of(protocolo("BORRADOR")));

        assertEquals(HttpStatus.BAD_REQUEST, errorDe(() -> servicio().cambiarEstado(1,
                new EstadoProtocoloRequest("REGISTRADO_TERRENO", null, ""))).getStatusCode());
    }

    @Test
    void cambiarEstadoGuardaElPasoEnElHistorial() {
        Protocolo borrador = protocolo("BORRADOR");
        when(protocoloRepository.findById(1)).thenReturn(Optional.of(borrador));
        when(protocoloRepository.save(borrador)).thenReturn(borrador);

        ProtocoloResponse respuesta = servicio().cambiarEstado(1,
                new EstadoProtocoloRequest("REGISTRADO_TERRENO", 2, ""));

        assertEquals("REGISTRADO_TERRENO", respuesta.estado());
        verify(historialRepository).save(any(HistorialEstado.class));
    }

    @Test
    void cambiarEstadoPermiteObservarYVolverARegistrar() {
        Protocolo registrado = protocolo("REGISTRADO_TERRENO");
        when(protocoloRepository.findById(1)).thenReturn(Optional.of(registrado));
        when(protocoloRepository.save(registrado)).thenReturn(registrado);

        assertEquals("OBSERVADO", servicio().cambiarEstado(1,
                new EstadoProtocoloRequest("OBSERVADO", 2, "Falta firma")).estado());

        Protocolo observado = protocolo("OBSERVADO");
        when(protocoloRepository.findById(1)).thenReturn(Optional.of(observado));

        assertEquals("REGISTRADO_TERRENO", servicio().cambiarEstado(1,
                new EstadoProtocoloRequest("REGISTRADO_TERRENO", 2, "")).estado());
    }

    @Test
    void cambiarEstadoPermiteFormalizarUnProtocoloRegistrado() {
        Protocolo registrado = protocolo("REGISTRADO_TERRENO");
        when(protocoloRepository.findById(1)).thenReturn(Optional.of(registrado));
        when(protocoloRepository.save(registrado)).thenReturn(registrado);

        assertEquals("FORMALIZADO", servicio().cambiarEstado(1,
                new EstadoProtocoloRequest("FORMALIZADO", 2, "")).estado());
    }

    @Test
    void cambiarEstadoPermiteAnularConMotivo() {
        Protocolo borrador = protocolo("BORRADOR");
        when(protocoloRepository.findById(1)).thenReturn(Optional.of(borrador));
        when(protocoloRepository.save(borrador)).thenReturn(borrador);

        assertEquals("ANULADO", servicio().cambiarEstado(1,
                new EstadoProtocoloRequest("ANULADO", 2, "Equipo dado de baja")).estado());
    }

    @Test
    void cambiarEstadoFallaSiNoExiste() {
        when(protocoloRepository.findById(9)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, errorDe(() -> servicio().cambiarEstado(9,
                new EstadoProtocoloRequest("REGISTRADO_TERRENO", 2, ""))).getStatusCode());
    }

    @Test
    void detalleDejaLaMedicionNoNumericaEnCero() {
        when(protocoloRepository.findById(1)).thenReturn(Optional.of(protocolo("BORRADOR")));
        when(pruebaRepository.findByIdProtocoloOrderByIdAsc(1)).thenReturn(List.of(prueba()));
        when(medicionRepository.findByIdPruebaOrderByIdAsc(20)).thenReturn(List.of(
                Medicion.builder().idPrueba(20).codigoParametro("PI").valor("no-numero").build()));

        assertEquals(0.0, servicio().detalle(1).pruebas().get(0).mediciones().get("PI"));
    }

    @Test
    void elProtocoloNuevoQuedaConLaFechaDeHoy() {
        simularGuardado();
        when(protocoloRepository.findFirstByNumeroStartingWithOrderByNumeroDesc(any()))
                .thenReturn(Optional.empty());

        ProtocoloResponse respuesta = servicio().crear(new ProtocoloRequest(3, 10, 2, null, null, null));

        assertNotNull(respuesta.fechaHoraPrueba());
        assertTrue(respuesta.fechaHoraPrueba().matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}"));
    }
}
