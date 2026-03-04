package fr.pokenity.pokenity.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object PokemonBrowseState {
    private val _pokemonIds = MutableStateFlow<List<Int>>(emptyList())
    val pokemonIds: StateFlow<List<Int>> = _pokemonIds.asStateFlow()

    fun setList(ids: List<Int>) {
        _pokemonIds.value = ids
    }

    fun previousOf(currentId: Int): Int? {
        val list = _pokemonIds.value
        val idx = list.indexOf(currentId)
        if (idx <= 0) return null
        return list[idx - 1]
    }

    fun nextOf(currentId: Int): Int? {
        val list = _pokemonIds.value
        val idx = list.indexOf(currentId)
        if (idx == -1 || idx >= list.lastIndex) return null
        return list[idx + 1]
    }
}
