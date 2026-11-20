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
    suspend fun listarAuxiliares(): List<Usuario>
}
