const express = require("express");
const { prisma } = require("../lib/prisma");
const { authRequired } = require("../middleware/auth");

const router = express.Router();

function serializeItem(item) {
  return {
    id: item.id,
    userId: item.userId,
    resourceType: item.resourceType,
    resourceId: item.resourceId,
    resourceName: item.resourceName,
    quantity: item.quantity,
    firstObtainedAt: item.firstObtainedAt,
    lastObtainedAt: item.lastObtainedAt,
  };
}

router.get("/me", authRequired, async (req, res) => {
  const user = await prisma.user.findUnique({
    where: { id: req.user.sub },
    select: { id: true, username: true, email: true, xp: true },
  });

  if (!user) {
    return res.status(404).json({ error: "User not found." });
  }

  const items = await prisma.inventoryItem.findMany({
    where: {
      userId: req.user.sub,
    },
    orderBy: [
      {
        lastObtainedAt: "desc",
      },
      {
        createdAt: "desc",
      },
    ],
  });

  return res.json({
    user,
    inventory: items.map(serializeItem),
  });
});

router.get("/users/:userId", async (req, res) => {
  const userId = String(req.params.userId || "");
  const user = await prisma.user.findUnique({
    where: { id: userId },
    select: { id: true, username: true, email: true, xp: true },
  });

  if (!user) {
    return res.status(404).json({ error: "User not found." });
  }

  const items = await prisma.inventoryItem.findMany({
    where: {
      userId,
    },
    orderBy: [
      {
        lastObtainedAt: "desc",
      },
      {
        createdAt: "desc",
      },
    ],
  });

  return res.json({
    user,
    inventory: items.map(serializeItem),
  });
});

module.exports = router;
