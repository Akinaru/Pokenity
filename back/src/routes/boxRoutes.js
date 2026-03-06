const { Prisma } = require("@prisma/client");
const express = require("express");
const { prisma } = require("../lib/prisma");
const { PokeApiError, resolveDropResource } = require("../lib/pokeapi");
const { authRequired } = require("../middleware/auth");

const router = express.Router();
const SHINY_CHANCE = 0.25;

function serializeEntry(entry) {
  return {
    id: entry.id,
    resourceType: entry.resourceType,
    resourceId: entry.resourceId,
    resourceName: entry.resourceName,
    dropRate: entry.dropRate,
    createdAt: entry.createdAt,
    updatedAt: entry.updatedAt,
  };
}

function serializeBox(box) {
  const entries = box.entries || [];
  const totalDropRate = entries.reduce((sum, entry) => sum + entry.dropRate, 0);

  return {
    id: box.id,
    name: box.name,
    pokeballImage: box.pokeballImage,
    totalDropRate,
    createdAt: box.createdAt,
    updatedAt: box.updatedAt,
    entries: entries.map(serializeEntry),
  };
}

function toNumber(value) {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : NaN;
}

async function normalizeEntries(rawEntries) {
  if (!Array.isArray(rawEntries) || rawEntries.length === 0) {
    return {
      error: "entries must be a non-empty array.",
    };
  }

  const normalized = [];
  const seen = new Set();
  let total = 0;

  for (const rawEntry of rawEntries) {
    const resourceType = String(rawEntry.resourceType || "").trim().toUpperCase();
    const resourceId = Math.trunc(toNumber(rawEntry.resourceId));
    const dropRate = toNumber(rawEntry.dropRate);

    if (!["POKEMON", "ITEM", "MACHINE"].includes(resourceType)) {
      return {
        error: "Each entry resourceType must be POKEMON, ITEM or MACHINE.",
      };
    }

    if (!Number.isInteger(resourceId) || resourceId <= 0) {
      return {
        error: "Each entry resourceId must be a positive integer.",
      };
    }

    if (!Number.isFinite(dropRate) || dropRate <= 0) {
      return {
        error: "Each entry dropRate must be a positive number.",
      };
    }

    const dedupeKey = `${resourceType}:${resourceId}`;
    if (seen.has(dedupeKey)) {
      return {
        error: "Duplicate entry detected in the same box.",
      };
    }
    seen.add(dedupeKey);

    normalized.push({
      resourceType,
      resourceId,
      dropRate,
    });
    total += dropRate;
  }

  if (Math.abs(total - 100) > 0.001) {
    return {
      error: "The sum of all dropRate values must be exactly 100.",
    };
  }

  const resolvedEntries = [];
  for (const entry of normalized) {
    const resource = await resolveDropResource(entry.resourceType, entry.resourceId);
    resolvedEntries.push({
      ...entry,
      resourceName: resource.resourceName,
    });
  }

  return {
    entries: resolvedEntries,
  };
}

function pickWeightedEntry(entries) {
  const total = entries.reduce((sum, entry) => sum + entry.dropRate, 0);
  let random = Math.random() * total;

  for (const entry of entries) {
    random -= entry.dropRate;
    if (random <= 0) {
      return entry;
    }
  }

  return entries[entries.length - 1];
}

function rollShiny(entry) {
  if (!entry || entry.resourceType !== "POKEMON") {
    return false;
  }
  return Math.random() < SHINY_CHANCE;
}

router.get("/", async (req, res) => {
  const boxes = await prisma.box.findMany({
    include: {
      entries: {
        orderBy: {
          dropRate: "desc",
        },
      },
    },
    orderBy: {
      createdAt: "desc",
    },
  });

  return res.json({ boxes: boxes.map(serializeBox) });
});

router.get("/:boxId", async (req, res) => {
  const box = await prisma.box.findUnique({
    where: { id: req.params.boxId },
    include: {
      entries: {
        orderBy: {
          dropRate: "desc",
        },
      },
    },
  });

  if (!box) {
    return res.status(404).json({ error: "Box not found." });
  }

  return res.json({ box: serializeBox(box) });
});

router.post("/", async (req, res) => {
  const name = String(req.body.name || "").trim();
  const pokeballImage = String(req.body.pokeballImage || "").trim();

  if (!name) {
    return res.status(400).json({ error: "name is required." });
  }

  if (!pokeballImage) {
    return res.status(400).json({ error: "pokeballImage is required." });
  }

  try {
    const normalized = await normalizeEntries(req.body.entries);
    if (normalized.error) {
      return res.status(400).json({ error: normalized.error });
    }

    const box = await prisma.box.create({
      data: {
        name,
        pokeballImage,
        entries: {
          create: normalized.entries,
        },
      },
      include: {
        entries: {
          orderBy: {
            dropRate: "desc",
          },
        },
      },
    });

    return res.status(201).json({ box: serializeBox(box) });
  } catch (error) {
    if (error instanceof PokeApiError) {
      return res.status(error.status).json({ error: error.message });
    }

    if (
      error instanceof Prisma.PrismaClientKnownRequestError &&
      error.code === "P2002"
    ) {
      return res.status(409).json({ error: "Box contains duplicate resources." });
    }

    throw error;
  }
});

router.patch("/:boxId", async (req, res) => {
  const id = String(req.params.boxId || "");
  const patch = {};

  if (req.body.name !== undefined) {
    const name = String(req.body.name || "").trim();
    if (!name) {
      return res.status(400).json({ error: "name cannot be empty." });
    }
    patch.name = name;
  }

  if (req.body.pokeballImage !== undefined) {
    const pokeballImage = String(req.body.pokeballImage || "").trim();
    if (!pokeballImage) {
      return res.status(400).json({ error: "pokeballImage cannot be empty." });
    }
    patch.pokeballImage = pokeballImage;
  }

  try {
    if (req.body.entries !== undefined) {
      const normalized = await normalizeEntries(req.body.entries);
      if (normalized.error) {
        return res.status(400).json({ error: normalized.error });
      }

      await prisma.$transaction([
        prisma.boxEntry.deleteMany({
          where: { boxId: id },
        }),
        prisma.box.update({
          where: { id },
          data: {
            ...patch,
            entries: {
              create: normalized.entries,
            },
          },
        }),
      ]);
    } else {
      if (Object.keys(patch).length === 0) {
        return res.status(400).json({
          error: "Provide at least one field to update: name, pokeballImage, entries.",
        });
      }

      await prisma.box.update({
        where: { id },
        data: patch,
      });
    }

    const box = await prisma.box.findUnique({
      where: { id },
      include: {
        entries: {
          orderBy: { dropRate: "desc" },
        },
      },
    });

    return res.json({ box: serializeBox(box) });
  } catch (error) {
    if (error instanceof PokeApiError) {
      return res.status(error.status).json({ error: error.message });
    }

    if (
      error instanceof Prisma.PrismaClientKnownRequestError &&
      error.code === "P2025"
    ) {
      return res.status(404).json({ error: "Box not found." });
    }

    if (
      error instanceof Prisma.PrismaClientKnownRequestError &&
      error.code === "P2002"
    ) {
      return res.status(409).json({ error: "Box contains duplicate resources." });
    }

    throw error;
  }
});

router.delete("/:boxId", async (req, res) => {
  try {
    await prisma.box.delete({
      where: { id: req.params.boxId },
    });
    return res.status(204).send();
  } catch (error) {
    if (
      error instanceof Prisma.PrismaClientKnownRequestError &&
      error.code === "P2025"
    ) {
      return res.status(404).json({ error: "Box not found." });
    }
    throw error;
  }
});

router.post("/:boxId/open", authRequired, async (req, res) => {
  const user = await prisma.user.findUnique({
    where: { id: req.user.sub },
    select: { id: true },
  });

  if (!user) {
    return res.status(404).json({ error: "User not found." });
  }

  const box = await prisma.box.findUnique({
    where: { id: req.params.boxId },
    include: {
      entries: true,
    },
  });

  if (!box) {
    return res.status(404).json({ error: "Box not found." });
  }

  if (!box.entries.length) {
    return res.status(400).json({ error: "Box has no entries." });
  }

  const selectedEntry = pickWeightedEntry(box.entries);
  const isShiny = rollShiny(selectedEntry);
  const now = new Date();

  const { inventoryItem, boxOpening, userWithXp } = await prisma.$transaction(
    async (tx) => {
      const nextInventoryItem = await tx.inventoryItem.upsert({
        where: {
          userId_resourceType_resourceId_isShiny: {
            userId: req.user.sub,
            resourceType: selectedEntry.resourceType,
            resourceId: selectedEntry.resourceId,
            isShiny,
          },
        },
        update: {
          resourceName: selectedEntry.resourceName,
          quantity: {
            increment: 1,
          },
          lastObtainedAt: now,
        },
        create: {
          userId: req.user.sub,
          resourceType: selectedEntry.resourceType,
          resourceId: selectedEntry.resourceId,
          resourceName: selectedEntry.resourceName,
          isShiny,
          quantity: 1,
          firstObtainedAt: now,
          lastObtainedAt: now,
        },
      });

      const nextBoxOpening = await tx.boxOpening.create({
        data: {
          userId: req.user.sub,
          boxId: box.id,
          boxName: box.name,
          boxPokeballImage: box.pokeballImage,
          resourceType: selectedEntry.resourceType,
          resourceId: selectedEntry.resourceId,
          resourceName: selectedEntry.resourceName,
          isShiny,
          dropRate: selectedEntry.dropRate,
          openedAt: now,
        },
      });

      const nextUser = await tx.user.update({
        where: { id: req.user.sub },
        data: {
          xp: {
            increment: 1,
          },
        },
        select: {
          xp: true,
        },
      });

      return {
        inventoryItem: nextInventoryItem,
        boxOpening: nextBoxOpening,
        userWithXp: nextUser,
      };
    }
  );

  return res.json({
    box: {
      id: box.id,
      name: box.name,
      pokeballImage: box.pokeballImage,
    },
    reward: {
      resourceType: selectedEntry.resourceType,
      resourceId: selectedEntry.resourceId,
      resourceName: selectedEntry.resourceName,
      isShiny,
      dropRate: selectedEntry.dropRate,
    },
    inventoryItem: {
      id: inventoryItem.id,
      isShiny: inventoryItem.isShiny,
      quantity: inventoryItem.quantity,
      lastObtainedAt: inventoryItem.lastObtainedAt,
    },
    boxOpening: {
      id: boxOpening.id,
      isShiny: boxOpening.isShiny,
      openedAt: boxOpening.openedAt,
    },
    user: {
      xp: userWithXp.xp,
    },
  });
});

module.exports = router;
