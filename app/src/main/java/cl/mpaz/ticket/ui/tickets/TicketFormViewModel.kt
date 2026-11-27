package cl.mpaz.ticket.ui.tickets

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.mpaz.ticket.data.AdjuntoStorage
import cl.mpaz.ticket.data.ArchivoCopiado
import cl.mpaz.ticket.data.repository.TicketRepository
import cl.mpaz.ticket.model.Categoria
import cl.mpaz.ticket.model.Ticket
import cl.mpaz.ticket.session.SesionActual
import cl.mpaz.ticket.util.ReglasTicket
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
    private val almacen: AdjuntoStorage,
    private val ticketId: Long?
) : ViewModel() {

    val ticketExistente: StateFlow<Ticket?> =
        if (ticketId == null) {
            MutableStateFlow<Ticket?>(null)
        } else {
            repository.observar(ticketId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
        }

    /** Documentos elegidos antes de guardar (solo al crear). Ya están copiados a la app. */
    private val _pendientes = MutableStateFlow<List<ArchivoCopiado>>(emptyList())
    val pendientes: StateFlow<List<ArchivoCopiado>> = _pendientes.asStateFlow()

    private val _guardando = MutableStateFlow(false)
    val guardando: StateFlow<Boolean> = _guardando.asStateFlow()

    private val _guardado = MutableStateFlow(false)
    val guardado: StateFlow<Boolean> = _guardado.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun agregarArchivos(uris: List<Uri>) {
        viewModelScope.launch {
            _error.value = null
            for (uri in uris) {
                if (_pendientes.value.size >= ReglasTicket.MAX_ADJUNTOS) {
                    _error.value = "Máximo ${ReglasTicket.MAX_ADJUNTOS} documentos por ticket"
                    break
                }
                try {
                    _pendientes.value = _pendientes.value + almacen.copiar(uri)
                } catch (e: Exception) {
                    _error.value = e.message ?: "No se pudo adjuntar el documento"
                }
            }
        }
    }

    fun quitarPendiente(archivo: ArchivoCopiado) {
        almacen.eliminar(archivo.ruta)
        _pendientes.value = _pendientes.value - archivo
    }

    fun guardar(titulo: String, categoria: Categoria, ubicacion: String, descripcion: String) {
        val autor = sesion.usuario.value ?: return
        viewModelScope.launch {
            _guardando.value = true
            _error.value = null
            try {
                if (ticketId == null) {
                    val id = repository.crear(titulo, categoria, ubicacion, descripcion, autor)
                    _pendientes.value.forEach {
                        repository.agregarAdjunto(id, it.nombre, it.tipoMime, it.ruta, it.tamano, autor)
                    }
                } else {
                    repository.editar(ticketId, titulo, categoria, ubicacion, descripcion, autor)
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

    override fun onCleared() {
        // Si se sale sin guardar, se borran los archivos copiados que quedaron huérfanos.
        if (!_guardado.value) _pendientes.value.forEach { almacen.eliminar(it.ruta) }
    }
}
