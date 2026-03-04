# Pokenity API (Express + MySQL + Prisma)

API backend pour l'app Android Pokenity.

Fonctionnalites actuelles:
- Auth (register/login/me)
- Proxy Pokemon vers PokeAPI
- Inventaire par utilisateur (Pokemon + Items)
- Systeme de box (pokeball + drops avec pourcentage)
- Ouverture de box (tirage aleatoire pondere)
- Back-office public multi-pages `/admin` (dashboard, users, boxes)
- Catalogue visuel (pokeballs, recherche pokemon/items)

## Stack

- Node.js 22+
- Express 5
- Prisma + MySQL
- JWT (`jsonwebtoken`)
- Hash mot de passe (`bcryptjs`)

## Configuration

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

Creer la base si besoin:

```bash
docker exec db_docker_pokenity mariadb -uroot -e "CREATE DATABASE IF NOT EXISTS pokenity CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

Appliquer les migrations:

```bash
npm run prisma:deploy
```

En dev, pour creer de nouvelles migrations:

```bash
npm run prisma:migrate -- --name your_migration_name
```

Regenerer le client Prisma:

```bash
npm run prisma:generate
```

Seeder des boxes preconfigurees (40 boxes, 12 pokemons par box, max 1 legendaire a 1% par box):

```bash
npm run seed:boxes
```

## Lancer l'API

```bash
npm run dev
```

- API: `http://localhost:3000`
- Back-office: `http://localhost:3000/admin/`
- Pages BO:
  - `http://localhost:3000/admin/` (dashboard)
  - `http://localhost:3000/admin/users.html`
  - `http://localhost:3000/admin/boxes.html`

## Entites Prisma

Schema: [schema.prisma](/Users/maximegallotta/Documents/Developpement/Pokenity/back/prisma/schema.prisma)

### `User` (`users`)
- `id`, `username`, `email`, `passwordHash`, `createdAt`, `updatedAt`

### `Box` (`boxes`)
- `id`, `name`, `pokeballImage`, `createdAt`, `updatedAt`

### `BoxEntry` (`box_entries`)
- `id`, `boxId`, `resourceType` (`POKEMON`, `ITEM` ou `MACHINE`), `resourceId`, `resourceName`, `dropRate`
- contrainte: somme des `dropRate` d'une box = `100`

### `InventoryItem` (`inventory_items`)
- `id`, `userId`, `resourceType`, `resourceId`, `resourceName`, `quantity`
- `firstObtainedAt`, `lastObtainedAt`
- unique: `(userId, resourceType, resourceId)`

## Documentation Endpoints

Base URL: `http://localhost:3000/api`

### 1) Healthcheck

`GET /health`

### 2) Auth

`POST /auth/register`

Body:

```json
{
  "username": "ash",
  "email": "ash@kanto.com",
  "password": "pikachu123"
}
```

`POST /auth/login`

Body:

```json
{
  "identifier": "ash@kanto.com",
  "password": "pikachu123"
}
```

`GET /auth/me` (Bearer token requis)

Header:

```http
Authorization: Bearer <token>
```

### 3) Pokemon (PokeAPI proxy)

`GET /pokemon/:name` (Bearer token requis)

Exemple:

```http
GET /pokemon/pikachu
```

### 4) Users CRUD (public, pour back-office)

`GET /users`

`GET /users/:id`

`POST /users`

Body:

```json
{
  "username": "misty",
  "email": "misty@kanto.com",
  "password": "togepi123"
}
```

`PATCH /users/:id`

Body (au moins un champ):

```json
{
  "username": "misty-new",
  "email": "misty-new@kanto.com",
  "password": "newpass123"
}
```

`DELETE /users/:id`

### 5) Boxes CRUD

`GET /boxes`

`GET /boxes/:boxId`

`POST /boxes`

Body:

```json
{
  "name": "kanto-box",
  "pokeballImage": "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/items/poke-ball.png",
  "entries": [
    { "resourceType": "POKEMON", "resourceId": 25, "dropRate": 70 },
    { "resourceType": "ITEM", "resourceId": 4, "dropRate": 30 }
  ]
}
```

Regles:
- `resourceType` doit etre `POKEMON`, `ITEM` ou `MACHINE`
- `resourceId` doit exister sur PokeAPI
- somme des `dropRate` doit etre exactement `100`

`PATCH /boxes/:boxId`

Body (un ou plusieurs champs):

```json
{
  "name": "kanto-box-v2",
  "pokeballImage": "https://...",
  "entries": [
    { "resourceType": "POKEMON", "resourceId": 1, "dropRate": 50 },
    { "resourceType": "ITEM", "resourceId": 5, "dropRate": 50 }
  ]
}
```

`DELETE /boxes/:boxId`

### 6) Ouvrir une box

`POST /boxes/:boxId/open` (Bearer token requis)

Effet:
- tire 1 drop selon `dropRate`
- ajoute/incremente l'item dans l'inventaire du user connecte

Exemple reponse:

```json
{
  "box": {
    "id": "uuid",
    "name": "kanto-box",
    "pokeballImage": "https://..."
  },
  "reward": {
    "resourceType": "POKEMON",
    "resourceId": 25,
    "resourceName": "pikachu",
    "dropRate": 70
  },
  "inventoryItem": {
    "id": "uuid",
    "quantity": 3,
    "lastObtainedAt": "2026-03-04T10:00:00.000Z"
  }
}
```

### 7) Inventaires

`GET /inventory/me` (Bearer token requis)

`GET /inventory/users/:userId` (public pour back-office)

### 8) Catalogue visuel (pour BO)

`GET /catalog/pokeballs?search=ultra&limit=48`

`GET /catalog/pokemon?search=pika&limit=12`

`GET /catalog/items?search=potion&limit=12`

## Commandes utiles

Recuperer un JWT:

```bash
curl -sS -X POST http://127.0.0.1:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"ash@kanto.com","password":"pikachu123"}' \
| node -e "let d='';process.stdin.on('data',c=>d+=c).on('end',()=>console.log(JSON.parse(d).token))"
```

Ouvrir une box en CLI:

```bash
curl -sS -X POST http://127.0.0.1:3000/api/boxes/<box_id>/open \
  -H "Authorization: Bearer <token>"
```
