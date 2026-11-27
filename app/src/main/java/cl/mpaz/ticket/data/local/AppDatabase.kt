package cl.mpaz.ticket.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
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
                    .addMigrations(MIGRACION_1_2)
                    .build()
                    .also { instancia = it }
            }
    }
}

/**
 * Versión 2: agrega la tabla de adjuntos (documentos de cualquier tipo), convierte las fotos
 * existentes en adjuntos, elimina la columna fotoPath de tickets y agrega un auxiliar de demostración.
 */
val MIGRACION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS adjuntos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "ticketId INTEGER NOT NULL, " +
                "nombre TEXT NOT NULL, " +
                "tipoMime TEXT NOT NULL, " +
                "ruta TEXT NOT NULL, " +
                "tamano INTEGER NOT NULL, " +
                "subidoPorId INTEGER NOT NULL, " +
                "subidoPorNombre TEXT NOT NULL, " +
                "creadoEn INTEGER NOT NULL)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_adjuntos_ticketId ON adjuntos (ticketId)")

        // Las fotos que ya existían pasan a ser adjuntos de tipo imagen.
        db.execSQL(
            "INSERT INTO adjuntos (ticketId, nombre, tipoMime, ruta, tamano, subidoPorId, subidoPorNombre, creadoEn) " +
                "SELECT id, 'foto.jpg', 'image/jpeg', fotoPath, 0, solicitanteId, solicitanteNombre, creadoEn " +
                "FROM tickets WHERE fotoPath IS NOT NULL"
        )

        // SQLite no puede quitar una columna en todas las versiones de Android: se recrea la tabla.
        db.execSQL(
            "CREATE TABLE tickets_nueva (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "titulo TEXT NOT NULL, " +
                "categoria TEXT NOT NULL, " +
                "ubicacion TEXT NOT NULL, " +
                "descripcion TEXT NOT NULL, " +
                "prioridad TEXT NOT NULL, " +
                "estado TEXT NOT NULL, " +
                "solicitanteId INTEGER NOT NULL, " +
                "solicitanteNombre TEXT NOT NULL, " +
                "asignadoId INTEGER, " +
                "asignadoNombre TEXT, " +
                "creadoEn INTEGER NOT NULL, " +
                "actualizadoEn INTEGER NOT NULL, " +
                "resueltoEn INTEGER)"
        )
        db.execSQL(
            "INSERT INTO tickets_nueva (id, titulo, categoria, ubicacion, descripcion, prioridad, estado, " +
                "solicitanteId, solicitanteNombre, asignadoId, asignadoNombre, creadoEn, actualizadoEn, resueltoEn) " +
                "SELECT id, titulo, categoria, ubicacion, descripcion, prioridad, estado, " +
                "solicitanteId, solicitanteNombre, asignadoId, asignadoNombre, creadoEn, actualizadoEn, resueltoEn " +
                "FROM tickets"
        )
        db.execSQL("DROP TABLE tickets")
        db.execSQL("ALTER TABLE tickets_nueva RENAME TO tickets")

        sembrarUsuariosDemo(db)
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
}
