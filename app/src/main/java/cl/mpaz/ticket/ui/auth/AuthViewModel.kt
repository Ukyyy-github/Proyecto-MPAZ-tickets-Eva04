package cl.mpaz.ticket.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.mpaz.ticket.data.repository.UsuarioRepository
import cl.mpaz.ticket.model.Rol
import cl.mpaz.ticket.model.Usuario
import cl.mpaz.ticket.session.SesionActual
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: UsuarioRepository,
    private val sesion: SesionActual
) : ViewModel() {

    val usuario: StateFlow<Usuario?> = sesion.usuario

    private val _sesionLista = MutableStateFlow(false)
    /** true cuando ya se intentó restaurar la sesión guardada. */
    val sesionLista: StateFlow<Boolean> = _sesionLista.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            sesion.idGuardado()?.let { id -> repository.obtener(id) }?.let { sesion.iniciar(it) }
            _sesionLista.value = true
        }
    }

    fun iniciarSesion(correo: String, password: String) {
        viewModelScope.launch {
            _cargando.value = true
            _error.value = null
            repository.iniciarSesion(correo, password)
                .onSuccess { sesion.iniciar(it) }
                .onFailure { _error.value = it.message ?: "No se pudo iniciar sesión" }
            _cargando.value = false
        }
    }

    fun registrar(nombre: String, correo: String, password: String, rol: Rol) {
        viewModelScope.launch {
            _cargando.value = true
            _error.value = null
            repository.registrar(nombre, correo, password, rol)
                .onSuccess { sesion.iniciar(it) }
                .onFailure { _error.value = it.message ?: "No se pudo crear la cuenta" }
            _cargando.value = false
        }
    }

    fun limpiarError() {
        _error.value = null
    }
}
