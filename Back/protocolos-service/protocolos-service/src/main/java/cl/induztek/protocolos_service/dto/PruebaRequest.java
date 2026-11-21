package cl.induztek.protocolos_service.dto;

import java.util.Map;

public record PruebaRequest(
        String codigoTipoPrueba,
        String elemento,
        Boolean realizada,
        String motivoNoRealizada,
        Integer idInstrumento,
        String resultado,
        String comentario,
        Map<String, Double> mediciones
) {
}
