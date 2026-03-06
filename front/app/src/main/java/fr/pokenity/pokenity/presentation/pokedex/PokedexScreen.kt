package fr.pokenity.pokenity.presentation.pokedex

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import fr.pokenity.pokenity.ui.components.PrimaryButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import fr.pokenity.data.core.PokemonImageSettings
import fr.pokenity.data.core.PokemonImageType
import fr.pokenity.data.model.PokemonFilterOption
import fr.pokenity.data.model.PokemonSummary
import fr.pokenity.pokenity.R
import fr.pokenity.pokenity.presentation.auth.AuthAccentYellow
import fr.pokenity.pokenity.presentation.auth.AuthBodyFontFamily
import fr.pokenity.pokenity.presentation.auth.AuthInputBackground
import fr.pokenity.pokenity.presentation.auth.AuthInputPlaceholder
import fr.pokenity.pokenity.presentation.auth.AuthInputText
import fr.pokenity.pokenity.ui.components.PokemonSpriteImage

private enum class OwnershipFilter {
    ALL,
    OWNED,
    MISSING
}

@Composable
fun PokedexScreen(
    uiState: PokedexUiState,
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
    collectionMode: Boolean = false,
    ownedQuantities: Map<Int, Int> = emptyMap(),
    showOwnershipFilter: Boolean = false,
    totalPokemonCount: Int? = null,
    headerContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { mutableStateOf("") }
    var typeExpanded by rememberSaveable { mutableStateOf(false) }
    var generationExpanded by rememberSaveable { mutableStateOf(false) }
    var abilityExpanded by rememberSaveable { mutableStateOf(false) }
    var habitatExpanded by rememberSaveable { mutableStateOf(false) }
    var regionExpanded by rememberSaveable { mutableStateOf(false) }
    var shapeExpanded by rememberSaveable { mutableStateOf(false) }
    var ownershipFilter by rememberSaveable { mutableStateOf(OwnershipFilter.ALL) }
    val listState = rememberLazyListState()

    val spriteType by PokemonImageSettings.imageType.collectAsState()
    val shinyEnabled by PokemonImageSettings.isShiny.collectAsState()

    val sourcePokemon = if (uiState.hasActiveFilters) uiState.filteredPokemon else uiState.pokemon
    val ownedPokemonSummaries = remember(
        collectionMode,
        showOwnershipFilter,
        ownershipFilter,
        ownedQuantities,
        sourcePokemon
    ) {
        if (!(collectionMode && showOwnershipFilter && ownershipFilter == OwnershipFilter.OWNED)) {
            emptyList()
        } else {
            val knownPokemonById = sourcePokemon.associateBy { it.id }
            ownedQuantities
                .asSequence()
                .filter { (_, quantity) -> quantity > 0 }
                .map { (id, _) ->
                    knownPokemonById[id]
                        ?: PokemonSummary(
                            id = id,
                            name = "Pokemon #$id",
                            imageUrl = ""
                        )
                }
                .sortedBy { it.id }
                .toList()
        }
    }

    val normalizedQuery = remember(query) { query.trim() }
    val searchedPokemon = remember(sourcePokemon, normalizedQuery) {
        if (normalizedQuery.isBlank()) {
            sourcePokemon
        } else {
            sourcePokemon.filter {
                it.name.contains(normalizedQuery, ignoreCase = true) ||
                    it.id.toString().contains(normalizedQuery)
            }
        }
    }
    val displayedPokemon = remember(
        searchedPokemon,
        ownedPokemonSummaries,
        ownedQuantities,
        collectionMode,
        showOwnershipFilter,
        ownershipFilter
    ) {
        if (collectionMode && showOwnershipFilter && ownershipFilter == OwnershipFilter.OWNED) {
            ownedPokemonSummaries
        } else if (!showOwnershipFilter) {
            searchedPokemon
        } else {
            searchedPokemon.filter { pokemon ->
                val isOwned = (ownedQuantities[pokemon.id] ?: 0) > 0
                when (ownershipFilter) {
                    OwnershipFilter.ALL -> true
                    OwnershipFilter.OWNED -> isOwned
                    OwnershipFilter.MISSING -> !isOwned
                }
            }
        }
    }
    val ownedCount = remember(ownedQuantities) { ownedQuantities.count { (_, quantity) -> quantity > 0 } }
    val counterTotal = totalPokemonCount ?: uiState.totalPokemonCount.takeIf { it > 0 } ?: sourcePokemon.size

    Surface(modifier = modifier.fillMaxSize(), color = Color.Transparent) {
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null -> {
                ErrorState(message = uiState.errorMessage, onRetry = onRetry)
            }

            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    if (headerContent != null) {
                        item {
                            Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                                headerContent()
                            }
                        }
                    }

                    item {
                        val inputColors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AuthInputText,
                            unfocusedTextColor = AuthInputText,
                            disabledTextColor = AuthInputText.copy(alpha = 0.7f),
                            focusedContainerColor = AuthInputBackground,
                            unfocusedContainerColor = AuthInputBackground,
                            disabledContainerColor = AuthInputBackground.copy(alpha = 0.8f),
                            cursorColor = AuthInputText,
                            focusedBorderColor = AuthAccentYellow,
                            unfocusedBorderColor = AuthInputBackground,
                            disabledBorderColor = AuthInputBackground.copy(alpha = 0.8f),
                            focusedLabelColor = AuthInputText,
                            unfocusedLabelColor = AuthInputText,
                            disabledLabelColor = AuthInputText
                        )
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(0.dp),
                            singleLine = true,
                            label = {
                                Text(
                                    text = "Recherche",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = AuthBodyFontFamily),
                                    color = AuthInputText,
                                    modifier = Modifier
                                        .background(AuthInputBackground)
                                        .padding(horizontal = 4.dp)
                                )
                            },
                            placeholder = {
                                Text(
                                    text = "Nom ou numero",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = AuthBodyFontFamily),
                                    color = AuthInputPlaceholder
                                )
                            },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = AuthBodyFontFamily),
                            colors = inputColors
                        )
                    }

                    if (collectionMode) {
                        item {
                            Text(
                                text = "$ownedCount/$counterTotal",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                            )
                        }
                    }

                    if (showOwnershipFilter) {
                        item {
                            Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                                OwnershipFilterBar(
                                    selected = ownershipFilter,
                                    onSelected = { ownershipFilter = it }
                                )
                            }
                        }
                    }

                    item {
                        Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                            FiltersSection(
                                uiState = uiState,
                                typeExpanded = typeExpanded,
                                onTypeExpandedChange = { expanded ->
                                    typeExpanded = expanded
                                    if (expanded) onFilterCategorySelected(PokedexSection.TYPE)
                                },
                                onTypeClicked = onTypeClicked,
                                onClearTypeFilter = onClearTypeFilter,
                                generationExpanded = generationExpanded,
                                onGenerationExpandedChange = { expanded ->
                                    generationExpanded = expanded
                                    if (expanded) onFilterCategorySelected(PokedexSection.GENERATION)
                                },
                                onGenerationClicked = onGenerationClicked,
                                onClearGenerationFilter = onClearGenerationFilter,
                                abilityExpanded = abilityExpanded,
                                onAbilityExpandedChange = { expanded ->
                                    abilityExpanded = expanded
                                    if (expanded) onFilterCategorySelected(PokedexSection.ABILITY)
                                },
                                onAbilityClicked = onAbilityClicked,
                                onClearAbilityFilter = onClearAbilityFilter,
                                habitatExpanded = habitatExpanded,
                                onHabitatExpandedChange = { expanded ->
                                    habitatExpanded = expanded
                                    if (expanded) onFilterCategorySelected(PokedexSection.HABITAT)
                                },
                                onHabitatClicked = onHabitatClicked,
                                onClearHabitatFilter = onClearHabitatFilter,
                                regionExpanded = regionExpanded,
                                onRegionExpandedChange = { expanded ->
                                    regionExpanded = expanded
                                    if (expanded) onFilterCategorySelected(PokedexSection.REGION)
                                },
                                onRegionClicked = onRegionClicked,
                                onClearRegionFilter = onClearRegionFilter,
                                shapeExpanded = shapeExpanded,
                                onShapeExpandedChange = { expanded ->
                                    shapeExpanded = expanded
                                    if (expanded) onFilterCategorySelected(PokedexSection.SHAPE)
                                },
                                onShapeClicked = onShapeClicked,
                                onClearShapeFilter = onClearShapeFilter
                            )
                        }
                    }

                    item {
                        Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                            ActiveFilterBar(
                                uiState = uiState,
                                onClearTypeFilter = onClearTypeFilter,
                                onClearGenerationFilter = onClearGenerationFilter,
                                onClearAbilityFilter = onClearAbilityFilter,
                                onClearHabitatFilter = onClearHabitatFilter,
                                onClearRegionFilter = onClearRegionFilter,
                                onClearShapeFilter = onClearShapeFilter
                            )
                        }
                    }

                    PokemonListItems(
                        pokemon = displayedPokemon,
                        onPokemonClick = onPokemonClick,
                        spriteType = spriteType,
                        shinyEnabled = shinyEnabled,
                        collectionMode = collectionMode,
                        ownedQuantities = ownedQuantities
                    )

                    if (uiState.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }

                val shouldDisableLoadMoreForOwnedOnly =
                    collectionMode && showOwnershipFilter && ownershipFilter == OwnershipFilter.OWNED

                if (!uiState.hasActiveFilters && !shouldDisableLoadMoreForOwnedOnly) {
                    LaunchedEffect(
                        listState,
                        displayedPokemon.size,
                        uiState.hasMorePokemon,
                        uiState.isLoadingMore,
                        shouldDisableLoadMoreForOwnedOnly
                    ) {
                        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
                            .collect { lastVisibleIndex ->
                                val totalRenderedItems = listState.layoutInfo.totalItemsCount
                                val threshold = (totalRenderedItems - 4).coerceAtLeast(0)
                                if (lastVisibleIndex >= threshold && uiState.hasMorePokemon && !uiState.isLoadingMore) {
                                    onLoadMore()
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
private fun FiltersSection(
    uiState: PokedexUiState,
    typeExpanded: Boolean,
    onTypeExpandedChange: (Boolean) -> Unit,
    onTypeClicked: (PokemonFilterOption) -> Unit,
    onClearTypeFilter: () -> Unit,
    generationExpanded: Boolean,
    onGenerationExpandedChange: (Boolean) -> Unit,
    onGenerationClicked: (PokemonFilterOption) -> Unit,
    onClearGenerationFilter: () -> Unit,
    abilityExpanded: Boolean,
    onAbilityExpandedChange: (Boolean) -> Unit,
    onAbilityClicked: (PokemonFilterOption) -> Unit,
    onClearAbilityFilter: () -> Unit,
    habitatExpanded: Boolean,
    onHabitatExpandedChange: (Boolean) -> Unit,
    onHabitatClicked: (PokemonFilterOption) -> Unit,
    onClearHabitatFilter: () -> Unit,
    regionExpanded: Boolean,
    onRegionExpandedChange: (Boolean) -> Unit,
    onRegionClicked: (PokemonFilterOption) -> Unit,
    onClearRegionFilter: () -> Unit,
    shapeExpanded: Boolean,
    onShapeExpandedChange: (Boolean) -> Unit,
    onShapeClicked: (PokemonFilterOption) -> Unit,
    onClearShapeFilter: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Filtres",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterSelect(
                    label = "Generation",
                    selected = uiState.selectedGenerationLabel,
                    options = uiState.generations,
                    expanded = generationExpanded,
                    onExpandedChange = onGenerationExpandedChange,
                    onSelected = onGenerationClicked,
                    onClear = onClearGenerationFilter,
                    modifier = Modifier.size(width = 180.dp, height = 84.dp)
                )
            }
            item {
                FilterSelect(
                    label = "Type",
                    selected = uiState.selectedTypeLabel,
                    options = uiState.types,
                    expanded = typeExpanded,
                    onExpandedChange = onTypeExpandedChange,
                    onSelected = onTypeClicked,
                    onClear = onClearTypeFilter,
                    modifier = Modifier.size(width = 180.dp, height = 84.dp)
                )
            }
            item {
                FilterSelect(
                    label = "Ability",
                    selected = uiState.selectedAbilityLabel,
                    options = uiState.abilities,
                    expanded = abilityExpanded,
                    onExpandedChange = onAbilityExpandedChange,
                    onSelected = onAbilityClicked,
                    onClear = onClearAbilityFilter,
                    modifier = Modifier.size(width = 180.dp, height = 84.dp)
                )
            }
            item {
                FilterSelect(
                    label = "Habitat",
                    selected = uiState.selectedHabitatLabel,
                    options = uiState.habitats,
                    expanded = habitatExpanded,
                    onExpandedChange = onHabitatExpandedChange,
                    onSelected = onHabitatClicked,
                    onClear = onClearHabitatFilter,
                    modifier = Modifier.size(width = 180.dp, height = 84.dp)
                )
            }
            item {
                FilterSelect(
                    label = "Region",
                    selected = uiState.selectedRegionLabel,
                    options = uiState.regions,
                    expanded = regionExpanded,
                    onExpandedChange = onRegionExpandedChange,
                    onSelected = onRegionClicked,
                    onClear = onClearRegionFilter,
                    modifier = Modifier.size(width = 180.dp, height = 84.dp)
                )
            }
            item {
                FilterSelect(
                    label = "Shape",
                    selected = uiState.selectedShapeLabel,
                    options = uiState.shapes,
                    expanded = shapeExpanded,
                    onExpandedChange = onShapeExpandedChange,
                    onSelected = onShapeClicked,
                    onClear = onClearShapeFilter,
                    modifier = Modifier.size(width = 180.dp, height = 84.dp)
                )
            }
        }
    }
}

@Composable
private fun FilterSelect(
    label: String,
    selected: String?,
    options: List<PokemonFilterOption>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelected: (PokemonFilterOption) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontFamily = AuthBodyFontFamily),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { onExpandedChange(true) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(0.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (selected != null) AuthAccentYellow else AuthInputBackground
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = AuthInputBackground,
                    contentColor = AuthInputText
                )
            ) {
                Text(
                    text = selected ?: "Tous",
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = AuthBodyFontFamily),
                    color = AuthInputText
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Tous",
                            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = AuthBodyFontFamily),
                            color = AuthInputText
                        )
                    },
                    onClick = {
                        onClear()
                        onExpandedChange(false)
                    }
                )
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = AuthBodyFontFamily),
                                color = AuthInputText
                            )
                        },
                        onClick = {
                            onSelected(option)
                            onExpandedChange(false)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveFilterBar(
    uiState: PokedexUiState,
    onClearTypeFilter: () -> Unit,
    onClearGenerationFilter: () -> Unit,
    onClearAbilityFilter: () -> Unit,
    onClearHabitatFilter: () -> Unit,
    onClearRegionFilter: () -> Unit,
    onClearShapeFilter: () -> Unit
) {
    val activeFilters = buildList<Pair<String, () -> Unit>> {
        uiState.selectedGenerationLabel?.let { add("Generation: $it" to onClearGenerationFilter) }
        uiState.selectedTypeLabel?.let { add("Type: $it" to onClearTypeFilter) }
        uiState.selectedAbilityLabel?.let { add("Ability: $it" to onClearAbilityFilter) }
        uiState.selectedHabitatLabel?.let { add("Habitat: $it" to onClearHabitatFilter) }
        uiState.selectedRegionLabel?.let { add("Region: $it" to onClearRegionFilter) }
        uiState.selectedShapeLabel?.let { add("Shape: $it" to onClearShapeFilter) }
    }

    if (activeFilters.isNotEmpty()) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(activeFilters) { (label, clear) ->
                OutlinedButton(onClick = clear) {
                    Text("$label x")
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.PokemonListItems(
    pokemon: List<PokemonSummary>,
    onPokemonClick: (Int, List<Int>) -> Unit,
    spriteType: PokemonImageType,
    shinyEnabled: Boolean,
    collectionMode: Boolean = false,
    ownedQuantities: Map<Int, Int> = emptyMap()
) {
    if (pokemon.isEmpty()) {
        item {
            Text(
                text = "Aucun Pokemon trouve pour ce filtre.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    } else if (!collectionMode) {
        val ids = pokemon.map { it.id }
        items(items = pokemon, key = { it.id }) { item ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                PokemonRow(
                    pokemon = item,
                    spriteType = spriteType,
                    shinyEnabled = shinyEnabled,
                    onClick = { onPokemonClick(item.id, ids) }
                )
            }
        }
    } else {
        val ownedIds = pokemon.mapNotNull { summary ->
            if ((ownedQuantities[summary.id] ?: 0) > 0) summary.id else null
        }
        val chunked = pokemon.chunked(3)

        itemsIndexed(
            items = chunked,
            key = { _, row -> row.joinToString(separator = "-") { it.id.toString() } },
            contentType = { _, _ -> "closet_row" }
        ) { rowIndex, row ->
            val backgroundRes = when {
                rowIndex == 0 -> R.drawable.closet_top
                rowIndex == chunked.lastIndex -> R.drawable.closet_bottom
                else -> R.drawable.closet_middle
            }
            ClosetPokemonRow(
                row = row,
                backgroundRes = backgroundRes,
                ownedQuantities = ownedQuantities,
                spriteType = spriteType,
                shinyEnabled = shinyEnabled,
                onPokemonClick = { id -> onPokemonClick(id, ownedIds) }
            )
        }
    }
}

@Composable
private fun ClosetPokemonRow(
    row: List<PokemonSummary>,
    backgroundRes: Int,
    ownedQuantities: Map<Int, Int>,
    spriteType: PokemonImageType,
    shinyEnabled: Boolean,
    onPokemonClick: (Int) -> Unit
) {
    val backgroundPainter = painterResource(id = backgroundRes)
    val backgroundRatio = remember(backgroundPainter) {
        val size = backgroundPainter.intrinsicSize
        if (size.isSpecified && size.height > 0f) size.width / size.height else 1f
    }
    val spriteYOffset = if (backgroundRes == R.drawable.closet_top) 36.dp else 4.dp

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(backgroundRatio)
    ) {
        Box(
            modifier = Modifier
                .requiredWidth(maxWidth)
                .fillMaxHeight()
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
                    val pokemon = row.getOrNull(slotIndex)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (pokemon != null) {
                            val quantity = ownedQuantities[pokemon.id] ?: 0
                            val owned = quantity > 0

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable(enabled = owned) {
                                        onPokemonClick(pokemon.id)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                PokemonSpriteImage(
                                    pokemonId = pokemon.id,
                                    contentDescription = pokemon.name,
                                    imageType = spriteType,
                                    shiny = shinyEnabled,
                                    colorFilter = if (owned) null else ColorFilter.tint(Color.Black, BlendMode.SrcIn),
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .size(76.dp)
                                        .offset(y = spriteYOffset)
                                )

                                if (quantity > 1) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(2.dp)
                                    ) {
                                        Text(
                                            text = "x$quantity",
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
}

@Composable
private fun OwnershipFilterBar(
    selected: OwnershipFilter,
    onSelected: (OwnershipFilter) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            OutlinedButton(onClick = { onSelected(OwnershipFilter.ALL) }) {
                Text(if (selected == OwnershipFilter.ALL) "Possession: Tous *" else "Possession: Tous")
            }
        }
        item {
            OutlinedButton(onClick = { onSelected(OwnershipFilter.OWNED) }) {
                Text(if (selected == OwnershipFilter.OWNED) "Possedes *" else "Possedes")
            }
        }
        item {
            OutlinedButton(onClick = { onSelected(OwnershipFilter.MISSING) }) {
                Text(if (selected == OwnershipFilter.MISSING) "Pas possedes *" else "Pas possedes")
            }
        }
    }
}

@Composable
private fun CollectionPokemonCard(
    pokemon: PokemonSummary,
    quantity: Int,
    owned: Boolean,
    spriteType: PokemonImageType,
    shinyEnabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.18f),
        tonalElevation = 0.dp,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.30f)),
        modifier = Modifier
            .size(108.dp)
            .clickable(enabled = owned, onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.16f),
                            Color.White.copy(alpha = 0.06f),
                            Color.Black.copy(alpha = 0.08f)
                        )
                    )
                )
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            PokemonSpriteImage(
                pokemonId = pokemon.id,
                contentDescription = pokemon.name,
                imageType = spriteType,
                shiny = shinyEnabled,
                colorFilter = if (owned) null else ColorFilter.tint(Color.Black, BlendMode.SrcIn),
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            if (quantity > 1) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(3.dp)
                ) {
                    Text(
                        text = "x$quantity",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PokemonRow(
    pokemon: PokemonSummary,
    spriteType: PokemonImageType,
    shinyEnabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.18f),
        tonalElevation = 0.dp,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.14f),
                            Color.White.copy(alpha = 0.05f),
                            Color.Black.copy(alpha = 0.06f)
                        )
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PokemonSpriteImage(
                pokemonId = pokemon.id,
                contentDescription = pokemon.name,
                imageType = spriteType,
                shiny = shinyEnabled,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(84.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "#${pokemon.id}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = pokemon.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(12.dp))
        PrimaryButton(onClick = onRetry) {
            Text("Reessayer")
        }
    }
}
