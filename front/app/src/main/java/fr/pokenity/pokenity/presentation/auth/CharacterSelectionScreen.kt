package fr.pokenity.pokenity.presentation.auth

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage

@Composable
fun CharacterSelectionScreen(
    uiState: AuthFlowUiState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onValidate: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedCharacter = uiState.characters.getOrNull(uiState.selectedCharacterIndex)

    AuthBackgroundContainer(
        backgroundDrawableName = "background",
        modifier = modifier.fillMaxSize()
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            val imageBottomOverflow = maxHeight * 0.15f

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
                    .zIndex(2f),
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
                    .padding(top = 42.dp)
                    .zIndex(2f),
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
            }

            if (uiState.isLoading && selectedCharacter == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (selectedCharacter != null) {
                AsyncImage(
                    model = if (selectedCharacter.imageUrl.isNotBlank()) {
                        selectedCharacter.imageUrl
                    } else {
                        selectedCharacter.avatarUrl
                    },
                    contentDescription = selectedCharacter.name,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .fillMaxHeight(0.95f)
                        .offset(y = imageBottomOverflow)
                        .graphicsLayer(
                            scaleX = 1.2f,
                            scaleY = 1.2f
                        ),
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.BottomCenter
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = 28.dp)
                        .zIndex(2f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (uiState.selectedCharacterIndex > 0) {
                            Button(
                                onClick = onPrevious,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AuthAccentYellow,
                                    contentColor = Color.Black
                                )
                            ) {
                                Text("<")
                            }
                        } else {
                            Spacer(Modifier.width(60.dp))
                        }

                        if (uiState.selectedCharacterIndex < uiState.characters.lastIndex) {
                            Button(
                                onClick = onNext,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AuthAccentYellow,
                                    contentColor = Color.Black
                                )
                            ) {
                                Text(">")
                            }
                        } else {
                            Spacer(Modifier.width(60.dp))
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

                    Button(
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
                        Text(
                            text = "Valider",
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = AuthFontFamily)
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
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
        }
    }
}
