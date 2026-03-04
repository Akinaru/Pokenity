const { verifyToken } = require("../lib/token");

function authRequired(req, res, next) {
  const authHeader = req.headers.authorization || "";
  const [scheme, token] = authHeader.split(" ");

  if (scheme !== "Bearer" || !token) {
    return res.status(401).json({ error: "Missing or invalid Bearer token." });
  }

  const payload = verifyToken(token);
  if (!payload || typeof payload !== "object" || !payload.sub) {
    return res.status(401).json({ error: "Invalid or expired token." });
  }

  req.user = payload;
  return next();
}

module.exports = {
  authRequired,
};
