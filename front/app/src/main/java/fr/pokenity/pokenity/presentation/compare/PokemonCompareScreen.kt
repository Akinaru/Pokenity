package fr.pokenity.pokenity.presentation.compare

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import fr.pokenity.pokenity.ui.components.PrimaryButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.pokenity.data.core.PokemonImageSettings
import fr.pokenity.pokenity.ui.components.PokemonSpriteImage

private val StatLabels = mapOf(
    "Hp" to "HP",
    "Attack" to "ATK",
    "Defense" to "DEF",
    "Special attack" to "SP.ATK",
    "Special defense" to "SP.DEF",
    "Speed" to "SPD"
)

private val TypeColors = mapOf(
    "Normal" to Color(0xFFA8A77A),
    "Fire" to Color(0xFFEE8130),
    "Water" to Color(0xFF6390F0),
    "Electric" to Color(0xFFF7D02C),
    "Grass" to Color(0xFF7AC74C),
    "Ice" to Color(0xFF96D9D6),
    "Fighting" to Color(0xFFC22E28),
    "Poison" to Color(0xFFA33EA1),
    "Ground" to Color(0xFFE2BF65),
    "Flying" to Color(0xFFA98FF3),
    "Psychic" to Color(0xFFF95587),
    "Bug" to Color(0xFFA6B91A),
    "Rock" to Color(0xFFB6A136),
    "Ghost" to Color(0xFF735797),
    "Dragon" to Color(0xFF6F35FC),
    "Dark" to Color(0xFF705746),
    "Steel" to Color(0xFFB7B7CE),
    "Fairy" to Color(0xFFD685AD)
)

@Composable
fun PokemonCompareScreen(
    uiState: PokemonCompareUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onOpenSelector: () -> Unit,
    onOpenPokemonDetail: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val imageType by PokemonImageSettings.imageType.collectAsState()
    val shiny by PokemonImageSettings.isShiny.collectAsState()
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Surface(modifier = modifier.fillMaxSize(), color = Color.Transparent) {
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(uiState.errorMessage, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(12.dp))
                    PrimaryButton(onClick = onRetry) { Text(fr.pokenity.pokenity.ui.i18n.uiText("Reessayer")) }
                }
            }

            uiState.basePokemon != null -> {
                val base = uiState.basePokemon
                val compared = uiState.comparedPokemon
                val leftColor = TypeColors[base.types.firstOrNull()?.name] ?: MaterialTheme.colorScheme.primary
                val rightColor = TypeColors[compared?.types?.firstOrNull()?.name] ?: MaterialTheme.colorScheme.secondary

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = statusBarTop + 8.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = fr.pokenity.pokenity.ui.i18n.uiText("Retour"))
                            }
                            Text(
                                fr.pokenity.pokenity.ui.i18n.uiText("Comparateur"),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedButton(onClick = onOpenSelector) {
                                Text(if (compared == null) "Choisir" else "Changer")
                            }
                        }
                    }

                    item {
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            tonalElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            listOf(leftColor.copy(alpha = 0.22f), rightColor.copy(alpha = 0.22f))
                                        )
                                    )
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        PokemonSpriteImage(
                                            pokemonId = base.id,
                                            contentDescription = base.name,
                                            imageType = imageType,
                                            shiny = shiny,
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.size(96.dp)
                                        )
                                        Text(base.name, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                        OutlinedButton(onClick = { onOpenPokemonDetail(base.id) }) { Text(fr.pokenity.pokenity.ui.i18n.uiText("Voir fiche")) }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        if (compared != null) {
                                            PokemonSpriteImage(
                                                pokemonId = compared.id,
                                                contentDescription = compared.name,
                                                imageType = imageType,
                                                shiny = shiny,
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier.size(96.dp)
                                            )
                                            Text(compared.name, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                            OutlinedButton(onClick = { onOpenPokemonDetail(compared.id) }) { Text(fr.pokenity.pokenity.ui.i18n.uiText("Voir fiche")) }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(96.dp)
                                                    .clip(MaterialTheme.shapes.medium)
                                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("?", style = MaterialTheme.typography.headlineMedium)
                                            }
                                            Text(
                                                fr.pokenity.pokenity.ui.i18n.uiText("Aucun Pokemon"),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            tonalElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                ComparisonRow("Taille", "${base.height / 10.0} m", compared?.let { "${it.height / 10.0} m" } ?: "-")
                                ComparisonRow("Poids", "${base.weight / 10.0} kg", compared?.let { "${it.weight / 10.0} kg" } ?: "-")
                                ComparisonRow("Types", base.types.joinToString(" / ") { it.name }, compared?.types?.joinToString(" / ") { it.name } ?: "-")
                            }
                        }
                    }

                    item {
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            tonalElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    fr.pokenity.pokenity.ui.i18n.uiText("Stats"),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                base.stats.forEach { stat ->
                                    val rightValue = compared?.stats?.firstOrNull { it.name == stat.name }?.baseStat ?: 0
                                    StatCompareBarRow(
                                        label = StatLabels[stat.name] ?: stat.name,
                                        leftValue = stat.baseStat,
                                        rightValue = if (compared != null) rightValue else null,
                                        leftColor = leftColor,
                                        rightColor = rightColor
                                    )
                                }
                            }
                        }
                    }

                    if (compared == null) {
                        item {
                            PrimaryButton(
                                onClick = onOpenSelector,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text(fr.pokenity.pokenity.ui.i18n.uiText("Selectionner un Pokemon a comparer"))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparisonRow(label: String, left: String, right: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.width(72.dp), style = MaterialTheme.typography.bodyMedium)
        Text(
            text = left,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "vs",
            modifier = Modifier.padding(horizontal = 8.dp),
            style = MaterialTheme.typography.labelLarge
        )
        Text(
            text = right,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StatCompareBarRow(
    label: String,
    leftValue: Int,
    rightValue: Int?,
    leftColor: Color,
    rightColor: Color
) {
    val maxStat = 255f
    val leftProgress = (leftValue / maxStat).coerceIn(0f, 1f)
    val rightProgress = ((rightValue ?: 0) / maxStat).coerceIn(0f, 1f)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Text(
                text = if (rightValue != null) "$leftValue vs $rightValue" else "$leftValue",
                style = MaterialTheme.typography.labelLarge
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator(
                progress = { leftProgress },
                color = leftColor,
                trackColor = leftColor.copy(alpha = 0.18f),
                modifier = Modifier.weight(1f)
            )
            LinearProgressIndicator(
                progress = { rightProgress },
                color = rightColor,
                trackColor = rightColor.copy(alpha = 0.18f),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
