(() => {
  const {
    activateNav,
    api,
    bindModal,
    closeModal,
    escapeHtml,
    formatDate,
    notify,
    openModal,
    qs,
  } = window.AdminCommon;

  activateNav();

  const charactersBadge = qs("#charactersBadge");
  const charactersCount = qs("#charactersCount");
  const charactersSearch = qs("#charactersSearch");
  const tableWrap = qs("#charactersTableWrap");

  const characterModal = qs("#characterModal");
  const characterModalTitle = qs("#characterModalTitle");
  const characterModalMeta = qs("#characterModalMeta");
  const characterForm = qs("#characterForm");
  const submitCharacterFormBtn = qs("#submitCharacterForm");
  const openCreateCharacterModalBtn = qs("#openCreateCharacterModal");

  bindModal(characterModal);

  const state = {
    characters: [],
    mode: "create",
    filter: "",
    editingCharacter: null,
  };

  function normalizedText(value) {
    return String(value || "").trim().toLowerCase();
  }

  function filteredCharacters() {
    const query = normalizedText(state.filter);
    if (!query) {
      return state.characters;
    }

    return state.characters.filter((character) => {
      const avatar = character.avatarFileName || character.avatarUrl;
      const image = character.imageFileName || character.imageUrl;

      return [character.name, avatar, image]
        .map(normalizedText)
        .some((value) => value.includes(query));
    });
  }

  function openCreateCharacterModal() {
    state.mode = "create";
    state.editingCharacter = null;

    characterModalTitle.textContent = "Creer un personnage";
    characterModalMeta.textContent = "Creation complete avec les 3 champs requis.";
    submitCharacterFormBtn.textContent = "Creer";

    characterForm.reset();
    openModal(characterModal, "#characterName");
  }

  function openEditCharacterModal(character) {
    state.mode = "edit";
    state.editingCharacter = character;

    characterModalTitle.textContent = `Editer ${character.name}`;
    characterModalMeta.textContent = "Modifie le nom ou les fichiers image directement.";
    submitCharacterFormBtn.textContent = "Enregistrer";

    characterForm.name.value = character.name;
    characterForm.avatarFileName.value = character.avatarFileName || character.avatarUrl || "";
    characterForm.imageFileName.value = character.imageFileName || character.imageUrl || "";

    openModal(characterModal, "#characterName");
  }

  function renderTable() {
    const rowsData = filteredCharacters();

    charactersBadge.textContent = `${state.characters.length} characters`;
    charactersCount.textContent = String(rowsData.length);

    if (!rowsData.length) {
      tableWrap.innerHTML = '<div class="bo-empty">Aucun personnage trouve.</div>';
      return;
    }

    const rows = rowsData
      .map((character) => {
        const avatar = character.avatarFileName || character.avatarUrl || "-";
        const image = character.imageFileName || character.imageUrl || "-";

        return `
          <tr>
            <td>
              <strong>${escapeHtml(character.name)}</strong>
              <div class="bo-meta">${escapeHtml(character.id)}</div>
            </td>
            <td><span class="bo-pill">${escapeHtml(avatar)}</span></td>
            <td><span class="bo-pill">${escapeHtml(image)}</span></td>
            <td>${formatDate(character.updatedAt || character.createdAt)}</td>
            <td>
              <div class="bo-actions">
                <button class="bo-btn soft" data-action="edit" data-id="${character.id}" type="button">Editer</button>
                <button class="bo-btn danger" data-action="delete" data-id="${character.id}" type="button">Supprimer</button>
              </div>
            </td>
          </tr>
        `;
      })
      .join("");

    tableWrap.innerHTML = `
      <table class="bo-table">
        <thead>
          <tr>
            <th>Character</th>
            <th>Avatar file</th>
            <th>Main image file</th>
            <th>Updated</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>${rows}</tbody>
      </table>
    `;
  }

  function buildCreatePayload() {
    const name = String(characterForm.name.value || "").trim();
    const avatarFileName = String(characterForm.avatarFileName.value || "").trim();
    const imageFileName = String(characterForm.imageFileName.value || "").trim();

    if (!name || !avatarFileName || !imageFileName) {
      throw new Error("Name, avatar filename et main image filename sont requis.");
    }

    return { name, avatarFileName, imageFileName };
  }

  function buildEditPayload() {
    if (!state.editingCharacter) {
      throw new Error("Aucun personnage selectionne pour edition.");
    }

    const payload = {};
    const name = String(characterForm.name.value || "").trim();
    const avatarFileName = String(characterForm.avatarFileName.value || "").trim();
    const imageFileName = String(characterForm.imageFileName.value || "").trim();

    if (!name || !avatarFileName || !imageFileName) {
      throw new Error("Name, avatar filename et main image filename sont requis.");
    }

    if (name !== state.editingCharacter.name) {
      payload.name = name;
    }

    if (avatarFileName !== (state.editingCharacter.avatarFileName || state.editingCharacter.avatarUrl || "")) {
      payload.avatarFileName = avatarFileName;
    }

    if (imageFileName !== (state.editingCharacter.imageFileName || state.editingCharacter.imageUrl || "")) {
      payload.imageFileName = imageFileName;
    }

    if (!Object.keys(payload).length) {
      throw new Error("Aucune modification detectee.");
    }

    return payload;
  }

  async function loadCharacters() {
    const data = await api("/api/characters");
    state.characters = data.characters || [];
    renderTable();
  }

  openCreateCharacterModalBtn.addEventListener("click", openCreateCharacterModal);

  charactersSearch.addEventListener("input", () => {
    state.filter = charactersSearch.value;
    renderTable();
  });

  submitCharacterFormBtn.addEventListener("click", async () => {
    try {
      if (state.mode === "create") {
        const payload = buildCreatePayload();
        await api("/api/characters", {
          method: "POST",
          body: JSON.stringify(payload),
        });
        closeModal(characterModal);
        await loadCharacters();
        notify("Character cree.");
        return;
      }

      const payload = buildEditPayload();
      await api(`/api/characters/${state.editingCharacter.id}`, {
        method: "PATCH",
        body: JSON.stringify(payload),
      });
      closeModal(characterModal);
      await loadCharacters();
      notify("Character modifie.");
    } catch (error) {
      notify(error.message, "err");
    }
  });

  tableWrap.addEventListener("click", async (event) => {
    const button = event.target.closest("button[data-action]");
    if (!button) {
      return;
    }

    const characterId = String(button.dataset.id || "");
    const character = state.characters.find((entry) => entry.id === characterId);
    if (!character) {
      return;
    }

    if (button.dataset.action === "edit") {
      openEditCharacterModal(character);
      return;
    }

    if (button.dataset.action === "delete") {
      if (!window.confirm(`Supprimer le character ${character.name} ?`)) {
        return;
      }

      try {
        await api(`/api/characters/${character.id}`, { method: "DELETE" });
        await loadCharacters();
        notify("Character supprime.");
      } catch (error) {
        notify(error.message, "err");
      }
    }
  });

  loadCharacters().catch((error) => notify(error.message, "err"));
})();
