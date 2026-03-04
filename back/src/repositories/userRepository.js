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

async function listUsers() {
  return prisma.user.findMany({
    orderBy: {
      createdAt: "desc",
    },
  });
}

async function updateUser(id, data) {
  return prisma.user.update({
    where: { id },
    data,
  });
}

async function deleteUser(id) {
  return prisma.user.delete({
    where: { id },
  });
}

module.exports = {
  createUser,
  deleteUser,
  findUserByEmail,
  findUserById,
  findUserByUsername,
  listUsers,
  updateUser,
};
