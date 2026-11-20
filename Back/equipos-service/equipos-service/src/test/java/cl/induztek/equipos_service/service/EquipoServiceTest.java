package cl.induztek.equipos_service.service;

import cl.induztek.equipos_service.dto.EquipoRequest;
import cl.induztek.equipos_service.dto.EquipoResponse;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipoServiceTest {

    @Mock
    private EquipoRepository equipoRepository;

    @Mock
    private InstalacionRepository instalacionRepository;

    @Mock
    private TipoEquipoRepository tipoEquipoRepository;

    @Mock
    private InstrumentoRepository instrumentoRepository;

    private EquipoService servicio() {
        return new EquipoService(equipoRepository, instalacionRepository, tipoEquipoRepository, instrumentoRepository);
    }

    private Instalacion instalacion() {
        return Instalacion.builder().id(1).idUsuarioCliente(2).nombre("Planta Lampa")
                .ubicacion("Sector industrial").comuna("Lampa").observacionesAcceso("").build();
    }

    private TipoEquipo tipo() {
        return TipoEquipo.builder().id(1).nombre("Transformador").clasificacion("PRIMARIO").build();
    }

    private Equipo equipo() {
        return Equipo.builder().id(10).idInstalacion(1).tag("TR-01").idTipoEquipo(1)
                .marca("Rhona").modelo("RH-18733").numeroSerie("RH-18733").anioFabricacion(2012)
                .potenciaKva(1500.0).tensionPrimariaKv(12.0).tensionSecundariaKv(0.4)
                .observaciones("").build();
    }

    private EquipoRequest peticion(Integer idInstalacion, String tag, Integer idTipo) {
        return new EquipoRequest(idInstalacion, tag, idTipo, "Rhona", "RH-18733", "RH-18733",
                2012, 1500.0, 12.0, 0.4, null, null, "");
    }

    private ResponseStatusException errorDe(Runnable accion) {
        return assertThrows(ResponseStatusException.class, accion::run);
    }

    @Test
    void listarInstalacionesSinClienteDevuelveTodas() {
        when(instalacionRepository.findAll()).thenReturn(List.of(instalacion()));

        assertEquals(1, servicio().listarInstalaciones(null).size());
    }

    @Test
    void listarInstalacionesFiltraPorCliente() {
        when(instalacionRepository.findByIdUsuarioClienteOrderByNombreAsc(2)).thenReturn(List.of(instalacion()));

        assertEquals("Planta Lampa", servicio().listarInstalaciones(2).get(0).getNombre());
    }

    @Test
    void listarTiposDevuelveLosTiposDeEquipo() {
        when(tipoEquipoRepository.findAll()).thenReturn(List.of(tipo()));

        assertEquals(1, servicio().listarTipos().size());
    }

    @Test
    void listarInstrumentosLosOrdenaPorVencimiento() {
        when(instrumentoRepository.findAllByOrderByFechaVenceAsc())
                .thenReturn(List.of(Instrumento.builder().id(1).nombre("Megóhmetro").build()));

        assertEquals("Megóhmetro", servicio().listarInstrumentos().get(0).getNombre());
    }

    @Test
    void instrumentosVencidosExigeLaFecha() {
        assertEquals(HttpStatus.BAD_REQUEST,
                errorDe(() -> servicio().instrumentosVencidos(" ")).getStatusCode());
    }

    @Test
    void instrumentosVencidosFiltraPorLaFecha() {
        when(instrumentoRepository.findByFechaVenceLessThanOrderByFechaVenceAsc("2026-11-19"))
                .thenReturn(List.of(Instrumento.builder().id(1).nombre("Megóhmetro").build()));

        assertEquals(1, servicio().instrumentosVencidos("2026-11-19").size());
    }

    @Test
    void listarPorInstalacionDevuelveSusEquipos() {
        when(equipoRepository.findByIdInstalacionOrderByTagAsc(1)).thenReturn(List.of(equipo()));
        when(tipoEquipoRepository.findAll()).thenReturn(List.of(tipo()));
        when(instalacionRepository.findAll()).thenReturn(List.of(instalacion()));

        List<EquipoResponse> lista = servicio().listar(null, 1);

        assertEquals(1, lista.size());
        assertEquals("Transformador", lista.get(0).tipoEquipo());
        assertEquals("Planta Lampa", lista.get(0).instalacionNombre());
    }

    @Test
    void listarPorClienteRecorreSusInstalaciones() {
        when(instalacionRepository.findByIdUsuarioClienteOrderByNombreAsc(2)).thenReturn(List.of(instalacion()));
        when(equipoRepository.findByIdInstalacionOrderByTagAsc(1)).thenReturn(List.of(equipo()));
        when(tipoEquipoRepository.findAll()).thenReturn(List.of(tipo()));
        when(instalacionRepository.findAll()).thenReturn(List.of(instalacion()));

        assertEquals(1, servicio().listar(2, null).size());
    }

    @Test
    void listarSinFiltrosDevuelveTodoYCompletaVacios() {
        when(equipoRepository.findAll()).thenReturn(List.of(equipo()));
        when(tipoEquipoRepository.findAll()).thenReturn(List.of());
        when(instalacionRepository.findAll()).thenReturn(List.of());

        EquipoResponse respuesta = servicio().listar(null, null).get(0);

        assertEquals("", respuesta.tipoEquipo());
        assertEquals("", respuesta.clasificacion());
        assertEquals("", respuesta.instalacionNombre());
    }

    @Test
    void obtenerDevuelveElEquipo() {
        when(equipoRepository.findById(10)).thenReturn(Optional.of(equipo()));
        when(tipoEquipoRepository.findById(1)).thenReturn(Optional.of(tipo()));
        when(instalacionRepository.findById(1)).thenReturn(Optional.of(instalacion()));

        assertEquals("TR-01", servicio().obtener(10).tag());
    }

    @Test
    void obtenerFallaSiNoExiste() {
        when(equipoRepository.findById(99)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, errorDe(() -> servicio().obtener(99)).getStatusCode());
    }

    @Test
    void crearExigeInstalacion() {
        assertEquals(HttpStatus.BAD_REQUEST,
                errorDe(() -> servicio().crear(peticion(null, "TR-01", 1))).getStatusCode());
    }

    @Test
    void crearExigeUnTipoExistente() {
        when(tipoEquipoRepository.findById(1)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.BAD_REQUEST,
                errorDe(() -> servicio().crear(peticion(1, "TR-01", 1))).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST,
                errorDe(() -> servicio().crear(peticion(1, "TR-01", null))).getStatusCode());
    }

    @Test
    void crearExigeTag() {
        when(tipoEquipoRepository.findById(1)).thenReturn(Optional.of(tipo()));

        assertEquals(HttpStatus.BAD_REQUEST,
                errorDe(() -> servicio().crear(peticion(1, " ", 1))).getStatusCode());
    }

    @Test
    void crearExigeQueLaInstalacionExista() {
        when(tipoEquipoRepository.findById(1)).thenReturn(Optional.of(tipo()));
        when(instalacionRepository.findById(1)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.BAD_REQUEST,
                errorDe(() -> servicio().crear(peticion(1, "TR-01", 1))).getStatusCode());
    }

    @Test
    void crearRechazaUnTagRepetidoEnLaInstalacion() {
        when(tipoEquipoRepository.findById(1)).thenReturn(Optional.of(tipo()));
        when(instalacionRepository.findById(1)).thenReturn(Optional.of(instalacion()));
        when(equipoRepository.findByIdInstalacionAndTag(1, "TR-01")).thenReturn(Optional.of(equipo()));

        assertEquals(HttpStatus.CONFLICT,
                errorDe(() -> servicio().crear(peticion(1, "TR-01", 1))).getStatusCode());
        verify(equipoRepository, never()).save(any());
    }

    @Test
    void crearGuardaElEquipo() {
        when(tipoEquipoRepository.findById(1)).thenReturn(Optional.of(tipo()));
        when(instalacionRepository.findById(1)).thenReturn(Optional.of(instalacion()));
        when(equipoRepository.findByIdInstalacionAndTag(1, "TR-01")).thenReturn(Optional.empty());
        when(equipoRepository.save(any(Equipo.class))).thenAnswer(inv -> {
            Equipo guardado = inv.getArgument(0);
            guardado.setId(11);
            return guardado;
        });

        EquipoResponse respuesta = servicio().crear(peticion(1, "TR-01", 1));

        ArgumentCaptor<Equipo> captor = ArgumentCaptor.forClass(Equipo.class);
        verify(equipoRepository).save(captor.capture());
        assertEquals(11, respuesta.id());
        assertEquals("Rhona", captor.getValue().getMarca());
        assertEquals("", captor.getValue().getObservaciones());
    }

    @Test
    void crearCompletaLosTextosNulos() {
        EquipoRequest datos = new EquipoRequest(1, "TR-09", 1, null, null, null,
                null, null, null, null, null, null, null);
        when(tipoEquipoRepository.findById(1)).thenReturn(Optional.of(tipo()));
        when(instalacionRepository.findById(1)).thenReturn(Optional.of(instalacion()));
        when(equipoRepository.findByIdInstalacionAndTag(1, "TR-09")).thenReturn(Optional.empty());
        when(equipoRepository.save(any(Equipo.class))).thenAnswer(inv -> inv.getArgument(0));

        servicio().crear(datos);

        ArgumentCaptor<Equipo> captor = ArgumentCaptor.forClass(Equipo.class);
        verify(equipoRepository).save(captor.capture());
        assertEquals("", captor.getValue().getMarca());
        assertEquals("", captor.getValue().getModelo());
        assertEquals("", captor.getValue().getNumeroSerie());
        assertEquals("", captor.getValue().getObservaciones());
    }

    @Test
    void actualizarPermiteMantenerSuPropioTag() {
        when(equipoRepository.findById(10)).thenReturn(Optional.of(equipo()));
        when(tipoEquipoRepository.findById(1)).thenReturn(Optional.of(tipo()));
        when(instalacionRepository.findById(1)).thenReturn(Optional.of(instalacion()));
        when(equipoRepository.findByIdInstalacionAndTag(1, "TR-01")).thenReturn(Optional.of(equipo()));
        when(equipoRepository.save(any(Equipo.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals("TR-01", servicio().actualizar(10, peticion(1, "TR-01", 1)).tag());
    }

    @Test
    void actualizarFallaSiNoExiste() {
        when(equipoRepository.findById(99)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND,
                errorDe(() -> servicio().actualizar(99, peticion(1, "TR-01", 1))).getStatusCode());
    }

    @Test
    void eliminarBorraElEquipo() {
        Equipo encontrado = equipo();
        when(equipoRepository.findById(10)).thenReturn(Optional.of(encontrado));

        servicio().eliminar(10);

        verify(equipoRepository).delete(encontrado);
    }

    @Test
    void eliminarFallaSiNoExiste() {
        when(equipoRepository.findById(99)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, errorDe(() -> servicio().eliminar(99)).getStatusCode());
    }

    @Test
    void elEquipoGuardadoQuedaConSuTipo() {
        when(equipoRepository.findById(10)).thenReturn(Optional.of(equipo()));
        when(tipoEquipoRepository.findById(1)).thenReturn(Optional.of(tipo()));
        when(instalacionRepository.findById(1)).thenReturn(Optional.of(instalacion()));

        EquipoResponse respuesta = servicio().obtener(10);

        assertEquals("PRIMARIO", respuesta.clasificacion());
        assertTrue(respuesta.potenciaKva() > 0);
    }
}
