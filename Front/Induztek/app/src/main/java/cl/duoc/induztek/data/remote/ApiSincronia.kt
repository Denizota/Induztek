package cl.duoc.induztek.data.remote

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiSincronia {

    @GET("api/usuarios")
    fun usuarios(): Call<List<UsuarioRemoto>>

    @POST("api/usuarios")
    fun registrar(@Body datos: RegistroApiRequest): Call<UsuarioRemoto>

    @PUT("api/usuarios/{id}")
    fun actualizar(@Path("id") id: Int, @Body datos: ActualizarApiRequest): Call<UsuarioRemoto>

    @PUT("api/usuarios/{id}/activo")
    fun activo(@Path("id") id: Int, @Body datos: ActivoApiRequest): Call<UsuarioRemoto>

    @GET("api/solicitudes")
    fun solicitudes(): Call<List<SolicitudApi>>

    @POST("api/solicitudes")
    fun crearSolicitud(@Body datos: SolicitudApiRequest): Call<SolicitudApi>

    @PUT("api/solicitudes/{id}/estado")
    fun estadoSolicitud(
        @Path("id") id: Int,
        @Body datos: EstadoSolicitudApiRequest
    ): Call<SolicitudApi>

    @GET("api/visitas")
    fun visitas(): Call<List<VisitaApi>>

    @GET("api/visitas/{id}/detalle")
    fun visitaDetalle(@Path("id") id: Int): Call<VisitaDetalleApi>

    @POST("api/visitas")
    fun crearVisita(@Body datos: VisitaApiRequest): Call<VisitaApi>

    @POST("api/visitas/{id}/participantes")
    fun participante(
        @Path("id") id: Int,
        @Body datos: ParticipanteApiRequest
    ): Call<List<Int>>

    @PUT("api/visitas/{id}/cierre")
    fun cerrarVisita(@Path("id") id: Int, @Body datos: CierreApiRequest): Call<RecepcionApi>

    @GET("api/protocolos")
    fun protocolos(@Query("visita") visita: Int? = null, @Query("equipo") equipo: Int? = null): Call<List<ProtocoloApi>>

    @GET("api/protocolos/{id}/detalle")
    fun protocoloDetalle(@Path("id") id: Int): Call<ProtocoloDetalleApi>

    @POST("api/protocolos")
    fun crearProtocolo(@Body datos: ProtocoloApiRequest): Call<ProtocoloApi>

    @PUT("api/protocolos/{id}/ambientes")
    fun ambientes(@Path("id") id: Int, @Body datos: AmbientesApiRequest): Call<ProtocoloApi>

    @POST("api/protocolos/{id}/pruebas")
    fun guardarPrueba(@Path("id") id: Int, @Body datos: PruebaApiRequest): Call<PruebaApi>

    @PUT("api/protocolos/{id}/estado")
    fun estadoProtocolo(
        @Path("id") id: Int,
        @Body datos: EstadoProtocoloApiRequest
    ): Call<ProtocoloApi>

    @GET("api/equipos")
    fun equipos(): Call<List<EquipoApi>>

    @GET("api/instalaciones")
    fun instalaciones(): Call<List<InstalacionApi>>

    @GET("api/instrumentos")
    fun instrumentos(): Call<List<InstrumentoApi>>

    @GET("api/tipos-equipo")
    fun tiposEquipo(): Call<List<TipoEquipoApi>>

    @GET("api/tipos-prueba")
    fun tiposPrueba(): Call<List<TipoPruebaApi>>
}
