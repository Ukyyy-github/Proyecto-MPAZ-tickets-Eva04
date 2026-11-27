package cl.mpaz.ticket.session

import android.content.Context
import cl.mpaz.ticket.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Usuario con sesión iniciada. Guarda solo su id para restaurar la sesión al reabrir la app. */
class SesionActual(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("mpaz_sesion", Context.MODE_PRIVATE)

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario.asStateFlow()

    fun idGuardado(): Long? = prefs.getLong(CLAVE_ID, -1L).takeIf { it > 0 }

    fun iniciar(usuario: Usuario) {
        prefs.edit().putLong(CLAVE_ID, usuario.id).apply()
        _usuario.value = usuario
    }

    /** Refresca el usuario en memoria (por ejemplo, tras editar el perfil). */
    fun actualizarUsuario(usuario: Usuario) {
        _usuario.value = usuario
    }

    fun cerrar() {
        prefs.edit().remove(CLAVE_ID).apply()
        _usuario.value = null
    }

    private companion object {
        const val CLAVE_ID = "usuario_id"
    }
}
