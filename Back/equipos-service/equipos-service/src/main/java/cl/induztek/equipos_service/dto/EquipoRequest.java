package cl.induztek.equipos_service.dto;

public record EquipoRequest(
        Integer idInstalacion,
        String tag,
        Integer idTipoEquipo,
        String marca,
        String modelo,
        String numeroSerie,
        Integer anioFabricacion,
        Double potenciaKva,
        Double tensionPrimariaKv,
        Double tensionSecundariaKv,
        Double corrienteNominalA,
        Double maxContactosUohm,
        String observaciones
) {
}
