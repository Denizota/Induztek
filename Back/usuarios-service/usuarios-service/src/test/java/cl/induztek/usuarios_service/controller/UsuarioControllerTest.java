package cl.induztek.usuarios_service.controller;

import cl.induztek.usuarios_service.dto.UsuarioResponse;
import cl.induztek.usuarios_service.service.UsuarioService;
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
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    private MockMvc mockMvc;

    @BeforeEach
    void preparar() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UsuarioController(usuarioService)).build();
    }

    private UsuarioResponse respuesta() {
        return new UsuarioResponse(5, "17.284.519-3", "Camila Soto", "camila@duoc.cl",
                "+56911111111", true, 2, "Ingeniero en pruebas", null, "", "", "", "", "", "", null, null);
    }

    @Test
    void listarPasaLaBusqueda() throws Exception {
        when(usuarioService.listar("camila")).thenReturn(List.of(respuesta()));

        mockMvc.perform(get("/api/usuarios").param("buscar", "camila"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Camila Soto"));
    }

    @Test
    void obtenerRespondeElUsuario() throws Exception {
        when(usuarioService.obtener(5)).thenReturn(respuesta());

        mockMvc.perform(get("/api/usuarios/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rolNombre").value("Ingeniero en pruebas"));
    }

    @Test
    void listarPorRolRespondeLaLista() throws Exception {
        when(usuarioService.listarPorRol(2)).thenReturn(List.of(respuesta()));

        mockMvc.perform(get("/api/usuarios/rol/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5));
    }

    @Test
    void listarPorActivoRespondeLaLista() throws Exception {
        when(usuarioService.listarPorActivo(true)).thenReturn(List.of(respuesta()));

        mockMvc.perform(get("/api/usuarios/activo/true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].correo").value("camila@duoc.cl"));
    }

    @Test
    void registrarRespondeCreated() throws Exception {
        when(usuarioService.registrar(any())).thenReturn(respuesta());

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rut\":\"17.284.519-3\",\"nombre\":\"Camila Soto\"," +
                                "\"correo\":\"camila@duoc.cl\",\"contrasena\":\"secreta\",\"idRol\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Camila Soto"));
    }

    @Test
    void iniciarSesionRespondeElUsuario() throws Exception {
        when(usuarioService.iniciarSesion(any())).thenReturn(respuesta());

        mockMvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"camila@duoc.cl\",\"contrasena\":\"secreta\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void actualizarRespondeElUsuario() throws Exception {
        when(usuarioService.actualizar(eq(5), any())).thenReturn(respuesta());

        mockMvc.perform(put("/api/usuarios/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Camila Soto\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rut").value("17.284.519-3"));
    }

    @Test
    void cambiarContrasenaRespondeNoContent() throws Exception {
        mockMvc.perform(put("/api/usuarios/5/contrasena")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actual\":\"secreta\",\"nueva\":\"nuevaClave\"}"))
                .andExpect(status().isNoContent());

        verify(usuarioService).cambiarContrasena(eq(5), any());
    }

    @Test
    void marcarActivoRespondeElUsuario() throws Exception {
        when(usuarioService.marcarActivo(5, false)).thenReturn(respuesta());

        mockMvc.perform(put("/api/usuarios/5/activo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\":false}"))
                .andExpect(status().isOk());
    }

    @Test
    void eliminarRespondeNoContent() throws Exception {
        mockMvc.perform(delete("/api/usuarios/5"))
                .andExpect(status().isNoContent());

        verify(usuarioService).eliminar(5);
    }
}
