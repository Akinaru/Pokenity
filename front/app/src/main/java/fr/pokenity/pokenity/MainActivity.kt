package fr.pokenity.pokenity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import fr.pokenity.data.core.AppThemeMode
import fr.pokenity.data.core.AppThemeState
import fr.pokenity.data.core.AuthSessionState
import fr.pokenity.data.core.PokemonBrowseState
import fr.pokenity.data.core.PokemonImageSettings
import fr.pokenity.data.core.PokemonImageType
import fr.pokenity.data.model.LootBox
import fr.pokenity.data.model.PokemonFilterOption
import fr.pokenity.pokenity.R
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
import fr.pokenity.pokenity.ui.theme.AppTitleFontFamily
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
            val navSpriteType by PokemonImageSettings.imageType.collectAsState()
            val navShinyEnabled by PokemonImageSettings.isShiny.collectAsState()

            val systemDark = isSystemInDarkTheme()
            val isDarkTheme = when (themeMode) {
                AppThemeMode.SYSTEM -> systemDark
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            val navController = rememberNavController()
            var selectedDestination by rememberSaveable {
                mutableStateOf(MainDestination.ACCUEIL)
            }
            var moiScreen by rememberSaveable {
                mutableStateOf(MoiScreen.PROFILE)
            }
            val onMainDestinationSelected: (MainDestination) -> Unit = { destination ->
                selectedDestination = destination
                if (navController.currentDestination?.route != "home") {
                    navController.navigate("home") {
                        launchSingleTop = true
                    }
                }
            }

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
                                }
                            ) { innerPadding ->
                                Box(modifier = Modifier.fillMaxSize()) {
                                    when (selectedDestination) {
                                        MainDestination.SOCIAL -> {
                                            SocialScreen(
                                                uiState = socialUiState,
                                                pokedexUiState = pokedexUiState,
                                                onSelectTab = socialViewModel::selectTab,
                                                onAcceptTrade = socialViewModel::showAcceptDialog,
                                                onAcceptTradeWithItem = socialViewModel::acceptTrade,
                                                onDismissAcceptDialog = socialViewModel::dismissAcceptDialog,
                                                onConfirmTrade = socialViewModel::confirmTrade,
                                                onCancelTrade = socialViewModel::cancelTrade,
                                                onDeclineTrade = socialViewModel::declineTrade,
                                                onRefreshMyTrades = socialViewModel::loadMyTrades,
                                                onSelectInventoryItem = socialViewModel::selectInventoryItem,
                                                onAddRequestedPokemon = socialViewModel::addRequestedPokemon,
                                                onRemoveRequestedPokemonAt = socialViewModel::removeRequestedPokemonAt,
                                                onOpenInventorySelector = socialViewModel::openInventorySelector,
                                                onCloseInventorySelector = socialViewModel::closeInventorySelector,
                                                onOpenPokedexSelector = socialViewModel::openPokedexSelector,
                                                onClosePokedexSelector = socialViewModel::closePokedexSelector,
                                                onCreateTrade = socialViewModel::createTrade,
                                                onRefreshOpenTrades = socialViewModel::loadOpenTrades,
                                                onClearMessages = socialViewModel::clearMessages,
                                                onFilterCategorySelected = pokedexViewModel::onFilterCategorySelected,
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

                                        MainDestination.ACCUEIL -> {
                                            HomeScreen(
                                                uiState = homeUiState,
                                                onRetry = homeViewModel::loadLatestBoxes,
                                                onOpenPokedex = {
                                                    navController.navigate("pokedex")
                                                },
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
                                                        onPokemonClick = { id ->
                                                            PokemonBrowseState.setList(
                                                                accountUiState.pokemonCollection
                                                                    .asSequence()
                                                                    .filter { (_, quantity) -> quantity > 0 }
                                                                    .map { (pokemonId, _) -> pokemonId }
                                                                    .sorted()
                                                                    .toList()
                                                            )
                                                            navController.navigate("detail/$id")
                                                        },
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

                                    MainBottomBar(
                                        selectedDestination = selectedDestination,
                                        onSelected = onMainDestinationSelected,
                                        isDarkTheme = isDarkTheme,
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    )
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
                                Box(modifier = Modifier.fillMaxSize()) {
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
                                    MainBottomBar(
                                        selectedDestination = selectedDestination,
                                        onSelected = onMainDestinationSelected,
                                        isDarkTheme = isDarkTheme,
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    )
                                }
                            }
                        }

                        composable("pokedex") {
                            Scaffold(
                                containerColor = Color.Transparent,
                                topBar = {
                                    TopAppBar(
                                        title = { Text("Pokedex") },
                                        navigationIcon = {
                                            IconButton(onClick = { navController.popBackStack() }) {
                                                Icon(
                                                    Icons.AutoMirrored.Filled.ArrowBack,
                                                    contentDescription = "Retour"
                                                )
                                            }
                                        },
                                        actions = {
                                            ShinyToggleTopBarAction(
                                                isShinyEnabled = navShinyEnabled,
                                                isEnabled = navSpriteType.supportsShiny,
                                                onToggle = { PokemonImageSettings.toggleShiny() }
                                            )
                                        },
                                        colors = TopAppBarDefaults.topAppBarColors(
                                            containerColor = Color(0xFF1B4167),
                                            titleContentColor = Color.White,
                                            navigationIconContentColor = Color.White,
                                            actionIconContentColor = Color.White
                                        )
                                    )
                                }
                            ) { innerPadding ->
                                Box(modifier = Modifier.fillMaxSize()) {
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
                                        collectionMode = true,
                                        ownedQuantities = accountUiState.pokemonCollection,
                                        showOwnershipFilter = false,
                                        totalPokemonCount = pokedexUiState.totalPokemonCount,
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                    MainBottomBar(
                                        selectedDestination = selectedDestination,
                                        onSelected = onMainDestinationSelected,
                                        isDarkTheme = isDarkTheme,
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    )
                                }
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
                                Box(modifier = Modifier.fillMaxSize()) {
                                    BoxListScreen(
                                        uiState = boxListUiState,
                                        onRetry = boxListViewModel::loadBoxes,
                                        onBoxClick = { boxId ->
                                            navController.navigate("box-detail/$boxId")
                                        },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                    MainBottomBar(
                                        selectedDestination = selectedDestination,
                                        onSelected = onMainDestinationSelected,
                                        isDarkTheme = isDarkTheme,
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    )
                                }
                            }
                        }

                        composable(
                            route = "box-detail/{boxId}",
                            arguments = listOf(navArgument("boxId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val boxId = backStackEntry.arguments?.getString("boxId")
                                ?: return@composable
                            var boxDetailBottomBarHeightPx by remember { mutableIntStateOf(0) }
                            val density = LocalDensity.current
                            val boxDetailBottomBarHeight = if (boxDetailBottomBarHeightPx > 0) {
                                with(density) { boxDetailBottomBarHeightPx.toDp() }
                            } else {
                                96.dp
                            }
                            val isDrawOpen = boxDetailUiState.isOpening || boxDetailUiState.isSpinning
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
                                Box(modifier = Modifier.fillMaxSize()) {
                                    BoxDetailScreen(
                                        uiState = boxDetailUiState,
                                        onRetry = { boxDetailViewModel.loadBox(boxId) },
                                        onOpenBox = boxDetailViewModel::openBox,
                                        onSpinAnimationCompleted = boxDetailViewModel::onSpinAnimationCompleted,
                                        onDismissRewardDialog = boxDetailViewModel::dismissRewardDialog,
                                        bottomInset = boxDetailBottomBarHeight,
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                    if (!isDrawOpen) {
                                        MainBottomBar(
                                            selectedDestination = selectedDestination,
                                            isDarkTheme = isDarkTheme,
                                            onSelected = onMainDestinationSelected,
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .onSizeChanged { boxDetailBottomBarHeightPx = it.height }
                                        )
                                    }
                                }
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

                            Scaffold(
                                containerColor = Color.Transparent
                            ) { innerPadding ->
                                Box(modifier = Modifier.fillMaxSize()) {
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
                                    MainBottomBar(
                                        selectedDestination = selectedDestination,
                                        onSelected = onMainDestinationSelected,
                                        isDarkTheme = isDarkTheme,
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    )
                                }
                            }
                        }

                        composable(
                            route = "compare/{baseId}",
                            arguments = listOf(navArgument("baseId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val baseId = backStackEntry.arguments?.getInt("baseId") ?: return@composable
                            LaunchedEffect(baseId) {
                                compareViewModel.load(basePokemonId = baseId, comparedPokemonId = null)
                            }

                            Scaffold(
                                containerColor = Color.Transparent
                            ) { innerPadding ->
                                Box(modifier = Modifier.fillMaxSize()) {
                                    PokemonCompareScreen(
                                        uiState = compareUiState,
                                        onBack = { navController.popBackStack() },
                                        onRetry = { compareViewModel.load(basePokemonId = baseId, comparedPokemonId = null) },
                                        onOpenSelector = { navController.navigate("compare-picker/$baseId") },
                                        onOpenPokemonDetail = { id -> navController.navigate("detail/$id") }
                                    )
                                    MainBottomBar(
                                        selectedDestination = selectedDestination,
                                        onSelected = onMainDestinationSelected,
                                        isDarkTheme = isDarkTheme,
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    )
                                }
                            }
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

                            Scaffold(
                                containerColor = Color.Transparent
                            ) { innerPadding ->
                                Box(modifier = Modifier.fillMaxSize()) {
                                    PokemonCompareScreen(
                                        uiState = compareUiState,
                                        onBack = { navController.popBackStack() },
                                        onRetry = { compareViewModel.load(basePokemonId = baseId, comparedPokemonId = comparedId) },
                                        onOpenSelector = { navController.navigate("compare-picker/$baseId") },
                                        onOpenPokemonDetail = { id -> navController.navigate("detail/$id") }
                                    )
                                    MainBottomBar(
                                        selectedDestination = selectedDestination,
                                        onSelected = onMainDestinationSelected,
                                        isDarkTheme = isDarkTheme,
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    )
                                }
                            }
                        }

                        composable(
                            route = "compare-picker/{baseId}",
                            arguments = listOf(navArgument("baseId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val baseId = backStackEntry.arguments?.getInt("baseId") ?: return@composable
                            Scaffold(
                                containerColor = Color.Transparent
                            ) { innerPadding ->
                                Box(modifier = Modifier.fillMaxSize()) {
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
                                    MainBottomBar(
                                        selectedDestination = selectedDestination,
                                        onSelected = onMainDestinationSelected,
                                        isDarkTheme = isDarkTheme,
                                        modifier = Modifier.align(Alignment.BottomCenter)
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
    onOpenPokedex: () -> Unit,
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
                .clickable(onClick = onOpenPokedex),
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
                    imageVector = Icons.Filled.CatchingPokemon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pokedex",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Acceder au Pokedex complet avec tous les filtres",
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
    onPokemonClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val spriteType by PokemonImageSettings.imageType.collectAsState()
    val shinyEnabled by PokemonImageSettings.isShiny.collectAsState()
    val ownedPokemon = remember(accountUiState.pokemonCollection) {
        accountUiState.pokemonCollection
            .asSequence()
            .filter { (_, quantity) -> quantity > 0 }
            .sortedBy { (pokemonId, _) -> pokemonId }
            .map { (pokemonId, quantity) -> pokemonId to quantity }
            .toList()
    }
    val gridState = rememberLazyGridState()

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        state = gridState,
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 136.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                MoiHeaderCard(uiState = accountUiState)
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Collection: ${ownedPokemon.size} Pokemon",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (ownedPokemon.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0x33180707)
                    ) {
                        Text(
                            text = "Aucun Pokemon possede pour le moment.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
        } else {
            item(span = { GridItemSpan(maxLineSpan) }) {
                ProfileClosetCollection(
                    ownedPokemon = ownedPokemon,
                    spriteType = spriteType,
                    shinyEnabled = shinyEnabled,
                    onPokemonClick = onPokemonClick
                )
            }
        }
    }
}

@Composable
private fun ProfileClosetCollection(
    ownedPokemon: List<Pair<Int, Int>>,
    spriteType: PokemonImageType,
    shinyEnabled: Boolean,
    onPokemonClick: (Int) -> Unit
) {
    val rows = remember(ownedPokemon) { ownedPokemon.chunked(3) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        rows.forEachIndexed { rowIndex, row ->
            val backgroundRes = when {
                rowIndex == 0 -> R.drawable.closet_top
                rowIndex == rows.lastIndex -> R.drawable.closet_bottom
                else -> R.drawable.closet_middle
            }
            ProfileClosetRow(
                row = row,
                backgroundRes = backgroundRes,
                spriteType = spriteType,
                shinyEnabled = shinyEnabled,
                onPokemonClick = onPokemonClick
            )
        }
    }
}

@Composable
private fun ProfileClosetRow(
    row: List<Pair<Int, Int>>,
    backgroundRes: Int,
    spriteType: PokemonImageType,
    shinyEnabled: Boolean,
    onPokemonClick: (Int) -> Unit
) {
    val backgroundPainter = painterResource(id = backgroundRes)
    val backgroundRatio = remember(backgroundPainter) {
        val size = backgroundPainter.intrinsicSize
        if (size.isSpecified && size.height > 0f) size.width / size.height else 1f
    }
    val spriteYOffset = if (backgroundRes == R.drawable.closet_top) 38.dp else 4.dp
    val badgeYOffset = if (backgroundRes == R.drawable.closet_top) 38.dp else 0.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(backgroundRatio)
    ) {
        Image(
            painter = backgroundPainter,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillWidth
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { slotIndex ->
                val pokemonEntry = row.getOrNull(slotIndex)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    if (pokemonEntry != null) {
                        val pokemonId = pokemonEntry.first
                        val quantity = pokemonEntry.second
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { onPokemonClick(pokemonId) },
                            contentAlignment = Alignment.Center
                        ) {
                            PokemonSpriteImage(
                                pokemonId = pokemonId,
                                contentDescription = "Pokemon #$pokemonId",
                                imageType = spriteType,
                                shiny = shinyEnabled,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(76.dp)
                                    .offset(y = spriteYOffset)
                            )

                            if (quantity > 1) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 2.dp + badgeYOffset, end = 2.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.badge_duplicate),
                                        contentDescription = "Duplicate badge",
                                        modifier = Modifier.size(40.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                    Text(
                                        text = "x$quantity",
                                        color = Color.Black,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontFamily = AppTitleFontFamily,
                                            fontSize = 18.sp,
                                            shadow = androidx.compose.ui.graphics.Shadow(
                                                color = Color.White,
                                                offset = androidx.compose.ui.geometry.Offset(0f, 0f),
                                                blurRadius = 4f
                                            )
                                        ),
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.align(Alignment.Center)
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
private fun OwnedPokemonCard(
    pokemonId: Int,
    quantity: Int,
    spriteType: PokemonImageType,
    shinyEnabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color(0x33180707)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PokemonSpriteImage(
                pokemonId = pokemonId,
                contentDescription = "Pokemon #$pokemonId",
                imageType = spriteType,
                shiny = shinyEnabled,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "#$pokemonId",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "x$quantity",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )
        }
    }
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
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Niv. ${levelProgress.nextLevel}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
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
private fun ShinyToggleTopBarAction(
    isShinyEnabled: Boolean,
    isEnabled: Boolean,
    onToggle: () -> Unit
) {
    val frames = remember {
        intArrayOf(
            R.drawable.shiny_1,
            R.drawable.shiny_2,
            R.drawable.shiny_3,
            R.drawable.shiny_4,
            R.drawable.shiny_5
        )
    }
    var frameIndex by remember { mutableIntStateOf(if (isShinyEnabled) 4 else 0) }
    var isAnimating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isShinyEnabled, isAnimating) {
        if (!isAnimating) {
            frameIndex = if (isShinyEnabled) 4 else 0
        }
    }

    Image(
        painter = painterResource(id = frames[frameIndex]),
        contentDescription = if (isShinyEnabled) "Shiny ON" else "Shiny OFF",
        modifier = Modifier
            .padding(end = 8.dp)
            .size(width = 112.dp, height = 40.dp)
            .alpha(if (isEnabled) 1f else 0.45f)
            .clickable(
                enabled = isEnabled && !isAnimating,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                coroutineScope.launch {
                    isAnimating = true
                    if (isShinyEnabled) {
                        for (index in 3 downTo 0) {
                            frameIndex = index
                            delay(55)
                        }
                    } else {
                        for (index in 1..4) {
                            frameIndex = index
                            delay(55)
                        }
                    }
                    onToggle()
                    isAnimating = false
                }
            },
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun MainBottomBar(
    selectedDestination: MainDestination,
    onSelected: (MainDestination) -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val footerPainter = painterResource(
        id = if (isDarkTheme) R.drawable.footer else R.drawable.footer_white
    )
    val footerAspectRatio = remember(footerPainter) {
        val intrinsicSize = footerPainter.intrinsicSize
        if (intrinsicSize.isSpecified && intrinsicSize.height > 0f) {
            intrinsicSize.width / intrinsicSize.height
        } else {
            1f
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
            .aspectRatio(footerAspectRatio)
    ) {
        Image(
            painter = footerPainter,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            alignment = Alignment.BottomCenter
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 40.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FooterSocialButton(
                isSelected = selectedDestination == MainDestination.SOCIAL,
                onClick = { onSelected(MainDestination.SOCIAL) },
                contentDescription = "Social"
            )
            FooterAccueilButton(
                isSelected = selectedDestination == MainDestination.ACCUEIL,
                onClick = { onSelected(MainDestination.ACCUEIL) },
                contentDescription = "Accueil"
            )
            FooterIconButton(
                isSelected = selectedDestination == MainDestination.MOI,
                onClick = { onSelected(MainDestination.MOI) },
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "Moi"
            )
        }
    }
}

@Composable
private fun FooterSocialButton(
    isSelected: Boolean,
    onClick: () -> Unit,
    contentDescription: String
) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(
                id = if (isSelected) R.drawable.social_on else R.drawable.social_off
            ),
            contentDescription = contentDescription,
            modifier = Modifier
                .size(50.dp)
                .offset(y = 26.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun FooterAccueilButton(
    isSelected: Boolean,
    onClick: () -> Unit,
    contentDescription: String
) {
    val frames = remember {
        intArrayOf(
            R.drawable.acceuil1,
            R.drawable.acceuil2,
            R.drawable.acceuil3
        )
    }
    var frameIndex by remember { mutableIntStateOf(0) }
    var animationCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(animationCount) {
        if (animationCount == 0) return@LaunchedEffect
        frameIndex = 0
        delay(180)
        frameIndex = 1
        delay(180)
        frameIndex = 2
        delay(180)
        frameIndex = 0
    }

    Box(
        modifier = Modifier
            .size(112.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                animationCount += 1
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = frames[frameIndex]),
            contentDescription = contentDescription,
            modifier = Modifier.size(104.dp),
            contentScale = ContentScale.Fit,
            alpha = if (isSelected) 1f else 0.9f
        )
    }
}

@Composable
private fun FooterIconButton(
    isSelected: Boolean,
    onClick: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String
) {
    IconButton(onClick = onClick, modifier = Modifier.size(44.dp)) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (isSelected) Color(0x22FFFFFF) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            val tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.82f)
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = tint
            )
        }
    }
}
