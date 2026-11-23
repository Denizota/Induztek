package cl.induztek.tareas_service.dto;

public record VisitaRequest(
        Integer idInstalacion,
        String fecha,
        String tipoVisita,
        Integer idIngenieroCargo,
        String descripcionMantenimiento
) {
}
