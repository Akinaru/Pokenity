package fr.pokenity.data.remote.auth

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

internal interface AuthRetrofitApi {

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequestBody): Response<AuthSessionDto>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequestBody): Response<AuthSessionDto>

    @GET("auth/me")
    suspend fun me(@Header("Authorization") authorization: String): Response<AuthMeResponseDto>

    @GET("characters")
    suspend fun characters(): Response<AuthCharactersResponseDto>
}
