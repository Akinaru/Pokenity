package fr.pokenity.pokenity.data.remote

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class PokeApiService {

    fun fetchPokemonList(limit: Int, offset: Int): List<PokemonListItemDto> {
        val endpoint = "https://pokeapi.co/api/v2/pokemon?limit=$limit&offset=$offset"
        return fetchNamedResults(endpoint).map { PokemonListItemDto(name = it.name, url = it.url) }
    }

    fun fetchPokemonTypes(): List<NamedResourceDto> {
        return fetchNamedResults("https://pokeapi.co/api/v2/type?limit=100")
    }

    fun fetchPokemonGenerations(): List<NamedResourceDto> {
        return fetchNamedResults("https://pokeapi.co/api/v2/generation?limit=100")
    }

    private fun fetchNamedResults(endpoint: String): List<NamedResourceDto> {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
        }

        return try {
            val statusCode = connection.responseCode
            if (statusCode !in 200..299) {
                error("Erreur API PokeAPI: HTTP $statusCode")
            }

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(response)
            val results = root.getJSONArray("results")
            List(results.length()) { index ->
                val item = results.getJSONObject(index)
                NamedResourceDto(
                    name = item.getString("name"),
                    url = item.getString("url")
                )
            }
        } finally {
            connection.disconnect()
        }
    }
}

data class PokemonListItemDto(
    val name: String,
    val url: String
)

data class NamedResourceDto(
    val name: String,
    val url: String
)
