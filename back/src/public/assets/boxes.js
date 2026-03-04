(() => {
  const { activateNav, api, escapeHtml, notify, qs, qsa, toTitleCase } = window.AdminCommon;
  activateNav();

  const createBoxForm = qs("#createBoxForm");
  const boxNameInput = qs("#boxName");
  const pokeballSearchInput = qs("#pokeballSearch");
  const pokeballGrid = qs("#pokeballGrid");
  const selectedPokeballText = qs("#selectedPokeballText");
  const pokeballImageInput = qs("#pokeballImageInput");
  const dropRows = qs("#dropRows");
  const addDropBtn = qs("#addDropBtn");
  const dropRateTotal = qs("#dropRateTotal");
  const boxesCount = qs("#boxesCount");
  const boxesBadge = qs("#boxesBadge");
  const boxesTableWrap = qs("#boxesTableWrap");

  let pokeballs = [];
  let selectedPokeball = null;
  let boxes = [];

  function debounce(callback, delay = 300) {
    let timer;
    return (...args) => {
      clearTimeout(timer);
      timer = setTimeout(() => callback(...args), delay);
    };
  }

  function selectedRowData(row) {
    const selectedType = row.dataset.resourceType || "";
    const selectedId = Number(row.dataset.resourceId || 0);
    const selectedName = row.dataset.resourceName || "";
    const selectedImage = row.dataset.resourceImage || "";

    return {
      selectedType,
      selectedId,
      selectedName,
      selectedImage,
    };
  }

  function formatRate(value) {
    if (Number.isInteger(value)) {
      return `${value}%`;
    }
    return `${value.toFixed(2)}%`;
  }

  function entryImageUrl(entry) {
    if (entry.resourceType === "POKEMON") {
      return `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${entry.resourceId}.png`;
    }

    if (entry.resourceType === "ITEM" || entry.resourceType === "MACHINE") {
      return `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/items/${encodeURIComponent(
        entry.resourceName
      )}.png`;
    }

    return "";
  }

  function entryFallback(entry) {
    if (entry.resourceType === "POKEMON") {
      return "PKM";
    }
    if (entry.resourceType === "ITEM") {
      return "ITM";
    }
    return "MCH";
  }

  function renderDropVisuals(entries) {
    const sortedEntries = [...entries].sort((a, b) => {
      if (a.dropRate !== b.dropRate) {
        return a.dropRate - b.dropRate;
      }
      return a.resourceId - b.resourceId;
    });

    return `
      <div class="bo-drop-grid">
        ${sortedEntries
          .map((entry) => {
            const imageUrl = entryImageUrl(entry);
            const label = toTitleCase(entry.resourceName);
            return `
              <article class="bo-drop-card" title="${escapeHtml(
                `${entry.resourceType} #${entry.resourceId} ${label} - ${formatRate(entry.dropRate)}`
              )}">
                <span class="bo-drop-badge">${formatRate(entry.dropRate)}</span>
                <img
                  src="${escapeHtml(imageUrl)}"
                  alt="${escapeHtml(label)}"
                  onerror="this.closest('.bo-drop-card').classList.add('no-image')"
                />
                <div class="bo-drop-fallback">${entryFallback(entry)}</div>
                <div class="bo-drop-name">${escapeHtml(label)}</div>
              </article>
            `;
          })
          .join("")}
      </div>
    `;
  }

  function updateDropRateTotal() {
    const total = qsa('[data-field="drop-rate"]', dropRows).reduce((sum, input) => {
      return sum + Number(input.value || 0);
    }, 0);

    dropRateTotal.textContent = `Total drop rate: ${total.toFixed(2)}% (must be 100%)`;
  }

  function renderPokeballs() {
    if (!pokeballs.length) {
      pokeballGrid.innerHTML = '<div class="bo-empty">No pokeballs found.</div>';
      return;
    }

    pokeballGrid.innerHTML = pokeballs
      .map((ball) => {
        const selectedClass = selectedPokeball && selectedPokeball.id === ball.id ? "selected" : "";
        return `
          <button class="bo-pokeball-card ${selectedClass}" data-ball-id="${ball.id}" type="button">
            <img src="${escapeHtml(ball.image)}" alt="${escapeHtml(ball.name)}" />
            <div class="bo-meta">${escapeHtml(toTitleCase(ball.name))}</div>
          </button>
        `;
      })
      .join("");
  }

  function setSelectedPokeball(ball) {
    selectedPokeball = ball;
    pokeballImageInput.value = ball ? ball.image : "";
    selectedPokeballText.textContent = ball
      ? `Selected pokeball: ${toTitleCase(ball.name)} (#${ball.id})`
      : "No pokeball selected.";
    renderPokeballs();
  }

  async function loadPokeballs(search = "") {
    const query = encodeURIComponent(search || "");
    const data = await api(`/api/catalog/pokeballs?search=${query}&limit=48`);
    pokeballs = data.results || [];

    if (!selectedPokeball && pokeballs.length) {
      setSelectedPokeball(pokeballs[0]);
      return;
    }

    if (selectedPokeball) {
      const stillExists = pokeballs.find((ball) => ball.id === selectedPokeball.id);
      if (!stillExists && pokeballs.length) {
        setSelectedPokeball(pokeballs[0]);
        return;
      }
    }

    renderPokeballs();
  }

  function renderDropSearchResults(row, results) {
    const container = qs('[data-role="search-results"]', row);

    if (!results.length) {
      container.innerHTML = '<div class="bo-empty">No results.</div>';
      return;
    }

    container.innerHTML = results
      .map((entry) => {
        return `
          <button class="bo-search-row" type="button" data-pick-id="${entry.id}" data-pick-name="${escapeHtml(
            entry.name
          )}" data-pick-image="${escapeHtml(entry.image || "")}">
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

  function setDropSelection(row, data) {
    row.dataset.resourceType = data.type;
    row.dataset.resourceId = String(data.id);
    row.dataset.resourceName = data.name;
    row.dataset.resourceImage = data.image || "";

    const selectedInfo = qs('[data-role="selected-info"]', row);
    selectedInfo.innerHTML = `
      <span class="bo-pill">${escapeHtml(data.type)}</span>
      #${data.id} ${escapeHtml(toTitleCase(data.name))}
    `;

    qs('[data-role="search-results"]', row).innerHTML = "";
  }

  async function searchDropsForRow(row) {
    const type = qs('[data-field="resource-type"]', row).value;
    const term = qs('[data-field="search-term"]', row).value.trim();
    const endpoint = type === "POKEMON" ? "pokemon" : "items";
    const data = await api(`/api/catalog/${endpoint}?search=${encodeURIComponent(term)}&limit=12`);
    renderDropSearchResults(row, data.results || []);
  }

  function createDropRow() {
    const row = document.createElement("div");
    row.className = "bo-panel";
    row.style.padding = "10px";
    row.innerHTML = `
      <div class="bo-grid" style="gap: 8px">
        <div class="bo-field">
          <label>Type</label>
          <select data-field="resource-type">
            <option value="POKEMON">POKEMON</option>
            <option value="ITEM">ITEM</option>
          </select>
        </div>
        <div class="bo-field">
          <label>Search resource</label>
          <input data-field="search-term" type="text" placeholder="search by name or id" />
        </div>
        <div class="bo-search-results" data-role="search-results"></div>
        <div class="bo-meta" data-role="selected-info">No resource selected.</div>
        <div class="bo-field">
          <label>Drop rate (%)</label>
          <input data-field="drop-rate" type="number" min="0.01" step="0.01" required />
        </div>
        <div class="bo-actions">
          <button class="bo-btn danger" data-action="remove-drop" type="button">Remove</button>
        </div>
      </div>
    `;

    const searchHandler = debounce(() => {
      searchDropsForRow(row).catch((error) => notify(error.message, "err"));
    }, 280);

    qs('[data-field="resource-type"]', row).addEventListener("change", () => {
      row.dataset.resourceId = "";
      row.dataset.resourceName = "";
      row.dataset.resourceImage = "";
      row.dataset.resourceType = "";
      qs('[data-role="selected-info"]', row).textContent = "No resource selected.";
      searchHandler();
    });

    qs('[data-field="search-term"]', row).addEventListener("input", () => searchHandler());
    qs('[data-field="drop-rate"]', row).addEventListener("input", updateDropRateTotal);

    row.addEventListener("click", (event) => {
      const pick = event.target.closest("[data-pick-id]");
      if (pick) {
        setDropSelection(row, {
          type: qs('[data-field="resource-type"]', row).value,
          id: Number(pick.dataset.pickId),
          name: pick.dataset.pickName || "",
          image: pick.dataset.pickImage || "",
        });
        return;
      }

      const removeBtn = event.target.closest('[data-action="remove-drop"]');
      if (removeBtn) {
        row.remove();
        updateDropRateTotal();
      }
    });

    dropRows.appendChild(row);
    updateDropRateTotal();
  }

  function collectDropEntries() {
    const rows = qsa(".bo-panel", dropRows);

    if (!rows.length) {
      throw new Error("Add at least one drop.");
    }

    const entries = rows.map((row) => {
      const rateValue = Number(qs('[data-field="drop-rate"]', row).value || 0);
      const picked = selectedRowData(row);

      if (!picked.selectedId || !picked.selectedName || !picked.selectedType) {
        throw new Error("Each drop row must have a selected resource.");
      }

      if (!Number.isFinite(rateValue) || rateValue <= 0) {
        throw new Error("Each drop rate must be a positive number.");
      }

      return {
        resourceType: picked.selectedType,
        resourceId: picked.selectedId,
        dropRate: rateValue,
      };
    });

    const total = entries.reduce((sum, entry) => sum + entry.dropRate, 0);
    if (Math.abs(total - 100) > 0.001) {
      throw new Error("Total drop rate must be exactly 100.");
    }

    return entries;
  }

  function renderBoxesTable() {
    boxesCount.textContent = String(boxes.length);
    boxesBadge.textContent = `${boxes.length} boxes`;

    if (!boxes.length) {
      boxesTableWrap.innerHTML = '<div class="bo-empty">No boxes yet.</div>';
      return;
    }

    const rows = boxes
      .map((box) => {
        const entrySummary = renderDropVisuals(box.entries || []);

        return `
          <tr>
            <td>
              <div style="display: grid; grid-template-columns: 48px 1fr; gap: 8px; align-items: center">
                <img src="${escapeHtml(box.pokeballImage)}" alt="${escapeHtml(box.name)}" style="width: 44px; height: 44px; object-fit: contain;" onerror="this.style.visibility='hidden'" />
                <div><strong>${escapeHtml(box.name)}</strong></div>
              </div>
            </td>
            <td>
              <div class="bo-help" style="margin-bottom: 7px">Rarity order: rare -> common</div>
              ${entrySummary || "-"}
            </td>
            <td>${box.totalDropRate.toFixed(2)}%</td>
            <td>
              <div class="bo-actions">
                <button class="bo-btn danger" data-action="delete-box" data-id="${box.id}" type="button">Delete</button>
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

  async function loadBoxes() {
    const data = await api("/api/boxes");
    boxes = data.boxes || [];
    renderBoxesTable();
  }

  pokeballSearchInput.addEventListener(
    "input",
    debounce(() => {
      loadPokeballs(pokeballSearchInput.value).catch((error) => notify(error.message, "err"));
    }, 260)
  );

  pokeballGrid.addEventListener("click", (event) => {
    const card = event.target.closest("[data-ball-id]");
    if (!card) {
      return;
    }

    const ball = pokeballs.find((entry) => entry.id === Number(card.dataset.ballId));
    if (!ball) {
      return;
    }

    setSelectedPokeball(ball);
  });

  addDropBtn.addEventListener("click", () => createDropRow());

  createBoxForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    try {
      const entries = collectDropEntries();
      const name = boxNameInput.value.trim();

      if (!name) {
        throw new Error("Box name is required.");
      }

      if (!selectedPokeball || !pokeballImageInput.value) {
        throw new Error("Select a pokeball.");
      }

      await api("/api/boxes", {
        method: "POST",
        body: JSON.stringify({
          name,
          pokeballImage: pokeballImageInput.value,
          entries,
        }),
      });

      createBoxForm.reset();
      dropRows.innerHTML = "";
      createDropRow();
      createDropRow();
      setSelectedPokeball(pokeballs[0] || null);
      await loadBoxes();
      notify("Box created.");
    } catch (error) {
      notify(error.message, "err");
    }
  });

  boxesTableWrap.addEventListener("click", async (event) => {
    const button = event.target.closest('[data-action="delete-box"]');
    if (!button) {
      return;
    }

    const box = boxes.find((entry) => entry.id === button.dataset.id);
    if (!box) {
      return;
    }

    if (!confirm(`Delete box ${box.name}?`)) {
      return;
    }

    try {
      await api(`/api/boxes/${box.id}`, { method: "DELETE" });
      await loadBoxes();
      notify("Box deleted.");
    } catch (error) {
      notify(error.message, "err");
    }
  });

  createDropRow();
  createDropRow();

  Promise.all([loadPokeballs(), loadBoxes()]).catch((error) => notify(error.message, "err"));
})();
