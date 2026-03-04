package fr.pokenity.pokenity.core

import fr.pokenity.pokenity.data.remote.PokeApiService
import fr.pokenity.pokenity.data.repository.PokemonRepositoryImpl

object AppContainer {
    val pokeApiService: PokeApiService by lazy { PokeApiService() }
    val pokemonRepository by lazy { PokemonRepositoryImpl(pokeApiService) }
}
