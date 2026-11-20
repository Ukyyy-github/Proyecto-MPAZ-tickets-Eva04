package cl.mpaz.ticket.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tickets")
data class Ticket(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titulo: String,
    val categoria: Categoria,
    val ubicacion: String,
    val descripcion: String,
    val fotoPath: String? = null,
    val prioridad: Prioridad = Prioridad.MEDIA,
    val estado: EstadoTicket = EstadoTicket.PENDIENTE,
    val solicitanteId: Long,
    val solicitanteNombre: String,
    val asignadoId: Long? = null,
    val asignadoNombre: String? = null,
    val creadoEn: Long,
    val actualizadoEn: Long,
    val resueltoEn: Long? = null
)
