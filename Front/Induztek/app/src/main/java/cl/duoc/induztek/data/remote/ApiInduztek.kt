
package cl.duoc.induztek.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiInduztek {

    @POST("api/usuarios/login")
    suspend fun ingresar(@Body datos: LoginRemoto): UsuarioRemoto

    @GET("api/usuarios")
    suspend fun usuarios(): List<UsuarioRemoto>

    @POST("api/solicitudes")
    suspend fun crearSolicitud(@Body datos: SolicitudRemota): SolicitudApi
}
