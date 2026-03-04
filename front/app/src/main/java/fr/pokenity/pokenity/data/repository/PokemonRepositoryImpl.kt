package fr.pokenity.pokenity.data.repository

import fr.pokenity.pokenity.core.AppLanguageState
import fr.pokenity.pokenity.data.remote.NamedResourceDto
import fr.pokenity.pokenity.data.remote.PokeApiService
import fr.pokenity.pokenity.domain.model.EvolutionStage
import fr.pokenity.pokenity.domain.model.LanguageOption
import fr.pokenity.pokenity.domain.model.PokemonDetail
import fr.pokenity.pokenity.domain.model.PokemonFilterOption
import fr.pokenity.pokenity.domain.model.PokemonMove
import fr.pokenity.pokenity.domain.model.PokemonStat
import fr.pokenity.pokenity.domain.model.PokemonSummary
import fr.pokenity.pokenity.domain.model.PokemonType
import fr.pokenity.pokenity.domain.repository.PokemonRepository
import java.util.Locale
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class PokemonRepositoryImpl(
    private val pokeApiService: PokeApiService
) : PokemonRepository {

    override suspend fun getAvailableLanguages(): List<LanguageOption> {
        return pokeApiService.fetchAvailableLanguages()
            .map { LanguageOption(code = it.code, label = it.label) }
            .sortedBy { it.code }
    }

    override suspend fun getPokemonList(limit: Int, offset: Int): List<PokemonSummary> {
        val language = currentLanguage()
        return pokeApiService
            .fetchPokemonList(limit = limit, offset = offset)
            .mapNotNull { dto ->
                val id = dto.url.toResourceId() ?: return@mapNotNull null
                PokemonSummary(
                    id = id,
                    name = localizedPokemonName(id = id, fallback = dto.name.asDisplayName(), language = language),
                    imageUrl = artworkUrl(id)
                )
            }
    }

    override suspend fun getPokemonTypes(): List<PokemonFilterOption> {
        val language = currentLanguage()
        return pokeApiService.fetchPokemonTypes().mapNotNull { resource ->
            val typeId = resource.url.toResourceId() ?: return@mapNotNull null
            PokemonFilterOption(
                apiName = resource.name,
                label = localizedResourceName(resource, language),
                imageUrl = typeImageUrl(typeId)
            )
        }
    }

    override suspend fun getPokemonGenerations(): List<PokemonFilterOption> {
        val language = currentLanguage()
        return pokeApiService.fetchPokemonGenerations().map { resource ->
            PokemonFilterOption(
                apiName = resource.name,
                label = localizedResourceName(resource, language)
            )
        }
    }

    override suspend fun getPokemonAbilities(): List<PokemonFilterOption> {
        val language = currentLanguage()
        return pokeApiService.fetchPokemonAbilities().map { resource ->
            PokemonFilterOption(
                apiName = resource.name,
                label = localizedResourceName(resource, language)
            )
        }
    }

    override suspend fun getPokemonHabitats(): List<PokemonFilterOption> {
        val language = currentLanguage()
        return pokeApiService.fetchPokemonHabitats().map { resource ->
            PokemonFilterOption(
                apiName = resource.name,
                label = localizedResourceName(resource, language)
            )
        }
    }

    override suspend fun getPokemonRegions(): List<PokemonFilterOption> {
        val language = currentLanguage()
        return pokeApiService.fetchPokemonRegions().map { resource ->
            PokemonFilterOption(
                apiName = resource.name,
                label = localizedResourceName(resource, language)
            )
        }
    }

    override suspend fun getPokemonShapes(): List<PokemonFilterOption> {
        val language = currentLanguage()
        return pokeApiService.fetchPokemonShapes().map { resource ->
            PokemonFilterOption(
                apiName = resource.name,
                label = localizedResourceName(resource, language)
            )
        }
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
        return pokeApiService.fetchLocationsByRegion(regionName).map { resource ->
            PokemonFilterOption(
                apiName = resource.name,
                label = localizedResourceName(resource, language)
            )
        }
    }

    override suspend fun getLocationAreasByLocation(locationName: String): List<PokemonFilterOption> {
        val language = currentLanguage()
        return pokeApiService.fetchLocationAreasByLocation(locationName).map { resource ->
            PokemonFilterOption(
                apiName = resource.name,
                label = localizedResourceName(resource, language)
            )
        }
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

        val (evolutionChain, moves) = coroutineScope {
            val evolutionDeferred = async {
                runCatching {
                    pokeApiService.fetchEvolutionChain(id).map { stage ->
                        EvolutionStage(
                            id = stage.id,
                            name = localizedPokemonName(stage.id, stage.name.asDisplayName(), language),
                            imageUrl = artworkUrl(stage.id),
                            isCurrent = stage.id == id
                        )
                    }
                }.getOrElse { emptyList() }
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
                                        name = localizedTypeName(moveDto.typeId, moveDto.typeName.asDisplayName(), language),
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

            evolutionDeferred.await() to movesDeferred.await()
        }

        return PokemonDetail(
            id = dto.id,
            name = localizedPokemonName(id = dto.id, fallback = dto.name.asDisplayName(), language = language),
            imageUrl = artworkUrl(dto.id),
            types = dto.types.map { typeDto ->
                PokemonType(
                    name = localizedResourceName(
                        resource = NamedResourceDto(typeDto.name, typeDto.url),
                        language = language
                    ),
                    imageUrl = typeImageUrl(typeDto.id)
                )
            },
            height = dto.height,
            weight = dto.weight,
            stats = dto.stats.map { stat ->
                PokemonStat(
                    name = pokeApiService.fetchLocalizedName(stat.url, language, stat.name.asDisplayName()),
                    baseStat = stat.baseStat
                )
            },
            abilities = dto.abilities.map { ability ->
                localizedResourceName(ability, language)
            },
            moves = moves,
            evolutionChain = evolutionChain
        )
    }

    private fun List<NamedResourceDto>.toPokemonSummaries(): List<PokemonSummary> {
        val language = currentLanguage()
        return mapNotNull { resource ->
            val id = resource.url.toResourceId() ?: return@mapNotNull null
            PokemonSummary(
                id = id,
                name = localizedPokemonName(id = id, fallback = resource.name.asDisplayName(), language = language),
                imageUrl = artworkUrl(id)
            )
        }
    }

    private fun localizedResourceName(resource: NamedResourceDto, language: String): String {
        val fallback = resource.name.asDisplayName()
        if (language == "en") return fallback
        return runCatching {
            pokeApiService.fetchLocalizedName(resource.url, language, fallback)
        }.getOrElse { fallback }
    }

    private fun localizedPokemonName(id: Int, fallback: String, language: String): String {
        if (language == "en") return fallback
        return runCatching {
            pokeApiService.fetchPokemonSpeciesNameById(id, language, fallback)
        }.getOrElse { fallback }
    }

    private fun localizedTypeName(typeId: Int, fallback: String, language: String): String {
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
