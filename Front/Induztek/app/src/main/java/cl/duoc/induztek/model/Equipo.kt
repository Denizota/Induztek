package cl.duoc.induztek.model

data class Equipo(
    val idEquipo: Int,
    val idInstalacion: Int,
    val tag: String,
    val idTipoEquipo: Int,
    val tipoEquipo: String,
    val clasificacion: String,
    val marca: String,
    val modelo: String,
    val numeroSerie: String,
    val anioFabricacion: Int,
    val potenciaKva: Double?,
    val tensionPrimariaKv: Double?,
    val tensionSecundariaKv: Double?,
    val corrienteNominalA: Double?,
    val maxContactosUohm: Double?,
    val observaciones: String,
    val instalacionNombre: String = ""
) {
    val exigeLectura10Min: Boolean
        get() = tipoEquipo == "Transformador" || tipoEquipo == "Cable de poder"
}
