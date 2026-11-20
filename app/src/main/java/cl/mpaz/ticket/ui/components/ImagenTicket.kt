package cl.mpaz.ticket.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

/** Muestra una imagen desde una ruta de archivo ([String]) o una [Uri] de la galería. */
@Composable
fun ImagenTicket(fuente: Any?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val resultado by produceState<Pair<Boolean, ImageBitmap?>>(initialValue = false to null, fuente) {
        value = true to withContext(Dispatchers.IO) { cargarBitmap(context, fuente) }
    }
    val (terminado, bitmap) = resultado

    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = "Foto del problema",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Box(modifier, contentAlignment = Alignment.Center) {
            if (!terminado) CircularProgressIndicator() else Text("No se pudo cargar la foto")
        }
    }
}

private fun cargarBitmap(context: Context, fuente: Any?): ImageBitmap? = runCatching {
    fun abrir(): InputStream? = when (fuente) {
        is Uri -> context.contentResolver.openInputStream(fuente)
        is String -> File(fuente).inputStream()
        else -> null
    }

    val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    abrir()?.use { BitmapFactory.decodeStream(it, null, limites) }
    var muestra = 1
    while (limites.outWidth / muestra > 1600) muestra *= 2

    val opciones = BitmapFactory.Options().apply { inSampleSize = muestra }
    abrir()?.use { BitmapFactory.decodeStream(it, null, opciones) }?.asImageBitmap()
}.getOrNull()
