package cl.induztek.protocolos_service.controller;

import cl.induztek.protocolos_service.dto.ProtocoloDetalleResponse;
import cl.induztek.protocolos_service.dto.ProtocoloResponse;
import cl.induztek.protocolos_service.dto.PruebaResponse;
import cl.induztek.protocolos_service.model.TipoPrueba;
import cl.induztek.protocolos_service.service.ProtocoloService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProtocoloControllerTest {

    @Mock
    private ProtocoloService protocoloService;

    private MockMvc mockMvc;

    @BeforeEach
    void preparar() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ProtocoloController(protocoloService)).build();
    }

    private ProtocoloResponse respuesta() {
        return new ProtocoloResponse(1, "PT-2026-401", 3, 10, 2, "2026-11-19 10:00",
                22.5, 48.0, "BORRADOR", "");
    }

    @Test
    void listarTiposRespondeLaLista() throws Exception {
        when(protocoloService.listarTipos())
                .thenReturn(List.of(new TipoPrueba(1, "AISLACION", "Aislación")));

        mockMvc.perform(get("/api/tipos-prueba"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("AISLACION"));
    }

    @Test
    void listarTiposDeEquipoRespondeLaLista() throws Exception {
        when(protocoloService.listarTiposDeEquipo(1))
                .thenReturn(List.of(new TipoPrueba(1, "AISLACION", "Aislación")));

        mockMvc.perform(get("/api/tipos-equipo/1/pruebas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Aislación"));
    }

    @Test
    void listarProtocolosPasaLosFiltros() throws Exception {
        when(protocoloService.listar(3, null)).thenReturn(List.of(respuesta()));

        mockMvc.perform(get("/api/protocolos").param("visita", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numero").value("PT-2026-401"));
    }

    @Test
    void obtenerRespondeElProtocolo() throws Exception {
        when(protocoloService.obtener(1)).thenReturn(respuesta());

        mockMvc.perform(get("/api/protocolos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("BORRADOR"));
    }

    @Test
    void detalleRespondePruebasEHistorial() throws Exception {
        when(protocoloService.detalle(1))
                .thenReturn(new ProtocoloDetalleResponse(respuesta(), List.of(), List.of()));

        mockMvc.perform(get("/api/protocolos/1/detalle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.protocolo.id").value(1));
    }

    @Test
    void historialRespondeLosPasos() throws Exception {
        when(protocoloService.historial(1)).thenReturn(List.of());

        mockMvc.perform(get("/api/protocolos/1/historial"))
                .andExpect(status().isOk());
    }

    @Test
    void crearRespondeCreated() throws Exception {
        when(protocoloService.crear(any())).thenReturn(respuesta());

        mockMvc.perform(post("/api/protocolos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idVisita\":3,\"idEquipo\":10,\"idUsuarioRegistra\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numero").value("PT-2026-401"));
    }

    @Test
    void actualizarAmbientesRespondeElProtocolo() throws Exception {
        when(protocoloService.actualizarAmbientes(eq(1), any())).thenReturn(respuesta());

        mockMvc.perform(put("/api/protocolos/1/ambientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"temperaturaC\":22.5,\"humedadPct\":48.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperaturaC").value(22.5));
    }

    @Test
    void guardarPruebaRespondeLaPrueba() throws Exception {
        when(protocoloService.guardarPrueba(eq(1), any())).thenReturn(
                new PruebaResponse(20, "AISLACION", "", "UNICA", true, "", 1,
                        "APROBADO", "", Map.of("R60", 2500.0)));

        mockMvc.perform(post("/api/protocolos/1/pruebas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigoTipoPrueba\":\"AISLACION\",\"realizada\":true," +
                                "\"resultado\":\"APROBADO\",\"mediciones\":{\"R60\":2500.0}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value("APROBADO"))
                .andExpect(jsonPath("$.mediciones.R60").value(2500.0));
    }

    @Test
    void cambiarEstadoRespondeElProtocolo() throws Exception {
        when(protocoloService.cambiarEstado(eq(1), any())).thenReturn(respuesta());

        mockMvc.perform(put("/api/protocolos/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"REGISTRADO_TERRENO\",\"idUsuario\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
