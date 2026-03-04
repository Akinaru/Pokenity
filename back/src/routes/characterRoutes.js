const { Prisma } = require("@prisma/client");
const express = require("express");
const { prisma } = require("../lib/prisma");

const router = express.Router();

const ALLOWED_IMAGE_EXTENSIONS = new Set([
  ".png",
  ".jpg",
  ".jpeg",
  ".webp",
  ".gif",
  ".svg",
]);

function cleanFileName(raw) {
  const value = String(raw || "").trim();
  if (!value) {
    return "";
  }

  const slashNormalized = value.replaceAll("\\", "/");
  const fromPath = slashNormalized.split("/").pop() || "";
  return fromPath.trim();
}

function validateImageFileName(rawValue, fieldLabel) {
  const fileName = cleanFileName(rawValue);
  if (!fileName) {
    return { error: `${fieldLabel} is required.` };
  }

  if (!/^[A-Za-z0-9._-]+$/.test(fileName)) {
    return {
      error: `${fieldLabel} must only contain letters, digits, dot, underscore or dash.`,
    };
  }

  const lower = fileName.toLowerCase();
  const dot = lower.lastIndexOf(".");
  const ext = dot === -1 ? "" : lower.slice(dot);
  if (!ALLOWED_IMAGE_EXTENSIONS.has(ext)) {
    return {
      error: `${fieldLabel} must use one of: ${Array.from(ALLOWED_IMAGE_EXTENSIONS).join(", ")}.`,
    };
  }

  return { value: fileName };
}

function readAvatarFileName(body = {}) {
  return body.avatarFileName ?? body.avatarUrl ?? body.avatar;
}

function readImageFileName(body = {}) {
  return body.imageFileName ?? body.imageUrl ?? body.image;
}

function serializeCharacter(character) {
  const avatarFileName = cleanFileName(character.avatarUrl);
  const imageFileName = cleanFileName(character.imageUrl);
  return {
    id: character.id,
    name: character.name,
    avatarUrl: avatarFileName,
    imageUrl: imageFileName,
    avatarFileName,
    imageFileName,
    createdAt: character.createdAt,
    updatedAt: character.updatedAt,
  };
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
  const name = String(req.body.name || "").trim();
  if (!name) {
    return res.status(400).json({ error: "name is required." });
  }

  const avatarResult = validateImageFileName(
    readAvatarFileName(req.body),
    "avatarFileName"
  );
  if (avatarResult.error) {
    return res.status(400).json({ error: avatarResult.error });
  }

  const imageResult = validateImageFileName(
    readImageFileName(req.body),
    "imageFileName"
  );
  if (imageResult.error) {
    return res.status(400).json({ error: imageResult.error });
  }

  const character = await prisma.character.create({
    data: {
      name,
      avatarUrl: avatarResult.value,
      imageUrl: imageResult.value,
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

  const nextData = {};

  if (req.body.name !== undefined) {
    const name = String(req.body.name || "").trim();
    if (!name) {
      return res.status(400).json({ error: "name cannot be empty." });
    }
    nextData.name = name;
  }

  if (
    req.body.avatarFileName !== undefined ||
    req.body.avatarUrl !== undefined ||
    req.body.avatar !== undefined
  ) {
    const avatarResult = validateImageFileName(
      readAvatarFileName(req.body),
      "avatarFileName"
    );
    if (avatarResult.error) {
      return res.status(400).json({ error: avatarResult.error });
    }
    nextData.avatarUrl = avatarResult.value;
  }

  if (
    req.body.imageFileName !== undefined ||
    req.body.imageUrl !== undefined ||
    req.body.image !== undefined
  ) {
    const imageResult = validateImageFileName(
      readImageFileName(req.body),
      "imageFileName"
    );
    if (imageResult.error) {
      return res.status(400).json({ error: imageResult.error });
    }
    nextData.imageUrl = imageResult.value;
  }

  if (Object.keys(nextData).length === 0) {
    return res.status(400).json({
      error: "Provide at least one field to update: name, avatarFileName, imageFileName.",
    });
  }

  try {
    const character = await prisma.character.update({
      where: { id },
      data: nextData,
    });

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
  return res.status(204).send();
});

module.exports = router;
