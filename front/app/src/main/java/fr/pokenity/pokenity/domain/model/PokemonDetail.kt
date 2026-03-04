package fr.pokenity.pokenity.domain.model

data class PokemonDetail(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val types: List<String>,
    val height: Int,
    val weight: Int,
    val stats: List<PokemonStat>,
    val abilities: List<String>,
    val evolutionChain: List<EvolutionStage> = emptyList()
)

data class PokemonStat(
    val name: String,
    val baseStat: Int
)

data class EvolutionStage(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val isCurrent: Boolean
)
