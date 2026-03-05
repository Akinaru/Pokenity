package fr.pokenity.data.repository

import fr.pokenity.data.core.AppContainer
import fr.pokenity.data.core.AppLanguageState
import fr.pokenity.data.model.EvolutionStage
import fr.pokenity.data.model.LanguageOption
import fr.pokenity.data.model.MegaEvolution
import fr.pokenity.data.model.PokemonAbility
import fr.pokenity.data.model.PokemonDetail
import fr.pokenity.data.model.PokemonFilterOption
import fr.pokenity.data.model.PokemonMove
import fr.pokenity.data.model.PokemonStat
import fr.pokenity.data.model.PokemonSummary
import fr.pokenity.data.model.PokemonType
import fr.pokenity.data.remote.poke.NamedResourceDto
import fr.pokenity.data.remote.poke.PokeApiService
import java.util.Locale
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class PokemonRepositoryImpl(
) : PokemonRepository {

    internal val pokeApiService = AppContainer.pokeApiService


    override suspend fun getAvailableLanguages(): List<LanguageOption> {
        return pokeApiService.fetchAvailableLanguages()
            .map { LanguageOption(code = it.code, label = it.label) }
            .sortedBy { it.code }
    }

    override suspend fun getPokemonTotalCount(): Int {
        return pokeApiService.fetchPokemonTotalCount()
    }

    override suspend fun getPokemonList(limit: Int, offset: Int): List<PokemonSummary> {
        val language = currentLanguage()
        val dtos = pokeApiService.fetchPokemonList(limit = limit, offset = offset)
        val result = mutableListOf<PokemonSummary>()
        for (dto in dtos) {
            val id = dto.url.toResourceId() ?: continue
            result += PokemonSummary(
                id = id,
                name = localizedPokemonName(
                    id = id,
                    fallback = dto.name.asDisplayName(),
                    language = language
                ),
                imageUrl = artworkUrl(id)
            )
        }

        return result
    }

    override suspend fun getPokemonTypes(): List<PokemonFilterOption> {
        val language = currentLanguage()
        val resources = pokeApiService.fetchPokemonTypes()
        val result = mutableListOf<PokemonFilterOption>()

        for (resource in resources) {
            val typeId = resource.url.toResourceId() ?: continue
            result += PokemonFilterOption(
                apiName = resource.name,
                label = localizedResourceName(resource, language),
                imageUrl = typeImageUrl(typeId)
            )
        }

        return result
    }

    override suspend fun getPokemonGenerations(): List<PokemonFilterOption> {
        val language = currentLanguage()
        val resources = pokeApiService.fetchPokemonGenerations()
        val result = mutableListOf<PokemonFilterOption>()

        for (resource in resources) {
            result += PokemonFilterOption(
                apiName = resource.name,
                label = localizedResourceName(resource, language)
            )
        }

        return result
    }

    override suspend fun getPokemonAbilities(): List<PokemonFilterOption> {
        val language = currentLanguage()
        val resources = pokeApiService.fetchPokemonAbilities()
        val result = mutableListOf<PokemonFilterOption>()

        for (resource in resources) {
            result += PokemonFilterOption(
                apiName = resource.name,
                label = localizedResourceName(resource, language)
            )
        }

        return result
    }

    override suspend fun getPokemonHabitats(): List<PokemonFilterOption> {
        val language = currentLanguage()
        val resources = pokeApiService.fetchPokemonHabitats()
        val result = mutableListOf<PokemonFilterOption>()

        for (resource in resources) {
            result += PokemonFilterOption(
                apiName = resource.name,
                label = localizedResourceName(resource, language)
            )
        }

        return result
    }

    override suspend fun getPokemonRegions(): List<PokemonFilterOption> {
        val language = currentLanguage()
        val resources = pokeApiService.fetchPokemonRegions()
        val result = mutableListOf<PokemonFilterOption>()

        for (resource in resources) {
            result += PokemonFilterOption(
                apiName = resource.name,
                label = localizedResourceName(resource, language)
            )
        }

        return result
    }

    override suspend fun getPokemonShapes(): List<PokemonFilterOption> {
        val language = currentLanguage()
        val resources = pokeApiService.fetchPokemonShapes()
        val result = mutableListOf<PokemonFilterOption>()

        for (resource in resources) {
            result += PokemonFilterOption(
                apiName = resource.name,
                label = localizedResourceName(resource, language)
            )
        }

        return result
    }

    override suspend fun getPokemonByType(typeName: String): List<PokemonSummary> {
        return pokeApiService
            .fetchPokemonByType(typeName)
            .toPokemonSummaries()
            .sortedBy { it.id }
    }

    override suspend fun getPokemonByGeneration(generationName: String): List<PokemonSummary> {
        return pokeApiService
            .fetchPokemonByGeneration(generationName)
            .toPokemonSummaries()
            .sortedBy { it.id }
    }

    override suspend fun getPokemonByAbility(abilityName: String): List<PokemonSummary> {
        return pokeApiService
            .fetchPokemonByAbility(abilityName)
            .toPokemonSummaries()
            .sortedBy { it.id }
    }

    override suspend fun getPokemonByHabitat(habitatName: String): List<PokemonSummary> {
        return pokeApiService
            .fetchPokemonByHabitat(habitatName)
            .toPokemonSummaries()
            .sortedBy { it.id }
    }

    override suspend fun getPokemonByRegion(regionName: String): List<PokemonSummary> {
        return pokeApiService
            .fetchPokemonByRegion(regionName)
            .toPokemonSummaries()
            .sortedBy { it.id }
    }

    override suspend fun getPokemonByShape(shapeName: String): List<PokemonSummary> {
        return pokeApiService
            .fetchPokemonByShape(shapeName)
            .toPokemonSummaries()
            .sortedBy { it.id }
    }

    override suspend fun getLocationsByRegion(regionName: String): List<PokemonFilterOption> {
        val language = currentLanguage()
        val resources = pokeApiService.fetchLocationsByRegion(regionName)
        val result = mutableListOf<PokemonFilterOption>()

        for (resource in resources) {
            result += PokemonFilterOption(
                apiName = resource.name,
                label = localizedResourceName(resource, language)
            )
        }

        return result
    }

    override suspend fun getLocationAreasByLocation(locationName: String): List<PokemonFilterOption> {
        val language = currentLanguage()
        val resources = pokeApiService.fetchLocationAreasByLocation(locationName)
        val result = mutableListOf<PokemonFilterOption>()

        for (resource in resources) {
            result += PokemonFilterOption(
                apiName = resource.name,
                label = localizedResourceName(resource, language)
            )
        }

        return result
    }

    override suspend fun getPokemonByLocationArea(locationAreaName: String): List<PokemonSummary> {
        return pokeApiService
            .fetchPokemonByLocationArea(locationAreaName)
            .toPokemonSummaries()
            .sortedBy { it.id }
    }

    override suspend fun getPokemonDetail(id: Int): PokemonDetail {
        val language = currentLanguage()
        val dto = pokeApiService.fetchPokemonDetail(id)

        val (evolutionChain, moves, megaEvolutions, abilities) = coroutineScope {
            val evolutionAndVarietiesDeferred = async {
                runCatching {
                    pokeApiService.fetchEvolutionChainAndVarieties(id)
                }.getOrNull()
            }

            val movesDeferred = async {
                runCatching {
                    dto.moveNames.take(3).map { moveName ->
                        async {
                            runCatching {
                                val moveDto = pokeApiService.fetchMoveDetail(moveName, language)
                                PokemonMove(
                                    name = moveDto.name,
                                    type = PokemonType(
                                        id = moveDto.typeId,
                                        name = localizedTypeName(
                                            moveDto.typeId,
                                            moveDto.typeName.asDisplayName(),
                                            language
                                        ),
                                        imageUrl = typeImageUrl(moveDto.typeId)
                                    ),
                                    description = moveDto.description,
                                    power = moveDto.power,
                                    accuracy = moveDto.accuracy,
                                    pp = moveDto.pp
                                )
                            }.getOrNull()
                        }
                    }.awaitAll().filterNotNull()
                }.getOrElse { emptyList() }
            }

            val abilitiesDeferred = async {
                dto.abilities.map { ability ->
                    async {
                        runCatching {
                            pokeApiService.fetchAbilityDetail(ability.url, language)
                        }.getOrNull()
                    }
                }.awaitAll().filterNotNull().map { abilityDto ->
                    PokemonAbility(
                        name = abilityDto.name,
                        description = abilityDto.description
                    )
                }
            }

            val evoResult = evolutionAndVarietiesDeferred.await()

            val evolutionStages = mutableListOf<EvolutionStage>()
            if (evoResult != null) {
                for (stage in evoResult.evolutionStages) {
                    evolutionStages += EvolutionStage(
                        id = stage.id,
                        name = localizedPokemonName(stage.id, stage.name.asDisplayName(), language),
                        imageUrl = artworkUrl(stage.id),
                        isCurrent = stage.id == id
                    )
                }
            }

            val megas = evoResult?.megaVarieties?.map { variety ->
                MegaEvolution(
                    name = variety.name.asDisplayName(),
                    imageUrl = artworkUrl(variety.id)
                )
            } ?: emptyList()

            data class DetailResults(
                val evolution: List<EvolutionStage>,
                val moves: List<PokemonMove>,
                val megas: List<MegaEvolution>,
                val abilities: List<PokemonAbility>
            )

            DetailResults(
                evolution = evolutionStages,
                moves = movesDeferred.await(),
                megas = megas,
                abilities = abilitiesDeferred.await()
            )
        }

        return PokemonDetail(
            id = dto.id,
            name = localizedPokemonName(
                id = dto.id,
                fallback = dto.name.asDisplayName(),
                language = language
            ),
            imageUrl = artworkUrl(dto.id),
            shinyImageUrl = shinyArtworkUrl(dto.id),
             types = run {
                 val list = mutableListOf<PokemonType>()
                 for (typeDto in dto.types) {
                     list += PokemonType(
                         id = typeDto.id,
                         name = localizedResourceName(
                             resource = NamedResourceDto(typeDto.name, typeDto.url),
                             language = language
                         ),
                         imageUrl = typeImageUrl(typeDto.id)
                     )
                 }
                 list
             },
            height = dto.height,
            weight = dto.weight,
             stats = run {
                 val list = mutableListOf<PokemonStat>()
                 for (stat in dto.stats) {
                     val localizedName = pokeApiService.fetchLocalizedName(
                         stat.url,
                         language,
                         stat.name.asDisplayName()
                     )
                     list += PokemonStat(
                         name = localizedName,
                         baseStat = stat.baseStat
                     )
                 }
                 list
             },
            abilities = abilities,
            moves = moves,
            evolutionChain = evolutionChain,
            megaEvolutions = megaEvolutions
        )
    }

    private suspend fun List<NamedResourceDto>.toPokemonSummaries(): List<PokemonSummary> {
        val language = currentLanguage()
        val result = mutableListOf<PokemonSummary>()

        for (resource in this) {
            val id = resource.url.toResourceId() ?: continue
            result += PokemonSummary(
                id = id,
                name = localizedPokemonName(
                    id = id,
                    fallback = resource.name.asDisplayName(),
                    language = language
                ),
                imageUrl = artworkUrl(id)
            )
        }

        return result
    }

    private suspend fun localizedResourceName(resource: NamedResourceDto, language: String): String {
        val fallback = resource.name.asDisplayName()
        if (language == "en") return fallback
        return runCatching {
            pokeApiService.fetchLocalizedName(resource.url, language, fallback)
        }.getOrElse { fallback }
    }

    private suspend fun localizedPokemonName(id: Int, fallback: String, language: String): String {
        if (language == "en") return fallback
        return runCatching {
            pokeApiService.fetchPokemonSpeciesNameById(id, language, fallback)
        }.getOrElse { fallback }
    }

    private suspend fun localizedTypeName(typeId: Int, fallback: String, language: String): String {
        if (language == "en") return fallback
        return runCatching {
            pokeApiService.fetchLocalizedName(
                resourceUrl = "https://pokeapi.co/api/v2/type/$typeId",
                languageCode = language,
                fallbackName = fallback
            )
        }.getOrElse { fallback }
    }

    private fun currentLanguage(): String = AppLanguageState.currentLanguage()

    private fun artworkUrl(id: Int): String {
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
    }

    private fun shinyArtworkUrl(id: Int): String {
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/shiny/$id.png"
    }

    private fun typeImageUrl(typeId: Int): String {
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/types/generation-viii/sword-shield/$typeId.png"
    }

    private fun String.toResourceId(): Int? {
        return trimEnd('/').substringAfterLast('/').toIntOrNull()
    }

    private fun String.asDisplayName(): String {
        return replace('-', ' ').replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
    }
}
