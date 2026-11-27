package cl.mpaz.ticket.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun numeroTicket(id: Long): String = "MPZ-%04d".format(id)

fun formatearFecha(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL")).format(Date(millis))

fun formatearTamano(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "%.0f KB".format(bytes / 1024.0)
    else -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
}
