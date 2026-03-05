package fr.pokenity.data.core

import fr.pokenity.data.repository.AuthRepositoryImpl
import fr.pokenity.data.repository.PokemonRepositoryImpl

object AppContainer {

    val pokemonRepository by lazy { PokemonRepositoryImpl() }
    val authRepository by lazy { AuthRepositoryImpl() }
}
