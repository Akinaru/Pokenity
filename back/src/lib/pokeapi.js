const { POKEAPI_BASE_URL } = require("../config/env");

class PokeApiError extends Error {
  constructor(message, status = 500) {
    super(message);
    this.status = status;
  }
}

async function fetchPokeApi(path) {
  let response;
  try {
    response = await fetch(`${POKEAPI_BASE_URL}${path}`);
  } catch {
    throw new PokeApiError("PokeAPI is unreachable right now.", 502);
  }

  if (response.status === 404) {
    return null;
  }

  if (!response.ok) {
    throw new PokeApiError("Failed to fetch data from PokeAPI.", 502);
  }

  return response.json();
}

async function resolveDropResource(resourceType, resourceId) {
  if (resourceType === "POKEMON") {
    const pokemon = await fetchPokeApi(`/pokemon/${resourceId}`);
    if (!pokemon) {
      throw new PokeApiError(`Pokemon #${resourceId} not found on PokeAPI.`, 400);
    }

    return {
      resourceType,
      resourceId: pokemon.id,
      resourceName: pokemon.name,
      metadata: {
        sprite: pokemon.sprites?.front_default || null,
      },
    };
  }

  if (resourceType === "ITEM") {
    const item = await fetchPokeApi(`/item/${resourceId}`);
    if (!item) {
      throw new PokeApiError(`Item #${resourceId} not found on PokeAPI.`, 400);
    }

    return {
      resourceType,
      resourceId: item.id,
      resourceName: item.name,
      metadata: {
        sprite: item.sprites?.default || null,
      },
    };
  }

  if (resourceType === "MACHINE") {
    const machine = await fetchPokeApi(`/machine/${resourceId}`);
    if (!machine) {
      throw new PokeApiError(`Machine #${resourceId} not found on PokeAPI.`, 400);
    }

    return {
      resourceType,
      resourceId: machine.id,
      resourceName: machine.item?.name || machine.move?.name || `machine-${machine.id}`,
      metadata: {
        itemName: machine.item?.name || null,
        moveName: machine.move?.name || null,
      },
    };
  }

  throw new PokeApiError("resourceType must be POKEMON, ITEM or MACHINE.", 400);
}

module.exports = {
  PokeApiError,
  resolveDropResource,
};
