package fr.pokenity.pokenity.presentation.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AccountScreen(
    uiState: AccountUiState,
    onModeChange: (AuthMode) -> Unit,
    onLoginIdentifierChange: (String) -> Unit,
    onLoginPasswordChange: (String) -> Unit,
    onRegisterUsernameChange: (String) -> Unit,
    onRegisterEmailChange: (String) -> Unit,
    onRegisterPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onFetchMe: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = uiState.user?.username ?: "Compte",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )

            if (uiState.errorMessage != null) {
                Text(uiState.errorMessage, color = MaterialTheme.colorScheme.error)
            }
            if (uiState.infoMessage != null) {
                Text(uiState.infoMessage, color = MaterialTheme.colorScheme.primary)
            }

            if (uiState.user != null) {
                Text("Connecte en tant que:", style = MaterialTheme.typography.titleMedium)
                Text("Id: ${uiState.user.id}")
                Text("Username: ${uiState.user.username}")
                Text("Email: ${uiState.user.email}")
                if (!uiState.user.createdAt.isNullOrBlank()) {
                    Text("Compte cree le: ${uiState.user.createdAt}")
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onFetchMe) { Text("Rafraichir profil") }
                    Button(onClick = onLogout) { Text("Deconnexion") }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ModeButton(
                        label = "Connexion",
                        selected = uiState.mode == AuthMode.LOGIN,
                        onClick = { onModeChange(AuthMode.LOGIN) }
                    )
                    ModeButton(
                        label = "Inscription",
                        selected = uiState.mode == AuthMode.REGISTER,
                        onClick = { onModeChange(AuthMode.REGISTER) }
                    )
                }

                if (uiState.mode == AuthMode.LOGIN) {
                    OutlinedTextField(
                        value = uiState.loginIdentifier,
                        onValueChange = onLoginIdentifierChange,
                        label = { Text("Email ou username") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = uiState.loginPassword,
                        onValueChange = onLoginPasswordChange,
                        label = { Text("Mot de passe") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Button(onClick = onLogin, enabled = !uiState.isLoading) { Text("Se connecter") }
                } else {
                    OutlinedTextField(
                        value = uiState.registerUsername,
                        onValueChange = onRegisterUsernameChange,
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = uiState.registerEmail,
                        onValueChange = onRegisterEmailChange,
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = uiState.registerPassword,
                        onValueChange = onRegisterPasswordChange,
                        label = { Text("Mot de passe") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Button(onClick = onRegister, enabled = !uiState.isLoading) { Text("Creer le compte") }
                }
            }

            if (uiState.isLoading) {
                Spacer(Modifier.height(8.dp))
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun ModeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    if (selected) {
        Button(onClick = onClick) { Text(label) }
    } else {
        OutlinedButton(onClick = onClick) { Text(label) }
    }
}
