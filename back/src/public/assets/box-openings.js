(() => {
  const { activateNav, api, escapeHtml, formatDate, notify, qs, toTitleCase } =
    window.AdminCommon;

  activateNav();

  const openingsBadge = qs("#openingsBadge");
  const openingsCount = qs("#openingsCount");
  const openingsSearch = qs("#openingsSearch");
  const openingsSummary = qs("#openingsSummary");
  const openingsTableWrap = qs("#openingsTableWrap");
  const refreshOpeningsBtn = qs("#refreshOpeningsBtn");

  const state = {
    openings: [],
    summary: null,
    filter: "",
  };

  function normalized(value) {
    return String(value || "").trim().toLowerCase();
  }

  function detailsPreview(details) {
    if (!details) {
      return "-";
    }

    try {
      const raw = JSON.stringify(details);
      if (!raw) {
        return "-";
      }
      return raw.length > 160 ? `${raw.slice(0, 157)}...` : raw;
    } catch {
      return String(details);
    }
  }

  function openingSearchText(opening) {
    return [
      opening.id,
      opening.user?.username,
      opening.user?.email,
      opening.boxName,
      opening.box?.name,
      opening.reward?.resourceName,
      opening.reward?.resourceType,
      opening.reward?.resourceId,
      opening.userId,
      opening.boxId,
    ]
      .map(normalized)
      .join(" ");
  }

  function filteredOpenings() {
    const query = normalized(state.filter);
    if (!query) {
      return state.openings;
    }

    return state.openings.filter((opening) => openingSearchText(opening).includes(query));
  }

  function renderSummary() {
    const summary = state.summary || {};
    const total = Number(summary.total || 0);
    const last24h = Number(summary.last24h || 0);
    const returned = Number(summary.returned || 0);

    openingsBadge.textContent = `${total} openings`;

    openingsSummary.innerHTML = [
      `<span class="bo-pill">Total ${total}</span>`,
      `<span class="bo-pill">24h ${last24h}</span>`,
      `<span class="bo-pill">Affiches ${returned}</span>`,
    ].join("");
  }

  function renderTable() {
    const rowsData = filteredOpenings();
    openingsCount.textContent = String(rowsData.length);

    if (!rowsData.length) {
      openingsTableWrap.innerHTML = '<div class="bo-empty">Aucune ouverture de box trouvee.</div>';
      return;
    }

    const rows = rowsData
      .map((opening) => {
        const userLabel = opening.user
          ? `${opening.user.username} (XP ${opening.user.xp})`
          : opening.userId;
        const boxLabel = opening.box?.name || opening.boxName || "-";
        const rewardLabel = `${opening.reward?.resourceType || "-"} #${
          opening.reward?.resourceId || "-"
        } ${toTitleCase(opening.reward?.resourceName || "")}${
          opening.reward?.isShiny ? " ✨Shiny" : ""
        }`;

        return `
          <tr>
            <td>
              <strong>${escapeHtml(opening.id)}</strong>
              <div class="bo-meta">${formatDate(opening.openedAt)}</div>
            </td>
            <td>
              ${escapeHtml(userLabel)}
              <div class="bo-meta">${escapeHtml(opening.user?.email || "-")}</div>
            </td>
            <td>
              ${escapeHtml(boxLabel)}
              <div class="bo-meta">${escapeHtml(opening.boxId || "-")}</div>
            </td>
            <td>${escapeHtml(rewardLabel)}</td>
            <td><span class="bo-pill">${Number(opening.reward?.dropRate || 0).toFixed(2)}%</span></td>
            <td title="${escapeHtml(detailsPreview(opening.details))}">
              <span class="bo-meta">${escapeHtml(detailsPreview(opening.details))}</span>
            </td>
          </tr>
        `;
      })
      .join("");

    openingsTableWrap.innerHTML = `
      <table class="bo-table">
        <thead>
          <tr>
            <th>Opening</th>
            <th>User</th>
            <th>Box</th>
            <th>Reward</th>
            <th>Drop rate</th>
            <th>Details</th>
          </tr>
        </thead>
        <tbody>${rows}</tbody>
      </table>
    `;
  }

  async function loadOpenings() {
    const data = await api("/api/box-openings/admin/history?limit=300");
    state.summary = data.summary || {};
    state.openings = data.openings || [];
    renderSummary();
    renderTable();
  }

  openingsSearch.addEventListener("input", () => {
    state.filter = openingsSearch.value;
    renderTable();
  });

  refreshOpeningsBtn.addEventListener("click", () => {
    loadOpenings().catch((error) => notify(error.message, "err"));
  });

  loadOpenings().catch((error) => notify(error.message, "err"));
})();
