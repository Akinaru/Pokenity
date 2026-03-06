const express = require("express");
const {
  CONFIG_KEYS,
  listConfigurations,
  normalizeConfigKey,
  parseDropRate,
  updateConfigurationValue,
} = require("../lib/configuration");

const router = express.Router();

router.get("/", async (_req, res) => {
  const configurations = await listConfigurations();
  return res.json({ configurations });
});

router.patch("/:key", async (req, res) => {
  const key = normalizeConfigKey(req.params.key);
  if (key !== CONFIG_KEYS.SHINY_DROP_RATE) {
    return res.status(404).json({ error: "Configuration not found." });
  }

  const rawPercent = req.body.percent;
  const rawValue = req.body.value;

  if (rawPercent === undefined && rawValue === undefined) {
    return res.status(400).json({ error: "value or percent is required." });
  }

  let nextRate = Number.NaN;
  if (rawPercent !== undefined) {
    const percent = Number(rawPercent);
    if (Number.isFinite(percent)) {
      nextRate = percent / 100;
    }
  } else {
    nextRate = parseDropRate(rawValue, Number.NaN);
  }

  if (!Number.isFinite(nextRate) || nextRate < 0 || nextRate > 1) {
    return res.status(400).json({
      error: "Invalid shiny drop rate. Expected value between 0 and 1, or percent between 0 and 100.",
    });
  }

  const configuration = await updateConfigurationValue(key, nextRate);
  return res.json({ configuration });
});

module.exports = router;
