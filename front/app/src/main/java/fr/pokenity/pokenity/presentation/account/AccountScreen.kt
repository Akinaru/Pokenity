package fr.pokenity.pokenity.presentation.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import fr.pokenity.pokenity.ui.components.PrimaryButton
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import fr.pokenity.data.core.PokemonImageSettings
import fr.pokenity.data.core.PokemonImageType
import fr.pokenity.pokenity.R
import fr.pokenity.pokenity.presentation.auth.AuthAccentYellow
import fr.pokenity.pokenity.ui.components.PokemonSpriteImage
import fr.pokenity.pokenity.ui.media.resolveCharacterMediaModel
import fr.pokenity.pokenity.ui.theme.MistWhite
import fr.pokenity.pokenity.ui.theme.PrimaryButtonOrange

@Composable
fun AccountScreen(
    uiState: AccountUiState,
    onFetchMe: () -> Unit,
    onLogout: () -> Unit,
    onGoToWelcome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spriteType by PokemonImageSettings.imageType.collectAsState()
    val shinyEnabled by PokemonImageSettings.isShiny.collectAsState()
    val ownedPokemonIds = remember(uiState.pokemonCollection) {
        uiState.pokemonCollection
            .asSequence()
            .filter { (_, quantity) -> quantity > 0 }
            .map { it.key }
            .sorted()
            .toList()
    }

    Surface(modifier = modifier.fillMaxSize(), color = Color.Transparent) {
        if (uiState.isLoading && uiState.user == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AuthAccentYellow)
            }
            return@Surface
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = uiState.user?.username ?: "Compte",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MistWhite,
                textAlign = TextAlign.Center
            )

            if (uiState.user != null) {
                AccountAvatar(
                    url = uiState.user.characterAvatarUrl ?: uiState.user.characterImageUrl,
                    label = uiState.user.username
                )

                if (!uiState.user.characterName.isNullOrBlank()) {
                    Text(
                        text = uiState.user.characterName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MistWhite
                    )
                }

                if (!uiState.errorMessage.isNullOrBlank()) {
                    MessageCard(
                        message = uiState.errorMessage,
                        color = Color(0xFFFF8A80)
                    )
                }

                if (!uiState.infoMessage.isNullOrBlank()) {
                    MessageCard(
                        message = uiState.infoMessage,
                        color = AuthAccentYellow
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0x66180707)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Informations du compte",
                            style = MaterialTheme.typography.titleMedium,
                            color = MistWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                        AccountInfoRow(label = "Pseudo", value = uiState.user.username)
                        AccountInfoRow(label = "Email", value = uiState.user.email)
                        AccountInfoRow(
                            label = "Dresseur",
                            value = uiState.user.characterName ?: "Non defini"
                        )
                        if (!uiState.user.createdAt.isNullOrBlank()) {
                            AccountInfoRow(
                                label = "Date de creation de compte",
                                value = uiState.user.createdAt
                            )
                        }
                    }
                }

                if (ownedPokemonIds.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0x66180707)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Collection",
                                style = MaterialTheme.typography.titleMedium,
                                color = MistWhite,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${ownedPokemonIds.size} Pokemon possedes",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MistWhite.copy(alpha = 0.82f)
                            )
                            AccountClosetCollection(
                                pokemonIds = ownedPokemonIds,
                                ownedQuantities = uiState.pokemonCollection,
                                spriteType = spriteType,
                                shinyEnabled = shinyEnabled
                            )
                        }
                    }
                }

                PrimaryButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp),
                    border = BorderStroke(3.dp, PrimaryButtonOrange)
                ) {
                    Text(
                        text = "DECONNEXION",
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp,
                        textDecoration = TextDecoration.None
                    )
                }

                if (uiState.isLoading) {
                    CircularProgressIndicator(color = AuthAccentYellow)
                }
            } else {
                if (!uiState.errorMessage.isNullOrBlank()) {
                    MessageCard(
                        message = uiState.errorMessage,
                        color = Color(0xFFFF8A80)
                    )
                }

                Text(
                    text = "Session deconnectee",
                    style = MaterialTheme.typography.titleMedium,
                    color = MistWhite
                )

                PrimaryButton(
                    onClick = onGoToWelcome,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Retour au Welcome", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AccountClosetCollection(
    pokemonIds: List<Int>,
    ownedQuantities: Map<Int, Int>,
    spriteType: PokemonImageType,
    shinyEnabled: Boolean
) {
    val rows = remember(pokemonIds) { pokemonIds.chunked(3) }

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
            AccountClosetRow(
                row = row,
                backgroundRes = backgroundRes,
                ownedQuantities = ownedQuantities,
                spriteType = spriteType,
                shinyEnabled = shinyEnabled
            )
        }
    }
}

@Composable
private fun AccountClosetRow(
    row: List<Int>,
    backgroundRes: Int,
    ownedQuantities: Map<Int, Int>,
    spriteType: PokemonImageType,
    shinyEnabled: Boolean
) {
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
                    val pokemonId = row.getOrNull(slotIndex)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (pokemonId != null) {
                            val quantity = ownedQuantities[pokemonId] ?: 0
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                PokemonSpriteImage(
                                    pokemonId = pokemonId,
                                    contentDescription = "Pokemon $pokemonId",
                                    imageType = spriteType,
                                    shiny = shinyEnabled,
                                    colorFilter = if (quantity > 0) null else ColorFilter.tint(Color.Black, BlendMode.SrcIn),
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .size(76.dp)
                                        .offset(y = spriteYOffset)
                                )

                                if (quantity > 1) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(2.dp)
                                    ) {
                                        Text(
                                            text = "x$quantity",
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
}

@Composable
private fun AccountAvatar(
    url: String?,
    label: String
) {
    val mediaModel = resolveCharacterMediaModel(url)

    Box(
        modifier = Modifier
            .size(164.dp)
            .clip(CircleShape)
            .background(Color(0x66180707))
            .border(width = 2.dp, color = MistWhite.copy(alpha = 0.65f), shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (mediaModel != null) {
            AsyncImage(
                model = mediaModel,
                contentDescription = "$label avatar",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                tint = MistWhite.copy(alpha = 0.82f)
            )
        }
    }
}

@Composable
private fun AccountInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MistWhite.copy(alpha = 0.72f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MistWhite,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun MessageCard(
    message: String,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0x66180707)
    ) {
        Text(
            text = message,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}
