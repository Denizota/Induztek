package cl.induztek.protocolos_service.dto;

import java.util.Map;

public record PruebaResponse(
        Integer id,
        String codigoTipoPrueba,
        String elemento,
        String momento,
        Boolean realizada,
        String motivoNoRealizada,
        Integer idInstrumento,
        String resultado,
        String comentario,
        Map<String, Double> mediciones
) {
}
