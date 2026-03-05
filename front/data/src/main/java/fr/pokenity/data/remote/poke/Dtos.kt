package fr.pokenity.data.remote.poke

internal data class PokemonListItemDto(
    val name: String,
    val url: String
)

internal data class NamedResourceDto(
    val name: String,
    val url: String
)

internal data class LanguageDto(
    val code: String,
    val label: String
)

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
