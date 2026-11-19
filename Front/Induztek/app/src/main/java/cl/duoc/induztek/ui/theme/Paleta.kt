package cl.duoc.induztek.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class Paleta(
    val textoTitulo: Color,
    val textoPlaceholder: Color,
    val textoOlvidado: Color,
    val bordeCampo: Color,
    val fondoCampo: Color,
    val iconoCampo: Color,
    val botonDeshabilitado: Color,
    val fondoPantalla: Color,
    val azulMarca: Color,
    val textoPrincipal: Color,
    val azulAcero: Color,
    val fondoSuave: Color,
    val bordeSuave: Color,
    val verdeAprobado: Color,
    val naranjaObservado: Color,
    val rojoRechazado: Color,
    val textoSecundario: Color,
    val fondoFilaTabla: Color,
    val fondoActa: Color,
    val acento: Color
)

val PaletaClara = Paleta(
    textoTitulo = TextoTituloClaro,
    textoPlaceholder = TextoPlaceholderClaro,
    textoOlvidado = TextoOlvidadoClaro,
    bordeCampo = BordeCampoClaro,
    fondoCampo = FondoCampoClaro,
    iconoCampo = IconoCampoClaro,
    botonDeshabilitado = BotonDeshabilitadoClaro,
    fondoPantalla = FondoPantallaClaro,
    azulMarca = AzulMarcaClaro,
    textoPrincipal = TextoPrincipalClaro,
    azulAcero = AzulAceroClaro,
    fondoSuave = FondoSuaveClaro,
    bordeSuave = BordeSuaveClaro,
    verdeAprobado = VerdeAprobadoClaro,
    naranjaObservado = NaranjaObservadoClaro,
    rojoRechazado = RojoRechazadoClaro,
    textoSecundario = TextoSecundarioClaro,
    fondoFilaTabla = FondoFilaTablaClaro,
    fondoActa = FondoActaClaro,
    acento = AcentoClaro
)

val PaletaOscura = Paleta(
    textoTitulo = TextoTituloOscuro,
    textoPlaceholder = TextoPlaceholderOscuro,
    textoOlvidado = TextoOlvidadoOscuro,
    bordeCampo = BordeCampoOscuro,
    fondoCampo = FondoCampoOscuro,
    iconoCampo = IconoCampoOscuro,
    botonDeshabilitado = BotonDeshabilitadoOscuro,
    fondoPantalla = FondoPantallaOscuro,
    azulMarca = AzulMarcaOscuro,
    textoPrincipal = TextoPrincipalOscuro,
    azulAcero = AzulAceroOscuro,
    fondoSuave = FondoSuaveOscuro,
    bordeSuave = BordeSuaveOscuro,
    verdeAprobado = VerdeAprobadoOscuro,
    naranjaObservado = NaranjaObservadoOscuro,
    rojoRechazado = RojoRechazadoOscuro,
    textoSecundario = TextoSecundarioOscuro,
    fondoFilaTabla = FondoFilaTablaOscuro,
    fondoActa = FondoActaOscuro,
    acento = AcentoOscuro
)

val LocalPaleta = staticCompositionLocalOf { PaletaClara }

private val paletaActual: Paleta
    @Composable get() = LocalPaleta.current

@Composable
fun esModoOscuro(): Boolean = LocalPaleta.current === PaletaOscura

@Composable fun TextoTitulo(): Color = paletaActual.textoTitulo
@Composable fun TextoPlaceholder(): Color = paletaActual.textoPlaceholder
@Composable fun TextoOlvidado(): Color = paletaActual.textoOlvidado
@Composable fun BordeCampo(): Color = paletaActual.bordeCampo
@Composable fun FondoCampo(): Color = paletaActual.fondoCampo
@Composable fun IconoCampo(): Color = paletaActual.iconoCampo
@Composable fun BotonDeshabilitado(): Color = paletaActual.botonDeshabilitado
@Composable fun FondoPantalla(): Color = paletaActual.fondoPantalla
@Composable fun AzulMarca(): Color = paletaActual.azulMarca
@Composable fun TextoPrincipal(): Color = paletaActual.textoPrincipal
@Composable fun AzulAcero(): Color = paletaActual.azulAcero
@Composable fun FondoSuave(): Color = paletaActual.fondoSuave
@Composable fun BordeSuave(): Color = paletaActual.bordeSuave
@Composable fun VerdeAprobado(): Color = paletaActual.verdeAprobado
@Composable fun NaranjaObservado(): Color = paletaActual.naranjaObservado
@Composable fun RojoRechazado(): Color = paletaActual.rojoRechazado
@Composable fun TextoSecundario(): Color = paletaActual.textoSecundario
@Composable fun FondoFilaTabla(): Color = paletaActual.fondoFilaTabla
@Composable fun FondoActa(): Color = paletaActual.fondoActa
@Composable fun Acento(): Color = paletaActual.acento
