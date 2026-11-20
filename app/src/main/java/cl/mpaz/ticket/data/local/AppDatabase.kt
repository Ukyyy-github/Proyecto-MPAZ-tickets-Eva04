package cl.mpaz.ticket.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.mpaz.ticket.model.Comentario
import cl.mpaz.ticket.model.Rol
import cl.mpaz.ticket.model.Ticket
import cl.mpaz.ticket.model.Usuario
import cl.mpaz.ticket.util.Seguridad

@Database(
    entities = [Usuario::class, Ticket::class, Comentario::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun ticketDao(): TicketDao
    abstract fun comentarioDao(): ComentarioDao

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
                    .build()
                    .also { instancia = it }
            }
    }
}

/** Usuarios de demostración, creados una sola vez al crear la base de datos (clave: Mpaz1234). */
private object CargaInicial : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        val clave = Seguridad.hash("Mpaz1234")
        val demo = listOf(
            Triple("Encargado TI", "ti@mpaz.cl", Rol.ENCARGADO_TI),
            Triple("Auxiliar Mantención", "auxiliar@mpaz.cl", Rol.AUXILIAR),
            Triple("Funcionario Demo", "funcionario@mpaz.cl", Rol.FUNCIONARIO)
        )
        demo.forEach { (nombre, correo, rol) ->
            db.execSQL(
                "INSERT INTO usuarios (nombre, correo, passwordHash, rol) VALUES (?, ?, ?, ?)",
                arrayOf<Any?>(nombre, correo, clave, rol.name)
            )
        }
    }
}
