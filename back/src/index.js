const cors = require("cors");
const express = require("express");
const { PORT } = require("./config/env");
const { prisma } = require("./lib/prisma");
const authRoutes = require("./routes/authRoutes");
const pokemonRoutes = require("./routes/pokemonRoutes");

const app = express();

app.use(cors());
app.use(express.json());

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
app.use("/api/pokemon", pokemonRoutes);

app.use((req, res) => {
  res.status(404).json({ error: "Route not found." });
});

app.use((err, req, res, _next) => {
  console.error(err);
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
