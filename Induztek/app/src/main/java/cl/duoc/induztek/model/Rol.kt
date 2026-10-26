package cl.duoc.induztek.model


enum class Rol(val codigo: String, val nombre: String, val puedeIngresar: Boolean = true) {
    TECNICO(
        "TECNICO",
        "Técnico de terreno",
        puedeIngresar = false),

    INGENIERO(
        "INGENIERO",
        "Ingeniero en pruebas"),

    JEFE_TECNICO("JEFE_TECNICO",
        "Jefe técnico",
        puedeIngresar = false),

    CLIENTE("CLIENTE",
        "Cliente / Visitante")
}
