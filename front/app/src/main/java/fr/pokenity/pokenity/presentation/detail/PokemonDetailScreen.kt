package fr.pokenity.pokenity.presentation.detail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import fr.pokenity.pokenity.core.PokemonImageSettings
import fr.pokenity.pokenity.core.PokemonImageType
import fr.pokenity.pokenity.core.pokemonImageUrl
import fr.pokenity.pokenity.domain.model.EvolutionStage
import fr.pokenity.pokenity.domain.model.PokemonDetail
import fr.pokenity.pokenity.domain.model.PokemonMove
import fr.pokenity.pokenity.domain.model.PokemonStat
import fr.pokenity.pokenity.domain.model.PokemonType

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

private val StatColors = mapOf(
    "Hp" to Color(0xFFFF5252),
    "Attack" to Color(0xFFFF7043),
    "Defense" to Color(0xFFFFCA28),
    "Special attack" to Color(0xFF42A5F5),
    "Special defense" to Color(0xFF66BB6A),
    "Speed" to Color(0xFFAB47BC)
)

private val StatLabels = mapOf(
    "Hp" to "HP",
    "Attack" to "ATK",
    "Defense" to "DEF",
    "Special attack" to "SP.ATK",
    "Special defense" to "SP.DEF",
    "Speed" to "SPD"
)

@Composable
fun PokemonDetailScreen(
    uiState: PokemonDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onPreviousPokemon: (() -> Unit)? = null,
    onNextPokemon: (() -> Unit)? = null,
    onPokemonClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val imageType by PokemonImageSettings.imageType.collectAsState()
    val shinyEnabled by PokemonImageSettings.isShiny.collectAsState()

    Surface(modifier = modifier.fillMaxSize()) {
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
                    Text(text = uiState.errorMessage, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onRetry) {
                        Text("Reessayer")
                    }
                }
            }

            uiState.pokemon != null -> {
                val pokemon = uiState.pokemon
                val primaryTypeColor = TypeColors[pokemon.types.firstOrNull()?.name] ?: MaterialTheme.colorScheme.primary

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(pokemon.id, onPreviousPokemon, onNextPokemon) {
                            var dragOffset = 0f
                            detectHorizontalDragGestures(
                                onHorizontalDrag = { _, dragAmount ->
                                    dragOffset += dragAmount
                                },
                                onDragEnd = {
                                    when {
                                        dragOffset > 120f && onPreviousPokemon != null -> onPreviousPokemon()
                                        dragOffset < -120f && onNextPokemon != null -> onNextPokemon()
                                    }
                                    dragOffset = 0f
                                }
                            )
                        },
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Swipe gauche/droite pour naviguer",
                                style = MaterialTheme.typography.labelLarge
                            )
                            OutlinedButton(
                                onClick = { PokemonImageSettings.toggleShiny() },
                                enabled = imageType.supportsShiny
                            ) {
                                Text(if (shinyEnabled) "Shiny ON" else "Shiny OFF")
                            }
                        }
                    }

                    // Header with image and gradient background
                    item {
                        DetailHeader(
                            pokemon = pokemon,
                            primaryTypeColor = primaryTypeColor,
                            imageType = imageType,
                            shinyEnabled = shinyEnabled,
                            onBack = onBack
                        )
                    }

                    // Info card (types, height, weight)
                    item {
                        InfoSection(pokemon = pokemon)
                    }

                    // Stats section
                    item {
                        StatsSection(stats = pokemon.stats)
                    }

                    // Evolution chain section
                    if (pokemon.evolutionChain.size > 1) {
                        item {
                            EvolutionSection(
                                evolutionChain = pokemon.evolutionChain,
                                primaryTypeColor = primaryTypeColor,
                                imageType = imageType,
                                shinyEnabled = shinyEnabled,
                                onPokemonClick = onPokemonClick
                            )
                        }
                    }

                    // Moves section
                    if (pokemon.moves.isNotEmpty()) {
                        item {
                            MovesSection(moves = pokemon.moves)
                        }
                    }

                    // Abilities section
                    item {
                        AbilitiesSection(abilities = pokemon.abilities)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailHeader(
    pokemon: PokemonDetail,
    primaryTypeColor: Color,
    imageType: PokemonImageType,
    shinyEnabled: Boolean,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        primaryTypeColor,
                        primaryTypeColor.copy(alpha = 0.6f)
                    )
                )
            )
    ) {
        // Back button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(top = 40.dp, start = 8.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Retour",
                tint = Color.White
            )
        }

        // Pokemon ID top right
        Text(
            text = "#${pokemon.id.toString().padStart(3, '0')}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier
                .padding(top = 48.dp, end = 20.dp)
                .align(Alignment.TopEnd)
        )

        // Pokemon image centered
        AsyncImage(
            model = pokemonImageUrl(pokemon.id, imageType, shinyEnabled),
            contentDescription = pokemon.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.Center)
        )

        // Pokemon name at bottom
        Text(
            text = pokemon.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun InfoSection(pokemon: PokemonDetail) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // Types row — images instead of text chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            pokemon.types.forEachIndexed { index, type ->
                TypeImageChip(type = type)
                if (index < pokemon.types.lastIndex) {
                    Spacer(modifier = Modifier.width(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Height / Weight row
        Surface(
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InfoItem(
                    label = "Taille",
                    value = "${pokemon.height / 10.0} m"
                )
                // Vertical divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                InfoItem(
                    label = "Poids",
                    value = "${pokemon.weight / 10.0} kg"
                )
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TypeImageChip(type: PokemonType) {
    val color = TypeColors[type.name] ?: MaterialTheme.colorScheme.primary

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color,
        modifier = Modifier
    ) {
        AsyncImage(
            model = type.imageUrl,
            contentDescription = type.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .height(34.dp)
                .padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun StatsSection(stats: List<PokemonStat>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Statistiques",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Surface(
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                stats.forEach { stat ->
                    StatBar(stat = stat)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Total
                val total = stats.sumOf { it.baseStat }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(64.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = total.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.width(40.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
private fun StatBar(stat: PokemonStat) {
    val label = StatLabels[stat.name] ?: stat.name.uppercase()
    val color = StatColors[stat.name] ?: MaterialTheme.colorScheme.primary
    val maxStat = 255f
    val fraction = (stat.baseStat / maxStat).coerceIn(0f, 1f)

    var animationPlayed by remember { mutableStateOf(false) }
    val animatedFraction by animateFloatAsState(
        targetValue = if (animationPlayed) fraction else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "stat_anim"
    )

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Stat label
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(64.dp),
            color = color
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Stat value
        Text(
            text = stat.baseStat.toString(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.End
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Progress bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(color.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(5.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(color, color.copy(alpha = 0.7f))
                        )
                    )
            )
        }
    }
}

@Composable
private fun EvolutionSection(
    evolutionChain: List<EvolutionStage>,
    primaryTypeColor: Color,
    imageType: PokemonImageType,
    shinyEnabled: Boolean,
    onPokemonClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Evolutions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Surface(
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                evolutionChain.forEachIndexed { index, stage ->
                    EvolutionStageItem(
                        stage = stage,
                        primaryTypeColor = primaryTypeColor,
                        imageType = imageType,
                        shinyEnabled = shinyEnabled,
                        onClick = { if (!stage.isCurrent) onPokemonClick(stage.id) },
                        modifier = Modifier.weight(1f)
                    )
                    if (index < evolutionChain.lastIndex) {
                        Text(
                            text = "\u2192", // arrow
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EvolutionStageItem(
    stage: EvolutionStage,
    primaryTypeColor: Color,
    imageType: PokemonImageType,
    shinyEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Image with highlight ring for current stage
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .then(
                    if (stage.isCurrent) {
                        Modifier
                            .clip(CircleShape)
                            .background(primaryTypeColor.copy(alpha = 0.15f))
                    } else {
                        Modifier
                    }
                )
        ) {
            AsyncImage(
                model = pokemonImageUrl(stage.id, imageType, shinyEnabled),
                contentDescription = stage.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = stage.name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (stage.isCurrent) FontWeight.ExtraBold else FontWeight.Normal,
            color = if (stage.isCurrent) primaryTypeColor else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        // Small indicator dot for current
        if (stage.isCurrent) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(primaryTypeColor)
            )
        }
    }
}

@Composable
private fun MovesSection(moves: List<PokemonMove>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Attaques",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            moves.forEach { move ->
                MoveCard(move = move)
            }
        }
    }
}

@Composable
private fun MoveCard(move: PokemonMove) {
    val typeColor = TypeColors[move.type.name] ?: MaterialTheme.colorScheme.primary

    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Row 1: Move name + type image
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = move.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = typeColor,
                    modifier = Modifier
                ) {
                    AsyncImage(
                        model = move.type.imageUrl,
                        contentDescription = move.type.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .height(24.dp)
                            .padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 2: Power / Accuracy / PP
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                move.power?.let {
                    MoveStatLabel(label = "PWR", value = it.toString(), color = typeColor)
                }
                move.accuracy?.let {
                    MoveStatLabel(label = "ACC", value = "$it%", color = typeColor)
                }
                move.pp?.let {
                    MoveStatLabel(label = "PP", value = it.toString(), color = typeColor)
                }
            }

            // Row 3: Description
            if (move.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = move.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MoveStatLabel(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$label ",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun AbilitiesSection(abilities: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Talents",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Surface(
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                abilities.forEach { ability ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = ability,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}
