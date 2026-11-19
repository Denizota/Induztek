
package cl.duoc.induztek.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun fechaHoy(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
}

fun fechaHoraAhora(): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
}

internal fun String.fechaCorta(): String {
    val partes = split("-")
    if (partes.size == 3) {
        return partes[2] + "-" + partes[1] + "-" + partes[0]
    }
    return this
}
