package cl.mpaz.ticket.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.mpaz.ticket.model.Ticket
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {
    @Insert
    suspend fun insertar(ticket: Ticket): Long

    @Update
    suspend fun actualizar(ticket: Ticket)

    @Delete
    suspend fun eliminar(ticket: Ticket)

    @Query("SELECT * FROM tickets WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): Ticket?

    @Query("SELECT * FROM tickets WHERE id = :id LIMIT 1")
    fun observarPorId(id: Long): Flow<Ticket?>

    @Query("SELECT * FROM tickets ORDER BY actualizadoEn DESC")
    fun observarTodos(): Flow<List<Ticket>>

    @Query("SELECT * FROM tickets WHERE solicitanteId = :usuarioId ORDER BY actualizadoEn DESC")
    fun observarPorSolicitante(usuarioId: Long): Flow<List<Ticket>>

    @Query("SELECT * FROM tickets WHERE asignadoId = :usuarioId ORDER BY actualizadoEn DESC")
    fun observarPorAsignado(usuarioId: Long): Flow<List<Ticket>>
}
