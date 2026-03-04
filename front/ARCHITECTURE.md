# Pokenity - Architecture proposee (V1)

## Objectifs
- Consultation des Pokemon sans connexion (Pokedex, comparaison, filtres)
- Fonctionnalites connectees (collection, boites, echanges, profil)
- Base evolutive pour internationalisation et backend

## Organisation du code Android

```
app/src/main/java/fr/pokenity/pokenity/
  data/
    remote/      // appels PokeAPI + DTO
    repository/  // implementation des repositories
  domain/
    model/       // modeles metier purs
    repository/  // contrats de repositories
    usecase/     // logique metier applicative
  presentation/
    pokedex/     // ViewModel + UiState + composables
```

## Flux de donnees
1. UI Compose observe le `StateFlow` du ViewModel.
2. Le ViewModel appelle un `UseCase`.
3. Le UseCase utilise un `Repository` (interface domaine).
4. Le Repository implemente la source (PokeAPI et plus tard backend/bdd).
5. Resultat mappe en modeles metier puis affiche en UI.

## Evolution prevue
- `data/local/` : cache SQLite/Room (Pokemon consultables hors-ligne)
- `data/remote/backend/` : authentification + collection + echanges
- `presentation/auth/`, `presentation/collection/`, `presentation/trade/`
- Navigation Compose pour gerer les ecrans
- Systeme de traduction via `strings.xml` par locale

## Decoupage fonctionnel cible
- Public (sans login):
  - Liste Pokemon
  - Filtres (types, lieux, generations)
  - Comparaison de stats
- Connecte:
  - Ouverture de boites
  - Collection personnelle
  - Collections publiques d'autres joueurs
  - Echanges entre utilisateurs
  - Gestion du compte

## Backend / BDD (phase suivante)
- API backend (Node, Java ou autre) + DB relationnelle
- Entites: `users`, `pokemon_instances`, `boxes`, `trades`, `collections`
- JWT/Session pour auth
- Synchronisation client/back pour collection et echanges
