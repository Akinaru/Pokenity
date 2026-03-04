const { Prisma } = require("@prisma/client");
const express = require("express");
const { authRequired } = require("../middleware/auth");
const { createPasswordHash, verifyPassword } = require("../lib/password");
const { prisma } = require("../lib/prisma");
const { signToken } = require("../lib/token");
const {
  createUser,
  findUserByEmail,
  findUserById,
  findUserByUsername,
} = require("../repositories/userRepository");

const router = express.Router();

function getRequestOrigin(req) {
  const forwardedProto = String(req.headers["x-forwarded-proto"] || "")
    .split(",")[0]
    .trim();
  const forwardedHost = String(req.headers["x-forwarded-host"] || "")
    .split(",")[0]
    .trim();
  const protocol = forwardedProto || req.protocol;
  const host = forwardedHost || req.get("host");
  if (!protocol || !host) {
    return "";
  }
  return `${protocol}://${host}`;
}

function normalizeMediaUrl(req, rawUrl) {
  const value = String(rawUrl || "").trim();
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
  if (value.startsWith("http://") || value.startsWith("https://")) {
    return value;
  }
  if (!value.includes("/")) {
    return value;
  }

  const origin = getRequestOrigin(req);
  if (!origin) {
    return value;
  }

  if (value.startsWith("/")) {
    return `${origin}${value}`;
  }

  return `${origin}/${value}`;
}

function cleanUser(req, user) {
  const characterAvatar = user.character
    ? normalizeMediaUrl(req, user.character.avatarUrl)
    : "";
  const characterImage = user.character
    ? normalizeMediaUrl(req, user.character.imageUrl)
    : "";

  return {
    id: user.id,
    username: user.username,
    email: user.email,
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
  };
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

router.post("/register", async (req, res) => {
  const username = String(req.body.username || "").trim().toLowerCase();
  const email = String(req.body.email || "").trim().toLowerCase();
  const password = String(req.body.password || "");

  if (!username || !email || !password) {
    return res.status(400).json({
      error: "username, email and password are required.",
    });
  }

  if (password.length < 6) {
    return res.status(400).json({
      error: "password must be at least 6 characters long.",
    });
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) {
    return res.status(400).json({
      error: "email format is invalid.",
    });
  }

  const existingEmail = await findUserByEmail(email);
  if (existingEmail) {
    return res.status(409).json({ error: "email is already used." });
  }

  const existingUsername = await findUserByUsername(username);
  if (existingUsername) {
    return res.status(409).json({ error: "username is already used." });
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

  let user;
  try {
    user = await createUser({
      username,
      email,
      passwordHash: await createPasswordHash(password),
      characterId: resolvedCharacter.characterId,
    });
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

  const token = signToken({
    sub: user.id,
    email: user.email,
    username: user.username,
  });

  return res.status(201).json({
    token,
    user: cleanUser(req, user),
  });
});

router.post("/login", async (req, res) => {
  const identifier = String(req.body.identifier || req.body.email || "").trim();
  const password = String(req.body.password || "");

  if (!identifier || !password) {
    return res.status(400).json({
      error: "identifier/email and password are required.",
    });
  }

  const user =
    (await findUserByEmail(identifier.toLowerCase())) ||
    (await findUserByUsername(identifier.toLowerCase()));

  if (!user || !(await verifyPassword(password, user.passwordHash))) {
    return res.status(401).json({ error: "Invalid credentials." });
  }

  const token = signToken({
    sub: user.id,
    email: user.email,
    username: user.username,
  });

  return res.json({
    token,
    user: cleanUser(req, user),
  });
});

router.get("/me", authRequired, async (req, res) => {
  const user = await findUserById(req.user.sub);
  if (!user) {
    return res.status(404).json({ error: "User not found." });
  }

  return res.json({
    user: cleanUser(req, user),
  });
});

module.exports = router;
