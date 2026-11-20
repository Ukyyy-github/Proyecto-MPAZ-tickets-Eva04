package cl.mpaz.ticket.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cl.mpaz.ticket.model.Comentario
import kotlinx.coroutines.flow.Flow

@Dao
interface ComentarioDao {
    @Insert
    suspend fun insertar(comentario: Comentario): Long

    @Query("SELECT * FROM comentarios WHERE ticketId = :ticketId ORDER BY creadoEn ASC, id ASC")
    fun observarPorTicket(ticketId: Long): Flow<List<Comentario>>

    @Query("DELETE FROM comentarios WHERE ticketId = :ticketId")
    suspend fun eliminarPorTicket(ticketId: Long)
}
