package fr.pokenity.pokenity.presentation.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import fr.pokenity.pokenity.ui.components.PrimaryButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import fr.pokenity.pokenity.ui.media.resolveCharacterMediaModel

@Composable
fun CharacterSelectionScreen(
    uiState: AuthFlowUiState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onValidate: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val characterCount = uiState.characters.size
    val selectedCharacter = uiState.characters.getOrNull(uiState.selectedCharacterIndex)
    val previousCharacter = if (characterCount > 1) {
        uiState.characters[(uiState.selectedCharacterIndex - 1 + characterCount) % characterCount]
    } else {
        null
    }
    val nextCharacter = if (characterCount > 1) {
        uiState.characters[(uiState.selectedCharacterIndex + 1) % characterCount]
    } else {
        null
    }

    AuthBackgroundContainer(
        backgroundDrawableName = "dresseur_background",
        modifier = modifier.fillMaxSize()
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            if (uiState.isLoading && selectedCharacter == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (selectedCharacter != null) {
                val selectedModel by remember(selectedCharacter) {
                    androidx.compose.runtime.mutableStateOf(
                        selectedCharacter.preferredMediaModel()
                    )
                }
                val previousModel = if (previousCharacter != null) {
                    previousCharacter.preferredMediaModel()
                } else null
                val nextModel = if (nextCharacter != null) {
                    nextCharacter.preferredMediaModel()
                } else null

                // Prefetch adjacent characters to avoid network flash during slide.
                if (!previousModel.isNullOrBlank()) {
                    AsyncImage(
                        model = previousModel,
                        contentDescription = null,
                        modifier = Modifier
                            .size(1.dp)
                            .alpha(0f)
                            .align(Alignment.TopStart)
                    )
                }
                if (!nextModel.isNullOrBlank()) {
                    AsyncImage(
                        model = nextModel,
                        contentDescription = null,
                        modifier = Modifier
                            .size(1.dp)
                            .alpha(0f)
                            .align(Alignment.TopStart)
                    )
                }

                AnimatedContent(
                    targetState = uiState.selectedCharacterIndex,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxSize(),
                    transitionSpec = {
                        val moveForward = when {
                            characterCount <= 1 -> true
                            initialState == characterCount - 1 && targetState == 0 -> true
                            initialState == 0 && targetState == characterCount - 1 -> false
                            else -> targetState > initialState
                        }
                        if (moveForward) {
                            (slideInHorizontally(
                                animationSpec = tween(260),
                                initialOffsetX = { it / 2 }
                            ) + fadeIn(animationSpec = tween(200)))
                                .togetherWith(
                                    slideOutHorizontally(
                                        animationSpec = tween(260),
                                        targetOffsetX = { -it / 2 }
                                    ) + fadeOut(animationSpec = tween(180))
                                )
                        } else {
                            (slideInHorizontally(
                                animationSpec = tween(260),
                                initialOffsetX = { -it / 2 }
                            ) + fadeIn(animationSpec = tween(200)))
                                .togetherWith(
                                    slideOutHorizontally(
                                        animationSpec = tween(260),
                                        targetOffsetX = { it / 2 }
                                    ) + fadeOut(animationSpec = tween(180))
                                )
                        }
                    },
                    label = "character-slide"
                ) { index ->
                    val character = uiState.characters.getOrNull(index)
                    if (character != null) {
                        AsyncImage(
                            model = character.preferredMediaModel(),
                            contentDescription = character.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds,
                            alignment = Alignment.Center
                        )
                    } else {
                        AsyncImage(
                            model = selectedModel,
                            contentDescription = selectedCharacter.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds,
                            alignment = Alignment.Center
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Aucun dresseur disponible.",
                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = AuthFontFamily),
                        color = Color(0xFFD0EAFD),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .zIndex(2f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart),
                    horizontalArrangement = Arrangement.Start
                ) {
                    TextButton(onClick = onBack) {
                        Text(
                            text = "Retour",
                            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = AuthFontFamily),
                            color = Color(0xFFD0EAFD)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 42.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Choisis ton",
                        style = MaterialTheme.typography.headlineMedium.copy(fontFamily = AuthFontFamily),
                        color = Color(0xFFD0EAFD),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Dresseur",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = AuthFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color(0xFFD0EAFD),
                        textAlign = TextAlign.Center
                    )

                    if (uiState.errorMessage != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = uiState.errorMessage,
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = AuthFontFamily),
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (characterCount > 0) {
                        Spacer(Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(characterCount) { index ->
                                val isActive = index == uiState.selectedCharacterIndex
                                Box(
                                    modifier = Modifier
                                        .width(if (isActive) 18.dp else 12.dp)
                                        .height(if (isActive) 5.dp else 4.dp)
                                        .background(
                                            color = Color(0xFFD0EAFD).copy(alpha = if (isActive) 0.62f else 0.28f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                )
                            }
                        }
                    }
                }

                if (selectedCharacter != null) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(bottom = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PrimaryButton(
                                onClick = onPrevious,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AuthAccentYellow,
                                    contentColor = Color.Black
                                )
                            ) {
                                Text("<")
                            }

                            Spacer(Modifier.width(60.dp))

                            PrimaryButton(
                                onClick = onNext,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AuthAccentYellow,
                                    contentColor = Color.Black
                                )
                            ) {
                                Text(">")
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = selectedCharacter.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = AuthFontFamily,
                                fontWeight = FontWeight.Black,
                                fontSize = 34.sp
                            ),
                            color = Color(0xFFD0EAFD),
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(10.dp))

                        if (!uiState.errorMessage.isNullOrBlank()) {
                            Text(
                                text = uiState.errorMessage,
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = AuthFontFamily),
                                color = Color(0xFFFF8A80),
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(8.dp))
                        }

                        PrimaryButton(
                            onClick = onValidate,
                            enabled = !uiState.isLoading,
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AuthAccentYellow,
                                contentColor = Color.Black
                            )
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.Black,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Valider",
                                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = AuthFontFamily)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun AuthCharacterUiModel.preferredMediaModel(): String? {
    val rawValue = if (imageUrl.isNotBlank()) imageUrl else avatarUrl
    return resolveCharacterMediaModel(rawValue)
}
