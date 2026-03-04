package fr.pokenity.pokenity.presentation.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.pokenity.pokenity.core.PokemonImageSettings
import fr.pokenity.pokenity.domain.model.PokemonFilterOption
import fr.pokenity.pokenity.domain.model.PokemonSummary
import fr.pokenity.pokenity.ui.components.PokemonSpriteImage
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MapScreen(
    uiState: MapUiState,
    onRetry: () -> Unit,
    onPokemonClick: (Int, List<Int>) -> Unit,
    onRegionSelected: (PokemonFilterOption) -> Unit,
    onLocationSelected: (PokemonFilterOption) -> Unit,
    onAreaSelected: (PokemonFilterOption) -> Unit,
    onBackToRegions: () -> Unit,
    onBackToLocations: () -> Unit,
    onBackToAreas: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spriteType by PokemonImageSettings.imageType.collectAsState()
    val shinyEnabled by PokemonImageSettings.isShiny.collectAsState()

    Surface(modifier = modifier.fillMaxSize(), color = Color.Transparent) {
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
                            subtitle = "Pince pour zoomer, glisse pour explorer, touche une region"
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = { PokemonImageSettings.toggleShiny() },
                                enabled = spriteType.supportsShiny
                            ) {
                                Text(if (shinyEnabled) "Shiny ON" else "Shiny OFF")
                            }
                        }
                    }

                    item {
                        StepRow(
                            region = uiState.selectedRegion?.label,
                            location = uiState.selectedLocation?.label,
                            area = uiState.selectedArea?.label
                        )
                    }

                    if (uiState.selectedRegion != null) {
                        item { OutlinedButton(onClick = onBackToRegions) { Text("Changer de region") } }
                    }
                    if (uiState.selectedLocation != null) {
                        item { OutlinedButton(onClick = onBackToLocations) { Text("Changer de location") } }
                    }
                    if (uiState.selectedArea != null) {
                        item { OutlinedButton(onClick = onBackToAreas) { Text("Changer de zone") } }
                    }

                    when {
                        uiState.selectedRegion == null -> {
                            item { SectionTitle("Carte des regions") }
                            item {
                                InteractiveRegionMap(
                                    regions = uiState.regions,
                                    selectedRegion = uiState.selectedRegion,
                                    onRegionSelected = onRegionSelected
                                )
                            }
                            item {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(uiState.regions, key = { it.apiName }) { region ->
                                        OutlinedButton(onClick = { onRegionSelected(region) }) {
                                            Text(region.label)
                                        }
                                    }
                                }
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
                                val ids = uiState.pokemons.map { it.id }
                                items(uiState.pokemons, key = { it.id }) { pokemon ->
                                    PokemonRow(
                                        pokemon = pokemon,
                                        imageType = spriteType,
                                        shinyEnabled = shinyEnabled,
                                        onClick = { onPokemonClick(pokemon.id, ids) }
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
            Text(text = title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
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
private fun InteractiveRegionMap(
    regions: List<PokemonFilterOption>,
    selectedRegion: PokemonFilterOption?,
    onRegionSelected: (PokemonFilterOption) -> Unit
) {
    val density = LocalDensity.current
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Surface(
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFFDCF1FF), Color(0xFFCBE8FF), Color(0xFFEAF6FF))
                    )
                )
                .padding(12.dp)
        ) {
            val mapWidthPx = with(density) { maxWidth.toPx() }
            val mapHeightPx = with(density) { 340.dp.toPx() }

            val regionPins = regions.mapIndexed { index, region ->
                val fallback = circularPin(index, regions.size)
                val mapped = regionPinByName(region.apiName)
                RegionPin(
                    region = region,
                    x = mapped?.x ?: fallback.first,
                    y = mapped?.y ?: fallback.second,
                    color = mapped?.color ?: Color(0xFF3B82F6)
                )
            }

            val maxPanX = ((scale - 1f) * mapWidthPx * 0.5f).coerceAtLeast(0f)
            val maxPanY = ((scale - 1f) * mapHeightPx * 0.5f).coerceAtLeast(0f)

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(20.dp))
                    .pointerInput(mapWidthPx, mapHeightPx, scale) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 3.2f)
                            val newX = offsetX + pan.x
                            val newY = offsetY + pan.y
                            val nextMaxPanX = ((scale - 1f) * mapWidthPx * 0.5f).coerceAtLeast(0f)
                            val nextMaxPanY = ((scale - 1f) * mapHeightPx * 0.5f).coerceAtLeast(0f)
                            offsetX = newX.coerceIn(-nextMaxPanX, nextMaxPanX)
                            offsetY = newY.coerceIn(-nextMaxPanY, nextMaxPanY)
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offsetX
                            translationY = offsetY
                        }
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        drawRect(color = Color(0xFFB8E3FF).copy(alpha = 0.25f))
                        repeat(8) { row ->
                            val y = size.height * (row / 8f)
                            drawLine(
                                color = Color.White.copy(alpha = 0.22f),
                                start = androidx.compose.ui.geometry.Offset(0f, y),
                                end = androidx.compose.ui.geometry.Offset(size.width, y),
                                strokeWidth = 2f
                            )
                        }
                        repeat(10) { col ->
                            val x = size.width * (col / 10f)
                            drawLine(
                                color = Color.White.copy(alpha = 0.16f),
                                start = androidx.compose.ui.geometry.Offset(x, 0f),
                                end = androidx.compose.ui.geometry.Offset(x, size.height),
                                strokeWidth = 2f
                            )
                        }

                        drawCircle(
                            color = Color(0xFF79C972).copy(alpha = 0.22f),
                            radius = size.minDimension * 0.20f,
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.22f, size.height * 0.52f)
                        )
                        drawCircle(
                            color = Color(0xFF7ACC88).copy(alpha = 0.20f),
                            radius = size.minDimension * 0.17f,
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.58f, size.height * 0.34f)
                        )
                        drawCircle(
                            color = Color(0xFF66C68A).copy(alpha = 0.20f),
                            radius = size.minDimension * 0.16f,
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.73f, size.height * 0.67f)
                        )

                        regionPins.zipWithNext().forEach { (a, b) ->
                            drawLine(
                                color = Color(0xFF0EA5E9).copy(alpha = 0.45f),
                                start = androidx.compose.ui.geometry.Offset(a.x * size.width, a.y * size.height),
                                end = androidx.compose.ui.geometry.Offset(b.x * size.width, b.y * size.height),
                                strokeWidth = 4f
                            )
                        }
                    }

                    regionPins.forEach { pin ->
                        val isSelected = selectedRegion?.apiName == pin.region.apiName
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .align(BiasAlignment((pin.x * 2f) - 1f, (pin.y * 2f) - 1f))
                                .clickable { onRegionSelected(pin.region) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 20.dp else 16.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color.White else pin.color)
                                    .border(
                                        width = 2.dp,
                                        color = if (isSelected) pin.color else Color.White,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            )
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) pin.color else pin.color.copy(alpha = 0.90f),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = pin.region.label,
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(onClick = {
                    scale = (scale - 0.25f).coerceAtLeast(1f)
                    if (scale == 1f) {
                        offsetX = 0f
                        offsetY = 0f
                    } else {
                        offsetX = offsetX.coerceIn(-maxPanX, maxPanX)
                        offsetY = offsetY.coerceIn(-maxPanY, maxPanY)
                    }
                }) { Text("-") }
                OutlinedButton(onClick = { scale = (scale + 0.25f).coerceAtMost(3.2f) }) { Text("+") }
                OutlinedButton(onClick = {
                    scale = 1f
                    offsetX = 0f
                    offsetY = 0f
                }) { Text("Reset") }
            }
        }
    }
}

private data class RegionPin(
    val region: PokemonFilterOption,
    val x: Float,
    val y: Float,
    val color: Color
)

private data class MappedPin(val x: Float, val y: Float, val color: Color)

private fun regionPinByName(apiName: String): MappedPin? {
    return when (apiName.lowercase()) {
        "kanto" -> MappedPin(0.18f, 0.55f, Color(0xFFEF4444))
        "johto" -> MappedPin(0.30f, 0.38f, Color(0xFFF97316))
        "hoenn" -> MappedPin(0.45f, 0.62f, Color(0xFF06B6D4))
        "sinnoh" -> MappedPin(0.56f, 0.30f, Color(0xFF3B82F6))
        "unova" -> MappedPin(0.68f, 0.49f, Color(0xFF6366F1))
        "kalos" -> MappedPin(0.77f, 0.28f, Color(0xFF8B5CF6))
        "alola" -> MappedPin(0.79f, 0.70f, Color(0xFF14B8A6))
        "galar" -> MappedPin(0.90f, 0.43f, Color(0xFF10B981))
        "paldea" -> MappedPin(0.61f, 0.73f, Color(0xFFF59E0B))
        else -> null
    }
}

private fun circularPin(index: Int, total: Int): Pair<Float, Float> {
    if (total <= 1) return 0.5f to 0.5f
    val angle = ((2 * PI) / total) * index
    val radius = 0.30f
    val x = 0.5f + (cos(angle).toFloat() * radius)
    val y = 0.5f + (sin(angle).toFloat() * radius)
    return x.coerceIn(0.1f, 0.9f) to y.coerceIn(0.12f, 0.88f)
}

@Composable
private fun PokemonRow(
    pokemon: PokemonSummary,
    imageType: fr.pokenity.pokenity.core.PokemonImageType,
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
                imageType = imageType,
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
        Button(onClick = onRetry) { Text("Reessayer") }
    }
}
