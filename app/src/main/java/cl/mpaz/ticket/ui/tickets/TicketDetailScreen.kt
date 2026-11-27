package cl.mpaz.ticket.ui.tickets

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cl.mpaz.ticket.model.Adjunto
import cl.mpaz.ticket.model.Prioridad
import cl.mpaz.ticket.ui.components.CampoFormulario
import cl.mpaz.ticket.ui.components.EtiquetaEstado
import cl.mpaz.ticket.ui.components.ImagenTicket
import cl.mpaz.ticket.ui.components.SelectorDropdown
import cl.mpaz.ticket.util.ReglasTicket
import cl.mpaz.ticket.util.Validadores
import cl.mpaz.ticket.util.formatearFecha
import cl.mpaz.ticket.util.formatearTamano
import cl.mpaz.ticket.util.numeroTicket
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(
    viewModel: TicketDetailViewModel,
    onVolver: () -> Unit,
    onEditar: (Long) -> Unit
) {
    val usuario by viewModel.usuario.collectAsState()
    val ticket by viewModel.ticket.collectAsState()
    val historial by viewModel.historial.collectAsState()
    val adjuntos by viewModel.adjuntos.collectAsState()
    val asignables by viewModel.asignables.collectAsState()
    val error by viewModel.error.collectAsState()

    var confirmarEliminar by remember { mutableStateOf(false) }
    var adjuntoAQuitar by remember { mutableStateOf<Adjunto?>(null) }
    var comentario by rememberSaveable { mutableStateOf("") }
    var intentoComentario by rememberSaveable { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(error) {
        error?.let {
            snackbar.showSnackbar(it)
            viewModel.limpiarError()
        }
    }

    val selectorDocumentos = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris -> viewModel.adjuntarArchivos(uris) }

    fun abrirAdjunto(adjunto: Adjunto) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(viewModel.uriDe(adjunto), adjunto.tipoMime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            scope.launch { snackbar.showSnackbar("No hay una aplicación instalada para abrir este archivo") }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(ticket?.let { numeroTicket(it.id) } ?: "Ticket") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    val t = ticket
                    val u = usuario
                    if (t != null && u != null) {
                        if (ReglasTicket.puedeEditar(u, t)) {
                            IconButton(onClick = { onEditar(t.id) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar ticket")
                            }
                        }
                        if (ReglasTicket.puedeEliminar(u, t)) {
                            IconButton(onClick = { confirmarEliminar = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar ticket")
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        val t = ticket
        val u = usuario
        if (t == null || u == null) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val estadosPermitidos = ReglasTicket.estadosPermitidos(u, t)
            val puedePriorizar = ReglasTicket.puedePriorizar(u)
            val puedeAsignar = ReglasTicket.puedeAsignar(u)
            val soloLectura = ReglasTicket.esSoloLectura(u, t)

            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (soloLectura) {
                    Text(
                        "Estás viendo este ticket en modo lectura: solo participan quien lo reportó, quien lo tiene asignado y el encargado de TI.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Encabezado
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                numeroTicket(t.id),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            EtiquetaEstado(t.estado)
                        }
                        Text(t.titulo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }

                // Información
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilaDato("Categoría", t.categoria.etiqueta)
                        FilaDato("Ubicación", t.ubicacion)
                        FilaDato("Solicitante", t.solicitanteNombre)
                        FilaDato("Prioridad", t.prioridad.etiqueta)
                        FilaDato("Asignado a", t.asignadoNombre ?: "Sin asignar")
                        FilaDato("Creado", formatearFecha(t.creadoEn))
                        FilaDato("Actualizado", formatearFecha(t.actualizadoEn))
                        t.resueltoEn?.let { FilaDato("Resuelto", formatearFecha(it)) }
                    }
                }

                // Descripción
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Descripción", style = MaterialTheme.typography.titleSmall)
                        Text(t.descripcion, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                // Documentos adjuntos
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Documentos adjuntos (${adjuntos.size})",
                            style = MaterialTheme.typography.titleSmall
                        )
                        if (adjuntos.isEmpty()) {
                            Text(
                                "Este ticket no tiene documentos.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        adjuntos.forEach { adjunto ->
                            FilaAdjunto(
                                adjunto = adjunto,
                                onAbrir = { abrirAdjunto(adjunto) },
                                onQuitar = if (ReglasTicket.puedeEliminarAdjunto(u, adjunto)) {
                                    { adjuntoAQuitar = adjunto }
                                } else null
                            )
                        }
                        if (ReglasTicket.puedeAdjuntar(u, t)) {
                            OutlinedButton(
                                onClick = { selectorDocumentos.launch(arrayOf("*/*")) },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Adjuntar documentos") }
                        }
                    }
                }

                // Gestión según rol
                if (estadosPermitidos.isNotEmpty() || puedePriorizar || puedeAsignar) {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Gestión del ticket", style = MaterialTheme.typography.titleSmall)
                            if (estadosPermitidos.isNotEmpty()) {
                                SelectorDropdown(
                                    label = "Cambiar estado a",
                                    opciones = estadosPermitidos,
                                    seleccionado = null,
                                    etiqueta = { it.etiqueta },
                                    onSeleccion = { viewModel.cambiarEstado(it) }
                                )
                            }
                            if (puedePriorizar) {
                                SelectorDropdown(
                                    label = "Prioridad",
                                    opciones = Prioridad.entries,
                                    seleccionado = t.prioridad,
                                    etiqueta = { it.etiqueta },
                                    onSeleccion = { viewModel.cambiarPrioridad(it) }
                                )
                            }
                            if (puedeAsignar) {
                                SelectorDropdown(
                                    label = "Asignar a",
                                    opciones = asignables,
                                    seleccionado = asignables.firstOrNull { it.id == t.asignadoId },
                                    etiqueta = { "${it.nombre} · ${it.rol.etiqueta}" },
                                    onSeleccion = { viewModel.asignar(it) },
                                    placeholder = if (asignables.isEmpty()) "No hay usuarios disponibles" else "Sin asignar"
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (ReglasTicket.puedeAutoAsignarse(u, t)) {
                                        OutlinedButton(onClick = { viewModel.asignarmeAMi() }) {
                                            Text("Asignarme a mí")
                                        }
                                    }
                                    if (t.asignadoId != null) {
                                        TextButton(onClick = { viewModel.quitarAsignacion() }) {
                                            Text("Quitar asignación")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Historial y comentarios
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Historial y comentarios (${historial.size})",
                            style = MaterialTheme.typography.titleSmall
                        )
                        historial.forEachIndexed { indice, entrada ->
                            if (indice > 0) HorizontalDivider()
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    "${entrada.autorNombre} · ${formatearFecha(entrada.creadoEn)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    entrada.texto,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontStyle = if (entrada.esCambioAutomatico) FontStyle.Italic else FontStyle.Normal,
                                    color = if (entrada.esCambioAutomatico) {
                                        MaterialTheme.colorScheme.secondary
                                    } else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        if (ReglasTicket.puedeComentar(u, t)) {
                            HorizontalDivider()
                            CampoFormulario(
                                valor = comentario,
                                onCambio = { comentario = it },
                                etiqueta = "Agregar comentario",
                                error = if (intentoComentario) Validadores.comentario(comentario) else null,
                                singleLine = false,
                                minLines = 2
                            )
                            Button(
                                onClick = {
                                    intentoComentario = true
                                    if (Validadores.comentario(comentario) == null) {
                                        viewModel.comentar(comentario)
                                        comentario = ""
                                        intentoComentario = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Enviar comentario") }
                        }
                    }
                }
            }
        }
    }

    if (confirmarEliminar) {
        AlertDialog(
            onDismissRequest = { confirmarEliminar = false },
            title = { Text("Eliminar ticket") },
            text = { Text("Se borrará el ticket con su historial y sus documentos. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarEliminar = false
                    viewModel.eliminar(onVolver)
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { confirmarEliminar = false }) { Text("Cancelar") } }
        )
    }

    adjuntoAQuitar?.let { adjunto ->
        AlertDialog(
            onDismissRequest = { adjuntoAQuitar = null },
            title = { Text("Quitar documento") },
            text = { Text("¿Quitar \"${adjunto.nombre}\" de este ticket?") },
            confirmButton = {
                TextButton(onClick = {
                    adjuntoAQuitar = null
                    viewModel.quitarAdjunto(adjunto)
                }) { Text("Quitar") }
            },
            dismissButton = { TextButton(onClick = { adjuntoAQuitar = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun FilaAdjunto(adjunto: Adjunto, onAbrir: () -> Unit, onQuitar: (() -> Unit)?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAbrir)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (adjunto.tipoMime.startsWith("image/")) {
            ImagenTicket(
                fuente = adjunto.ruta,
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)),
                anchoMaximo = 200
            )
        } else {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    adjunto.nombre.substringAfterLast('.', "").uppercase().take(4).ifEmpty { "ARCH" },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Column(Modifier.weight(1f)) {
            Text(
                adjunto.nombre,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${formatearTamano(adjunto.tamano)} · ${adjunto.subidoPorNombre}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (onQuitar != null) {
            TextButton(onClick = onQuitar) { Text("Quitar") }
        }
    }
}

@Composable
private fun FilaDato(etiqueta: String, valor: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
