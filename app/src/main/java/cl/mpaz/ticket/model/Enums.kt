package cl.mpaz.ticket.model

enum class Rol(val etiqueta: String) {
    FUNCIONARIO("Funcionario"),
    ENCARGADO_TI("Encargado de TI"),
    AUXILIAR("Personal auxiliar")
}

enum class EstadoTicket(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    REVISADO("Revisado"),
    ASIGNADO("Asignado"),
    EN_PROCESO("En proceso"),
    SOLUCIONADO("Solucionado"),
    CERRADO("Cerrado")
}

enum class Prioridad(val etiqueta: String) {
    BAJA("Baja"),
    MEDIA("Media"),
    ALTA("Alta"),
    URGENTE("Urgente")
}

enum class Categoria(val etiqueta: String) {
    INFRAESTRUCTURA("Infraestructura"),
    MOBILIARIO("Mobiliario"),
    TECNOLOGIA("Tecnología")
}
