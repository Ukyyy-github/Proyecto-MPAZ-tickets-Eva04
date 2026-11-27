package cl.mpaz.ticket.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import cl.mpaz.ticket.model.Adjunto
import kotlinx.coroutines.flow.Flow

@Dao
interface AdjuntoDao {
    @Insert
    suspend fun insertar(adjunto: Adjunto): Long

    @Query("SELECT * FROM adjuntos WHERE ticketId = :ticketId ORDER BY creadoEn ASC, id ASC")
    fun observarPorTicket(ticketId: Long): Flow<List<Adjunto>>

    @Query("SELECT * FROM adjuntos WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): Adjunto?

    @Delete
    suspend fun eliminar(adjunto: Adjunto)

    @Query("DELETE FROM adjuntos WHERE ticketId = :ticketId")
    suspend fun eliminarPorTicket(ticketId: Long)
}
