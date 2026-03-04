(function bootstrapCommon() {
  function qs(selector, scope = document) {
    return scope.querySelector(selector);
  }

  function qsa(selector, scope = document) {
    return Array.from(scope.querySelectorAll(selector));
  }

  function escapeHtml(value) {
    return String(value)
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll('"', "&quot;")
      .replaceAll("'", "&#39;");
  }

  function formatDate(value) {
    if (!value) {
      return "-";
    }

    try {
      return new Date(value).toLocaleString("fr-FR");
    } catch {
      return String(value);
    }
  }

  function toTitleCase(value) {
    return String(value || "")
      .split("-")
      .map((part) => (part ? `${part[0].toUpperCase()}${part.slice(1)}` : part))
      .join(" ");
  }

  async function api(path, options = {}) {
    const response = await fetch(path, {
      headers: {
        "Content-Type": "application/json",
        ...(options.headers || {}),
      },
      ...options,
    });

    if (response.status === 204) {
      return null;
    }

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.error || "API error");
    }

    return data;
  }

  function ensureToast() {
    let toast = qs("#boToast");
    if (!toast) {
      toast = document.createElement("div");
      toast.id = "boToast";
      toast.className = "bo-toast";
      document.body.appendChild(toast);
    }
    return toast;
  }

  function notify(message, type = "ok") {
    const toast = ensureToast();
    toast.textContent = message;
    toast.className = `bo-toast show ${type}`;
    clearTimeout(notify.timer);
    notify.timer = setTimeout(() => {
      toast.className = "bo-toast";
    }, 2300);
  }

  function activateNav() {
    const page = document.body.dataset.page || "";
    qsa(".bo-nav-link").forEach((link) => {
      if (link.dataset.page === page) {
        link.classList.add("active");
      }
    });
  }

  window.AdminCommon = {
    api,
    activateNav,
    escapeHtml,
    formatDate,
    notify,
    qs,
    qsa,
    toTitleCase,
  };
})();
