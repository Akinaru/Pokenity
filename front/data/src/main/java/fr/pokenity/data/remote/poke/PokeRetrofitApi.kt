package fr.pokenity.data.remote.poke

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Retrofit interface for the PokeAPI.
 * Typed routes for all known static endpoints; @Url variant for fully dynamic URLs
 * (pokedex entries, evolution chains, localized resources fetched by URL).
 */
internal interface PokeRetrofitApi {

    // ------------------------------------------------------------------
    // Generic dynamic URL — used for pokedex-by-url, evolution-chain-by-url,
    // and any localized resource fetched by its own URL.
    // ------------------------------------------------------------------

    @GET
    suspend fun getByUrl(@Url url: String): Response<LocalizableResourceDto>

    @GET
    suspend fun getPokedexByUrl(@Url url: String): Response<PokedexResponseDto>

    @GET
    suspend fun getEvolutionChainByUrl(@Url url: String): Response<EvolutionChainResponseDto>

    // ------------------------------------------------------------------
    // Languages
    // ------------------------------------------------------------------

    @GET("language")
    suspend fun getLanguages(): Response<NamedResultsPageDto>

    @GET("language/{name}")
    suspend fun getLanguage(@Path("name") name: String): Response<LanguageResponseDto>

    // ------------------------------------------------------------------
    // Pokemon list
    // ------------------------------------------------------------------

    @GET("pokemon")
    suspend fun getPokemons(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Response<PokemonResultsPageDto>

    // ------------------------------------------------------------------
    // Type
    // ------------------------------------------------------------------

    @GET("type")
    suspend fun getTypes(@Query("limit") limit: Int = 100): Response<NamedResultsPageDto>

    @GET("type/{name}")
    suspend fun getTypeDetail(@Path("name") name: String): Response<TypeDetailResponseDto>

    // ------------------------------------------------------------------
    // Generation
    // ------------------------------------------------------------------

    @GET("generation")
    suspend fun getGenerations(@Query("limit") limit: Int = 100): Response<NamedResultsPageDto>

    @GET("generation/{name}")
    suspend fun getGenerationDetail(@Path("name") name: String): Response<GenerationDetailResponseDto>

    // ------------------------------------------------------------------
    // Ability
    // ------------------------------------------------------------------

    @GET("ability")
    suspend fun getAbilities(@Query("limit") limit: Int = 1000): Response<NamedResultsPageDto>

    @GET("ability/{name}")
    suspend fun getAbilityDetail(@Path("name") name: String): Response<AbilityDetailResponseDto>

    // ------------------------------------------------------------------
    // Habitat
    // ------------------------------------------------------------------

    @GET("pokemon-habitat")
    suspend fun getHabitats(@Query("limit") limit: Int = 100): Response<NamedResultsPageDto>

    @GET("pokemon-habitat/{name}")
    suspend fun getHabitatDetail(@Path("name") name: String): Response<HabitatDetailResponseDto>

    // ------------------------------------------------------------------
    // Region
    // ------------------------------------------------------------------

    @GET("region")
    suspend fun getRegions(@Query("limit") limit: Int = 100): Response<NamedResultsPageDto>

    @GET("region/{name}")
    suspend fun getRegionDetail(@Path("name") name: String): Response<RegionDetailResponseDto>

    // ------------------------------------------------------------------
    // Shape
    // ------------------------------------------------------------------

    @GET("pokemon-shape")
    suspend fun getShapes(@Query("limit") limit: Int = 100): Response<NamedResultsPageDto>

    @GET("pokemon-shape/{name}")
    suspend fun getShapeDetail(@Path("name") name: String): Response<ShapeDetailResponseDto>

    // ------------------------------------------------------------------
    // Location
    // ------------------------------------------------------------------

    @GET("location/{name}")
    suspend fun getLocationDetail(@Path("name") name: String): Response<LocationDetailResponseDto>

    // ------------------------------------------------------------------
    // Location-area
    // ------------------------------------------------------------------

    @GET("location-area/{name}")
    suspend fun getLocationAreaDetail(@Path("name") name: String): Response<LocationAreaDetailResponseDto>

    // ------------------------------------------------------------------
    // Move
    // ------------------------------------------------------------------

    @GET("move/{name}")
    suspend fun getMoveDetail(@Path("name") name: String): Response<MoveDetailResponseDto>

    // ------------------------------------------------------------------
    // Pokemon species
    // ------------------------------------------------------------------

    @GET("pokemon-species/{id}")
    suspend fun getPokemonSpecies(@Path("id") id: Int): Response<PokemonSpeciesResponseDto>

    // ------------------------------------------------------------------
    // Pokemon raw detail  (complex nested structure for detail parsing)
    // ------------------------------------------------------------------

    @GET("pokemon/{id}")
    suspend fun getPokemonRaw(@Path("id") id: Int): Response<PokemonRawResponseDto>

    // ------------------------------------------------------------------
    // Ability detail by URL  (called with the ability's own URL)
    // ------------------------------------------------------------------

    @GET
    suspend fun getAbilityDetailByUrl(@Url url: String): Response<AbilityDetailResponseDto>
}
