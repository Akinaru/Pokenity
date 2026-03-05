package fr.pokenity.data.remote.poke

import retrofit2.Response

/**
 * Service PokeAPI basé sur Retrofit avec parsing Gson.
 * Toutes les fonctions réseau sont `suspend` et utilisent .parse() pour extraire le body typé.
 * Les DTOs de réponse bruts (XxxResponseDto) sont mappés vers les DTOs domaine ici.
 */
internal class PokeApiService(
    private val api: PokeRetrofitApi
) {
    private val localizedNameCache = mutableMapOf<String, String>()

    // ------------------------------------------------------------------
    // Extension helper — uniform error handling
    // ------------------------------------------------------------------

    private fun <T> Response<T>.parse(): T {
        if (isSuccessful) return body() ?: error("Reponse vide de PokeAPI (HTTP ${code()})")
        error("Erreur API PokeAPI: HTTP ${code()}")
    }

    // ------------------------------------------------------------------
    // Languages
    // ------------------------------------------------------------------

    suspend fun fetchAvailableLanguages(): List<LanguageDto> {
        val page = api.getLanguages().parse()
        return page.results.map { resource ->
            val detail = api.getLanguage(resource.name).parse()
            val label = detail.names
                ?.firstOrNull { it.language.name == "en" }
                ?.name
                ?: resource.name
            LanguageDto(code = resource.name, label = label)
        }
    }

    // ------------------------------------------------------------------
    // Pokemon list
    // ------------------------------------------------------------------

    suspend fun fetchPokemonList(limit: Int, offset: Int): List<PokemonListItemDto> =
        api.getPokemons(limit, offset).parse().results

    // ------------------------------------------------------------------
    // Filter lists (returns NamedResourceDto for further use)
    // ------------------------------------------------------------------

    suspend fun fetchPokemonTypes(): List<NamedResourceDto> =
        api.getTypes().parse().results

    suspend fun fetchPokemonGenerations(): List<NamedResourceDto> =
        api.getGenerations().parse().results

    suspend fun fetchPokemonAbilities(): List<NamedResourceDto> =
        api.getAbilities().parse().results

    suspend fun fetchPokemonHabitats(): List<NamedResourceDto> =
        api.getHabitats().parse().results

    suspend fun fetchPokemonRegions(): List<NamedResourceDto> =
        api.getRegions().parse().results

    suspend fun fetchPokemonShapes(): List<NamedResourceDto> =
        api.getShapes().parse().results

    // ------------------------------------------------------------------
    // Filter — pokemon by category
    // ------------------------------------------------------------------

    suspend fun fetchPokemonByType(typeName: String): List<NamedResourceDto> =
        api.getTypeDetail(typeName).parse().pokemon.map { it.pokemon }

    suspend fun fetchPokemonByGeneration(generationName: String): List<NamedResourceDto> =
        api.getGenerationDetail(generationName).parse().pokemonSpecies

    suspend fun fetchPokemonByAbility(abilityName: String): List<NamedResourceDto> =
        api.getAbilityDetail(abilityName).parse().pokemon.map { it.pokemon }

    suspend fun fetchPokemonByHabitat(habitatName: String): List<NamedResourceDto> =
        api.getHabitatDetail(habitatName).parse().pokemonSpecies

    suspend fun fetchPokemonByShape(shapeName: String): List<NamedResourceDto> =
        api.getShapeDetail(shapeName).parse().pokemonSpecies

    suspend fun fetchPokemonByRegion(regionName: String): List<NamedResourceDto> {
        val region = api.getRegionDetail(regionName).parse()
        val allSpecies = linkedMapOf<String, NamedResourceDto>()
        for (pokedexRef in region.pokedexes) {
            val pokedex = api.getPokedexByUrl(pokedexRef.url).parse()
            for (entry in pokedex.pokemonEntries) {
                val species = entry.pokemonSpecies
                allSpecies[species.name] = species
            }
        }
        return allSpecies.values.toList()
    }

    // ------------------------------------------------------------------
    // Locations
    // ------------------------------------------------------------------

    suspend fun fetchLocationsByRegion(regionName: String): List<NamedResourceDto> =
        api.getRegionDetail(regionName).parse().locations

    suspend fun fetchLocationAreasByLocation(locationName: String): List<NamedResourceDto> =
        api.getLocationDetail(locationName).parse().areas

    suspend fun fetchPokemonByLocationArea(locationAreaName: String): List<NamedResourceDto> =
        api.getLocationAreaDetail(locationAreaName).parse().pokemonEncounters.map { it.pokemon }

    // ------------------------------------------------------------------
    // Pokemon detail
    // ------------------------------------------------------------------

    suspend fun fetchPokemonDetail(id: Int): PokemonDetailDto {
        // Pokemon detail is not a direct Gson target (complex nested structure),
        // so we keep a typed manual parse here via the raw detail endpoint.
        // We use a local helper that mirrors the old fetchObject approach but stays typed.
        val raw = api.getPokemonRaw(id).parse()

        val types = raw.types.map { slot ->
            val typeUrl = slot.type.url
            val typeId = typeUrl.trimEnd('/').substringAfterLast('/').toInt()
            PokemonTypeDto(name = slot.type.name, id = typeId, url = typeUrl)
        }

        val stats = raw.stats.map { slot ->
            PokemonStatDto(
                name = slot.stat.name,
                url = slot.stat.url,
                baseStat = slot.baseStat
            )
        }

        val abilities = raw.abilities.map { slot -> slot.ability }

        val levelUpMoves = mutableListOf<Pair<String, Int>>()
        for (moveSlot in raw.moves) {
            for (detail in moveSlot.versionGroupDetails) {
                if (detail.moveLearnMethod.name == "level-up") {
                    levelUpMoves += moveSlot.move.name to detail.levelLearnedAt
                    break
                }
            }
        }
        val topMoveNames = levelUpMoves.sortedByDescending { it.second }.take(6).map { it.first }

        return PokemonDetailDto(
            id = raw.id,
            name = raw.name,
            height = raw.height,
            weight = raw.weight,
            types = types,
            stats = stats,
            abilities = abilities,
            moveNames = topMoveNames
        )
    }

    // ------------------------------------------------------------------
    // Move detail
    // ------------------------------------------------------------------

    suspend fun fetchMoveDetail(moveName: String, languageCode: String): MoveDetailDto {
        val raw = api.getMoveDetail(moveName).parse()

        val typeUrl = raw.type.url
        val typeId = typeUrl.trimEnd('/').substringAfterLast('/').toInt()

        val description = raw.flavorTextEntries
            ?.firstOrNull { it.language.name == languageCode }
            ?.flavorText
            ?.sanitize()
            ?: raw.flavorTextEntries
                ?.firstOrNull { it.language.name == "en" }
                ?.flavorText
                ?.sanitize()
            ?: ""

        val localizedName = raw.names
            ?.firstOrNull { it.language.name == languageCode }
            ?.name
            ?: raw.name

        return MoveDetailDto(
            name = localizedName,
            typeName = raw.type.name,
            typeId = typeId,
            description = description,
            power = raw.power,
            accuracy = raw.accuracy,
            pp = raw.pp
        )
    }

    // ------------------------------------------------------------------
    // Evolution chain + varieties
    // ------------------------------------------------------------------

    suspend fun fetchEvolutionChainAndVarieties(pokemonId: Int): EvolutionAndVarietiesDto {
        val species = api.getPokemonSpecies(pokemonId).parse()

        val megaVarieties = species.varieties
            .filter { !it.isDefault && it.pokemon.name.contains("-mega") }
            .map { variety ->
                val varId = variety.pokemon.url.trimEnd('/').substringAfterLast('/').toInt()
                VarietyDto(id = varId, name = variety.pokemon.name)
            }

        val chainResponse = api.getEvolutionChainByUrl(species.evolutionChain.url).parse()
        val stages = mutableListOf<EvolutionStageDto>()
        flattenChain(chainResponse.chain, stages)

        return EvolutionAndVarietiesDto(evolutionStages = stages, megaVarieties = megaVarieties)
    }

    // ------------------------------------------------------------------
    // Ability detail
    // ------------------------------------------------------------------

    suspend fun fetchAbilityDetail(abilityUrl: String, languageCode: String): AbilityDetailDto {
        val raw = api.getAbilityDetailByUrl(abilityUrl).parse()

        val localizedName = raw.names
            ?.firstOrNull { it.language.name == languageCode }?.name
            ?: raw.names?.firstOrNull { it.language.name == "en" }?.name
            ?: raw.name

        val description = raw.flavorTextEntries
            ?.firstOrNull { it.language.name == languageCode }?.flavorText?.sanitize()
            ?: raw.flavorTextEntries?.firstOrNull { it.language.name == "en" }?.flavorText?.sanitize()
            ?: raw.effectEntries?.firstOrNull { it.language.name == languageCode }?.shortEffect
            ?: raw.effectEntries?.firstOrNull { it.language.name == "en" }?.shortEffect
            ?: ""

        return AbilityDetailDto(name = localizedName, description = description)
    }

    // ------------------------------------------------------------------
    // Localized name (cached)
    // ------------------------------------------------------------------

    suspend fun fetchLocalizedName(
        resourceUrl: String,
        languageCode: String,
        fallbackName: String
    ): String {
        val key = "$resourceUrl|$languageCode"
        localizedNameCache[key]?.let { return it }

        val raw = api.getByUrl(resourceUrl).parse()
        val localized = raw.names
            ?.firstOrNull { it.language.name == languageCode }?.name
            ?: raw.names?.firstOrNull { it.language.name == "en" }?.name
            ?: raw.name

        localizedNameCache[key] = localized
        return localized
    }

    suspend fun fetchPokemonSpeciesNameById(
        id: Int,
        languageCode: String,
        fallbackName: String
    ): String {
        val key = "pokemon-species/$id|$languageCode"
        localizedNameCache[key]?.let { return it }

        val species = api.getPokemonSpecies(id).parse()
        val localized = species.names
            ?.firstOrNull { it.language.name == languageCode }?.name
            ?: species.names?.firstOrNull { it.language.name == "en" }?.name
            ?: species.name

        localizedNameCache[key] = localized
        return localized
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    private fun flattenChain(node: ChainLinkDto, out: MutableList<EvolutionStageDto>) {
        val speciesUrl = node.species.url
        val id = speciesUrl.trimEnd('/').substringAfterLast('/').toInt()
        out += EvolutionStageDto(id = id, name = node.species.name)
        for (child in node.evolvesTo) flattenChain(child, out)
    }

    private fun String.sanitize() = replace("\n", " ").replace("\u000c", " ")
}
