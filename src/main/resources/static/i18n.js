/**
 * Prosty przełącznik PL / EN dla frontu (booking + restaurant).
 * Preferencja w localStorage (`rc.locale`); synchronizuje select[name=locale].
 */
(function () {
  const STORAGE_KEY = "rc.locale";

  const dict = {
    pl: {
      "doc.booking.title": "Reservation Core — rezerwacja",
      "doc.restaurant.title": "Reservation Core — panel restauracji",

      "booking.hero.lead":
        "Zarezerwuj stolik na wybrany dzień i godzinę. Potwierdzenie z numerem wyślemy mailem — po polsku lub po angielsku.",
      "booking.cta.book": "Rezerwuję stolik",
      "booking.cta.panel": "Panel restauracji",
      "booking.form.title": "Formularz rezerwacji",
      "booking.form.lead": "Wymagane pola są walidowane po stronie API.",
      "booking.label.place": "Lokal",
      "booking.label.name": "Imię",
      "booking.label.surname": "Nazwisko",
      "booking.label.email": "Email",
      "booking.label.phone": "Telefon",
      "booking.label.date": "Data",
      "booking.label.time": "Godzina",
      "booking.label.duration": "Czas (min)",
      "booking.label.people": "Osoby",
      "booking.submit": "Wyślij rezerwację",
      "booking.places.empty": "Brak lokali",
      "booking.success": "Rezerwacja przyjęta. Numer: {0}",
      "booking.error": "Błąd",

      "nav.aria": "Sekcje",
      "nav.schedule": "Harmonogram",
      "nav.analytics": "Analityka",
      "nav.cancel": "Odwołania",
      "nav.setup": "Lokal",
      "brand.sub": "Panel restauracji",

      "controls.place": "Lokal",
      "controls.day": "Dzień",
      "controls.refresh": "Odśwież",
      "controls.guest": "Formularz gościa",

      "schedule.title": "Harmonogram dnia",
      "schedule.lead": "Godzina, dostępne stoliki i liczba zarezerwowanych miejsc.",
      "schedule.h.hour": "Godzina",
      "schedule.h.available": "Dostępne",
      "schedule.h.reserved": "Zarezerwowane",
      "schedule.h.total": "Razem",
      "schedule.h.controls": "Sterowanie",
      "schedule.lock": "Zablokuj",
      "schedule.unlock": "Odblokuj",
      "schedule.locked": "zablokowany",
      "schedule.open": "otwarty",

      "analytics.title": "Analityka",
      "analytics.lead": "Osobna tabela: dzień, godzina, zarezerwowane stoliki.",
      "analytics.h.day": "Dzień",
      "analytics.h.hour": "Godzina",
      "analytics.h.reserved": "Zarezerwowane stoliki",
      "analytics.h.cancelled": "Odwołane",
      "analytics.h.people": "Osoby",

      "cancel.title": "Odwołanie rezerwacji",
      "cancel.lead": "Po odwołaniu dostępność stolika wraca do harmonogramu.",
      "cancel.label.number": "Numer rezerwacji",
      "cancel.label.reason": "Powód (opcjonalnie)",
      "cancel.placeholder.reason": "Prośba gościa",
      "cancel.submit": "Odwołaj",
      "cancel.active": "Aktywne rezerwacje",
      "cancel.h.number": "Numer",
      "cancel.h.day": "Dzień",
      "cancel.h.time": "Godzina",
      "cancel.h.people": "Osoby",
      "cancel.h.table": "Stolik / status",
      "cancel.confirmed": "potwierdzona",

      "setup.title": "Lokal i konfiguracja",
      "setup.lead": "Utwórz lokal, potem wygeneruj kalendarz i schedule stolików.",
      "setup.label.name": "Nazwa",
      "setup.label.city": "Miasto",
      "setup.label.street": "Ulica",
      "setup.label.number": "Numer",
      "setup.label.postal": "Kod pocztowy",
      "setup.label.post": "Poczta",
      "setup.create": "Utwórz lokal",
      "setup.label.year": "Rok",
      "setup.label.open": "Otwarcie",
      "setup.label.close": "Zamknięcie",
      "setup.label.tables": "Liczba stolików",
      "setup.label.seats": "Miejsc / stolik",
      "setup.init": "Inicjalizuj schedule",
      "setup.created": "Utworzono lokal #{0} — {1}",
      "setup.inited": "Zainicjalizowano: {0} stolików, {1} dni + schedule.",
      "setup.needPlace": "Najpierw utwórz lokal.",
      "places.empty": "Brak lokali — utwórz w zakładce Lokal",
      "table.empty": "Brak danych dla wybranego dnia.",
      "api.error": "Błąd {0}",

      "lang.label": "Język",
      "lang.pl": "PL",
      "lang.en": "EN",
    },
    en: {
      "doc.booking.title": "Reservation Core — booking",
      "doc.restaurant.title": "Reservation Core — restaurant panel",

      "booking.hero.lead":
        "Book a table for your chosen day and time. We'll email a confirmation number — in Polish or English.",
      "booking.cta.book": "Book a table",
      "booking.cta.panel": "Restaurant panel",
      "booking.form.title": "Booking form",
      "booking.form.lead": "Required fields are validated by the API.",
      "booking.label.place": "Venue",
      "booking.label.name": "First name",
      "booking.label.surname": "Last name",
      "booking.label.email": "Email",
      "booking.label.phone": "Phone",
      "booking.label.date": "Date",
      "booking.label.time": "Time",
      "booking.label.duration": "Duration (min)",
      "booking.label.people": "Guests",
      "booking.submit": "Submit booking",
      "booking.places.empty": "No venues",
      "booking.success": "Booking accepted. Number: {0}",
      "booking.error": "Error",

      "nav.aria": "Sections",
      "nav.schedule": "Schedule",
      "nav.analytics": "Analytics",
      "nav.cancel": "Cancellations",
      "nav.setup": "Venue",
      "brand.sub": "Restaurant panel",

      "controls.place": "Venue",
      "controls.day": "Day",
      "controls.refresh": "Refresh",
      "controls.guest": "Guest form",

      "schedule.title": "Daily schedule",
      "schedule.lead": "Hour, available tables and reserved seats.",
      "schedule.h.hour": "Hour",
      "schedule.h.available": "Available",
      "schedule.h.reserved": "Reserved",
      "schedule.h.total": "Total",
      "schedule.h.controls": "Controls",
      "schedule.lock": "Lock",
      "schedule.unlock": "Unlock",
      "schedule.locked": "locked",
      "schedule.open": "open",

      "analytics.title": "Analytics",
      "analytics.lead": "Separate table: day, hour, reserved tables.",
      "analytics.h.day": "Day",
      "analytics.h.hour": "Hour",
      "analytics.h.reserved": "Reserved tables",
      "analytics.h.cancelled": "Cancelled",
      "analytics.h.people": "Guests",

      "cancel.title": "Cancel reservation",
      "cancel.lead": "After cancellation, table availability returns to the schedule.",
      "cancel.label.number": "Reservation number",
      "cancel.label.reason": "Reason (optional)",
      "cancel.placeholder.reason": "Guest request",
      "cancel.submit": "Cancel",
      "cancel.active": "Active reservations",
      "cancel.h.number": "Number",
      "cancel.h.day": "Day",
      "cancel.h.time": "Time",
      "cancel.h.people": "Guests",
      "cancel.h.table": "Table / status",
      "cancel.confirmed": "confirmed",

      "setup.title": "Venue and setup",
      "setup.lead": "Create a venue, then generate the calendar and table schedule.",
      "setup.label.name": "Name",
      "setup.label.city": "City",
      "setup.label.street": "Street",
      "setup.label.number": "Number",
      "setup.label.postal": "Postal code",
      "setup.label.post": "Post office",
      "setup.create": "Create venue",
      "setup.label.year": "Year",
      "setup.label.open": "Opens",
      "setup.label.close": "Closes",
      "setup.label.tables": "Tables",
      "setup.label.seats": "Seats / table",
      "setup.init": "Initialize schedule",
      "setup.created": "Created venue #{0} — {1}",
      "setup.inited": "Initialized: {0} tables, {1} days + schedule.",
      "setup.needPlace": "Create a venue first.",
      "places.empty": "No venues — create one in the Venue tab",
      "table.empty": "No data for the selected day.",
      "api.error": "Error {0}",

      "lang.label": "Language",
      "lang.pl": "PL",
      "lang.en": "EN",
    },
  };

  function normalize(lang) {
    return lang && String(lang).toLowerCase().startsWith("en") ? "en" : "pl";
  }

  function format(template, args) {
    return String(template).replace(/\{(\d+)\}/g, (_, i) =>
      args[i] !== undefined && args[i] !== null ? String(args[i]) : ""
    );
  }

  const I18N = {
    lang: "pl",

    t(key, ...args) {
      const table = dict[this.lang] || dict.pl;
      const raw = table[key] ?? dict.pl[key] ?? key;
      return args.length ? format(raw, args) : raw;
    },

    getLang() {
      return this.lang;
    },

    setLang(lang, { persist = true, emit = true } = {}) {
      this.lang = normalize(lang);
      if (persist) {
        try {
          localStorage.setItem(STORAGE_KEY, this.lang);
        } catch (_) {
          /* ignore */
        }
      }
      this.apply();
      if (emit) {
        document.dispatchEvent(new CustomEvent("rc:locale", { detail: { lang: this.lang } }));
      }
    },

    apply() {
      document.documentElement.lang = this.lang;

      document.querySelectorAll("[data-i18n]").forEach((el) => {
        el.textContent = this.t(el.getAttribute("data-i18n"));
      });

      document.querySelectorAll("[data-i18n-html]").forEach((el) => {
        el.innerHTML = this.t(el.getAttribute("data-i18n-html"));
      });

      document.querySelectorAll("[data-i18n-placeholder]").forEach((el) => {
        el.setAttribute("placeholder", this.t(el.getAttribute("data-i18n-placeholder")));
      });

      document.querySelectorAll("[data-i18n-aria]").forEach((el) => {
        el.setAttribute("aria-label", this.t(el.getAttribute("data-i18n-aria")));
      });

      document.querySelectorAll("[data-i18n-title]").forEach((el) => {
        document.title = this.t(el.getAttribute("data-i18n-title"));
      });

      document.querySelectorAll('select[name="locale"]').forEach((sel) => {
        sel.value = this.lang;
      });

      document.querySelectorAll("[data-lang]").forEach((btn) => {
        const active = btn.getAttribute("data-lang") === this.lang;
        btn.classList.toggle("is-active", active);
        btn.setAttribute("aria-pressed", active ? "true" : "false");
      });
    },

    bindSwitchers(root = document) {
      root.querySelectorAll("[data-lang]").forEach((btn) => {
        btn.addEventListener("click", () => this.setLang(btn.getAttribute("data-lang")));
      });
    },

    init(defaultLang) {
      let stored = null;
      try {
        stored = localStorage.getItem(STORAGE_KEY);
      } catch (_) {
        /* ignore */
      }
      this.lang = normalize(stored || defaultLang || document.documentElement.lang || "pl");
      this.bindSwitchers();
      this.apply();
      return this.lang;
    },
  };

  window.I18N = I18N;
})();
