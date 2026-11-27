package cl.mpaz.ticket.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.mpaz.ticket.model.Adjunto
import cl.mpaz.ticket.model.Categoria
import cl.mpaz.ticket.model.Comentario
import cl.mpaz.ticket.model.EstadoTicket
import cl.mpaz.ticket.model.Prioridad
import cl.mpaz.ticket.model.Rol
import cl.mpaz.ticket.model.Ticket
import cl.mpaz.ticket.model.Usuario
import cl.mpaz.ticket.util.Seguridad

class Converters {
    @TypeConverter fun fromRol(rol: Rol): String = rol.name
    @TypeConverter fun toRol(value: String): Rol = Rol.valueOf(value)

    @TypeConverter fun fromCategoria(categoria: Categoria): String = categoria.name
    @TypeConverter fun toCategoria(value: String): Categoria = Categoria.valueOf(value)

    @TypeConverter fun fromEstado(estado: EstadoTicket): String = estado.name
    @TypeConverter fun toEstado(value: String): EstadoTicket = EstadoTicket.valueOf(value)

    @TypeConverter fun fromPrioridad(prioridad: Prioridad): String = prioridad.name
    @TypeConverter fun toPrioridad(value: String): Prioridad = Prioridad.valueOf(value)
}

@Database(
    entities = [Usuario::class, Ticket::class, Comentario::class, Adjunto::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun ticketDao(): TicketDao
    abstract fun comentarioDao(): ComentarioDao
    abstract fun adjuntoDao(): AdjuntoDao

    companion object {
        @Volatile private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mpaz_ticket.db"
                )
                    .addCallback(CargaInicial)
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { instancia = it }
            }
    }
}

/** Usuarios de demostración (clave: Mpaz1234). INSERT OR IGNORE evita duplicar si ya existen. */
private fun sembrarUsuariosDemo(db: SupportSQLiteDatabase) {
    val clave = Seguridad.hash("Mpaz1234")
    val demo = listOf(
        Triple("Encargado TI", "ti@mpaz.cl", Rol.ENCARGADO_TI),
        Triple("Auxiliar Mantención", "auxiliar@mpaz.cl", Rol.AUXILIAR),
        Triple("Auxiliar Soporte", "auxiliar2@mpaz.cl", Rol.AUXILIAR),
        Triple("Funcionario Demo", "funcionario@mpaz.cl", Rol.FUNCIONARIO)
    )
    demo.forEach { (nombre, correo, rol) ->
        db.execSQL(
            "INSERT OR IGNORE INTO usuarios (nombre, correo, passwordHash, rol) VALUES (?, ?, ?, ?)",
            arrayOf<Any?>(nombre, correo, clave, rol.name)
        )
    }
}

private object CargaInicial : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        sembrarUsuariosDemo(db)
    }

    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        sembrarUsuariosDemo(db)
    }
}
