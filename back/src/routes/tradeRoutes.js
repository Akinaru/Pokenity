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
  const offeredPokemons = (trade.offeredItems || []).map((item) => ({
    resourceType: item.resourceType,
    resourceId: item.resourceId,
    resourceName: item.resourceName,
    isShiny: item.isShiny === true,
    quantity: item.quantity,
  }));

  const requestedPokemons = (trade.requestedItems || []).map((item) => ({
    resourceType: item.resourceType,
    resourceId: item.resourceId,
    resourceName: item.resourceName,
    isShiny: item.isShiny === true,
    quantity: item.quantity,
  }));

  return {
    id: trade.id,
    status: trade.status,
    proposerId: trade.proposerId,
    recipientId: trade.recipientId,
    proposer: trade.proposer || null,
    recipient: trade.recipient || null,
    offeredPokemons,
    offeredPokemon: {
      resourceType: trade.offeredResourceType,
      resourceId: trade.offeredResourceId,
      resourceName: trade.offeredResourceName,
      isShiny: trade.offeredIsShiny,
      quantity: offeredPokemons[0]?.quantity || 1,
    },
    receivedPokemon:
      trade.receivedResourceType && trade.receivedResourceId != null
        ? {
            resourceType: trade.receivedResourceType,
            resourceId: trade.receivedResourceId,
            resourceName: trade.receivedResourceName,
            isShiny: trade.receivedIsShiny === true,
          }
        : null,
    requestedPokemons,
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
  offeredItems: true,
  requestedItems: true,
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

function toPositiveInt(value, fallback = 1) {
  const parsed = Number(value);
  if (!Number.isFinite(parsed) || parsed <= 0) {
    return fallback;
  }
  return Math.trunc(parsed);
}

async function findPokemonInventoryItem(itemId, userId, minQuantity = 1, tx = prisma) {
  return tx.inventoryItem.findFirst({
    where: {
      id: itemId,
      userId,
      resourceType: "POKEMON",
      quantity: {
        gte: minQuantity,
      },
    },
  });
}

async function findPokemonInventoryByKey(
  userId,
  resourceType,
  resourceId,
  isShiny,
  minQuantity = 1,
  tx = prisma
) {
  return tx.inventoryItem.findUnique({
    where: {
      userId_resourceType_resourceId_isShiny: {
        userId,
        resourceType,
        resourceId,
        isShiny,
      },
    },
  }).then((item) => {
    if (!item || item.quantity < minQuantity) {
      return null;
    }
    return item;
  });
}

async function decreaseInventoryQuantity(tx, item, quantity) {
  if (item.quantity <= quantity) {
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
        decrement: quantity,
      },
    },
  });
}

async function increaseInventory(
  tx,
  userId,
  resourceType,
  resourceId,
  resourceName,
  isShiny,
  quantity,
  now
) {
  return tx.inventoryItem.upsert({
    where: {
      userId_resourceType_resourceId_isShiny: {
        userId,
        resourceType,
        resourceId,
        isShiny,
      },
    },
    update: {
      resourceName,
      quantity: {
        increment: quantity,
      },
      lastObtainedAt: now,
    },
    create: {
      userId,
      resourceType,
      resourceId,
      resourceName,
      isShiny,
      quantity,
      firstObtainedAt: now,
      lastObtainedAt: now,
    },
  });
}

router.post("/", authRequired, async (req, res) => {
  const rawOfferedPokemons = Array.isArray(req.body.offeredPokemons)
    ? req.body.offeredPokemons
    : [];
  const offeredInventoryItemId = String(req.body.offeredInventoryItemId || "").trim();
  const rawRequestedPokemons = req.body.requestedPokemons;

  const offeredPokemonBodies =
    rawOfferedPokemons.length > 0
      ? rawOfferedPokemons
      : offeredInventoryItemId
        ? [{ inventoryItemId: offeredInventoryItemId, quantity: 1 }]
        : [];

  if (!Array.isArray(offeredPokemonBodies) || offeredPokemonBodies.length < 1) {
    return res.status(400).json({ error: "offeredPokemons must be an array with at least 1 item." });
  }

  if (offeredPokemonBodies.length > 5) {
    return res.status(400).json({ error: "offeredPokemons must have at most 5 items." });
  }

  if (!Array.isArray(rawRequestedPokemons) || rawRequestedPokemons.length < 1) {
    return res.status(400).json({ error: "requestedPokemons must be an array with at least 1 item." });
  }

  if (rawRequestedPokemons.length > 5) {
    return res.status(400).json({ error: "requestedPokemons must have at most 5 items." });
  }

  const requestedByKey = new Map();
  for (const item of rawRequestedPokemons) {
    const resourceId = Number(item.resourceId);
    const resourceName = String(item.resourceName || "").trim();
    const quantity = toPositiveInt(item.quantity, 1);
    const isShiny = item.isShiny === true;

    if (!Number.isFinite(resourceId) || resourceId <= 0) {
      return res.status(400).json({ error: "Each requestedPokemon must have a valid resourceId." });
    }
    if (!resourceName) {
      return res.status(400).json({ error: "Each requestedPokemon must have a resourceName." });
    }
    if (quantity > 999) {
      return res.status(400).json({ error: "Each requestedPokemon quantity must be <= 999." });
    }

    const key = `POKEMON:${resourceId}:${isShiny ? "1" : "0"}`;
    const previous = requestedByKey.get(key);
    if (previous) {
      previous.quantity += quantity;
    } else {
      requestedByKey.set(key, {
        resourceType: "POKEMON",
        resourceId,
        resourceName,
        isShiny,
        quantity,
      });
    }
  }

  const requestedPokemons = Array.from(requestedByKey.values());
  if (requestedPokemons.length > 5) {
    return res.status(400).json({ error: "requestedPokemons must contain at most 5 distinct Pokemon." });
  }

  const offeredByInventoryId = new Map();
  for (const item of offeredPokemonBodies) {
    const inventoryItemId = String(item.inventoryItemId || "").trim();
    const quantity = toPositiveInt(item.quantity, 1);

    if (!inventoryItemId) {
      return res.status(400).json({ error: "Each offeredPokemon must have an inventoryItemId." });
    }
    if (quantity > 999) {
      return res.status(400).json({ error: "Each offeredPokemon quantity must be <= 999." });
    }

    const previousQty = offeredByInventoryId.get(inventoryItemId) || 0;
    offeredByInventoryId.set(inventoryItemId, previousQty + quantity);
  }

  const offeredPokemons = [];
  for (const [inventoryItemId, quantity] of offeredByInventoryId.entries()) {
    const offeredItem = await findPokemonInventoryItem(
      inventoryItemId,
      req.user.sub,
      quantity,
      prisma
    );

    if (!offeredItem) {
      return res.status(404).json({
        error: `Offered pokemon ${inventoryItemId} not found in your inventory with requested quantity.`,
      });
    }

    offeredPokemons.push({
      resourceType: offeredItem.resourceType,
      resourceId: offeredItem.resourceId,
      resourceName: offeredItem.resourceName,
      isShiny: offeredItem.isShiny === true,
      quantity,
    });
  }

  if (offeredPokemons.length < 1) {
    return res.status(400).json({ error: "At least one offered pokemon is required." });
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

  const primaryOffered = offeredPokemons[0];

  const trade = await prisma.trade.create({
    data: {
      proposerId: req.user.sub,
      status: TRADE_STATUS.PENDING,
      offeredResourceType: primaryOffered.resourceType,
      offeredResourceId: primaryOffered.resourceId,
      offeredResourceName: primaryOffered.resourceName,
      offeredIsShiny: primaryOffered.isShiny,
      expiresAt,
      offeredItems: {
        create: offeredPokemons.map((op) => ({
          resourceType: op.resourceType,
          resourceId: op.resourceId,
          resourceName: op.resourceName,
          isShiny: op.isShiny,
          quantity: op.quantity,
        })),
      },
      requestedItems: {
        create: requestedPokemons.map((rp) => ({
          resourceType: rp.resourceType,
          resourceId: rp.resourceId,
          resourceName: rp.resourceName,
          isShiny: rp.isShiny,
          quantity: rp.quantity,
        })),
      },
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

  const trade = await prisma.trade.findUnique({
    where: {
      id: tradeId,
    },
    include: {
      requestedItems: true,
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

  const recipientHasAllRequested = await Promise.all(
    (trade.requestedItems || []).map(async (requestedItem) => {
      const quantity = toPositiveInt(requestedItem.quantity, 1);
      const inventoryItem = await findPokemonInventoryByKey(
        req.user.sub,
        requestedItem.resourceType,
        requestedItem.resourceId,
        requestedItem.isShiny === true,
        quantity,
        prisma
      );
      return inventoryItem != null;
    })
  );

  if (recipientHasAllRequested.some((hasItem) => !hasItem)) {
    return res.status(409).json({
      error: "Vous ne possedez pas toutes les ressources demandees avec les quantites requises.",
    });
  }

  const primaryRequested = (trade.requestedItems || [])[0] || null;

  const acceptedAt = new Date();
  const updateData = {
    recipientId: req.user.sub,
    status: TRADE_STATUS.WAITING_CONFIRMATION,
    acceptedAt,
  };

  if (primaryRequested) {
    updateData.receivedResourceType = primaryRequested.resourceType;
    updateData.receivedResourceId = primaryRequested.resourceId;
    updateData.receivedResourceName = primaryRequested.resourceName;
    updateData.receivedIsShiny = primaryRequested.isShiny === true;
  } else {
    updateData.receivedResourceType = null;
    updateData.receivedResourceId = null;
    updateData.receivedResourceName = null;
    updateData.receivedIsShiny = null;
  }

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
    data: updateData,
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
        include: {
          offeredItems: true,
          requestedItems: true,
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

      if (!currentTrade.offeredItems || currentTrade.offeredItems.length === 0) {
        const error = new Error("Trade has no offered pokemon.");
        error.status = 409;
        throw error;
      }

      if (!currentTrade.requestedItems || currentTrade.requestedItems.length === 0) {
        const error = new Error("Trade has no requested pokemon.");
        error.status = 409;
        throw error;
      }

      const proposerItems = await Promise.all(
        currentTrade.offeredItems.map(async (offeredItem) => {
          const quantity = toPositiveInt(offeredItem.quantity, 1);
          const inventoryItem = await findPokemonInventoryByKey(
            currentTrade.proposerId,
            offeredItem.resourceType,
            offeredItem.resourceId,
            offeredItem.isShiny === true,
            quantity,
            tx
          );
          return { inventoryItem, offeredItem, quantity };
        })
      );

      const missingProposerItem = proposerItems.find((entry) => !entry.inventoryItem);
      if (missingProposerItem) {
        const error = new Error("You no longer own all offered pokemon quantities.");
        error.status = 409;
        throw error;
      }

      const recipientItems = await Promise.all(
        currentTrade.requestedItems.map(async (requestedItem) => {
          const quantity = toPositiveInt(requestedItem.quantity, 1);
          const inventoryItem = await findPokemonInventoryByKey(
            currentTrade.recipientId,
            requestedItem.resourceType,
            requestedItem.resourceId,
            requestedItem.isShiny === true,
            quantity,
            tx
          );
          return { inventoryItem, requestedItem, quantity };
        })
      );

      const missingRecipientItem = recipientItems.find((entry) => !entry.inventoryItem);
      if (missingRecipientItem) {
        const error = new Error("Recipient no longer owns all requested pokemon quantities.");
        error.status = 409;
        throw error;
      }

      const now = new Date();

      for (const entry of proposerItems) {
        await decreaseInventoryQuantity(tx, entry.inventoryItem, entry.quantity);
        await increaseInventory(
          tx,
          currentTrade.recipientId,
          entry.offeredItem.resourceType,
          entry.offeredItem.resourceId,
          entry.offeredItem.resourceName,
          entry.offeredItem.isShiny === true,
          entry.quantity,
          now
        );
      }

      for (const entry of recipientItems) {
        await decreaseInventoryQuantity(tx, entry.inventoryItem, entry.quantity);
        await increaseInventory(
          tx,
          currentTrade.proposerId,
          entry.requestedItem.resourceType,
          entry.requestedItem.resourceId,
          entry.requestedItem.resourceName,
          entry.requestedItem.isShiny === true,
          entry.quantity,
          now
        );
      }

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
