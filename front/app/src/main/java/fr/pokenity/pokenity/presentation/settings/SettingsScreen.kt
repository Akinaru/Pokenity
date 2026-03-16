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
import androidx.compose.ui.res.stringResource
import fr.pokenity.data.core.AppThemeMode
import fr.pokenity.data.core.AppUiLanguage
import fr.pokenity.data.core.PokemonImageType
import fr.pokenity.pokenity.R

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onRetry: () -> Unit,
    onUiLanguageSelected: (AppUiLanguage) -> Unit,
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
                    Text(stringResource(R.string.common_retry))
                }
            }
        }

        else -> {
            var languageExpanded by remember { mutableStateOf(false) }
            var imageTypeExpanded by remember { mutableStateOf(false) }
            var themeExpanded by remember { mutableStateOf(false) }
            var uiLanguageExpanded by remember { mutableStateOf(false) }
            val selectedLanguage = uiState.languages.firstOrNull { it.code == uiState.selectedLanguageCode }

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineSmall)

                Text(
                    text = stringResource(R.string.settings_app_language_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { uiLanguageExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            when (uiState.selectedUiLanguage) {
                                AppUiLanguage.FRENCH -> stringResource(R.string.settings_language_french)
                                AppUiLanguage.ENGLISH -> stringResource(R.string.settings_language_english)
                            }
                        )
                    }
                    DropdownMenu(expanded = uiLanguageExpanded, onDismissRequest = { uiLanguageExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settings_language_french)) },
                            onClick = {
                                onUiLanguageSelected(AppUiLanguage.FRENCH)
                                uiLanguageExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settings_language_english)) },
                            onClick = {
                                onUiLanguageSelected(AppUiLanguage.ENGLISH)
                                uiLanguageExpanded = false
                            }
                        )
                    }
                }

                Text(text = stringResource(R.string.settings_theme_title), style = MaterialTheme.typography.titleMedium)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { themeExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            when (uiState.selectedThemeMode) {
                                AppThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
                                AppThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
                                AppThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
                            }
                        )
                    }
                    DropdownMenu(expanded = themeExpanded, onDismissRequest = { themeExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settings_theme_system)) },
                            onClick = {
                                onThemeModeSelected(AppThemeMode.SYSTEM)
                                themeExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settings_theme_light)) },
                            onClick = {
                                onThemeModeSelected(AppThemeMode.LIGHT)
                                themeExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settings_theme_dark)) },
                            onClick = {
                                onThemeModeSelected(AppThemeMode.DARK)
                                themeExpanded = false
                            }
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.settings_pokeapi_language_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { languageExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(selectedLanguage?.let { "${it.label} (${it.code})" } ?: stringResource(R.string.settings_choose_language))
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
                    text = stringResource(R.string.settings_image_type_title),
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
