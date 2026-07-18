# Reservation-Core

System rezerwacji stolików dla restauracji (Spring Boot 4 / Java 21).

## Co jest w systemie

| Element | Opis |
|--------|------|
| Serwer aplikacyjny | Spring Boot WAR (`ReservationSystemApplication`), scheduling włączony |
| Baza danych | **PostgreSQL** (domyślnie) — uruchamiana przez Podman |
| Schedule | Encja `ScheduleSlot`: data, godzina, dostępne / zarezerwowane stoliki |
| Analityka | Osobna tabela `reservation_analytics` (dzień, godzina, liczba zarezerwowanych stolików) |
| SMTP | `EmailService` + `spring-boot-starter-mail` (Gmail STARTTLS) |
| Generator potwierdzeń | Numer `RES-YYYYMMDD-XXXXXX` + mail w wybranym języku |
| API | REST pod `/api/**` |
| Walidator | Bean Validation na formularzu (`@NotBlank`, `@Email`, `@NotNull`, …) |
| Język obcy | `pl` / `en` — `messages_*.properties` dla maili i komunikatów |
| Front restauracji | `/restaurant/` — harmonogram, analityka, odwołania, setup lokalu |
| Front gościa | `/booking/` — formularz rezerwacji |
| Odwoływanie | `POST /api/reservations/cancel` — po odwołaniu zwiększa dostępność w schedule |

## Mechanizmy czyszczenia bazy

Konfiguracja: `app.cleanup.*`

1. **Wygasanie niepotwierdzonych** (`DatabaseCleanupService.expireUnconfirmedReservations`, co godzinę)  
   Rezerwacje ACTIVE bez potwierdzenia starsze niż `app.cleanup.unconfirmed-hours` (domyślnie 24h) → status `EXPIRED`, zwolnienie stolika w schedule, aktualizacja analityki.

2. **Purge historyczny** (`purgeHistoricalData`, codziennie 03:30)  
   Usuwa sloty schedule, rekordy analityki i odwołania starsze niż `app.cleanup.past-slots-days` (domyślnie 30 dni).

## Encje kluczowe

- **Formularz** — `CreateReservationFormDTO` (walidowany)
- **Schedule** — `ScheduleSlot` (data + godzina + available/reserved tables + `locked`)
- **Reservation** — status `ACTIVE` / `CANCELLED` / `EXPIRED`, pole `locale`
- **Confirmation** — numer rezerwacji
- **Cancellation** — rekord odwołania
- **ReservationAnalytics** — osobna tabela analityki

## Baza PostgreSQL (Podman)

Lokalny plik `podman-compose.yml` jest w `.gitignore` (wraz z `postgres-data/`).

```bash
cp podman-compose.yml.example podman-compose.yml
podman compose -f podman-compose.yml up -d
```

Domyślne dane połączenia (zgodne z `application.properties`):

- DB: `reservation`
- user/hasło: `reservation` / `reservation`
- port: `5432`

## Uruchomienie

```bash
mvn spring-boot:run
```

- Panel: http://localhost:8080/restaurant/
- Booking: http://localhost:8080/booking/

## Testy

```bash
# unit + integracyjne (profil test = H2 w trybie PostgreSQL)
mvn test
```

## Szybki flow API

```bash
# 1. Lokal
curl -s -X POST http://localhost:8080/api/places \
  -H 'Content-Type: application/json' \
  -d '{"name":"Trattoria","city":"Warszawa","street":"Marszałkowska","streetNumber":1,"postalCode":"00-001","postOffice":"Warszawa"}'

# 2. Setup (kalendarz + stoliki + schedule)
curl -s -X POST http://localhost:8080/api/places/1/setup \
  -H 'Content-Type: application/json' \
  -d '{"year":2026,"open":true,"defaultOpenFrom":"12:00:00","defaultOpenTo":"22:00:00","tables":[{"number":1,"seatsCount":4,"active":true},{"number":2,"seatsCount":2,"active":true}]}'

# 3. Rezerwacja (locale=en|pl)
curl -s -X POST http://localhost:8080/api/reservations \
  -H 'Content-Type: application/json' \
  -d '{"ordererName":"Anna","ordererSurname":"Nowak","placeId":1,"email":"anna@example.com","phoneNumber":"+48123123123","date":"2026-07-20","startTime":"18:00:00","durationMinutes":90,"peopleCount":2,"locale":"pl"}'

# 4. Harmonogram dnia
curl -s 'http://localhost:8080/api/places/1/schedule?date=2026-07-20'

# 5. Analityka
curl -s 'http://localhost:8080/api/places/1/analytics?date=2026-07-20'

# 6. Odwołanie (przywraca dostępność)
curl -s -X POST http://localhost:8080/api/reservations/cancel \
  -H 'Content-Type: application/json' \
  -d '{"reservationNumber":"RES-20260720-XXXXXX","reason":"Zmiana planów","locale":"pl"}'
```

## SMTP

Uzupełnij w `application.properties`:

```
spring.mail.username=...
spring.mail.password=...   # hasło aplikacji Gmail
```

Przy błędnej konfiguracji SMTP rezerwacja i tak jest zapisywana (mail logowany jako warning).
