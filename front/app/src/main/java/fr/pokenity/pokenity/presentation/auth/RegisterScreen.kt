package fr.pokenity.pokenity.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun RegisterScreen(
    uiState: AuthFlowUiState,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRegister: () -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val inputShape = RoundedCornerShape(16.dp)
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
        focusedLabelColor = AuthAccentYellow,
        unfocusedLabelColor = AuthInputText.copy(alpha = 0.85f),
        disabledLabelColor = AuthInputText.copy(alpha = 0.65f)
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
                text = "Creer un compte",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = AuthFontFamily
                ),
                fontWeight = FontWeight.ExtraBold,
                color = AuthAccentYellow,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.registerUsername,
                onValueChange = onUsernameChange,
                label = {
                    Text(
                        text = "Username",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = AuthFontFamily)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = AuthFontFamily),
                shape = inputShape,
                colors = inputColors,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = uiState.registerEmail,
                onValueChange = onEmailChange,
                label = {
                    Text(
                        text = "Email",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = AuthFontFamily)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = AuthFontFamily),
                shape = inputShape,
                colors = inputColors,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = uiState.registerPassword,
                onValueChange = onPasswordChange,
                label = {
                    Text(
                        text = "Mot de passe",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = AuthFontFamily)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = AuthFontFamily),
                shape = inputShape,
                colors = inputColors,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onRegister() })
            )

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = AuthFontFamily),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = onRegister,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AuthAccentYellow,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "Creer le compte",
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = AuthFontFamily)
                )
            }

            TextButton(
                onClick = onBackToLogin,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Deja un compte ? Se connecter",
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = AuthFontFamily),
                    color = AuthAccentYellow
                )
            }

            if (uiState.isLoading) {
                Spacer(Modifier.height(8.dp))
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}
