package fr.pokenity.pokenity.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import fr.pokenity.data.core.AppUiLanguage
import fr.pokenity.data.core.AppUiLanguageState

private val frToEn = mapOf(
    "Accueil" to "Home",
    "Aucun Pokemon" to "No Pokemon",
    "Annuler" to "Cancel",
    "Avatar" to "Avatar",
    "Badge probabilite" to "Probability badge",
    "Badge shiny" to "Shiny badge",
    "Retour au Welcome" to "Back to welcome",
    "Box introuvable." to "Box not found.",
    "Changer de location" to "Change location",
    "Changer de region" to "Change region",
    "Changer de zone" to "Change area",
    "Choisir un Pokemon" to "Choose a Pokemon",
    "Comparateur" to "Comparator",
    "Comparer ce Pokemon" to "Compare this Pokemon",
    "Compte" to "Account",
    "Confirmer" to "Confirm",
    "Dresseur" to "Trainer",
    "Fermer" to "Close",
    "Moi" to "Me",
    "Ouvrir" to "Open",
    "Parametres" to "Settings",
    "Pokedex" to "Pokedex",
    "Preference" to "Preferences",
    "Proposer l'echange" to "Offer trade",
    "Rafraichir" to "Refresh",
    "Rechercher..." to "Search...",
    "Reessayer" to "Retry",
    "Refuser" to "Decline",
    "Reset" to "Reset",
    "Retirer" to "Remove",
    "Retirer tout" to "Remove all",
    "Retour" to "Back",
    "Selectionner un Pokemon a comparer" to "Select a Pokemon to compare",
    "Social" to "Social",
    "Stats" to "Stats",
    "Suivant" to "Next",
    "Toutes les boxes" to "All boxes",
    "Valider" to "Confirm",
    "Voir fiche" to "View details"
)

private val enToFr = frToEn.entries.associate { (fr, en) -> en to fr } + mapOf(
    "Settings" to "Parametres",
    "World Map Explorer" to "Explorateur de la carte du monde",
    "Duplicate badge" to "Badge doublon"
)

@Composable
fun uiText(value: String): String {
    val language by AppUiLanguageState.selectedLanguage.collectAsState()
    return when (language) {
        AppUiLanguage.ENGLISH -> frToEn[value] ?: value
        AppUiLanguage.FRENCH -> enToFr[value] ?: value
    }
}
