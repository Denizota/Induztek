package cl.induztek.tareas_service.controller;

import cl.induztek.tareas_service.dto.RecepcionResponse;
import cl.induztek.tareas_service.dto.VisitaDetalleResponse;
import cl.induztek.tareas_service.dto.VisitaResponse;
import cl.induztek.tareas_service.service.VisitaService;
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
class VisitaControllerTest {

    @Mock
    private VisitaService visitaService;

    private MockMvc mockMvc;

    @BeforeEach
    void preparar() {
        mockMvc = MockMvcBuilders.standaloneSetup(new VisitaController(visitaService)).build();
    }

    private VisitaResponse respuesta() {
        return new VisitaResponse(1, "OT-2026-1301", 3, "2026-11-19", "SOLO_PRUEBAS", 2, null, "");
    }

    private RecepcionResponse acta() {
        return new RecepcionResponse(1, "Ana Pérez", "17.284.519-3", "Administradora", "2026-11-19 10:30");
    }

    @Test
    void listarPasaLosFiltros() throws Exception {
        when(visitaService.listar(3, 2)).thenReturn(List.of(respuesta()));

        mockMvc.perform(get("/api/visitas").param("instalacion", "3").param("ingeniero", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numeroOt").value("OT-2026-1301"));
    }

    @Test
    void listarAbiertasPasaLaInstalacion() throws Exception {
        when(visitaService.listarAbiertas(3)).thenReturn(List.of(respuesta()));

        mockMvc.perform(get("/api/visitas/abiertas").param("instalacion", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void obtenerRespondeLaVisita() throws Exception {
        when(visitaService.obtener(1)).thenReturn(respuesta());

        mockMvc.perform(get("/api/visitas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoVisita").value("SOLO_PRUEBAS"));
    }

    @Test
    void detalleRespondeLaVisitaCompleta() throws Exception {
        when(visitaService.detalle(1))
                .thenReturn(new VisitaDetalleResponse(respuesta(), List.of(5), acta()));

        mockMvc.perform(get("/api/visitas/1/detalle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participantes[0]").value(5))
                .andExpect(jsonPath("$.acta.receptorNombre").value("Ana Pérez"));
    }

    @Test
    void crearRespondeCreated() throws Exception {
        when(visitaService.crear(any())).thenReturn(respuesta());

        mockMvc.perform(post("/api/visitas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idInstalacion\":3,\"idIngenieroCargo\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroOt").value("OT-2026-1301"));
    }

    @Test
    void participantesRespondeLosIds() throws Exception {
        when(visitaService.participantes(1)).thenReturn(List.of(5, 6));

        mockMvc.perform(get("/api/visitas/1/participantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1]").value(6));
    }

    @Test
    void agregarParticipanteRespondeLaListaNueva() throws Exception {
        when(visitaService.agregarParticipante(1, 7)).thenReturn(List.of(5, 7));

        mockMvc.perform(post("/api/visitas/1/participantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idUsuario\":7}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1]").value(7));
    }

    @Test
    void quitarParticipanteRespondeLaLista() throws Exception {
        when(visitaService.quitarParticipante(1, 7)).thenReturn(List.of(5));

        mockMvc.perform(delete("/api/visitas/1/participantes/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(5));
    }

    @Test
    void cerrarRespondeElActa() throws Exception {
        when(visitaService.cerrar(eq(1), any())).thenReturn(acta());

        mockMvc.perform(put("/api/visitas/1/cierre")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"receptorNombre\":\"Ana Pérez\",\"receptorRut\":\"17.284.519-3\"," +
                                "\"receptorCargo\":\"Administradora\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaHoraRecepcion").value("2026-11-19 10:30"));
    }

    @Test
    void actaRespondeLaRecepcion() throws Exception {
        when(visitaService.acta(1)).thenReturn(acta());

        mockMvc.perform(get("/api/visitas/1/acta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receptorRut").value("17.284.519-3"));

        verify(visitaService).acta(1);
    }
}
