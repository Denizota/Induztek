package cl.duoc.induztek.model

data class Instalacion(
    val idInstalacion: Int,
    val idUsuarioCliente: Int,
    val nombre: String,
    val ubicacion: String,
    val comuna: String,
    val observacionesAcceso: String,
    val clienteNombre: String = ""
)

data class Visita(
    val idVisita: Int,
    val numeroOt: String,
    val idInstalacion: Int,
    val fecha: String,
    val tipoVisita: String,
    val idIngenieroCargo: Int?,
    val fechaHoraCierre: String?
)

data class RecepcionActa(
    val receptorNombre: String,
    val receptorRut: String,
    val receptorCargo: String,
    val fechaHoraRecepcion: String
)
