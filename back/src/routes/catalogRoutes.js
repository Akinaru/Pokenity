const express = require("express");
const { POKEAPI_BASE_URL } = require("../config/env");
const { PokeApiError } = require("../lib/pokeapi");

const router = express.Router();

let pokemonCatalogCache = null;
let itemCatalogCache = null;
let pokeballCatalogCache = null;

function parseIdFromResourceUrl(url) {
  const match = String(url || "").match(/\/(\d+)\/?$/);
  if (!match) {
    return null;
  }
  return Number(match[1]);
}

function normalizeLimit(value, fallback = 20, max = 100) {
  const parsed = Number(value);
  if (!Number.isFinite(parsed) || parsed <= 0) {
    return fallback;
  }
  return Math.min(Math.trunc(parsed), max);
}

function itemSpriteUrl(name) {
  return `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/items/${encodeURIComponent(
    name
  )}.png`;
}

async function fetchResourceList(path, limit = 3000) {
  let response;

  try {
    response = await fetch(`${POKEAPI_BASE_URL}${path}?offset=0&limit=${limit}`);
  } catch {
    throw new PokeApiError("PokeAPI is unreachable right now.", 502);
  }

  if (!response.ok) {
    throw new PokeApiError("Failed to fetch resource list from PokeAPI.", 502);
  }

  const data = await response.json();
  return Array.isArray(data.results) ? data.results : [];
}

async function ensurePokemonCatalog() {
  if (pokemonCatalogCache) {
    return pokemonCatalogCache;
  }

  const list = await fetchResourceList("/pokemon", 2000);
  pokemonCatalogCache = list
    .map((entry) => {
      const id = parseIdFromResourceUrl(entry.url);
      if (!id) {
        return null;
      }
      return {
        id,
        name: entry.name,
        image: `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${id}.png`,
      };
    })
    .filter(Boolean)
    .sort((a, b) => a.id - b.id);

  return pokemonCatalogCache;
}

async function ensureItemCatalog() {
  if (itemCatalogCache) {
    return itemCatalogCache;
  }

  const list = await fetchResourceList("/item", 2500);
  itemCatalogCache = list
    .map((entry) => {
      const id = parseIdFromResourceUrl(entry.url);
      if (!id) {
        return null;
      }
      return {
        id,
        name: entry.name,
        image: itemSpriteUrl(entry.name),
      };
    })
    .filter(Boolean)
    .sort((a, b) => a.id - b.id);

  return itemCatalogCache;
}

async function ensurePokeballCatalog() {
  if (pokeballCatalogCache) {
    return pokeballCatalogCache;
  }

  let category = await fetch(`${POKEAPI_BASE_URL}/item-category/poke-balls`)
    .then((response) => (response.ok ? response.json() : null))
    .catch(() => null);

  if (!category) {
    category = await fetch(`${POKEAPI_BASE_URL}/item-category/pokeballs`)
      .then((response) => (response.ok ? response.json() : null))
      .catch(() => null);
  }

  if (category && Array.isArray(category.items)) {
    pokeballCatalogCache = category.items
      .map((entry) => {
        const id = parseIdFromResourceUrl(entry.url);
        if (!id) {
          return null;
        }
        return {
          id,
          name: entry.name,
          image: itemSpriteUrl(entry.name),
        };
      })
      .filter(Boolean)
      .sort((a, b) => a.id - b.id);

    return pokeballCatalogCache;
  }

  const itemCatalog = await ensureItemCatalog();
  pokeballCatalogCache = itemCatalog.filter((item) => item.name.includes("ball"));
  return pokeballCatalogCache;
}

function searchCatalog(catalog, search, limit) {
  const query = String(search || "").trim().toLowerCase();

  if (!query) {
    return catalog.slice(0, limit);
  }

  return catalog
    .filter((entry) => {
      return (
        entry.name.includes(query) ||
        String(entry.id) === query ||
        String(entry.id).startsWith(query)
      );
    })
    .slice(0, limit);
}

router.get("/pokemon", async (req, res) => {
  const limit = normalizeLimit(req.query.limit, 24, 100);
  const catalog = await ensurePokemonCatalog();
  const results = searchCatalog(catalog, req.query.search, limit);
  return res.json({ results });
});

router.get("/items", async (req, res) => {
  const limit = normalizeLimit(req.query.limit, 24, 100);
  const catalog = await ensureItemCatalog();
  const results = searchCatalog(catalog, req.query.search, limit);
  return res.json({ results });
});

router.get("/pokeballs", async (req, res) => {
  const limit = normalizeLimit(req.query.limit, 40, 120);
  const catalog = await ensurePokeballCatalog();
  const results = searchCatalog(catalog, req.query.search, limit);
  return res.json({ results });
});

module.exports = router;
