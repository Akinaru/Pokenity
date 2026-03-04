const bcrypt = require("bcryptjs");

async function createPasswordHash(password) {
  return bcrypt.hash(password, 10);
}

async function verifyPassword(password, storedHash) {
  if (!storedHash) {
    return false;
  }
  return bcrypt.compare(password, storedHash);
}

module.exports = {
  createPasswordHash,
  verifyPassword,
};
