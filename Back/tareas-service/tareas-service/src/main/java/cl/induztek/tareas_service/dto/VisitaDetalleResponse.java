package cl.induztek.tareas_service.dto;

import java.util.List;

public record VisitaDetalleResponse(
        VisitaResponse visita,
        List<Integer> participantes,
        RecepcionResponse acta
) {
}
