package fr.pokenity.pokenity.domain.repository

import fr.pokenity.pokenity.domain.model.PokemonSummary

interface PokemonRepository {
    suspend fun getPokemonList(limit: Int = 80, offset: Int = 0): List<PokemonSummary>
}
