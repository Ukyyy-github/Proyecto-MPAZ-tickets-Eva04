package cl.mpaz.ticket.ui.tickets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cl.mpaz.ticket.model.EstadoTicket
import cl.mpaz.ticket.model.Rol
import cl.mpaz.ticket.model.Ticket
import cl.mpaz.ticket.ui.components.EtiquetaEstado
import cl.mpaz.ticket.ui.components.EtiquetaPrioridad
import cl.mpaz.ticket.util.ReglasTicket
import cl.mpaz.ticket.util.formatearFecha
import cl.mpaz.ticket.util.numeroTicket

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketListScreen(
    viewModel: TicketListViewModel,
    onAbrirTicket: (Long) -> Unit,
    onCrearTicket: () -> Unit
) {
    val usuario by viewModel.usuario.collectAsState()
    val tickets by viewModel.tickets.collectAsState()
    val filtro by viewModel.filtro.collectAsState()
    val puedeCrear = usuario?.let { ReglasTicket.puedeCrear(it) } == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MPAZ Ticket") },
                actions = { TextButton(onClick = { viewModel.cerrarSesion() }) { Text("Salir") } }
            )
        },
        floatingActionButton = {
            if (puedeCrear) {
                ExtendedFloatingActionButton(
                    onClick = onCrearTicket,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Nuevo ticket") }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            usuario?.let { u ->
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        "Hola, ${u.nombre}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    val descripcion = when (u.rol) {
                        Rol.ENCARGADO_TI -> "Bandeja central: todos los tickets"
                        Rol.FUNCIONARIO -> "Tus solicitudes de mantención"
                        Rol.AUXILIAR -> "Tickets asignados a ti"
                    }
                    Text(
                        "${u.rol.etiqueta} · $descripcion",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = filtro == null,
                        onClick = { viewModel.seleccionarFiltro(null) },
                        label = { Text("Todos") }
                    )
                }
                items(EstadoTicket.entries) { estado ->
                    FilterChip(
                        selected = filtro == estado,
                        onClick = { viewModel.seleccionarFiltro(estado) },
                        label = { Text(estado.etiqueta) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            if (tickets.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        if (filtro == null) "Aún no hay tickets para mostrar" else "No hay tickets con ese estado",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tickets, key = { it.id }) { ticket ->
                        TicketCard(ticket = ticket, onClick = { onAbrirTicket(ticket.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketCard(ticket: Ticket, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    numeroTicket(ticket.id),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                EtiquetaEstado(ticket.estado)
            }
            Text(
                ticket.titulo,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${ticket.categoria.etiqueta} · ${ticket.ubicacion}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EtiquetaPrioridad(ticket.prioridad)
                Text(
                    formatearFecha(ticket.actualizadoEn),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
