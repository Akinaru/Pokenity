package fr.pokenity.data.model

data class PokemonDetail(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val shinyImageUrl: String,
    val types: List<PokemonType>,
    val height: Int,
    val weight: Int,
    val stats: List<PokemonStat>,
    val abilities: List<PokemonAbility>,
    val moves: List<PokemonMove> = emptyList(),
    val evolutionChain: List<EvolutionStage> = emptyList(),
    val megaEvolutions: List<MegaEvolution> = emptyList()
)

data class PokemonType(
    val id: Int,
    val name: String,
    val imageUrl: String
)

data class PokemonStat(
    val name: String,
    val baseStat: Int
)

data class PokemonAbility(
    val name: String,
    val description: String
)

data class PokemonMove(
    val name: String,
    val type: PokemonType,
    val description: String,
    val power: Int?,
    val accuracy: Int?,
    val pp: Int?
)

data class EvolutionStage(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val isCurrent: Boolean
)

data class MegaEvolution(
    val name: String,
    val imageUrl: String
)
