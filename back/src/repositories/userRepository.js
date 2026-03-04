const { prisma } = require("../lib/prisma");

const userInclude = {
  character: true,
};

async function findUserByEmail(email) {
  return prisma.user.findUnique({
    where: { email },
    include: userInclude,
  });
}

async function findUserByUsername(username) {
  return prisma.user.findUnique({
    where: { username },
    include: userInclude,
  });
}

async function findUserById(id) {
  return prisma.user.findUnique({
    where: { id },
    include: userInclude,
  });
}

async function createUser(newUser) {
  return prisma.user.create({
    data: newUser,
    include: userInclude,
  });
}

async function listUsers() {
  return prisma.user.findMany({
    include: userInclude,
    orderBy: {
      createdAt: "desc",
    },
  });
}

async function updateUser(id, data) {
  return prisma.user.update({
    where: { id },
    data,
    include: userInclude,
  });
}

async function deleteUser(id) {
  return prisma.user.delete({
    where: { id },
    include: userInclude,
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
