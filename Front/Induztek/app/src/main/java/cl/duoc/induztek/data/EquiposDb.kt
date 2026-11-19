
package cl.duoc.induztek.data

import cl.duoc.induztek.model.Equipo
import cl.duoc.induztek.model.Instrumento
import cl.duoc.induztek.model.TipoPrueba

private const val COLUMNAS_EQUIPO =
    "e.id_equipo,e.id_instalacion,e.tag,e.id_tipo_equipo,t.nombre AS tipo,t.clasificacion,e.marca,e.modelo," +
            "e.numero_serie,e.anio_fabricacion,e.potencia_kva,e.tension_primaria_kv,e.tension_secundaria_kv," +
            "e.corriente_nominal_a,e.max_contactos_uohm,e.observaciones,i.nombre AS instalacion"

private const val DESDE_EQUIPO =
    " FROM equipo e JOIN tipo_equipo t ON t.id_tipo_equipo=e.id_tipo_equipo " +
            "JOIN instalacion i ON i.id_instalacion=e.id_instalacion "

fun InduztekDbHelper.listarEquipos(): List<Equipo> {
    val lista = mutableListOf<Equipo>()
    val registros = filas("SELECT $COLUMNAS_EQUIPO $DESDE_EQUIPO ORDER BY e.tag")
    for (registro in registros) {
        val equipo = aEquipo(registro)
        lista.add(equipo)
    }
    return lista
}

fun InduztekDbHelper.listarEquiposDeCliente(idEmpresa: Int): List<Equipo> {
    val lista = mutableListOf<Equipo>()
    val registros = filas(
        "SELECT $COLUMNAS_EQUIPO $DESDE_EQUIPO WHERE i.id_usuario_cliente=? ORDER BY e.tag",
        listOf(idEmpresa)
    )
    for (registro in registros) {
        val equipo = aEquipo(registro)
        lista.add(equipo)
    }
    return lista
}

fun InduztekDbHelper.equipoPorId(id: Int): Equipo? {
    val fila = fila("SELECT $COLUMNAS_EQUIPO $DESDE_EQUIPO WHERE e.id_equipo=?", listOf(id))
    if (fila == null) return null
    return aEquipo(fila)
}

fun InduztekDbHelper.equipoDeCliente(idEquipo: Int, idEmpresa: Int): Equipo? {
    val equipo = equipoPorId(idEquipo)
    if (equipo == null) return null
    val esSuya = fila(
        "SELECT 1 FROM instalacion WHERE id_instalacion=? AND id_usuario_cliente=?",
        listOf(equipo.idInstalacion, idEmpresa)
    )
    if (esSuya == null) return null
    return equipo
}

fun InduztekDbHelper.tiposPruebaDeEquipo(idEquipo: Int): List<TipoPrueba> {
    val lista = mutableListOf<TipoPrueba>()
    val registros = filas(
        "SELECT tp.codigo FROM tipo_prueba tp " +
                "JOIN tipo_equipo_prueba x ON x.id_tipo_prueba=tp.id_tipo_prueba " +
                "JOIN equipo e ON e.id_tipo_equipo=x.id_tipo_equipo WHERE e.id_equipo=?",
        listOf(idEquipo)
    )
    for (registro in registros) {
        val codigo = registro.texto("codigo")
        lista.add(TipoPrueba.valueOf(codigo))
    }
    return lista
}

fun InduztekDbHelper.listarInstrumentos(): List<Instrumento> {
    val lista = mutableListOf<Instrumento>()
    val registros = filas(
        "SELECT id_instrumento,nombre,marca,modelo,numero_serie,fecha_ultima_cal,fecha_vence " +
                "FROM instrumento ORDER BY nombre"
    )
    for (registro in registros) {
        val instrumento = aInstrumento(registro)
        lista.add(instrumento)
    }
    return lista
}
