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
    const headers = {
      ...(options.headers || {}),
    };

    const isFormData = typeof FormData !== "undefined" && options.body instanceof FormData;
    if (!isFormData && !headers["Content-Type"]) {
      headers["Content-Type"] = "application/json";
    }

    const response = await fetch(path, {
      headers,
      ...options,
    });

    if (response.status === 204) {
      return null;
    }

    const rawText = await response.text();
    let data = null;

    if (rawText) {
      try {
        data = JSON.parse(rawText);
      } catch {
        data = { error: rawText };
      }
    }

    if (!response.ok) {
      throw new Error((data && data.error) || "API error");
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
    }, 2400);
  }

  function activateNav() {
    const page = document.body.dataset.page || "";
    qsa(".bo-nav-link").forEach((link) => {
      if (link.dataset.page === page) {
        link.classList.add("active");
      }
    });
  }

  function syncBodyModalState() {
    const openedModal = qsa(".bo-modal").some((modal) => !modal.hidden);
    document.body.classList.toggle("bo-modal-open", openedModal);
  }

  function openModal(modal, focusSelector = "input, select, textarea, button") {
    if (!modal) {
      return;
    }

    modal.hidden = false;
    syncBodyModalState();

    const firstFocusable = modal.querySelector(focusSelector);
    if (firstFocusable) {
      firstFocusable.focus();
    }
  }

  function closeModal(modal) {
    if (!modal) {
      return;
    }

    modal.hidden = true;
    syncBodyModalState();
  }

  function bindModal(modal) {
    if (!modal) {
      return;
    }

    modal.addEventListener("click", (event) => {
      if (event.target === modal || event.target.closest("[data-modal-close]")) {
        closeModal(modal);
      }
    });
  }

  document.addEventListener("keydown", (event) => {
    if (event.key !== "Escape") {
      return;
    }

    const opened = qsa(".bo-modal").filter((modal) => !modal.hidden);
    if (!opened.length) {
      return;
    }

    closeModal(opened[opened.length - 1]);
  });

  window.AdminCommon = {
    activateNav,
    api,
    bindModal,
    closeModal,
    escapeHtml,
    formatDate,
    notify,
    openModal,
    qs,
    qsa,
    toTitleCase,
  };
})();
