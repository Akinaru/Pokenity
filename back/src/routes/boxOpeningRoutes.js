const express = require("express");
const { prisma } = require("../lib/prisma");
const { authRequired } = require("../middleware/auth");

const router = express.Router();

function normalizeLimit(value, fallback = 30, max = 200) {
  const parsed = Number(value);
  if (!Number.isFinite(parsed) || parsed <= 0) {
    return fallback;
  }
  return Math.min(Math.trunc(parsed), max);
}

function serializeOpening(opening) {
  return {
    id: opening.id,
    userId: opening.userId,
    boxId: opening.boxId,
    boxName: opening.boxName,
    boxPokeballImage: opening.boxPokeballImage,
    reward: {
      resourceType: opening.resourceType,
      resourceId: opening.resourceId,
      resourceName: opening.resourceName,
      isShiny: opening.isShiny,
      dropRate: opening.dropRate,
    },
    details: opening.details,
    openedAt: opening.openedAt,
    createdAt: opening.createdAt,
    updatedAt: opening.updatedAt,
  };
}

function serializeOpeningAdmin(opening) {
  return {
    ...serializeOpening(opening),
    user: opening.user
      ? {
          id: opening.user.id,
          username: opening.user.username,
          email: opening.user.email,
          xp: opening.user.xp,
        }
      : null,
    box: opening.box
      ? {
          id: opening.box.id,
          name: opening.box.name,
          pokeballImage: opening.box.pokeballImage,
        }
      : null,
  };
}

router.get("/admin/history", async (req, res) => {
  const limit = normalizeLimit(req.query.limit, 100, 500);
  const userId = String(req.query.userId || "").trim();
  const boxId = String(req.query.boxId || "").trim();
  const resourceType = String(req.query.resourceType || "")
    .trim()
    .toUpperCase();

  const where = {};

  if (userId) {
    where.userId = userId;
  }

  if (boxId) {
    where.boxId = boxId;
  }

  if (["POKEMON", "ITEM", "MACHINE"].includes(resourceType)) {
    where.resourceType = resourceType;
  }

  const twentyFourHoursAgo = new Date(Date.now() - 24 * 60 * 60 * 1000);

  const [openings, totalCount, last24hCount] = await Promise.all([
    prisma.boxOpening.findMany({
      where,
      orderBy: {
        openedAt: "desc",
      },
      take: limit,
      include: {
        user: {
          select: {
            id: true,
            username: true,
            email: true,
            xp: true,
          },
        },
        box: {
          select: {
            id: true,
            name: true,
            pokeballImage: true,
          },
        },
      },
    }),
    prisma.boxOpening.count({
      where,
    }),
    prisma.boxOpening.count({
      where: {
        ...where,
        openedAt: {
          gte: twentyFourHoursAgo,
        },
      },
    }),
  ]);

  return res.json({
    summary: {
      total: totalCount,
      last24h: last24hCount,
      returned: openings.length,
    },
    openings: openings.map(serializeOpeningAdmin),
  });
});

router.get("/me", authRequired, async (req, res) => {
  const limit = normalizeLimit(req.query.limit);

  const openings = await prisma.boxOpening.findMany({
    where: {
      userId: req.user.sub,
    },
    orderBy: {
      openedAt: "desc",
    },
    take: limit,
  });

  return res.json({
    openings: openings.map(serializeOpening),
  });
});

router.get("/users/:userId", async (req, res) => {
  const limit = normalizeLimit(req.query.limit);
  const userId = String(req.params.userId || "");

  const user = await prisma.user.findUnique({
    where: {
      id: userId,
    },
    select: {
      id: true,
      username: true,
      xp: true,
    },
  });

  if (!user) {
    return res.status(404).json({ error: "User not found." });
  }

  const openings = await prisma.boxOpening.findMany({
    where: {
      userId,
    },
    orderBy: {
      openedAt: "desc",
    },
    take: limit,
  });

  return res.json({
    user,
    openings: openings.map(serializeOpening),
  });
});

module.exports = router;
