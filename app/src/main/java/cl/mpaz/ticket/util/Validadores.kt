package cl.mpaz.ticket.util

/** Cada función devuelve el mensaje de error, o null si el valor es válido. */
object Validadores {
    private val REGEX_CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun nombre(valor: String): String? = when {
        valor.isBlank() -> "Ingresa tu nombre"
        valor.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        valor.trim().length > 50 -> "Máximo 50 caracteres"
        else -> null
    }

    fun correo(valor: String): String? = when {
        valor.isBlank() -> "Ingresa tu correo"
        !REGEX_CORREO.matches(valor.trim()) -> "Correo no válido"
        else -> null
    }

    fun passwordLogin(valor: String): String? =
        if (valor.isEmpty()) "Ingresa tu contraseña" else null

    fun passwordRegistro(valor: String): String? = when {
        valor.isEmpty() -> "Ingresa una contraseña"
        valor.length < 8 -> "Mínimo 8 caracteres"
        valor.none { it.isLetter() } -> "Debe incluir al menos una letra"
        valor.none { it.isDigit() } -> "Debe incluir al menos un número"
        else -> null
    }

    fun confirmacion(password: String, confirmacion: String): String? = when {
        confirmacion.isEmpty() -> "Repite la contraseña"
        password != confirmacion -> "Las contraseñas no coinciden"
        else -> null
    }

    fun tituloTicket(valor: String): String? = when {
        valor.isBlank() -> "Ingresa un título"
        valor.trim().length < 5 -> "Debe tener al menos 5 caracteres"
        valor.trim().length > 60 -> "Máximo 60 caracteres"
        else -> null
    }

    fun ubicacion(valor: String): String? = when {
        valor.isBlank() -> "Indica dónde está el problema"
        valor.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        valor.trim().length > 80 -> "Máximo 80 caracteres"
        else -> null
    }

    const val MAX_DESCRIPCION = 500

    fun descripcion(valor: String): String? = when {
        valor.isBlank() -> "Describe el problema"
        valor.trim().length < 10 -> "Cuéntanos un poco más (mínimo 10 caracteres)"
        valor.trim().length > MAX_DESCRIPCION -> "Máximo $MAX_DESCRIPCION caracteres"
        else -> null
    }

    fun comentario(valor: String): String? =
        if (valor.isBlank()) "Escribe un comentario" else null
}
