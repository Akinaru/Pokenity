package fr.pokenity.data.remote.auth

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

internal interface AuthRetrofitApi {

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequestBody): Response<AuthSessionDto>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequestBody): Response<AuthSessionDto>

    @GET("auth/email-exists")
    suspend fun emailExists(@Query("email") email: String): Response<AuthEmailExistsResponseDto>

    @GET("users")
    suspend fun users(): Response<AuthUsersResponseDto>

    @GET("auth/me")
    suspend fun me(@Header("Authorization") authorization: String): Response<AuthMeResponseDto>

    @GET("inventory/me")
    suspend fun inventory(@Header("Authorization") authorization: String): Response<AuthInventoryResponseDto>

    @GET("characters")
    suspend fun characters(): Response<AuthCharactersResponseDto>

    // --- Trades ---

    @GET("trades/open")
    suspend fun getOpenTrades(
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int? = null
    ): Response<TradeListResponseDto>

    @GET("trades/me")
    suspend fun getMyTrades(
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int? = null
    ): Response<TradeListResponseDto>

    @GET("trades/{tradeId}")
    suspend fun getTradeDetail(
        @Header("Authorization") authorization: String,
        @Path("tradeId") tradeId: String
    ): Response<TradeResponseDto>

    @POST("trades")
    suspend fun createTrade(
        @Header("Authorization") authorization: String,
        @Body body: CreateTradeRequestBody
    ): Response<TradeResponseDto>

    @POST("trades/{tradeId}/accept")
    suspend fun acceptTrade(
        @Header("Authorization") authorization: String,
        @Path("tradeId") tradeId: String,
        @Body body: AcceptTradeRequestBody
    ): Response<TradeResponseDto>

    @POST("trades/{tradeId}/confirm")
    suspend fun confirmTrade(
        @Header("Authorization") authorization: String,
        @Path("tradeId") tradeId: String
    ): Response<TradeResponseDto>

    @POST("trades/{tradeId}/cancel")
    suspend fun cancelTrade(
        @Header("Authorization") authorization: String,
        @Path("tradeId") tradeId: String
    ): Response<TradeResponseDto>

    @POST("trades/{tradeId}/decline")
    suspend fun declineTrade(
        @Header("Authorization") authorization: String,
        @Path("tradeId") tradeId: String
    ): Response<TradeResponseDto>

    // --- Users ---

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: String): Response<UserDetailResponseDto>

    // --- Inventory ---

    @GET("inventory/me")
    suspend fun getMyInventory(
        @Header("Authorization") authorization: String
    ): Response<InventoryResponseDto>

    @GET("inventory/users/{userId}")
    suspend fun getUserInventory(
        @Path("userId") userId: String
    ): Response<InventoryResponseDto>
}
