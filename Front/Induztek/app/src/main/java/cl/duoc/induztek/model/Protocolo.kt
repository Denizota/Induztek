package cl.duoc.induztek.model

data class Protocolo(
    val idProtocolo: Int,
    val numero: String,
    val idVisita: Int,
    val idEquipo: Int,
    val idUsuarioRegistra: Int,
    val fechaHoraPrueba: String,
    val temperaturaC: Double?,
    val humedadPct: Double?,
    val estado: EstadoProtocolo,
    val observaciones: String = "",
    val equipoTag: String = "",
    val tipoEquipo: String = "",
    val instalacionNombre: String = "",
    val tecnicoNombre: String = ""
)
