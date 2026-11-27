package cl.mpaz.ticket.ui.tickets

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.mpaz.ticket.data.AdjuntoStorage
import cl.mpaz.ticket.data.repository.TicketRepository
import cl.mpaz.ticket.data.repository.UsuarioRepository
import cl.mpaz.ticket.model.Adjunto
import cl.mpaz.ticket.model.Comentario
import cl.mpaz.ticket.model.EstadoTicket
import cl.mpaz.ticket.model.Prioridad
import cl.mpaz.ticket.model.Ticket
import cl.mpaz.ticket.model.Usuario
import cl.mpaz.ticket.session.SesionActual
import cl.mpaz.ticket.util.ReglasTicket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TicketDetailViewModel(
    private val repository: TicketRepository,
    usuarios: UsuarioRepository,
    private val sesion: SesionActual,
    private val almacen: AdjuntoStorage,
    private val ticketId: Long
) : ViewModel() {

    val usuario: StateFlow<Usuario?> = sesion.usuario

    val ticket: StateFlow<Ticket?> = repository.observar(ticketId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val historial: StateFlow<List<Comentario>> = repository.observarHistorial(ticketId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val adjuntos: StateFlow<List<Adjunto>> = repository.observarAdjuntos(ticketId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _asignables = MutableStateFlow<List<Usuario>>(emptyList())
    /** Encargados de TI y personal auxiliar: a quienes se puede asignar el ticket. */
    val asignables: StateFlow<List<Usuario>> = _asignables.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch { _asignables.value = usuarios.listarAsignables() }
    }

    fun cambiarEstado(estado: EstadoTicket) =
        ejecutar { autor -> repository.cambiarEstado(ticketId, estado, autor) }

    fun cambiarPrioridad(prioridad: Prioridad) =
        ejecutar { autor -> repository.cambiarPrioridad(ticketId, prioridad, autor) }

    fun asignar(asignado: Usuario) =
        ejecutar { autor -> repository.asignar(ticketId, asignado, autor) }

    fun asignarmeAMi() {
        val yo = sesion.usuario.value ?: return
        asignar(yo)
    }

    fun quitarAsignacion() =
        ejecutar { autor -> repository.quitarAsignacion(ticketId, autor) }

    fun comentar(texto: String) =
        ejecutar { autor -> repository.comentar(ticketId, autor, texto) }

    fun adjuntarArchivos(uris: List<Uri>) {
        if (uris.isEmpty()) return
        if (adjuntos.value.size + uris.size > ReglasTicket.MAX_ADJUNTOS) {
            _error.value = "Máximo ${ReglasTicket.MAX_ADJUNTOS} documentos por ticket"
            return
        }
        ejecutar { autor ->
            uris.forEach { uri ->
                val archivo = almacen.copiar(uri)
                repository.agregarAdjunto(
                    ticketId, archivo.nombre, archivo.tipoMime, archivo.ruta, archivo.tamano, autor
                )
            }
        }
    }

    fun quitarAdjunto(adjunto: Adjunto) =
        ejecutar { autor ->
            repository.eliminarAdjunto(adjunto.id, autor)
            almacen.eliminar(adjunto.ruta)
        }

    /** Uri compartible para abrir el documento con otra aplicación. */
    fun uriDe(adjunto: Adjunto): Uri = almacen.uriParaAbrir(adjunto.ruta)

    fun eliminar(alTerminar: () -> Unit) {
        val rutas = adjuntos.value.map { it.ruta }
        ejecutar {
            repository.eliminar(ticketId)
            rutas.forEach(almacen::eliminar)
            alTerminar()
        }
    }

    fun limpiarError() {
        _error.value = null
    }

    private fun ejecutar(bloque: suspend (Usuario) -> Unit) {
        val autor = sesion.usuario.value ?: return
        viewModelScope.launch {
            try {
                bloque(autor)
            } catch (e: Exception) {
                _error.value = e.message ?: "No se pudo completar la acción"
            }
        }
    }
}
