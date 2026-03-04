package fr.pokenity.pokenity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import fr.pokenity.pokenity.presentation.detail.PokemonDetailScreen
import fr.pokenity.pokenity.presentation.detail.PokemonDetailViewModel
import fr.pokenity.pokenity.presentation.map.MapScreen
import fr.pokenity.pokenity.presentation.map.MapViewModel
import fr.pokenity.pokenity.presentation.pokedex.PokedexScreen
import fr.pokenity.pokenity.presentation.pokedex.PokedexViewModel
import fr.pokenity.pokenity.presentation.settings.SettingsScreen
import fr.pokenity.pokenity.presentation.settings.SettingsViewModel
import fr.pokenity.pokenity.ui.theme.PokenityTheme

private enum class MainDestination {
    POKEMONS,
    MAP,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private val pokedexViewModel: PokedexViewModel by viewModels { PokedexViewModel.factory }
    private val mapViewModel: MapViewModel by viewModels { MapViewModel.factory }
    private val settingsViewModel: SettingsViewModel by viewModels { SettingsViewModel.factory }
    private val detailViewModel: PokemonDetailViewModel by viewModels { PokemonDetailViewModel.factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val pokedexUiState by pokedexViewModel.uiState.collectAsState()
            val mapUiState by mapViewModel.uiState.collectAsState()
            val settingsUiState by settingsViewModel.uiState.collectAsState()
            val detailUiState by detailViewModel.uiState.collectAsState()

            val navController = rememberNavController()

            PokenityTheme {
                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    // Home screen with bottom navigation bar
                    composable("home") {
                        var selectedDestination by rememberSaveable {
                            mutableStateOf(MainDestination.POKEMONS)
                        }

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
                                        onLoadMore = pokedexViewModel::loadMorePokemonIfNeeded,
                                        onPokemonClick = { id ->
                                            navController.navigate("detail/$id")
                                        },
                                        onTypeClicked = pokedexViewModel::onTypeClicked,
                                        onGenerationClicked = pokedexViewModel::onGenerationClicked,
                                        onAbilityClicked = pokedexViewModel::onAbilityClicked,
                                        onHabitatClicked = pokedexViewModel::onHabitatClicked,
                                        onRegionClicked = pokedexViewModel::onRegionClicked,
                                        onShapeClicked = pokedexViewModel::onShapeClicked,
                                        onClearTypeFilter = pokedexViewModel::clearTypeFilter,
                                        onClearGenerationFilter = pokedexViewModel::clearGenerationFilter,
                                        onClearAbilityFilter = pokedexViewModel::clearAbilityFilter,
                                        onClearHabitatFilter = pokedexViewModel::clearHabitatFilter,
                                        onClearRegionFilter = pokedexViewModel::clearRegionFilter,
                                        onClearShapeFilter = pokedexViewModel::clearShapeFilter,
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }

                                MainDestination.MAP -> {
                                    MapScreen(
                                        uiState = mapUiState,
                                        onRetry = mapViewModel::loadRegions,
                                        onPokemonClick = { id ->
                                            navController.navigate("detail/$id")
                                        },
                                        onRegionSelected = mapViewModel::onRegionSelected,
                                        onLocationSelected = mapViewModel::onLocationSelected,
                                        onAreaSelected = mapViewModel::onAreaSelected,
                                        onBackToRegions = mapViewModel::backToRegions,
                                        onBackToLocations = mapViewModel::backToLocations,
                                        onBackToAreas = mapViewModel::backToAreas,
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }

                                MainDestination.SETTINGS -> {
                                    SettingsScreen(
                                        uiState = settingsUiState,
                                        onRetry = settingsViewModel::loadLanguages,
                                        onLanguageSelected = settingsViewModel::onLanguageSelected,
                                        onImageTypeSelected = settingsViewModel::onImageTypeSelected,
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                            }
                        }
                    }

                    // Detail screen — each navigation pushes onto the back stack
                    composable(
                        route = "detail/{pokemonId}",
                        arguments = listOf(
                            navArgument("pokemonId") { type = NavType.IntType }
                        )
                    ) { backStackEntry ->
                        val pokemonId = backStackEntry.arguments?.getInt("pokemonId") ?: return@composable

                        LaunchedEffect(pokemonId) {
                            detailViewModel.loadPokemon(pokemonId)
                        }

                        PokemonDetailScreen(
                            uiState = detailUiState,
                            onBack = { navController.popBackStack() },
                            onRetry = { detailViewModel.loadPokemon(pokemonId) },
                            onPokemonClick = { id ->
                                navController.navigate("detail/$id")
                            }
                        )
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
        NavigationBarItem(
            selected = selectedDestination == MainDestination.MAP,
            onClick = { onSelected(MainDestination.MAP) },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Map,
                    contentDescription = "Map"
                )
            },
            label = { Text("Map") }
        )
        NavigationBarItem(
            selected = selectedDestination == MainDestination.SETTINGS,
            onClick = { onSelected(MainDestination.SETTINGS) },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings"
                )
            },
            label = { Text("Settings") }
        )
    }
}
