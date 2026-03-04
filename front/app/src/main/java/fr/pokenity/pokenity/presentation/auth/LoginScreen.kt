package fr.pokenity.pokenity.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onGoToRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(48.dp))

            Text(
                text = "Connecte-toi",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(Modifier.height(8.dp))

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            OutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = if (!uiState.emailChecked) ImeAction.Done else ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onDone = { if (!uiState.emailChecked) onCheckEmail() }
                ),
                enabled = !uiState.emailChecked
            )

            // Phase 1 : email pas encore verifie -> bouton Connexion
            if (!uiState.emailChecked) {
                Button(
                    onClick = onCheckEmail,
                    enabled = !uiState.isLoading && uiState.email.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Connexion")
                }
            }

            // Phase 2 : email verifie et compte existant -> champ mot de passe
            AnimatedVisibility(visible = uiState.emailChecked && uiState.emailExists) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = uiState.loginPassword,
                        onValueChange = onPasswordChange,
                        label = { Text("Mot de passe") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onLogin() })
                    )

                    Button(
                        onClick = onLogin,
                        enabled = !uiState.isLoading && uiState.loginPassword.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Se connecter")
                    }
                }
            }

            // Phase 2 : email verifie et pas de compte -> message + bouton inscription
            AnimatedVisibility(visible = uiState.emailChecked && !uiState.emailExists) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Aucun compte associe a cet email.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = onGoToRegister,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Creer un compte")
                    }
                }
            }

            TextButton(
                onClick = onGoToRegister,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Pas de compte ? S'inscrire")
            }

            if (uiState.isLoading) {
                Spacer(Modifier.height(8.dp))
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}
