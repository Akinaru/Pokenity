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
import coil.compose.AsyncImage
import fr.pokenity.pokenity.core.PokemonImageSettings
import fr.pokenity.pokenity.core.pokemonImageUrl
import fr.pokenity.pokenity.domain.model.PokemonFilterOption
import fr.pokenity.pokenity.domain.model.PokemonSummary

@Composable
fun PokedexScreen(
    uiState: PokedexUiState,
    onRetry: () -> Unit,
    onSectionSelected: (PokedexSection) -> Unit,
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
    val listState = rememberLazyListState()

    val spriteType by PokemonImageSettings.imageType.collectAsState()
    val shinyEnabled by PokemonImageSettings.isShiny.collectAsState()

    val allFilteredPokemon by remember(uiState.pokemon, query) {
        mutableStateOf(
            if (query.isBlank()) {
                uiState.pokemon
            } else {
                uiState.pokemon.filter {
                    it.name.contains(query.trim(), ignoreCase = true) || it.id.toString().contains(query.trim())
                }
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
                                text = "${currentTotal(uiState = uiState, allPokemonCount = allFilteredPokemon.size)} elements",
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
                        SectionSelector(
                            selectedSection = uiState.selectedSection,
                            onSectionSelected = onSectionSelected
                        )
                    }

                    when (uiState.selectedSection) {
                        PokedexSection.ALL -> {
                            item {
                                OutlinedTextField(
                                    value = query,
                                    onValueChange = { query = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    singleLine = true,
                                    label = { Text("Rechercher un Pokemon") },
                                    placeholder = { Text("Nom ou numero") }
                                )
                            }
                            PokemonListItems(allFilteredPokemon, onPokemonClick, spriteType, shinyEnabled)
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

                        PokedexSection.TYPE -> {
                            FilterSectionContent(
                                selectedLabel = uiState.selectedTypeLabel,
                                headerPrefix = "Type",
                                options = uiState.types,
                                onOptionClicked = onTypeClicked,
                                onBack = onClearTypeFilter
                            )
                        }

                        PokedexSection.GENERATION -> {
                            FilterSectionContent(
                                selectedLabel = uiState.selectedGenerationLabel,
                                headerPrefix = "Generation",
                                options = uiState.generations,
                                onOptionClicked = onGenerationClicked,
                                onBack = onClearGenerationFilter
                            )
                        }

                        PokedexSection.ABILITY -> {
                            FilterSectionContent(
                                selectedLabel = uiState.selectedAbilityLabel,
                                headerPrefix = "Ability",
                                options = uiState.abilities,
                                onOptionClicked = onAbilityClicked,
                                onBack = onClearAbilityFilter
                            )
                        }

                        PokedexSection.HABITAT -> {
                            FilterSectionContent(
                                selectedLabel = uiState.selectedHabitatLabel,
                                headerPrefix = "Habitat",
                                options = uiState.habitats,
                                onOptionClicked = onHabitatClicked,
                                onBack = onClearHabitatFilter
                            )
                        }

                        PokedexSection.REGION -> {
                            FilterSectionContent(
                                selectedLabel = uiState.selectedRegionLabel,
                                headerPrefix = "Region",
                                options = uiState.regions,
                                onOptionClicked = onRegionClicked,
                                onBack = onClearRegionFilter
                            )
                        }

                        PokedexSection.SHAPE -> {
                            FilterSectionContent(
                                selectedLabel = uiState.selectedShapeLabel,
                                headerPrefix = "Shape",
                                options = uiState.shapes,
                                onOptionClicked = onShapeClicked,
                                onBack = onClearShapeFilter
                            )
                        }
                    }

                    if (uiState.selectedSection != PokedexSection.ALL && selectedLabelForSection(uiState) != null) {
                        PokemonListItems(uiState.filteredPokemon, onPokemonClick, spriteType, shinyEnabled)
                    }
                }

                if (uiState.selectedSection == PokedexSection.ALL) {
                    LaunchedEffect(listState, allFilteredPokemon.size, uiState.hasMorePokemon, uiState.isLoadingMore) {
                        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
                            .collect { lastVisibleIndex ->
                                val threshold = (allFilteredPokemon.size - 4).coerceAtLeast(0)
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

private fun androidx.compose.foundation.lazy.LazyListScope.FilterSectionContent(
    selectedLabel: String?,
    headerPrefix: String,
    options: List<PokemonFilterOption>,
    onOptionClicked: (PokemonFilterOption) -> Unit,
    onBack: () -> Unit
) {
    if (selectedLabel == null) {
        items(options, key = { it.apiName }) { option ->
            NameCard(title = option.label, imageUrl = option.imageUrl, onClick = { onOptionClicked(option) })
        }
    } else {
        item {
            FilterHeader(title = "$headerPrefix: $selectedLabel", onBack = onBack)
        }
    }
}

private fun selectedLabelForSection(uiState: PokedexUiState): String? {
    return when (uiState.selectedSection) {
        PokedexSection.ALL -> null
        PokedexSection.TYPE -> uiState.selectedTypeLabel
        PokedexSection.GENERATION -> uiState.selectedGenerationLabel
        PokedexSection.ABILITY -> uiState.selectedAbilityLabel
        PokedexSection.HABITAT -> uiState.selectedHabitatLabel
        PokedexSection.REGION -> uiState.selectedRegionLabel
        PokedexSection.SHAPE -> uiState.selectedShapeLabel
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
                imageUrl = pokemonImageUrl(item.id, spriteType, shinyEnabled),
                onClick = { onPokemonClick(item.id, ids) }
            )
        }
    }
}

private fun currentTotal(uiState: PokedexUiState, allPokemonCount: Int): Int {
    val selectedLabel = selectedLabelForSection(uiState)
    if (uiState.selectedSection == PokedexSection.ALL) {
        return allPokemonCount
    }

    return if (selectedLabel == null) {
        when (uiState.selectedSection) {
            PokedexSection.TYPE -> uiState.types.size
            PokedexSection.GENERATION -> uiState.generations.size
            PokedexSection.ABILITY -> uiState.abilities.size
            PokedexSection.HABITAT -> uiState.habitats.size
            PokedexSection.REGION -> uiState.regions.size
            PokedexSection.SHAPE -> uiState.shapes.size
            PokedexSection.ALL -> allPokemonCount
        }
    } else {
        uiState.filteredPokemon.size
    }
}

@Composable
private fun FilterHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        OutlinedButton(onClick = onBack) {
            Text("Retour")
        }
    }
}

@Composable
private fun SectionSelector(
    selectedSection: PokedexSection,
    onSectionSelected: (PokedexSection) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            SectionButton(
                label = "All",
                selected = selectedSection == PokedexSection.ALL,
                onClick = { onSectionSelected(PokedexSection.ALL) }
            )
        }
        item {
            SectionButton(
                label = "Type",
                selected = selectedSection == PokedexSection.TYPE,
                onClick = { onSectionSelected(PokedexSection.TYPE) }
            )
        }
        item {
            SectionButton(
                label = "Generation",
                selected = selectedSection == PokedexSection.GENERATION,
                onClick = { onSectionSelected(PokedexSection.GENERATION) }
            )
        }
        item {
            SectionButton(
                label = "Ability",
                selected = selectedSection == PokedexSection.ABILITY,
                onClick = { onSectionSelected(PokedexSection.ABILITY) }
            )
        }
        item {
            SectionButton(
                label = "Habitat",
                selected = selectedSection == PokedexSection.HABITAT,
                onClick = { onSectionSelected(PokedexSection.HABITAT) }
            )
        }
        item {
            SectionButton(
                label = "Region",
                selected = selectedSection == PokedexSection.REGION,
                onClick = { onSectionSelected(PokedexSection.REGION) }
            )
        }
        item {
            SectionButton(
                label = "Shape",
                selected = selectedSection == PokedexSection.SHAPE,
                onClick = { onSectionSelected(PokedexSection.SHAPE) }
            )
        }
    }
}

@Composable
private fun SectionButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    if (selected) {
        Button(onClick = onClick) {
            Text(label)
        }
    } else {
        OutlinedButton(onClick = onClick) {
            Text(label)
        }
    }
}

@Composable
private fun PokemonRow(pokemon: PokemonSummary, imageUrl: String, onClick: () -> Unit) {
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
            AsyncImage(
                model = imageUrl,
                contentDescription = pokemon.name,
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
private fun NameCard(title: String, imageUrl: String?, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(40.dp)
                )
            }
            Text(text = title, style = MaterialTheme.typography.titleMedium)
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
