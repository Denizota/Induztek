
package cl.duoc.induztek.data

import android.content.ContentValues


internal fun InduztekDbHelper.filas(sql: String, args: List<Any?> = emptyList()): List<Map<String, String?>> {
    val cursor = writableDatabase.rawQuery(sql, aArgumentos(args))
    val lista = mutableListOf<Map<String, String?>>()
    val columnas = cursor.columnNames

    while (cursor.moveToNext()) {
        val fila = mutableMapOf<String, String?>()
        for (indice in columnas.indices) {
            fila[columnas[indice]] = cursor.getString(indice)
        }
        lista.add(fila)
    }

    cursor.close()
    return lista
}

internal fun InduztekDbHelper.fila(sql: String, args: List<Any?> = emptyList()): Map<String, String?>? {
    val resultado = filas(sql, args)
    if (resultado.isEmpty()) {
        return null
    }
    return resultado[0]
}

internal fun InduztekDbHelper.contar(sql: String, args: List<Any?> = emptyList()): Int {
    val registro = fila(sql, args)
    if (registro == null) {
        return 0
    }
    val valores = registro.values.toList()
    if (valores.isEmpty()) {
        return 0
    }
    val valor = valores[0]
    if (valor == null) {
        return 0
    }
    val numero = valor.toIntOrNull()
    if (numero == null) {
        return 0
    }
    return numero
}

internal fun InduztekDbHelper.ejecutar(sql: String, args: List<Any?> = emptyList()) {
    writableDatabase.execSQL(sql, aArgumentos(args))
}

internal fun aValores(datos: Map<String, Any?>): ContentValues {
    val valores = ContentValues()
    for ((columna, valor) in datos) {
        if (valor == null) {
            valores.putNull(columna)
        } else if (valor is Int) {
            valores.put(columna, valor)
        } else if (valor is Long) {
            valores.put(columna, valor)
        } else if (valor is Double) {
            valores.put(columna, valor)
        } else if (valor is Boolean) {
            valores.put(columna, if (valor) 1 else 0)
        } else {
            valores.put(columna, valor.toString())
        }
    }
    return valores
}

internal fun InduztekDbHelper.insertar(tabla: String, datos: Map<String, Any?>): Long {
    return writableDatabase.insert(tabla, null, aValores(datos))
}

internal fun InduztekDbHelper.actualizar(
    tabla: String, datos: Map<String, Any?>, donde: String, args: List<Any?> = emptyList()
): Boolean {
    val cambiadas = writableDatabase.update(tabla, aValores(datos), donde, aArgumentos(args))
    return cambiadas > 0
}

private fun aArgumentos(args: List<Any?>): Array<String?> {
    val argumentos = arrayOfNulls<String>(args.size)
    for (indice in args.indices) {
        val valor = args[indice]
        if (valor != null) {
            argumentos[indice] = valor.toString()
        }
    }
    return argumentos
}

internal fun Map<String, String?>.texto(columna: String): String {
    val valor = this[columna]
    if (valor == null) return ""
    return valor
}

internal fun Map<String, String?>.entero(columna: String): Int {
    val valor = this[columna]
    if (valor == null) return 0
    val numero = valor.toIntOrNull()
    if (numero == null) return 0
    return numero
}

internal fun Map<String, String?>.enteroSiHay(columna: String): Int? {
    val valor = this[columna]
    if (valor == null) {
        return null
    }
    return valor.toIntOrNull()
}

internal fun Map<String, String?>.decimal(columna: String): Double? {
    val valor = this[columna]
    if (valor == null) return null
    return valor.toDoubleOrNull()
}

internal fun Map<String, String?>.siNo(columna: String): Boolean = entero(columna) == 1
