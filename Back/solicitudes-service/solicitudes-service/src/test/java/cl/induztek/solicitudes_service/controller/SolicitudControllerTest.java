package cl.induztek.solicitudes_service.controller;

import cl.induztek.solicitudes_service.dto.SolicitudResponse;
import cl.induztek.solicitudes_service.service.SolicitudService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SolicitudControllerTest {

    @Mock
    private SolicitudService solicitudService;

    private MockMvc mockMvc;

    @BeforeEach
    void preparar() {
        mockMvc = MockMvcBuilders.standaloneSetup(new SolicitudController(solicitudService)).build();
    }

    private SolicitudResponse respuesta() {
        return new SolicitudResponse(1, "Ana", "Pérez", "Chile", "Lampa",
                "ana@duoc.cl", "motivo", "PENDIENTE", "INGRESO", null, "", "");
    }

    @Test
    void listarPasaLosFiltros() throws Exception {
        when(solicitudService.listar("PENDIENTE", "INGRESO")).thenReturn(List.of(respuesta()));

        mockMvc.perform(get("/api/solicitudes").param("estado", "PENDIENTE").param("tipo", "INGRESO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].correo").value("ana@duoc.cl"));
    }

    @Test
    void obtenerRespondeLaSolicitud() throws Exception {
        when(solicitudService.obtener(1)).thenReturn(respuesta());

        mockMvc.perform(get("/api/solicitudes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana"));
    }

    @Test
    void pendientesRespondeElTotal() throws Exception {
        when(solicitudService.pendientes()).thenReturn(5L);

        mockMvc.perform(get("/api/solicitudes/pendientes/total"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));
    }

    @Test
    void crearRespondeCreated() throws Exception {
        when(solicitudService.crear(any())).thenReturn(respuesta());

        mockMvc.perform(post("/api/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana\",\"apellido\":\"Pérez\",\"pais\":\"Chile\"," +
                                "\"ciudad\":\"Lampa\",\"correo\":\"ana@duoc.cl\",\"contrasena\":\"secreta\"," +
                                "\"justificacion\":\"motivo\",\"tipo\":\"INGRESO\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void cambiarEstadoRespondeLaSolicitud() throws Exception {
        SolicitudResponse aprobada = new SolicitudResponse(1, "Ana", "Pérez", "Chile", "Lampa",
                "ana@duoc.cl", "motivo", "APROBADA", "INGRESO", null, "", "");
        when(solicitudService.cambiarEstado(eq(1), eq("APROBADA"))).thenReturn(aprobada);

        mockMvc.perform(put("/api/solicitudes/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"APROBADA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"));
    }

    @Test
    void eliminarRespondeNoContent() throws Exception {
        mockMvc.perform(delete("/api/solicitudes/1"))
                .andExpect(status().isNoContent());

        verify(solicitudService).eliminar(1);
    }
}
