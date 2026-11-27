package cl.mpaz.ticket.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.mpaz.ticket.data.repository.TicketRepository
import cl.mpaz.ticket.data.repository.UsuarioRepository
import cl.mpaz.ticket.model.EstadoTicket
import cl.mpaz.ticket.model.Usuario
import cl.mpaz.ticket.session.SesionActual
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EstadisticasPerfil(
    val creados: Int = 0,
    val asignados: Int = 0,
    val resueltos: Int = 0
)

class PerfilViewModel(
    private val usuarios: UsuarioRepository,
    tickets: TicketRepository,
    private val sesion: SesionActual
) : ViewModel() {

    val usuario: StateFlow<Usuario?> = sesion.usuario

    val estadisticas: StateFlow<EstadisticasPerfil> = combine(
        tickets.observarTodos(), sesion.usuario
    ) { lista, usuario ->
        if (usuario == null) {
            EstadisticasPerfil()
        } else {
            val asignados = lista.filter { it.asignadoId == usuario.id }
            EstadisticasPerfil(
                creados = lista.count { it.solicitanteId == usuario.id },
                asignados = asignados.size,
                resueltos = asignados.count {
                    it.estado == EstadoTicket.SOLUCIONADO || it.estado == EstadoTicket.CERRADO
                }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadisticasPerfil())

    private val _aviso = MutableStateFlow<String?>(null)
    /** Mensaje para mostrar al usuario (éxito o error). */
    val aviso: StateFlow<String?> = _aviso.asStateFlow()

    fun guardarNombre(nombre: String) {
        val actual = sesion.usuario.value ?: return
        viewModelScope.launch {
            usuarios.actualizarNombre(actual.id, nombre)
                .onSuccess {
                    sesion.actualizarUsuario(it)
                    _aviso.value = "Nombre actualizado"
                }
                .onFailure { _aviso.value = it.message ?: "No se pudo actualizar el nombre" }
        }
    }

    fun cambiarPassword(actual: String, nueva: String, alExito: () -> Unit) {
        val usuario = sesion.usuario.value ?: return
        viewModelScope.launch {
            usuarios.cambiarPassword(usuario.id, actual, nueva)
                .onSuccess {
                    _aviso.value = "Contraseña actualizada"
                    alExito()
                }
                .onFailure { _aviso.value = it.message ?: "No se pudo cambiar la contraseña" }
        }
    }

    fun consumirAviso() {
        _aviso.value = null
    }

    fun cerrarSesion() = sesion.cerrar()
}
