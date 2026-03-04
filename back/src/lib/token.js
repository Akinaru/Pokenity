const jwt = require("jsonwebtoken");
const { JWT_EXPIRES_IN, JWT_SECRET } = require("../config/env");

function signToken(payload) {
  return jwt.sign(payload, JWT_SECRET, {
    algorithm: "HS256",
    expiresIn: JWT_EXPIRES_IN,
  });
}

function verifyToken(token) {
  try {
    return jwt.verify(token, JWT_SECRET, {
      algorithms: ["HS256"],
    });
  } catch {
    return null;
  }
}

module.exports = {
  signToken,
  verifyToken,
};
