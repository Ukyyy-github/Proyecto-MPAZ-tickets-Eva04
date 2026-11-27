package cl.mpaz.ticket.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.mpaz.ticket.model.Rol
import cl.mpaz.ticket.model.Usuario

@Dao
interface UsuarioDao {
    @Insert
    suspend fun insertar(usuario: Usuario): Long

    @Update
    suspend fun actualizar(usuario: Usuario)

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): Usuario?

    @Query("SELECT * FROM usuarios WHERE rol = :rol ORDER BY nombre")
    suspend fun listarPorRol(rol: Rol): List<Usuario>

    // Al cambiar el nombre de un usuario se actualizan las copias del nombre guardadas en otras tablas.
    @Query("UPDATE tickets SET solicitanteNombre = :nombre WHERE solicitanteId = :usuarioId")
    suspend fun renombrarEnSolicitudes(usuarioId: Long, nombre: String)

    @Query("UPDATE tickets SET asignadoNombre = :nombre WHERE asignadoId = :usuarioId")
    suspend fun renombrarEnAsignaciones(usuarioId: Long, nombre: String)

    @Query("UPDATE comentarios SET autorNombre = :nombre WHERE autorId = :usuarioId")
    suspend fun renombrarEnComentarios(usuarioId: Long, nombre: String)

    @Query("UPDATE adjuntos SET subidoPorNombre = :nombre WHERE subidoPorId = :usuarioId")
    suspend fun renombrarEnAdjuntos(usuarioId: Long, nombre: String)
}
