package cl.induztek.solicitudes_service.service;

import cl.induztek.solicitudes_service.config.Seguridad;
import cl.induztek.solicitudes_service.dto.SolicitudRequest;
import cl.induztek.solicitudes_service.dto.SolicitudResponse;
import cl.induztek.solicitudes_service.model.SolicitudAcceso;
import cl.induztek.solicitudes_service.repository.SolicitudAccesoRepository;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitudServiceTest {

    @Mock
    private SolicitudAccesoRepository solicitudRepository;

    private SolicitudService servicio() {
        return new SolicitudService(solicitudRepository);
    }

    private SolicitudAcceso solicitud() {
        return SolicitudAcceso.builder()
                .id(1).nombre("Ana").apellido("Pérez").pais("Chile").ciudad("Lampa")
                .correo("ana@duoc.cl").passwordHash(Seguridad.hashear("secreta"))
                .justificacion("Necesito probar equipos").estado("PENDIENTE").tipo("INGRESO")
                .campo("").valor("").build();
    }

    private SolicitudRequest ingreso(String correo, String clave) {
        return new SolicitudRequest("Ana", "Pérez", "Chile", "Lampa",
                correo, clave, "Necesito probar equipos", "INGRESO", null, "", "");
    }

    private ResponseStatusException errorDe(Runnable accion) {
        return assertThrows(ResponseStatusException.class, accion::run);
    }

    @Test
    void listarFiltraPorEstadoYTipo() {
        when(solicitudRepository.findByEstadoAndTipoOrderByIdAsc("PENDIENTE", "INGRESO"))
                .thenReturn(List.of(solicitud()));

        List<SolicitudResponse> lista = servicio().listar("PENDIENTE", "INGRESO");

        assertEquals(1, lista.size());
        assertEquals("ana@duoc.cl", lista.get(0).correo());
    }

    @Test
    void listarFiltraSoloPorEstado() {
        when(solicitudRepository.findByEstadoOrderByIdAsc("APROBADA")).thenReturn(List.of(solicitud()));

        assertEquals(1, servicio().listar("APROBADA", " ").size());
    }

    @Test
    void listarFiltraSoloPorTipo() {
        when(solicitudRepository.findByTipoOrderByIdAsc("BAJA")).thenReturn(List.of(solicitud()));

        assertEquals(1, servicio().listar(null, "BAJA").size());
    }

    @Test
    void listarSinFiltrosDevuelveTodo() {
        when(solicitudRepository.findAll()).thenReturn(List.of(solicitud()));

        assertEquals(1, servicio().listar("", "").size());
    }

    @Test
    void obtenerDevuelveLaSolicitud() {
        when(solicitudRepository.findById(1)).thenReturn(Optional.of(solicitud()));

        assertEquals("Ana", servicio().obtener(1).nombre());
    }

    @Test
    void obtenerFallaSiNoExiste() {
        when(solicitudRepository.findById(9)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, errorDe(() -> servicio().obtener(9)).getStatusCode());
    }

    @Test
    void pendientesCuentaLasSolicitudesPendientes() {
        when(solicitudRepository.countByEstado("PENDIENTE")).thenReturn(3L);

        assertEquals(3L, servicio().pendientes());
    }

    @Test
    void crearExigeNombreApellidoYCorreo() {
        SolicitudRequest datos = new SolicitudRequest("", "Pérez", "Chile", "Lampa",
                "ana@duoc.cl", "secreta", "motivo", "INGRESO", null, "", "");

        assertEquals(HttpStatus.BAD_REQUEST, errorDe(() -> servicio().crear(datos)).getStatusCode());
        verify(solicitudRepository, never()).save(any());
    }

    @Test
    void crearExigeJustificacion() {
        SolicitudRequest datos = new SolicitudRequest("Ana", "Pérez", "Chile", "Lampa",
                "ana@duoc.cl", "secreta", "  ", "INGRESO", null, "", "");

        assertEquals(HttpStatus.BAD_REQUEST, errorDe(() -> servicio().crear(datos)).getStatusCode());
    }

    @Test
    void crearRechazaUnTipoDesconocido() {
        SolicitudRequest datos = new SolicitudRequest("Ana", "Pérez", "Chile", "Lampa",
                "ana@duoc.cl", "secreta", "motivo", "OTRO", null, "", "");

        assertEquals(HttpStatus.BAD_REQUEST, errorDe(() -> servicio().crear(datos)).getStatusCode());
    }

    @Test
    void crearIngresoExigeClaveDeMinimoSeis() {
        SolicitudRequest datos = ingreso("ana@duoc.cl", "123");

        assertEquals(HttpStatus.BAD_REQUEST, errorDe(() -> servicio().crear(datos)).getStatusCode());
    }

    @Test
    void crearRechazaCorreoConSolicitudPendiente() {
        when(solicitudRepository.findFirstByCorreoAndEstado("ana@duoc.cl", "PENDIENTE"))
                .thenReturn(Optional.of(solicitud()));

        assertEquals(HttpStatus.CONFLICT, errorDe(() -> servicio().crear(ingreso("ana@duoc.cl", "secreta"))).getStatusCode());
    }

    @Test
    void crearGuardaLaSolicitudConLaClaveCifrada() {
        when(solicitudRepository.findFirstByCorreoAndEstado("ana@duoc.cl", "PENDIENTE"))
                .thenReturn(Optional.empty());
        when(solicitudRepository.save(any(SolicitudAcceso.class))).thenAnswer(inv -> {
            SolicitudAcceso guardada = inv.getArgument(0);
            guardada.setId(7);
            return guardada;
        });

        SolicitudResponse respuesta = servicio().crear(ingreso("ana@duoc.cl", "secreta"));

        ArgumentCaptor<SolicitudAcceso> captor = ArgumentCaptor.forClass(SolicitudAcceso.class);
        verify(solicitudRepository).save(captor.capture());
        SolicitudAcceso guardada = captor.getValue();
        assertEquals("PENDIENTE", guardada.getEstado());
        assertFalse(guardada.getPasswordHash().equals("secreta"));
        assertTrue(guardada.getPasswordHash().startsWith("$2"));
        assertEquals(7, respuesta.id());
    }

    @Test
    void crearSinTipoAsumeIngresoYCompletaCamposVacios() {
        SolicitudRequest datos = new SolicitudRequest("Ana", "Pérez", null, null,
                "ana@duoc.cl", "secreta", "motivo", null, null, null, null);
        when(solicitudRepository.findFirstByCorreoAndEstado("ana@duoc.cl", "PENDIENTE"))
                .thenReturn(Optional.empty());
        when(solicitudRepository.save(any(SolicitudAcceso.class))).thenAnswer(inv -> inv.getArgument(0));

        SolicitudResponse respuesta = servicio().crear(datos);

        assertEquals("INGRESO", respuesta.tipo());
        assertEquals("", respuesta.pais());
        assertEquals("", respuesta.ciudad());
        assertEquals("", respuesta.campo());
        assertEquals("", respuesta.valor());
    }

    @Test
    void crearUnaBajaNoExigeClave() {
        SolicitudRequest datos = new SolicitudRequest("Ana", "Pérez", "Chile", "Lampa",
                "ana@duoc.cl", null, "motivo", "BAJA", 3, "", "");
        when(solicitudRepository.findFirstByCorreoAndEstado("ana@duoc.cl", "PENDIENTE"))
                .thenReturn(Optional.empty());
        when(solicitudRepository.save(any(SolicitudAcceso.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals("BAJA", servicio().crear(datos).tipo());
    }

    @Test
    void cambiarEstadoRechazaEstadoInvalido() {
        assertEquals(HttpStatus.BAD_REQUEST,
                errorDe(() -> servicio().cambiarEstado(1, "CUALQUIERA")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST,
                errorDe(() -> servicio().cambiarEstado(1, null)).getStatusCode());
    }

    @Test
    void cambiarEstadoFallaSiNoExiste() {
        when(solicitudRepository.findById(9)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND,
                errorDe(() -> servicio().cambiarEstado(9, "APROBADA")).getStatusCode());
    }

    @Test
    void cambiarEstadoGuardaElNuevoEstado() {
        SolicitudAcceso encontrada = solicitud();
        when(solicitudRepository.findById(1)).thenReturn(Optional.of(encontrada));
        when(solicitudRepository.save(encontrada)).thenReturn(encontrada);

        assertEquals("APROBADA", servicio().cambiarEstado(1, "APROBADA").estado());
    }

    @Test
    void eliminarBorraLaSolicitud() {
        SolicitudAcceso encontrada = solicitud();
        when(solicitudRepository.findById(1)).thenReturn(Optional.of(encontrada));

        servicio().eliminar(1);

        verify(solicitudRepository).delete(encontrada);
    }

    @Test
    void eliminarFallaSiNoExiste() {
        when(solicitudRepository.findById(9)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, errorDe(() -> servicio().eliminar(9)).getStatusCode());
    }
}
