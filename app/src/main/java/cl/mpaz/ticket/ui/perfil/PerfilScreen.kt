package cl.mpaz.ticket.ui.perfil

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import cl.mpaz.ticket.ui.components.CampoFormulario
import cl.mpaz.ticket.util.Validadores

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(viewModel: PerfilViewModel, onVolver: () -> Unit) {
    val usuario by viewModel.usuario.collectAsState()
    val estadisticas by viewModel.estadisticas.collectAsState()
    val aviso by viewModel.aviso.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    var nombre by rememberSaveable(usuario?.id) { mutableStateOf(usuario?.nombre ?: "") }
    var intentoNombre by rememberSaveable { mutableStateOf(false) }

    var passwordActual by rememberSaveable { mutableStateOf("") }
    var passwordNueva by rememberSaveable { mutableStateOf("") }
    var passwordConfirmacion by rememberSaveable { mutableStateOf("") }
    var intentoPassword by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(aviso) {
        aviso?.let {
            snackbar.showSnackbar(it)
            viewModel.consumirAviso()
        }
    }

    val errorNombre = if (intentoNombre) Validadores.nombre(nombre) else null
    val errorActual = if (intentoPassword) Validadores.passwordLogin(passwordActual) else null
    val errorNueva =
        if (intentoPassword) Validadores.nuevaPassword(passwordActual, passwordNueva) else null
    val errorConfirmacion =
        if (intentoPassword) Validadores.confirmacion(passwordNueva, passwordConfirmacion) else null

    fun guardarNombre() {
        intentoNombre = true
        if (Validadores.nombre(nombre) == null) viewModel.guardarNombre(nombre)
    }

    fun cambiarPassword() {
        intentoPassword = true
        val sinErrores = listOf(
            Validadores.passwordLogin(passwordActual),
            Validadores.nuevaPassword(passwordActual, passwordNueva),
            Validadores.confirmacion(passwordNueva, passwordConfirmacion)
        ).all { it == null }
        if (sinErrores) {
            viewModel.cambiarPassword(passwordActual, passwordNueva) {
                passwordActual = ""
                passwordNueva = ""
                passwordConfirmacion = ""
                intentoPassword = false
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        val u = usuario
        if (u == null) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Datos de la cuenta
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                u.nombre.trim().take(1).uppercase().ifEmpty { "?" },
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(u.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            u.correo,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            u.rol.etiqueta,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Resumen de actividad
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ResumenNumero("Creados", estadisticas.creados, Modifier.weight(1f))
                    ResumenNumero("Asignados", estadisticas.asignados, Modifier.weight(1f))
                    ResumenNumero("Resueltos", estadisticas.resueltos, Modifier.weight(1f))
                }

                // Editar nombre
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Datos personales", style = MaterialTheme.typography.titleSmall)
                        CampoFormulario(
                            nombre, { nombre = it }, "Nombre completo",
                            error = errorNombre, imeAction = ImeAction.Done
                        )
                        Button(onClick = ::guardarNombre, modifier = Modifier.fillMaxWidth()) {
                            Text("Guardar nombre")
                        }
                    }
                }

                // Cambiar contraseña
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Cambiar contraseña", style = MaterialTheme.typography.titleSmall)
                        CampoFormulario(
                            passwordActual, { passwordActual = it }, "Contraseña actual",
                            error = errorActual, esPassword = true
                        )
                        CampoFormulario(
                            passwordNueva, { passwordNueva = it }, "Nueva contraseña",
                            error = errorNueva,
                            ayuda = "Mínimo 8 caracteres, con letras y números",
                            esPassword = true
                        )
                        CampoFormulario(
                            passwordConfirmacion, { passwordConfirmacion = it }, "Repetir nueva contraseña",
                            error = errorConfirmacion, esPassword = true, imeAction = ImeAction.Done
                        )
                        Button(onClick = ::cambiarPassword, modifier = Modifier.fillMaxWidth()) {
                            Text("Cambiar contraseña")
                        }
                    }
                }

                OutlinedButton(
                    onClick = { viewModel.cerrarSesion() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Cerrar sesión") }
            }
        }
    }
}

@Composable
private fun ResumenNumero(etiqueta: String, valor: Int, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                valor.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                etiqueta,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
