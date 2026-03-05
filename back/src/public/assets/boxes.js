(() => {
  const {
    activateNav,
    api,
    bindModal,
    closeModal,
    escapeHtml,
    notify,
    openModal,
    qs,
    qsa,
    toTitleCase,
  } = window.AdminCommon;

  activateNav();

  const boxesBadge = qs("#boxesBadge");
  const boxesCount = qs("#boxesCount");
  const boxesSearch = qs("#boxesSearch");
  const boxesTableWrap = qs("#boxesTableWrap");

  const boxModal = qs("#boxModal");
  const boxModalTitle = qs("#boxModalTitle");
  const boxModalMeta = qs("#boxModalMeta");
  const boxForm = qs("#boxForm");
  const submitBoxFormBtn = qs("#submitBoxForm");
  const openCreateBoxModalBtn = qs("#openCreateBoxModal");

  const boxNameInput = qs("#boxNameInput");
  const pokeballSearchInput = qs("#pokeballSearchInput");
  const pokeballGrid = qs("#pokeballGrid");
  const selectedPokeballText = qs("#selectedPokeballText");
  const addDropBtn = qs("#addDropBtn");
  const dropRows = qs("#dropRows");
  const dropRateTotal = qs("#dropRateTotal");

  bindModal(boxModal);

  const state = {
    boxes: [],
    filter: "",
    mode: "create",
    editingBox: null,
    pokeballs: [],
    pokeballRenderList: [],
    selectedPokeball: null,
  };

  function debounce(callback, delay = 300) {
    let timer;
    return (...args) => {
      window.clearTimeout(timer);
      timer = window.setTimeout(() => callback(...args), delay);
    };
  }

  function normalizedText(value) {
    return String(value || "").trim().toLowerCase();
  }

  function formatRate(value) {
    const numeric = Number(value || 0);
    return Number.isInteger(numeric) ? `${numeric}%` : `${numeric.toFixed(2)}%`;
  }

  function entryImageUrl(resourceType, resourceId, resourceName) {
    if (resourceType === "POKEMON") {
      return `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${resourceId}.png`;
    }

    if (resourceType === "ITEM" || resourceType === "MACHINE") {
      return `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/items/${encodeURIComponent(
        resourceName
      )}.png`;
    }

    return "";
  }

  function entryFallback(resourceType) {
    if (resourceType === "POKEMON") {
      return "PKM";
    }
    if (resourceType === "ITEM") {
      return "ITM";
    }
    return "MCH";
  }

  function filteredBoxes() {
    const query = normalizedText(state.filter);
    if (!query) {
      return state.boxes;
    }

    return state.boxes.filter((box) => {
      const inName = normalizedText(box.name).includes(query);
      const inEntries = (box.entries || []).some((entry) => {
        return normalizedText(entry.resourceName).includes(query);
      });

      return inName || inEntries;
    });
  }

  function renderDropVisuals(entries) {
    if (!entries.length) {
      return '<span class="bo-muted">Aucun drop</span>';
    }

    const sorted = [...entries].sort((a, b) => {
      if (a.dropRate !== b.dropRate) {
        return a.dropRate - b.dropRate;
      }
      return a.resourceId - b.resourceId;
    });

    return `
      <div class="bo-drop-grid">
        ${sorted
          .map((entry) => {
            const image = entryImageUrl(entry.resourceType, entry.resourceId, entry.resourceName);
            const label = toTitleCase(entry.resourceName);

            return `
              <article class="bo-drop-card" title="${escapeHtml(
                `${entry.resourceType} #${entry.resourceId} ${label} - ${formatRate(entry.dropRate)}`
              )}">
                <span class="bo-drop-badge">${formatRate(entry.dropRate)}</span>
                <img src="${escapeHtml(image)}" alt="${escapeHtml(label)}" onerror="this.closest('.bo-drop-card').classList.add('no-image')" />
                <div class="bo-drop-fallback">${entryFallback(entry.resourceType)}</div>
                <div class="bo-drop-name">${escapeHtml(label)}</div>
              </article>
            `;
          })
          .join("")}
      </div>
    `;
  }

  function renderBoxesTable() {
    const rowsData = filteredBoxes();

    boxesBadge.textContent = `${state.boxes.length} boxes`;
    boxesCount.textContent = String(rowsData.length);

    if (!rowsData.length) {
      boxesTableWrap.innerHTML = '<div class="bo-empty">Aucune box trouvee.</div>';
      return;
    }

    const rows = rowsData
      .map((box) => {
        return `
          <tr>
            <td>
              <div style="display:grid;grid-template-columns:48px 1fr;gap:10px;align-items:center;">
                <img class="bo-thumb" src="${escapeHtml(box.pokeballImage)}" alt="${escapeHtml(
                  box.name
                )}" onerror="this.style.visibility='hidden'" />
                <div>
                  <strong>${escapeHtml(box.name)}</strong>
                  <div class="bo-meta">${escapeHtml(box.id)}</div>
                </div>
              </div>
            </td>
            <td>${renderDropVisuals(box.entries || [])}</td>
            <td><span class="bo-pill">${formatRate(box.totalDropRate || 0)}</span></td>
            <td>
              <div class="bo-actions">
                <button class="bo-btn soft" data-action="edit" data-id="${box.id}" type="button">Editer</button>
                <button class="bo-btn danger" data-action="delete" data-id="${box.id}" type="button">Supprimer</button>
              </div>
            </td>
          </tr>
        `;
      })
      .join("");

    boxesTableWrap.innerHTML = `
      <table class="bo-table">
        <thead>
          <tr>
            <th>Box</th>
            <th>Drops</th>
            <th>Total</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>${rows}</tbody>
      </table>
    `;
  }

  function setSelectedPokeball(pokeball) {
    state.selectedPokeball = pokeball;

    if (!pokeball) {
      selectedPokeballText.textContent = "Aucune pokeball selectionnee.";
      renderPokeballGrid();
      return;
    }

    selectedPokeballText.textContent = `Pokeball selectionnee: ${toTitleCase(pokeball.name)} ${
      pokeball.id ? `(#${pokeball.id})` : ""
    }`;

    renderPokeballGrid();
  }

  function renderPokeballGrid() {
    const renderList = [...state.pokeballs];

    if (
      state.selectedPokeball &&
      !renderList.some((entry) => entry.image === state.selectedPokeball.image)
    ) {
      renderList.unshift(state.selectedPokeball);
    }

    state.pokeballRenderList = renderList.map((entry, index) => ({
      ...entry,
      _key: `${entry.id || "custom"}-${index}`,
    }));

    if (!state.pokeballRenderList.length) {
      pokeballGrid.innerHTML = '<div class="bo-empty">Aucune pokeball trouvee.</div>';
      return;
    }

    pokeballGrid.innerHTML = state.pokeballRenderList
      .map((pokeball) => {
        const selected =
          state.selectedPokeball && state.selectedPokeball.image === pokeball.image
            ? "selected"
            : "";

        return `
          <button class="bo-pokeball-card ${selected}" data-ball-key="${pokeball._key}" type="button">
            <img src="${escapeHtml(pokeball.image)}" alt="${escapeHtml(pokeball.name)}" />
            <span class="bo-help">${escapeHtml(toTitleCase(pokeball.name))}</span>
          </button>
        `;
      })
      .join("");
  }

  async function loadPokeballs(search = "") {
    const query = encodeURIComponent(search || "");
    const data = await api(`/api/catalog/pokeballs?search=${query}&limit=60`);

    state.pokeballs = data.results || [];

    if (!state.selectedPokeball && state.pokeballs.length) {
      setSelectedPokeball(state.pokeballs[0]);
      return;
    }

    if (state.selectedPokeball) {
      const matching = state.pokeballs.find(
        (entry) => entry.image === state.selectedPokeball.image
      );
      if (matching) {
        state.selectedPokeball = matching;
      }
    }

    renderPokeballGrid();
  }

  function updateDropRateTotal() {
    const total = qsa('[data-field="drop-rate"]', dropRows).reduce((sum, input) => {
      return sum + Number(input.value || 0);
    }, 0);

    const rounded = Number(total.toFixed(2));
    dropRateTotal.textContent = `Total drop rate: ${rounded}% (doit etre 100%)`;
    dropRateTotal.classList.toggle("error", Math.abs(total - 100) > 0.001);
  }

  function setDropSelection(row, data) {
    row.dataset.resourceType = data.resourceType;
    row.dataset.resourceId = String(data.resourceId);
    row.dataset.resourceName = data.resourceName;

    const selectedInfo = qs('[data-role="selected-info"]', row);
    selectedInfo.innerHTML = `
      <span class="bo-pill">${escapeHtml(data.resourceType)}</span>
      #${data.resourceId} ${escapeHtml(toTitleCase(data.resourceName))}
    `;

    const results = qs('[data-role="search-results"]', row);
    results.innerHTML = "";
  }

  function renderSearchResults(row, results) {
    const container = qs('[data-role="search-results"]', row);

    if (!results.length) {
      container.innerHTML = '<div class="bo-empty">Aucun resultat.</div>';
      return;
    }

    container.innerHTML = results
      .map((entry) => {
        return `
          <button
            class="bo-search-row"
            type="button"
            data-action="pick-drop"
            data-id="${entry.id}"
            data-name="${escapeHtml(entry.name)}"
          >
            <img src="${escapeHtml(entry.image || "")}" alt="" onerror="this.style.visibility='hidden'" />
            <div>
              <strong>${escapeHtml(toTitleCase(entry.name))}</strong>
            </div>
            <span class="bo-pill">#${entry.id}</span>
          </button>
        `;
      })
      .join("");
  }

  async function searchRowResource(row) {
    const resourceType = String(qs('[data-field="resource-type"]', row).value || "POKEMON");
    const term = String(qs('[data-field="search-term"]', row).value || "").trim();

    const endpoint = resourceType === "POKEMON" ? "pokemon" : "items";
    const data = await api(`/api/catalog/${endpoint}?search=${encodeURIComponent(term)}&limit=12`);

    renderSearchResults(row, data.results || []);
  }

  function createDropRow(initial = null) {
    const row = document.createElement("article");
    row.className = "bo-drop-row";
    row.innerHTML = `
      <div class="bo-form-grid two">
        <div class="bo-field">
          <label>Type</label>
          <select class="bo-select" data-field="resource-type">
            <option value="POKEMON">POKEMON</option>
            <option value="ITEM">ITEM</option>
            <option value="MACHINE">MACHINE</option>
          </select>
        </div>
        <div class="bo-field">
          <label>Drop rate (%)</label>
          <input class="bo-input" data-field="drop-rate" type="number" min="0.01" step="0.01" required />
        </div>
      </div>

      <div class="bo-field">
        <label>Recherche ressource</label>
        <input class="bo-input" data-field="search-term" type="search" placeholder="Nom ou ID" />
      </div>

      <div class="bo-search-results" data-role="search-results"></div>
      <p class="bo-meta" data-role="selected-info">Aucune ressource selectionnee.</p>

      <div class="bo-actions">
        <button class="bo-btn danger" data-action="remove-drop" type="button">Supprimer ce drop</button>
      </div>
    `;

    const typeSelect = qs('[data-field="resource-type"]', row);
    const searchInput = qs('[data-field="search-term"]', row);
    const rateInput = qs('[data-field="drop-rate"]', row);

    const debouncedSearch = debounce(() => {
      searchRowResource(row).catch((error) => notify(error.message, "err"));
    }, 280);

    typeSelect.addEventListener("change", () => {
      row.dataset.resourceType = "";
      row.dataset.resourceId = "";
      row.dataset.resourceName = "";
      qs('[data-role="selected-info"]', row).textContent = "Aucune ressource selectionnee.";
      debouncedSearch();
    });

    searchInput.addEventListener("input", () => debouncedSearch());
    rateInput.addEventListener("input", updateDropRateTotal);

    row.addEventListener("click", (event) => {
      const removeButton = event.target.closest('[data-action="remove-drop"]');
      if (removeButton) {
        row.remove();
        if (!dropRows.children.length) {
          createDropRow();
        }
        updateDropRateTotal();
        return;
      }

      const pickButton = event.target.closest('[data-action="pick-drop"]');
      if (!pickButton) {
        return;
      }

      setDropSelection(row, {
        resourceType: String(typeSelect.value || "POKEMON"),
        resourceId: Number(pickButton.dataset.id || 0),
        resourceName: String(pickButton.dataset.name || ""),
      });
    });

    if (initial) {
      typeSelect.value = initial.resourceType;
      rateInput.value = String(initial.dropRate);
      setDropSelection(row, {
        resourceType: initial.resourceType,
        resourceId: initial.resourceId,
        resourceName: initial.resourceName,
      });
    }

    dropRows.appendChild(row);
    updateDropRateTotal();
  }

  function collectEntries() {
    const rows = qsa(".bo-drop-row", dropRows);

    if (!rows.length) {
      throw new Error("Ajoute au moins un drop.");
    }

    const entries = rows.map((row) => {
      const resourceType = String(row.dataset.resourceType || "");
      const resourceId = Number(row.dataset.resourceId || 0);
      const resourceName = String(row.dataset.resourceName || "");
      const dropRate = Number(qs('[data-field="drop-rate"]', row).value || 0);

      if (!resourceType || !resourceId || !resourceName) {
        throw new Error("Chaque ligne doit avoir une ressource selectionnee.");
      }

      if (!Number.isFinite(dropRate) || dropRate <= 0) {
        throw new Error("Chaque drop rate doit etre un nombre positif.");
      }

      return {
        resourceType,
        resourceId,
        dropRate,
      };
    });

    const total = entries.reduce((sum, entry) => sum + entry.dropRate, 0);
    if (Math.abs(total - 100) > 0.001) {
      throw new Error("Le total des drop rates doit etre exactement 100.");
    }

    return entries;
  }

  function openCreateBoxModal() {
    state.mode = "create";
    state.editingBox = null;

    boxModalTitle.textContent = "Creer une box";
    boxModalMeta.textContent = "Creation complete avec pokeball et drops.";
    submitBoxFormBtn.textContent = "Creer";

    boxForm.reset();
    dropRows.innerHTML = "";
    createDropRow();
    createDropRow();
    state.selectedPokeball = null;
    setSelectedPokeball(null);

    openModal(boxModal, "#boxNameInput");
    loadPokeballs(pokeballSearchInput.value).catch((error) => notify(error.message, "err"));
  }

  function openEditBoxModal(box) {
    state.mode = "edit";
    state.editingBox = box;

    boxModalTitle.textContent = `Editer ${box.name}`;
    boxModalMeta.textContent = "Modification complete: nom, pokeball et liste des drops.";
    submitBoxFormBtn.textContent = "Enregistrer";

    boxForm.reset();
    boxNameInput.value = box.name;
    pokeballSearchInput.value = "";

    state.selectedPokeball = {
      id: null,
      name: box.name,
      image: box.pokeballImage,
    };
    setSelectedPokeball(state.selectedPokeball);

    dropRows.innerHTML = "";
    if ((box.entries || []).length) {
      box.entries.forEach((entry) => {
        createDropRow({
          resourceType: entry.resourceType,
          resourceId: entry.resourceId,
          resourceName: entry.resourceName,
          dropRate: entry.dropRate,
        });
      });
    } else {
      createDropRow();
    }

    openModal(boxModal, "#boxNameInput");
    loadPokeballs("").catch((error) => notify(error.message, "err"));
  }

  async function loadBoxes() {
    const data = await api("/api/boxes");
    state.boxes = data.boxes || [];
    renderBoxesTable();
  }

  openCreateBoxModalBtn.addEventListener("click", openCreateBoxModal);

  boxesSearch.addEventListener("input", () => {
    state.filter = boxesSearch.value;
    renderBoxesTable();
  });

  pokeballSearchInput.addEventListener(
    "input",
    debounce(() => {
      loadPokeballs(pokeballSearchInput.value).catch((error) => notify(error.message, "err"));
    }, 280)
  );

  pokeballGrid.addEventListener("click", (event) => {
    const card = event.target.closest("[data-ball-key]");
    if (!card) {
      return;
    }

    const selected = state.pokeballRenderList.find((entry) => entry._key === card.dataset.ballKey);
    if (!selected) {
      return;
    }

    setSelectedPokeball(selected);
  });

  addDropBtn.addEventListener("click", () => createDropRow());

  submitBoxFormBtn.addEventListener("click", async () => {
    try {
      const name = String(boxNameInput.value || "").trim();
      if (!name) {
        throw new Error("Le nom de box est requis.");
      }

      if (!state.selectedPokeball || !state.selectedPokeball.image) {
        throw new Error("Selectionne une pokeball.");
      }

      const entries = collectEntries();
      const payload = {
        name,
        pokeballImage: state.selectedPokeball.image,
        entries,
      };

      if (state.mode === "create") {
        await api("/api/boxes", {
          method: "POST",
          body: JSON.stringify(payload),
        });
        closeModal(boxModal);
        await loadBoxes();
        notify("Box creee.");
        return;
      }

      await api(`/api/boxes/${state.editingBox.id}`, {
        method: "PATCH",
        body: JSON.stringify(payload),
      });
      closeModal(boxModal);
      await loadBoxes();
      notify("Box modifiee.");
    } catch (error) {
      notify(error.message, "err");
    }
  });

  boxesTableWrap.addEventListener("click", async (event) => {
    const button = event.target.closest("button[data-action]");
    if (!button) {
      return;
    }

    const boxId = String(button.dataset.id || "");
    const box = state.boxes.find((entry) => entry.id === boxId);
    if (!box) {
      return;
    }

    if (button.dataset.action === "edit") {
      openEditBoxModal(box);
      return;
    }

    if (button.dataset.action === "delete") {
      if (!window.confirm(`Supprimer la box ${box.name} ?`)) {
        return;
      }

      try {
        await api(`/api/boxes/${box.id}`, { method: "DELETE" });
        await loadBoxes();
        notify("Box supprimee.");
      } catch (error) {
        notify(error.message, "err");
      }
    }
  });

  Promise.all([loadBoxes(), loadPokeballs()]).catch((error) => notify(error.message, "err"));
})();
