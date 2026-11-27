package cl.mpaz.ticket.ui.tickets

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cl.mpaz.ticket.model.Categoria
import cl.mpaz.ticket.ui.components.CampoFormulario
import cl.mpaz.ticket.ui.components.SelectorDropdown
import cl.mpaz.ticket.util.ReglasTicket
import cl.mpaz.ticket.util.Validadores
import cl.mpaz.ticket.util.formatearTamano

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketFormScreen(
    viewModel: TicketFormViewModel,
    esEdicion: Boolean,
    onVolver: () -> Unit
) {
    var titulo by rememberSaveable { mutableStateOf("") }
    var categoria by rememberSaveable { mutableStateOf(Categoria.TECNOLOGIA) }
    var ubicacion by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var precargado by rememberSaveable { mutableStateOf(!esEdicion) }
    var intento by rememberSaveable { mutableStateOf(false) }

    val existente by viewModel.ticketExistente.collectAsState()
    val pendientes by viewModel.pendientes.collectAsState()
    val guardando by viewModel.guardando.collectAsState()
    val guardado by viewModel.guardado.collectAsState()
    val error by viewModel.error.collectAsState()

    // En edición, rellena el formulario una sola vez cuando carga el ticket.
    LaunchedEffect(existente) {
        val ticket = existente
        if (ticket != null && !precargado) {
            titulo = ticket.titulo
            categoria = ticket.categoria
            ubicacion = ticket.ubicacion
            descripcion = ticket.descripcion
            precargado = true
        }
    }
    LaunchedEffect(guardado) { if (guardado) onVolver() }

    val selectorDocumentos = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris -> viewModel.agregarArchivos(uris) }

    val errorTitulo = if (intento) Validadores.tituloTicket(titulo) else null
    val errorUbicacion = if (intento) Validadores.ubicacion(ubicacion) else null
    val errorDescripcion = if (intento) Validadores.descripcion(descripcion) else null

    fun enviar() {
        intento = true
        val sinErrores = listOf(
            Validadores.tituloTicket(titulo),
            Validadores.ubicacion(ubicacion),
            Validadores.descripcion(descripcion)
        ).all { it == null }
        if (sinErrores) viewModel.guardar(titulo, categoria, ubicacion, descripcion)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (esEdicion) "Editar ticket" else "Nuevo ticket") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!esEdicion) {
                Text(
                    "El ticket quedará como Pendiente. El encargado de TI definirá su prioridad y responsable.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            CampoFormulario(titulo, { titulo = it }, "Título del problema", error = errorTitulo)
            SelectorDropdown(
                label = "Categoría",
                opciones = Categoria.entries,
                seleccionado = categoria,
                etiqueta = { it.etiqueta },
                onSeleccion = { categoria = it }
            )
            CampoFormulario(
                ubicacion, { ubicacion = it }, "Ubicación (sala, piso, edificio)",
                error = errorUbicacion
            )
            CampoFormulario(
                descripcion, { descripcion = it }, "Descripción",
                error = errorDescripcion,
                ayuda = "${descripcion.length}/${Validadores.MAX_DESCRIPCION}",
                singleLine = false,
                minLines = 4
            )

            if (esEdicion) {
                Text(
                    "Los documentos adjuntos se gestionan desde el detalle del ticket.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text("Documentos adjuntos (opcional)", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Fotos, PDF, Word, Excel u otros archivos. Máximo ${ReglasTicket.MAX_ADJUNTOS} de hasta 10 MB cada uno.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                pendientes.forEach { archivo ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                archivo.nombre,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                formatearTamano(archivo.tamano),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = { viewModel.quitarPendiente(archivo) }) { Text("Quitar") }
                    }
                }
                OutlinedButton(
                    onClick = { selectorDocumentos.launch(arrayOf("*/*")) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (pendientes.isEmpty()) "Adjuntar documentos" else "Adjuntar más documentos") }
            }

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Button(
                onClick = ::enviar,
                enabled = !guardando,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                if (guardando) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(if (esEdicion) "Guardar cambios" else "Crear ticket")
                }
            }
        }
    }
}
