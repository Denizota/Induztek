package cl.induztek.equipos_service.controller;

import cl.induztek.equipos_service.dto.EquipoResponse;
import cl.induztek.equipos_service.model.Instalacion;
import cl.induztek.equipos_service.model.Instrumento;
import cl.induztek.equipos_service.model.TipoEquipo;
import cl.induztek.equipos_service.service.EquipoService;
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
class EquipoControllerTest {

    @Mock
    private EquipoService equipoService;

    private MockMvc mockMvc;

    @BeforeEach
    void preparar() {
        mockMvc = MockMvcBuilders.standaloneSetup(new EquipoController(equipoService)).build();
    }

    private EquipoResponse respuesta() {
        return new EquipoResponse(10, 1, "Planta Lampa", "TR-01", 1, "Transformador", "PRIMARIO",
                "Rhona", "RH-18733", "RH-18733", 2012, 1500.0, 12.0, 0.4, null, null, "");
    }

    @Test
    void listarInstalacionesPasaElCliente() throws Exception {
        when(equipoService.listarInstalaciones(2))
                .thenReturn(List.of(Instalacion.builder().id(1).nombre("Planta Lampa").build()));

        mockMvc.perform(get("/api/instalaciones").param("cliente", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Planta Lampa"));
    }

    @Test
    void listarTiposRespondeLaLista() throws Exception {
        when(equipoService.listarTipos())
                .thenReturn(List.of(TipoEquipo.builder().id(1).nombre("Transformador").clasificacion("PRIMARIO").build()));

        mockMvc.perform(get("/api/tipos-equipo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clasificacion").value("PRIMARIO"));
    }

    @Test
    void listarInstrumentosRespondeLaLista() throws Exception {
        when(equipoService.listarInstrumentos())
                .thenReturn(List.of(Instrumento.builder().id(1).nombre("Megóhmetro").build()));

        mockMvc.perform(get("/api/instrumentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Megóhmetro"));
    }

    @Test
    void instrumentosVencidosPasaLaFecha() throws Exception {
        when(equipoService.instrumentosVencidos("2026-11-19"))
                .thenReturn(List.of(Instrumento.builder().id(1).nombre("Megóhmetro").build()));

        mockMvc.perform(get("/api/instrumentos/vencidos").param("fecha", "2026-11-19"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void listarEquiposPasaLosFiltros() throws Exception {
        when(equipoService.listar(2, 1)).thenReturn(List.of(respuesta()));

        mockMvc.perform(get("/api/equipos").param("cliente", "2").param("instalacion", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tag").value("TR-01"));
    }

    @Test
    void obtenerRespondeElEquipo() throws Exception {
        when(equipoService.obtener(10)).thenReturn(respuesta());

        mockMvc.perform(get("/api/equipos/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void crearRespondeCreated() throws Exception {
        when(equipoService.crear(any())).thenReturn(respuesta());

        mockMvc.perform(post("/api/equipos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idInstalacion\":1,\"tag\":\"TR-01\",\"idTipoEquipo\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tag").value("TR-01"));
    }

    @Test
    void actualizarRespondeElEquipo() throws Exception {
        when(equipoService.actualizar(eq(10), any())).thenReturn(respuesta());

        mockMvc.perform(put("/api/equipos/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idInstalacion\":1,\"tag\":\"TR-01\",\"idTipoEquipo\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoEquipo").value("Transformador"));
    }

    @Test
    void eliminarRespondeNoContent() throws Exception {
        mockMvc.perform(delete("/api/equipos/10"))
                .andExpect(status().isNoContent());

        verify(equipoService).eliminar(10);
    }
}
