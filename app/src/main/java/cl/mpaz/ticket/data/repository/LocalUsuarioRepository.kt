package cl.mpaz.ticket.data.repository

import cl.mpaz.ticket.data.local.UsuarioDao
import cl.mpaz.ticket.model.Rol
import cl.mpaz.ticket.model.Usuario
import cl.mpaz.ticket.util.Seguridad

class LocalUsuarioRepository(private val dao: UsuarioDao) : UsuarioRepository {

    override suspend fun registrar(
        nombre: String,
        correo: String,
        password: String,
        rol: Rol
    ): Result<Usuario> {
        val correoNormalizado = correo.trim().lowercase()
        if (dao.buscarPorCorreo(correoNormalizado) != null) {
            return Result.failure(IllegalStateException("Ya existe una cuenta con ese correo"))
        }
        val nuevo = Usuario(
            nombre = nombre.trim(),
            correo = correoNormalizado,
            passwordHash = Seguridad.hash(password),
            rol = rol
        )
        val id = dao.insertar(nuevo)
        return Result.success(nuevo.copy(id = id))
    }

    override suspend fun iniciarSesion(correo: String, password: String): Result<Usuario> {
        val usuario = dao.buscarPorCorreo(correo.trim().lowercase())
        return if (usuario == null || usuario.passwordHash != Seguridad.hash(password)) {
            Result.failure(IllegalArgumentException("Correo o contraseña incorrectos"))
        } else {
            Result.success(usuario)
        }
    }

    override suspend fun obtener(id: Long): Usuario? = dao.buscarPorId(id)

    override suspend fun listarAuxiliares(): List<Usuario> = dao.listarPorRol(Rol.AUXILIAR)
}
