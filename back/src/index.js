const path = require("path");
const cors = require("cors");
const express = require("express");
const { PORT } = require("./config/env");
const { prisma } = require("./lib/prisma");
const authRoutes = require("./routes/authRoutes");
const boxRoutes = require("./routes/boxRoutes");
const catalogRoutes = require("./routes/catalogRoutes");
const inventoryRoutes = require("./routes/inventoryRoutes");
const boxOpeningRoutes = require("./routes/boxOpeningRoutes");
const pokemonRoutes = require("./routes/pokemonRoutes");
const tradeRoutes = require("./routes/tradeRoutes");
const userManagementRoutes = require("./routes/userManagementRoutes");
const characterRoutes = require("./routes/characterRoutes");
const configurationRoutes = require("./routes/configurationRoutes");

const app = express();

app.use(cors());
app.use(express.json());
app.use("/admin", express.static(path.join(__dirname, "public")));
app.use("/uploads", express.static(path.join(__dirname, "uploads")));

app.get("/", (req, res) => {
  return res.redirect("/admin");
});

app.get("/api/health", async (req, res) => {
  let db = "up";

  try {
    await prisma.$queryRaw`SELECT 1`;
  } catch {
    db = "down";
  }

  res.json({
    status: db === "up" ? "ok" : "degraded",
    service: "pokenity-back",
    db,
    now: new Date().toISOString(),
  });
});

app.use("/api/auth", authRoutes);
app.use("/api/boxes", boxRoutes);
app.use("/api/catalog", catalogRoutes);
app.use("/api/inventory", inventoryRoutes);
app.use("/api/box-openings", boxOpeningRoutes);
app.use("/api/pokemon", pokemonRoutes);
app.use("/api/trades", tradeRoutes);
app.use("/api/users", userManagementRoutes);
app.use("/api/characters", characterRoutes);
app.use("/api/configurations", configurationRoutes);

app.use((req, res) => {
  res.status(404).json({ error: "Route not found." });
});

app.use((err, req, res, _next) => {
  console.error(err);

  if (typeof err.status === "number" && err.message) {
    return res.status(err.status).json({ error: err.message });
  }

  res.status(500).json({ error: "Internal server error." });
});

async function startServer() {
  await prisma.$connect();
  app.listen(PORT, () => {
    console.log(`Pokenity API running on http://localhost:${PORT}`);
  });
}

startServer().catch((error) => {
  console.error("Unable to start API:", error);
  process.exit(1);
});

process.on("SIGINT", async () => {
  await prisma.$disconnect();
  process.exit(0);
});

process.on("SIGTERM", async () => {
  await prisma.$disconnect();
  process.exit(0);
});
