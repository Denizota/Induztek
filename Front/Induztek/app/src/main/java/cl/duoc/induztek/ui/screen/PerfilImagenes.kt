
package cl.duoc.induztek.ui.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import cl.duoc.induztek.R
import cl.duoc.induztek.ui.theme.DatoTecnico
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.esModoOscuro
import java.io.File
import java.io.IOException


class ImagenesPerfil(context: Context) {
    private val aplicacion = context.applicationContext
    private val preferencias = aplicacion.getSharedPreferences("perfil_imagenes", Context.MODE_PRIVATE)
    private val carpeta = File(aplicacion.filesDir, "perfil")

    fun archivo(id: Int, tipo: String): File? {
        val ruta = preferencias.getString("$tipo-$id", null)
        if (ruta == null) return null
        val archivo = File(ruta)
        if (archivo.exists()) return archivo
        return null
    }

    fun guardarDesdeTelefono(id: Int, tipo: String, uri: Uri): Boolean {
        val bitmap = decimar(uri, ladoMaximo(tipo))
        if (bitmap == null) return false
        return guardar(id, tipo, bitmap)
    }

    fun guardarDesdeCamara(id: Int, tipo: String, bitmap: Bitmap): Boolean =
        guardar(id, tipo, reducir(bitmap, ladoMaximo(tipo)))

    fun borrar(id: Int, tipo: String) {
        val guardado = archivo(id, tipo)
        if (guardado != null) guardado.delete()
        preferencias.edit { putString("$tipo-$id", null) }
    }

    private fun guardar(id: Int, tipo: String, bitmap: Bitmap): Boolean {
        carpeta.mkdirs()
        val destino = File(carpeta, "${tipo}_$id.jpg")
        try {
            val salida = destino.outputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, salida)
            salida.close()
            preferencias.edit { putString("$tipo-$id", destino.absolutePath) }
            return true
        } catch (e: IOException) {
            return false
        }
    }

    private fun decimar(uri: Uri, lado: Int): Bitmap? {
        val medidas = BitmapFactory.Options()
        medidas.inJustDecodeBounds = true
        try {
            val entrada = aplicacion.contentResolver.openInputStream(uri)
            if (entrada != null) {
                BitmapFactory.decodeStream(entrada, null, medidas)
                entrada.close()
            }
        } catch (e: IOException) {
            return null
        }
        if (medidas.outWidth <= 0 || medidas.outHeight <= 0) return null

        val opciones = BitmapFactory.Options()
        opciones.inSampleSize = mayorPotenciaDeDos(medidas.outWidth, medidas.outHeight, lado)

        var crudo: Bitmap? = null
        try {
            val entrada = aplicacion.contentResolver.openInputStream(uri)
            if (entrada != null) {
                crudo = BitmapFactory.decodeStream(entrada, null, opciones)
                entrada.close()
            }
        } catch (e: IOException) {
            crudo = null
        }
        if (crudo == null) return null

        return reducir(girar(uri, crudo), lado)
    }

    private fun girar(uri: Uri, bitmap: Bitmap): Bitmap {
        var angulo = 0
        try {
            val cursor = aplicacion.contentResolver.query(
                uri, arrayOf(MediaStore.MediaColumns.ORIENTATION), null, null, null
            )
            if (cursor != null) {
                if (cursor.moveToFirst()) angulo = cursor.getInt(0)
                cursor.close()
            }
        } catch (e: Exception) {
            angulo = 0
        }
        if (angulo == 0) return bitmap
        val matriz = Matrix()
        matriz.postRotate(angulo.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matriz, true)
    }

    private fun ladoMaximo(tipo: String) = if (tipo == TIPO_BANNER) 1200 else 420

    private fun mayorPotenciaDeDos(ancho: Int, alto: Int, lado: Int): Int {
        var factor = 1
        while (ancho / (factor * 2) >= lado || alto / (factor * 2) >= lado) factor *= 2
        return factor
    }

    private fun reducir(bitmap: Bitmap, lado: Int): Bitmap {
        val mayor = maxOf(bitmap.width, bitmap.height)
        if (mayor <= lado) return bitmap
        val escala = lado.toFloat() / mayor.toFloat()
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * escala).toInt().coerceAtLeast(1),
            (bitmap.height * escala).toInt().coerceAtLeast(1),
            true
        )
    }

    companion object {
        const val TIPO_FOTO = "foto"
        const val TIPO_BANNER = "banner"
    }
}

fun inicialesDe(nombre: String): String {
    val partes = nombre.trim().split(Regex("\\s+"))
    val conNombre = mutableListOf<String>()
    for (parte in partes) {
        val limpia = parte.trim('.', ',', '-')
        if (limpia.length > 1) conNombre.add(limpia)
    }

    val partesDelNombre = conNombre
    if (partesDelNombre.isEmpty()) {
        for (parte in partes) {
            if (parte.isNotBlank()) partesDelNombre.add(parte)
        }
    }
    if (partesDelNombre.isEmpty()) return "?"

    val primera = partesDelNombre[0].first().uppercaseChar()
    val ultima = partesDelNombre[partesDelNombre.size - 1].first().uppercaseChar()
    if (primera == ultima) return "$primera"
    return "$primera$ultima"
}

@Composable
fun AvatarPersona(
    nombre: String,
    foto: File?,
    lado: Dp,
    modifier: Modifier = Modifier,
    radio: Dp = Dimens.radioBotonChico,
    borde: Dp = 0.dp,
    colorBorde: Color = Color.Transparent
) {
    val forma = RoundedCornerShape(radio)
    val fondo = if (esModoOscuro()) Color(0xFF39434C) else Color(0xFFD7DDE3)
    val tinta = if (esModoOscuro()) Color(0xFFE4EAF0) else Color(0xFF3E4A55)
    Box(
        modifier
            .size(lado)
            .clip(forma)
            .background(fondo)
            .then(if (borde > 0.dp) Modifier.border(borde, colorBorde, forma) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (foto != null) {
            Image(
                painter = painterImagen(foto, R.drawable.ic_perfil),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                inicialesDe(nombre),
                style = DatoTecnico.copy(
                    fontSize = (lado.value * 0.36f).sp,
                    fontWeight = FontWeight.Bold
                ),
                color = tinta,
                maxLines = 1
            )
        }
    }
}

@Composable
fun BannerUsuario(archivo: File?, modifier: Modifier = Modifier) {
    Box(
        modifier.background(if (esModoOscuro()) Color(0xFF242A30) else Color(0xFF4B5158))
    ) {
        if (archivo != null) {
            Image(
                painter = painterImagen(archivo, R.drawable.banner_inicio),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xFF07131C).copy(alpha = Dimens.opacidadVeloFoto))
            )
        }
    }
}

@Composable
fun painterImagen(archivo: File?, recurso: Int): Painter {
    var ruta = ""
    var modificado = 0L
    if (archivo != null) {
        ruta = archivo.absolutePath
        modificado = archivo.lastModified()
    }
    val local = remember(ruta, modificado) {
        var dibujo: ImageBitmap? = null
        if (archivo != null) {
            try {
                val decodificado = BitmapFactory.decodeFile(archivo.absolutePath)
                if (decodificado != null) dibujo = decodificado.asImageBitmap()
            } catch (e: Exception) {
                dibujo = null
            }
        }
        dibujo
    }
    if (local != null) return BitmapPainter(local)
    return painterResource(id = recurso)
}

@Composable
fun imagenGuardada(idUsuario: Int, tipo: String): File? {
    val contexto = LocalContext.current
    val guardado = remember(contexto) { ImagenesPerfil(contexto) }
    return guardado.archivo(idUsuario, tipo)
}
