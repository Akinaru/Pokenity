const path = require("path");
const { Prisma } = require("@prisma/client");
const express = require("express");
const { prisma } = require("../lib/prisma");
const {
  parseMultipartFormData,
  saveImageFile,
  safeDeleteFile,
} = require("../lib/multipart");

const router = express.Router();
const uploadsDir = path.join(__dirname, "..", "uploads", "characters");

function serializeCharacter(character) {
  return {
    id: character.id,
    name: character.name,
    avatarUrl: character.avatarUrl,
    imageUrl: character.imageUrl,
    createdAt: character.createdAt,
    updatedAt: character.updatedAt,
  };
}

function toAbsoluteFromPublicUrl(publicUrl) {
  if (!publicUrl || !publicUrl.startsWith("/uploads/characters/")) {
    return null;
  }
  const filename = publicUrl.replace("/uploads/characters/", "");
  return path.join(uploadsDir, filename);
}

router.get("/", async (_req, res) => {
  const characters = await prisma.character.findMany({
    orderBy: { createdAt: "desc" },
  });
  return res.json({ characters: characters.map(serializeCharacter) });
});

router.get("/:id", async (req, res) => {
  const character = await prisma.character.findUnique({
    where: { id: String(req.params.id || "") },
  });

  if (!character) {
    return res.status(404).json({ error: "Character not found." });
  }

  return res.json({ character: serializeCharacter(character) });
});

router.post("/", async (req, res) => {
  const parsed = await parseMultipartFormData(req);
  if (parsed.error) {
    return res.status(parsed.status || 400).json({ error: parsed.error });
  }

  const { fields, files } = parsed;
  const name = String(fields.name || "").trim();
  if (!name) {
    return res.status(400).json({ error: "name is required." });
  }

  const avatarSaved = await saveImageFile(files.avatar, uploadsDir, "avatar");
  if (avatarSaved.error) {
    return res.status(avatarSaved.status || 400).json({ error: avatarSaved.error });
  }

  const imageSaved = await saveImageFile(files.image, uploadsDir, "image");
  if (imageSaved.error) {
    await safeDeleteFile(path.join(uploadsDir, avatarSaved.filename));
    return res.status(imageSaved.status || 400).json({ error: imageSaved.error });
  }

  const avatarUrl = `/uploads/characters/${avatarSaved.filename}`;
  const imageUrl = `/uploads/characters/${imageSaved.filename}`;

  const character = await prisma.character.create({
    data: {
      name,
      avatarUrl,
      imageUrl,
    },
  });

  return res.status(201).json({ character: serializeCharacter(character) });
});

router.patch("/:id", async (req, res) => {
  const id = String(req.params.id || "");
  const existing = await prisma.character.findUnique({ where: { id } });
  if (!existing) {
    return res.status(404).json({ error: "Character not found." });
  }

  const parsed = await parseMultipartFormData(req);
  if (parsed.error) {
    return res.status(parsed.status || 400).json({ error: parsed.error });
  }
  const { fields, files } = parsed;

  const nextData = {};
  if (fields.name !== undefined) {
    const name = String(fields.name || "").trim();
    if (!name) {
      return res.status(400).json({ error: "name cannot be empty." });
    }
    nextData.name = name;
  }

  let avatarToDelete = null;
  let imageToDelete = null;

  if (files.avatar) {
    const saved = await saveImageFile(files.avatar, uploadsDir, "avatar");
    if (saved.error) {
      return res.status(saved.status || 400).json({ error: saved.error });
    }
    nextData.avatarUrl = `/uploads/characters/${saved.filename}`;
    avatarToDelete = toAbsoluteFromPublicUrl(existing.avatarUrl);
  }

  if (files.image) {
    const saved = await saveImageFile(files.image, uploadsDir, "image");
    if (saved.error) {
      if (nextData.avatarUrl) {
        await safeDeleteFile(path.join(uploadsDir, nextData.avatarUrl.split("/").pop()));
      }
      return res.status(saved.status || 400).json({ error: saved.error });
    }
    nextData.imageUrl = `/uploads/characters/${saved.filename}`;
    imageToDelete = toAbsoluteFromPublicUrl(existing.imageUrl);
  }

  if (Object.keys(nextData).length === 0) {
    return res.status(400).json({
      error: "Provide at least one field to update: name, avatar, image.",
    });
  }

  try {
    const character = await prisma.character.update({
      where: { id },
      data: nextData,
    });

    if (avatarToDelete) await safeDeleteFile(avatarToDelete);
    if (imageToDelete) await safeDeleteFile(imageToDelete);

    return res.json({ character: serializeCharacter(character) });
  } catch (error) {
    if (
      error instanceof Prisma.PrismaClientKnownRequestError &&
      error.code === "P2025"
    ) {
      return res.status(404).json({ error: "Character not found." });
    }
    throw error;
  }
});

router.delete("/:id", async (req, res) => {
  const id = String(req.params.id || "");
  const existing = await prisma.character.findUnique({ where: { id } });
  if (!existing) {
    return res.status(404).json({ error: "Character not found." });
  }

  await prisma.character.delete({ where: { id } });
  await safeDeleteFile(toAbsoluteFromPublicUrl(existing.avatarUrl));
  await safeDeleteFile(toAbsoluteFromPublicUrl(existing.imageUrl));

  return res.status(204).send();
});

module.exports = router;

