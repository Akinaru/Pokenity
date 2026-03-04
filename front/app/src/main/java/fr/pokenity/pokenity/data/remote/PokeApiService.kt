package fr.pokenity.pokenity.data.remote

import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

class PokeApiService {

    fun fetchPokemonList(limit: Int, offset: Int): List<PokemonListItemDto> {
        val endpoint = "https://pokeapi.co/api/v2/pokemon?limit=$limit&offset=$offset"
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

            val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)
            parsePokemonList(response)
        } finally {
            connection.disconnect()
        }
    }

    private fun parsePokemonList(json: String): List<PokemonListItemDto> {
        val root = JSONObject(json)
        val results = root.getJSONArray("results")
        return List(results.length()) { index ->
            val item = results.getJSONObject(index)
            PokemonListItemDto(
                name = item.getString("name"),
                url = item.getString("url")
            )
        }
    }
}

data class PokemonListItemDto(
    val name: String,
    val url: String
)
