package fr.pokenity.pokenity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import fr.pokenity.data.core.AppThemeMode
import fr.pokenity.data.core.AppThemeState
import fr.pokenity.data.core.AuthSessionState
import fr.pokenity.data.core.PokemonBrowseState
import fr.pokenity.data.core.PokemonImageType
import fr.pokenity.data.model.LootBox
import fr.pokenity.data.model.PokemonFilterOption
import fr.pokenity.pokenity.presentation.account.AccountScreen
import fr.pokenity.pokenity.presentation.account.AccountUiState
import fr.pokenity.pokenity.presentation.account.AccountViewModel
import fr.pokenity.pokenity.presentation.auth.AuthFlowViewModel
import fr.pokenity.pokenity.presentation.auth.CharacterSelectionScreen
import fr.pokenity.pokenity.presentation.auth.LoginScreen
import fr.pokenity.pokenity.presentation.auth.ProfileSetupScreen
import fr.pokenity.pokenity.presentation.auth.RegisterScreen
import fr.pokenity.pokenity.presentation.auth.WelcomeScreen
import fr.pokenity.pokenity.presentation.box.BoxDetailScreen
import fr.pokenity.pokenity.presentation.box.BoxDetailViewModel
import fr.pokenity.pokenity.presentation.box.BoxListScreen
import fr.pokenity.pokenity.presentation.box.BoxListViewModel
import fr.pokenity.pokenity.presentation.compare.PokemonCompareScreen
import fr.pokenity.pokenity.presentation.compare.PokemonCompareViewModel
import fr.pokenity.pokenity.presentation.detail.PokemonDetailScreen
import fr.pokenity.pokenity.presentation.detail.PokemonDetailViewModel
import fr.pokenity.pokenity.presentation.map.MapScreen
import fr.pokenity.pokenity.presentation.map.MapViewModel
import fr.pokenity.pokenity.presentation.home.HomeUiState
import fr.pokenity.pokenity.presentation.home.HomeViewModel
import fr.pokenity.pokenity.presentation.pokedex.PokedexScreen
import fr.pokenity.pokenity.presentation.pokedex.PokedexSection
import fr.pokenity.pokenity.presentation.pokedex.PokedexUiState
import fr.pokenity.pokenity.presentation.pokedex.PokedexViewModel
import fr.pokenity.pokenity.presentation.settings.SettingsScreen
import fr.pokenity.pokenity.presentation.settings.SettingsViewModel
import fr.pokenity.pokenity.presentation.social.SocialScreen
import fr.pokenity.pokenity.presentation.social.SocialViewModel
import fr.pokenity.pokenity.ui.components.PokemonSpriteImage
import fr.pokenity.pokenity.ui.media.resolveCharacterMediaModel
import fr.pokenity.pokenity.ui.theme.AppBackground
import fr.pokenity.pokenity.ui.theme.PokenityTheme

private enum class MainDestination {
    SOCIAL,
    ACCUEIL,
    MOI
}

private enum class MoiScreen {
    PROFILE,
    SETTINGS,
    PREFERENCE,
    COMPTE
}

class MainActivity : ComponentActivity() {

    private val pokedexViewModel: PokedexViewModel by viewModels { PokedexViewModel.factory }
    private val mapViewModel: MapViewModel by viewModels { MapViewModel.factory }
    private val settingsViewModel: SettingsViewModel by viewModels { SettingsViewModel.factory }
    private val accountViewModel: AccountViewModel by viewModels { AccountViewModel.factory }
    private val detailViewModel: PokemonDetailViewModel by viewModels { PokemonDetailViewModel.factory }
    private val compareViewModel: PokemonCompareViewModel by viewModels { PokemonCompareViewModel.factory }
    private val authFlowViewModel: AuthFlowViewModel by viewModels { AuthFlowViewModel.factory }
    private val socialViewModel: SocialViewModel by viewModels { SocialViewModel.factory }
    private val homeViewModel: HomeViewModel by viewModels { HomeViewModel.factory }
    private val boxListViewModel: BoxListViewModel by viewModels { BoxListViewModel.factory }
    private val boxDetailViewModel: BoxDetailViewModel by viewModels { BoxDetailViewModel.factory }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AuthSessionState.initialize(applicationContext)

        val startDest = when {
            AuthSessionState.token.value == null -> "welcome"
            AuthSessionState.isNewAccount.value -> "profile-setup"
            else -> "home"
        }

        setContent {
            val pokedexUiState by pokedexViewModel.uiState.collectAsState()
            val mapUiState by mapViewModel.uiState.collectAsState()
            val settingsUiState by settingsViewModel.uiState.collectAsState()
            val accountUiState by accountViewModel.uiState.collectAsState()
            val detailUiState by detailViewModel.uiState.collectAsState()
            val compareUiState by compareViewModel.uiState.collectAsState()
            val authFlowUiState by authFlowViewModel.uiState.collectAsState()
            val socialUiState by socialViewModel.uiState.collectAsState()
            val homeUiState by homeViewModel.uiState.collectAsState()
            val boxListUiState by boxListViewModel.uiState.collectAsState()
            val boxDetailUiState by boxDetailViewModel.uiState.collectAsState()
            val themeMode by AppThemeState.themeMode.collectAsState()

            val systemDark = isSystemInDarkTheme()
            val isDarkTheme = when (themeMode) {
                AppThemeMode.SYSTEM -> systemDark
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            val navController = rememberNavController()

            PokenityTheme(darkTheme = isDarkTheme) {
                AppBackground(darkTheme = isDarkTheme) {
                    NavHost(
                        navController = navController,
                        startDestination = startDest
                    ) {
                        composable("welcome") {
                            WelcomeScreen(
                                onStart = {
                                    authFlowViewModel.onWelcomeDone()
                                    navController.navigate("login") {
                                        popUpTo("welcome") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("login") {
                            LoginScreen(
                                uiState = authFlowUiState,
                                onEmailChange = authFlowViewModel::updateEmail,
                                onCheckEmail = {
                                    authFlowViewModel.checkEmail {
                                        navController.navigate("register")
                                    }
                                },
                                onPasswordChange = authFlowViewModel::updateLoginPassword,
                                onLogin = {
                                    authFlowViewModel.login {
                                        navController.navigate("home") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                        composable("register") {
                            RegisterScreen(
                                uiState = authFlowUiState,
                                onUsernameChange = authFlowViewModel::updateRegisterUsername,
                                onEmailChange = authFlowViewModel::updateRegisterEmail,
                                onConfirmEmail = {
                                    authFlowViewModel.confirmRegisterEmail {
                                        navController.navigate("login") {
                                            popUpTo("register") { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                },
                                onPasswordChange = authFlowViewModel::updateRegisterPassword,
                                onRegister = {
                                    authFlowViewModel.goToCharacterSelection {
                                        navController.navigate("register-character")
                                    }
                                }
                            )
                        }

                        composable("register-character") {
                            CharacterSelectionScreen(
                                uiState = authFlowUiState,
                                onPrevious = authFlowViewModel::selectPreviousCharacter,
                                onNext = authFlowViewModel::selectNextCharacter,
                                onValidate = {
                                    authFlowViewModel.registerWithSelectedCharacter(
                                        onSuccess = {
                                            navController.navigate("home") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        },
                                        onFormError = {
                                            navController.popBackStack()
                                        }
                                    )
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("profile-setup") {
                            ProfileSetupScreen(
                                onNext = {
                                    authFlowViewModel.onProfileSetupDone()
                                    navController.navigate("home") {
                                        popUpTo("profile-setup") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("home") {
                            var selectedDestination by rememberSaveable {
                                mutableStateOf(MainDestination.ACCUEIL)
                            }
                            var moiScreen by rememberSaveable {
                                mutableStateOf(MoiScreen.PROFILE)
                            }

                            LaunchedEffect(
                                selectedDestination,
                                moiScreen,
                                accountUiState.token,
                                accountUiState.user,
                                accountUiState.isLoading
                            ) {
                                val shouldFetchProfile =
                                    selectedDestination == MainDestination.MOI &&
                                        moiScreen == MoiScreen.PROFILE &&
                                        accountUiState.token != null &&
                                        accountUiState.user == null &&
                                        !accountUiState.isLoading
                                if (shouldFetchProfile) {
                                    accountViewModel.fetchMe()
                                }
                            }

                            // Refresh account data (pokemon collection) when a trade changes inventory
                            LaunchedEffect(socialUiState.inventoryVersion) {
                                if (socialUiState.inventoryVersion > 0) {
                                    accountViewModel.fetchMe()
                                }
                            }

                            // Sync current user id into SocialViewModel for trade role detection
                            LaunchedEffect(accountUiState.user?.id) {
                                socialViewModel.setCurrentUserId(accountUiState.user?.id)
                            }

                            Scaffold(
                                modifier = Modifier.fillMaxSize(),
                                containerColor = Color.Transparent,
                                topBar = {
                                    if (selectedDestination == MainDestination.MOI) {
                                        MoiTopBar(
                                            screen = moiScreen,
                                            onBack = {
                                                moiScreen = when (moiScreen) {
                                                    MoiScreen.PROFILE -> MoiScreen.PROFILE
                                                    MoiScreen.SETTINGS -> MoiScreen.PROFILE
                                                    MoiScreen.PREFERENCE -> MoiScreen.SETTINGS
                                                    MoiScreen.COMPTE -> MoiScreen.SETTINGS
                                                }
                                            },
                                            onOpenSettings = {
                                                moiScreen = MoiScreen.SETTINGS
                                            }
                                        )
                                    }
                                },
                                bottomBar = {
                                    MainBottomBar(
                                        selectedDestination = selectedDestination,
                                        onSelected = { destination ->
                                            selectedDestination = destination
                                        }
                                    )
                                }
                            ) { innerPadding ->
                                when (selectedDestination) {
                                    MainDestination.SOCIAL -> {
                                        SocialScreen(
                                            uiState = socialUiState,
                                            onSelectTab = socialViewModel::selectTab,
                                            onAcceptTrade = socialViewModel::showAcceptDialog,
                                            onAcceptTradeWithItem = socialViewModel::acceptTrade,
                                            onDismissAcceptDialog = socialViewModel::dismissAcceptDialog,
                                            onConfirmTrade = socialViewModel::confirmTrade,
                                            onCancelTrade = socialViewModel::cancelTrade,
                                            onDeclineTrade = socialViewModel::declineTrade,
                                            onRefreshMyTrades = socialViewModel::loadMyTrades,
                                            onSelectInventoryItem = socialViewModel::selectInventoryItem,
                                            onPokemonSearchQueryChange = socialViewModel::updatePokemonSearchQuery,
                                            onAddRequestedPokemon = socialViewModel::addRequestedPokemon,
                                            onRemoveRequestedPokemon = socialViewModel::removeRequestedPokemon,
                                            onCreateTrade = socialViewModel::createTrade,
                                            onRefreshOpenTrades = socialViewModel::loadOpenTrades,
                                            onClearMessages = socialViewModel::clearMessages,
                                            modifier = Modifier.padding(innerPadding)
                                        )
                                    }

                                    MainDestination.ACCUEIL -> {
                                        HomeScreen(
                                            uiState = homeUiState,
                                            onRetry = homeViewModel::loadLatestBoxes,
                                            onOpenMapExplorer = {
                                                navController.navigate("map-explorer")
                                            },
                                            onOpenAllBoxes = {
                                                navController.navigate("boxes")
                                            },
                                            onOpenBox = { boxId ->
                                                navController.navigate("box-detail/$boxId")
                                            },
                                            modifier = Modifier.padding(innerPadding)
                                        )
                                    }

                                    MainDestination.MOI -> {
                                        when (moiScreen) {
                                            MoiScreen.PROFILE -> {
                                                MoiProfileScreen(
                                                    accountUiState = accountUiState,
                                                    pokedexUiState = pokedexUiState,
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

                                            MoiScreen.SETTINGS -> {
                                                SettingsMenuScreen(
                                                    onOpenPreference = { moiScreen = MoiScreen.PREFERENCE },
                                                    onOpenCompte = { moiScreen = MoiScreen.COMPTE },
                                                    modifier = Modifier.padding(innerPadding)
                                                )
                                            }

                                            MoiScreen.PREFERENCE -> {
                                                SettingsScreen(
                                                    uiState = settingsUiState,
                                                    onRetry = settingsViewModel::loadLanguages,
                                                    onLanguageSelected = settingsViewModel::onLanguageSelected,
                                                    onImageTypeSelected = settingsViewModel::onImageTypeSelected,
                                                    onThemeModeSelected = settingsViewModel::onThemeModeSelected,
                                                    modifier = Modifier.padding(innerPadding)
                                                )
                                            }

                                            MoiScreen.COMPTE -> {
                                                AccountScreen(
                                                    uiState = accountUiState,
                                                    onFetchMe = accountViewModel::fetchMe,
                                                    onLogout = {
                                                        accountViewModel.logout()
                                                        navController.navigate("welcome") {
                                                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                                            launchSingleTop = true
                                                        }
                                                    },
                                                    onGoToWelcome = {
                                                        navController.navigate("welcome") {
                                                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                                            launchSingleTop = true
                                                        }
                                                    },
                                                    modifier = Modifier.padding(innerPadding)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        composable("map-explorer") {
                            Scaffold(
                                containerColor = Color.Transparent,
                                topBar = {
                                    TopAppBar(
                                        title = { Text("World Map Explorer") },
                                        navigationIcon = {
                                            IconButton(onClick = { navController.popBackStack() }) {
                                                Icon(
                                                    Icons.AutoMirrored.Filled.ArrowBack,
                                                    contentDescription = "Retour"
                                                )
                                            }
                                        },
                                        colors = TopAppBarDefaults.topAppBarColors()
                                    )
                                }
                            ) { innerPadding ->
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
                        }

                        composable("boxes") {
                            Scaffold(
                                containerColor = Color.Transparent,
                                topBar = {
                                    TopAppBar(
                                        title = { Text("Toutes les boxes") },
                                        navigationIcon = {
                                            IconButton(onClick = { navController.popBackStack() }) {
                                                Icon(
                                                    Icons.AutoMirrored.Filled.ArrowBack,
                                                    contentDescription = "Retour"
                                                )
                                            }
                                        },
                                        colors = TopAppBarDefaults.topAppBarColors()
                                    )
                                }
                            ) { innerPadding ->
                                BoxListScreen(
                                    uiState = boxListUiState,
                                    onRetry = boxListViewModel::loadBoxes,
                                    onBoxClick = { boxId ->
                                        navController.navigate("box-detail/$boxId")
                                    },
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }

                        composable(
                            route = "box-detail/{boxId}",
                            arguments = listOf(navArgument("boxId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val boxId = backStackEntry.arguments?.getString("boxId")
                                ?: return@composable
                            LaunchedEffect(boxId) {
                                boxDetailViewModel.loadBox(boxId)
                            }

                            Scaffold(
                                containerColor = Color.Transparent,
                                topBar = {
                                    TopAppBar(
                                        title = { Text(boxDetailUiState.box?.name ?: "Detail box") },
                                        navigationIcon = {
                                            IconButton(onClick = { navController.popBackStack() }) {
                                                Icon(
                                                    Icons.AutoMirrored.Filled.ArrowBack,
                                                    contentDescription = "Retour"
                                                )
                                            }
                                        },
                                        colors = TopAppBarDefaults.topAppBarColors()
                                    )
                                }
                            ) { innerPadding ->
                                BoxDetailScreen(
                                    uiState = boxDetailUiState,
                                    onRetry = { boxDetailViewModel.loadBox(boxId) },
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }

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
                                ownedQuantities = accountUiState.pokemonCollection,
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoiTopBar(
    screen: MoiScreen,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit
) {
    when (screen) {
        MoiScreen.PROFILE -> {
            TopAppBar(
                title = { },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        }

        MoiScreen.SETTINGS -> {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        }

        MoiScreen.PREFERENCE -> {
            TopAppBar(
                title = { Text("Preference") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        }

        MoiScreen.COMPTE -> {
            TopAppBar(
                title = { Text("Compte") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        }
    }
}

@Composable
private fun SocialScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Social (bientot)",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HomeScreen(
    uiState: HomeUiState,
    onRetry: () -> Unit,
    onOpenMapExplorer: () -> Unit,
    onOpenAllBoxes: () -> Unit,
    onOpenBox: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Accueil",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable(onClick = onOpenMapExplorer),
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Map,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "World Map Explorer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Acceder a la page des cartes et des regions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Ouvrir"
                )
            }
        }

        Text(
            text = "Dernieres boxes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(onClick = onOpenAllBoxes) {
                Text("Voir toutes les boxes")
            }
        }

        when {
            uiState.isLoading -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        OutlinedButton(onClick = onRetry) {
                            Text("Reessayer")
                        }
                    }
                }
            }

            uiState.latestBoxes.isEmpty() -> {
                Text(
                    text = "Aucune box trouvee.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            else -> {
                uiState.latestBoxes.forEach { box ->
                    HomeBoxCard(
                        box = box,
                        onClick = { onOpenBox(box.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeBoxCard(
    box: LootBox,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = box.pokeballImage,
                contentDescription = box.name,
                modifier = Modifier.size(46.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = box.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${box.entries.size} drops",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "${"%.2f".format(box.totalDropRate)}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SettingsMenuScreen(
    onOpenPreference: () -> Unit,
    onOpenCompte: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Parametres",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        HorizontalDivider()
        OutlinedButton(
            onClick = onOpenPreference,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Preference")
        }
        OutlinedButton(
            onClick = onOpenCompte,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Compte")
        }
    }
}

@Composable
private fun MoiProfileScreen(
    accountUiState: AccountUiState,
    pokedexUiState: PokedexUiState,
    onRetry: () -> Unit,
    onFilterCategorySelected: (PokedexSection) -> Unit,
    onLoadMore: () -> Unit,
    onPokemonClick: (Int, List<Int>) -> Unit,
    onTypeClicked: (PokemonFilterOption) -> Unit,
    onGenerationClicked: (PokemonFilterOption) -> Unit,
    onAbilityClicked: (PokemonFilterOption) -> Unit,
    onHabitatClicked: (PokemonFilterOption) -> Unit,
    onRegionClicked: (PokemonFilterOption) -> Unit,
    onShapeClicked: (PokemonFilterOption) -> Unit,
    onClearTypeFilter: () -> Unit,
    onClearGenerationFilter: () -> Unit,
    onClearAbilityFilter: () -> Unit,
    onClearHabitatFilter: () -> Unit,
    onClearRegionFilter: () -> Unit,
    onClearShapeFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    PokedexScreen(
        uiState = pokedexUiState,
        onRetry = onRetry,
        onFilterCategorySelected = onFilterCategorySelected,
        onLoadMore = onLoadMore,
        onPokemonClick = onPokemonClick,
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
        collectionMode = true,
        ownedQuantities = accountUiState.pokemonCollection,
        showOwnershipFilter = true,
        totalPokemonCount = pokedexUiState.totalPokemonCount,
        headerContent = { MoiHeaderCard(uiState = accountUiState) },
        modifier = modifier
    )
}

@Composable
private fun MoiHeaderCard(uiState: AccountUiState) {
    val levelProgress = remember(uiState.user?.xp) {
        computeLevelProgress((uiState.user?.xp ?: 0).coerceAtLeast(0))
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color(0x33180707)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val avatarUrl = uiState.user?.characterAvatarUrl ?: uiState.user?.characterImageUrl
            val mediaModel = resolveCharacterMediaModel(avatarUrl)
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(Color(0x22180707))
                    .border(2.dp, Color.White.copy(alpha = 0.55f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (mediaModel != null) {
                    AsyncImage(
                        model = mediaModel,
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Niv. ${levelProgress.currentLevel}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Niv. ${levelProgress.nextLevel}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            LinearProgressIndicator(
                progress = { levelProgress.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(999.dp)),
                trackColor = Color.White.copy(alpha = 0.18f),
                color = MaterialTheme.colorScheme.primary,
                gapSize = 0.dp
            )

            Text(
                text = "${levelProgress.xpInCurrentLevel}/${levelProgress.xpForNextLevel} XP",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        }
    }
}

private data class LevelProgress(
    val currentLevel: Int,
    val nextLevel: Int,
    val xpInCurrentLevel: Int,
    val xpForNextLevel: Int,
    val progressFraction: Float
)

private fun computeLevelProgress(totalXp: Int): LevelProgress {
    var currentLevel = 1
    var remainingXp = totalXp
    var xpForNextLevel = 10

    while (remainingXp >= xpForNextLevel) {
        remainingXp -= xpForNextLevel
        currentLevel += 1
        xpForNextLevel += 1
    }

    val progressFraction = if (xpForNextLevel <= 0) 0f else remainingXp.toFloat() / xpForNextLevel.toFloat()

    return LevelProgress(
        currentLevel = currentLevel,
        nextLevel = currentLevel + 1,
        xpInCurrentLevel = remainingXp,
        xpForNextLevel = xpForNextLevel,
        progressFraction = progressFraction.coerceIn(0f, 1f)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComparePickerScreen(
    uiState: PokedexUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onFilterCategorySelected: (PokedexSection) -> Unit,
    onLoadMore: () -> Unit,
    onTypeClicked: (PokemonFilterOption) -> Unit,
    onGenerationClicked: (PokemonFilterOption) -> Unit,
    onAbilityClicked: (PokemonFilterOption) -> Unit,
    onHabitatClicked: (PokemonFilterOption) -> Unit,
    onRegionClicked: (PokemonFilterOption) -> Unit,
    onShapeClicked: (PokemonFilterOption) -> Unit,
    onClearTypeFilter: () -> Unit,
    onClearGenerationFilter: () -> Unit,
    onClearAbilityFilter: () -> Unit,
    onClearHabitatFilter: () -> Unit,
    onClearRegionFilter: () -> Unit,
    onClearShapeFilter: () -> Unit,
    onPokemonPicked: (Int) -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
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
            selected = selectedDestination == MainDestination.SOCIAL,
            onClick = { onSelected(MainDestination.SOCIAL) },
            icon = {
                Icon(
                    imageVector = Icons.Filled.CatchingPokemon,
                    contentDescription = "Social"
                )
            },
            label = { Text("Social") }
        )
        NavigationBarItem(
            selected = selectedDestination == MainDestination.ACCUEIL,
            onClick = { onSelected(MainDestination.ACCUEIL) },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Map,
                    contentDescription = "Accueil"
                )
            },
            label = { Text("Accueil") }
        )
        NavigationBarItem(
            selected = selectedDestination == MainDestination.MOI,
            onClick = { onSelected(MainDestination.MOI) },
            icon = {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = "Moi"
                )
            },
            label = { Text("Moi") }
        )
    }
}
