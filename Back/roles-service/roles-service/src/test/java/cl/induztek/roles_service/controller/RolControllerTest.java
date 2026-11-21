package cl.induztek.roles_service.controller;

import cl.induztek.roles_service.model.Rol;
import cl.induztek.roles_service.service.RolService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RolControllerTest {

    @Mock
    private RolService rolService;

    private MockMvc mockMvc;

    @BeforeEach
    void preparar() {
        mockMvc = MockMvcBuilders.standaloneSetup(new RolController(rolService)).build();
    }

    @Test
    void listarRespondeLaLista() throws Exception {
        when(rolService.listar()).thenReturn(List.of(
                new Rol(1, "TECNICO", "Técnico de terreno")));

        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("TECNICO"));
    }

    @Test
    void obtenerRespondeElRol() throws Exception {
        when(rolService.obtener(3)).thenReturn(new Rol(3, "JEFE_TECNICO", "Jefe técnico"));

        mockMvc.perform(get("/api/roles/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Jefe técnico"));
    }

    @Test
    void porCodigoRespondeElRol() throws Exception {
        when(rolService.porCodigo("CLIENTE")).thenReturn(new Rol(4, "CLIENTE", "Cliente / Visitante"));

        mockMvc.perform(get("/api/roles/codigo/CLIENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4));
    }
}
