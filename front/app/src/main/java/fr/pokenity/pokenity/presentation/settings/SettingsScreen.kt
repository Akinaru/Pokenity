package fr.pokenity.pokenity.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import fr.pokenity.pokenity.ui.components.PrimaryButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.pokenity.data.core.AppThemeMode
import fr.pokenity.data.core.PokemonImageType

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onRetry: () -> Unit,
    onLanguageSelected: (String) -> Unit,
    onImageTypeSelected: (PokemonImageType) -> Unit,
    onThemeModeSelected: (AppThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        }

        uiState.errorMessage != null -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = uiState.errorMessage, style = MaterialTheme.typography.bodyLarge)
                PrimaryButton(onClick = onRetry, modifier = Modifier.padding(top = 12.dp)) {
                    Text("Reessayer")
                }
            }
        }

        else -> {
            var languageExpanded by remember { mutableStateOf(false) }
            var imageTypeExpanded by remember { mutableStateOf(false) }
            var themeExpanded by remember { mutableStateOf(false) }
            val selectedLanguage = uiState.languages.firstOrNull { it.code == uiState.selectedLanguageCode }

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "Settings", style = MaterialTheme.typography.headlineSmall)

                Text(text = "Theme", style = MaterialTheme.typography.titleMedium)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { themeExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            when (uiState.selectedThemeMode) {
                                AppThemeMode.SYSTEM -> "Appareil"
                                AppThemeMode.LIGHT -> "Clair"
                                AppThemeMode.DARK -> "Sombre"
                            }
                        )
                    }
                    DropdownMenu(expanded = themeExpanded, onDismissRequest = { themeExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Appareil") },
                            onClick = {
                                onThemeModeSelected(AppThemeMode.SYSTEM)
                                themeExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Clair") },
                            onClick = {
                                onThemeModeSelected(AppThemeMode.LIGHT)
                                themeExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sombre") },
                            onClick = {
                                onThemeModeSelected(AppThemeMode.DARK)
                                themeExpanded = false
                            }
                        )
                    }
                }

                Text(
                    text = "Langue des donnees PokeAPI",
                    style = MaterialTheme.typography.titleMedium
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { languageExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(selectedLanguage?.let { "${it.label} (${it.code})" } ?: "Choisir une langue")
                    }
                    DropdownMenu(expanded = languageExpanded, onDismissRequest = { languageExpanded = false }) {
                        uiState.languages.forEach { language ->
                            DropdownMenuItem(
                                text = { Text("${language.label} (${language.code})") },
                                onClick = {
                                    onLanguageSelected(language.code)
                                    languageExpanded = false
                                }
                            )
                        }
                    }
                }

                Text(
                    text = "Type d'image Pokemon",
                    style = MaterialTheme.typography.titleMedium
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { imageTypeExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(uiState.selectedImageType.label)
                    }
                    DropdownMenu(expanded = imageTypeExpanded, onDismissRequest = { imageTypeExpanded = false }) {
                        PokemonImageType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.label) },
                                onClick = {
                                    onImageTypeSelected(type)
                                    imageTypeExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
