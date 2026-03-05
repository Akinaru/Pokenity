const { Prisma } = require("@prisma/client");
const express = require("express");
const { createPasswordHash } = require("../lib/password");
const { PokeApiError, resolveDropResource } = require("../lib/pokeapi");
const { prisma } = require("../lib/prisma");
const {
  createUser,
  deleteUser,
  findUserById,
  listUsers,
  updateUser,
} = require("../repositories/userRepository");

const router = express.Router();

function normalizeCharacterMediaValue(rawValue) {
  const value = String(rawValue || "").trim();
  if (!value) {
    return "";
  }
  const slashNormalized = value.replaceAll("\\", "/");
  if (slashNormalized.includes("/uploads/characters/")) {
    return slashNormalized.split("/").pop() || "";
  }
  if (slashNormalized.startsWith("uploads/characters/")) {
    return slashNormalized.split("/").pop() || "";
  }
  return value;
}

function cleanUser(user) {
  const characterAvatar = user.character
    ? normalizeCharacterMediaValue(user.character.avatarUrl)
    : "";
  const characterImage = user.character
    ? normalizeCharacterMediaValue(user.character.imageUrl)
    : "";

  return {
    id: user.id,
    username: user.username,
    email: user.email,
    xp: user.xp,
    characterId: user.characterId ?? null,
    character: user.character
      ? {
          id: user.character.id,
          name: user.character.name,
          avatarUrl: characterAvatar,
          imageUrl: characterImage,
          avatarFileName: characterAvatar,
          imageFileName: characterImage,
        }
      : null,
    createdAt: user.createdAt,
    updatedAt: user.updatedAt,
  };
}

function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

async function ensureCharacterIdOrDefault(rawCharacterId) {
  const normalized =
    rawCharacterId === undefined || rawCharacterId === null
      ? ""
      : String(rawCharacterId).trim();

  if (normalized) {
    const character = await prisma.character.findUnique({
      where: { id: normalized },
      select: { id: true },
    });
    if (!character) {
      return { error: "character_not_found" };
    }
    return { characterId: character.id };
  }

  const fallbackCharacter = await prisma.character.findFirst({
    orderBy: { createdAt: "asc" },
    select: { id: true },
  });

  if (!fallbackCharacter) {
    return { error: "no_character_available" };
  }

  return { characterId: fallbackCharacter.id };
}

router.get("/", async (req, res) => {
  const users = await listUsers();
  return res.json({ users: users.map(cleanUser) });
});

router.get("/:id", async (req, res) => {
  const user = await findUserById(req.params.id);
  if (!user) {
    return res.status(404).json({ error: "User not found." });
  }
  return res.json({ user: cleanUser(user) });
});

router.post("/", async (req, res) => {
  const username = String(req.body.username || "").trim().toLowerCase();
  const email = String(req.body.email || "").trim().toLowerCase();
  const password = String(req.body.password || "");
  const hasCustomXp = req.body.xp !== undefined;
  const parsedXp = Number(req.body.xp);

  if (!username || !email || !password) {
    return res.status(400).json({
      error: "username, email and password are required.",
    });
  }

  if (!isValidEmail(email)) {
    return res.status(400).json({ error: "email format is invalid." });
  }

  if (password.length < 6) {
    return res
      .status(400)
      .json({ error: "password must be at least 6 characters long." });
  }

  if (hasCustomXp && (!Number.isInteger(parsedXp) || parsedXp < 0)) {
    return res
      .status(400)
      .json({ error: "xp must be an integer greater than or equal to 0." });
  }

  const resolvedCharacter = await ensureCharacterIdOrDefault(req.body.characterId);
  if (resolvedCharacter.error === "character_not_found") {
    return res.status(404).json({ error: "character not found." });
  }

  if (resolvedCharacter.error === "no_character_available") {
    return res.status(400).json({
      error: "No character available. Create at least one character first.",
    });
  }

  try {
    const user = await createUser({
      username,
      email,
      passwordHash: await createPasswordHash(password),
      characterId: resolvedCharacter.characterId,
      ...(hasCustomXp ? { xp: parsedXp } : {}),
    });
    return res.status(201).json({ user: cleanUser(user) });
  } catch (error) {
    if (
      error instanceof Prisma.PrismaClientKnownRequestError &&
      error.code === "P2002"
    ) {
      return res.status(409).json({ error: "email or username is already used." });
    }
    if (
      error instanceof Prisma.PrismaClientKnownRequestError &&
      error.code === "P2003"
    ) {
      return res.status(400).json({ error: "invalid character id." });
    }
    throw error;
  }
});

router.patch("/:id", async (req, res) => {
  const id = String(req.params.id || "");
  const patch = {};

  if (req.body.username !== undefined) {
    const username = String(req.body.username || "").trim().toLowerCase();
    if (!username) {
      return res.status(400).json({ error: "username cannot be empty." });
    }
    patch.username = username;
  }

  if (req.body.email !== undefined) {
    const email = String(req.body.email || "").trim().toLowerCase();
    if (!isValidEmail(email)) {
      return res.status(400).json({ error: "email format is invalid." });
    }
    patch.email = email;
  }

  if (req.body.password !== undefined) {
    const password = String(req.body.password || "");
    if (password.length < 6) {
      return res
        .status(400)
        .json({ error: "password must be at least 6 characters long." });
    }
    patch.passwordHash = await createPasswordHash(password);
  }

  if (req.body.characterId !== undefined) {
    const characterId = String(req.body.characterId || "").trim();
    if (!characterId) {
      return res.status(400).json({ error: "characterId cannot be empty." });
    }

    const character = await prisma.character.findUnique({
      where: { id: characterId },
      select: { id: true },
    });

    if (!character) {
      return res.status(404).json({ error: "character not found." });
    }

    patch.characterId = character.id;
  }

  if (req.body.xp !== undefined) {
    const xp = Number(req.body.xp);
    if (!Number.isInteger(xp) || xp < 0) {
      return res
        .status(400)
        .json({ error: "xp must be an integer greater than or equal to 0." });
    }
    patch.xp = xp;
  }

  if (Object.keys(patch).length === 0) {
    return res.status(400).json({
      error:
        "Provide at least one field to update: username, email, password, characterId, xp.",
    });
  }

  try {
    const user = await updateUser(id, patch);
    return res.json({ user: cleanUser(user) });
  } catch (error) {
    if (
      error instanceof Prisma.PrismaClientKnownRequestError &&
      error.code === "P2025"
    ) {
      return res.status(404).json({ error: "User not found." });
    }

    if (
      error instanceof Prisma.PrismaClientKnownRequestError &&
      error.code === "P2002"
    ) {
      return res.status(409).json({ error: "email or username is already used." });
    }

    if (
      error instanceof Prisma.PrismaClientKnownRequestError &&
      error.code === "P2003"
    ) {
      return res.status(400).json({ error: "invalid character id." });
    }

    throw error;
  }
});

router.post("/:id/pokemon", async (req, res) => {
  const userId = String(req.params.id || "").trim();
  const pokemonId = Math.trunc(Number(req.body.pokemonId));
  const quantityRaw = req.body.quantity === undefined ? 1 : Number(req.body.quantity);
  const quantity = Math.trunc(quantityRaw);

  if (!userId) {
    return res.status(400).json({ error: "User id is required." });
  }

  if (!Number.isInteger(pokemonId) || pokemonId <= 0) {
    return res
      .status(400)
      .json({ error: "pokemonId must be a positive integer." });
  }

  if (!Number.isInteger(quantity) || quantity <= 0) {
    return res
      .status(400)
      .json({ error: "quantity must be a positive integer." });
  }

  const user = await findUserById(userId);
  if (!user) {
    return res.status(404).json({ error: "User not found." });
  }

  try {
    const pokemon = await resolveDropResource("POKEMON", pokemonId);
    const now = new Date();

    const inventoryItem = await prisma.inventoryItem.upsert({
      where: {
        userId_resourceType_resourceId: {
          userId,
          resourceType: "POKEMON",
          resourceId: pokemon.resourceId,
        },
      },
      update: {
        resourceName: pokemon.resourceName,
        quantity: {
          increment: quantity,
        },
        lastObtainedAt: now,
      },
      create: {
        userId,
        resourceType: "POKEMON",
        resourceId: pokemon.resourceId,
        resourceName: pokemon.resourceName,
        quantity,
        firstObtainedAt: now,
        lastObtainedAt: now,
      },
    });

    return res.status(201).json({
      user: cleanUser(user),
      added: {
        resourceType: "POKEMON",
        resourceId: pokemon.resourceId,
        resourceName: pokemon.resourceName,
        quantityAdded: quantity,
      },
      inventoryItem: {
        id: inventoryItem.id,
        quantity: inventoryItem.quantity,
        lastObtainedAt: inventoryItem.lastObtainedAt,
      },
    });
  } catch (error) {
    if (error instanceof PokeApiError) {
      return res.status(error.status).json({ error: error.message });
    }
    throw error;
  }
});

router.delete("/:id", async (req, res) => {
  const id = String(req.params.id || "");

  try {
    await deleteUser(id);
    return res.status(204).send();
  } catch (error) {
    if (
      error instanceof Prisma.PrismaClientKnownRequestError &&
      error.code === "P2025"
    ) {
      return res.status(404).json({ error: "User not found." });
    }
    throw error;
  }
});

module.exports = router;
