# Pokenity API (Express + MySQL + Prisma)

API backend pour l'app Android Pokenity.

V1 actuelle:
- Auth: inscription, connexion, profil courant
- Endpoint Pokémon (proxy vers PokéAPI)
- Persistance MySQL avec migrations Prisma

## Stack

- Node.js 22+
- Express 5
- Prisma + MySQL
- JWT (`jsonwebtoken`)
- Hash mot de passe (`bcryptjs`)

## Configuration

Copier le fichier d'env:

```bash
cd /Users/maximegallotta/Documents/Developpement/Pokenity/back
cp .env.example .env
```

Variables importantes:

```env
PORT=3000
JWT_SECRET=change_me_to_a_long_random_string
JWT_EXPIRES_IN=7d
DATABASE_URL="mysql://root@127.0.0.1:3309/pokenity"
POKEAPI_BASE_URL=https://pokeapi.co/api/v2
```

## Installation

```bash
npm install
```

## Migrations

Créer la base si besoin:

```bash
docker exec db_docker_pokenity mariadb -uroot -e "CREATE DATABASE IF NOT EXISTS pokenity CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

Appliquer/générer les migrations en dev:

```bash
npm run prisma:migrate -- --name init_auth
```

Regénérer le client Prisma:

```bash
npm run prisma:generate
```

En environnement non-dev (CI/prod):

```bash
npm run prisma:deploy
```

## Lancer l'API

```bash
npm run dev
```

API dispo sur: `http://localhost:3000`

## Entités (Prisma)

### `User` (`users`)

- `id` (UUID, PK)
- `username` (unique)
- `email` (unique)
- `passwordHash`
- `createdAt`
- `updatedAt`

Schéma: [schema.prisma](/Users/maximegallotta/Documents/Developpement/Pokenity/back/prisma/schema.prisma)

## Documentation Endpoints

Base URL: `http://localhost:3000/api`

### 1) Healthcheck

`GET /health`

Exemple réponse:

```json
{
  "status": "ok",
  "service": "pokenity-back",
  "db": "up",
  "now": "2026-03-04T08:00:00.000Z"
}
```

### 2) Register

`POST /auth/register`

Body:

```json
{
  "username": "ash",
  "email": "ash@kanto.com",
  "password": "pikachu123"
}
```

Réponse `201`:

```json
{
  "token": "<jwt>",
  "user": {
    "id": "uuid",
    "username": "ash",
    "email": "ash@kanto.com",
    "createdAt": "2026-03-04T08:00:00.000Z"
  }
}
```

Erreurs:
- `400` données invalides
- `409` email/username déjà pris

### 3) Login

`POST /auth/login`

Body:

```json
{
  "identifier": "ash@kanto.com",
  "password": "pikachu123"
}
```

`identifier` accepte email ou username.

Réponse `200`:

```json
{
  "token": "<jwt>",
  "user": {
    "id": "uuid",
    "username": "ash",
    "email": "ash@kanto.com",
    "createdAt": "2026-03-04T08:00:00.000Z"
  }
}
```

Erreurs:
- `400` données manquantes
- `401` credentials invalides

### 4) Me (protégé JWT)

`GET /auth/me`

Headers:

```http
Authorization: Bearer <token>
```

Réponse `200`:

```json
{
  "user": {
    "id": "uuid",
    "username": "ash",
    "email": "ash@kanto.com",
    "createdAt": "2026-03-04T08:00:00.000Z"
  }
}
```

Erreurs:
- `401` token manquant/invalide/expiré
- `404` user introuvable

### 5) Pokémon (protégé JWT)

`GET /pokemon/:name`

Headers:

```http
Authorization: Bearer <token>
```

Exemple:

```http
GET /pokemon/pikachu
```

Réponse `200`:

```json
{
  "id": 25,
  "name": "pikachu",
  "height": 4,
  "weight": 60,
  "types": ["electric"],
  "stats": [
    { "name": "hp", "value": 35 }
  ],
  "sprites": {
    "frontDefault": "https://...",
    "officialArtwork": "https://..."
  }
}
```

Erreurs:
- `400` nom manquant
- `401` token manquant/invalide
- `404` Pokémon introuvable
- `502` PokéAPI indisponible

## Commandes utiles

Extraire un JWT depuis le login:

```bash
curl -sS -X POST http://127.0.0.1:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"ash@kanto.com","password":"pikachu123"}' \
| node -e "let d='';process.stdin.on('data',c=>d+=c).on('end',()=>console.log(JSON.parse(d).token))"
```
