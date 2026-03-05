package fr.pokenity.data.core

import fr.pokenity.data.remote.auth.AuthApiService
import fr.pokenity.data.remote.auth.AuthRetrofitApi
import fr.pokenity.data.remote.poke.PokeApiService
import fr.pokenity.data.remote.poke.PokeRetrofitApi
import fr.pokenity.data.repository.AuthRepositoryImpl
import fr.pokenity.data.repository.PokemonRepositoryImpl

object AppContainer {

    internal val authRetrofitApi: AuthRetrofitApi by lazy {
        NetworkModule.authRetrofit.create(AuthRetrofitApi::class.java)
    }

    internal val authApiService: AuthApiService by lazy {
        AuthApiService(api = authRetrofitApi)
    }

    internal val pokeRetrofitApi: PokeRetrofitApi by lazy {
        NetworkModule.pokeRetrofit.create(PokeRetrofitApi::class.java)
    }

    internal val pokeApiService: PokeApiService by lazy {
        PokeApiService(api = pokeRetrofitApi)
    }

    val pokemonRepository by lazy { PokemonRepositoryImpl() }
    val authRepository by lazy { AuthRepositoryImpl(authApiService) }
}
