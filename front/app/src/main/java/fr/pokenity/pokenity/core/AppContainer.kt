package fr.pokenity.pokenity.core

import fr.pokenity.pokenity.data.remote.PokeApiService
import fr.pokenity.pokenity.data.remote.auth.AuthApiService
import fr.pokenity.pokenity.data.repository.AuthRepositoryImpl
import fr.pokenity.pokenity.data.repository.PokemonRepositoryImpl

object AppContainer {
    val pokeApiService: PokeApiService by lazy { PokeApiService() }
    val authApiService: AuthApiService by lazy { AuthApiService() }

    val pokemonRepository by lazy { PokemonRepositoryImpl(pokeApiService) }
    val authRepository by lazy { AuthRepositoryImpl(authApiService) }
}
