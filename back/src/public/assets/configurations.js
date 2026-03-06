(() => {
  const { activateNav, api, escapeHtml, formatDate, notify, qs } = window.AdminCommon;

  activateNav();

  const configurationsBadge = qs("#configurationsBadge");
  const configurationsCount = qs("#configurationsCount");
  const configurationsTableWrap = qs("#configurationsTableWrap");
  const refreshConfigurationsBtn = qs("#refreshConfigurationsBtn");

  const state = {
    configurations: [],
    savingKey: null,
  };

  function toPercent(configuration) {
    const fromApi = Number(configuration.percentValue);
    if (Number.isFinite(fromApi)) {
      return Number(fromApi.toFixed(2));
    }

    const fromValue = Number(configuration.value);
    if (!Number.isFinite(fromValue)) {
      return 0;
    }

    return Number((fromValue * 100).toFixed(2));
  }

  function renderTable() {
    const rowsData = state.configurations;
    configurationsBadge.textContent = `${rowsData.length} configs`;
    configurationsCount.textContent = String(rowsData.length);

    if (!rowsData.length) {
      configurationsTableWrap.innerHTML = '<div class="bo-empty">Aucune configuration.</div>';
      return;
    }

    const rows = rowsData
      .map((configuration) => {
        const isSaving = state.savingKey === configuration.key;
        const inputId = `cfg-percent-${configuration.key}`;
        return `
          <tr>
            <td>
              <strong>${escapeHtml(configuration.label || configuration.key)}</strong>
              <div class="bo-meta">${escapeHtml(configuration.description || "-")}</div>
            </td>
            <td><code>${escapeHtml(configuration.key)}</code></td>
            <td><span class="bo-pill">${escapeHtml(String(configuration.value))}</span></td>
            <td>
              <div class="bo-split">
                <input
                  id="${escapeHtml(inputId)}"
                  class="bo-input"
                  type="number"
                  min="0"
                  max="100"
                  step="0.01"
                  value="${toPercent(configuration)}"
                  style="max-width: 130px;"
                  ${isSaving ? "disabled" : ""}
                />
                <button
                  class="bo-btn primary"
                  type="button"
                  data-action="save"
                  data-key="${escapeHtml(configuration.key)}"
                  ${isSaving ? "disabled" : ""}
                >
                  ${isSaving ? "En cours..." : "Sauvegarder"}
                </button>
              </div>
            </td>
            <td>${formatDate(configuration.updatedAt)}</td>
          </tr>
        `;
      })
      .join("");

    configurationsTableWrap.innerHTML = `
      <table class="bo-table">
        <thead>
          <tr>
            <th>Configuration</th>
            <th>Cle</th>
            <th>Valeur brute (0..1)</th>
            <th>Valeur (%)</th>
            <th>Derniere MAJ</th>
          </tr>
        </thead>
        <tbody>${rows}</tbody>
      </table>
    `;
  }

  async function loadConfigurations() {
    const data = await api("/api/configurations");
    state.configurations = data.configurations || [];
    renderTable();
  }

  async function saveConfiguration(key) {
    const input = document.getElementById(`cfg-percent-${key}`);
    if (!input) {
      return;
    }

    const percent = Number(input.value);
    if (!Number.isFinite(percent) || percent < 0 || percent > 100) {
      notify("La valeur doit etre entre 0 et 100.", "err");
      return;
    }

    state.savingKey = key;
    renderTable();

    try {
      const data = await api(`/api/configurations/${encodeURIComponent(key)}`, {
        method: "PATCH",
        body: JSON.stringify({ percent }),
      });

      const updated = data.configuration;
      if (updated) {
        state.configurations = state.configurations.map((configuration) =>
          configuration.key === key ? updated : configuration
        );
      }
      notify("Configuration mise a jour.");
    } finally {
      state.savingKey = null;
      renderTable();
    }
  }

  refreshConfigurationsBtn.addEventListener("click", () => {
    loadConfigurations().catch((error) => notify(error.message, "err"));
  });

  configurationsTableWrap.addEventListener("click", (event) => {
    const button = event.target.closest('[data-action="save"]');
    if (!button) {
      return;
    }
    const key = button.dataset.key;
    if (!key) {
      return;
    }

    saveConfiguration(key).catch((error) => {
      notify(error.message, "err");
    });
  });

  loadConfigurations().catch((error) => notify(error.message, "err"));
})();
