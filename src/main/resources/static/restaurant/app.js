const api = {
  async json(url, options = {}) {
    const res = await fetch(url, {
      headers: { "Content-Type": "application/json", ...(options.headers || {}) },
      ...options,
    });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) {
      const msg = data.message || data.error || `Błąd ${res.status}`;
      throw new Error(typeof msg === "string" ? msg : JSON.stringify(msg));
    }
    return data;
  },
};

const els = {
  placeSelect: document.getElementById("placeSelect"),
  dateInput: document.getElementById("dateInput"),
  refreshBtn: document.getElementById("refreshBtn"),
  scheduleTable: document.getElementById("scheduleTable"),
  analyticsTable: document.getElementById("analyticsTable"),
  reservationsTable: document.getElementById("reservationsTable"),
  cancelForm: document.getElementById("cancelForm"),
  cancelResult: document.getElementById("cancelResult"),
  cancelNumber: document.getElementById("cancelNumber"),
  placeForm: document.getElementById("placeForm"),
  setupForm: document.getElementById("setupForm"),
  setupResult: document.getElementById("setupResult"),
};

function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

function renderRows(container, headers, rows) {
  const head = `<div class="row head" role="row">${headers.map((h) => `<div>${h}</div>`).join("")}</div>`;
  const body = rows.length
    ? rows.map((cells) => `<div class="row" role="row">${cells.map((c) => `<div>${c}</div>`).join("")}</div>`).join("")
    : `<div class="row"><div>Brak danych dla wybranego dnia.</div></div>`;
  container.innerHTML = head + body;
}

async function loadPlaces() {
  const places = await api.json("/api/places");
  els.placeSelect.innerHTML = places
    .map((p) => `<option value="${p.id}">#${p.id} — ${p.name} (${p.city})</option>`)
    .join("");
  if (!places.length) {
    els.placeSelect.innerHTML = `<option value="">Brak lokali — utwórz w zakładce Lokal</option>`;
  }
}

async function loadSchedule() {
  const placeId = els.placeSelect.value;
  const date = els.dateInput.value;
  if (!placeId || !date) return;
  const slots = await api.json(`/api/places/${placeId}/schedule?date=${date}`);
  renderRows(
    els.scheduleTable,
    ["Godzina", "Dostępne", "Zarezerwowane", "Razem", "Sterowanie"],
    slots.map((s) => [
      s.hour,
      s.availableTables,
      `<strong>${s.reservedTables}</strong>`,
      s.totalTables,
      s.locked
        ? `<button class="btn btn-ghost" data-unlock="${s.hour}">Odblokuj</button> <span class="badge badge-lock">zablokowany</span>`
        : `<button class="btn btn-ghost" data-lock="${s.hour}">Zablokuj</button> <span class="badge badge-ok">otwarty</span>`,
    ])
  );

  els.scheduleTable.querySelectorAll("[data-lock]").forEach((btn) => {
    btn.addEventListener("click", () => setLock(btn.dataset.lock, true));
  });
  els.scheduleTable.querySelectorAll("[data-unlock]").forEach((btn) => {
    btn.addEventListener("click", () => setLock(btn.dataset.unlock, false));
  });
}

async function setLock(hour, locked) {
  const placeId = els.placeSelect.value;
  const date = els.dateInput.value;
  await api.json(`/api/places/${placeId}/schedule/availability`, {
    method: "PATCH",
    body: JSON.stringify({ date, hour, locked }),
  });
  await loadSchedule();
}

async function loadAnalytics() {
  const placeId = els.placeSelect.value;
  const date = els.dateInput.value;
  if (!placeId || !date) return;
  const rows = await api.json(`/api/places/${placeId}/analytics?date=${date}`);
  renderRows(
    els.analyticsTable,
    ["Dzień", "Godzina", "Zarezerwowane stoliki", "Odwołane", "Osoby"],
    rows.map((r) => [r.date, r.hour, `<strong>${r.reservedTables}</strong>`, r.cancelledTables, r.peopleCount])
  );
}

async function loadReservations() {
  const placeId = els.placeSelect.value;
  if (!placeId) return;
  const rows = await api.json(`/api/reservations?placeId=${placeId}`);
  renderRows(
    els.reservationsTable,
    ["Numer", "Dzień", "Godzina", "Osoby", "Stolik / status"],
    rows.map((r) => [
      r.reservationNumber,
      r.date,
      r.startTime,
      r.peopleCount,
      `#${r.tableNumber} · ${r.status}${r.confirmed ? " · potwierdzona" : ""}`,
    ])
  );
}

async function refreshAll() {
  await Promise.all([loadSchedule(), loadAnalytics(), loadReservations()]);
}

document.querySelectorAll(".tab").forEach((tab) => {
  tab.addEventListener("click", () => {
    document.querySelectorAll(".tab").forEach((t) => t.classList.remove("is-active"));
    document.querySelectorAll(".panel").forEach((p) => p.classList.remove("is-visible"));
    tab.classList.add("is-active");
    document.getElementById(`panel-${tab.dataset.tab}`).classList.add("is-visible");
  });
});

els.refreshBtn.addEventListener("click", () => refreshAll().catch(alert));
els.placeSelect.addEventListener("change", () => refreshAll().catch(alert));
els.dateInput.addEventListener("change", () => refreshAll().catch(alert));

els.cancelForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  const fd = new FormData(els.cancelForm);
  try {
    const data = await api.json("/api/reservations/cancel", {
      method: "POST",
      body: JSON.stringify({
        reservationNumber: fd.get("reservationNumber"),
        reason: fd.get("reason") || null,
        locale: fd.get("locale"),
      }),
    });
    els.cancelResult.textContent = data.message;
    await refreshAll();
  } catch (err) {
    els.cancelResult.textContent = err.message;
  }
});

els.placeForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  const fd = new FormData(els.placeForm);
  try {
    const place = await api.json("/api/places", {
      method: "POST",
      body: JSON.stringify({
        name: fd.get("name"),
        city: fd.get("city"),
        street: fd.get("street"),
        streetNumber: Number(fd.get("streetNumber")),
        postalCode: fd.get("postalCode"),
        postOffice: fd.get("postOffice"),
      }),
    });
    els.setupResult.textContent = `Utworzono lokal #${place.id} — ${place.name}`;
    await loadPlaces();
    els.placeSelect.value = String(place.id);
  } catch (err) {
    els.setupResult.textContent = err.message;
  }
});

els.setupForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  const placeId = els.placeSelect.value;
  if (!placeId) {
    els.setupResult.textContent = "Najpierw utwórz lokal.";
    return;
  }
  const fd = new FormData(els.setupForm);
  const tableCount = Number(fd.get("tableCount"));
  const seats = Number(fd.get("seats"));
  const tables = Array.from({ length: tableCount }, (_, i) => ({
    number: i + 1,
    seatsCount: seats,
    active: true,
  }));
  try {
    const place = await api.json(`/api/places/${placeId}/setup`, {
      method: "POST",
      body: JSON.stringify({
        year: Number(fd.get("year")),
        open: true,
        defaultOpenFrom: fd.get("defaultOpenFrom"),
        defaultOpenTo: fd.get("defaultOpenTo"),
        tables,
      }),
    });
    els.setupResult.textContent = `Zainicjalizowano: ${place.tablesCount} stolików, ${place.daysCount} dni + schedule.`;
    await refreshAll();
  } catch (err) {
    els.setupResult.textContent = err.message;
  }
});

(async function init() {
  els.dateInput.value = todayIso();
  const params = new URLSearchParams(location.search);
  if (params.get("cancel")) {
    els.cancelNumber.value = params.get("cancel");
    document.querySelector('[data-tab="cancel"]').click();
  }
  try {
    await loadPlaces();
    await refreshAll();
  } catch (err) {
    console.error(err);
  }
})();
