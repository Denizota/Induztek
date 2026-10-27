package cl.duoc.induztek.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import cl.duoc.induztek.data.InduztekDbHelper

@Composable
fun ActaScreen(
    idVisita: Int,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val bd = remember { InduztekDbHelper.obtener(context) }

    // Consultar los datos de la visita.
    val visita = remember(idVisita) {
        bd.visitaPorId(idVisita)
    }

    // Consultar la instalación asociada a la visita.
    val instalacion = remember(visita?.idInstalacion) {
        visita?.let { bd.instalacionPorId(it.idInstalacion) }
    }

    // Consultar los protocolos que pertenecen a la visita.
    val protocolos = remember(idVisita) {
        bd.protocolosDeVisita(idVisita)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        TextButton(onClick = onVolver) {
            Text("← Volver")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Acta de visita",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (visita == null) {
            Text(
                text = "No se encontraron datos para esta visita.",
                color = Color.Red
            )

            return@Column
        }

        // Información general de la visita.
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF4F7F1)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Información de la visita",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                DatoActa("Número de OT", visita.numeroOt)
                DatoActa("Fecha", visita.fecha)
                DatoActa("Tipo de visita", visita.tipoVisita)

                DatoActa(
                    "Estado",
                    if (visita.fechaHoraCierre.isNullOrBlank()) {
                        "Abierta"
                    } else {
                        "Cerrada"
                    }
                )

                DatoActa(
                    "Fecha de cierre",
                    visita.fechaHoraCierre ?: "Sin fecha de cierre"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Información de la instalación.
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Instalación",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (instalacion != null) {
                    DatoActa("Nombre", instalacion.nombre)
                    DatoActa("Cliente", instalacion.clienteNombre)
                    DatoActa("Ubicación", instalacion.ubicacion)
                    DatoActa("Comuna", instalacion.comuna)
                    DatoActa(
                        "Observaciones de acceso",
                        instalacion.observacionesAcceso
                    )
                } else {
                    Text("No hay información de la instalación.")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Protocolos asociados a la visita.
        Text(
            text = "Protocolos asociados",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Cantidad de protocolos: ${protocolos.size}")

        Spacer(modifier = Modifier.height(12.dp))

        if (protocolos.isEmpty()) {
            Text("Esta visita todavía no tiene protocolos registrados.")
        } else {
            protocolos.forEach { protocolo ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF8F8F8)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Protocolo ${protocolo.numero}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        DatoActa("Equipo", protocolo.equipoTag)
                        DatoActa("Tipo de equipo", protocolo.tipoEquipo)
                        DatoActa("Estado", protocolo.estado.name)
                        DatoActa(
                            "Fecha de prueba",
                            protocolo.fechaHoraPrueba
                        )

                        DatoActa(
                            "Temperatura",
                            protocolo.temperaturaC?.let { "$it °C" }
                                ?: "Sin registro"
                        )

                        DatoActa(
                            "Humedad",
                            protocolo.humedadPct?.let { "$it %" }
                                ?: "Sin registro"
                        )

                        DatoActa(
                            "Observaciones",
                            protocolo.observaciones.ifBlank {
                                "Sin observaciones"
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Divider()

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Fin del resumen del acta",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al equipo")
        }
    }
}

@Composable
private fun DatoActa(
    titulo: String,
    valor: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$titulo:",
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = valor,
            modifier = Modifier.weight(1f)
        )
    }
}
