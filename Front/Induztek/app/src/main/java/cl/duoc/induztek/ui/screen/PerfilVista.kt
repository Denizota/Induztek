
package cl.duoc.induztek.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import cl.duoc.induztek.R
import cl.duoc.induztek.model.Usuario
import cl.duoc.induztek.ui.components.BarraInferior
import cl.duoc.induztek.ui.components.CajaBordeada
import cl.duoc.induztek.ui.components.SeccionBarra
import cl.duoc.induztek.ui.components.fondoPantallaInterna
import cl.duoc.induztek.ui.theme.Acento
import cl.duoc.induztek.ui.theme.Blanco
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.TextoSecundario
import cl.duoc.induztek.ui.theme.InduztekTheme
import cl.duoc.induztek.ui.theme.BordeSuave
import cl.duoc.induztek.ui.theme.TextoTitulo
import cl.duoc.induztek.ui.theme.TextoPrincipal
import cl.duoc.induztek.viewmodel.ResumenPerfil
import java.io.File


@Composable
internal fun PerfilContenido(
    usuarioActual: Usuario,
    numeros: ResumenPerfil,
    esCliente: Boolean,
    equiposPlanta: Int,
    actas: Int,
    empresa: String,
    foto: File?,
    banner: File?,
    onEditar: () -> Unit,
    onIrConsultas: () -> Unit,
    onSalir: () -> Unit,
    onReiniciar: () -> Unit,
    onIrEquipos: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .fondoPantallaInterna()
    ) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Box(Modifier.fillMaxWidth()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(Dimens.altoBannerPerfil)
                ) {
                    BannerUsuario(banner, Modifier.fillMaxSize())
                    MenuPerfil(
                        onIrConsultas = onIrConsultas,
                        onSalir = onSalir,
                        onReiniciar = onReiniciar,
                        modifier = Modifier.align(Alignment.TopEnd).padding(Dimens.salto4)
                    )
                    Box(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(Dimens.grosorFirmaMarca)
                            .background(Acento())
                    )
                }
                AvatarPersona(
                    nombre = usuarioActual.nombre,
                    foto = foto,
                    lado = Dimens.ladoAvatarPerfil,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = Dimens.margenHorizontalPantalla)
                        .offset(y = Dimens.desplomeAvatarPerfil),
                    borde = Dimens.grosorBordeAvatar,
                    colorBorde = Blanco
                )
            }
            Spacer(Modifier.height(Dimens.desplomeAvatarPerfil + Dimens.salto5))

            Column(Modifier.padding(horizontal = Dimens.margenHorizontalPantalla)) {
                Text(
                    usuarioActual.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextoTitulo()
                )
                Text(
                    usuarioActual.cargo.ifBlank { usuarioActual.rol.nombre },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSecundario()
                )

                Spacer(Modifier.height(Dimens.salto5))
                BotonBordeadoPropio(
                    texto = stringResource(R.string.perfil_menu_editar),
                    habilitado = true,
                    onClick = onEditar
                )

                Spacer(Modifier.height(Dimens.espacioMedio))
                TituloRaya(stringResource(R.string.perfil_sobre_mi))
                Text(
                    if (usuarioActual.bio.isBlank()) stringResource(R.string.perfil_sin_bio) else usuarioActual.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (usuarioActual.bio.isBlank()) TextoSecundario() else TextoPrincipal()
                )

                Spacer(Modifier.height(Dimens.espacioMedio))
                CajaBordeada(Modifier.fillMaxWidth()) {
                    if (esCliente) {
                        FilaEstadistica(
                            stringResource(R.string.perfil_mis_equipos),
                            equiposPlanta.toString()
                        )
                        HorizontalDivider(thickness = Dimens.grosorDivisor, color = BordeSuave())
                        FilaEstadistica(
                            stringResource(R.string.perfil_actas_recibidas),
                            actas.toString()
                        )
                    } else {
                        FilaEstadistica(
                            stringResource(R.string.perfil_protocolos),
                            numeros.protocolos.toString()
                        )
                        HorizontalDivider(thickness = Dimens.grosorDivisor, color = BordeSuave())
                        FilaEstadistica(
                            stringResource(R.string.perfil_equipos),
                            numeros.equiposProbados.toString()
                        )
                        HorizontalDivider(thickness = Dimens.grosorDivisor, color = BordeSuave())
                        FilaEstadistica(
                            stringResource(R.string.perfil_visitas),
                            numeros.visitasCerradas.toString()
                        )
                    }
                    HorizontalDivider(thickness = Dimens.grosorDivisor, color = BordeSuave())
                    val base = if (esCliente) actas else numeros.visitasCerradas
                    val nota = usuarioActual.calificacion
                    if (nota != null && base > 0) {
                        FilaEstadistica(
                            stringResource(R.string.perfil_calificacion),
                            stringResource(R.string.perfil_estrellas, nota)
                        )
                        FilaNota(
                            if (esCliente) R.string.perfil_califica_n_cliente
                            else R.string.perfil_califica_n,
                            base
                        )
                    } else {
                        FilaEstadistica(
                            stringResource(R.string.perfil_calificacion),
                            stringResource(R.string.perfil_sin_calificacion)
                        )
                    }
                }

                Spacer(Modifier.height(Dimens.espacioMedio))
                TituloRaya(stringResource(R.string.perfil_contacto))
                CajaBordeada(Modifier.fillMaxWidth()) {
                    if (esCliente && empresa.isNotBlank()) {
                        FilaDato(stringResource(R.string.perfil_empresa), empresa)
                        HorizontalDivider(thickness = Dimens.grosorDivisor, color = BordeSuave())
                    }
                    FilaDato(stringResource(R.string.perfil_correo), usuarioActual.correo.ifBlank { "—" })
                    HorizontalDivider(thickness = Dimens.grosorDivisor, color = BordeSuave())
                    FilaDato(stringResource(R.string.perfil_telefono), usuarioActual.telefono.ifBlank { "—" })
                    if (usuarioActual.giro.isNotBlank()) {
                        HorizontalDivider(thickness = Dimens.grosorDivisor, color = BordeSuave())
                        FilaDato(stringResource(R.string.perfil_giro), usuarioActual.giro)
                    }
                }

                val especialidades = usuarioActual.listaDeEspecialidades()
                if (especialidades.isNotEmpty()) {
                    Spacer(Modifier.height(Dimens.espacioMedio))
                    TituloRaya(stringResource(R.string.perfil_especialidades))
                    CajaBordeada(Modifier.fillMaxWidth()) {
                        for (posicion in especialidades.indices) {
                            if (posicion > 0)
                                HorizontalDivider(
                                    thickness = Dimens.grosorDivisor,
                                    color = BordeSuave()
                                )
                            FilaEspecialidad(especialidades[posicion])
                        }
                    }
                }

                if (usuarioActual.desde != null || usuarioActual.formacion.isNotBlank() || usuarioActual.certificaciones.isNotBlank()) {
                    Spacer(Modifier.height(Dimens.espacioMedio))
                    TituloRaya(stringResource(R.string.perfil_contacto))
                    CajaBordeada(Modifier.fillMaxWidth()) {
                        if (usuarioActual.desde != null) {
                            FilaDato(stringResource(R.string.perfil_desde), usuarioActual.desde.toString())
                            HorizontalDivider(thickness = Dimens.grosorDivisor, color = BordeSuave())
                        }
                        if (usuarioActual.formacion.isNotBlank()) {
                            FilaDato(stringResource(R.string.perfil_formacion), usuarioActual.formacion)
                            HorizontalDivider(thickness = Dimens.grosorDivisor, color = BordeSuave())
                        }
                        FilaDato(
                            stringResource(R.string.perfil_certificaciones),
                            usuarioActual.certificaciones.ifBlank { "—" }
                        )
                    }
                }

                Spacer(Modifier.height(Dimens.espacioGrande))
            }
        }

        BarraInferior(
            activa = SeccionBarra.PERFIL,
            onIrEquipos = onIrEquipos,
            onIrPerfil = {}
        )
    }
}

@Composable
private fun MenuPerfil(
    onIrConsultas: () -> Unit,
    onSalir: () -> Unit,
    onReiniciar: () -> Unit,
    modifier: Modifier = Modifier
) {
    MenuLineas(
        onSalir = onSalir,
        onReiniciar = onReiniciar,
        modifier = modifier,
        opciones = { cerrar ->
            DropdownMenuItem(
                text = { Text(stringResource(R.string.perfil_consultas)) },
                onClick = { cerrar(); onIrConsultas() }
            )
        }
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun PerfilVistoPreview() {
    InduztekTheme {
        PerfilContenido(
            usuarioActual = personaDePreview(),
            numeros = ResumenPerfil(58, 41, 22),
            esCliente = false,
            equiposPlanta = 0,
            actas = 0,
            empresa = "",
            foto = null,
            banner = null,
            onEditar = {},
            onIrConsultas = {},
            onSalir = {},
            onReiniciar = {},
            onIrEquipos = {}
        )
    }
}
