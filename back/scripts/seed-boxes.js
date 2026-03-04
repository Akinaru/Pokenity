const { prisma } = require("../src/lib/prisma");

const POKEAPI_BASE_URL = process.env.POKEAPI_BASE_URL || "https://pokeapi.co/api/v2";
const ITEM_SPRITE_BASE =
  "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/items";

const BOX_COUNT = 40;
const MAX_POKEMON_ID = 1025;

// Progression interne de chaque box:
// commun fort taux -> peu commun -> rare -> epique -> legendaire
const SLOT_PROFILE = [
  { tier: "common", rate: 26 },
  { tier: "common", rate: 18 },
  { tier: "common", rate: 12 },
  { tier: "uncommon", rate: 10 },
  { tier: "uncommon", rate: 8 },
  { tier: "uncommon", rate: 7 },
  { tier: "rare", rate: 6 },
  { tier: "rare", rate: 5 },
  { tier: "rare", rate: 3 },
  { tier: "epic", rate: 2 },
  { tier: "mythic", rate: 2 },
  { tier: "legend", rate: 1 },
];

const LEGENDARY_OR_MYTHICAL_IDS = new Set([
  144, 145, 146, 150, 151, 243, 244, 245, 249, 250, 251, 377, 378, 379, 380,
  381, 382, 383, 384, 385, 386, 480, 481, 482, 483, 484, 485, 486, 487, 488,
  489, 490, 491, 492, 493, 638, 639, 640, 641, 642, 643, 644, 645, 646, 647,
  648, 649, 716, 717, 718, 719, 720, 721, 785, 786, 787, 788, 789, 790, 791,
  792, 800, 801, 802, 807, 888, 889, 890, 891, 892, 893, 894, 895, 896, 897,
  898, 905, 1001, 1002, 1003, 1004, 1007, 1008, 1014, 1015, 1016, 1017,
]);

const STYLISH_NAMES = [
  "Aube de Kanto",
  "Brume de Jadielle",
  "Eclair Indigo",
  "Couronne Azur",
  "Echo du Marais",
  "Sanctuaire Boreal",
  "Rift Obsidienne",
  "Pulse Rubis",
  "Rivage Saphir",
  "Mirage Emeraude",
  "Crepuscule Johto",
  "Bastion de Sinnoh",
  "Nexus Unys",
  "Rose de Kalos",
  "Halo Alola",
  "Forge de Galar",
  "Granit de Paldea",
  "Tempete Prisme",
  "Comete Stellaire",
  "Citadelle Arcane",
  "Nocturne Celeste",
  "Festival Lumina",
  "Dragonium Prime",
  "Arene Atlas",
  "Symphonie Neon",
  "Paradis Corail",
  "Givre Royal",
  "Lande Volta",
  "Chrono Horizon",
  "Nebuleuse Delta",
  "Astre Velours",
  "Titan Solaris",
  "Lueur Sakura",
  "Frontiere Mirage",
  "Helice Zenith",
  "Jungle Aurora",
  "Hydre Temporelle",
  "Porte Eclipse",
  "Orbite Infinity",
  "Omega Ultime",
];

const BALL_SLUGS = [
  "poke-ball",
  "great-ball",
  "ultra-ball",
  "premier-ball",
  "luxury-ball",
  "dusk-ball",
  "dive-ball",
  "quick-ball",
  "timer-ball",
  "heal-ball",
  "repeat-ball",
  "net-ball",
  "nest-ball",
  "moon-ball",
  "friend-ball",
  "level-ball",
  "love-ball",
  "lure-ball",
  "heavy-ball",
  "dream-ball",
  "beast-ball",
  "cherish-ball",
  "master-ball",
  "sport-ball",
  "safari-ball",
  "fast-ball",
];

function ballImage(ballSlug) {
  return `${ITEM_SPRITE_BASE}/${encodeURIComponent(ballSlug)}.png`;
}

function parseIdFromResourceUrl(url) {
  const match = String(url || "").match(/\/(\d+)\/?$/);
  return match ? Number(match[1]) : null;
}

function shuffle(items) {
  const array = [...items];
  for (let i = array.length - 1; i > 0; i -= 1) {
    const j = Math.floor(Math.random() * (i + 1));
    [array[i], array[j]] = [array[j], array[i]];
  }
  return array;
}

async function withConcurrency(items, concurrency, asyncMapper) {
  const results = new Array(items.length);
  let cursor = 0;

  async function worker() {
    while (true) {
      const current = cursor;
      cursor += 1;

      if (current >= items.length) {
        return;
      }

      results[current] = await asyncMapper(items[current], current);
    }
  }

  await Promise.all(Array.from({ length: Math.max(1, concurrency) }, () => worker()));
  return results;
}

async function fetchPokemonIndex() {
  const response = await fetch(`${POKEAPI_BASE_URL}/pokemon?offset=0&limit=2000`);
  if (!response.ok) {
    throw new Error(`Unable to fetch pokemon index (status ${response.status}).`);
  }

  const payload = await response.json();
  const index = [];

  for (const entry of payload.results || []) {
    const id = parseIdFromResourceUrl(entry.url);
    if (!id || id > MAX_POKEMON_ID) {
      continue;
    }

    index.push({
      id,
      name: entry.name,
    });
  }

  return index.sort((a, b) => a.id - b.id);
}

async function fetchPokemonStats(id) {
  const response = await fetch(`${POKEAPI_BASE_URL}/pokemon/${id}`);
  if (!response.ok) {
    throw new Error(`Unable to fetch pokemon #${id} (status ${response.status}).`);
  }

  const payload = await response.json();
  const bst = (payload.stats || []).reduce((sum, stat) => sum + Number(stat.base_stat || 0), 0);

  return {
    id: payload.id,
    bst,
  };
}

function tierByBst(id, bst) {
  if (LEGENDARY_OR_MYTHICAL_IDS.has(id)) {
    return "legend";
  }

  if (bst <= 390) {
    return "common";
  }
  if (bst <= 470) {
    return "uncommon";
  }
  if (bst <= 540) {
    return "rare";
  }
  if (bst <= 610) {
    return "epic";
  }
  return "mythic";
}

function createPools(index, statsById) {
  const pools = {
    common: [],
    uncommon: [],
    rare: [],
    epic: [],
    mythic: [],
    legend: [],
  };

  for (const pokemon of index) {
    const stats = statsById.get(pokemon.id);
    if (!stats) {
      continue;
    }

    const tier = tierByBst(pokemon.id, stats.bst);
    pools[tier].push({
      id: pokemon.id,
      name: pokemon.name,
      bst: stats.bst,
    });
  }

  for (const tierName of Object.keys(pools)) {
    pools[tierName] = shuffle(
      pools[tierName].sort((a, b) => {
        if (a.bst !== b.bst) {
          return a.bst - b.bst;
        }
        return a.id - b.id;
      })
    );
  }

  return pools;
}

function createPoolCursor(pools) {
  return Object.fromEntries(Object.keys(pools).map((tier) => [tier, 0]));
}

function pickFromPool(tier, pools, poolCursor, usedInBox, usedGlobal) {
  const pool = pools[tier] || [];
  if (!pool.length) {
    return null;
  }

  let attempts = 0;
  while (attempts < pool.length) {
    const idx = poolCursor[tier] % pool.length;
    poolCursor[tier] += 1;
    attempts += 1;

    const candidate = pool[idx];
    if (usedInBox.has(candidate.id)) {
      continue;
    }

    if (!usedGlobal.has(candidate.id)) {
      return candidate;
    }
  }

  // second pass: autorise les doublons globaux si besoin
  attempts = 0;
  while (attempts < pool.length) {
    const idx = poolCursor[tier] % pool.length;
    poolCursor[tier] += 1;
    attempts += 1;

    const candidate = pool[idx];
    if (usedInBox.has(candidate.id)) {
      continue;
    }
    return candidate;
  }

  return null;
}

function pickPokemonForTier(targetTier, pools, poolCursor, usedInBox, usedGlobal) {
  const fallbackOrder = {
    common: ["common", "uncommon", "rare", "epic", "mythic"],
    uncommon: ["uncommon", "common", "rare", "epic", "mythic"],
    rare: ["rare", "uncommon", "epic", "common", "mythic"],
    epic: ["epic", "rare", "mythic", "uncommon", "common"],
    mythic: ["mythic", "epic", "rare", "uncommon", "common"],
    legend: ["legend", "mythic", "epic"],
  };

  for (const tier of fallbackOrder[targetTier] || [targetTier]) {
    const picked = pickFromPool(tier, pools, poolCursor, usedInBox, usedGlobal);
    if (picked) {
      return {
        ...picked,
        pickedTier: tier,
      };
    }
  }

  return null;
}

function buildBoxEntries(pools, poolCursor, usedGlobal) {
  const usedInBox = new Set();
  const entries = [];

  for (const slot of SLOT_PROFILE) {
    const picked = pickPokemonForTier(slot.tier, pools, poolCursor, usedInBox, usedGlobal);
    if (!picked) {
      throw new Error(`Not enough pokemon to fill slot tier '${slot.tier}'.`);
    }

    usedInBox.add(picked.id);
    usedGlobal.add(picked.id);

    entries.push({
      resourceType: "POKEMON",
      resourceId: picked.id,
      resourceName: picked.name,
      dropRate: slot.rate,
      _tier: picked.pickedTier,
      _bst: picked.bst,
    });
  }

  // sécurité: au plus 2 légendaires dans une box, et seulement en slots bas taux
  const legendEntries = entries.filter((entry) =>
    LEGENDARY_OR_MYTHICAL_IDS.has(entry.resourceId)
  );
  if (legendEntries.length > 2) {
    throw new Error("Generated box has too many legendary/mythical entries.");
  }

  const legendTooCommon = legendEntries.some((entry) => entry.dropRate > 2);
  if (legendTooCommon) {
    throw new Error("Generated box has a legendary/mythical with dropRate > 2.");
  }

  const total = entries.reduce((sum, entry) => sum + entry.dropRate, 0);
  if (Math.abs(total - 100) > 0.0001) {
    throw new Error(`Invalid generated drop total: ${total}`);
  }

  return entries.map(({ _tier, _bst, ...entry }) => entry);
}

async function resetAndSeedBoxes(boxPayloads) {
  await prisma.$transaction([
    prisma.boxEntry.deleteMany({}),
    prisma.box.deleteMany({}),
  ]);

  for (const payload of boxPayloads) {
    await prisma.box.create({
      data: payload,
    });
  }
}

async function main() {
  if (STYLISH_NAMES.length !== BOX_COUNT) {
    throw new Error(`Need exactly ${BOX_COUNT} stylish names.`);
  }

  const pokemonIndex = await fetchPokemonIndex();
  const ids = pokemonIndex.map((pokemon) => pokemon.id);

  const stats = await withConcurrency(ids, 18, async (id) => fetchPokemonStats(id));
  const statsById = new Map(stats.map((entry) => [entry.id, entry]));

  const pools = createPools(pokemonIndex, statsById);
  const poolCursor = createPoolCursor(pools);
  const usedGlobal = new Set();

  const boxPayloads = [];

  for (let i = 0; i < BOX_COUNT; i += 1) {
    const entries = buildBoxEntries(pools, poolCursor, usedGlobal);
    const boxName = STYLISH_NAMES[i];
    const ballSlug = BALL_SLUGS[i % BALL_SLUGS.length];

    boxPayloads.push({
      name: boxName,
      pokeballImage: ballImage(ballSlug),
      entries: {
        create: entries,
      },
    });
  }

  await resetAndSeedBoxes(boxPayloads);

  const uniquePokemonCount = usedGlobal.size;
  console.log(
    `Seed complete. Boxes: ${BOX_COUNT}, Entries/box: ${SLOT_PROFILE.length}, Unique pokemon used: ${uniquePokemonCount}`
  );
}

main()
  .catch((error) => {
    console.error(error);
    process.exitCode = 1;
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
