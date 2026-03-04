package fr.pokenity.pokenity.presentation.pokedex

import androidx.compose.foundation.background
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import fr.pokenity.pokenity.domain.model.PokemonSummary

@Composable
fun PokedexScreen(
    uiState: PokedexUiState,
    onRetry: () -> Unit,
    onSectionSelected: (PokedexSection) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { mutableStateOf("") }

    val filteredPokemon by remember(uiState.pokemon, query) {
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
                ErrorState(
                    message = uiState.errorMessage,
                    onRetry = onRetry
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        HeaderCard(total = filteredPokemon.size)
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

                            if (filteredPokemon.isEmpty()) {
                                item {
                                    Text(
                                        text = "Aucun Pokemon ne correspond a ta recherche.",
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                }
                            }

                            items(items = filteredPokemon, key = { it.id }) { pokemon ->
                                PokemonRow(pokemon = pokemon)
                            }
                        }

                        PokedexSection.TYPE -> {
                            items(uiState.types, key = { it }) { type ->
                                NameCard(title = type)
                            }
                        }

                        PokedexSection.GENERATION -> {
                            items(uiState.generations, key = { it }) { generation ->
                                NameCard(title = generation)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionSelector(
    selectedSection: PokedexSection,
    onSectionSelected: (PokedexSection) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionButton(
            label = "All",
            selected = selectedSection == PokedexSection.ALL,
            onClick = { onSectionSelected(PokedexSection.ALL) },
            modifier = Modifier.weight(1f)
        )
        SectionButton(
            label = "Type",
            selected = selectedSection == PokedexSection.TYPE,
            onClick = { onSectionSelected(PokedexSection.TYPE) },
            modifier = Modifier.weight(1f)
        )
        SectionButton(
            label = "Generation",
            selected = selectedSection == PokedexSection.GENERATION,
            onClick = { onSectionSelected(PokedexSection.GENERATION) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SectionButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selected) {
        Button(onClick = onClick, modifier = modifier) {
            Text(label)
        }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier) {
            Text(label)
        }
    }
}

@Composable
private fun HeaderCard(total: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFE3350D), Color(0xFFFFCB05), Color(0xFF3B4CCA))
                )
            )
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "Pokenity Pokedex",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$total Pokemon charges",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
    }
}

@Composable
private fun PokemonRow(pokemon: PokemonSummary) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
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
private fun NameCard(title: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        )
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
