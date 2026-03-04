package fr.pokenity.pokenity.data.repository

import fr.pokenity.pokenity.data.remote.NamedResourceDto
import fr.pokenity.pokenity.data.remote.PokeApiService
import fr.pokenity.pokenity.domain.model.EvolutionStage
import fr.pokenity.pokenity.domain.model.PokemonDetail
import fr.pokenity.pokenity.domain.model.PokemonStat
import fr.pokenity.pokenity.domain.model.PokemonFilterOption
import fr.pokenity.pokenity.domain.model.PokemonSummary
import fr.pokenity.pokenity.domain.repository.PokemonRepository
import java.util.Locale

class PokemonRepositoryImpl(
    private val pokeApiService: PokeApiService
) : PokemonRepository {

    override suspend fun getPokemonList(limit: Int, offset: Int): List<PokemonSummary> {
        return pokeApiService
            .fetchPokemonList(limit = limit, offset = offset)
            .mapNotNull { dto ->
                val id = dto.url.toResourceId() ?: return@mapNotNull null
                PokemonSummary(
                    id = id,
                    name = dto.name.asDisplayName(),
                    imageUrl = artworkUrl(id)
                )
            }
    }

    override suspend fun getPokemonTypes(): List<PokemonFilterOption> {
        return pokeApiService.fetchPokemonTypes().mapNotNull { resource ->
            val typeId = resource.url.toResourceId() ?: return@mapNotNull null
            PokemonFilterOption(
                apiName = resource.name,
                label = resource.name.asDisplayName(),
                imageUrl = typeImageUrl(typeId)
            )
        }
    }

    override suspend fun getPokemonGenerations(): List<PokemonFilterOption> {
        return pokeApiService.fetchPokemonGenerations().map { resource ->
            PokemonFilterOption(
                apiName = resource.name,
                label = resource.name.asDisplayName()
            )
        }
    }

    override suspend fun getPokemonAbilities(): List<PokemonFilterOption> {
        return pokeApiService.fetchPokemonAbilities().map { resource ->
            PokemonFilterOption(
                apiName = resource.name,
                label = resource.name.asDisplayName()
            )
        }
    }

    override suspend fun getPokemonHabitats(): List<PokemonFilterOption> {
        return pokeApiService.fetchPokemonHabitats().map { resource ->
            PokemonFilterOption(
                apiName = resource.name,
                label = resource.name.asDisplayName()
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

    private fun List<NamedResourceDto>.toPokemonSummaries(): List<PokemonSummary> {
        return mapNotNull { resource ->
            val id = resource.url.toResourceId() ?: return@mapNotNull null
            PokemonSummary(
                id = id,
                name = resource.name.asDisplayName(),
                imageUrl = artworkUrl(id)
            )
        }
    }

    override suspend fun getPokemonDetail(id: Int): PokemonDetail {
        val dto = pokeApiService.fetchPokemonDetail(id)

        // Fetch evolution chain (gracefully fallback to empty if it fails)
        val evolutionChain = try {
            pokeApiService.fetchEvolutionChain(id).map { stage ->
                EvolutionStage(
                    id = stage.id,
                    name = stage.name.asDisplayName(),
                    imageUrl = artworkUrl(stage.id),
                    isCurrent = stage.id == id
                )
            }
        } catch (_: Exception) {
            emptyList()
        }

        return PokemonDetail(
            id = dto.id,
            name = dto.name.asDisplayName(),
            imageUrl = artworkUrl(dto.id),
            types = dto.types.map { it.asDisplayName() },
            height = dto.height,
            weight = dto.weight,
            stats = dto.stats.map { stat ->
                PokemonStat(
                    name = stat.name.asDisplayName(),
                    baseStat = stat.baseStat
                )
            },
            abilities = dto.abilities.map { it.asDisplayName() },
            evolutionChain = evolutionChain
        )
    }

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
