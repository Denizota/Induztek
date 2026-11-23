package cl.duoc.induztek.data.remote

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cl.duoc.induztek.BuildConfig
import cl.duoc.induztek.data.InduztekDbHelper
import cl.duoc.induztek.data.PaqueteRemoto
import cl.duoc.induztek.data.SubidaRemota
import cl.duoc.induztek.data.gsonCambios
import cl.duoc.induztek.data.mezclarPaquete
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object Sincronia {

    private val cliente = OkHttpClient
        .Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .build()

    private val api: ApiSincronia by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiSincronia::class.java)
    }

    private val ejecutor = Executors.newSingleThreadExecutor()

    private lateinit var baseDatos: InduztekDbHelper
    private var arrancado = false
    private var ultimaHuella = ""

    var version by mutableStateOf(0L)
        private set

    @Volatile
    var enLinea = false
        private set

    private class CorteDescarga : Exception()

    fun iniciar(contexto: Context) {
        if (arrancado) return
        arrancado = true
        baseDatos = InduztekDbHelper.obtener(contexto.applicationContext)
        val hilo = Thread {
            while (true) {
                try {
                    sincronizar()
                } catch (error: Exception) {
                    enLinea = false
                }
                try {
                    Thread.sleep(3000)
                } catch (error: InterruptedException) {
                    return@Thread
                }
            }
        }
        hilo.isDaemon = true
        hilo.start()
    }

    fun sincronizarAhora() {
        if (!arrancado) return
        ejecutor.execute {
            try {
                sincronizar()
            } catch (error: Exception) {
                enLinea = false
            }
        }
    }

    @Synchronized
    private fun sincronizar() {
        val subida = SubidaRemota(baseDatos, api)
        subida.drenar()

        val paquete = descargar()
        if (paquete == null) {
            enLinea = false
            return
        }
        baseDatos.mezclarPaquete(paquete)

        val huella = gsonCambios.toJson(paquete)
        if (huella != ultimaHuella) {
            ultimaHuella = huella
            version = version + 1
        }
        enLinea = true
    }

    private fun descargar(): PaqueteRemoto? {
        return try {
            val usuarios = traer(api.usuarios())
            val instalaciones = traer(api.instalaciones())
            val tiposEquipo = traer(api.tiposEquipo())
            val tiposPrueba = traer(api.tiposPrueba())
            val instrumentos = traer(api.instrumentos())
            val equipos = traer(api.equipos())
            val solicitudes = traer(api.solicitudes())
            val visitas = traer(api.visitas())

            val detallesVisita = mutableMapOf<Int, VisitaDetalleApi>()
            for (visita in visitas) {
                val id = visita.id ?: continue
                val detalle = traerOpcional(api.visitaDetalle(id)) ?: continue
                detallesVisita[id] = detalle
            }

            val protocolos = traer(api.protocolos())
            val detallesProtocolo = mutableMapOf<Int, ProtocoloDetalleApi>()
            for (protocolo in protocolos) {
                val id = protocolo.id ?: continue
                val detalle = traerOpcional(api.protocoloDetalle(id)) ?: continue
                detallesProtocolo[id] = detalle
            }

            PaqueteRemoto(
                usuarios = usuarios,
                solicitudes = solicitudes,
                visitas = visitas,
                detallesVisita = detallesVisita,
                protocolos = protocolos,
                detallesProtocolo = detallesProtocolo,
                equipos = equipos,
                instalaciones = instalaciones,
                instrumentos = instrumentos,
                tiposEquipo = tiposEquipo,
                tiposPrueba = tiposPrueba
            )
        } catch (error: CorteDescarga) {
            null
        }
    }

    private fun <T> traer(llamada: Call<T>): T {
        try {
            val cuerpo = llamada.execute().body()
            if (cuerpo == null) throw CorteDescarga()
            return cuerpo
        } catch (error: IOException) {
            throw CorteDescarga()
        } catch (error: HttpException) {
            throw CorteDescarga()
        }
    }

    private fun <T> traerOpcional(llamada: Call<T>): T? {
        return try {
            llamada.execute().body()
        } catch (error: Exception) {
            null
        }
    }
}
