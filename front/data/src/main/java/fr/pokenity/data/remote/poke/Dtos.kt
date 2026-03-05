package fr.pokenity.data.remote.poke

import com.google.gson.annotations.SerializedName

// ---------------------------------------------------------------------------
// Generic wrappers
// ---------------------------------------------------------------------------

/** Standard paginated list response: { count, results: [...] } */
internal data class NamedResultsPageDto(
    val count: Int,
    val results: List<NamedResourceDto>
)

// ---------------------------------------------------------------------------
// Shared primitives
// ---------------------------------------------------------------------------

internal data class PokemonListItemDto(
    val name: String,
    val url: String
)

internal data class NamedResourceDto(
    val name: String,
    val url: String
)

/** { name: String, language: { name, url } } — used in "names" arrays */
internal data class LocalizedNameEntryDto(
    val name: String,
    val language: NamedResourceDto
)

/** { flavor_text: String, language: { name, url } } — used in flavor_text_entries */
internal data class FlavorTextEntryDto(
    @SerializedName("flavor_text") val flavorText: String,
    val language: NamedResourceDto
)

/** { short_effect: String, effect: String, language: { name, url } } */
internal data class EffectEntryDto(
    @SerializedName("short_effect") val shortEffect: String,
    val effect: String,
    val language: NamedResourceDto
)

// ---------------------------------------------------------------------------
// Language
// ---------------------------------------------------------------------------

internal data class LanguageDto(
    val code: String,
    val label: String
)

/** Raw API language resource — used for the list response */
internal data class LanguageResponseDto(
    val name: String,
    val url: String,
    val names: List<LocalizedNameEntryDto>?
)

internal data class LanguageResultsPageDto(
    val count: Int,
    val results: List<LanguageResponseDto>
)

// ---------------------------------------------------------------------------
// Pokemon list (paginated)
// ---------------------------------------------------------------------------

internal data class PokemonResultsPageDto(
    val count: Int,
    val results: List<PokemonListItemDto>
)

// ---------------------------------------------------------------------------
// Pokemon detail
// ---------------------------------------------------------------------------

internal data class PokemonDetailDto(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val types: List<PokemonTypeDto>,
    val stats: List<PokemonStatDto>,
    val abilities: List<NamedResourceDto>,
    val moveNames: List<String> = emptyList()
)

internal data class PokemonTypeDto(
    val name: String,
    val id: Int,
    val url: String
)

internal data class PokemonStatDto(
    val name: String,
    val url: String,
    val baseStat: Int
)

// ---------------------------------------------------------------------------
// Type detail  (GET /type/{name})
// ---------------------------------------------------------------------------

/** { pokemon: [ { pokemon: { name, url } } ] } */
internal data class TypeDetailResponseDto(
    val pokemon: List<TypePokemonSlotDto>,
    val names: List<LocalizedNameEntryDto>?
)

internal data class TypePokemonSlotDto(
    val pokemon: NamedResourceDto
)

// ---------------------------------------------------------------------------
// Generation detail  (GET /generation/{name})
// ---------------------------------------------------------------------------

internal data class GenerationDetailResponseDto(
    @SerializedName("pokemon_species") val pokemonSpecies: List<NamedResourceDto>,
    val names: List<LocalizedNameEntryDto>?
)

// ---------------------------------------------------------------------------
// Ability detail  (GET /ability/{name})
// ---------------------------------------------------------------------------

internal data class AbilityDetailResponseDto(
    val name: String,
    val names: List<LocalizedNameEntryDto>?,
    val pokemon: List<AbilityPokemonSlotDto>,
    @SerializedName("flavor_text_entries") val flavorTextEntries: List<FlavorTextEntryDto>?,
    @SerializedName("effect_entries") val effectEntries: List<EffectEntryDto>?
)

internal data class AbilityPokemonSlotDto(
    val pokemon: NamedResourceDto
)

// ---------------------------------------------------------------------------
// Habitat  (GET /pokemon-habitat/{name})
// ---------------------------------------------------------------------------

internal data class HabitatDetailResponseDto(
    @SerializedName("pokemon_species") val pokemonSpecies: List<NamedResourceDto>,
    val names: List<LocalizedNameEntryDto>?
)

// ---------------------------------------------------------------------------
// Shape  (GET /pokemon-shape/{name})
// ---------------------------------------------------------------------------

internal data class ShapeDetailResponseDto(
    @SerializedName("pokemon_species") val pokemonSpecies: List<NamedResourceDto>,
    val names: List<LocalizedNameEntryDto>?
)

// ---------------------------------------------------------------------------
// Region  (GET /region/{name})
// ---------------------------------------------------------------------------

internal data class RegionDetailResponseDto(
    val pokedexes: List<NamedResourceDto>,
    val locations: List<NamedResourceDto>,
    val names: List<LocalizedNameEntryDto>?
)

// ---------------------------------------------------------------------------
// Pokedex  (GET /pokedex/{id|name} or @Url)
// ---------------------------------------------------------------------------

internal data class PokedexResponseDto(
    @SerializedName("pokemon_entries") val pokemonEntries: List<PokedexEntryDto>
)

internal data class PokedexEntryDto(
    @SerializedName("pokemon_species") val pokemonSpecies: NamedResourceDto
)

// ---------------------------------------------------------------------------
// Location  (GET /location/{name})
// ---------------------------------------------------------------------------

internal data class LocationDetailResponseDto(
    val areas: List<NamedResourceDto>,
    val names: List<LocalizedNameEntryDto>?
)

// ---------------------------------------------------------------------------
// Location-area  (GET /location-area/{name})
// ---------------------------------------------------------------------------

internal data class LocationAreaDetailResponseDto(
    @SerializedName("pokemon_encounters") val pokemonEncounters: List<LocationAreaEncounterDto>,
    val names: List<LocalizedNameEntryDto>?
)

internal data class LocationAreaEncounterDto(
    val pokemon: NamedResourceDto
)

// ---------------------------------------------------------------------------
// Move  (GET /move/{name})
// ---------------------------------------------------------------------------

internal data class MoveDetailResponseDto(
    val name: String,
    val type: NamedResourceDto,
    val power: Int?,
    val accuracy: Int?,
    val pp: Int?,
    val names: List<LocalizedNameEntryDto>?,
    @SerializedName("flavor_text_entries") val flavorTextEntries: List<FlavorTextEntryDto>?
)

// ---------------------------------------------------------------------------
// Pokemon species  (GET /pokemon-species/{id})
// ---------------------------------------------------------------------------

internal data class PokemonSpeciesResponseDto(
    val name: String,
    val names: List<LocalizedNameEntryDto>?,
    @SerializedName("evolution_chain") val evolutionChain: EvolutionChainLinkDto,
    val varieties: List<SpeciesVarietyDto>
)

internal data class EvolutionChainLinkDto(
    val url: String
)

internal data class SpeciesVarietyDto(
    @SerializedName("is_default") val isDefault: Boolean,
    val pokemon: NamedResourceDto
)

// ---------------------------------------------------------------------------
// Evolution chain  (GET /evolution-chain/{id} or @Url)
// ---------------------------------------------------------------------------

internal data class EvolutionChainResponseDto(
    val chain: ChainLinkDto
)

internal data class ChainLinkDto(
    val species: NamedResourceDto,
    @SerializedName("evolves_to") val evolvesTo: List<ChainLinkDto>
)

// ---------------------------------------------------------------------------
// Pokemon raw detail  (GET /pokemon/{id})
// ---------------------------------------------------------------------------

internal data class PokemonRawResponseDto(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val types: List<PokemonTypeSlotDto>,
    val stats: List<PokemonStatSlotDto>,
    val abilities: List<PokemonAbilitySlotDto>,
    val moves: List<PokemonMoveSlotDto>
)

internal data class PokemonTypeSlotDto(
    val type: NamedResourceDto
)

internal data class PokemonStatSlotDto(
    @SerializedName("base_stat") val baseStat: Int,
    val stat: NamedResourceDto
)

internal data class PokemonAbilitySlotDto(
    val ability: NamedResourceDto
)

internal data class PokemonMoveSlotDto(
    val move: NamedResourceDto,
    @SerializedName("version_group_details") val versionGroupDetails: List<MoveVersionGroupDetailDto>
)

internal data class MoveVersionGroupDetailDto(
    @SerializedName("level_learned_at") val levelLearnedAt: Int,
    @SerializedName("move_learn_method") val moveLearnMethod: NamedResourceDto
)

// ---------------------------------------------------------------------------
// Generic localized resource  (GET @Url — type / stat / etc.)
// ---------------------------------------------------------------------------

internal data class LocalizableResourceDto(
    val name: String,
    val names: List<LocalizedNameEntryDto>?
)

// ---------------------------------------------------------------------------
// Assembled domain DTOs (built by PokeApiService, not direct Gson targets)
// ---------------------------------------------------------------------------

internal data class MoveDetailDto(
    val name: String,
    val typeName: String,
    val typeId: Int,
    val description: String,
    val power: Int?,
    val accuracy: Int?,
    val pp: Int?
)

internal data class EvolutionStageDto(
    val id: Int,
    val name: String
)

internal data class EvolutionAndVarietiesDto(
    val evolutionStages: List<EvolutionStageDto>,
    val megaVarieties: List<VarietyDto>
)

internal data class VarietyDto(
    val id: Int,
    val name: String
)

internal data class AbilityDetailDto(
    val name: String,
    val description: String
)
