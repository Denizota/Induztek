package cl.induztek.tareas_service.dto;

public record CierreRequest(
        String receptorNombre,
        String receptorRut,
        String receptorCargo
) {
}
