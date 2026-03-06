package fr.pokenity.data.remote.box

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

internal interface BoxRetrofitApi {

    @GET("boxes")
    suspend fun getBoxes(): Response<BoxesResponseDto>

    @GET("boxes/{boxId}")
    suspend fun getBoxById(@Path("boxId") boxId: String): Response<BoxResponseDto>

    @POST("boxes/{boxId}/open")
    suspend fun openBox(
        @Header("Authorization") authorization: String,
        @Path("boxId") boxId: String
    ): Response<OpenBoxResponseDto>
}
