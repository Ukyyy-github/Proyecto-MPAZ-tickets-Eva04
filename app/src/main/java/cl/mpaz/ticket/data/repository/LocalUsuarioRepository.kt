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

    override suspend fun listarAsignables(): List<Usuario> =
        dao.listarPorRol(Rol.ENCARGADO_TI) + dao.listarPorRol(Rol.AUXILIAR)

    override suspend fun actualizarNombre(id: Long, nombre: String): Result<Usuario> {
        val usuario = dao.buscarPorId(id)
            ?: return Result.failure(IllegalStateException("Usuario no encontrado"))
        val limpio = nombre.trim()
        val actualizado = usuario.copy(nombre = limpio)
        dao.actualizar(actualizado)
        dao.renombrarEnSolicitudes(id, limpio)
        dao.renombrarEnAsignaciones(id, limpio)
        dao.renombrarEnComentarios(id, limpio)
        dao.renombrarEnAdjuntos(id, limpio)
        return Result.success(actualizado)
    }

    override suspend fun cambiarPassword(id: Long, actual: String, nueva: String): Result<Unit> {
        val usuario = dao.buscarPorId(id)
            ?: return Result.failure(IllegalStateException("Usuario no encontrado"))
        if (usuario.passwordHash != Seguridad.hash(actual)) {
            return Result.failure(IllegalArgumentException("La contraseña actual no es correcta"))
        }
        dao.actualizar(usuario.copy(passwordHash = Seguridad.hash(nueva)))
        return Result.success(Unit)
    }
}
