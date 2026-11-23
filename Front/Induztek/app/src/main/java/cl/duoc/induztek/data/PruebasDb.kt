
package cl.duoc.induztek.data

import cl.duoc.induztek.model.Prueba
import cl.duoc.induztek.model.Resultado
import cl.duoc.induztek.model.TipoPrueba

fun InduztekDbHelper.listarPruebas(idProtocolo: Int): List<Prueba> {
    val lista = mutableListOf<Prueba>()
    val registros = filas(
        "SELECT id_prueba,id_protocolo,codigo_tipo_prueba,elemento,realizada,motivo_no_realizada," +
                "id_instrumento,resultado,comentario FROM prueba WHERE id_protocolo=? ORDER BY id_prueba",
        listOf(idProtocolo)
    )
    for (registro in registros) {
        val mediciones = mutableMapOf<String, Double>()
        val idPrueba = registro.entero("id_prueba")
        val filasMedicion = filas(
            "SELECT codigo_parametro,valor FROM medicion WHERE id_prueba=?",
            listOf(idPrueba)
        )
        for (filaMedicion in filasMedicion) {
            val codigo = filaMedicion.texto("codigo_parametro")
            var valor = 0.0
            val numero = filaMedicion.texto("valor").toDoubleOrNull()
            if (numero != null) valor = numero
            mediciones[codigo] = valor
        }
        val prueba = aPrueba(registro, mediciones)
        lista.add(prueba)
    }
    return lista
}

fun InduztekDbHelper.guardarPrueba(
    idProtocolo: Int, tipo: TipoPrueba, elemento: String, realizada: Boolean,
    motivo: String, idInstrumento: Int?, resultado: Resultado?, comentario: String,
    mediciones: Map<String, Double>
) {
    val registro = fila(
        "SELECT id_prueba FROM prueba WHERE id_protocolo=? AND codigo_tipo_prueba=? AND elemento=?",
        listOf(idProtocolo, tipo.name, elemento)
    )
    var existente: Int? = null
    if (registro != null) existente = registro.enteroSiHay("id_prueba")
    if (existente != null) {
        ejecutar("DELETE FROM medicion WHERE id_prueba=?", listOf(existente))
        ejecutar("DELETE FROM prueba WHERE id_prueba=?", listOf(existente))
    }

    var hecha = 0
    if (realizada) hecha = 1
    var codigoResultado: String? = null
    if (resultado != null) codigoResultado = resultado.codigo

    val idPrueba = insertar(
        "prueba",
        mapOf(
            "id_protocolo" to idProtocolo,
            "codigo_tipo_prueba" to tipo.name,
            "elemento" to elemento,
            "realizada" to hecha,
            "motivo_no_realizada" to motivo,
            "id_instrumento" to idInstrumento,
            "resultado" to codigoResultado,
            "comentario" to comentario
        )
    ).toInt()

    if (realizada) {
        for ((codigo, valor) in mediciones) {
            insertar(
                "medicion",
                mapOf("id_prueba" to idPrueba, "codigo_parametro" to codigo, "valor" to valor.toString())
            )
        }
    }
    encolarCambio(CAMBIO_PRUEBA, idProtocolo)
}

fun InduztekDbHelper.valorAnterior(
    idEquipo: Int, tipo: TipoPrueba, codigoParametro: String, excluirProtocolo: Int
): Double? {
    val registro = fila(
        "SELECT m.valor FROM medicion m " +
                "JOIN prueba p ON p.id_prueba=m.id_prueba " +
                "JOIN protocolo pr ON pr.id_protocolo=p.id_protocolo " +
                "WHERE pr.id_equipo=? AND p.codigo_tipo_prueba=? AND m.codigo_parametro=? " +
                "AND p.realizada=1 AND pr.id_protocolo!=? AND pr.estado!='ANULADO' " +
                "ORDER BY pr.fecha_hora_prueba DESC LIMIT 1",
        listOf(idEquipo, tipo.name, codigoParametro, excluirProtocolo)
    )
    if (registro == null) return null
    return registro.texto("valor").toDoubleOrNull()
}

fun InduztekDbHelper.valoresHistoricos(
    idEquipo: Int, tipo: TipoPrueba, codigoParametro: String, excluirProtocolo: Int
): List<String> {
    val lista = mutableListOf<String>()
    val registros = filas(
        "SELECT DISTINCT m.valor FROM medicion m " +
                "JOIN prueba p ON p.id_prueba=m.id_prueba " +
                "JOIN protocolo pr ON pr.id_protocolo=p.id_protocolo " +
                "WHERE pr.id_equipo=? AND p.codigo_tipo_prueba=? AND m.codigo_parametro=? " +
                "AND p.realizada=1 AND pr.id_protocolo!=? AND pr.estado!='ANULADO' " +
                "ORDER BY m.valor DESC LIMIT 6",
        listOf(idEquipo, tipo.name, codigoParametro, excluirProtocolo)
    )
    for (registro in registros) {
        var valor = registro.texto("valor")
        if (valor.endsWith(".0")) valor = valor.removeSuffix(".0")
        lista.add(valor)
    }
    return lista
}

fun InduztekDbHelper.historialAislacion(idEquipo: Int): List<Triple<String, String, Double>> {
    val lista = mutableListOf<Triple<String, String, Double>>()
    val registros = filas(
        "SELECT pr.numero, substr(pr.fecha_hora_prueba,1,10) AS fecha, m.valor FROM medicion m " +
                "JOIN prueba p ON p.id_prueba=m.id_prueba JOIN protocolo pr ON pr.id_protocolo=p.id_protocolo " +
                "WHERE pr.id_equipo=? AND p.codigo_tipo_prueba='AISLACION' AND m.codigo_parametro='LECTURA_1MIN' " +
                "AND pr.estado NOT IN ('BORRADOR','ANULADO') ORDER BY pr.fecha_hora_prueba",
        listOf(idEquipo)
    )
    for (registro in registros) {
        val numero = registro.texto("numero")
        val fecha = registro.texto("fecha")
        val valor = registro.texto("valor").toDouble()
        lista.add(Triple(numero, fecha, valor))
    }
    return lista
}
