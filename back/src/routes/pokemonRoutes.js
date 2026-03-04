const express = require("express");
const { authRequired } = require("../middleware/auth");
const { POKEAPI_BASE_URL } = require("../config/env");

const router = express.Router();

router.get("/:name", authRequired, async (req, res) => {
  const name = String(req.params.name || "").trim().toLowerCase();
  if (!name) {
    return res.status(400).json({ error: "Pokemon name is required." });
  }

  try {
    const response = await fetch(
      `${POKEAPI_BASE_URL}/pokemon/${encodeURIComponent(name)}`
    );

    if (response.status === 404) {
      return res.status(404).json({ error: "Pokemon not found." });
    }

    if (!response.ok) {
      return res
        .status(502)
        .json({ error: "Failed to fetch data from PokeAPI." });
    }

    const data = await response.json();
    return res.json({
      id: data.id,
      name: data.name,
      height: data.height,
      weight: data.weight,
      types: data.types.map((item) => item.type.name),
      stats: data.stats.map((item) => ({
        name: item.stat.name,
        value: item.base_stat,
      })),
      sprites: {
        frontDefault: data.sprites.front_default,
        officialArtwork:
          data.sprites.other?.["official-artwork"]?.front_default || null,
      },
    });
  } catch {
    return res.status(502).json({ error: "PokeAPI is unreachable right now." });
  }
});

module.exports = router;
