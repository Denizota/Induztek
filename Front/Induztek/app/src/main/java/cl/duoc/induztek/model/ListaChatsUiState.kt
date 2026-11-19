package cl.duoc.induztek.model

enum class FiltroChats { TODOS, SIN_LEER }

data class ListaChatsUiState(
    val nombreUsuario: String = "",
    val rolUsuario: String = "",
    val chats: List<ChatResumen> = emptyList(),
    val busqueda: String = "",
    val filtro: FiltroChats = FiltroChats.TODOS
) {
    // Lo no estoy seguro si se hacia asi
    val chatsVisibles: List<ChatResumen>
        get() = chats
            .filter { filtro == FiltroChats.TODOS || it.sinLeer > 0 }
            .filter { it.nombre.contains(busqueda.trim(), ignoreCase = true) }
}