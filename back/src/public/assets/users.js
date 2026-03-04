(() => {
  const { activateNav, api, escapeHtml, formatDate, notify, qs } = window.AdminCommon;
  activateNav();

  const createForm = qs("#createUserForm");
  const editForm = qs("#editUserForm");
  const cancelEditBtn = qs("#cancelEditBtn");
  const editMeta = qs("#editMeta");
  const usersCount = qs("#usersCount");
  const usersBadge = qs("#usersBadge");
  const usersTableWrap = qs("#usersTableWrap");
  const inventoryTableWrap = qs("#inventoryTableWrap");

  let users = [];
  let selectedUserId = null;

  function resetEditForm() {
    selectedUserId = null;
    editForm.reset();
    editMeta.textContent = "No user selected.";
    inventoryTableWrap.innerHTML = '<div class="bo-empty">Inventory not loaded.</div>';
  }

  function renderUsersTable() {
    usersCount.textContent = String(users.length);
    usersBadge.textContent = `${users.length} users`;

    if (!users.length) {
      usersTableWrap.innerHTML = '<div class="bo-empty">No users yet.</div>';
      return;
    }

    const rows = users
      .map((user) => {
        return `
          <tr>
            <td><strong>${escapeHtml(user.username)}</strong></td>
            <td>${escapeHtml(user.email)}</td>
            <td>${formatDate(user.createdAt)}</td>
            <td>
              <div class="bo-actions">
                <button class="bo-btn soft" data-action="inventory" data-id="${user.id}" type="button">Inventory</button>
                <button class="bo-btn soft" data-action="edit" data-id="${user.id}" type="button">Edit</button>
                <button class="bo-btn danger" data-action="delete" data-id="${user.id}" type="button">Delete</button>
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
            <th>Username</th>
            <th>Email</th>
            <th>Created at</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>${rows}</tbody>
      </table>
    `;
  }

  function renderInventoryTable(inventory) {
    if (!inventory.length) {
      inventoryTableWrap.innerHTML = '<div class="bo-empty">Inventory is empty.</div>';
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
            <th>Name</th>
            <th>Qty</th>
            <th>Last obtained</th>
          </tr>
        </thead>
        <tbody>${rows}</tbody>
      </table>
    `;
  }

  async function loadUsers() {
    const data = await api("/api/users");
    users = data.users;
    renderUsersTable();
  }

  async function loadInventory(userId) {
    const data = await api(`/api/inventory/users/${userId}`);
    renderInventoryTable(data.inventory || []);
  }

  createForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    const formData = new FormData(createForm);

    try {
      await api("/api/users", {
        method: "POST",
        body: JSON.stringify({
          username: formData.get("username"),
          email: formData.get("email"),
          password: formData.get("password"),
        }),
      });

      createForm.reset();
      await loadUsers();
      notify("User created.");
    } catch (error) {
      notify(error.message, "err");
    }
  });

  editForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    if (!selectedUserId) {
      notify("Select a user first.", "err");
      return;
    }

    const formData = new FormData(editForm);
    const payload = {};

    if (String(formData.get("username") || "").trim()) {
      payload.username = formData.get("username");
    }
    if (String(formData.get("email") || "").trim()) {
      payload.email = formData.get("email");
    }
    if (String(formData.get("password") || "").trim()) {
      payload.password = formData.get("password");
    }

    if (!Object.keys(payload).length) {
      notify("No changes to save.", "err");
      return;
    }

    try {
      await api(`/api/users/${selectedUserId}`, {
        method: "PATCH",
        body: JSON.stringify(payload),
      });

      await loadUsers();
      await loadInventory(selectedUserId);
      notify("User updated.");
    } catch (error) {
      notify(error.message, "err");
    }
  });

  cancelEditBtn.addEventListener("click", resetEditForm);

  usersTableWrap.addEventListener("click", async (event) => {
    const actionButton = event.target.closest("button[data-action]");
    if (!actionButton) {
      return;
    }

    const user = users.find((entry) => entry.id === actionButton.dataset.id);
    if (!user) {
      return;
    }

    if (actionButton.dataset.action === "edit") {
      selectedUserId = user.id;
      editMeta.textContent = `Editing ${user.username} (${user.email})`;
      editForm.username.value = user.username;
      editForm.email.value = user.email;
      editForm.password.value = "";
      return;
    }

    if (actionButton.dataset.action === "inventory") {
      selectedUserId = user.id;
      editMeta.textContent = `Selected ${user.username} (${user.email})`;
      await loadInventory(user.id);
      return;
    }

    if (actionButton.dataset.action === "delete") {
      if (!confirm(`Delete user ${user.username}?`)) {
        return;
      }

      try {
        await api(`/api/users/${user.id}`, { method: "DELETE" });
        if (selectedUserId === user.id) {
          resetEditForm();
        }
        await loadUsers();
        notify("User deleted.");
      } catch (error) {
        notify(error.message, "err");
      }
    }
  });

  resetEditForm();
  loadUsers().catch((error) => notify(error.message, "err"));
})();
