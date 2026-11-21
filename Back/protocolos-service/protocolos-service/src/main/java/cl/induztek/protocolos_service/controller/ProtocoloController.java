package cl.induztek.protocolos_service.controller;

import cl.induztek.protocolos_service.dto.AmbientesRequest;
import cl.induztek.protocolos_service.dto.EstadoProtocoloRequest;
import cl.induztek.protocolos_service.dto.HistorialResponse;
import cl.induztek.protocolos_service.dto.ProtocoloDetalleResponse;
import cl.induztek.protocolos_service.dto.ProtocoloRequest;
import cl.induztek.protocolos_service.dto.ProtocoloResponse;
import cl.induztek.protocolos_service.dto.PruebaRequest;
import cl.induztek.protocolos_service.dto.PruebaResponse;
import cl.induztek.protocolos_service.model.TipoPrueba;
import cl.induztek.protocolos_service.service.ProtocoloService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
public class ProtocoloController {

    private final ProtocoloService protocoloService;

    @GetMapping("/tipos-prueba")
    public List<TipoPrueba> listarTipos() {
        return protocoloService.listarTipos();
    }

    @GetMapping("/tipos-equipo/{id}/pruebas")
    public List<TipoPrueba> listarTiposDeEquipo(@PathVariable Integer id) {
        return protocoloService.listarTiposDeEquipo(id);
    }

    @GetMapping("/protocolos")
    public List<ProtocoloResponse> listar(
            @RequestParam(required = false) Integer visita,
            @RequestParam(required = false) Integer equipo) {
        return protocoloService.listar(visita, equipo);
    }

    @GetMapping("/protocolos/{id}")
    public ProtocoloResponse obtener(@PathVariable Integer id) {
        return protocoloService.obtener(id);
    }

    @GetMapping("/protocolos/{id}/detalle")
    public ProtocoloDetalleResponse detalle(@PathVariable Integer id) {
        return protocoloService.detalle(id);
    }

    @GetMapping("/protocolos/{id}/historial")
    public List<HistorialResponse> historial(@PathVariable Integer id) {
        return protocoloService.historial(id);
    }

    @PostMapping("/protocolos")
    @ResponseStatus(HttpStatus.CREATED)
    public ProtocoloResponse crear(@RequestBody ProtocoloRequest datos) {
        return protocoloService.crear(datos);
    }

    @PutMapping("/protocolos/{id}/ambientes")
    public ProtocoloResponse actualizarAmbientes(@PathVariable Integer id, @RequestBody AmbientesRequest datos) {
        return protocoloService.actualizarAmbientes(id, datos);
    }

    @PostMapping("/protocolos/{id}/pruebas")
    public PruebaResponse guardarPrueba(@PathVariable Integer id, @RequestBody PruebaRequest datos) {
        return protocoloService.guardarPrueba(id, datos);
    }

    @PutMapping("/protocolos/{id}/estado")
    public ProtocoloResponse cambiarEstado(@PathVariable Integer id, @RequestBody EstadoProtocoloRequest datos) {
        return protocoloService.cambiarEstado(id, datos);
    }
}
