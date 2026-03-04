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

    fun fetchPokemonDetail(id: Int): PokemonDetailDto {
        val endpoint = "https://pokeapi.co/api/v2/pokemon/$id"
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

            val typesArray = root.getJSONArray("types")
            val types = List(typesArray.length()) { i ->
                typesArray.getJSONObject(i).getJSONObject("type").getString("name")
            }

            val statsArray = root.getJSONArray("stats")
            val stats = List(statsArray.length()) { i ->
                val statObj = statsArray.getJSONObject(i)
                PokemonStatDto(
                    name = statObj.getJSONObject("stat").getString("name"),
                    baseStat = statObj.getInt("base_stat")
                )
            }

            val abilitiesArray = root.getJSONArray("abilities")
            val abilities = List(abilitiesArray.length()) { i ->
                abilitiesArray.getJSONObject(i).getJSONObject("ability").getString("name")
            }

            PokemonDetailDto(
                id = root.getInt("id"),
                name = root.getString("name"),
                height = root.getInt("height"),
                weight = root.getInt("weight"),
                types = types,
                stats = stats,
                abilities = abilities
            )
        } finally {
            connection.disconnect()
        }
    }

    /**
     * Fetches the evolution chain for a given Pokémon.
     * 1) GET /pokemon-species/{id} → extract evolution_chain.url
     * 2) GET that URL → parse the recursive chain into a flat list
     */
    fun fetchEvolutionChain(pokemonId: Int): List<EvolutionStageDto> {
        // Step 1: get species to find evolution chain URL
        val speciesJson = fetchJson("https://pokeapi.co/api/v2/pokemon-species/$pokemonId")
        val chainUrl = speciesJson.getJSONObject("evolution_chain").getString("url")

        // Step 2: get evolution chain
        val chainJson = fetchJson(chainUrl)
        val chain = chainJson.getJSONObject("chain")

        // Step 3: flatten the recursive structure
        val stages = mutableListOf<EvolutionStageDto>()
        flattenChain(chain, stages)
        return stages
    }

    private fun flattenChain(node: JSONObject, out: MutableList<EvolutionStageDto>) {
        val speciesObj = node.getJSONObject("species")
        val speciesUrl = speciesObj.getString("url")
        // Extract ID from URL: .../pokemon-species/25/
        val id = speciesUrl.trimEnd('/').substringAfterLast('/').toInt()
        val name = speciesObj.getString("name")
        out.add(EvolutionStageDto(id = id, name = name))

        val evolvesTo = node.getJSONArray("evolves_to")
        for (i in 0 until evolvesTo.length()) {
            flattenChain(evolvesTo.getJSONObject(i), out)
        }
    }

    private fun fetchJson(endpoint: String): JSONObject {
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

data class PokemonDetailDto(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val types: List<String>,
    val stats: List<PokemonStatDto>,
    val abilities: List<String>
)

data class PokemonStatDto(
    val name: String,
    val baseStat: Int
)

data class EvolutionStageDto(
    val id: Int,
    val name: String
)
