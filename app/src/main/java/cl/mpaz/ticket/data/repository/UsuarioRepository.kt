package cl.mpaz.ticket.data.repository

import cl.mpaz.ticket.model.Rol
import cl.mpaz.ticket.model.Usuario

/**
 * Contrato de acceso a usuarios. Hoy lo implementa Room ([LocalUsuarioRepository]);
 * la implementación remota (Retrofit + microservicio de usuarios) se enchufa aquí
 * sin tocar ViewModels ni pantallas.
 */
interface UsuarioRepository {
    suspend fun registrar(nombre: String, correo: String, password: String, rol: Rol): Result<Usuario>
    suspend fun iniciarSesion(correo: String, password: String): Result<Usuario>
    suspend fun obtener(id: Long): Usuario?

    /** Usuarios a los que se puede asignar un ticket: encargados de TI y personal auxiliar. */
    suspend fun listarAsignables(): List<Usuario>

    suspend fun actualizarNombre(id: Long, nombre: String): Result<Usuario>
    suspend fun cambiarPassword(id: Long, actual: String, nueva: String): Result<Unit>
}
