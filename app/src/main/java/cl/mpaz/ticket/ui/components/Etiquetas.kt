package cl.mpaz.ticket.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cl.mpaz.ticket.model.EstadoTicket
import cl.mpaz.ticket.model.Prioridad

@Composable
private fun Etiqueta(texto: String, fondo: Color, contenido: Color, modifier: Modifier) {
    Surface(color = fondo, shape = RoundedCornerShape(50), modifier = modifier) {
        Text(
            text = texto,
            color = contenido,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun EtiquetaEstado(estado: EstadoTicket, modifier: Modifier = Modifier) {
    val (fondo, texto) = when (estado) {
        EstadoTicket.PENDIENTE -> Color(0xFFFFF3CD) to Color(0xFF7A5B00)
        EstadoTicket.REVISADO -> Color(0xFFE1E7F5) to Color(0xFF2F3E66)
        EstadoTicket.ASIGNADO -> Color(0xFFDCEBFF) to Color(0xFF0D47A1)
        EstadoTicket.EN_PROCESO -> Color(0xFFFFE0CC) to Color(0xFF8A3B00)
        EstadoTicket.SOLUCIONADO -> Color(0xFFD7F2DD) to Color(0xFF14602B)
        EstadoTicket.CERRADO -> Color(0xFFE3E3E3) to Color(0xFF424242)
    }
    Etiqueta(estado.etiqueta, fondo, texto, modifier)
}

@Composable
fun EtiquetaPrioridad(prioridad: Prioridad, modifier: Modifier = Modifier) {
    val (fondo, texto) = when (prioridad) {
        Prioridad.BAJA -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        Prioridad.MEDIA -> Color(0xFFFFF8E1) to Color(0xFF8D6E00)
        Prioridad.ALTA -> Color(0xFFFFE8D6) to Color(0xFFB34700)
        Prioridad.URGENTE -> Color(0xFFFDE0E0) to Color(0xFFB00020)
    }
    Etiqueta("Prioridad ${prioridad.etiqueta.lowercase()}", fondo, texto, modifier)
}
