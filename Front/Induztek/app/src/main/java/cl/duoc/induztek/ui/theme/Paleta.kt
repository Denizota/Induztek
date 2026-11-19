package cl.duoc.induztek.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color


data class Paleta(
    val textoGris: Color,
    val textoPlaceholder: Color,
    val textoOlvido: Color,
    val bordeCampo: Color,
    val fondoCampo: Color,
    val iconoCampo: Color,
    val botonDeshabilitado: Color,
    val fondoRedSocial: Color,
    val fondoPantalla: Color,
    val primario: Color,
    val textoOscuro: Color,
    val moraditoFin: Color,
    val fonditoMorado: Color,
    val lavandaPrimario: Color,
    val lavandaSuave: Color,
    val lavandaFondo: Color,
    val lavandaBorde: Color,
    val verdeAprobado: Color,
    val naranjaObservado: Color,
    val rojoRechazado: Color,
    val grisTexto: Color,
    val fondoFilaTabla: Color,
    val lavandaActa: Color,
    val moradoFondo: Color
)

val PaletaClara = Paleta(
    textoGris = TextoGrisClaro,
    textoPlaceholder = TextoPlaceholderClaro,
    textoOlvido = TextoOlvidoClaro,
    bordeCampo = BordeCampoClaro,
    fondoCampo = FondoCampoClaro,
    iconoCampo = IconoCampoClaro,
    botonDeshabilitado = BotonDeshabilitadoClaro,
    fondoRedSocial = FondoRedSocialClaro,
    fondoPantalla = FondoPantallaClaro,
    primario = PrimarioClaro,
    textoOscuro = TextoOscuroClaro,
    moraditoFin = MoraditoFinClaro,
    fonditoMorado = fonditoMoradoClaro,
    lavandaPrimario = LavandaPrimarioClaro,
    lavandaSuave = LavandaSuaveClaro,
    lavandaFondo = LavandaFondoClaro,
    lavandaBorde = LavandaBordeClaro,
    verdeAprobado = VerdeAprobadoClaro,
    naranjaObservado = NaranjaObservadoClaro,
    rojoRechazado = RojoRechazadoClaro,
    grisTexto = GrisTextoClaro,
    fondoFilaTabla = FondoFilaTablaClaro,
    lavandaActa = LavandaActaClaro,
    moradoFondo = MoradoFondoClaro
)


val PaletaOscura = Paleta(
    textoGris = TextoGrisOscuro,
    textoPlaceholder = TextoPlaceholderOscuro,
    textoOlvido = TextoOlvidoOscuro,
    bordeCampo = BordeCampoOscuro,
    fondoCampo = FondoCampoOscuro,
    iconoCampo = IconoCampoOscuro,
    botonDeshabilitado = BotonDeshabilitadoOscuro,
    fondoRedSocial = FondoRedSocialOscuro,
    fondoPantalla = FondoPantallaOscuro,
    primario = PrimarioOscuro,
    textoOscuro = TextoOscuroOscuro,
    moraditoFin = MoraditoFinOscuro,
    fonditoMorado = fonditoMoradoOscuro,
    lavandaPrimario = LavandaPrimarioOscuro,
    lavandaSuave = LavandaSuaveOscuro,
    lavandaFondo = LavandaFondoOscuro,
    lavandaBorde = LavandaBordeOscuro,
    verdeAprobado = VerdeAprobadoOscuro,
    naranjaObservado = NaranjaObservadoOscuro,
    rojoRechazado = RojoRechazadoOscuro,
    grisTexto = GrisTextoOscuro,
    fondoFilaTabla = FondoFilaTablaOscuro,
    lavandaActa = LavandaActaOscuro,
    moradoFondo = MoradoFondoOscuro
)

val LocalPaleta = staticCompositionLocalOf { PaletaClara }

private val paletaActual: Paleta
    @Composable get() = LocalPaleta.current

@Composable
fun esModoOscuro(): Boolean = LocalPaleta.current === PaletaOscura

@Composable fun TextoGris(): Color = paletaActual.textoGris
@Composable fun TextoPlaceholder(): Color = paletaActual.textoPlaceholder
@Composable fun TextoOlvido(): Color = paletaActual.textoOlvido
@Composable fun BordeCampo(): Color = paletaActual.bordeCampo
@Composable fun FondoCampo(): Color = paletaActual.fondoCampo
@Composable fun IconoCampo(): Color = paletaActual.iconoCampo
@Composable fun BotonDeshabilitado(): Color = paletaActual.botonDeshabilitado
@Composable fun FondoRedSocial(): Color = paletaActual.fondoRedSocial
@Composable fun FondoPantalla(): Color = paletaActual.fondoPantalla
@Composable fun Primario(): Color = paletaActual.primario
@Composable fun TextoOscuro(): Color = paletaActual.textoOscuro
@Composable fun MoraditoClaro(): Color = paletaActual.moraditoFin
@Composable fun fonditoMorado(): Color = paletaActual.fonditoMorado
@Composable fun LavandaPrimario(): Color = paletaActual.lavandaPrimario
@Composable fun LavandaSuave(): Color = paletaActual.lavandaSuave
@Composable fun LavandaFondo(): Color = paletaActual.lavandaFondo
@Composable fun LavandaBorde(): Color = paletaActual.lavandaBorde
@Composable fun VerdeAprobado(): Color = paletaActual.verdeAprobado
@Composable fun NaranjaObservado(): Color = paletaActual.naranjaObservado
@Composable fun RojoRechazado(): Color = paletaActual.rojoRechazado
@Composable fun GrisTexto(): Color = paletaActual.grisTexto
@Composable fun FondoFilaTabla(): Color = paletaActual.fondoFilaTabla
@Composable fun LavandaActa(): Color = paletaActual.lavandaActa
@Composable fun MoradoFondo(): Color = paletaActual.moradoFondo
