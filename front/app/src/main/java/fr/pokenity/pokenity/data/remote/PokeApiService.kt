package fr.pokenity.pokenity.data.remote

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class PokeApiService {
    private val localizedNameCache = mutableMapOf<String, String>()

    fun fetchAvailableLanguages(): List<LanguageDto> {
        return fetchNamedResults("https://pokeapi.co/api/v2/language/").map { resource ->
            val json = fetchObject(resource.url)
            val names = json.optJSONArray("names")
            var label = resource.name
            if (names != null) {
                for (i in 0 until names.length()) {
                    val entry = names.getJSONObject(i)
                    if (entry.getJSONObject("language").getString("name") == "en") {
                        label = entry.getString("name")
                        break
                    }
                }
            }
            LanguageDto(code = resource.name, label = label)
        }
    }

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

    fun fetchPokemonRegions(): List<NamedResourceDto> {
        return fetchNamedResults("https://pokeapi.co/api/v2/region?limit=100")
    }

    fun fetchPokemonShapes(): List<NamedResourceDto> {
        return fetchNamedResults("https://pokeapi.co/api/v2/pokemon-shape?limit=100")
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

    fun fetchPokemonByShape(shapeName: String): List<NamedResourceDto> {
        val endpoint = "https://pokeapi.co/api/v2/pokemon-shape/$shapeName"
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

    fun fetchPokemonByRegion(regionName: String): List<NamedResourceDto> {
        val endpoint = "https://pokeapi.co/api/v2/region/$regionName"
        val root = fetchObject(endpoint)
        val pokedexArray = root.getJSONArray("pokedexes")
        val allSpecies = linkedMapOf<String, NamedResourceDto>()

        for (i in 0 until pokedexArray.length()) {
            val pokedexUrl = pokedexArray.getJSONObject(i).getString("url")
            val pokedexRoot = fetchObject(pokedexUrl)
            val entries = pokedexRoot.getJSONArray("pokemon_entries")

            for (j in 0 until entries.length()) {
                val speciesObj = entries.getJSONObject(j).getJSONObject("pokemon_species")
                val name = speciesObj.getString("name")
                allSpecies[name] = NamedResourceDto(
                    name = name,
                    url = speciesObj.getString("url")
                )
            }
        }

        return allSpecies.values.toList()
    }

    fun fetchLocationsByRegion(regionName: String): List<NamedResourceDto> {
        val endpoint = "https://pokeapi.co/api/v2/region/$regionName"
        val root = fetchObject(endpoint)
        val locationsArray = root.getJSONArray("locations")

        return List(locationsArray.length()) { index ->
            val item = locationsArray.getJSONObject(index)
            NamedResourceDto(
                name = item.getString("name"),
                url = item.getString("url")
            )
        }
    }

    fun fetchLocationAreasByLocation(locationName: String): List<NamedResourceDto> {
        val endpoint = "https://pokeapi.co/api/v2/location/$locationName"
        val root = fetchObject(endpoint)
        val areasArray = root.getJSONArray("areas")

        return List(areasArray.length()) { index ->
            val item = areasArray.getJSONObject(index)
            NamedResourceDto(
                name = item.getString("name"),
                url = item.getString("url")
            )
        }
    }

    fun fetchPokemonByLocationArea(locationAreaName: String): List<NamedResourceDto> {
        val endpoint = "https://pokeapi.co/api/v2/location-area/$locationAreaName"
        val root = fetchObject(endpoint)
        val encountersArray = root.getJSONArray("pokemon_encounters")

        return List(encountersArray.length()) { index ->
            val item = encountersArray.getJSONObject(index).getJSONObject("pokemon")
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
                val typeObj = typesArray.getJSONObject(i).getJSONObject("type")
                val typeUrl = typeObj.getString("url")
                val typeId = typeUrl.trimEnd('/').substringAfterLast('/').toInt()
                PokemonTypeDto(
                    name = typeObj.getString("name"),
                    id = typeId,
                    url = typeUrl
                )
            }

            val statsArray = root.getJSONArray("stats")
            val stats = List(statsArray.length()) { i ->
                val statObj = statsArray.getJSONObject(i)
                val statResource = statObj.getJSONObject("stat")
                PokemonStatDto(
                    name = statResource.getString("name"),
                    url = statResource.getString("url"),
                    baseStat = statObj.getInt("base_stat")
                )
            }

            val abilitiesArray = root.getJSONArray("abilities")
            val abilities = List(abilitiesArray.length()) { i ->
                val abilityObj = abilitiesArray.getJSONObject(i).getJSONObject("ability")
                NamedResourceDto(
                    name = abilityObj.getString("name"),
                    url = abilityObj.getString("url")
                )
            }

            // Extract level-up moves, take the 6 highest level ones
            val movesArray = root.getJSONArray("moves")
            val levelUpMoves = mutableListOf<Pair<String, Int>>() // name to level
            for (i in 0 until movesArray.length()) {
                val moveObj = movesArray.getJSONObject(i)
                val details = moveObj.getJSONArray("version_group_details")
                for (j in 0 until details.length()) {
                    val detail = details.getJSONObject(j)
                    val method = detail.getJSONObject("move_learn_method").getString("name")
                    if (method == "level-up") {
                        val level = detail.getInt("level_learned_at")
                        val moveName = moveObj.getJSONObject("move").getString("name")
                        levelUpMoves.add(moveName to level)
                        break
                    }
                }
            }
            val topMoveNames = levelUpMoves
                .sortedByDescending { it.second }
                .take(6)
                .map { it.first }

            PokemonDetailDto(
                id = root.getInt("id"),
                name = root.getString("name"),
                height = root.getInt("height"),
                weight = root.getInt("weight"),
                types = types,
                stats = stats,
                abilities = abilities,
                moveNames = topMoveNames
            )
        } finally {
            connection.disconnect()
        }
    }

    /**
     * Fetches detailed info for a single move (type, description, power, accuracy, pp).
     */
    fun fetchMoveDetail(moveName: String, languageCode: String): MoveDetailDto {
        val root = fetchJson("https://pokeapi.co/api/v2/move/$moveName")

        val typeObj = root.getJSONObject("type")
        val typeUrl = typeObj.getString("url")
        val typeId = typeUrl.trimEnd('/').substringAfterLast('/').toInt()
        val typeName = typeObj.getString("name")

        // Get localized flavor text (fallback to english)
        val flavorEntries = root.getJSONArray("flavor_text_entries")
        var description = ""
        for (i in 0 until flavorEntries.length()) {
            val entry = flavorEntries.getJSONObject(i)
            if (entry.getJSONObject("language").getString("name") == languageCode) {
                description = entry.getString("flavor_text").replace("\n", " ").replace("\u000c", " ")
                break
            }
        }
        if (description.isBlank()) {
            for (i in 0 until flavorEntries.length()) {
                val entry = flavorEntries.getJSONObject(i)
                if (entry.getJSONObject("language").getString("name") == "en") {
                    description = entry.getString("flavor_text").replace("\n", " ").replace("\u000c", " ")
                    break
                }
            }
        }

        var localizedMoveName = root.getString("name")
        val moveNames = root.optJSONArray("names")
        if (moveNames != null) {
            for (i in 0 until moveNames.length()) {
                val entry = moveNames.getJSONObject(i)
                if (entry.getJSONObject("language").getString("name") == languageCode) {
                    localizedMoveName = entry.getString("name")
                    break
                }
            }
        }

        return MoveDetailDto(
            name = localizedMoveName,
            typeName = typeName,
            typeId = typeId,
            description = description,
            power = if (root.isNull("power")) null else root.getInt("power"),
            accuracy = if (root.isNull("accuracy")) null else root.getInt("accuracy"),
            pp = if (root.isNull("pp")) null else root.getInt("pp")
        )
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

    fun fetchLocalizedName(resourceUrl: String, languageCode: String, fallbackName: String): String {
        val key = "$resourceUrl|$languageCode"
        localizedNameCache[key]?.let { return it }

        val root = fetchObject(resourceUrl)
        val names = root.optJSONArray("names")
        var localized = fallbackName

        if (names != null) {
            for (i in 0 until names.length()) {
                val entry = names.getJSONObject(i)
                if (entry.getJSONObject("language").getString("name") == languageCode) {
                    localized = entry.getString("name")
                    break
                }
            }
            if (localized == fallbackName) {
                for (i in 0 until names.length()) {
                    val entry = names.getJSONObject(i)
                    if (entry.getJSONObject("language").getString("name") == "en") {
                        localized = entry.getString("name")
                        break
                    }
                }
            }
        } else if (root.has("name")) {
            localized = root.getString("name")
        }

        localizedNameCache[key] = localized
        return localized
    }

    fun fetchPokemonSpeciesNameById(id: Int, languageCode: String, fallbackName: String): String {
        val speciesUrl = "https://pokeapi.co/api/v2/pokemon-species/$id"
        return fetchLocalizedName(speciesUrl, languageCode, fallbackName)
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

data class LanguageDto(
    val code: String,
    val label: String
)

data class PokemonDetailDto(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val types: List<PokemonTypeDto>,
    val stats: List<PokemonStatDto>,
    val abilities: List<NamedResourceDto>,
    val moveNames: List<String> = emptyList()
)

data class PokemonTypeDto(
    val name: String,
    val id: Int,
    val url: String
)

data class PokemonStatDto(
    val name: String,
    val url: String,
    val baseStat: Int
)

data class MoveDetailDto(
    val name: String,
    val typeName: String,
    val typeId: Int,
    val description: String,
    val power: Int?,
    val accuracy: Int?,
    val pp: Int?
)

data class EvolutionStageDto(
    val id: Int,
    val name: String
)
