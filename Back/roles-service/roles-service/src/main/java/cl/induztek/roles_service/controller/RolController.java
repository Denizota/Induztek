package cl.induztek.roles_service.controller;

import cl.induztek.roles_service.model.Rol;
import cl.induztek.roles_service.service.RolService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolService rolService;

    @GetMapping
    public List<Rol> listar() {
        return rolService.listar();
    }

    @GetMapping("/{id}")
    public Rol obtener(@PathVariable Integer id) {
        return rolService.obtener(id);
    }

    @GetMapping("/codigo/{codigo}")
    public Rol porCodigo(@PathVariable String codigo) {
        return rolService.porCodigo(codigo);
    }
}
