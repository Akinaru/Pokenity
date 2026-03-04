package fr.pokenity.pokenity.domain.model

data class PokemonDetail(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val types: List<PokemonType>,
    val height: Int,
    val weight: Int,
    val stats: List<PokemonStat>,
    val abilities: List<String>,
    val moves: List<PokemonMove> = emptyList(),
    val evolutionChain: List<EvolutionStage> = emptyList()
)

data class PokemonType(
    val name: String,
    val imageUrl: String
)

data class PokemonStat(
    val name: String,
    val baseStat: Int
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
