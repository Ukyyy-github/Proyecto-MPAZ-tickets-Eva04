package cl.mpaz.ticket.ui.tickets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.mpaz.ticket.model.Prioridad
import cl.mpaz.ticket.model.Ticket
import cl.mpaz.ticket.ui.components.CampoFormulario
import cl.mpaz.ticket.ui.components.EtiquetaEstado
import cl.mpaz.ticket.ui.components.ImagenTicket
import cl.mpaz.ticket.ui.components.SelectorDropdown
import cl.mpaz.ticket.util.ReglasTicket
import cl.mpaz.ticket.util.Validadores
import cl.mpaz.ticket.util.formatearFecha
import cl.mpaz.ticket.util.numeroTicket

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
    val auxiliares by viewModel.auxiliares.collectAsState()
    val error by viewModel.error.collectAsState()

    var confirmarEliminar by remember { mutableStateOf(false) }
    var comentario by rememberSaveable { mutableStateOf("") }
    var intentoComentario by rememberSaveable { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let {
            snackbar.showSnackbar(it)
            viewModel.limpiarError()
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

            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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

                // Descripción y foto
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Descripción", style = MaterialTheme.typography.titleSmall)
                        Text(t.descripcion, style = MaterialTheme.typography.bodyMedium)
                        t.fotoPath?.let { ruta ->
                            ImagenTicket(
                                fuente = ruta,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
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
                                    opciones = auxiliares,
                                    seleccionado = auxiliares.firstOrNull { it.id == t.asignadoId },
                                    etiqueta = { it.nombre },
                                    onSeleccion = { viewModel.asignar(it) },
                                    placeholder = if (auxiliares.isEmpty()) "No hay personal auxiliar" else "Sin asignar"
                                )
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
            text = { Text("Se borrará el ticket y todo su historial. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarEliminar = false
                    viewModel.eliminar(onVolver)
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { confirmarEliminar = false }) { Text("Cancelar") } }
        )
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
