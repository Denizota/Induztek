package cl.duoc.induztek.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.duoc.induztek.data.InduztekDbHelper
import cl.duoc.induztek.model.Rol

/** El jefe técnico suspende el acceso local de otro usuario, conservando su registro. */
@Composable
fun SuspensionCuentaScreen(
    idJefeTecnico: Int,
    idUsuarioObjetivo: Int,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val db = remember(context) { InduztekDbHelper.obtener(context) }
    val jefe = remember(idJefeTecnico) { db.buscarUsuarioPorId(idJefeTecnico) }
    val objetivo = remember(idUsuarioObjetivo) { db.buscarUsuarioPorId(idUsuarioObjetivo) }
    val autorizado = jefe?.activo == true && jefe.rol == Rol.JEFE_TECNICO
    val puedeSuspender = autorizado && objetivo?.activo == true &&
        idJefeTecnico != idUsuarioObjetivo

    var motivo by remember(idUsuarioObjetivo) { mutableStateOf("") }
    var aceptado by remember(idUsuarioObjetivo) { mutableStateOf(false) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var suspendida by remember(idUsuarioObjetivo) { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onVolver) { Text("‹ Volver") }
                Text("Suspensión de cuenta", style = MaterialTheme.typography.titleLarge)
            }
        }
    ) { margen ->
        Column(
            modifier = Modifier.fillMaxSize().padding(margen)
                .verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!autorizado) {
                Text("Solo el jefe técnico puede suspender cuentas.",
                    color = MaterialTheme.colorScheme.error)
                return@Column
            }
            if (objetivo == null) {
                Text("No se encontró el perfil seleccionado.")
                return@Column
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Usuario seleccionado", style = MaterialTheme.typography.labelMedium)
                    Text(objetivo.nombre, style = MaterialTheme.typography.titleLarge)
                    Text("Cargo: ${objetivo.cargo.ifBlank { objetivo.rol.nombre }}")
                    Text("Correo: ${objetivo.correo}")
                    Text("Estado: ${if (objetivo.activo && !suspendida) "Activo" else "Suspendido"}")
                }
            }

            Text("Motivo de suspensión", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = motivo,
                onValueChange = { if (it.length <= 500) motivo = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Explica el motivo de la suspensión") },
                minLines = 4,
                enabled = puedeSuspender && !suspendida,
                supportingText = { Text("Mínimo 10 y máximo 500 caracteres") }
            )

            Text(
                "Al confirmar, se desactivará el acceso de esta cuenta en la base de datos " +
                    "local y quedará registrado el motivo. No se eliminará el usuario.",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "El diseño contempla un plazo de 30 días, pero su eliminación automática " +
                    "no está implementada.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = aceptado,
                    onCheckedChange = { aceptado = it },
                    enabled = puedeSuspender && !suspendida
                )
                Text("Confirmo que deseo suspender este perfil")
            }

            if (error.isNotBlank()) {
                Text(error, color = MaterialTheme.colorScheme.error)
            }
            if (suspendida) {
                Text("Cuenta suspendida correctamente.",
                    color = MaterialTheme.colorScheme.primary)
            }

            Button(
                onClick = { mostrarConfirmacion = true },
                enabled = puedeSuspender && !suspendida && aceptado && motivo.trim().length >= 10,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Suspender perfil")
            }
            OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
                Text(if (suspendida) "Volver a personal" else "Cancelar")
            }
        }
    }

    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text("Confirmar suspensión") },
            text = { Text("¿Suspender el acceso de ${objetivo?.nombre.orEmpty()}?") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarConfirmacion = false
                    suspendida = db.suspenderCuenta(
                        idAdministrador = idJefeTecnico,
                        idObjetivo = idUsuarioObjetivo,
                        motivo = motivo
                    )
                    if (!suspendida) error = "No se pudo suspender el perfil. Revisa sus permisos."
                }) { Text("Sí, suspender") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion = false }) { Text("Cancelar") }
            }
        )
    }
}
