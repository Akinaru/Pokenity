const { Prisma } = require("@prisma/client");
const { prisma } = require("./prisma");

const CONFIG_KEYS = {
  SHINY_DROP_RATE: "SHINY_DROP_RATE",
};

const CONFIG_DEFINITIONS = {
  [CONFIG_KEYS.SHINY_DROP_RATE]: {
    key: CONFIG_KEYS.SHINY_DROP_RATE,
    label: "Shiny drop rate",
    description: "Chance de drop shiny (entre 0 et 1).",
    defaultValue: "0.5",
  },
};

function normalizeConfigKey(rawKey) {
  return String(rawKey || "").trim().toUpperCase();
}

function parseDropRate(rawValue, fallback = 0.5) {
  const parsed = Number(rawValue);
  if (!Number.isFinite(parsed)) {
    return fallback;
  }

  const normalized = parsed > 1 && parsed <= 100 ? parsed / 100 : parsed;
  if (normalized < 0 || normalized > 1) {
    return fallback;
  }

  return normalized;
}

function formatNumericValue(value) {
  return Number(value).toFixed(6).replace(/\.?0+$/, "");
}

async function ensureConfiguration(key, tx = prisma) {
  const normalizedKey = normalizeConfigKey(key);
  const definition = CONFIG_DEFINITIONS[normalizedKey];
  if (!definition) {
    return null;
  }

  const existing = await tx.configuration.findUnique({
    where: {
      key: normalizedKey,
    },
  });

  if (existing) {
    return existing;
  }

  try {
    return await tx.configuration.create({
      data: {
        key: normalizedKey,
        value: definition.defaultValue,
        description: definition.description,
      },
    });
  } catch (error) {
    if (
      error instanceof Prisma.PrismaClientKnownRequestError &&
      error.code === "P2002"
    ) {
      return tx.configuration.findUnique({
        where: {
          key: normalizedKey,
        },
      });
    }
    throw error;
  }
}

function serializeConfiguration(configuration) {
  if (!configuration) {
    return null;
  }

  const definition = CONFIG_DEFINITIONS[configuration.key] || {
    key: configuration.key,
    label: configuration.key,
    description: configuration.description || null,
    defaultValue: configuration.value,
  };

  const numberValue = parseDropRate(configuration.value, parseDropRate(definition.defaultValue));

  return {
    id: configuration.id,
    key: configuration.key,
    label: definition.label,
    value: configuration.value,
    numberValue,
    percentValue: Number((numberValue * 100).toFixed(2)),
    description: configuration.description || definition.description || null,
    defaultValue: definition.defaultValue,
    createdAt: configuration.createdAt,
    updatedAt: configuration.updatedAt,
  };
}

async function listConfigurations(tx = prisma) {
  const keys = Object.keys(CONFIG_DEFINITIONS);
  const records = await Promise.all(keys.map((key) => ensureConfiguration(key, tx)));
  return records.map(serializeConfiguration);
}

async function updateConfigurationValue(key, nextValue, tx = prisma) {
  const normalizedKey = normalizeConfigKey(key);
  const definition = CONFIG_DEFINITIONS[normalizedKey];
  if (!definition) {
    return null;
  }

  const formattedValue = formatNumericValue(nextValue);

  const updated = await tx.configuration.upsert({
    where: {
      key: normalizedKey,
    },
    update: {
      value: formattedValue,
      description: definition.description,
    },
    create: {
      key: normalizedKey,
      value: formattedValue,
      description: definition.description,
    },
  });

  return serializeConfiguration(updated);
}

async function getShinyDropRate(tx = prisma) {
  const config = await ensureConfiguration(CONFIG_KEYS.SHINY_DROP_RATE, tx);
  return parseDropRate(config?.value, parseDropRate(CONFIG_DEFINITIONS.SHINY_DROP_RATE.defaultValue));
}

module.exports = {
  CONFIG_KEYS,
  CONFIG_DEFINITIONS,
  formatNumericValue,
  getShinyDropRate,
  listConfigurations,
  normalizeConfigKey,
  parseDropRate,
  serializeConfiguration,
  updateConfigurationValue,
};
