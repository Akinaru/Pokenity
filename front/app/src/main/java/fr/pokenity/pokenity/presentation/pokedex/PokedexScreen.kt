package fr.pokenity.pokenity.presentation.pokedex

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.pokenity.pokenity.core.PokemonImageSettings
import fr.pokenity.pokenity.domain.model.PokemonFilterOption
import fr.pokenity.pokenity.domain.model.PokemonSummary
import fr.pokenity.pokenity.ui.components.PokemonSpriteImage

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
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { mutableStateOf("") }
    var typeExpanded by rememberSaveable { mutableStateOf(false) }
    var generationExpanded by rememberSaveable { mutableStateOf(false) }
    var abilityExpanded by rememberSaveable { mutableStateOf(false) }
    var habitatExpanded by rememberSaveable { mutableStateOf(false) }
    var regionExpanded by rememberSaveable { mutableStateOf(false) }
    var shapeExpanded by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val spriteType by PokemonImageSettings.imageType.collectAsState()
    val shinyEnabled by PokemonImageSettings.isShiny.collectAsState()

    val sourcePokemon = if (uiState.hasActiveFilters) uiState.filteredPokemon else uiState.pokemon

    val displayedPokemon by remember(sourcePokemon, query) {
        mutableStateOf(
            if (query.isBlank()) sourcePokemon
            else sourcePokemon.filter {
                it.name.contains(query.trim(), ignoreCase = true) || it.id.toString().contains(query.trim())
            }
        )
    }

    Surface(modifier = modifier.fillMaxSize()) {
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
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${displayedPokemon.size} elements",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedButton(
                                onClick = { PokemonImageSettings.toggleShiny() },
                                enabled = spriteType.supportsShiny
                            ) {
                                Text(if (shinyEnabled) "Shiny ON" else "Shiny OFF")
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            label = { Text("Recherche") },
                            placeholder = { Text("Nom ou numero") }
                        )
                    }

                    item {
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

                    item {
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

                    PokemonListItems(displayedPokemon, onPokemonClick, spriteType, shinyEnabled)

                    if (uiState.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }

                if (!uiState.hasActiveFilters) {
                    LaunchedEffect(listState, displayedPokemon.size, uiState.hasMorePokemon, uiState.isLoadingMore) {
                        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
                            .collect { lastVisibleIndex ->
                                val threshold = (displayedPokemon.size - 4).coerceAtLeast(0)
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
            fontWeight = FontWeight.Bold
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
        Text(text = label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { onExpandedChange(true) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selected ?: "Tous")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                DropdownMenuItem(
                    text = { Text("Tous") },
                    onClick = {
                        onClear()
                        onExpandedChange(false)
                    }
                )
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
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
    spriteType: fr.pokenity.pokenity.core.PokemonImageType,
    shinyEnabled: Boolean
) {
    if (pokemon.isEmpty()) {
        item {
            Text(
                text = "Aucun Pokemon trouve pour ce filtre.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }
    } else {
        val ids = pokemon.map { it.id }
        items(items = pokemon, key = { it.id }) { item ->
            PokemonRow(
                pokemon = item,
                spriteType = spriteType,
                shinyEnabled = shinyEnabled,
                onClick = { onPokemonClick(item.id, ids) }
            )
        }
    }
}

@Composable
private fun PokemonRow(
    pokemon: PokemonSummary,
    spriteType: fr.pokenity.pokenity.core.PokemonImageType,
    shinyEnabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
        Button(onClick = onRetry) {
            Text("Reessayer")
        }
    }
}
