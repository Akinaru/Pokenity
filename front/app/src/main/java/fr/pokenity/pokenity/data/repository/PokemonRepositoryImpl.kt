package fr.pokenity.pokenity.data.repository

import fr.pokenity.pokenity.data.remote.PokeApiService
import fr.pokenity.pokenity.domain.model.EvolutionStage
import fr.pokenity.pokenity.domain.model.PokemonDetail
import fr.pokenity.pokenity.domain.model.PokemonStat
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
                val id = dto.url.trimEnd('/').substringAfterLast('/').toIntOrNull() ?: return@mapNotNull null
                PokemonSummary(
                    id = id,
                    name = dto.name.asDisplayName(),
                    imageUrl = artworkUrl(id)
                )
            }
    }

    override suspend fun getPokemonTypes(): List<String> {
        return pokeApiService.fetchPokemonTypes().map { it.name.asDisplayName() }
    }

    override suspend fun getPokemonGenerations(): List<String> {
        return pokeApiService.fetchPokemonGenerations().map { it.name.asDisplayName() }
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

    private fun String.asDisplayName(): String {
        return replace('-', ' ').replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
    }
}
