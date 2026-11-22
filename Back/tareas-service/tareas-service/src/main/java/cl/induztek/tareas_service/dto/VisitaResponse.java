package cl.induztek.tareas_service.dto;

public record VisitaResponse(
        Integer id,
        String numeroOt,
        Integer idInstalacion,
        String fecha,
        String tipoVisita,
        Integer idIngenieroCargo,
        String fechaHoraCierre,
        String descripcionMantenimiento
) {
}
