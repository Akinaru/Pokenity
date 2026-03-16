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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import fr.pokenity.data.core.PokemonImageType
import fr.pokenity.data.core.PokemonVisualPresets
import fr.pokenity.pokenity.R
import fr.pokenity.pokenity.ui.components.PrimaryButton
import fr.pokenity.pokenity.ui.components.PokemonSpriteImage
import fr.pokenity.pokenity.ui.theme.AppTitleFontFamily
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.roundToInt

private val BOX_PIXEL_PRESET =
    PokemonVisualPresets.firstOrNull { it.key == "gen-v-black-white" }

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
                    Text(fr.pokenity.pokenity.ui.i18n.uiText("Reessayer"))
                }
            }
        }

        uiState.box == null -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(fr.pokenity.pokenity.ui.i18n.uiText("Box introuvable."))
            }
        }

        else -> {
            val context = LocalContext.current
            val box = uiState.box
            val boxButtonBackgroundResId = remember {
                context.resources.getIdentifier("card_box", "drawable", context.packageName)
            }

            Box(
                modifier = modifier.fillMaxSize()
            ) {
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
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = Color.Transparent,
                            tonalElevation = 0.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                            ) {
                                if (boxButtonBackgroundResId != 0) {
                                    Image(
                                        painter = painterResource(id = boxButtonBackgroundResId),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF1A2A44))
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.size(96.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = box.pokeballImage,
                                            contentDescription = box.name,
                                            contentScale = ContentScale.Fit,
                                            filterQuality = FilterQuality.None,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Text(
                                        text = box.name,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "Ouvertures totales: ${box.stats.totalOpenings}  •  Mes ouvertures: ${box.stats.myOpenings}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Black.copy(alpha = 0.88f),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                    item {
                        val canOpenBox = !uiState.isOpening && !uiState.isSpinning && uiState.orderedEntries.isNotEmpty()
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            PrimaryButton(
                                onClick = onOpenBox,
                                enabled = canOpenBox,
                                modifier = Modifier.fillMaxWidth(0.72f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(
                                    text = "Ouvrir la box",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
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

                    if (uiState.orderedEntries.isEmpty()) {
                        item {
                            Text(
                                text = "Aucun pokemon dans cette box.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        item {
                            BoxClosetCollection(entries = uiState.orderedEntries)
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
                RewardResultDialog(
                    reward = reward,
                    isNewPokemonReward = uiState.isNewPokemonReward,
                    onDismiss = onDismissRewardDialog
                )
            }
        }
    }
}

@Composable
private fun RewardResultDialog(
    reward: BoxPokemonUi,
    isNewPokemonReward: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val cardBackgroundResId = remember {
        context.resources.getIdentifier("card_big_box", "drawable", context.packageName)
    }
    val shinyBadgeResId = remember {
        val preferred = context.resources.getIdentifier("badge_shiny", "drawable", context.packageName)
        if (preferred != 0) preferred
        else context.resources.getIdentifier("shiny", "drawable", context.packageName)
    }
    val modalOffsetY = remember { Animatable(0f) }
    var isClosing by remember { mutableStateOf(false) }
    val dismissTravel = configuration.screenHeightDp.dp.value * 3f

    LaunchedEffect(isClosing, dismissTravel) {
        if (!isClosing) return@LaunchedEffect
        modalOffsetY.animateTo(
            targetValue = dismissTravel,
            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
        )
        onDismiss()
    }

    Dialog(
        onDismissRequest = {
            if (!isClosing) {
                isClosing = true
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, modalOffsetY.value.roundToInt()) }
        ) {
            if (cardBackgroundResId != 0) {
                Image(
                    painter = painterResource(id = cardBackgroundResId),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1D2A3B))
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.offset(y = -4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (reward.isShiny) {
                            if (shinyBadgeResId != 0) {
                                Image(
                                    painter = painterResource(id = shinyBadgeResId),
                                    contentDescription = fr.pokenity.pokenity.ui.i18n.uiText("Badge shiny"),
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxWidth(0.42f)
                                        .height(34.dp)
                                )
                            } else {
                                Text(
                                    text = "SHINY",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Text(
                            text = if (isNewPokemonReward) "NOUVEAU" else "OBTENTION",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = reward.resourceName.prettyPokemonName(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            color = Color.Black,
                            modifier = Modifier.fillMaxWidth()
                        )

                        PokemonSpriteImage(
                            pokemonId = reward.resourceId,
                            contentDescription = reward.resourceName.prettyPokemonName(),
                            imageType = PokemonImageType.OFFICIAL_ARTWORK,
                            visualPreset = BOX_PIXEL_PRESET,
                            shiny = reward.isShiny,
                            filterQuality = FilterQuality.None,
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .aspectRatio(1f)
                        )

                    }
                }

                PrimaryButton(
                    onClick = {
                        if (!isClosing) {
                            isClosing = true
                        }
                    },
                    enabled = !isClosing,
                    modifier = Modifier.fillMaxWidth(0.72f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Fermer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun BoxClosetCollection(entries: List<BoxPokemonUi>) {
    val rows = remember(entries) {
        val chunked = entries.chunked(3).toMutableList()
        while (chunked.size < 4) {
            chunked.add(emptyList())
        }
        chunked.toList()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        rows.forEachIndexed { rowIndex, row ->
            val backgroundRes = when {
                rowIndex == 0 -> R.drawable.closet_top
                rowIndex == rows.lastIndex -> R.drawable.closet_bottom
                else -> R.drawable.closet_middle
            }
            BoxClosetRow(
                row = row,
                backgroundRes = backgroundRes
            )
        }
    }
}

@Composable
private fun BoxClosetRow(
    row: List<BoxPokemonUi>,
    backgroundRes: Int
) {
    val context = LocalContext.current
    val backgroundPainter = painterResource(id = backgroundRes)
    val backgroundRatio = remember(backgroundPainter) {
        val size = backgroundPainter.intrinsicSize
        if (size.isSpecified && size.height > 0f) size.width / size.height else 1f
    }
    val spriteYOffset = if (backgroundRes == R.drawable.closet_top) 12.dp else 4.dp

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
                            val rarity = rarityStyle(pokemon.dropRate)
                            val badgeResId = remember(rarity.badgeDrawableName) {
                                context.resources.getIdentifier(
                                    rarity.badgeDrawableName,
                                    "drawable",
                                    context.packageName
                                )
                            }
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                PokemonSpriteImage(
                                    pokemonId = pokemon.resourceId,
                                    contentDescription = pokemon.resourceName.prettyPokemonName(),
                                    imageType = PokemonImageType.OFFICIAL_ARTWORK,
                                    visualPreset = BOX_PIXEL_PRESET,
                                    shiny = pokemon.isShiny,
                                    filterQuality = FilterQuality.None,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .size(76.dp)
                                        .offset(y = spriteYOffset)
                                )

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(2.dp)
                                ) {
                                    Image(
                                        painter = painterResource(
                                            id = if (badgeResId != 0) badgeResId else R.drawable.badge_duplicate
                                        ),
                                        contentDescription = fr.pokenity.pokenity.ui.i18n.uiText("Badge probabilite"),
                                        modifier = Modifier.size(40.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                    Text(
                                        text = formatDropRatePercent(pokemon.dropRate),
                                        color = Color.Black,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontFamily = AppTitleFontFamily,
                                            fontSize = 12.sp,
                                            shadow = androidx.compose.ui.graphics.Shadow(
                                                color = Color.White,
                                                offset = androidx.compose.ui.geometry.Offset(0f, 0f),
                                                blurRadius = 4f
                                            )
                                        ),
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .padding(horizontal = 2.dp)
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
                imageType = PokemonImageType.OFFICIAL_ARTWORK,
                visualPreset = BOX_PIXEL_PRESET,
                shiny = pokemon.isShiny,
                filterQuality = FilterQuality.None,
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
                    imageType = PokemonImageType.OFFICIAL_ARTWORK,
                    visualPreset = BOX_PIXEL_PRESET,
                    shiny = pokemon.isShiny,
                    filterQuality = FilterQuality.None,
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
                        text = formatDropRatePercent(pokemon.dropRate),
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
            descBackgroundDrawableName = "desc_bg_legendary",
            badgeDrawableName = "badge_legendary"
        )
        dropRate <= 4.0 -> RarityStyle(
            backgroundDrawableName = "card_bg_epic",
            descBackgroundDrawableName = "desc_bg_epic",
            badgeDrawableName = "badge_epic"
        )
        dropRate <= 8.0 -> RarityStyle(
            backgroundDrawableName = "card_bg_rare",
            descBackgroundDrawableName = "desc_bg_rare",
            badgeDrawableName = "badge_rare"
        )
        dropRate <= 16.0 -> RarityStyle(
            backgroundDrawableName = "card_bg_uncommon",
            descBackgroundDrawableName = "desc_bg_uncommon",
            badgeDrawableName = "badge_uncommon"
        )
        else -> RarityStyle(
            backgroundDrawableName = "card_bg_common",
            descBackgroundDrawableName = "desc_bg_common",
            badgeDrawableName = "badge_common"
        )
    }
}

private fun formatDropRatePercent(dropRate: Double): String = "${dropRate.roundToInt()}%"

private fun cyclicIndex(index: Int, size: Int): Int {
    if (size <= 0) return 0
    val normalized = index % size
    return if (normalized < 0) normalized + size else normalized
}

private data class RarityStyle(
    val backgroundDrawableName: String,
    val descBackgroundDrawableName: String,
    val badgeDrawableName: String
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
