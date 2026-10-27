
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.induztek.data.ComunicacionLocal
import cl.duoc.induztek.data.InduztekDbHelper
import cl.duoc.induztek.model.Usuario
import cl.duoc.induztek.ui.theme.LocalConfiguracionTema


@Composable
private fun Cabecera(
    titulo: String,
    onVolver: () -> Unit,
    subtitulo: String = ""
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onVolver) {
                Text("‹ Volver")
            }
            Text(
                titulo,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (subtitulo.isNotEmpty()) {
            Text(
                subtitulo,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

@Composable
private fun TarjetaContenido(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun Dato(
    etiqueta: String,
    valor: String
) {
    Column(Modifier.padding(vertical = 5.dp)) {
        Text(
            etiqueta,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium
        )
        Text(
            valor.ifBlank { "Sin registrar" },
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// PANTALLA 1: LISTA DE MENSAJES

@Composable
fun ListaMensajesScreen(
    usuario: Usuario,
    onAbrirChat: (Int) -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val bd = remember {
        InduztekDbHelper.obtener(context)
    }
    val local = remember {
        ComunicacionLocal(context)
    }

    val contactos = remember(usuario.idUsuario) {
        bd.listarContactos(usuario.idUsuario)
    }

    var busqueda by remember {
        mutableStateOf("")
    }

    var soloConMensajes by remember {
        mutableStateOf(false)
    }

    val visibles = contactos.filter { contacto ->
        contacto.nombre.contains(
            busqueda,
            ignoreCase = true
        ) &&
        (
            !soloConMensajes ||
            local.ultimaConversacion(
                usuario.idUsuario,
                contacto.idUsuario
            ) != null
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Cabecera(
                "Mensajes",
                onVolver,
                "Conversaciones guardadas en este dispositivo"
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                label = {
                    Text("Buscar contacto")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = soloConMensajes,
                    onCheckedChange = {
                        soloConMensajes = it
                    }
                )
                Text("Solo conversaciones iniciadas")
            }

            LazyColumn {
                items(
                    visibles,
                    key = { it.idUsuario }
                ) { contacto ->
                    val ultimo = local.ultimaConversacion(
                        usuario.idUsuario,
                        contacto.idUsuario
                    )

                    TarjetaContenido {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onAbrirChat(contacto.idUsuario)
                                }
                        ) {
                            Text(
                                contacto.nombre,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                contacto.cargo.ifBlank {
                                    contacto.rol.nombre
                                },
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                ultimo?.texto
                                    ?: "Iniciar conversación",
                                maxLines = 1
                            )

                            if (ultimo != null) {
                                Text(
                                    ultimo.fecha,
                                    style = MaterialTheme
                                        .typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (visibles.isEmpty()) {
                    item {
                        Text(
                            "No hay contactos para mostrar.",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// PANTALLA 2: CHAT INDIVIDUAL

@Composable
fun ChatIndividualScreen(
    usuario: Usuario,
    idContacto: Int,
    onDetalles: () -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current

    val bd = remember {
        InduztekDbHelper.obtener(context)
    }

    val local = remember {
        ComunicacionLocal(context)
    }

    val contacto = remember(idContacto) {
        bd.buscarUsuarioPorId(idContacto)
    }

    var entrada by remember {
        mutableStateOf("")
    }

    var refrescar by remember {
        mutableIntStateOf(0)
    }

    val mensajes = remember(
        usuario.idUsuario,
        idContacto,
        refrescar
    ) {
        local.mensajes(
            usuario.idUsuario,
            idContacto
        )
    }

    if (
        contacto == null ||
        contacto.idUsuario == usuario.idUsuario
    ) {
        Cabecera(
            "Contacto no disponible",
            onVolver
        )
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                Cabecera(
                    contacto.nombre,
                    onVolver
                )
                TextButton(
                    onClick = onDetalles,
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text("Ver detalles de conversación")
                }
            }
        },
        bottomBar = {
            Column(
                Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = entrada,
                        onValueChange = {
                            if (it.length <= 1000) {
                                entrada = it
                            }
                        },
                        label = {
                            Text("Escribe un mensaje")
                        },
                        modifier = Modifier.weight(1f),
                        maxLines = 3
                    )

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (
                                local.enviar(
                                    usuario.idUsuario,
                                    idContacto,
                                    entrada
                                )
                            ) {
                                entrada = ""
                                refrescar++
                            }
                        },
                        enabled = entrada.isNotBlank()
                    ) {
                        Text("Enviar")
                    }
                }

                Text(
                    "Chat local: los mensajes no llegan a otro usuario.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (mensajes.isEmpty()) {
                item {
                    Text(
                        "Aún no hay mensajes. Puedes escribir uno " +
                        "para guardarlo en este dispositivo."
                    )
                }
            }

            items(
                mensajes,
                key = { it.id }
            ) { mensaje ->
                val mio =
                    mensaje.remitenteId == usuario.idUsuario

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        if (mio) {
                            Arrangement.End
                        } else {
                            Arrangement.Start
                        }
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (mio) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            contentColor = if (mio) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        ),
                        modifier = Modifier.fillMaxWidth(0.83f)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(mensaje.texto)
                            Text(
                                mensaje.fecha,
                                style = MaterialTheme
                                    .typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// PANTALLA 3: DETALLES DEL CHAT

@Composable
fun DetallesChatScreen(
    usuario: Usuario,
    idContacto: Int,
    onVolver: () -> Unit
) {
    val context = LocalContext.current

    val bd = remember {
        InduztekDbHelper.obtener(context)
    }

    val local = remember {
        ComunicacionLocal(context)
    }

    val contacto = remember(idContacto) {
        bd.buscarUsuarioPorId(idContacto)
    }

    val cantidad = remember(
        usuario.idUsuario,
        idContacto
    ) {
        local.mensajes(
            usuario.idUsuario,
            idContacto
        ).size
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Cabecera(
                "Detalles del chat",
                onVolver
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            if (contacto == null) {
                Text("Contacto no disponible")
            } else {
                TarjetaContenido {
                    Text(
                        contacto.nombre,
                        style = MaterialTheme
                            .typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Dato(
                        "Rol",
                        contacto.rol.nombre
                    )
                    Dato(
                        "Cargo",
                        contacto.cargo
                    )
                    Dato(
                        "Correo",
                        contacto.correo
                    )
                    Dato(
                        "Teléfono",
                        contacto.telefono
                    )
                }

                TarjetaContenido {
                    Dato(
                        "Mensajes guardados",
                        cantidad.toString()
                    )

                    Text(
                        "Solo datos locales. No se comparten " +
                        "archivos ni mensajes en línea.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// PANTALLA 4: PERFIL

@Composable
fun PerfilScreen(
    usuarioId: Int,
    onEditar: () -> Unit,
    onPersonal: () -> Unit,
    onValidar: () -> Unit,
    onSalir: () -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current

    val bd = remember {
        InduztekDbHelper.obtener(context)
    }

    val usuario = bd.buscarUsuarioPorId(usuarioId)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Cabecera(
                "Mi perfil",
                onVolver
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(18.dp)
        ) {
            if (usuario == null) {
                Text("No se encontró el usuario")
                return@Column
            }

            TarjetaContenido {
                Text(
                    usuario.nombre,
                    style = MaterialTheme
                        .typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    usuario.rol.nombre,
                    color = MaterialTheme.colorScheme.tertiary
                )

                HorizontalDivider(
                    Modifier.padding(vertical = 12.dp)
                )

                Dato(
                    "RUT",
                    usuario.rut
                )
                Dato(
                    "Correo",
                    usuario.correo
                )
                Dato(
                    "Teléfono",
                    usuario.telefono
                )
                Dato(
                    "Cargo",
                    usuario.cargo
                )
            }

            val configuracionTema = LocalConfiguracionTema.current
            TarjetaContenido {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Modo oscuro",
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Cambiar la apariencia de la aplicación",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = configuracionTema.modoOscuro,
                        onCheckedChange = configuracionTema.cambiarModoOscuro
                    )
                }
            }

            Button(
                onClick = onEditar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Editar perfil")
            }

            OutlinedButton(
                onClick = onPersonal,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Personal e historial")
            }

            OutlinedButton(
                onClick = onValidar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Validación / registro")
            }

            TextButton(
                onClick = onSalir,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}

// PANTALLA 5: EDITAR PERFIL

@Composable
fun EditarPerfilScreen(
    usuarioId: Int,
    onGuardado: () -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current

    val bd = remember {
        InduztekDbHelper.obtener(context)
    }

    val usuario = remember(usuarioId) {
        bd.buscarUsuarioPorId(usuarioId)
    }

    var nombre by remember {
        mutableStateOf(usuario?.nombre.orEmpty())
    }

    var correo by remember {
        mutableStateOf(usuario?.correo.orEmpty())
    }

    var telefono by remember {
        mutableStateOf(usuario?.telefono.orEmpty())
    }

    var cargo by remember {
        mutableStateOf(usuario?.cargo.orEmpty())
    }

    var error by remember {
        mutableStateOf("")
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Cabecera(
                "Editar perfil",
                onVolver
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre completo") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = cargo,
                onValueChange = { cargo = it },
                label = { Text("Cargo") },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                "El RUT y el rol no pueden modificarse desde aquí.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (error.isNotBlank()) {
                Text(
                    error,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    if (
                        bd.actualizarPerfil(
                            usuarioId,
                            nombre,
                            correo,
                            telefono,
                            cargo
                        )
                    ) {
                        onGuardado()
                    } else {
                        error = "Revisa el nombre y correo. " +
                            "Puede estar inválido o ya registrado."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar cambios")
            }

            OutlinedButton(
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    }
}

// PANTALLA 6: PERSONAL E HISTORIAL

@Composable
fun PersonalHistorialScreen(
    usuarioId: Int,
    onValidar: () -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current

    val bd = remember {
        InduztekDbHelper.obtener(context)
    }

    val personal = remember(usuarioId) {
        bd.listarContactos(usuarioId)
    }

    val protocolos = remember {
        bd.listarProtocolos()
    }

    var mostrarHistorial by remember {
        mutableStateOf(false)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Cabecera(
                "Personal e historial",
                onVolver
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        mostrarHistorial = false
                    }
                ) {
                    Text("Personal")
                }

                OutlinedButton(
                    onClick = {
                        mostrarHistorial = true
                    }
                ) {
                    Text("Historial")
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                if (!mostrarHistorial) {
                    items(
                        personal,
                        key = { it.idUsuario }
                    ) { persona ->
                        TarjetaContenido {
                            Text(
                                persona.nombre,
                                fontWeight = FontWeight.Bold
                            )
                            Text(persona.rol.nombre)
                            Text(
                                persona.correo,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    if (protocolos.isEmpty()) {
                        item {
                            Text(
                                "Aún no hay protocolos registrados."
                            )
                        }
                    }

                    items(
                        protocolos,
                        key = { it.idProtocolo }
                    ) { protocolo ->
                        TarjetaContenido {
                            Text(
                                protocolo.numero,
                                fontWeight = FontWeight.Bold
                            )
                            Dato(
                                "Equipo",
                                protocolo.equipoTag
                            )
                            Dato(
                                "Fecha",
                                protocolo.fechaHoraPrueba
                            )
                            Dato(
                                "Estado",
                                protocolo.estado.nombre
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onValidar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ir a validación / registro")
            }
        }
    }
}

// PANTALLA 7: VALIDACION Y REGISTRO

@Composable
fun ValidacionRegistroScreen(
    usuarioId: Int,
    onVolver: () -> Unit
) {
    val context = LocalContext.current

    val bd = remember {
        InduztekDbHelper.obtener(context)
    }

    val local = remember {
        ComunicacionLocal(context)
    }

    var nombre by remember {
        mutableStateOf(
            bd.buscarUsuarioPorId(usuarioId)?.nombre.orEmpty()
        )
    }

    var observaciones by remember {
        mutableStateOf("")
    }

    var confirmado by remember {
        mutableStateOf(false)
    }

    var mensaje by remember {
        mutableStateOf("")
    }

    var refrescar by remember {
        mutableIntStateOf(0)
    }

    val guardados = remember(
        usuarioId,
        refrescar
    ) {
        local.validaciones(usuarioId)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Cabecera(
                "Validación y registro",
                onVolver
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "Registrar constancia interna",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Este registro es local y no equivale " +
                "a una firma electrónica de acta.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = {
                    Text("Nombre del responsable")
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = observaciones,
                onValueChange = {
                    observaciones = it
                },
                label = {
                    Text("Observaciones")
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = confirmado,
                    onCheckedChange = {
                        confirmado = it
                    }
                )
                Text(
                    "Confirmo que revisé los datos"
                )
            }

            Button(
                enabled = confirmado && nombre.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (
                        local.guardarValidacion(
                            usuarioId,
                            nombre,
                            observaciones
                        )
                    ) {
                        observaciones = ""
                        confirmado = false
                        refrescar++
                        mensaje =
                            "Registro guardado en este dispositivo"
                    } else {
                        mensaje =
                            "No se pudo guardar el registro"
                    }
                }
            ) {
                Text("Guardar registro")
            }

            if (mensaje.isNotEmpty()) {
                Text(
                    mensaje,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "Registros previos",
                style = MaterialTheme.typography.titleMedium
            )

            LazyColumn {
                if (guardados.isEmpty()) {
                    item {
                        Text(
                            "No hay registros todavía."
                        )
                    }
                }

                items(
                    guardados,
                    key = { it.id }
                ) { registro ->
                    TarjetaContenido {
                        Text(
                            registro.responsable,
                            fontWeight = FontWeight.Bold
                        )
                        Text(registro.fecha)

                        if (
                            registro.observaciones.isNotBlank()
                        ) {
                            Text(
                                registro.observaciones
                            )
                        }
                    }
                }
            }
        }
    }
}
