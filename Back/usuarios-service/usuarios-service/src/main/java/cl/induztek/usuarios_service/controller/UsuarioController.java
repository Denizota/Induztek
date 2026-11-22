package cl.induztek.usuarios_service.controller;

import cl.induztek.usuarios_service.dto.ActivoRequest;
import cl.induztek.usuarios_service.dto.ActualizarRequest;
import cl.induztek.usuarios_service.dto.CambiarContrasenaRequest;
import cl.induztek.usuarios_service.dto.LoginRequest;
import cl.induztek.usuarios_service.dto.RegistroRequest;
import cl.induztek.usuarios_service.dto.UsuarioResponse;
import cl.induztek.usuarios_service.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public List<UsuarioResponse> listar(@RequestParam(required = false) String buscar) {
        return usuarioService.listar(buscar);
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Integer id) {
        return usuarioService.obtener(id);
    }

    @GetMapping("/rol/{idRol}")
    public List<UsuarioResponse> listarPorRol(@PathVariable Integer idRol) {
        return usuarioService.listarPorRol(idRol);
    }

    @GetMapping("/activo/{activo}")
    public List<UsuarioResponse> listarPorActivo(@PathVariable Boolean activo) {
        return usuarioService.listarPorActivo(activo);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@RequestBody RegistroRequest datos) {
        return usuarioService.registrar(datos);
    }

    @PostMapping("/login")
    public UsuarioResponse iniciarSesion(@RequestBody LoginRequest datos) {
        return usuarioService.iniciarSesion(datos);
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Integer id, @RequestBody ActualizarRequest datos) {
        return usuarioService.actualizar(id, datos);
    }

    @PutMapping("/{id}/contrasena")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarContrasena(@PathVariable Integer id, @RequestBody CambiarContrasenaRequest datos) {
        usuarioService.cambiarContrasena(id, datos);
    }

    @PutMapping("/{id}/activo")
    public UsuarioResponse marcarActivo(@PathVariable Integer id, @RequestBody ActivoRequest datos) {
        return usuarioService.marcarActivo(id, datos.activo());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        usuarioService.eliminar(id);
    }
}
