package fr.pokenity.pokenity.data.repository

import fr.pokenity.pokenity.data.remote.NamedResourceDto
import fr.pokenity.pokenity.data.remote.PokeApiService
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
        return pokeApiService.fetchPokemonTypes().map { resource ->
            PokemonFilterOption(
                apiName = resource.name,
                label = resource.name.asDisplayName()
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

    private fun artworkUrl(id: Int): String {
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
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
