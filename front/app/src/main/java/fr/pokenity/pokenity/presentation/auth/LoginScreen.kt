package fr.pokenity.pokenity.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import fr.pokenity.pokenity.ui.components.PrimaryButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    uiState: AuthFlowUiState,
    onEmailChange: (String) -> Unit,
    onCheckEmail: () -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val inputShape = RoundedCornerShape(0.dp)
    val inputColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = AuthInputText,
        unfocusedTextColor = AuthInputText,
        disabledTextColor = AuthInputText.copy(alpha = 0.7f),
        focusedContainerColor = AuthInputBackground,
        unfocusedContainerColor = AuthInputBackground,
        disabledContainerColor = AuthInputBackground.copy(alpha = 0.8f),
        cursorColor = AuthInputText,
        focusedBorderColor = AuthAccentYellow,
        unfocusedBorderColor = AuthInputBackground,
        disabledBorderColor = AuthInputBackground.copy(alpha = 0.8f),
        focusedLabelColor = AuthInputText,
        unfocusedLabelColor = AuthInputText,
        disabledLabelColor = AuthInputText
    )

    AuthBackgroundContainer(
        backgroundDrawableName = "auth_background",
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically)
        ) {
            Text(
                text = "Connecte-toi",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = AuthFontFamily
                ),
                fontWeight = FontWeight.ExtraBold,
                color = AuthAccentYellow,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = AuthBodyFontFamily),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = {
                    AuthInputLabel(text = "Email")
                },
                placeholder = {
                    Text(
                        text = "Email",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = AuthBodyFontFamily),
                        color = AuthInputPlaceholder
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = AuthBodyFontFamily),
                shape = inputShape,
                colors = inputColors,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = if (!uiState.emailChecked) ImeAction.Done else ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onDone = { if (!uiState.emailChecked) onCheckEmail() }
                )
            )

            // Phase 1 : email pas encore verifie -> bouton Connexion
            if (!uiState.emailChecked) {
                PrimaryButton(
                    onClick = onCheckEmail,
                    enabled = !uiState.isLoading && uiState.email.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Text(
                        text = "Connexion",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = AuthFontFamily)
                    )
                }
            }

            // Phase 2 : email verifie et compte existant -> champ mot de passe
            AnimatedVisibility(visible = uiState.emailChecked && uiState.emailExists) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = uiState.loginPassword,
                        onValueChange = onPasswordChange,
                        label = {
                            AuthInputLabel(text = "Mot de passe")
                        },
                        placeholder = {
                            Text(
                                text = "Mot de passe",
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = AuthBodyFontFamily),
                                color = AuthInputPlaceholder
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = AuthBodyFontFamily),
                        shape = inputShape,
                        colors = inputColors,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onLogin() })
                    )

                    PrimaryButton(
                        onClick = onLogin,
                        enabled = !uiState.isLoading && uiState.loginPassword.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text(
                            text = "Se connecter",
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = AuthFontFamily)
                        )
                    }
                }
            }

            if (uiState.isLoading) {
                Spacer(Modifier.height(8.dp))
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}

@Composable
private fun AuthInputLabel(
    text: String
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = AuthBodyFontFamily),
        color = AuthInputText,
        modifier = Modifier
            .background(AuthInputBackground)
            .padding(horizontal = 4.dp)
    )
}
