package cl.duoc.induztek.data.remote

data class InstalacionApi(
    val id: Int? = null,
    val idUsuarioCliente: Int? = null,
    val nombre: String? = null,
    val ubicacion: String? = null,
    val comuna: String? = null,
    val observacionesAcceso: String? = null
)

data class TipoEquipoApi(
    val id: Int? = null,
    val nombre: String? = null,
    val clasificacion: String? = null
)

data class InstrumentoApi(
    val id: Int? = null,
    val nombre: String? = null,
    val marca: String? = null,
    val modelo: String? = null,
    val numeroSerie: String? = null,
    val fechaUltimaCal: String? = null,
    val fechaVence: String? = null
)

data class EquipoApi(
    val id: Int? = null,
    val idInstalacion: Int? = null,
    val instalacionNombre: String? = null,
    val tag: String? = null,
    val idTipoEquipo: Int? = null,
    val tipoEquipo: String? = null,
    val clasificacion: String? = null,
    val marca: String? = null,
    val modelo: String? = null,
    val numeroSerie: String? = null,
    val anioFabricacion: Int? = null,
    val potenciaKva: Double? = null,
    val tensionPrimariaKv: Double? = null,
    val tensionSecundariaKv: Double? = null,
    val corrienteNominalA: Double? = null,
    val maxContactosUohm: Double? = null,
    val observaciones: String? = null
)

data class TipoPruebaApi(
    val id: Int? = null,
    val codigo: String? = null,
    val nombre: String? = null
)

data class VisitaApi(
    val id: Int? = null,
    val numeroOt: String? = null,
    val idInstalacion: Int? = null,
    val fecha: String? = null,
    val tipoVisita: String? = null,
    val idIngenieroCargo: Int? = null,
    val fechaHoraCierre: String? = null,
    val descripcionMantenimiento: String? = null
)

data class RecepcionApi(
    val idVisita: Int? = null,
    val receptorNombre: String? = null,
    val receptorRut: String? = null,
    val receptorCargo: String? = null,
    val fechaHoraRecepcion: String? = null
)

data class VisitaDetalleApi(
    val visita: VisitaApi? = null,
    val participantes: List<Int>? = null,
    val acta: RecepcionApi? = null
)

data class ProtocoloApi(
    val id: Int? = null,
    val numero: String? = null,
    val idVisita: Int? = null,
    val idEquipo: Int? = null,
    val idUsuarioRegistra: Int? = null,
    val fechaHoraPrueba: String? = null,
    val temperaturaC: Double? = null,
    val humedadPct: Double? = null,
    val estado: String? = null,
    val observaciones: String? = null
)

data class PruebaApi(
    val id: Int? = null,
    val codigoTipoPrueba: String? = null,
    val elemento: String? = null,
    val momento: String? = null,
    val realizada: Boolean? = null,
    val motivoNoRealizada: String? = null,
    val idInstrumento: Int? = null,
    val resultado: String? = null,
    val comentario: String? = null,
    val mediciones: Map<String, Double>? = null
)

data class HistorialApi(
    val id: Int? = null,
    val estadoAnterior: String? = null,
    val estadoNuevo: String? = null,
    val idUsuario: Int? = null,
    val fechaHora: String? = null,
    val motivo: String? = null
)

data class ProtocoloDetalleApi(
    val protocolo: ProtocoloApi? = null,
    val pruebas: List<PruebaApi>? = null,
    val historial: List<HistorialApi>? = null
)

data class SolicitudApi(
    val id: Int? = null,
    val nombre: String? = null,
    val apellido: String? = null,
    val pais: String? = null,
    val ciudad: String? = null,
    val correo: String? = null,
    val justificacion: String? = null,
    val estado: String? = null,
    val tipo: String? = null,
    val idUsuario: Int? = null,
    val campo: String? = null,
    val valor: String? = null
)

data class SolicitudApiRequest(
    val nombre: String,
    val apellido: String,
    val pais: String,
    val ciudad: String,
    val correo: String,
    val contrasena: String,
    val justificacion: String,
    val tipo: String = "INGRESO",
    val idUsuario: Int? = null,
    val campo: String = "",
    val valor: String = ""
)

data class EstadoSolicitudApiRequest(val estado: String)

data class VisitaApiRequest(
    val idInstalacion: Int,
    val fecha: String,
    val tipoVisita: String = "SOLO_PRUEBAS",
    val idIngenieroCargo: Int? = null,
    val descripcionMantenimiento: String = ""
)

data class ParticipanteApiRequest(val idUsuario: Int)

data class CierreApiRequest(
    val receptorNombre: String,
    val receptorRut: String,
    val receptorCargo: String
)

data class ProtocoloApiRequest(
    val idVisita: Int,
    val idEquipo: Int,
    val idUsuarioRegistra: Int,
    val temperaturaC: Double? = null,
    val humedadPct: Double? = null,
    val observaciones: String = ""
)

data class AmbientesApiRequest(
    val temperaturaC: Double?,
    val humedadPct: Double?,
    val observaciones: String
)

data class EstadoProtocoloApiRequest(
    val estado: String,
    val idUsuario: Int,
    val motivo: String = ""
)

data class PruebaApiRequest(
    val codigoTipoPrueba: String,
    val elemento: String,
    val realizada: Boolean,
    val motivoNoRealizada: String = "",
    val idInstrumento: Int? = null,
    val resultado: String? = null,
    val comentario: String = "",
    val mediciones: Map<String, Double> = emptyMap()
)

data class RegistroApiRequest(
    val rut: String,
    val nombre: String,
    val correo: String?,
    val contrasena: String,
    val telefono: String = "",
    val idRol: Int,
    val idEmpresa: Int? = null,
    val cargo: String = "",
    val giro: String = ""
)

data class ActualizarApiRequest(
    val nombre: String,
    val correo: String?,
    val telefono: String,
    val idRol: Int,
    val idEmpresa: Int?,
    val cargo: String,
    val giro: String,
    val bio: String,
    val especialidades: String,
    val formacion: String,
    val certificaciones: String,
    val desde: Int?,
    val calificacion: Int?
)

data class ActivoApiRequest(val activo: Boolean)
