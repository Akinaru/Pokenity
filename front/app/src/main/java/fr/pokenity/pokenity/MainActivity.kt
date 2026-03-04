package fr.pokenity.pokenity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import fr.pokenity.pokenity.core.AuthSessionState
import fr.pokenity.pokenity.core.PokemonBrowseState
import fr.pokenity.pokenity.presentation.detail.PokemonDetailScreen
import fr.pokenity.pokenity.presentation.detail.PokemonDetailViewModel
import fr.pokenity.pokenity.presentation.compare.PokemonCompareScreen
import fr.pokenity.pokenity.presentation.compare.PokemonCompareViewModel
import fr.pokenity.pokenity.presentation.map.MapScreen
import fr.pokenity.pokenity.presentation.map.MapViewModel
import fr.pokenity.pokenity.presentation.account.AccountScreen
import fr.pokenity.pokenity.presentation.account.AccountViewModel
import fr.pokenity.pokenity.presentation.pokedex.PokedexScreen
import fr.pokenity.pokenity.presentation.pokedex.PokedexViewModel
import fr.pokenity.pokenity.presentation.settings.SettingsScreen
import fr.pokenity.pokenity.presentation.settings.SettingsViewModel
import fr.pokenity.pokenity.ui.theme.PokenityTheme

private enum class MainDestination {
    POKEMONS,
    MAP,
    SETTINGS,
    ACCOUNT
}

class MainActivity : ComponentActivity() {

    private val pokedexViewModel: PokedexViewModel by viewModels { PokedexViewModel.factory }
    private val mapViewModel: MapViewModel by viewModels { MapViewModel.factory }
    private val settingsViewModel: SettingsViewModel by viewModels { SettingsViewModel.factory }
    private val accountViewModel: AccountViewModel by viewModels { AccountViewModel.factory }
    private val detailViewModel: PokemonDetailViewModel by viewModels { PokemonDetailViewModel.factory }
    private val compareViewModel: PokemonCompareViewModel by viewModels { PokemonCompareViewModel.factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AuthSessionState.initialize(applicationContext)

        setContent {
            val pokedexUiState by pokedexViewModel.uiState.collectAsState()
            val mapUiState by mapViewModel.uiState.collectAsState()
            val settingsUiState by settingsViewModel.uiState.collectAsState()
            val accountUiState by accountViewModel.uiState.collectAsState()
            val detailUiState by detailViewModel.uiState.collectAsState()
            val compareUiState by compareViewModel.uiState.collectAsState()

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
                                        onFilterCategorySelected = pokedexViewModel::onFilterCategorySelected,
                                        onLoadMore = pokedexViewModel::loadMorePokemonIfNeeded,
                                        onPokemonClick = { id, ids ->
                                            PokemonBrowseState.setList(ids)
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
                                        onPokemonClick = { id, ids ->
                                            PokemonBrowseState.setList(ids)
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

                                MainDestination.ACCOUNT -> {
                                    AccountScreen(
                                        uiState = accountUiState,
                                        onModeChange = accountViewModel::setMode,
                                        onLoginIdentifierChange = accountViewModel::updateLoginIdentifier,
                                        onLoginPasswordChange = accountViewModel::updateLoginPassword,
                                        onRegisterUsernameChange = accountViewModel::updateRegisterUsername,
                                        onRegisterEmailChange = accountViewModel::updateRegisterEmail,
                                        onRegisterPasswordChange = accountViewModel::updateRegisterPassword,
                                        onLogin = accountViewModel::login,
                                        onRegister = accountViewModel::register,
                                        onFetchMe = accountViewModel::fetchMe,
                                        onLogout = accountViewModel::logout,
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
                        val previousId = PokemonBrowseState.previousOf(pokemonId)
                        val nextId = PokemonBrowseState.nextOf(pokemonId)

                        LaunchedEffect(pokemonId) {
                            detailViewModel.loadPokemon(pokemonId)
                        }

                        PokemonDetailScreen(
                            uiState = detailUiState,
                            onBack = { navController.popBackStack() },
                            onRetry = { detailViewModel.loadPokemon(pokemonId) },
                            onOpenComparator = { baseId -> navController.navigate("compare/$baseId") },
                            onPreviousPokemon = if (previousId != null) {
                                { navController.navigate("detail/$previousId") }
                            } else null,
                            onNextPokemon = if (nextId != null) {
                                { navController.navigate("detail/$nextId") }
                            } else null,
                            onPokemonClick = { id ->
                                navController.navigate("detail/$id")
                            }
                        )
                    }

                    composable(
                        route = "compare/{baseId}",
                        arguments = listOf(navArgument("baseId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val baseId = backStackEntry.arguments?.getInt("baseId") ?: return@composable
                        LaunchedEffect(baseId) {
                            compareViewModel.load(basePokemonId = baseId, comparedPokemonId = null)
                        }

                        PokemonCompareScreen(
                            uiState = compareUiState,
                            onBack = { navController.popBackStack() },
                            onRetry = { compareViewModel.load(basePokemonId = baseId, comparedPokemonId = null) },
                            onOpenSelector = { navController.navigate("compare-picker/$baseId") },
                            onOpenPokemonDetail = { id -> navController.navigate("detail/$id") }
                        )
                    }

                    composable(
                        route = "compare/{baseId}/{comparedId}",
                        arguments = listOf(
                            navArgument("baseId") { type = NavType.IntType },
                            navArgument("comparedId") { type = NavType.IntType }
                        )
                    ) { backStackEntry ->
                        val baseId = backStackEntry.arguments?.getInt("baseId") ?: return@composable
                        val comparedId = backStackEntry.arguments?.getInt("comparedId") ?: return@composable

                        LaunchedEffect(baseId, comparedId) {
                            compareViewModel.load(basePokemonId = baseId, comparedPokemonId = comparedId)
                        }

                        PokemonCompareScreen(
                            uiState = compareUiState,
                            onBack = { navController.popBackStack() },
                            onRetry = { compareViewModel.load(basePokemonId = baseId, comparedPokemonId = comparedId) },
                            onOpenSelector = { navController.navigate("compare-picker/$baseId") },
                            onOpenPokemonDetail = { id -> navController.navigate("detail/$id") }
                        )
                    }

                    composable(
                        route = "compare-picker/{baseId}",
                        arguments = listOf(navArgument("baseId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val baseId = backStackEntry.arguments?.getInt("baseId") ?: return@composable
                        ComparePickerScreen(
                            uiState = pokedexUiState,
                            onBack = { navController.popBackStack() },
                            onRetry = pokedexViewModel::loadPokedexData,
                            onFilterCategorySelected = pokedexViewModel::onFilterCategorySelected,
                            onLoadMore = pokedexViewModel::loadMorePokemonIfNeeded,
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
                            onPokemonPicked = { pickedId ->
                                navController.navigate("compare/$baseId/$pickedId")
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComparePickerScreen(
    uiState: fr.pokenity.pokenity.presentation.pokedex.PokedexUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onFilterCategorySelected: (fr.pokenity.pokenity.presentation.pokedex.PokedexSection) -> Unit,
    onLoadMore: () -> Unit,
    onTypeClicked: (fr.pokenity.pokenity.domain.model.PokemonFilterOption) -> Unit,
    onGenerationClicked: (fr.pokenity.pokenity.domain.model.PokemonFilterOption) -> Unit,
    onAbilityClicked: (fr.pokenity.pokenity.domain.model.PokemonFilterOption) -> Unit,
    onHabitatClicked: (fr.pokenity.pokenity.domain.model.PokemonFilterOption) -> Unit,
    onRegionClicked: (fr.pokenity.pokenity.domain.model.PokemonFilterOption) -> Unit,
    onShapeClicked: (fr.pokenity.pokenity.domain.model.PokemonFilterOption) -> Unit,
    onClearTypeFilter: () -> Unit,
    onClearGenerationFilter: () -> Unit,
    onClearAbilityFilter: () -> Unit,
    onClearHabitatFilter: () -> Unit,
    onClearRegionFilter: () -> Unit,
    onClearShapeFilter: () -> Unit,
    onPokemonPicked: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choisir un Pokemon") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        }
    ) { innerPadding ->
        PokedexScreen(
            uiState = uiState,
            onRetry = onRetry,
            onFilterCategorySelected = onFilterCategorySelected,
            onLoadMore = onLoadMore,
            onPokemonClick = { id, _ -> onPokemonPicked(id) },
            onTypeClicked = onTypeClicked,
            onGenerationClicked = onGenerationClicked,
            onAbilityClicked = onAbilityClicked,
            onHabitatClicked = onHabitatClicked,
            onRegionClicked = onRegionClicked,
            onShapeClicked = onShapeClicked,
            onClearTypeFilter = onClearTypeFilter,
            onClearGenerationFilter = onClearGenerationFilter,
            onClearAbilityFilter = onClearAbilityFilter,
            onClearHabitatFilter = onClearHabitatFilter,
            onClearRegionFilter = onClearRegionFilter,
            onClearShapeFilter = onClearShapeFilter,
            modifier = Modifier.padding(innerPadding)
        )
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
        NavigationBarItem(
            selected = selectedDestination == MainDestination.ACCOUNT,
            onClick = { onSelected(MainDestination.ACCOUNT) },
            icon = {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = "Compte"
                )
            },
            label = { Text("Compte") }
        )
    }
}
