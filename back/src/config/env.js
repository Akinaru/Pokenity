const PORT = Number(process.env.PORT) || 3000;
const JWT_SECRET = process.env.JWT_SECRET || "dev_only_change_me";
const JWT_EXPIRES_IN = process.env.JWT_EXPIRES_IN || "7d";
const DATABASE_URL = process.env.DATABASE_URL;
const POKEAPI_BASE_URL =
  process.env.POKEAPI_BASE_URL || "https://pokeapi.co/api/v2";

if (!DATABASE_URL) {
  throw new Error("Missing DATABASE_URL in environment variables.");
}

module.exports = {
  PORT,
  JWT_SECRET,
  JWT_EXPIRES_IN,
  DATABASE_URL,
  POKEAPI_BASE_URL,
};
