const { Prisma } = require("@prisma/client");
const express = require("express");
const { authRequired } = require("../middleware/auth");
const { createPasswordHash, verifyPassword } = require("../lib/password");
const { signToken } = require("../lib/token");
const {
  createUser,
  findUserByEmail,
  findUserById,
  findUserByUsername,
} = require("../repositories/userRepository");

const router = express.Router();

function cleanUser(user) {
  return {
    id: user.id,
    username: user.username,
    email: user.email,
    createdAt: user.createdAt,
  };
}

router.post("/register", async (req, res) => {
  const username = String(req.body.username || "").trim().toLowerCase();
  const email = String(req.body.email || "").trim().toLowerCase();
  const password = String(req.body.password || "");

  if (!username || !email || !password) {
    return res.status(400).json({
      error: "username, email and password are required.",
    });
  }

  if (password.length < 6) {
    return res.status(400).json({
      error: "password must be at least 6 characters long.",
    });
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) {
    return res.status(400).json({
      error: "email format is invalid.",
    });
  }

  const existingEmail = await findUserByEmail(email);
  if (existingEmail) {
    return res.status(409).json({ error: "email is already used." });
  }

  const existingUsername = await findUserByUsername(username);
  if (existingUsername) {
    return res.status(409).json({ error: "username is already used." });
  }

  let user;
  try {
    user = await createUser({
      username,
      email,
      passwordHash: await createPasswordHash(password),
    });
  } catch (error) {
    if (
      error instanceof Prisma.PrismaClientKnownRequestError &&
      error.code === "P2002"
    ) {
      return res.status(409).json({ error: "email or username is already used." });
    }
    throw error;
  }

  const token = signToken({
    sub: user.id,
    email: user.email,
    username: user.username,
  });

  return res.status(201).json({
    token,
    user: cleanUser(user),
  });
});

router.post("/login", async (req, res) => {
  const identifier = String(req.body.identifier || req.body.email || "").trim();
  const password = String(req.body.password || "");

  if (!identifier || !password) {
    return res.status(400).json({
      error: "identifier/email and password are required.",
    });
  }

  const user =
    (await findUserByEmail(identifier.toLowerCase())) ||
    (await findUserByUsername(identifier.toLowerCase()));

  if (!user || !(await verifyPassword(password, user.passwordHash))) {
    return res.status(401).json({ error: "Invalid credentials." });
  }

  const token = signToken({
    sub: user.id,
    email: user.email,
    username: user.username,
  });

  return res.json({
    token,
    user: cleanUser(user),
  });
});

router.get("/me", authRequired, async (req, res) => {
  const user = await findUserById(req.user.sub);
  if (!user) {
    return res.status(404).json({ error: "User not found." });
  }

  return res.json({
    user: cleanUser(user),
  });
});

module.exports = router;
