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

    fun fetchPokemonAbilities(): List<NamedResourceDto> {
        return fetchNamedResults("https://pokeapi.co/api/v2/ability?limit=1000")
    }

    fun fetchPokemonHabitats(): List<NamedResourceDto> {
        return fetchNamedResults("https://pokeapi.co/api/v2/pokemon-habitat?limit=100")
    }

    fun fetchPokemonByType(typeName: String): List<NamedResourceDto> {
        val endpoint = "https://pokeapi.co/api/v2/type/$typeName"
        val root = fetchObject(endpoint)
        val pokemonArray = root.getJSONArray("pokemon")

        return List(pokemonArray.length()) { index ->
            val item = pokemonArray.getJSONObject(index).getJSONObject("pokemon")
            NamedResourceDto(
                name = item.getString("name"),
                url = item.getString("url")
            )
        }
    }

    fun fetchPokemonByGeneration(generationName: String): List<NamedResourceDto> {
        val endpoint = "https://pokeapi.co/api/v2/generation/$generationName"
        val root = fetchObject(endpoint)
        val speciesArray = root.getJSONArray("pokemon_species")

        return List(speciesArray.length()) { index ->
            val item = speciesArray.getJSONObject(index)
            NamedResourceDto(
                name = item.getString("name"),
                url = item.getString("url")
            )
        }
    }

    fun fetchPokemonByAbility(abilityName: String): List<NamedResourceDto> {
        val endpoint = "https://pokeapi.co/api/v2/ability/$abilityName"
        val root = fetchObject(endpoint)
        val pokemonArray = root.getJSONArray("pokemon")

        return List(pokemonArray.length()) { index ->
            val item = pokemonArray.getJSONObject(index).getJSONObject("pokemon")
            NamedResourceDto(
                name = item.getString("name"),
                url = item.getString("url")
            )
        }
    }

    fun fetchPokemonByHabitat(habitatName: String): List<NamedResourceDto> {
        val endpoint = "https://pokeapi.co/api/v2/pokemon-habitat/$habitatName"
        val root = fetchObject(endpoint)
        val speciesArray = root.getJSONArray("pokemon_species")

        return List(speciesArray.length()) { index ->
            val item = speciesArray.getJSONObject(index)
            NamedResourceDto(
                name = item.getString("name"),
                url = item.getString("url")
            )
        }
    }

    private fun fetchNamedResults(endpoint: String): List<NamedResourceDto> {
        val root = fetchObject(endpoint)
        val results = root.getJSONArray("results")
        return List(results.length()) { index ->
            val item = results.getJSONObject(index)
            NamedResourceDto(
                name = item.getString("name"),
                url = item.getString("url")
            )
        }
    }

    private fun fetchObject(endpoint: String): JSONObject {
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
            JSONObject(response)
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
