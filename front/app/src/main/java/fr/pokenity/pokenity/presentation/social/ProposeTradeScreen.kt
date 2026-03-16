package fr.pokenity.pokenity.presentation.social

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import fr.pokenity.pokenity.ui.components.PrimaryButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import fr.pokenity.data.model.InventoryItem
import fr.pokenity.data.model.PokemonFilterOption
import fr.pokenity.data.model.PokemonSummary
import fr.pokenity.pokenity.presentation.pokedex.ActiveFilterBar
import fr.pokenity.pokenity.presentation.pokedex.FiltersSection
import fr.pokenity.pokenity.presentation.pokedex.PokedexSection
import fr.pokenity.pokenity.presentation.pokedex.PokedexUiState

@Composable
fun ProposeTradeScreen(
    uiState: SocialUiState,
    pokedexUiState: PokedexUiState,
    onSelectInventoryItem: (InventoryItem?) -> Unit,
    onUpdateOfferedQuantity: (itemId: String, quantity: Int) -> Unit,
    onAddRequestedPokemon: (PokemonSummary) -> Unit,
    onUpdateRequestedQuantity: (resourceId: Int, quantity: Int) -> Unit,
    onRemoveRequestedPokemonAt: (Int) -> Unit,
    onOpenInventorySelector: () -> Unit,
    onCloseInventorySelector: () -> Unit,
    onOpenPokedexSelector: () -> Unit,
    onClosePokedexSelector: () -> Unit,
    onCreateTrade: () -> Unit,
    // Pokedex filter callbacks
    onFilterCategorySelected: (PokedexSection) -> Unit,
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
    // Fullscreen inventory selector dialog
    if (uiState.showInventorySelector) {
        InventorySelectorScreen(
            inventory = uiState.myInventory,
            selectedItems = uiState.selectedInventoryItems,
            isLoading = uiState.isLoading && uiState.myInventory.isEmpty(),
            onSelectItem = onSelectInventoryItem,
            onClose = onCloseInventorySelector
        )
    }

    // Fullscreen pokedex selector dialog
    if (uiState.showPokedexSelector) {
        PokedexSelectorScreen(
            uiState = uiState,
            pokedexUiState = pokedexUiState,
            onAddRequestedPokemon = onAddRequestedPokemon,
            onRemoveRequestedPokemonAt = onRemoveRequestedPokemonAt,
            onClose = onClosePokedexSelector,
            onFilterCategorySelected = onFilterCategorySelected,
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
            onClearShapeFilter = onClearShapeFilter
        )
    }

    if (uiState.isLoading && uiState.myInventory.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Pokemon a proposer
        Text(
            text = "Choisir un Pokemon a proposer",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        val selectedItems = uiState.selectedInventoryItems
        if (selectedItems.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Selection (${selectedItems.size}/5)",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { onSelectInventoryItem(null) }) {
                            Text(fr.pokenity.pokenity.ui.i18n.uiText("Retirer tout"))
                        }
                    }
                    selectedItems.forEach { selectedItem ->
                        val qty = uiState.selectedOfferedQuantities[selectedItem.id] ?: 1
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = selectedItem.imageUrl,
                                contentDescription = selectedItem.resourceName,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedItem.isShiny) "${selectedItem.resourceName} ✨" else selectedItem.resourceName,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onUpdateOfferedQuantity(selectedItem.id, qty - 1) },
                                    enabled = qty > 1,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text("-", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = "$qty",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(28.dp),
                                    textAlign = TextAlign.Center
                                )
                                IconButton(
                                    onClick = { onUpdateOfferedQuantity(selectedItem.id, qty + 1) },
                                    enabled = qty < selectedItem.quantity,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text("+", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = "/${selectedItem.quantity}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { onSelectInventoryItem(selectedItem) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = fr.pokenity.pokenity.ui.i18n.uiText("Retirer"),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onOpenInventorySelector,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (selectedItems.isNotEmpty()) "Modifier la selection" else "Selectionner jusqu'a 5 Pokemon")
        }

        HorizontalDivider()

        // Section 2: Pokemon souhaites
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pokemon souhaites",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${uiState.selectedRequestedPokemons.size}/5",
                style = MaterialTheme.typography.labelMedium,
                color = if (uiState.selectedRequestedPokemons.size >= 5)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (uiState.selectedRequestedPokemons.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    uiState.selectedRequestedPokemons.forEachIndexed { index, pokemon ->
                        val qty = uiState.selectedRequestedQuantities[pokemon.resourceId] ?: 1
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = pokemon.imageUrl,
                                contentDescription = pokemon.resourceName,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = pokemon.resourceName,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onUpdateRequestedQuantity(pokemon.resourceId, qty - 1) },
                                    enabled = qty > 1,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text("-", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = "$qty",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(28.dp),
                                    textAlign = TextAlign.Center
                                )
                                IconButton(
                                    onClick = { onUpdateRequestedQuantity(pokemon.resourceId, qty + 1) },
                                    enabled = qty < 999,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text("+", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                            IconButton(
                                onClick = { onRemoveRequestedPokemonAt(index) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = fr.pokenity.pokenity.ui.i18n.uiText("Retirer"),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (uiState.selectedRequestedPokemons.size < 5) {
            OutlinedButton(
                onClick = onOpenPokedexSelector,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    if (uiState.selectedRequestedPokemons.isEmpty())
                        "Selectionner les Pokemon souhaites"
                    else
                        "Ajouter un Pokemon souhaite"
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Summary card
        if (selectedItems.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Resume de l'echange",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Pokemon proposes : ${
                            selectedItems.joinToString(", ") { item ->
                                val qty = uiState.selectedOfferedQuantities[item.id] ?: 1
                                val name = if (item.isShiny) "${item.resourceName} ✨" else item.resourceName
                                "$name x$qty"
                            }
                        }",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (uiState.selectedRequestedPokemons.isNotEmpty()) {
                        Text(
                            text = "Pokemon souhaites : ${
                                uiState.selectedRequestedPokemons.joinToString(", ") { pokemon ->
                                    val qty = uiState.selectedRequestedQuantities[pokemon.resourceId] ?: 1
                                    "${pokemon.resourceName} x$qty"
                                }
                            }",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            text = "Pokemon souhaites : aucun selectionne",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        PrimaryButton(
            onClick = onCreateTrade,
            enabled = uiState.selectedInventoryItems.isNotEmpty()
                    && uiState.selectedRequestedPokemons.isNotEmpty()
                    && !uiState.isLoading,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text(fr.pokenity.pokenity.ui.i18n.uiText("Proposer l'echange"))
            }
        }
    }
}

// --- Inventory Selector (fullscreen dialog) ---

@Composable
private fun InventorySelectorScreen(
    inventory: List<InventoryItem>,
    selectedItems: List<InventoryItem>,
    isLoading: Boolean,
    onSelectItem: (InventoryItem) -> Unit,
    onClose: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredInventory = remember(searchQuery, inventory) {
        if (searchQuery.isBlank()) {
            inventory
        } else {
            val lower = searchQuery.lowercase().trim()
            inventory.filter { it.resourceName.lowercase().contains(lower) }
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Choisir un Pokemon",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = fr.pokenity.pokenity.ui.i18n.uiText("Fermer"))
                    }
                }

                HorizontalDivider()

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(fr.pokenity.pokenity.ui.i18n.uiText("Rechercher...")) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                    }
                )

                HorizontalDivider()

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (filteredInventory.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (inventory.isEmpty()) "Votre inventaire est vide."
                            else "Aucun resultat.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        state = rememberLazyListState()
                    ) {
                        items(filteredInventory, key = { it.id }) { item ->
                            val isSelected = selectedItems.any { it.id == item.id }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectItem(item) }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = item.imageUrl,
                                    contentDescription = item.resourceName,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (item.isShiny) "${item.resourceName} ✨" else item.resourceName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "x${item.quantity}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Text(
                                        text = "Selectionne",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(onClick = onClose, modifier = Modifier.weight(1f)) {
                        Text(fr.pokenity.pokenity.ui.i18n.uiText("Annuler"))
                    }
                    PrimaryButton(onClick = onClose, modifier = Modifier.weight(1f)) {
                        Text("${fr.pokenity.pokenity.ui.i18n.uiText("Valider")} (${selectedItems.size}/5)")
                    }
                }
            }
        }
    }
}

// --- Pokedex Selector (fullscreen dialog with filters) ---

@Composable
private fun PokedexSelectorScreen(
    uiState: SocialUiState,
    pokedexUiState: PokedexUiState,
    onAddRequestedPokemon: (PokemonSummary) -> Unit,
    onRemoveRequestedPokemonAt: (Int) -> Unit,
    onClose: () -> Unit,
    onFilterCategorySelected: (PokedexSection) -> Unit,
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
    onClearShapeFilter: () -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var typeExpanded by rememberSaveable { mutableStateOf(false) }
    var generationExpanded by rememberSaveable { mutableStateOf(false) }
    var abilityExpanded by rememberSaveable { mutableStateOf(false) }
    var habitatExpanded by rememberSaveable { mutableStateOf(false) }
    var regionExpanded by rememberSaveable { mutableStateOf(false) }
    var shapeExpanded by rememberSaveable { mutableStateOf(false) }

    // Use filtered pokemon from pokedex when filters are active, otherwise use allPokemonList
    val sourcePokemon = if (pokedexUiState.hasActiveFilters)
        pokedexUiState.filteredPokemon
    else
        uiState.allPokemonList

    val displayedList = remember(searchQuery, sourcePokemon) {
        if (searchQuery.isBlank()) {
            sourcePokemon
        } else {
            val lower = searchQuery.lowercase().trim()
            val asNumber = lower.toIntOrNull()
            sourcePokemon.filter { pokemon ->
                pokemon.name.lowercase().contains(lower) ||
                        (asNumber != null && pokemon.id == asNumber)
            }
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pokemons souhaites (${uiState.selectedRequestedPokemons.size}/5)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = fr.pokenity.pokenity.ui.i18n.uiText("Fermer"))
                    }
                }

                // Selected chips
                if (uiState.selectedRequestedPokemons.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.selectedRequestedPokemons.size) { index ->
                            val pokemon = uiState.selectedRequestedPokemons[index]
                            InputChip(
                                selected = true,
                                onClick = { onRemoveRequestedPokemonAt(index) },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        AsyncImage(
                                            model = pokemon.imageUrl,
                                            contentDescription = pokemon.resourceName,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = pokemon.resourceName,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = fr.pokenity.pokenity.ui.i18n.uiText("Retirer"),
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = InputChipDefaults.inputChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                HorizontalDivider()

                // Search + Filters section (scrollable header before the list)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    state = rememberLazyListState()
                ) {
                    // Search field
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text(fr.pokenity.pokenity.ui.i18n.uiText("Rechercher...")) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = null
                                )
                            },
                            trailingIcon = {
                                if (uiState.isPokemonListLoading || pokedexUiState.isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                }
                            }
                        )
                    }

                    // Filters
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            FiltersSection(
                                uiState = pokedexUiState,
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

                    // Active filter bar
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            ActiveFilterBar(
                                uiState = pokedexUiState,
                                onClearTypeFilter = onClearTypeFilter,
                                onClearGenerationFilter = onClearGenerationFilter,
                                onClearAbilityFilter = onClearAbilityFilter,
                                onClearHabitatFilter = onClearHabitatFilter,
                                onClearRegionFilter = onClearRegionFilter,
                                onClearShapeFilter = onClearShapeFilter
                            )
                        }
                    }

                    item { HorizontalDivider() }

                    // Pokemon list
                    if (pokedexUiState.isLoading && displayedList.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    } else if (displayedList.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Aucun Pokemon trouve.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        val canAdd = uiState.selectedRequestedPokemons.size < 5
                        items(displayedList, key = { it.id }) { pokemon ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = canAdd) { onAddRequestedPokemon(pokemon) }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = pokemon.imageUrl,
                                    contentDescription = pokemon.name,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = pokemon.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "#${pokemon.id}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }

                HorizontalDivider()

                // Bottom action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onClose,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(fr.pokenity.pokenity.ui.i18n.uiText("Annuler"))
                    }
                    PrimaryButton(
                        onClick = onClose,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("${fr.pokenity.pokenity.ui.i18n.uiText("Valider")} (${uiState.selectedRequestedPokemons.size})")
                    }
                }
            }
        }
    }
}
