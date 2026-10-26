package cl.duoc.induztek.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

//?!?!?!?!?! DESDE CUANDO HAY VERDE EN INDUZTEK.
private val VerdeInduztek = Color(0xFF83C94A)
private val FondoInduztek = Color(0xFFF6F8FC)

@Composable
fun InicioScreen(
    onProtocolosClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onPerfilClick: () -> Unit = {},
    onCerrarSesion: () -> Unit = {}
) {
    var pestanaSeleccionada by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = FondoInduztek,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White
            ) {
                NavigationBarItem(
                    selected = pestanaSeleccionada == 0,
                    onClick = { pestanaSeleccionada = 0 },
                    icon = { Text("⌂") },
                    label = { Text("Inicio") }
                )
                NavigationBarItem(
                    selected = pestanaSeleccionada == 1,
                    onClick = {
                        pestanaSeleccionada = 1
                        onProtocolosClick()
                    },
                    icon = { Text("▤") },
                    label = { Text("Protocolos") }
                )
                NavigationBarItem(
                    selected = pestanaSeleccionada == 2,
                    onClick = {
                        pestanaSeleccionada = 2
                        onChatClick()
                    },
                    icon = { Text("✉") },
                    label = { Text("Mensajes") }
                )
                NavigationBarItem(
                    selected = pestanaSeleccionada == 3,
                    onClick = {
                        pestanaSeleccionada = 3
                        onPerfilClick()
                    },
                    icon = { Text("○") },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Induztek",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = VerdeInduztek
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Protocolos de prueba en terreno",
                style = MaterialTheme.typography.titleMedium,
                color = Color.DarkGray
            )

            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(
                        text = "¡Bienvenido!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Desde aquí puedes consultar los protocolos " +
                                "de prueba, revisar su información y comunicarte " +
                                "con el equipo."
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Accesos rápidos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            TarjetaInicio(
                titulo = "Protocolos de prueba",
                descripcion = "Consulta los protocolos disponibles en terreno.",
                simbolo = "▤",
                onClick = onProtocolosClick
            )

            TarjetaInicio(
                titulo = "Mensajes",
                descripcion = "Consulta las conversaciones del equipo.",
                simbolo = "✉",
                onClick = onChatClick
            )

            TarjetaInicio(
                titulo = "Mi perfil",
                descripcion = "Consulta tu información de usuario.",
                simbolo = "○",
                onClick = onPerfilClick
            )

            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCerrarSesion() },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFEAF3E4)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Cerrar sesión",
                        color = Color(0xFF356D26),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun TarjetaInicio(
    titulo: String,
    descripcion: String,
    simbolo: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = simbolo,
                color = VerdeInduztek,
                style = MaterialTheme.typography.headlineMedium
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = descripcion,
                    color = Color.DarkGray,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Ver sección →",
                    color = VerdeInduztek,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}