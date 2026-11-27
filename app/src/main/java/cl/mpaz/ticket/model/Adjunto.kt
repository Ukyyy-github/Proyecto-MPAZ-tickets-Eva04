package cl.mpaz.ticket.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Documento (de cualquier tipo) asociado a un ticket. El archivo vive en el almacenamiento privado de la app. */
@Entity(tableName = "adjuntos", indices = [Index("ticketId")])
data class Adjunto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ticketId: Long,
    val nombre: String,
    val tipoMime: String,
    val ruta: String,
    val tamano: Long,
    val subidoPorId: Long,
    val subidoPorNombre: String,
    val creadoEn: Long
)
