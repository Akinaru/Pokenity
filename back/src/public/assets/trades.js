(() => {
  const { activateNav, api, escapeHtml, formatDate, notify, qs, toTitleCase } =
    window.AdminCommon;

  activateNav();

  const tradesBadge = qs("#tradesBadge");
  const tradesCount = qs("#tradesCount");
  const tradesSearch = qs("#tradesSearch");
  const tradesSummary = qs("#tradesSummary");
  const tradesTableWrap = qs("#tradesTableWrap");
  const refreshTradesBtn = qs("#refreshTradesBtn");

  const state = {
    trades: [],
    summary: null,
    filter: "",
  };

  function normalized(value) {
    return String(value || "").trim().toLowerCase();
  }

  function resourceLabel(resource) {
    if (!resource) {
      return "-";
    }

    return `${resource.resourceType} #${resource.resourceId} ${toTitleCase(resource.resourceName)}`;
  }

  function serializeSearchText(trade) {
    return [
      trade.id,
      trade.status,
      trade.proposer?.username,
      trade.proposer?.email,
      trade.recipient?.username,
      trade.recipient?.email,
      trade.offeredPokemon?.resourceName,
      trade.receivedPokemon?.resourceName,
      trade.offeredPokemon?.resourceType,
      trade.receivedPokemon?.resourceType,
      trade.offeredPokemon?.resourceId,
      trade.receivedPokemon?.resourceId,
      ...(trade.requestedPokemons || []).map((rp) => rp.resourceName),
      ...(trade.requestedPokemons || []).map((rp) => rp.resourceId),
    ]
      .map(normalized)
      .join(" ");
  }

  function filteredTrades() {
    const query = normalized(state.filter);
    if (!query) {
      return state.trades;
    }

    return state.trades.filter((trade) => serializeSearchText(trade).includes(query));
  }

  function renderSummary() {
    const summary = state.summary || {};
    const total = Number(summary.total || 0);
    const pending = Number(summary.PENDING || 0);
    const waiting = Number(summary.WAITING_CONFIRMATION || 0);

    tradesBadge.textContent = `${total} actifs`;

    tradesSummary.innerHTML = [
      `<span class="bo-pill">Total ${total}</span>`,
      `<span class="bo-pill">Pending ${pending}</span>`,
      `<span class="bo-pill">Waiting confirmation ${waiting}</span>`,
    ].join("");
  }

  function renderTable() {
    const rowsData = filteredTrades();
    tradesCount.textContent = String(rowsData.length);

    if (!rowsData.length) {
      tradesTableWrap.innerHTML = '<div class="bo-empty">Aucun échange en cours.</div>';
      return;
    }

    const rows = rowsData
      .map((trade) => {
        const proposerLabel = trade.proposer
          ? `${trade.proposer.username} (XP ${trade.proposer.xp})`
          : trade.proposerId;
        const recipientLabel = trade.recipient
          ? `${trade.recipient.username} (XP ${trade.recipient.xp})`
          : "En attente d'un joueur";

        const requestedLabel = (trade.requestedPokemons || []).length > 0
          ? (trade.requestedPokemons || []).map((rp) => escapeHtml(resourceLabel(rp))).join("<br>")
          : "-";

        return `
          <tr>
            <td>
              <strong>${escapeHtml(trade.id)}</strong>
              <div class="bo-meta">MAJ ${formatDate(trade.updatedAt)}</div>
            </td>
            <td><span class="bo-pill">${escapeHtml(trade.status)}</span></td>
            <td>
              ${escapeHtml(proposerLabel)}
              <div class="bo-meta">${escapeHtml(trade.proposer?.id || trade.proposerId)}</div>
            </td>
            <td>${escapeHtml(resourceLabel(trade.offeredPokemon))}</td>
            <td>${requestedLabel}</td>
            <td>
              ${escapeHtml(recipientLabel)}
              <div class="bo-meta">${escapeHtml(trade.recipient?.id || trade.recipientId || "-")}</div>
            </td>
            <td>${escapeHtml(resourceLabel(trade.receivedPokemon))}</td>
            <td>
              <div class="bo-meta">Cree le ${formatDate(trade.createdAt)}</div>
              <div class="bo-meta">Accepte le ${formatDate(trade.acceptedAt)}</div>
              <div class="bo-meta">Expire le ${formatDate(trade.expiresAt)}</div>
            </td>
          </tr>
        `;
      })
      .join("");

    tradesTableWrap.innerHTML = `
      <table class="bo-table">
        <thead>
          <tr>
            <th>Trade</th>
            <th>Status</th>
            <th>Proposer</th>
            <th>Pokemon offert</th>
            <th>Pokémons demandés</th>
            <th>Recipient</th>
            <th>Pokemon reçu</th>
            <th>Dates</th>
          </tr>
        </thead>
        <tbody>${rows}</tbody>
      </table>
    `;
  }

  async function loadTrades() {
    const data = await api("/api/trades/admin/current?limit=250");
    state.summary = data.summary || {};
    state.trades = data.trades || [];
    renderSummary();
    renderTable();
  }

  tradesSearch.addEventListener("input", () => {
    state.filter = tradesSearch.value;
    renderTable();
  });

  refreshTradesBtn.addEventListener("click", () => {
    loadTrades().catch((error) => notify(error.message, "err"));
  });

  loadTrades().catch((error) => notify(error.message, "err"));
})();
