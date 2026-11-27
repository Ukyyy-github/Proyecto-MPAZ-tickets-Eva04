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
import cl.mpaz.ticket.ui.components.CampoFormulario
import cl.mpaz.ticket.util.Validadores

@Composable
fun LoginScreen(viewModel: AuthViewModel, onIrARegistro: () -> Unit) {
    var correo by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var intento by rememberSaveable { mutableStateOf(false) }

    val cargando by viewModel.cargando.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) { viewModel.limpiarError() }

    val errorCorreo = if (intento) Validadores.correo(correo) else null
    val errorPassword = if (intento) Validadores.passwordLogin(password) else null

    fun ingresar() {
        intento = true
        if (Validadores.correo(correo) == null && Validadores.passwordLogin(password) == null) {
            viewModel.iniciarSesion(correo, password)
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "MPAZ Ticket",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Gestión de solicitudes de mantención",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(32.dp))

            CampoFormulario(
                valor = correo,
                onCambio = { correo = it },
                etiqueta = "Correo",
                error = errorCorreo,
                teclado = KeyboardType.Email
            )
            Spacer(Modifier.height(8.dp))
            CampoFormulario(
                valor = password,
                onCambio = { password = it },
                etiqueta = "Contraseña",
                error = errorPassword,
                esPassword = true,
                imeAction = ImeAction.Done
            )

            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(16.dp))
            Button(onClick = ::ingresar, enabled = !cargando, modifier = Modifier.fillMaxWidth()) {
                if (cargando) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Ingresar")
                }
            }
            TextButton(onClick = onIrARegistro) { Text("¿No tienes cuenta? Regístrate") }

            Spacer(Modifier.height(24.dp))
            Text(
                "Cuentas de prueba (clave Mpaz1234):\nti@mpaz.cl · auxiliar@mpaz.cl · auxiliar2@mpaz.cl · funcionario@mpaz.cl",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
