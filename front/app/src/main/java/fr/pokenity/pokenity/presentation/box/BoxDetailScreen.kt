package fr.pokenity.pokenity.presentation.box

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import coil.imageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

@Composable
fun BoxDetailScreen(
    uiState: BoxDetailUiState,
    onRetry: () -> Unit,
    onOpenBox: () -> Unit,
    onSpinAnimationCompleted: () -> Unit,
    onDismissRewardDialog: () -> Unit,
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
            val box = uiState.box
            val rows = remember(uiState.orderedEntries) { uiState.orderedEntries.chunked(2) }

            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
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

                item {
                    Text(
                        text = "Draw flash",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                item {
                    FlashDrawTrack(
                        items = uiState.rouletteItems,
                        spinRequestId = uiState.spinRequestId,
                        onSpinAnimationCompleted = onSpinAnimationCompleted,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenBox,
                            enabled = !uiState.isOpening && !uiState.isSpinning && uiState.orderedEntries.isNotEmpty()
                        ) {
                            Text(
                                when {
                                    uiState.isOpening -> "Ouverture..."
                                    uiState.isSpinning -> "Flash en cours..."
                                    else -> "Lancer l'ouverture"
                                }
                            )
                        }

                        if (uiState.openingErrorMessage != null) {
                            Text(
                                text = uiState.openingErrorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Pokemons (du plus rare au moins rare)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
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
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            val reward = uiState.pendingReward
            if (uiState.showRewardDialog && reward != null) {
                AlertDialog(
                    onDismissRequest = onDismissRewardDialog,
                    title = {
                        Text(
                            text = "Pokemon gagne",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AsyncImage(
                                model = reward.imageUrl,
                                contentDescription = reward.resourceName,
                                modifier = Modifier.size(124.dp)
                            )
                            Text(
                                text = reward.resourceName.prettyPokemonName(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "Drop rate: ${"%.2f".format(reward.dropRate)}%",
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
private fun FlashDrawTrack(
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
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(items.size) {
        if (!isFlashing) {
            currentIndex = if (items.isNotEmpty()) 0 else -1
        }
    }

    LaunchedEffect(items) {
        val urls = items
            .map { it.imageUrl }
            .filter { it.isNotBlank() }
            .distinct()
        urls.forEach { url ->
            context.imageLoader.enqueue(
                ImageRequest.Builder(context)
                    .data(url)
                    .build()
            )
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
    val slotOffsets = remember { listOf(-2, -1, 0, 1, 2) }

    Box(
        modifier = modifier
            .height(200.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFF031533))
            .border(width = 1.dp, color = Color(0xFF204A88), shape = RoundedCornerShape(28.dp))
    ) {
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

        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            slotOffsets.forEach { offset ->
                val slotPokemon = items.getOrNull(cyclicIndex(currentIndex + offset, items.size)) ?: displayedPokemon
                val slotSize = when (abs(offset)) {
                    2 -> 52.dp
                    1 -> 64.dp
                    else -> 112.dp
                }
                RouletteItemTile(
                    pokemon = slotPokemon,
                    isWinningItem = !isFlashing && spinRequestId > 0L && offset == 0 && currentIndex == items.lastIndex,
                    isCenter = offset == 0,
                    modifier = Modifier.size(slotSize)
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 14.dp)
                .height(24.dp)
                .width(110.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(if (isFlashing) Color(0xFF2A8BFF) else Color(0xFFFFDF2B))
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 78.dp)
                .height(3.dp)
                .width(100.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(if (isFlashing) Color(0x402A8BFF) else Color(0x80FFDF2B))
        )
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
    val borderColor = if (isWinningItem) Color(0xFFFFD54F) else rarity.border

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = rarity.background,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = pokemon.imageUrl,
                contentDescription = pokemon.resourceName,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(if (isCenter) 70.dp else 44.dp)
            )
            Text(
                text = pokemon.resourceName.prettyPokemonName(),
                style = if (isCenter) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall,
                color = Color(0xFFF0F6FF),
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp, vertical = 8.dp)
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
    Surface(
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        color = rarity.background,
        border = androidx.compose.foundation.BorderStroke(1.dp, rarity.border),
        tonalElevation = 2.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = pokemon.imageUrl,
                contentDescription = pokemon.resourceName,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(84.dp)
            )
            Text(
                text = pokemon.resourceName.prettyPokemonName(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 22.dp)
            )
            Text(
                text = "${"%.2f".format(pokemon.dropRate)}%",
                style = MaterialTheme.typography.labelSmall,
                color = rarity.dropRateText,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            )
        }
    }
}

private fun rarityStyle(dropRate: Double): RarityStyle {
    return when {
        dropRate <= 1.5 -> RarityStyle(
            border = Color(0xFFFFA726),
            background = Color(0xFF2A1A08),
            dropRateText = Color(0xFFFFD180)
        )
        dropRate <= 4.0 -> RarityStyle(
            border = Color(0xFFBA68C8),
            background = Color(0xFF24162B),
            dropRateText = Color(0xFFE1BEE7)
        )
        dropRate <= 8.0 -> RarityStyle(
            border = Color(0xFF64B5F6),
            background = Color(0xFF0F2336),
            dropRateText = Color(0xFFBBDEFB)
        )
        dropRate <= 16.0 -> RarityStyle(
            border = Color(0xFF81C784),
            background = Color(0xFF132717),
            dropRateText = Color(0xFFC8E6C9)
        )
        else -> RarityStyle(
            border = Color(0xFFB0BEC5),
            background = Color(0xFF1F252A),
            dropRateText = Color(0xFFECEFF1)
        )
    }
}

private fun cyclicIndex(index: Int, size: Int): Int {
    if (size <= 0) return 0
    val normalized = index % size
    return if (normalized < 0) normalized + size else normalized
}

private data class RarityStyle(
    val border: Color,
    val background: Color,
    val dropRateText: Color
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
