package cl.mpaz.ticket.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Entrada de la bitácora de un ticket: comentarios y cambios automáticos (trazabilidad). */
@Entity(tableName = "comentarios", indices = [Index("ticketId")])
data class Comentario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ticketId: Long,
    val autorId: Long,
    val autorNombre: String,
    val texto: String,
    val esCambioAutomatico: Boolean = false,
    val creadoEn: Long
)
