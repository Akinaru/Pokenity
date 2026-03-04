package fr.pokenity.pokenity.domain.repository

import fr.pokenity.pokenity.domain.model.PokemonDetail
import fr.pokenity.pokenity.domain.model.PokemonFilterOption
import fr.pokenity.pokenity.domain.model.PokemonSummary

interface PokemonRepository {
    suspend fun getPokemonList(limit: Int = 80, offset: Int = 0): List<PokemonSummary>
    suspend fun getPokemonTypes(): List<PokemonFilterOption>
    suspend fun getPokemonGenerations(): List<PokemonFilterOption>
    suspend fun getPokemonAbilities(): List<PokemonFilterOption>
    suspend fun getPokemonHabitats(): List<PokemonFilterOption>
    suspend fun getPokemonRegions(): List<PokemonFilterOption>
    suspend fun getPokemonShapes(): List<PokemonFilterOption>
    suspend fun getPokemonByType(typeName: String): List<PokemonSummary>
    suspend fun getPokemonByGeneration(generationName: String): List<PokemonSummary>
    suspend fun getPokemonByAbility(abilityName: String): List<PokemonSummary>
    suspend fun getPokemonByHabitat(habitatName: String): List<PokemonSummary>
    suspend fun getPokemonByRegion(regionName: String): List<PokemonSummary>
    suspend fun getPokemonByShape(shapeName: String): List<PokemonSummary>
    suspend fun getLocationsByRegion(regionName: String): List<PokemonFilterOption>
    suspend fun getLocationAreasByLocation(locationName: String): List<PokemonFilterOption>
    suspend fun getPokemonByLocationArea(locationAreaName: String): List<PokemonSummary>
    suspend fun getPokemonDetail(id: Int): PokemonDetail
}
