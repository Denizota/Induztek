package cl.duoc.induztek.model



enum class TipoPrueba(val codigo: String, val nombre: String) {
    AISLACION("AISLACION", "Resistencia de aislación"),
    TTR("TTR", "Relación de transformación"),
    DEVANADOS("DEVANADOS", "Resistencia de devanados"),
    CONTACTOS("CONTACTOS", "Resistencia de contactos"),
    OPERACION_RELE("OPERACION_RELE", "Prueba de operación (inyección secundaria)")
}