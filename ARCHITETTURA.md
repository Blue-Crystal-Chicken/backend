# Blue Crystal Chicken — Architettura & Porte

Mappa di tutte le parti del sistema, chi comunica con chi e su quale porta.
**Tutto gira in Docker** orchestrato da `docker-compose.yml` (vedi `DOCKER.md`).

Legenda stato: ✅ esistente · 🟡 parziale / in corso · ⬜ pianificato (porta proposta)

---

## 1. Tabella riepilogo (servizi Docker)

| # | Componente | Tipo | Servizio compose | Porta host | Stato | Note |
|---|-----------|------|------------------|------------|-------|------|
| 1 | **Backend principale** (`blue-crystal-chicken`) | Spring Boot 4 (REST + JWT) | `bcc-backend` | **8080** | ✅ | Cuore del sistema, tutte le entità |
| 2 | **Servizio Notifiche Push** (`com.r2u.notification`) | Spring Boot (REST + JWT) | `bcc-notification` | **8085** | ✅ | Firebase FCM |
| 3 | **Admin** (web) | React + Vite + nginx | `bcc-admin-web` | **8090** | ✅ | Gestionale completo |
| 4 | **Manager di sede** (web) | React + Vite + nginx | `bcc-manager` | **5185** | ✅ | Scoping sede, approvazioni, offerte, notifiche per sede |
| 5 | **Cassa / POS** (`counter`) | React + TS + nginx | `bcc-cassa` | **8081** | ✅ | Login ADMIN, lookup/modifica ordini + "Prepara" |
| 6 | **Cucina (KDS)** | React + Vite + nginx | `bcc-cucina` | **5183** | ✅ | Card colorate per categoria; per-sede (token-stazione) |
| 7 | **Tabellone** (ritiro) | React + Vite + nginx | `bcc-tabellone` | **5180** | ✅ | Polling ordini; per-sede (token-stazione) |
| 8 | **Totem** (self-service) | React / kiosk | — | **5182** | ⬜ | Inizio filone: crea ordini in sede |
| 9 | **App telefono** (cliente) | mobile / web | — | **5181** | ⬜ | Ordini cliente + ricezione push |
| 10 | **not_exp** (tester push) | Vite | — | **8082** | ✅ | Strumento di test (non dockerizzato) |
| — | **PostgreSQL** | Database | `postgres` | **5432** | ✅ | DB unico `blue_crystal_db` (vedi §5) |
| — | **RabbitMQ** | Broker | `rabbitmq` | **5672** / **15672** | ✅ | Management UI guest/guest |
| — | **Firebase FCM** | Cloud Google | 443 | ✅ | Push verso i device |

> **Docker vs Dev.** In Docker i frontend sono **build statiche servite da nginx**
> che fa da **reverse-proxy**: il browser chiama same-origin `/api` (e `/images`,
> `/uploads`, e per admin/manager `/api/notification`, `/devices`, …) e nginx
> inoltra al servizio backend → **niente CORS**. In sviluppo (`vite dev`) restano i
> default 5173 (admin) ecc. e il backend ammette quelle origin via allowlist CORS.

---

## 2. Chi comunica con chi

### Backend principale — `bcc-backend` :8080 ✅
- → PostgreSQL `blue_crystal_db` @ 5432 (JPA, **`ddl-auto=update`**; dati persistenti)
- → RabbitMQ @ 5672 — publish eventi su `bcc.events`, consume da `bcc.notifications` / `bcc.requests`
- Espone REST `/api/**` protette con **JWT HS256**; **secret condiviso** con 8085
- **DataLoader idempotente**: semina una sola volta (se esiste `admin@bluecrystal.it` salta)

### Servizio Notifiche Push — `bcc-notification` :8085 ✅
- → PostgreSQL **stesso DB** `blue_crystal_db` ma **schema dedicato `notif`** (no più `bcc_db`)
- → Firebase FCM per le push; valida il JWT emesso da 8080 (HS256 condiviso)

### Admin (web) — `bcc-admin-web` :8090 ✅
- nginx proxa `/api` → 8080 e `/api/notification` + `/devices` + `/user-devices` + `/device-notifications` → 8085
- Login `admin@bluecrystal.it` / `admin123` (o `gspptesse@gmail.com` / `123456`)
- Crea sedi **con account manager** (email+password) e legge il **token-stazione** di una sede
- Pagina **Richieste** con filtro per stato (approva/rifiuta)

### Manager di sede (web) — `bcc-manager` :5185 ✅
- App `manager/` = copia dell'admin **senza "Sedi"**; nginx proxa come l'admin
- Login `manager@bluecrystal.it` / `manager123` — **solo ROLE_MANAGER** (gli altri ruoli sono rifiutati)
- Sidebar/titolo mostrano la **sede gestita** (`locationName` dal JWT)
- **Scoping per sede**: Dashboard/Ordini/Finanze/Report/Magazzino sulla propria sede
- **Approvazioni**: Prodotti/Menu (create/update/delete) → *richieste* all'Admin; **Offerte** dirette
- Pagina **Richieste** con filtro per stato (le proprie)
- Esiste **un Manager per sede**: supervisiona Totem/Cassa/Cucina della propria sede via Backend

### Cassa / POS — `bcc-cassa` :8081 ✅ · *stazione di sede*
- FE `counter-main/counter-main` (React+TS). nginx proxa `/api`, `/images`, `/uploads` → 8080
- Login **richiede ROLE_ADMIN** (token in `localStorage["token"]`)
- `GET /api/orders`, `GET /api/orders/code/{code}` (lookup), `GET /api/categories`,
  `GET /api/products/v1/category/id/{id}` (prodotti per categoria)
- `PUT /api/orders/{id}` con `{items:[…]}` (modifica righe), `PUT /api/orders/{id}/status?status=PREPARING`

### Cucina / KDS — `bcc-cucina` :5183 ✅ · *stazione di sede*
- `GET /api/orders` (polling) + `GET /api/products/v1/products` (mappa colore-categoria, pubblici)
- `PUT /api/orders/{id}/status` con header **`X-Station-Token`** (Fatto→READY / Cancellato→CANCELLED)
- **Per-sede**: configurata con `VITE_STATION_TOKEN`; filtra gli ordini sulla propria sede e mostra **nome+città**

### Tabellone — `bcc-tabellone` :5180 ✅ · *stazione di sede*
- Polling `GET /api/orders`; avanza stato con `PUT …/status` + `X-Station-Token`
- **Per-sede**: come la cucina (filtro sede + badge sede)

### Componenti pianificati
- **Totem :5182** ⬜ — `POST /api/orders` (crea ordini in sede)
- **App telefono :5181** ⬜ — ordini cliente + push (fuori dal filone di sede)

---

## 3. Autenticazione & autorizzazione

- **JWT HS256** con secret condiviso 8080 ⇄ 8085 (`be.app.jwtSecret` / `BE_APP_JWTSECRET`).
- Scritture `/api/**` riservate ad **ADMIN**, con eccezioni:
  - **MANAGER**: `POST /api/requests`, `GET /api/requests/**`, `POST /api/uploads/**`,
    `POST/PUT/DELETE /api/offers/**`, `*/stock/**` della propria sede
  - **Cambio stato ordine** `PUT /api/orders/*/status`: **ADMIN (JWT)** *oppure*
    **token-stazione** (`X-Station-Token`) corrispondente alla **sede dell'ordine**
- **Token-stazione**: ogni sede ha uno `stationToken` (generato alla creazione, write-only).
  - Admin lo legge: `GET /api/locations/{id}/station-token`
  - Stazione lo risolve: `GET /api/locations/by-station-token` (header `X-Station-Token`) → `{id,name,city}`
- **CORS**: allowlist con origin dei container (8090/5185/5183/5180/8081) + dev (5173/8081/8082).

---

## 4. Il filone operativo di sede

```
   TOTEM  ──▶  CASSA  ──▶  CUCINA  ──▶  TABELLONE
 (ordine)    (pagam.)    (prepara)     (pronto/ritiro)
   ⬜          ✅:8081      ✅:5183        ✅:5180
```

- **Totem, Cassa, Cucina** sono *stazioni di sede*: si passano l'ordine lungo il
  filone e ognuna riporta sia all'**Admin** centrale sia al **Manager** della sede.
- **Cassa, Cucina e Tabellone** sono **per-sede**: cassa via login ADMIN, cucina/tabellone
  via **token-stazione** (filtrano e agiscono solo sugli ordini della propria sede).
- Esiste **un Manager per sede** (vede solo le sue stazioni); l'**Admin** vede tutte le sedi.
- **Admin ⇄ Manager** bidirezionale: il Manager invia *richieste*, l'Admin *approva/rifiuta*.

```
        ┌──────────────── ADMIN (centrale, tutte le sedi) ────────────────┐
        │      ▲ richieste │ approvazioni  (Manager ⇄ Admin, bcc.requests) │
        │      │           ▼                                               │
        │            ┌──────── MANAGER sede A ────────┐  ... sede B ...    │
        │            │ (supervisiona le sue stazioni) │                    │
        ▲  ▲  ▲      ▲          ▲          ▲                               │
   SEDE A │  │  │    │          │          │  (ogni stazione → Admin + Manager sede) │
       ┌──┴──┴──┐ ┌──┴────┐ ┌───┴─────┐ ┌──┴────────┐                      │
       │ TOTEM  │▶│ CASSA │▶│ CUCINA  │▶│ TABELLONE │  ── il filone ──▶    │
       │ :5182⬜│ │:8081✅│ │ :5183✅ │ │ :5180 ✅  │                      │
       └────────┘ └───┬───┘ └────┬────┘ └─────┬─────┘                      │
                      └──────────┴────────────┴── REST/JWT ──▶ Backend :8080
                                                              (token-stazione / JWT)
```

> Tutto il traffico passa dal **Backend :8080** (sorgente unica di verità).

---

## 5. Infrastruttura & dati

| Servizio | Porta | Avvio | Credenziali / DB |
|----------|-------|-------|------------------|
| PostgreSQL | 5432 | `docker compose up` | DB `blue_crystal_db` · user/pass da `.env` (non versionato) |
| RabbitMQ | 5672 / 15672 | idem | guest / guest |

**Database (un solo Postgres, un solo DB `blue_crystal_db`):**
- **Backend** → schema `public` (tutte le entità di dominio)
- **Notifiche** → schema **`notif`** (tabelle `notifications`, `devices`, `user_devices`,
  `device_notifications`) — isolato per evitare la collisione con `public.notifications`
- `ddl-auto=update` (backend) e `update` + `default_schema=notif` (notifiche): dati persistenti

**Topologia RabbitMQ:** exchange topic `bcc.events`.
- `event.#` → coda `bcc.notifications` (log eventi)
- `request.created` → coda `bcc.requests` (Manager → Admin)
- `request.reply.#` → coda `bcc.requests.reply` (Admin → Manager)

**Flusso richieste (Manager → Admin):**
- Prodotti/Menu (create/update/delete) → *richieste* PENDING; l'Admin approva (esegue la
  mutazione reale) o rifiuta dalla pagina **Richieste**. Offerte gestite **direttamente** dal Manager.
- Immagini: `POST /api/uploads/image` → `imgPath` nel payload della richiesta.

---

## 6. Avvio rapido

```bash
cp .env.example .env        # opzionale (porte, Firebase, VITE_STATION_TOKEN)
docker compose up -d --build
```

Admin → http://localhost:8090 · Manager → :5185 · Cassa → :8081 · Cucina → :5183 ·
Tabellone → :5180 · RabbitMQ UI → :15672

> Per cucina/tabellone serve il **token-stazione** della sede in `.env`
> (`VITE_STATION_TOKEN`) e un rebuild di quelle immagini. Recuperalo dal pannello
> Admin (Sedi) o via `GET /api/locations/{id}/station-token`.

---

*Riferimenti file: `docker-compose.yml`, `DOCKER.md`, `application.properties`
(backend/notifiche), `vite.config.js` / `nginx.conf` dei frontend.*
