
package cl.duoc.induztek.data.remote

import cl.duoc.induztek.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class ResultadoRemoto {
    data class Ok(val usuario: UsuarioRemoto) : ResultadoRemoto()
    object ClaveMala : ResultadoRemoto()
    object Suspendido : ResultadoRemoto()
    object SinConexion : ResultadoRemoto()
}

sealed class ResultadoSolicitud {
    object Ok : ResultadoSolicitud()
    object CorreoUsado : ResultadoSolicitud()
    object SinConexion : ResultadoSolicitud()
}

object Remoto {

    private val cliente = OkHttpClient
        .Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(4, TimeUnit.SECONDS)
        .build()

    private val api: ApiInduztek by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInduztek::class.java)
    }

    suspend fun ingresar(correo: String, contrasena: String): ResultadoRemoto {
        return try {
            ResultadoRemoto.Ok(api.ingresar(LoginRemoto(correo, contrasena)))
        } catch (e: HttpException) {
            when (e.code()) {
                403 -> ResultadoRemoto.Suspendido
                400, 401 -> ResultadoRemoto.ClaveMala
                else -> ResultadoRemoto.SinConexion
            }
        } catch (e: IOException) {
            ResultadoRemoto.SinConexion
        }
    }

    suspend fun usuarios(): List<UsuarioRemoto> {
        return try {
            api.usuarios()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun crearSolicitud(datos: SolicitudRemota): ResultadoSolicitud {
        return try {
            api.crearSolicitud(datos)
            ResultadoSolicitud.Ok
        } catch (e: HttpException) {
            if (e.code() == 409) ResultadoSolicitud.CorreoUsado else ResultadoSolicitud.SinConexion
        } catch (e: IOException) {
            ResultadoSolicitud.SinConexion
        }
    }
}
