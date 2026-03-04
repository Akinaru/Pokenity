package fr.pokenity.pokenity.data.repository

import fr.pokenity.pokenity.data.remote.PokeApiService
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

    private fun artworkUrl(id: Int): String {
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
    }

    private fun String.asDisplayName(): String {
        return replace('-', ' ').replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
    }
}
