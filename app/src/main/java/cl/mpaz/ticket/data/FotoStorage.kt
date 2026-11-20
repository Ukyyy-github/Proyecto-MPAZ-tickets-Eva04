package cl.mpaz.ticket.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Copia las fotos elegidas por el usuario al almacenamiento privado de la app. */
class FotoStorage(context: Context) {
    private val appContext = context.applicationContext
    private val carpeta = File(appContext.filesDir, "fotos").apply { mkdirs() }

    suspend fun copiar(uri: Uri): String = withContext(Dispatchers.IO) {
        val destino = File(carpeta, "ticket_${System.currentTimeMillis()}.jpg")
        appContext.contentResolver.openInputStream(uri).use { entrada ->
            requireNotNull(entrada) { "No se pudo leer la imagen seleccionada" }
            destino.outputStream().use { salida -> entrada.copyTo(salida) }
        }
        destino.absolutePath
    }

    fun eliminar(ruta: String) {
        runCatching { File(ruta).delete() }
    }
}
