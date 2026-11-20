package cl.mpaz.ticket.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun numeroTicket(id: Long): String = "MPZ-%04d".format(id)

fun formatearFecha(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL")).format(Date(millis))
