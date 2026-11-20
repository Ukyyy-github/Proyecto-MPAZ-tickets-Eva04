package cl.mpaz.ticket.ui.tickets

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.mpaz.ticket.data.FotoStorage
import cl.mpaz.ticket.data.repository.TicketRepository
import cl.mpaz.ticket.model.Categoria
import cl.mpaz.ticket.model.Ticket
import cl.mpaz.ticket.session.SesionActual
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Crea un ticket nuevo ([ticketId] = null) o edita uno existente. */
class TicketFormViewModel(
    private val repository: TicketRepository,
    private val sesion: SesionActual,
    private val fotos: FotoStorage,
    private val ticketId: Long?
) : ViewModel() {

    val ticketExistente: StateFlow<Ticket?> =
        if (ticketId == null) {
            MutableStateFlow<Ticket?>(null)
        } else {
            repository.observar(ticketId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
        }

    private val _guardando = MutableStateFlow(false)
    val guardando: StateFlow<Boolean> = _guardando.asStateFlow()

    private val _guardado = MutableStateFlow(false)
    val guardado: StateFlow<Boolean> = _guardado.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun guardar(
        titulo: String,
        categoria: Categoria,
        ubicacion: String,
        descripcion: String,
        fotoNueva: Uri?,
        quitarFoto: Boolean
    ) {
        val autor = sesion.usuario.value ?: return
        viewModelScope.launch {
            _guardando.value = true
            _error.value = null
            try {
                val existente = ticketId?.let { repository.obtener(it) }
                var ruta = existente?.fotoPath
                if (quitarFoto || fotoNueva != null) {
                    ruta?.let(fotos::eliminar)
                    ruta = null
                }
                if (fotoNueva != null) ruta = fotos.copiar(fotoNueva)

                if (ticketId == null) {
                    repository.crear(titulo, categoria, ubicacion, descripcion, ruta, autor)
                } else {
                    repository.editar(ticketId, titulo, categoria, ubicacion, descripcion, ruta, autor)
                }
                _guardado.value = true
            } catch (e: Exception) {
                _error.value = "No se pudo guardar el ticket: ${e.message ?: "error desconocido"}"
            } finally {
                _guardando.value = false
            }
        }
    }

    fun limpiarError() {
        _error.value = null
    }
}
