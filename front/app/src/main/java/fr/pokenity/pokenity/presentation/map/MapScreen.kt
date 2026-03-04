package fr.pokenity.pokenity.presentation.map

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import fr.pokenity.pokenity.domain.model.PokemonFilterOption
import fr.pokenity.pokenity.domain.model.PokemonSummary

@Composable
fun MapScreen(
    uiState: MapUiState,
    onRetry: () -> Unit,
    onPokemonClick: (Int) -> Unit,
    onRegionSelected: (PokemonFilterOption) -> Unit,
    onLocationSelected: (PokemonFilterOption) -> Unit,
    onAreaSelected: (PokemonFilterOption) -> Unit,
    onBackToRegions: () -> Unit,
    onBackToLocations: () -> Unit,
    onBackToAreas: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        MapHeader(
                            title = "World Map Explorer",
                            subtitle = "Choisis une region, puis une location, puis une zone"
                        )
                    }

                    item {
                        StepRow(
                            region = uiState.selectedRegion?.label,
                            location = uiState.selectedLocation?.label,
                            area = uiState.selectedArea?.label
                        )
                    }

                    if (uiState.selectedRegion != null) {
                        item {
                            OutlinedButton(onClick = onBackToRegions) { Text("Changer de region") }
                        }
                    }
                    if (uiState.selectedLocation != null) {
                        item {
                            OutlinedButton(onClick = onBackToLocations) { Text("Changer de location") }
                        }
                    }
                    if (uiState.selectedArea != null) {
                        item {
                            OutlinedButton(onClick = onBackToAreas) { Text("Changer de zone") }
                        }
                    }

                    when {
                        uiState.selectedRegion == null -> {
                            item { SectionTitle("Regions") }
                            items(uiState.regions, key = { it.apiName }) { region ->
                                ChoiceCard(title = region.label, onClick = { onRegionSelected(region) })
                            }
                        }

                        uiState.selectedLocation == null -> {
                            item { SectionTitle("Locations de ${uiState.selectedRegion.label}") }
                            items(uiState.locations, key = { it.apiName }) { location ->
                                ChoiceCard(title = location.label, onClick = { onLocationSelected(location) })
                            }
                        }

                        uiState.selectedArea == null -> {
                            item { SectionTitle("Zones de ${uiState.selectedLocation.label}") }
                            items(uiState.areas, key = { it.apiName }) { area ->
                                ChoiceCard(title = area.label, onClick = { onAreaSelected(area) })
                            }
                        }

                        else -> {
                            item { SectionTitle("Pokemons trouves") }
                            if (uiState.pokemons.isEmpty()) {
                                item {
                                    Text(
                                        text = "Aucun Pokemon rencontre dans cette zone.",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            } else {
                                items(uiState.pokemons, key = { it.id }) { pokemon ->
                                    PokemonRow(pokemon = pokemon, onClick = { onPokemonClick(pokemon.id) })
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
private fun MapHeader(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF3B4CCA), Color(0xFF2D6A4F), Color(0xFFFFCB05))
                )
            )
            .padding(20.dp)
    ) {
        Column {
            Text(text = title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodyLarge, color = Color.White)
        }
    }
}

@Composable
private fun StepRow(region: String?, location: String?, area: String?) {
    Surface(shape = RoundedCornerShape(16.dp), tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = "1. Region: ${region ?: "-"}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "2. Location: ${location ?: "-"}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "3. Zone: ${area ?: "-"}", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}

@Composable
private fun ChoiceCard(title: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        )
    }
}

@Composable
private fun PokemonRow(pokemon: PokemonSummary, onClick: () -> Unit) {
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
                model = pokemon.imageUrl,
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
        Button(onClick = onRetry) { Text("Reessayer") }
    }
}
