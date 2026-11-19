
package cl.duoc.induztek.ui.screen

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import cl.duoc.induztek.R
import cl.duoc.induztek.ui.theme.AjustesTema
import cl.duoc.induztek.ui.theme.Apariencia
import cl.duoc.induztek.ui.theme.Blanco
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.BordeSuave

@Composable
internal fun MenuLineas(
    onSalir: () -> Unit,
    onReiniciar: () -> Unit,
    modifier: Modifier = Modifier,
    opciones: @Composable (cerrar: () -> Unit) -> Unit = {}
) {
    var abierto by remember { mutableStateOf(false) }
    var preguntar by remember { mutableStateOf(false) }
    var explicarLocutor by remember { mutableStateOf(false) }
    val contexto = LocalContext.current

    Box(modifier) {
        Icon(
            painter = painterResource(id = R.drawable.ic_menu_lineas),
            contentDescription = stringResource(R.string.inicio_menu),
            tint = Blanco,
            modifier = Modifier
                .size(Dimens.tamanoBotonAtras)
                .clip(RoundedCornerShape(Dimens.radioBotonChico))
                .background(Color(0xFF07131C).copy(alpha = 0.55f))
                .clickable(role = Role.Button) { abierto = true }
                .padding(Dimens.salto2)
        )
        DropdownMenu(
            expanded = abierto,
            onDismissRequest = { abierto = false },
            shape = RoundedCornerShape(Dimens.radioMenu),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = Dimens.sinElevacion,
            shadowElevation = Dimens.elevacionBarraFlotante,
            border = BorderStroke(Dimens.bordeTarjeta, BordeSuave())
        ) {
            opciones { abierto = false }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.inicio_menu_sistema)) },
                onClick = {
                    abierto = false
                    AjustesTema.guardar(contexto, Apariencia.SISTEMA)
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.inicio_menu_claro)) },
                onClick = {
                    abierto = false
                    AjustesTema.guardar(contexto, Apariencia.CLARO)
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.inicio_menu_oscuro)) },
                onClick = {
                    abierto = false
                    AjustesTema.guardar(contexto, Apariencia.OSCURO)
                }
            )
            DivisorMenu()
            DropdownMenuItem(
                text = { Text(stringResource(R.string.inicio_menu_reiniciar)) },
                onClick = {
                    abierto = false
                    preguntar = true
                }
            )
            DivisorMenu()
            DropdownMenuItem(
                text = { Text(stringResource(R.string.inicio_menu_talkback)) },
                onClick = {
                    abierto = false
                    explicarLocutor = true
                }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        stringResource(R.string.inicio_menu_salir),
                        color = MaterialTheme.colorScheme.error
                    )
                },
                onClick = { abierto = false; onSalir() }
            )
        }
    }

    if (preguntar) {
        AlertDialog(
            onDismissRequest = { preguntar = false },
            title = { Text(stringResource(R.string.inicio_reiniciar_titulo)) },
            text = { Text(stringResource(R.string.inicio_reiniciar_texto)) },
            confirmButton = {
                TextButton(onClick = { preguntar = false; onReiniciar() }) {
                    Text(stringResource(R.string.inicio_reiniciar_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { preguntar = false }) {
                    Text(stringResource(R.string.inicio_reiniciar_no))
                }
            }
        )
    }

    if (explicarLocutor) {
        AlertDialog(
            onDismissRequest = { explicarLocutor = false },
            title = { Text(stringResource(R.string.inicio_menu_talkback)) },
            text = { Text(stringResource(R.string.puente_locutor_nota)) },
            confirmButton = {
                TextButton(onClick = {
                    explicarLocutor = false
                    contexto.startActivity(
                        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }) {
                    Text(stringResource(R.string.inicio_menu_abrir_ajustes))
                }
            },
            dismissButton = {
                TextButton(onClick = { explicarLocutor = false }) {
                    Text(stringResource(R.string.btn_cancelar))
                }
            }
        )
    }
}

@Composable
private fun DivisorMenu() {
    HorizontalDivider(
        thickness = Dimens.grosorDivisor,
        color = BordeSuave(),
        modifier = Modifier.padding(horizontal = Dimens.salto3)
    )
}
