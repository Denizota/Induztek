package cl.induztek.tareas_service.dto;

public record RecepcionResponse(
        Integer idVisita,
        String receptorNombre,
        String receptorRut,
        String receptorCargo,
        String fechaHoraRecepcion
) {
}
