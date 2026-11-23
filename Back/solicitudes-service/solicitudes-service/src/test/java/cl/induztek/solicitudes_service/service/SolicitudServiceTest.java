package cl.induztek.solicitudes_service.service;

import cl.induztek.solicitudes_service.dto.SolicitudRequest;
import cl.induztek.solicitudes_service.model.SolicitudAcceso;
import cl.induztek.solicitudes_service.repository.SolicitudAccesoRepository;
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
class SolicitudServiceTest {

    @Mock
    private SolicitudAccesoRepository solicitudRepository;

    @InjectMocks
    private SolicitudService solicitudService;

    @Test
    void pendientesCuentaLasSolicitudesPendientes() {
        when(solicitudRepository.countByEstado("PENDIENTE")).thenReturn(3L);

        assertEquals(3L, solicitudService.pendientes());
    }

    @Test
    void listarFiltraPorEstadoYTipo() {
        SolicitudAcceso solicitud = SolicitudAcceso.builder()
                .id(1).nombre("Ana").apellido("Pérez").pais("Chile").ciudad("Santiago")
                .correo("ana@ejemplo.cl").justificacion("Acceso requerido").estado("PENDIENTE")
                .tipo("CAMBIO").campo("").valor("").build();
        when(solicitudRepository.findByEstadoAndTipoOrderByIdAsc("PENDIENTE", "CAMBIO"))
                .thenReturn(List.of(solicitud));

        assertEquals(1, solicitudService.listar("PENDIENTE", "CAMBIO").size());
    }

    @Test
    void crearGuardaUnaSolicitudValida() {
        SolicitudRequest datos = new SolicitudRequest("Ana", "Pérez", "Chile", "Santiago",
                "ana@ejemplo.cl", "clave123", "Necesito acceso", "INGRESO", null, null, null);
        when(solicitudRepository.findFirstByCorreoAndEstado("ana@ejemplo.cl", "PENDIENTE"))
                .thenReturn(Optional.empty());
        when(solicitudRepository.save(any(SolicitudAcceso.class))).thenAnswer(invocacion -> {
            SolicitudAcceso solicitud = invocacion.getArgument(0);
            solicitud.setId(1);
            return solicitud;
        });

        assertEquals("PENDIENTE", solicitudService.crear(datos).estado());
        verify(solicitudRepository).save(any(SolicitudAcceso.class));
    }

    @Test
    void crearRechazaUnaSolicitudSinJustificacion() {
        SolicitudRequest datos = new SolicitudRequest("Ana", "Pérez", "Chile", "Santiago",
                "ana@ejemplo.cl", "clave123", " ", "INGRESO", null, null, null);

        assertThrows(ResponseStatusException.class, () -> solicitudService.crear(datos));
    }

    @Test
    void cambiarEstadoRechazaUnEstadoDesconocido() {
        assertThrows(ResponseStatusException.class, () -> solicitudService.cambiarEstado(1, "OTRO"));
    }
}
