package cl.induztek.solicitudes_service.controller;

import cl.induztek.solicitudes_service.dto.EstadoSolicitudRequest;
import cl.induztek.solicitudes_service.dto.SolicitudRequest;
import cl.induztek.solicitudes_service.dto.SolicitudResponse;
import cl.induztek.solicitudes_service.service.SolicitudService;
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
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;

    @GetMapping
    public List<SolicitudResponse> listar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String tipo) {
        return solicitudService.listar(estado, tipo);
    }

    @GetMapping("/{id}")
    public SolicitudResponse obtener(@PathVariable Integer id) {
        return solicitudService.obtener(id);
    }

    @GetMapping("/pendientes/total")
    public long pendientes() {
        return solicitudService.pendientes();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SolicitudResponse crear(@RequestBody SolicitudRequest datos) {
        return solicitudService.crear(datos);
    }

    @PutMapping("/{id}/estado")
    public SolicitudResponse cambiarEstado(@PathVariable Integer id, @RequestBody EstadoSolicitudRequest datos) {
        return solicitudService.cambiarEstado(id, datos.estado());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        solicitudService.eliminar(id);
    }
}
