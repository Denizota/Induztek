package cl.induztek.tareas_service.controller;

import cl.induztek.tareas_service.dto.CierreRequest;
import cl.induztek.tareas_service.dto.ParticipanteRequest;
import cl.induztek.tareas_service.dto.RecepcionResponse;
import cl.induztek.tareas_service.dto.VisitaDetalleResponse;
import cl.induztek.tareas_service.dto.VisitaRequest;
import cl.induztek.tareas_service.dto.VisitaResponse;
import cl.induztek.tareas_service.service.VisitaService;
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
@RequestMapping("/api/visitas")
@RequiredArgsConstructor
public class VisitaController {

    private final VisitaService visitaService;

    @GetMapping
    public List<VisitaResponse> listar(
            @RequestParam(required = false) Integer instalacion,
            @RequestParam(required = false) Integer ingeniero) {
        return visitaService.listar(instalacion, ingeniero);
    }

    @GetMapping("/abiertas")
    public List<VisitaResponse> listarAbiertas(@RequestParam(required = false) Integer instalacion) {
        return visitaService.listarAbiertas(instalacion);
    }

    @GetMapping("/{id}")
    public VisitaResponse obtener(@PathVariable Integer id) {
        return visitaService.obtener(id);
    }

    @GetMapping("/{id}/detalle")
    public VisitaDetalleResponse detalle(@PathVariable Integer id) {
        return visitaService.detalle(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VisitaResponse crear(@RequestBody VisitaRequest datos) {
        return visitaService.crear(datos);
    }

    @GetMapping("/{id}/participantes")
    public List<Integer> participantes(@PathVariable Integer id) {
        return visitaService.participantes(id);
    }

    @PostMapping("/{id}/participantes")
    public List<Integer> agregarParticipante(@PathVariable Integer id, @RequestBody ParticipanteRequest datos) {
        return visitaService.agregarParticipante(id, datos.idUsuario());
    }

    @DeleteMapping("/{id}/participantes/{idUsuario}")
    public List<Integer> quitarParticipante(@PathVariable Integer id, @PathVariable Integer idUsuario) {
        return visitaService.quitarParticipante(id, idUsuario);
    }

    @PutMapping("/{id}/cierre")
    public RecepcionResponse cerrar(@PathVariable Integer id, @RequestBody CierreRequest datos) {
        return visitaService.cerrar(id, datos);
    }

    @GetMapping("/{id}/acta")
    public RecepcionResponse acta(@PathVariable Integer id) {
        return visitaService.acta(id);
    }
}
