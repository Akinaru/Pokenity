(() => {
  const { activateNav, api, escapeHtml, formatDate, notify, qs } = window.AdminCommon;
  activateNav();

  const createForm = qs("#createCharacterForm");
  const editForm = qs("#editCharacterForm");
  const cancelEditBtn = qs("#cancelCharacterEditBtn");
  const editMeta = qs("#editCharacterMeta");
  const charactersCount = qs("#charactersCount");
  const charactersBadge = qs("#charactersBadge");
  const tableWrap = qs("#charactersTableWrap");

  let characters = [];
  let selectedCharacterId = null;

  function resetEditForm() {
    selectedCharacterId = null;
    editMeta.textContent = "No character selected.";
    editForm.reset();
  }

  function imageTag(url, alt) {
    if (!url) return '<span class="bo-help">-</span>';
    return `<img src="${escapeHtml(url)}" alt="${escapeHtml(alt)}" class="bo-thumb" />`;
  }

  function renderTable() {
    charactersCount.textContent = String(characters.length);
    charactersBadge.textContent = `${characters.length} characters`;

    if (!characters.length) {
      tableWrap.innerHTML = '<div class="bo-empty">No characters yet.</div>';
      return;
    }

    const rows = characters
      .map((character) => {
        return `
          <tr>
            <td><strong>${escapeHtml(character.name)}</strong></td>
            <td>${imageTag(character.avatarUrl, `${character.name} avatar`)}</td>
            <td>${imageTag(character.imageUrl, `${character.name} image`)}</td>
            <td>${formatDate(character.createdAt)}</td>
            <td>
              <div class="bo-actions">
                <button class="bo-btn soft" data-action="edit" data-id="${character.id}" type="button">Edit</button>
                <button class="bo-btn danger" data-action="delete" data-id="${character.id}" type="button">Delete</button>
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
            <th>Name</th>
            <th>Avatar</th>
            <th>Image</th>
            <th>Created at</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>${rows}</tbody>
      </table>
    `;
  }

  async function loadCharacters() {
    const data = await api("/api/characters");
    characters = data.characters || [];
    renderTable();
  }

  createForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    const formData = new FormData(createForm);

    try {
      await api("/api/characters", {
        method: "POST",
        body: formData,
      });
      createForm.reset();
      await loadCharacters();
      notify("Character created.");
    } catch (error) {
      notify(error.message, "err");
    }
  });

  editForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    if (!selectedCharacterId) {
      notify("Select a character first.", "err");
      return;
    }

    const name = String(editForm.name.value || "").trim();
    const avatarFile = editForm.avatar.files?.[0] || null;
    const imageFile = editForm.image.files?.[0] || null;

    if (!name && !avatarFile && !imageFile) {
      notify("No changes to save.", "err");
      return;
    }

    const payload = new FormData();
    if (name) payload.append("name", name);
    if (avatarFile) payload.append("avatar", avatarFile);
    if (imageFile) payload.append("image", imageFile);

    try {
      await api(`/api/characters/${selectedCharacterId}`, {
        method: "PATCH",
        body: payload,
      });
      await loadCharacters();
      notify("Character updated.");
    } catch (error) {
      notify(error.message, "err");
    }
  });

  cancelEditBtn.addEventListener("click", resetEditForm);

  tableWrap.addEventListener("click", async (event) => {
    const button = event.target.closest("button[data-action]");
    if (!button) return;

    const character = characters.find((entry) => entry.id === button.dataset.id);
    if (!character) return;

    if (button.dataset.action === "edit") {
      selectedCharacterId = character.id;
      editMeta.textContent = `Editing ${character.name}`;
      editForm.name.value = character.name;
      editForm.avatar.value = "";
      editForm.image.value = "";
      return;
    }

    if (button.dataset.action === "delete") {
      if (!confirm(`Delete character ${character.name}?`)) return;
      try {
        await api(`/api/characters/${character.id}`, { method: "DELETE" });
        if (selectedCharacterId === character.id) resetEditForm();
        await loadCharacters();
        notify("Character deleted.");
      } catch (error) {
        notify(error.message, "err");
      }
    }
  });

  resetEditForm();
  loadCharacters().catch((error) => notify(error.message, "err"));
})();

