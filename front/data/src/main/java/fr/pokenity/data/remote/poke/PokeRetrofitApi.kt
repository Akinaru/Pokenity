package fr.pokenity.data.remote.poke

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * Interface Retrofit minimaliste: une seule méthode générique qui renvoie le body brut.
 * Le parsing reste géré manuellement via JSONObject dans PokeApiService.
 */
internal interface PokeRetrofitApi {

    @GET
    suspend fun getJson(@Url url: String): Response<ResponseBody>
}
