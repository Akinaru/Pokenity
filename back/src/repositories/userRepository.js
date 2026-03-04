const { prisma } = require("../lib/prisma");

async function findUserByEmail(email) {
  return prisma.user.findUnique({
    where: { email },
  });
}

async function findUserByUsername(username) {
  return prisma.user.findUnique({
    where: { username },
  });
}

async function findUserById(id) {
  return prisma.user.findUnique({
    where: { id },
  });
}

async function createUser(newUser) {
  return prisma.user.create({
    data: newUser,
  });
}

module.exports = {
  createUser,
  findUserByEmail,
  findUserById,
  findUserByUsername,
};
