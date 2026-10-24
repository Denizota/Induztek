package cl.duoc.induztek.model


//me refiero a la fila del historial ese que es como un resumen OK
// pal client el grafico ese que NO SE VCOMO VOY A HACER
// SOCORRO POR FABOR impriman buenas librerias.
// Impriman mas libros.
data class FilaHistorial (
    val numero: String, //XDDDDDDDD
    val fecha: String,
    val valor: String,
    val resultado: String,
    val estado: String,
    val variacionPct: Double?
)