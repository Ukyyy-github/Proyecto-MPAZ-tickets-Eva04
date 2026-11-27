package cl.mpaz.ticket.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Resultado de copiar un archivo elegido por el usuario al almacenamiento privado de la app. */
data class ArchivoCopiado(
    val nombre: String,
    val tipoMime: String,
    val ruta: String,
    val tamano: Long
)

/** Guarda los documentos adjuntos (cualquier tipo) en la carpeta privada de la app. */
class AdjuntoStorage(context: Context) {
    private val appContext = context.applicationContext
    private val carpeta = File(appContext.filesDir, "adjuntos").apply { mkdirs() }

    suspend fun copiar(uri: Uri): ArchivoCopiado = withContext(Dispatchers.IO) {
        val resolver = appContext.contentResolver

        var nombre = "archivo"
        var tamanoDeclarado = -1L
        resolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnaNombre = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (columnaNombre >= 0) cursor.getString(columnaNombre)?.let { nombre = it }
                val columnaTamano = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (columnaTamano >= 0 && !cursor.isNull(columnaTamano)) {
                    tamanoDeclarado = cursor.getLong(columnaTamano)
                }
            }
        }
        require(tamanoDeclarado <= MAX_BYTES) { "\"$nombre\" supera los 10 MB" }

        val tipo = resolver.getType(uri) ?: tipoPorExtension(nombre)
        val destino = File(carpeta, "${System.currentTimeMillis()}_${limpiarNombre(nombre)}")
        resolver.openInputStream(uri).use { entrada ->
            requireNotNull(entrada) { "No se pudo leer \"$nombre\"" }
            destino.outputStream().use { salida -> entrada.copyTo(salida) }
        }
        if (destino.length() > MAX_BYTES) {
            destino.delete()
            throw IllegalArgumentException("\"$nombre\" supera los 10 MB")
        }
        ArchivoCopiado(nombre, tipo, destino.absolutePath, destino.length())
    }

    fun eliminar(ruta: String) {
        runCatching { File(ruta).delete() }
    }

    /** Uri compartible (FileProvider) para abrir el archivo con otra aplicación. */
    fun uriParaAbrir(ruta: String): Uri =
        FileProvider.getUriForFile(appContext, "${appContext.packageName}.fileprovider", File(ruta))

    private fun tipoPorExtension(nombre: String): String {
        val extension = nombre.substringAfterLast('.', "").lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"
    }

    private fun limpiarNombre(nombre: String): String =
        nombre.replace(Regex("[^A-Za-z0-9._-]"), "_")

    companion object {
        const val MAX_BYTES = 10L * 1024 * 1024
    }
}
