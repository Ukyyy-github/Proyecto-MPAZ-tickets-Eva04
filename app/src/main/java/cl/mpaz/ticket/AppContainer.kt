package cl.mpaz.ticket

import android.app.Application
import android.content.Context
import cl.mpaz.ticket.data.FotoStorage
import cl.mpaz.ticket.data.local.AppDatabase
import cl.mpaz.ticket.data.repository.LocalTicketRepository
import cl.mpaz.ticket.data.repository.LocalUsuarioRepository
import cl.mpaz.ticket.data.repository.TicketRepository
import cl.mpaz.ticket.data.repository.UsuarioRepository
import cl.mpaz.ticket.session.SesionActual

/** Inyección de dependencias manual: un único lugar donde se eligen las implementaciones. */
class AppContainer(context: Context) {
    private val db = AppDatabase.obtener(context)

    val sesion = SesionActual(context)
    val fotos = FotoStorage(context)
    val usuarioRepository: UsuarioRepository = LocalUsuarioRepository(db.usuarioDao())
    val ticketRepository: TicketRepository =
        LocalTicketRepository(db.ticketDao(), db.comentarioDao())
}

class MpazApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
