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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AccountScreen(
    uiState: AccountUiState,
    onFetchMe: () -> Unit,
    onLogout: () -> Unit,
    onGoToWelcome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize(), color = Color.Transparent) {
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
                Text("Session deconnectee.")
                Button(onClick = onGoToWelcome, modifier = Modifier.fillMaxWidth()) {
                    Text("Retour au Welcome")
                }
            }

            if (uiState.isLoading) {
                Spacer(Modifier.height(8.dp))
                CircularProgressIndicator()
            }
        }
    }
}
