
package cl.duoc.induztek.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.induztek.R
import cl.duoc.induztek.model.Rol
import cl.duoc.induztek.model.Usuario
import cl.duoc.induztek.ui.components.BotonPrincipal
import cl.duoc.induztek.ui.components.CajaBordeada
import cl.duoc.induztek.ui.components.CampoSeleccion
import cl.duoc.induztek.ui.components.fondoPantallaInterna
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.RojoRechazado
import cl.duoc.induztek.ui.theme.TextoSecundario
import cl.duoc.induztek.ui.theme.TextoTitulo
import cl.duoc.induztek.ui.theme.VerdeAprobado
import cl.duoc.induztek.viewmodel.InicioViewModel


@Composable
fun AsignarTareaScreen(
    idPersona: Int,
    onAtras: () -> Unit,
    modeloPantalla: InicioViewModel = viewModel()
) {
    val persona = modeloPantalla.usuario(idPersona)
    if (persona == null) return

    var idCliente by rememberSaveable {
        mutableIntStateOf(if (persona.rol == Rol.CLIENTE) idPersona else -1)
    }
    var idIngeniero by rememberSaveable {
        mutableIntStateOf(if (persona.rol == Rol.INGENIERO) idPersona else -1)
    }
    var idEquipo by rememberSaveable { mutableIntStateOf(-1) }
    var aviso by rememberSaveable { mutableStateOf("") }
    var avisoBueno by rememberSaveable { mutableStateOf(false) }
    val sincronia = versionSincronia()

    val clientes = remember(sincronia) { if (persona.rol == Rol.INGENIERO) modeloPantalla.clientes() else emptyList() }
    val ingenieros = remember(sincronia) { if (persona.rol == Rol.CLIENTE) modeloPantalla.ingenieros() else emptyList() }
    val equipos = remember(idCliente, sincronia) {
        if (idCliente > 0) modeloPantalla.equiposDeCliente(idCliente) else emptyList()
    }

    val contexto = LocalContext.current
    val tCliente = stringResource(R.string.asignar_cliente)
    val tIngeniero = stringResource(R.string.asignar_ingeniero)
    val tEquipo = stringResource(R.string.asignar_equipo)
    val tElegirCliente = stringResource(R.string.asignar_elegir_cliente)
    val tElegirIngeniero = stringResource(R.string.asignar_elegir_ingeniero)
    val tElegirEquipo = stringResource(R.string.asignar_elegir_equipo)

    val etiquetasClientes = mutableListOf<String>()
    for (cliente in clientes) {
        etiquetasClientes.add(nombreDelCliente(cliente))
    }
    var clienteElegido = ""
    for (cliente in clientes) {
        if (cliente.idUsuario == idCliente) clienteElegido = nombreDelCliente(cliente)
    }

    val etiquetasIngenieros = mutableListOf<String>()
    var ingenieroElegido = ""
    for (ingeniero in ingenieros) {
        etiquetasIngenieros.add(ingeniero.nombre)
        if (ingeniero.idUsuario == idIngeniero) ingenieroElegido = ingeniero.nombre
    }

    val etiquetasEquipos = mutableListOf<String>()
    var equipoElegido = ""
    for (equipo in equipos) {
        val etiqueta = "${equipo.tag} · ${equipo.tipoEquipo}"
        etiquetasEquipos.add(etiqueta)
        if (equipo.idEquipo == idEquipo) equipoElegido = etiqueta
    }

    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .fondoPantallaInterna()
    ) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.margenHorizontalPantalla)
        ) {
            Text(
                stringResource(R.string.nombre_empresa),
                style = MaterialTheme.typography.labelLarge,
                color = TextoTitulo()
            )
            Spacer(Modifier.height(Dimens.salto1))
            CabeceraVolver(
                stringResource(R.string.asignar_titulo),
                onAtras = onAtras
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarPersona(
                    nombre = persona.nombre,
                    foto = imagenGuardada(persona.idUsuario, ImagenesPerfil.TIPO_FOTO),
                    lado = Dimens.tamanoAvatarRol
                )
                Spacer(Modifier.width(Dimens.espacioEntreCampos))
                Column(Modifier.weight(1f)) {
                    Text(
                        persona.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextoTitulo()
                    )
                    Text(
                        persona.rol.nombre,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario()
                    )
                }
            }

            Spacer(Modifier.height(Dimens.espacioMedio))

            val esIngeniero = persona.rol == Rol.INGENIERO
            val esCliente = persona.rol == Rol.CLIENTE
            val asignable = (esIngeniero && persona.activo) || esCliente

            if (!asignable) {
                CajaBordeada(Modifier.fillMaxWidth()) {
                    Text(
                        if (!esIngeniero && !esCliente) stringResource(R.string.asignar_solo_personal)
                        else stringResource(R.string.admin_asignar_suspendido),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoSecundario()
                    )
                }
            } else {
                if (esIngeniero) {
                    CampoSeleccion(
                        valor = clienteElegido,
                        opciones = etiquetasClientes,
                        onSeleccionar = { elegido ->
                            for (cliente in clientes) {
                                if (nombreDelCliente(cliente) == elegido) {
                                    idCliente = cliente.idUsuario
                                    idEquipo = -1
                                    aviso = ""
                                }
                            }
                        },
                        etiqueta = tCliente,
                        textoSugerido = tElegirCliente
                    )
                } else {
                    CampoSeleccion(
                        valor = ingenieroElegido,
                        opciones = etiquetasIngenieros,
                        onSeleccionar = { elegido ->
                            for (ingeniero in ingenieros) {
                                if (ingeniero.nombre == elegido) {
                                    idIngeniero = ingeniero.idUsuario
                                    aviso = ""
                                }
                            }
                        },
                        etiqueta = tIngeniero,
                        textoSugerido = tElegirIngeniero
                    )
                }

                Spacer(Modifier.height(Dimens.salto4))

                CampoSeleccion(
                    valor = equipoElegido,
                    opciones = etiquetasEquipos,
                    onSeleccionar = { elegido ->
                        for (equipo in equipos) {
                            if ("${equipo.tag} · ${equipo.tipoEquipo}" == elegido) {
                                idEquipo = equipo.idEquipo
                                aviso = ""
                            }
                        }
                    },
                    etiqueta = tEquipo,
                    textoSugerido = tElegirEquipo,
                    habilitado = idCliente > 0 && equipos.isNotEmpty()
                )

                if (idCliente > 0 && equipos.isEmpty()) {
                    Spacer(Modifier.height(Dimens.salto2))
                    Text(
                        stringResource(R.string.asignar_sin_equipos),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario()
                    )
                }

                Spacer(Modifier.height(Dimens.espacioMedio))

                val ingenieroFinal = if (esIngeniero) persona.idUsuario else idIngeniero
                BotonPrincipal(
                    texto = stringResource(R.string.admin_asignar),
                    onClick = {
                        val error = modeloPantalla.asignarTarea(idEquipo, ingenieroFinal)
                        if (error.isEmpty()) {
                            val nombreIngeniero = if (esIngeniero) persona.nombre else ingenieroElegido
                            aviso = contexto.getString(R.string.asignar_hecha, equipoElegido, nombreIngeniero)
                            avisoBueno = true
                            idEquipo = -1
                        } else {
                            aviso = error
                            avisoBueno = false
                        }
                    },
                    habilitado = idEquipo > 0 && ingenieroFinal > 0,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (aviso.isNotEmpty()) {
                Spacer(Modifier.height(Dimens.salto3))
                Text(
                    aviso,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (avisoBueno) VerdeAprobado() else RojoRechazado()
                )
            }

            Spacer(Modifier.height(Dimens.espacioGrande))
        }
    }
}

private fun nombreDelCliente(cliente: Usuario): String {
    if (cliente.rut.isBlank()) return cliente.nombre
    return "${cliente.nombre} · ${cliente.rut}"
}
