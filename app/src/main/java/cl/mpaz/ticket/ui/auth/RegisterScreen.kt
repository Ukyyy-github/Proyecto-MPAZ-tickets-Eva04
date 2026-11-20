package cl.mpaz.ticket.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.mpaz.ticket.model.Rol
import cl.mpaz.ticket.ui.components.CampoFormulario
import cl.mpaz.ticket.ui.components.SelectorDropdown
import cl.mpaz.ticket.util.Validadores

@Composable
fun RegisterScreen(viewModel: AuthViewModel, onVolver: () -> Unit) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var rol by rememberSaveable { mutableStateOf(Rol.FUNCIONARIO) }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmacion by rememberSaveable { mutableStateOf("") }
    var intento by rememberSaveable { mutableStateOf(false) }

    val cargando by viewModel.cargando.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) { viewModel.limpiarError() }

    val errorNombre = if (intento) Validadores.nombre(nombre) else null
    val errorCorreo = if (intento) Validadores.correo(correo) else null
    val errorPassword = if (intento) Validadores.passwordRegistro(password) else null
    val errorConfirmacion = if (intento) Validadores.confirmacion(password, confirmacion) else null

    fun crearCuenta() {
        intento = true
        val sinErrores = listOf(
            Validadores.nombre(nombre),
            Validadores.correo(correo),
            Validadores.passwordRegistro(password),
            Validadores.confirmacion(password, confirmacion)
        ).all { it == null }
        if (sinErrores) viewModel.registrar(nombre, correo, password, rol)
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Crear cuenta",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))

            CampoFormulario(nombre, { nombre = it }, "Nombre completo", error = errorNombre)
            CampoFormulario(
                correo, { correo = it }, "Correo",
                error = errorCorreo, teclado = KeyboardType.Email
            )
            SelectorDropdown(
                label = "Rol",
                opciones = Rol.entries,
                seleccionado = rol,
                etiqueta = { it.etiqueta },
                onSeleccion = { rol = it }
            )
            CampoFormulario(
                password, { password = it }, "Contraseña",
                error = errorPassword,
                ayuda = "Mínimo 8 caracteres, con letras y números",
                esPassword = true
            )
            CampoFormulario(
                confirmacion, { confirmacion = it }, "Repetir contraseña",
                error = errorConfirmacion, esPassword = true, imeAction = ImeAction.Done
            )

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(8.dp))
            Button(onClick = ::crearCuenta, enabled = !cargando, modifier = Modifier.fillMaxWidth()) {
                if (cargando) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Registrarme")
                }
            }
            TextButton(onClick = onVolver) { Text("Ya tengo cuenta") }
        }
    }
}
