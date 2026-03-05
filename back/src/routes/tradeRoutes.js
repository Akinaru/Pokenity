const express = require("express");
const { prisma } = require("../lib/prisma");
const { authRequired } = require("../middleware/auth");

const router = express.Router();

const TRADE_STATUS = {
  PENDING: "PENDING",
  WAITING_CONFIRMATION: "WAITING_CONFIRMATION",
  COMPLETED: "COMPLETED",
  CANCELED: "CANCELED",
  DECLINED: "DECLINED",
};

function normalizeLimit(value, fallback = 30, max = 200) {
  const parsed = Number(value);
  if (!Number.isFinite(parsed) || parsed <= 0) {
    return fallback;
  }
  return Math.min(Math.trunc(parsed), max);
}

function serializeTrade(trade) {
  return {
    id: trade.id,
    status: trade.status,
    proposerId: trade.proposerId,
    recipientId: trade.recipientId,
    proposer: trade.proposer || null,
    recipient: trade.recipient || null,
    offeredPokemon: {
      resourceType: trade.offeredResourceType,
      resourceId: trade.offeredResourceId,
      resourceName: trade.offeredResourceName,
    },
    receivedPokemon:
      trade.receivedResourceType && trade.receivedResourceId
        ? {
            resourceType: trade.receivedResourceType,
            resourceId: trade.receivedResourceId,
            resourceName: trade.receivedResourceName,
          }
        : null,
    acceptedAt: trade.acceptedAt,
    confirmedAt: trade.confirmedAt,
    completedAt: trade.completedAt,
    canceledAt: trade.canceledAt,
    declinedAt: trade.declinedAt,
    expiresAt: trade.expiresAt,
    createdAt: trade.createdAt,
    updatedAt: trade.updatedAt,
  };
}

function parseStatuses(rawValue) {
  if (!rawValue) {
    return [];
  }

  return String(rawValue)
    .split(",")
    .map((entry) => entry.trim().toUpperCase())
    .filter((entry) => Object.keys(TRADE_STATUS).includes(entry));
}

const tradeInclude = {
  proposer: {
    select: {
      id: true,
      username: true,
      xp: true,
    },
  },
  recipient: {
    select: {
      id: true,
      username: true,
      xp: true,
    },
  },
};

router.get("/admin/current", async (req, res) => {
  const limit = normalizeLimit(req.query.limit, 80, 400);
  const statusesFromQuery = parseStatuses(req.query.status);
  const statuses =
    statusesFromQuery.length > 0
      ? statusesFromQuery
      : [TRADE_STATUS.PENDING, TRADE_STATUS.WAITING_CONFIRMATION];

  const where = {
    status: {
      in: statuses,
    },
  };

  const [trades, statusRows] = await Promise.all([
    prisma.trade.findMany({
      where,
      orderBy: [
        {
          updatedAt: "desc",
        },
        {
          createdAt: "desc",
        },
      ],
      take: limit,
      include: tradeInclude,
    }),
    prisma.trade.groupBy({
      by: ["status"],
      where,
      _count: {
        _all: true,
      },
    }),
  ]);

  const summary = statusRows.reduce(
    (acc, row) => {
      acc[row.status] = row._count._all;
      return acc;
    },
    {
      total: trades.length,
      PENDING: 0,
      WAITING_CONFIRMATION: 0,
      COMPLETED: 0,
      CANCELED: 0,
      DECLINED: 0,
    }
  );

  return res.json({
    summary,
    trades: trades.map(serializeTrade),
  });
});

async function findPokemonInventoryItem(itemId, userId, tx = prisma) {
  return tx.inventoryItem.findFirst({
    where: {
      id: itemId,
      userId,
      resourceType: "POKEMON",
      quantity: {
        gte: 1,
      },
    },
  });
}

async function decreaseOneInventoryItem(tx, item) {
  if (item.quantity <= 1) {
    await tx.inventoryItem.delete({
      where: {
        id: item.id,
      },
    });
    return;
  }

  await tx.inventoryItem.update({
    where: {
      id: item.id,
    },
    data: {
      quantity: {
        decrement: 1,
      },
    },
  });
}

async function increaseInventory(tx, userId, resourceType, resourceId, resourceName, now) {
  return tx.inventoryItem.upsert({
    where: {
      userId_resourceType_resourceId: {
        userId,
        resourceType,
        resourceId,
      },
    },
    update: {
      resourceName,
      quantity: {
        increment: 1,
      },
      lastObtainedAt: now,
    },
    create: {
      userId,
      resourceType,
      resourceId,
      resourceName,
      quantity: 1,
      firstObtainedAt: now,
      lastObtainedAt: now,
    },
  });
}

router.post("/", authRequired, async (req, res) => {
  const offeredInventoryItemId = String(req.body.offeredInventoryItemId || "").trim();
  const targetUserId =
    req.body.targetUserId === undefined || req.body.targetUserId === null
      ? null
      : String(req.body.targetUserId).trim();

  if (!offeredInventoryItemId) {
    return res.status(400).json({ error: "offeredInventoryItemId is required." });
  }

  const offeredItem = await findPokemonInventoryItem(
    offeredInventoryItemId,
    req.user.sub,
    prisma
  );

  if (!offeredItem) {
    return res.status(404).json({
      error:
        "Offered pokemon not found in your inventory (or quantity is 0).",
    });
  }

  if (targetUserId && targetUserId === req.user.sub) {
    return res.status(400).json({ error: "You cannot create a trade with yourself." });
  }

  if (targetUserId) {
    const targetUser = await prisma.user.findUnique({
      where: {
        id: targetUserId,
      },
      select: {
        id: true,
      },
    });

    if (!targetUser) {
      return res.status(404).json({ error: "Target user not found." });
    }
  }

  const expiresAtRaw =
    req.body.expiresAt === undefined || req.body.expiresAt === null
      ? ""
      : String(req.body.expiresAt).trim();
  let expiresAt = null;

  if (expiresAtRaw) {
    const parsed = new Date(expiresAtRaw);
    if (Number.isNaN(parsed.getTime())) {
      return res.status(400).json({ error: "expiresAt must be a valid ISO date." });
    }
    if (parsed <= new Date()) {
      return res.status(400).json({ error: "expiresAt must be in the future." });
    }
    expiresAt = parsed;
  }

  const trade = await prisma.trade.create({
    data: {
      proposerId: req.user.sub,
      recipientId: targetUserId || null,
      status: TRADE_STATUS.PENDING,
      offeredResourceType: offeredItem.resourceType,
      offeredResourceId: offeredItem.resourceId,
      offeredResourceName: offeredItem.resourceName,
      expiresAt,
    },
    include: tradeInclude,
  });

  return res.status(201).json({
    trade: serializeTrade(trade),
  });
});

router.get("/open", authRequired, async (req, res) => {
  const limit = normalizeLimit(req.query.limit);

  const trades = await prisma.trade.findMany({
    where: {
      status: TRADE_STATUS.PENDING,
      proposerId: {
        not: req.user.sub,
      },
      AND: [
        {
          OR: [
            {
              recipientId: null,
            },
            {
              recipientId: req.user.sub,
            },
          ],
        },
        {
          OR: [
            {
              expiresAt: null,
            },
            {
              expiresAt: {
                gt: new Date(),
              },
            },
          ],
        },
      ],
    },
    orderBy: {
      createdAt: "desc",
    },
    take: limit,
    include: tradeInclude,
  });

  return res.json({
    trades: trades.map(serializeTrade),
  });
});

router.get("/me", authRequired, async (req, res) => {
  const limit = normalizeLimit(req.query.limit, 60, 250);

  const trades = await prisma.trade.findMany({
    where: {
      OR: [
        {
          proposerId: req.user.sub,
        },
        {
          recipientId: req.user.sub,
        },
      ],
    },
    orderBy: {
      createdAt: "desc",
    },
    take: limit,
    include: tradeInclude,
  });

  return res.json({
    trades: trades.map(serializeTrade),
  });
});

router.get("/:tradeId", authRequired, async (req, res) => {
  const tradeId = String(req.params.tradeId || "");

  const trade = await prisma.trade.findUnique({
    where: {
      id: tradeId,
    },
    include: tradeInclude,
  });

  if (!trade) {
    return res.status(404).json({ error: "Trade not found." });
  }

  const canAccess =
    trade.proposerId === req.user.sub ||
    trade.recipientId === req.user.sub ||
    (trade.status === TRADE_STATUS.PENDING &&
      !trade.recipientId &&
      trade.proposerId !== req.user.sub);

  if (!canAccess) {
    return res.status(403).json({ error: "You cannot access this trade." });
  }

  return res.json({
    trade: serializeTrade(trade),
  });
});

router.post("/:tradeId/accept", authRequired, async (req, res) => {
  const tradeId = String(req.params.tradeId || "");
  const offeredInventoryItemId = String(req.body.offeredInventoryItemId || "").trim();

  if (!offeredInventoryItemId) {
    return res.status(400).json({ error: "offeredInventoryItemId is required." });
  }

  const trade = await prisma.trade.findUnique({
    where: {
      id: tradeId,
    },
  });

  if (!trade) {
    return res.status(404).json({ error: "Trade not found." });
  }

  if (trade.status !== TRADE_STATUS.PENDING) {
    return res.status(409).json({ error: "Trade is not pending anymore." });
  }

  if (trade.expiresAt && trade.expiresAt <= new Date()) {
    return res.status(409).json({ error: "Trade has expired." });
  }

  if (trade.proposerId === req.user.sub) {
    return res.status(400).json({ error: "You cannot accept your own trade." });
  }

  if (trade.recipientId && trade.recipientId !== req.user.sub) {
    return res.status(403).json({
      error: "This trade is reserved for another user.",
    });
  }

  const offeredItem = await findPokemonInventoryItem(
    offeredInventoryItemId,
    req.user.sub,
    prisma
  );

  if (!offeredItem) {
    return res.status(404).json({
      error:
        "Selected pokemon not found in your inventory (or quantity is 0).",
    });
  }

  const acceptedAt = new Date();
  const updateResult = await prisma.trade.updateMany({
    where: {
      id: trade.id,
      status: TRADE_STATUS.PENDING,
      proposerId: {
        not: req.user.sub,
      },
      AND: [
        {
          OR: [
            {
              recipientId: null,
            },
            {
              recipientId: req.user.sub,
            },
          ],
        },
        {
          OR: [
            {
              expiresAt: null,
            },
            {
              expiresAt: {
                gt: acceptedAt,
              },
            },
          ],
        },
      ],
    },
    data: {
      recipientId: req.user.sub,
      status: TRADE_STATUS.WAITING_CONFIRMATION,
      receivedResourceType: offeredItem.resourceType,
      receivedResourceId: offeredItem.resourceId,
      receivedResourceName: offeredItem.resourceName,
      acceptedAt,
    },
  });

  if (updateResult.count !== 1) {
    return res.status(409).json({
      error: "Trade changed meanwhile. Reload trade and try again.",
    });
  }

  const updatedTrade = await prisma.trade.findUnique({
    where: {
      id: trade.id,
    },
    include: tradeInclude,
  });

  if (!updatedTrade) {
    return res.status(404).json({ error: "Trade not found after update." });
  }

  return res.json({
    trade: serializeTrade(updatedTrade),
  });
});

router.post("/:tradeId/confirm", authRequired, async (req, res) => {
  const tradeId = String(req.params.tradeId || "");

  try {
    const trade = await prisma.$transaction(async (tx) => {
      const currentTrade = await tx.trade.findUnique({
        where: {
          id: tradeId,
        },
      });

      if (!currentTrade) {
        const error = new Error("Trade not found.");
        error.status = 404;
        throw error;
      }

      if (currentTrade.proposerId !== req.user.sub) {
        const error = new Error("Only trade proposer can confirm this trade.");
        error.status = 403;
        throw error;
      }

      if (currentTrade.status !== TRADE_STATUS.WAITING_CONFIRMATION) {
        const error = new Error("Trade is not waiting for confirmation.");
        error.status = 409;
        throw error;
      }

      if (!currentTrade.recipientId) {
        const error = new Error("Trade has no recipient.");
        error.status = 409;
        throw error;
      }

      if (currentTrade.offeredResourceType !== "POKEMON") {
        const error = new Error("Only pokemon trades are supported.");
        error.status = 409;
        throw error;
      }

      if (!currentTrade.receivedResourceType || !currentTrade.receivedResourceId) {
        const error = new Error("Recipient pokemon is missing on this trade.");
        error.status = 409;
        throw error;
      }

      if (currentTrade.receivedResourceType !== "POKEMON") {
        const error = new Error("Only pokemon trades are supported.");
        error.status = 409;
        throw error;
      }

      if (!currentTrade.receivedResourceName) {
        const error = new Error("Recipient pokemon name is missing on this trade.");
        error.status = 409;
        throw error;
      }

      const proposerItem = await tx.inventoryItem.findUnique({
        where: {
          userId_resourceType_resourceId: {
            userId: currentTrade.proposerId,
            resourceType: currentTrade.offeredResourceType,
            resourceId: currentTrade.offeredResourceId,
          },
        },
      });

      if (!proposerItem || proposerItem.quantity < 1) {
        const error = new Error("You no longer own the offered pokemon.");
        error.status = 409;
        throw error;
      }

      const recipientItem = await tx.inventoryItem.findUnique({
        where: {
          userId_resourceType_resourceId: {
            userId: currentTrade.recipientId,
            resourceType: currentTrade.receivedResourceType,
            resourceId: currentTrade.receivedResourceId,
          },
        },
      });

      if (!recipientItem || recipientItem.quantity < 1) {
        const error = new Error("Recipient no longer owns the selected pokemon.");
        error.status = 409;
        throw error;
      }

      const now = new Date();

      await decreaseOneInventoryItem(tx, proposerItem);
      await decreaseOneInventoryItem(tx, recipientItem);

      await increaseInventory(
        tx,
        currentTrade.recipientId,
        currentTrade.offeredResourceType,
        currentTrade.offeredResourceId,
        currentTrade.offeredResourceName,
        now
      );

      await increaseInventory(
        tx,
        currentTrade.proposerId,
        currentTrade.receivedResourceType,
        currentTrade.receivedResourceId,
        currentTrade.receivedResourceName,
        now
      );

      await tx.user.update({
        where: {
          id: currentTrade.proposerId,
        },
        data: {
          xp: {
            increment: 2,
          },
        },
      });

      await tx.user.update({
        where: {
          id: currentTrade.recipientId,
        },
        data: {
          xp: {
            increment: 2,
          },
        },
      });

      await tx.trade.update({
        where: {
          id: currentTrade.id,
        },
        data: {
          status: TRADE_STATUS.COMPLETED,
          confirmedAt: now,
          completedAt: now,
        },
      });

      const completedTrade = await tx.trade.findUnique({
        where: {
          id: currentTrade.id,
        },
        include: tradeInclude,
      });

      if (!completedTrade) {
        const error = new Error("Trade was completed but could not be loaded.");
        error.status = 500;
        throw error;
      }

      return completedTrade;
    });

    return res.json({
      trade: serializeTrade(trade),
    });
  } catch (error) {
    if (error && typeof error.status === "number") {
      return res.status(error.status).json({ error: error.message });
    }
    throw error;
  }
});

router.post("/:tradeId/cancel", authRequired, async (req, res) => {
  const tradeId = String(req.params.tradeId || "");

  const trade = await prisma.trade.findUnique({
    where: {
      id: tradeId,
    },
  });

  if (!trade) {
    return res.status(404).json({ error: "Trade not found." });
  }

  if (trade.proposerId !== req.user.sub) {
    return res.status(403).json({ error: "Only trade proposer can cancel." });
  }

  if (
    ![
      TRADE_STATUS.PENDING,
      TRADE_STATUS.WAITING_CONFIRMATION,
    ].includes(trade.status)
  ) {
    return res.status(409).json({
      error: "Trade cannot be canceled in its current status.",
    });
  }

  const updatedTrade = await prisma.trade.update({
    where: {
      id: trade.id,
    },
    data: {
      status: TRADE_STATUS.CANCELED,
      canceledAt: new Date(),
    },
    include: tradeInclude,
  });

  return res.json({
    trade: serializeTrade(updatedTrade),
  });
});

router.post("/:tradeId/decline", authRequired, async (req, res) => {
  const tradeId = String(req.params.tradeId || "");

  const trade = await prisma.trade.findUnique({
    where: {
      id: tradeId,
    },
  });

  if (!trade) {
    return res.status(404).json({ error: "Trade not found." });
  }

  if (!trade.recipientId || trade.recipientId !== req.user.sub) {
    return res.status(403).json({
      error: "Only assigned recipient can decline this trade.",
    });
  }

  if (
    ![
      TRADE_STATUS.PENDING,
      TRADE_STATUS.WAITING_CONFIRMATION,
    ].includes(trade.status)
  ) {
    return res.status(409).json({
      error: "Trade cannot be declined in its current status.",
    });
  }

  const updatedTrade = await prisma.trade.update({
    where: {
      id: trade.id,
    },
    data: {
      status: TRADE_STATUS.DECLINED,
      declinedAt: new Date(),
    },
    include: tradeInclude,
  });

  return res.json({
    trade: serializeTrade(updatedTrade),
  });
});

module.exports = router;
