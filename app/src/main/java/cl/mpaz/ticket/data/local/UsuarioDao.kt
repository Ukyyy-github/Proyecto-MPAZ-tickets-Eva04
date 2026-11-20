package cl.mpaz.ticket.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cl.mpaz.ticket.model.Rol
import cl.mpaz.ticket.model.Usuario

@Dao
interface UsuarioDao {
    @Insert
    suspend fun insertar(usuario: Usuario): Long

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): Usuario?

    @Query("SELECT * FROM usuarios WHERE rol = :rol ORDER BY nombre")
    suspend fun listarPorRol(rol: Rol): List<Usuario>
}
