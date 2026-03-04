package fr.pokenity.pokenity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import fr.pokenity.pokenity.presentation.detail.PokemonDetailScreen
import fr.pokenity.pokenity.presentation.detail.PokemonDetailViewModel
import fr.pokenity.pokenity.presentation.pokedex.PokedexScreen
import fr.pokenity.pokenity.presentation.pokedex.PokedexViewModel
import fr.pokenity.pokenity.ui.theme.PokenityTheme

private enum class MainDestination {
    POKEMONS
}

class MainActivity : ComponentActivity() {

    private val pokedexViewModel: PokedexViewModel by viewModels { PokedexViewModel.factory }
    private val detailViewModel: PokemonDetailViewModel by viewModels { PokemonDetailViewModel.factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val pokedexUiState by pokedexViewModel.uiState.collectAsState()
            val detailUiState by detailViewModel.uiState.collectAsState()
            var selectedDestination by rememberSaveable { mutableStateOf(MainDestination.POKEMONS) }
            var selectedPokemonId by rememberSaveable { mutableStateOf<Int?>(null) }

            PokenityTheme {
                Crossfade(targetState = selectedPokemonId, label = "nav") { pokemonId ->
                    if (pokemonId != null) {
                        LaunchedEffect(pokemonId) {
                            detailViewModel.loadPokemon(pokemonId)
                        }

                        PokemonDetailScreen(
                            uiState = detailUiState,
                            onBack = { selectedPokemonId = null },
                            onRetry = { detailViewModel.loadPokemon(pokemonId) },
                            onPokemonClick = { id -> selectedPokemonId = id }
                        )
                    } else {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            bottomBar = {
                                MainBottomBar(
                                    selectedDestination = selectedDestination,
                                    onSelected = { selectedDestination = it }
                                )
                            }
                        ) { innerPadding ->
                            when (selectedDestination) {
                                MainDestination.POKEMONS -> {
                                    PokedexScreen(
                                        uiState = pokedexUiState,
                                        onRetry = pokedexViewModel::loadPokedexData,
                                        onSectionSelected = pokedexViewModel::onSectionSelected,
                                        onPokemonClick = { id -> selectedPokemonId = id },
                                        onTypeClicked = pokedexViewModel::onTypeClicked,
                                        onGenerationClicked = pokedexViewModel::onGenerationClicked,
                                        onAbilityClicked = pokedexViewModel::onAbilityClicked,
                                        onHabitatClicked = pokedexViewModel::onHabitatClicked,
                                        onClearTypeFilter = pokedexViewModel::clearTypeFilter,
                                        onClearGenerationFilter = pokedexViewModel::clearGenerationFilter,
                                        onClearAbilityFilter = pokedexViewModel::clearAbilityFilter,
                                        onClearHabitatFilter = pokedexViewModel::clearHabitatFilter,
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MainBottomBar(
    selectedDestination: MainDestination,
    onSelected: (MainDestination) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedDestination == MainDestination.POKEMONS,
            onClick = { onSelected(MainDestination.POKEMONS) },
            icon = {
                Icon(
                    imageVector = Icons.Filled.CatchingPokemon,
                    contentDescription = "Pokemons"
                )
            },
            label = { Text("Pokemons") }
        )
    }
}
