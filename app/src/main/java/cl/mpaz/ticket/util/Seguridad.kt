package cl.mpaz.ticket.util

import java.security.MessageDigest

object Seguridad {
    /** Hash SHA-256 de la contraseña (nunca se guarda en texto plano). */
    fun hash(texto: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(texto.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}
