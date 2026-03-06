package fr.pokenity.pokenity.presentation.box

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import fr.pokenity.data.core.PokemonImageType
import fr.pokenity.pokenity.ui.components.PokemonSpriteImage
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToLong

@Composable
fun BoxDetailScreen(
    uiState: BoxDetailUiState,
    onRetry: () -> Unit,
    onOpenBox: () -> Unit,
    onSpinAnimationCompleted: () -> Unit,
    onDismissRewardDialog: () -> Unit,
    bottomInset: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        uiState.errorMessage != null -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(uiState.errorMessage)
                OutlinedButton(onClick = onRetry, modifier = Modifier.padding(top = 10.dp)) {
                    Text("Reessayer")
                }
            }
        }

        uiState.box == null -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Box introuvable.")
            }
        }

        else -> {
            val context = LocalContext.current
            val box = uiState.box
            val rows = remember(uiState.orderedEntries) { uiState.orderedEntries.chunked(3) }
            val pageBackgroundResId = remember {
                context.resources.getIdentifier("draw_background", "drawable", context.packageName)
            }

            Box(
                modifier = modifier.fillMaxSize()
            ) {
                if (pageBackgroundResId != 0) {
                    Image(
                        painter = painterResource(id = pageBackgroundResId),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp + bottomInset
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        val canOpenBox = !uiState.isOpening && !uiState.isSpinning && uiState.orderedEntries.isNotEmpty()
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    enabled = canOpenBox,
                                    onClick = onOpenBox
                                ),
                            shape = RoundedCornerShape(18.dp),
                            tonalElevation = 2.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AsyncImage(
                                    model = box.pokeballImage,
                                    contentDescription = box.name,
                                    modifier = Modifier.size(96.dp)
                                )
                                Text(
                                    text = box.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${box.entries.size} pokemons dans la box",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    if (uiState.openingErrorMessage != null) {
                        item {
                            Text(
                                text = uiState.openingErrorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    if (rows.isEmpty()) {
                        item {
                            Text(
                                text = "Aucun pokemon dans cette box.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        items(rows) { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowItems.forEach { entry ->
                                    BoxPokemonTile(
                                        pokemon = entry,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                repeat(3 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            }

            if (uiState.isOpening || uiState.isSpinning) {
                FullScreenDrawOverlay(
                    isSpinning = uiState.isSpinning,
                    items = uiState.rouletteItems,
                    spinRequestId = uiState.spinRequestId,
                    onSpinAnimationCompleted = onSpinAnimationCompleted
                )
            }

            val reward = uiState.pendingReward
            if (uiState.showRewardDialog && reward != null) {
                AlertDialog(
                    onDismissRequest = onDismissRewardDialog,
                    title = {
                        Text(
                            text = if (uiState.isNewPokemonReward) {
                                "NOUVEAU POKEMON !"
                            } else {
                                "Pokemon gagne"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PokemonSpriteImage(
                                pokemonId = reward.resourceId,
                                contentDescription = reward.resourceName.prettyPokemonName(),
                                imageType = PokemonImageType.SHOWDOWN,
                                shiny = reward.isShiny,
                                modifier = Modifier.size(124.dp)
                            )
                            if (uiState.isNewPokemonReward) {
                                NewPokemonAnimatedBadge()
                            }
                            Text(
                                text = reward.resourceName.prettyPokemonName(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (reward.isShiny) {
                                Text(
                                    text = "Version Shiny ✨",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD54F),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Text(
                                text = if (uiState.isNewPokemonReward) {
                                    "Premiere obtention, bien joue !"
                                } else {
                                    "Drop rate: ${"%.2f".format(reward.dropRate)}%"
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = onDismissRewardDialog) {
                            Text("Fermer")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun NewPokemonAnimatedBadge(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "new-pokemon-badge")
    val pulseScale by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 720, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse-scale"
    )
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.78f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 720, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse-alpha"
    )

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = pulseScale
                scaleY = pulseScale
                alpha = pulseAlpha
            },
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color(0xFFFFD54F),
                            Color(0xFFFFB300)
                        )
                    )
                )
                .padding(horizontal = 10.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Nouveau !",
                color = Color.Black,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun FullScreenDrawOverlay(
    isSpinning: Boolean,
    items: List<BoxPokemonUi>,
    spinRequestId: Long,
    onSpinAnimationCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xED020A1A))
    ) {
        if (!isSpinning) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CircularProgressIndicator(color = Color(0xFFFFDF2B))
                Text(
                    text = "Preparation du draw...",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFF0F6FF),
                    textAlign = TextAlign.Center
                )
            }
            return@Box
        }

        FlashDrawVerticalTrack(
            items = items,
            spinRequestId = spinRequestId,
            onSpinAnimationCompleted = onSpinAnimationCompleted,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        )
    }
}

@Composable
private fun FlashDrawVerticalTrack(
    items: List<BoxPokemonUi>,
    spinRequestId: Long,
    onSpinAnimationCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentIndex by remember(items) {
        mutableIntStateOf(if (items.isNotEmpty()) 0 else -1)
    }
    var isFlashing by remember(spinRequestId) { mutableStateOf(false) }
    var lastPlayedSpinId by remember { mutableStateOf(-1L) }
    val context = LocalContext.current
    val drawBackgroundResId = remember {
        context.resources.getIdentifier("draw_background", "drawable", context.packageName)
    }

    LaunchedEffect(items.size) {
        if (!isFlashing) {
            currentIndex = if (items.isNotEmpty()) 0 else -1
        }
    }

    LaunchedEffect(spinRequestId, items.size) {
        if (spinRequestId <= 0L || items.isEmpty() || spinRequestId == lastPlayedSpinId) {
            return@LaunchedEffect
        }
        lastPlayedSpinId = spinRequestId

        isFlashing = true
        val steps = items.size
        val baseDelays = FloatArray(steps)
        val fastPhaseEnd = 0.50f
        var totalBaseDelay = 0f

        for (index in 0 until steps) {
            val progress = index.toFloat() / (steps - 1).coerceAtLeast(1)
            val baseDelay = if (progress < fastPhaseEnd) {
                val phase = progress / fastPhaseEnd
                10f + (10f * phase)
            } else {
                val phase = (progress - fastPhaseEnd) / (1f - fastPhaseEnd)
                22f + (140f * phase.pow(1.18f))
            }
            baseDelays[index] = baseDelay
            totalBaseDelay += baseDelay
        }

        val targetDurationMs = 6100f
        val speedScale = if (totalBaseDelay > 0f) targetDurationMs / totalBaseDelay else 1f

        for (index in items.indices) {
            currentIndex = index
            val delayMs = (baseDelays[index] * speedScale).roundToLong().coerceAtLeast(12L)
            delay(delayMs)
        }
        isFlashing = false
        delay(1000L)
        onSpinAnimationCompleted()
    }

    val displayedPokemon = items.getOrNull(currentIndex)
    val slotOffsets = remember { listOf(-3, -2, -1, 0, 1, 2, 3) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .border(width = 1.dp, color = Color(0xFF204A88), shape = RoundedCornerShape(28.dp))
    ) {
        if (drawBackgroundResId != 0) {
            Image(
                painter = painterResource(id = drawBackgroundResId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(28.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF031533))
            )
        }

        if (displayedPokemon == null) {
            Text(
                text = "Aucun pokemon pour le draw",
                color = Color(0xFFF0F6FF),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            return@Box
        }

        BoxWithConstraints(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 20.dp)
        ) {
            val itemSpacing = 6.dp
            val normalCardSize = (maxWidth * 0.58f).coerceIn(114.dp, 142.dp)
            val centerCardSize = (normalCardSize * 1.34f).coerceIn(148.dp, 188.dp)

            val centerToFirstOffset = ((centerCardSize + normalCardSize) / 2f) + itemSpacing
            val regularStep = normalCardSize + itemSpacing

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize()
            ) {
                slotOffsets.forEach { offset ->
                    val slotPokemon = items.getOrNull(cyclicIndex(currentIndex + offset, items.size)) ?: displayedPokemon
                    val isCenterSlot = offset == 0
                    val absOffset = kotlin.math.abs(offset)
                    val direction = if (offset < 0) -1 else 1
                    val slotOffsetY = when {
                        offset == 0 -> 0.dp
                        absOffset == 1 -> centerToFirstOffset * direction
                        else -> (centerToFirstOffset + (regularStep * (absOffset - 1))) * direction
                    }

                    RouletteItemTile(
                        pokemon = slotPokemon,
                        isWinningItem = !isFlashing && spinRequestId > 0L && isCenterSlot && currentIndex == items.lastIndex,
                        isCenter = isCenterSlot,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = slotOffsetY)
                            .size(if (isCenterSlot) centerCardSize else normalCardSize)
                    )
                }
            }
        }
    }
}

@Composable
private fun RouletteItemTile(
    pokemon: BoxPokemonUi,
    isWinningItem: Boolean,
    isCenter: Boolean,
    modifier: Modifier = Modifier
) {
    val rarity = rarityStyle(pokemon.dropRate)
    val context = LocalContext.current
    val rarityBackgroundResId = remember(rarity.backgroundDrawableName) {
        context.resources.getIdentifier(rarity.backgroundDrawableName, "drawable", context.packageName)
    }

    Box(modifier = modifier) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            if (rarityBackgroundResId != 0) {
                Image(
                    painter = painterResource(id = rarityBackgroundResId),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            val imageSize = if (isCenter) maxWidth * 0.82f else maxWidth * 0.78f
            PokemonSpriteImage(
                pokemonId = pokemon.resourceId,
                contentDescription = pokemon.resourceName.prettyPokemonName(),
                imageType = PokemonImageType.SHOWDOWN,
                shiny = pokemon.isShiny,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(imageSize),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun BoxPokemonTile(
    pokemon: BoxPokemonUi,
    modifier: Modifier = Modifier
) {
    val rarity = rarityStyle(pokemon.dropRate)
    val context = LocalContext.current
    val rarityBackgroundResId = remember(rarity.backgroundDrawableName) {
        context.resources.getIdentifier(rarity.backgroundDrawableName, "drawable", context.packageName)
    }
    val descBackgroundResId = remember(rarity.descBackgroundDrawableName) {
        context.resources.getIdentifier(rarity.descBackgroundDrawableName, "drawable", context.packageName)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                if (rarityBackgroundResId != 0) {
                    Image(
                        painter = painterResource(id = rarityBackgroundResId),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                PokemonSpriteImage(
                    pokemonId = pokemon.resourceId,
                    contentDescription = pokemon.resourceName.prettyPokemonName(),
                    imageType = PokemonImageType.SHOWDOWN,
                    shiny = pokemon.isShiny,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(maxWidth * 0.84f),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp),
            color = Color.Transparent
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (descBackgroundResId != 0) {
                    Image(
                        painter = painterResource(id = descBackgroundResId),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF2F2F2))
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = pokemon.resourceName.prettyPokemonName(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF111111),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${"%.2f".format(pokemon.dropRate)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF111111),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

private fun rarityStyle(dropRate: Double): RarityStyle {
    return when {
        dropRate <= 1.5 -> RarityStyle(
            backgroundDrawableName = "card_bg_legendary",
            descBackgroundDrawableName = "desc_bg_legendary"
        )
        dropRate <= 4.0 -> RarityStyle(
            backgroundDrawableName = "card_bg_epic",
            descBackgroundDrawableName = "desc_bg_epic"
        )
        dropRate <= 8.0 -> RarityStyle(
            backgroundDrawableName = "card_bg_rare",
            descBackgroundDrawableName = "desc_bg_rare"
        )
        dropRate <= 16.0 -> RarityStyle(
            backgroundDrawableName = "card_bg_uncommon",
            descBackgroundDrawableName = "desc_bg_uncommon"
        )
        else -> RarityStyle(
            backgroundDrawableName = "card_bg_common",
            descBackgroundDrawableName = "desc_bg_common"
        )
    }
}

private fun cyclicIndex(index: Int, size: Int): Int {
    if (size <= 0) return 0
    val normalized = index % size
    return if (normalized < 0) normalized + size else normalized
}

private data class RarityStyle(
    val backgroundDrawableName: String,
    val descBackgroundDrawableName: String
)

private fun String.prettyPokemonName(): String {
    val normalized = trim().replace('-', ' ')
    if (normalized.isBlank()) return "Pokemon"
    return normalized.replaceFirstChar { current ->
        if (current.isLowerCase()) {
            current.titlecase(Locale.getDefault())
        } else {
            current.toString()
        }
    }
}
