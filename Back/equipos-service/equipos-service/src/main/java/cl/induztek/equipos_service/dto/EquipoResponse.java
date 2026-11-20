package cl.induztek.equipos_service.dto;

public record EquipoResponse(
        Integer id,
        Integer idInstalacion,
        String instalacionNombre,
        String tag,
        Integer idTipoEquipo,
        String tipoEquipo,
        String clasificacion,
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
