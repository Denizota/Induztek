package cl.induztek.equipos_service.controller;

import cl.induztek.equipos_service.dto.EquipoRequest;
import cl.induztek.equipos_service.dto.EquipoResponse;
import cl.induztek.equipos_service.model.Instalacion;
import cl.induztek.equipos_service.model.Instrumento;
import cl.induztek.equipos_service.model.TipoEquipo;
import cl.induztek.equipos_service.service.EquipoService;
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
@RequestMapping("/api")
@RequiredArgsConstructor
public class EquipoController {

    private final EquipoService equipoService;

    @GetMapping("/instalaciones")
    public List<Instalacion> listarInstalaciones(@RequestParam(required = false) Integer cliente) {
        return equipoService.listarInstalaciones(cliente);
    }

    @GetMapping("/tipos-equipo")
    public List<TipoEquipo> listarTipos() {
        return equipoService.listarTipos();
    }

    @GetMapping("/instrumentos")
    public List<Instrumento> listarInstrumentos() {
        return equipoService.listarInstrumentos();
    }

    @GetMapping("/instrumentos/vencidos")
    public List<Instrumento> instrumentosVencidos(@RequestParam String fecha) {
        return equipoService.instrumentosVencidos(fecha);
    }

    @GetMapping("/equipos")
    public List<EquipoResponse> listar(
            @RequestParam(required = false) Integer cliente,
            @RequestParam(required = false) Integer instalacion) {
        return equipoService.listar(cliente, instalacion);
    }

    @GetMapping("/equipos/{id}")
    public EquipoResponse obtener(@PathVariable Integer id) {
        return equipoService.obtener(id);
    }

    @PostMapping("/equipos")
    @ResponseStatus(HttpStatus.CREATED)
    public EquipoResponse crear(@RequestBody EquipoRequest datos) {
        return equipoService.crear(datos);
    }

    @PutMapping("/equipos/{id}")
    public EquipoResponse actualizar(@PathVariable Integer id, @RequestBody EquipoRequest datos) {
        return equipoService.actualizar(id, datos);
    }

    @DeleteMapping("/equipos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        equipoService.eliminar(id);
    }
}
