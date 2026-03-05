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

  const usersBadge = qs("#usersBadge");
  const usersCount = qs("#usersCount");
  const usersSearch = qs("#usersSearch");
  const usersTableWrap = qs("#usersTableWrap");

  const userModal = qs("#userModal");
  const userModalTitle = qs("#userModalTitle");
  const userModalMeta = qs("#userModalMeta");
  const userPasswordHint = qs("#userPasswordHint");
  const userForm = qs("#userForm");
  const submitUserFormBtn = qs("#submitUserForm");
  const openCreateUserModalBtn = qs("#openCreateUserModal");

  const inventoryModal = qs("#inventoryModal");
  const inventoryModalTitle = qs("#inventoryModalTitle");
  const inventoryModalMeta = qs("#inventoryModalMeta");
  const inventoryTableWrap = qs("#inventoryTableWrap");

  bindModal(userModal);
  bindModal(inventoryModal);

  const state = {
    users: [],
    characters: [],
    mode: "create",
    filter: "",
    editingUser: null,
  };

  function userCharacterName(user) {
    return user.character?.name || "-";
  }

  function normalizedText(value) {
    return String(value || "").trim().toLowerCase();
  }

  function filteredUsers() {
    const query = normalizedText(state.filter);
    if (!query) {
      return state.users;
    }

    return state.users.filter((user) => {
      return [user.username, user.email, userCharacterName(user)]
        .map(normalizedText)
        .some((value) => value.includes(query));
    });
  }

  function renderUserCharacterOptions(selectedId = "") {
    const placeholderLabel =
      state.mode === "create"
        ? "Selection automatique"
        : "Conserver le personnage actuel";

    const options = [
      `<option value="">${escapeHtml(placeholderLabel)}</option>`,
      ...state.characters.map((character) => {
        const selected = selectedId === character.id ? "selected" : "";
        return `<option value="${character.id}" ${selected}>${escapeHtml(character.name)}</option>`;
      }),
    ];

    userForm.characterId.innerHTML = options.join("");
  }

  function renderUsersTable() {
    const rowsData = filteredUsers();
    usersBadge.textContent = `${state.users.length} users`;
    usersCount.textContent = String(rowsData.length);

    if (!rowsData.length) {
      usersTableWrap.innerHTML = '<div class="bo-empty">Aucun utilisateur trouve.</div>';
      return;
    }

    const rows = rowsData
      .map((user) => {
        return `
          <tr>
            <td>
              <strong>${escapeHtml(user.username)}</strong>
              <div class="bo-meta">${escapeHtml(user.id)}</div>
            </td>
            <td>${escapeHtml(user.email)}</td>
            <td><span class="bo-pill">${Number(user.xp || 0)}</span></td>
            <td>${escapeHtml(userCharacterName(user))}</td>
            <td>${formatDate(user.createdAt)}</td>
            <td>
              <div class="bo-actions">
                <button class="bo-btn soft" data-action="inventory" data-id="${user.id}" type="button">Inventaire</button>
                <button class="bo-btn soft" data-action="edit" data-id="${user.id}" type="button">Editer</button>
                <button class="bo-btn danger" data-action="delete" data-id="${user.id}" type="button">Supprimer</button>
              </div>
            </td>
          </tr>
        `;
      })
      .join("");

    usersTableWrap.innerHTML = `
      <table class="bo-table">
        <thead>
          <tr>
            <th>User</th>
            <th>Email</th>
            <th>XP</th>
            <th>Character</th>
            <th>Created</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>${rows}</tbody>
      </table>
    `;
  }

  function openCreateUserModal() {
    state.mode = "create";
    state.editingUser = null;

    userModalTitle.textContent = "Creer un utilisateur";
    userModalMeta.textContent = "Creation complete avec username, email, password, xp et personnage.";
    userPasswordHint.textContent = "Mot de passe requis a la creation (min 6 caracteres).";
    submitUserFormBtn.textContent = "Creer";

    userForm.reset();
    userForm.xp.value = "0";
    renderUserCharacterOptions("");
    openModal(userModal, "#userUsername");
  }

  function openEditUserModal(user) {
    state.mode = "edit";
    state.editingUser = user;

    userModalTitle.textContent = `Editer ${user.username}`;
    userModalMeta.textContent = `Modification complete de ${user.username} (${user.email}).`;
    userPasswordHint.textContent = "Laisser vide pour conserver le mot de passe actuel.";
    submitUserFormBtn.textContent = "Enregistrer";

    userForm.username.value = user.username;
    userForm.email.value = user.email;
    userForm.password.value = "";
    userForm.xp.value = String(Number(user.xp || 0));
    renderUserCharacterOptions(user.characterId || "");

    openModal(userModal, "#userUsername");
  }

  function buildCreatePayload() {
    const username = String(userForm.username.value || "").trim();
    const email = String(userForm.email.value || "").trim();
    const password = String(userForm.password.value || "");
    const xpValue = Number(userForm.xp.value);
    const characterId = String(userForm.characterId.value || "").trim();

    if (!username || !email || !password) {
      throw new Error("Username, email et password sont requis.");
    }

    if (!Number.isInteger(xpValue) || xpValue < 0) {
      throw new Error("XP doit etre un entier >= 0.");
    }

    return {
      username,
      email,
      password,
      xp: xpValue,
      ...(characterId ? { characterId } : {}),
    };
  }

  function buildEditPayload() {
    if (!state.editingUser) {
      throw new Error("Aucun utilisateur selectionne pour edition.");
    }

    const payload = {};
    const username = String(userForm.username.value || "").trim();
    const email = String(userForm.email.value || "").trim();
    const password = String(userForm.password.value || "");
    const characterId = String(userForm.characterId.value || "").trim();
    const xpValue = Number(userForm.xp.value);

    if (!username || !email) {
      throw new Error("Username et email sont requis.");
    }

    if (!Number.isInteger(xpValue) || xpValue < 0) {
      throw new Error("XP doit etre un entier >= 0.");
    }

    if (username !== state.editingUser.username) {
      payload.username = username;
    }

    if (email !== state.editingUser.email) {
      payload.email = email;
    }

    if (password) {
      payload.password = password;
    }

    if (xpValue !== Number(state.editingUser.xp || 0)) {
      payload.xp = xpValue;
    }

    if (characterId && characterId !== (state.editingUser.characterId || "")) {
      payload.characterId = characterId;
    }

    if (!Object.keys(payload).length) {
      throw new Error("Aucune modification detectee.");
    }

    return payload;
  }

  function renderInventory(inventory = []) {
    if (!inventory.length) {
      inventoryTableWrap.innerHTML = '<div class="bo-empty">Inventaire vide.</div>';
      return;
    }

    const rows = inventory
      .map((item) => {
        return `
          <tr>
            <td><span class="bo-pill">${escapeHtml(item.resourceType)}</span></td>
            <td>#${item.resourceId}</td>
            <td>${escapeHtml(item.resourceName)}</td>
            <td>${item.quantity}</td>
            <td>${formatDate(item.lastObtainedAt)}</td>
          </tr>
        `;
      })
      .join("");

    inventoryTableWrap.innerHTML = `
      <table class="bo-table">
        <thead>
          <tr>
            <th>Type</th>
            <th>ID</th>
            <th>Nom</th>
            <th>Quantite</th>
            <th>Derniere obtention</th>
          </tr>
        </thead>
        <tbody>${rows}</tbody>
      </table>
    `;
  }

  async function openInventoryModal(user) {
    inventoryModalTitle.textContent = `Inventaire de ${user.username}`;
    inventoryModalMeta.textContent = `${user.email} - XP ${Number(user.xp || 0)}`;
    inventoryTableWrap.innerHTML = '<div class="bo-empty">Chargement...</div>';
    openModal(inventoryModal);

    try {
      const data = await api(`/api/inventory/users/${user.id}`);
      renderInventory(data.inventory || []);
    } catch (error) {
      inventoryTableWrap.innerHTML = '<div class="bo-empty">Impossible de charger l\'inventaire.</div>';
      notify(error.message, "err");
    }
  }

  async function loadUsers() {
    const data = await api("/api/users");
    state.users = data.users || [];
    renderUsersTable();
  }

  async function loadCharacters() {
    const data = await api("/api/characters");
    state.characters = data.characters || [];
  }

  openCreateUserModalBtn.addEventListener("click", openCreateUserModal);

  usersSearch.addEventListener("input", () => {
    state.filter = usersSearch.value;
    renderUsersTable();
  });

  submitUserFormBtn.addEventListener("click", async () => {
    try {
      if (state.mode === "create") {
        const payload = buildCreatePayload();
        await api("/api/users", {
          method: "POST",
          body: JSON.stringify(payload),
        });
        closeModal(userModal);
        await loadUsers();
        notify("Utilisateur cree.");
        return;
      }

      const payload = buildEditPayload();
      await api(`/api/users/${state.editingUser.id}`, {
        method: "PATCH",
        body: JSON.stringify(payload),
      });
      closeModal(userModal);
      await loadUsers();
      notify("Utilisateur modifie.");
    } catch (error) {
      notify(error.message, "err");
    }
  });

  usersTableWrap.addEventListener("click", async (event) => {
    const button = event.target.closest("button[data-action]");
    if (!button) {
      return;
    }

    const userId = String(button.dataset.id || "");
    const user = state.users.find((entry) => entry.id === userId);
    if (!user) {
      return;
    }

    if (button.dataset.action === "edit") {
      openEditUserModal(user);
      return;
    }

    if (button.dataset.action === "inventory") {
      await openInventoryModal(user);
      return;
    }

    if (button.dataset.action === "delete") {
      if (!window.confirm(`Supprimer l'utilisateur ${user.username} ?`)) {
        return;
      }

      try {
        await api(`/api/users/${user.id}`, { method: "DELETE" });
        await loadUsers();
        notify("Utilisateur supprime.");
      } catch (error) {
        notify(error.message, "err");
      }
    }
  });

  Promise.all([loadCharacters(), loadUsers()]).catch((error) => notify(error.message, "err"));
})();
