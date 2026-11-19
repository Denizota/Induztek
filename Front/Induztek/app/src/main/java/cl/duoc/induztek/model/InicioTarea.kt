package cl.duoc.induztek.model

enum class GrupoInicio { BORRADOR, POR_FORMALIZAR, DEVUELTO, CERRADO }

data class InicioTarea(
    val idEquipo: Int,
    val tag: String,
    val tipoEquipo: String,
    val instalacion: String,
    val idCliente: Int,
    val cliente: String,
    val contacto: String = "",
    val numeroSerie: String,
    val numero: String,
    val fecha: String,
    val estado: EstadoProtocolo?,
    val resultado: Resultado?,
    val atrasado: Boolean,
    val pruebasTotales: Int = 0,
    val pruebasPendientes: Int = 0
) {
    val grupo: GrupoInicio
        get() {
            if (estado == null || estado == EstadoProtocolo.BORRADOR) return GrupoInicio.BORRADOR
            if (estado == EstadoProtocolo.REGISTRADO) return GrupoInicio.POR_FORMALIZAR
            if (estado == EstadoProtocolo.OBSERVADO) return GrupoInicio.DEVUELTO
            return GrupoInicio.CERRADO
        }

    val estadoLinea: String
        get() {
            val actual = estado
            if (actual == null) return EstadoProtocolo.BORRADOR.nombre
            val resultadoActual = resultado
            if (resultadoActual != null && actual == EstadoProtocolo.REGISTRADO) {
                return actual.nombre + " - " + resultadoActual.nombre
            }
            return actual.nombre
        }
}
