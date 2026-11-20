package cl.mpaz.ticket.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions

/** Campo de texto con validación visual (mensaje de error o texto de ayuda debajo). */
@Composable
fun CampoFormulario(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    ayuda: String? = null,
    teclado: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    esPassword: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    enabled: Boolean = true
) {
    var visible by remember { mutableStateOf(false) }
    val textoSoporte = error ?: ayuda

    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = singleLine,
        minLines = minLines,
        isError = error != null,
        supportingText = if (textoSoporte != null) ({ Text(textoSoporte) }) else null,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (esPassword) KeyboardType.Password else teclado,
            imeAction = imeAction
        ),
        visualTransformation =
            if (esPassword && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (esPassword) ({
            TextButton(onClick = { visible = !visible }) { Text(if (visible) "Ocultar" else "Ver") }
        }) else null
    )
}

/** Lista desplegable de solo lectura. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SelectorDropdown(
    label: String,
    opciones: List<T>,
    seleccionado: T?,
    etiqueta: (T) -> String,
    onSeleccion: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String = "Seleccionar"
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido && enabled,
        onExpandedChange = { if (enabled) expandido = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = seleccionado?.let(etiqueta) ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(etiqueta(opcion)) },
                    onClick = {
                        onSeleccion(opcion)
                        expandido = false
                    }
                )
            }
        }
    }
}
