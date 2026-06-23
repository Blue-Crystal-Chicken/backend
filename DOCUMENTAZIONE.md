# Blue Crystal Chicken — Documentazione tecnica (backend unificato `be_unification`)

Documentazione completa del sistema: backend unificato + i frontend operativi
(Admin, Manager, Cucina, Tabellone, Cassa). Spiega architettura, scelte, stack,
configurazioni, logiche, feature, flussi dati/DB, pro/contro e FAQ.

---

## 1. Panoramica

Blue Crystal Chicken è un sistema gestionale per una catena di fast-food multi-sede.
È composto da **un backend** (questo, `be_unification`) e da **più frontend** ciascuno
dedicato a un ruolo/stazione:

| App | Ruolo | Tipo |
|-----|-------|------|
| **Admin** | Direzione centrale (CEO): vede tutte le sedi | React + Vite |
| **Manager** | Responsabile di **una** sede | React + Vite |
| **Cucina (KDS)** | Stazione di preparazione di una sede | React + Vite |
| **Tabellone** | Display ritiro ordini di una sede | React + Vite |
| **Cassa (POS)** | Punto cassa di una sede | React + TS |
| **Totem** | Self-service in sede: il cliente crea l'ordine | (FE non in questo repo) |
| **Cliente / App mobile** | Cliente finale: ordini, preferiti, indirizzi, profilo | (FE non in questo repo) |
| **Backend** | API REST + JWT, sorgente unica di verità | Spring Boot 4 |

> Nota: i frontend **Totem** e **App mobile cliente** non sono in questo repo, **ma il
> backend li supporta già completamente** (endpoint ordini, catalogo, indirizzi,
> preferiti, profilo) — vedi §5.6 e §5.7.

Principio chiave: **il backend è l'unica sorgente di verità**. Nessun frontend parla
direttamente con un altro: tutto passa dalle REST API `/api/**`.

---

## 2. Stack tecnologico

**Backend (`be_unification`)**
- **Java 21+ / Spring Boot 4.0.3** (Spring Framework 7, Jackson 3 di default)
- **Spring Web** (REST), **Spring Data JPA** + **Hibernate ORM 7**
- **Spring Security** + **JWT** (libreria `jjwt`, HS256/HS512)
- **PostgreSQL** (produzione/Docker), **H2** (fallback dev)
- **RabbitMQ** (`spring-boot-starter-amqp`) per le notifiche/eventi event-driven
- **ModelMapper** per il mapping Entity ↔ DTO
- **springdoc-openapi** (Swagger UI)
- **Lombok**, **Maven** (wrapper `mvnw`)

**Frontend**
- React 19 + **Vite** + Tailwind CSS (Admin/Manager/Cucina/Tabellone in JS, Cassa in TS+shadcn/ui)
- Routing con `react-router-dom`, HTTP con `axios` (admin/manager) o `fetch` nativo (stazioni)

**Infrastruttura**
- **Docker / docker-compose** (orchestrazione di tutto)
- **nginx** (serve le SPA + reverse-proxy verso il backend)
- **Firebase Cloud Messaging** (push, gestito da un servizio notifiche separato fuori da questo repo)

---

## 3. Architettura

### 3.1 Topologia a container
Ogni componente è un container; `docker-compose.yml` li orchestra:

```
                 ┌───────────── PostgreSQL ─────────────┐
                 │              RabbitMQ                  │
                 └───────────────▲───────────────────────┘
                                 │ JDBC / AMQP
                        ┌────────┴─────────┐
                        │  Backend (8080)  │  ← REST /api/** + JWT
                        └────────▲─────────┘
              same-origin /api (reverse-proxy nginx)
   ┌───────────┬───────────┬───────────┬───────────┬───────────┐
 Admin       Manager     Cucina     Tabellone     Cassa
 (nginx)     (nginx)     (nginx)     (nginx)     (nginx)
```

### 3.2 I frontend sono SPA servite da nginx con reverse-proxy
Ogni frontend è una **build statica** servita da nginx. Il browser chiama
**same-origin** `/api` e nginx inoltra al backend. Per Admin/Manager nginx instrada
anche `/api/notification`, `/devices`, ecc. verso il servizio notifiche; per la Cassa
anche `/images` e `/uploads`.

**Perché reverse-proxy invece di chiamare `http://backend:8080` dal browser?**
Il JS gira nel **browser dell'utente**, non nella rete Docker: non potrebbe risolvere
il nome di servizio `backend`. Con il proxy le chiamate restano same-origin → **niente
problemi di CORS** e nessuna porta del backend "incollata" nel bundle.

### 3.3 Filone operativo di sede
```
 TOTEM ─▶ CASSA ─▶ CUCINA ─▶ TABELLONE
(ordine) (incassa) (prepara) (pronto/ritiro)
```
Cassa, Cucina e Tabellone sono **per-sede**: filtrano gli ordini sulla propria sede
e agiscono solo su quelli (via *token-stazione*, vedi §7.4). Esiste **un Manager per
sede**; l'**Admin** vede tutte le sedi.

---

## 4. Scelte architetturali e perché

| Scelta | Perché | Pro | Contro |
|--------|--------|-----|--------|
| **Backend unico** (non un BE per app) | Sorgente unica di verità, un solo modello dati | Coerenza, meno duplicazione | Un singolo punto da scalare |
| **Package per dominio** (`entity.order`, `entity.user`, …) | Organizzazione che scala col crescere delle entità | Navigabilità | Più cartelle |
| **DTO + Mapper (ModelMapper)** | Non esporre le entità JPA nelle risposte | Disaccoppia API↔DB, evita over-exposure e `LazyInitializationException` | Codice mapper in più |
| **JWT stateless (HS256) condiviso** | Scalabilità (no sessioni) + un token vale anche per il servizio notifiche | Semplicità, stateless | Revoca non immediata (scadenza 24h) |
| **RabbitMQ event-driven** per notifiche/richieste | Disaccoppia chi genera l'evento da chi lo persiste/mostra | Resilienza, estendibile | Dipendenza da un broker |
| **Token-stazione per sede** | Cucina/Tabellone/Cassa devono cambiare lo stato ordini senza login utente | Semplice da configurare | "Quick win", meno granulare di un vero ruolo-stazione |
| **Flusso richieste/approvazioni** (Manager→Admin) | Il Manager non scrive direttamente prodotti/menu sul DB | Controllo centralizzato | Passo in più (approvazione) |
| **nginx reverse-proxy same-origin** | Evita CORS, URL relativi | Deploy pulito | Config nginx per app |
| **`ddl-auto=update` + DataLoader idempotente** | Dati persistenti tra i riavvii senza riseminare | Comodo in dev/demo | Non sostituisce le migration (Flyway) in produzione |

---

## 5. Le applicazioni e tutte le feature

### 5.1 Admin (direzione centrale)
- Login (ruolo **ADMIN**); vede **tutte le sedi**.
- **Sedi**: elenco, mappa, apertura/chiusura (singola e massiva), creazione/modifica.
- **Prodotti / Menu / Offerte / Ingredienti**: CRUD completo.
- **Ordini**: consultazione, filtri, fatturato per sede/periodo.
- **Richieste**: pagina con **filtro per stato** (Tutte/In attesa/Approvate/Rifiutate);
  **approva** (esegue la modifica reale) o **rifiuta** (con nota) le richieste dei manager.
- **Notifiche**: centro notifiche (log eventi via RabbitMQ) con filtri per livello.
- **Token-stazione**: può leggere il token di una sede per configurare le stazioni.

### 5.2 Manager (responsabile di sede)
- Login (ruolo **MANAGER**); **solo i manager** accedono (gli altri ruoli sono rifiutati lato app).
- **Scoping per sede**: Dashboard/Ordini/Finanze/Report/Magazzino limitati alla **propria** sede.
- **Prodotti / Menu**: create/update/delete **come richieste** all'Admin (non scrivono subito sul DB).
- **Offerte**: gestite **direttamente** (senza approvazione).
- **Magazzino**: gestione scorte della propria sede.
- **Richieste**: pagina con filtro per stato (vede le proprie).
- **Notifiche**: filtrate sulla propria sede (+ globali); riceve l'attività delle stazioni della sede.

### 5.3 Cucina (KDS) — per-sede
- Mostra le card degli ordini **da preparare** (PENDING/PREPARING) della **propria sede**.
- Bordo **colorato per categoria** (Hamburger, Patatine, Bibita, …) → colpo d'occhio.
- Azioni: **Fatto** (→ READY) e **Cancellato** (→ CANCELLED).
- Badge con **nome+città** della sede (dal token-stazione).

### 5.4 Tabellone — per-sede
- Display a 2 colonne: **In Preparazione** e **Pronti/Asporto** della propria sede.
- Avanza lo stato con un click; polling automatico.
- Badge sede; soglie di attesa per evidenziare gli ordini in ritardo.

### 5.5 Cassa (POS) — per-sede
- Login **ADMIN**; cerca un ordine per **codice**, modifica le righe, lo manda in **PREPARING**.
- Catalogo per categoria; usa il token-stazione per i cambi stato.

### 5.6 Totem (self-service in sede) — *FE non in repo, BE pronto*
È l'inizio del filone di sede: il cliente compone l'ordine da solo. Lato backend usa:
- **Catalogo**: `GET /api/categories`, `GET /api/products/v1/category/id/{categoryId}`
  (`0` = tutti), `GET /api/menus`, `GET /api/offers` — tutte **pubbliche** (no login).
- **Creazione ordine**: `POST /api/orders` (pubblico) con le righe (`items`: productId/offerId/
  menuId, quantità, note, ingredienti) e la **sede** (`locationId`). Supporta ordini **"ospite"**
  (senza utente loggato) impostando `userId = null`.
- L'ordine creato entra nel filone: pubblica `event.order.created` (visibile a Cassa/Cucina/Manager).

### 5.7 Cliente / App mobile — *FE non in repo, BE pronto*
Canale del cliente finale (fuori dal filone di sede). Lato backend usa:
- **Auth**: `POST /api/auth/register`, `POST /api/auth/login` (ruolo `USER`); `GET /api/auth/me`.
- **Catalogo**: gli stessi endpoint pubblici del Totem (prodotti/menu/offerte/categorie).
- **Ordini**: `POST /api/orders` (a proprio nome, `userId` valorizzato) e storico
  `GET /api/orders/user/{userId}`.
- **Indirizzi di consegna**: `AddressController` (`/api/addresses`) — CRUD degli indirizzi del cliente.
- **Preferiti**: prodotti `POST/DELETE /api/products/favorite/**` e menu `POST/DELETE /api/menus/favorite/**`
  (entità `UserFavoriteProduct` / `UserFavoriteMenu`).
- **Profilo**: `PUT /api/users/v1/users/{id}` (dati profilo) e `PUT /api/users/v1/users/{id}/location/{locationId}` (sede preferita).
- **Notifiche push**: gestite dal servizio notifiche Firebase separato (registrazione device + invio);
  il backend pubblica gli eventi, il servizio push li recapita all'app.

> Tutte queste funzioni del cliente provengono dalla base di Giuseppe e sono **già presenti** nel
> backend unificato: per attivarle basta sviluppare/collegare i frontend Totem e App mobile.

---

## 6. File di configurazione

| File | Dove | Cosa configura |
|------|------|----------------|
| `application.properties` | `src/main/resources/` | ddl-auto, JWT secret (via env, default fittizio), RabbitMQ resiliente, upload dir. **Datasource NON hardcoded** (env in Docker / fallback locale). |
| `docker-compose.yml` | root del workspace | orchestrazione di tutti i container, porte, variabili d'ambiente, healthcheck, volumi |
| `.env` (NON versionato) | root | valori reali: `POSTGRES_PASSWORD`, `BE_APP_JWTSECRET`, `VITE_STATION_TOKEN` |
| `.env.example` | root | template con segnaposto |
| `nginx.conf` | ogni frontend | SPA fallback + reverse-proxy `/api` (+`/images`,`/uploads`,`/api/notification`) |
| `Dockerfile` | backend e ogni frontend | build multi-stage (Maven→JRE per il BE; node→nginx per i FE) |
| `pom.xml` | backend | dipendenze Maven |

**Segreti**: JWT secret e password DB **non sono nel codice** → arrivano da variabili
d'ambiente (`.env`, gitignored). Nei file versionati restano solo **segnaposto fittizi**.

---

## 7. Logiche principali

### 7.1 Autenticazione (JWT)
1. `POST /api/auth/login` con email+password.
2. Il backend autentica (BCrypt), genera un **JWT HS256** firmato con `be.app.jwtSecret`.
3. Il client salva il token e lo invia in `Authorization: Bearer <token>`.
4. `AuthTokenFilter` valida il token a ogni richiesta e popola il `SecurityContext`.
- Il login restituisce anche **`location`** (annidato, usato dalla Cassa) e **`locationId`/`locationName`** (piatti, usati dal Manager) → entrambi i frontend funzionano.
- `UserDetailsServiceImpl` è `@Transactional` per caricare la sede (LAZY) senza errori dentro il filtro.

### 7.2 Ruoli e permessi (`WebSecurityConfig`)
- Ruoli: `USER`, `CASHIER`, `MANAGER`, `ADMIN`.
- GET su risorse core: **pubblici**. Scritture `/api/**`: **ADMIN**, con eccezioni:
  - **MANAGER**: `POST /api/requests`, `GET /api/requests/**`, `POST /api/uploads/**`,
    `POST/PUT/DELETE /api/offers/**`, `*/stock/**` della propria sede.
  - **PUT `/api/orders/*/status`**: permesso a livello security; l'autorizzazione vera
    (ADMIN o token-stazione) la fa il controller.
- `@EnableMethodSecurity` + `@PreAuthorize` per il controllo fine sui metodi.

### 7.3 Flusso richieste/approvazioni (Manager → Admin)
- Il Manager crea/modifica/elimina Prodotti e Menu → `POST /api/requests` salva una
  **ChangeRequest** in stato **PENDING** (niente scrittura reale sul dominio).
- Evento pubblicato su RabbitMQ (`request.created`) + notifica all'Admin.
- L'Admin **approva** → il `ChangeRequestService` esegue la mutazione reale
  (`productService.create/update/delete`, `menuService...`) e marca APPROVED;
  oppure **rifiuta** (REJECTED con nota). Esito pubblicato (`request.reply.*`).
- Le **Offerte** invece le gestisce direttamente il Manager (senza approvazione).

### 7.4 Token-stazione (Cucina/Tabellone/Cassa)
- Ogni sede ha un `stationToken` (UUID) generato alla creazione (write-only, non esce nelle GET).
- L'Admin lo legge: `GET /api/locations/{id}/station-token`.
- La stazione lo invia nell'header **`X-Station-Token`** e scopre la propria sede con
  `GET /api/locations/by-station-token` → `{id, name, city}`.
- `OrderController.updateStatus` autorizza il cambio stato se il chiamante è **ADMIN (JWT)**
  **oppure** presenta un `X-Station-Token` che corrisponde alla **sede dell'ordine**.

### 7.5 Notifiche event-driven (RabbitMQ)
- Exchange topic `bcc.events`:
  - `event.#` → coda `bcc.notifications` (log eventi, persistiti da `NotificationConsumer`)
  - `request.created` → `bcc.requests` (Manager→Admin)
  - `request.reply.#` → `bcc.requests.reply` (Admin→Manager)
- `NotificationPublisher` pubblica in modo **resiliente** (se il broker è giù, logga e non rompe).
- Gli eventi possono essere **taggati con la sede** → il Manager vede solo i propri.

---

## 8. Flusso dei dati (esempi)

**Ordine (filone di sede):**
```
Totem POST /api/orders ─▶ DB (status PENDING) ─▶ event.order.created (RabbitMQ)
Cassa  PUT /api/orders/{id} (modifica righe) + PUT .../status=PREPARING (X-Station-Token)
Cucina PUT .../status=READY (X-Station-Token) ─▶ event.order.status ─▶ Manager/Tabellone
Tabellone (polling GET /api/orders) mostra READY → DELIVERED
```

**Richiesta del Manager:**
```
Manager POST /api/requests (PENDING) ─▶ Admin (notifica + pagina Richieste)
Admin   PUT /api/requests/{id}/approve ─▶ esecuzione mutazione reale ─▶ APPROVED
```

---

## 9. Flusso DB

### 9.1 Modello (entità principali)
- `UserEntity` (con `role` e `location`), `LocationEntity` (+`Address`, `stationToken`, `tables`),
  `ProductEntity`, `MenuEntity`, `OfferEntity`, `IngredientEntity`, `CategoryName`.
- Join: `OrderProduct` (riga ordine: product/offer/menu + ingredienti), `MenuProduct`,
  `OfferProduct`, `LocationIngredient` (scorte sede), `UserFavoriteProduct/Menu`.
- `OrderEntity` (con `orderId`, `code`, `status`, `location`, righe).
- `ChangeRequestEntity` (richieste), `NotificationEntity` (log notifiche), `Address`.
- Molte entità estendono `AuditingField` (createdAt/updatedAt/createdBy/updatedBy/deleted).

### 9.2 Schema & ciclo di vita
- **`ddl-auto=update`**: Hibernate crea/aggiorna le tabelle all'avvio; **i dati persistono**.
- **`DataLoader`** (CommandLineRunner) semina al **primo avvio** (guardie `count()>0` per
  sezione → **idempotente**): categorie, ingredienti, prodotti, **sedi** (con token-stazione),
  utenti (admin `gspptesse@gmail.com`/`123456` e admin `admin@bluecrystal.it`/`admin123`,
  **manager** `manager@bluecrystal.it`/`manager123` assegnato alla 1ª sede), menu, offerte, ordini demo.
- In Docker, datasource/credenziali/JWT/RabbitMQ arrivano da **variabili d'ambiente**.

---

## 10. Come l'abbiamo implementata (storia tecnica)

1. **Dockerizzazione**: Dockerfile per backend e per ogni frontend (multi-stage), `docker-compose.yml`
   globale, nginx reverse-proxy same-origin, cache mount BuildKit per build veloci.
2. **Integrazione Cassa**: collegata al backend; aggiunto endpoint prodotti-per-id-categoria e
   update ordini con righe.
3. **Fix emersi all'avvio reale** (non da Docker): Spring Boot 4 usa Jackson 3 → aggiunto bean
   `ObjectMapper` (Jackson 2) e payload come `Object`; `LazyInitializationException` sul login
   manager → `@Transactional` su `loadUserByUsername`; CORS → origin dei container in allowlist.
4. **Sicurezza segreti**: JWT secret e password DB spostati su variabili d'ambiente.
5. **Unificazione (`be_unification`)**: adottata la **struttura di Giuseppe** come base e portate
   sopra, fase per fase (con compilazione verde a ogni passo), le feature admin/manager/cucina/
   tabellone/cassa: notifiche, richieste/approvazioni, login manager, token-stazione, ordini,
   offerte al manager, upload, seed manager. Le firme dei service combaciavano → adattamenti minimi.

---

## 11. Pro e contro del progetto

**Pro**
- Separazione netta dei ruoli (un'app per stazione) con un **backend unico coerente**.
- **Stateless JWT** → scalabile; **event-driven** (RabbitMQ) → disaccoppiato ed estendibile.
- **DTO/mapper** → API stabili e disaccoppiate dal DB.
- **Dockerizzato** end-to-end → "clona e `docker compose up`".
- Flusso **approvazioni** → governance centralizzata delle modifiche.
- Dati **persistenti** + seed **idempotente** → demo/dev comodi.

**Contro / debiti tecnici**
- **Token-stazione** è una soluzione "rapida": meno granulare di un vero account-stazione;
  va distribuito/ruotato con cura.
- `ddl-auto=update` non è una strategia di migration robusta → in produzione meglio **Flyway/Liquibase**.
- Le immagini **caricate** (`uploads/`) non sono servite staticamente come quelle seed → manca un ResourceHandler.
- Revoca JWT non immediata (solo scadenza).
- Scoping del Manager è **anche** lato frontend; l'enforcement per-sede a livello API è parziale.
- `createWithManager` (admin crea sede + account manager) **non ancora riconciliato** col modello
  `Address` di Giuseppe (divergenza FE↔BE da chiudere).

---

## 12. FAQ

**Perché un backend solo e non uno per app?**
Per avere una sola sorgente di verità e un solo modello dati; i frontend sono "viste" diverse.

**Perché nginx fa da reverse-proxy?**
Così il browser chiama same-origin `/api` (niente CORS) e non si "incolla" l'URL del backend nel bundle.

**Perché Cucina/Tabellone/Cassa non fanno login utente?**
Sono stazioni fisiche di sede: usano un **token-stazione** per-sede per i cambi di stato.

**Perché il Manager non scrive direttamente Prodotti/Menu?**
Per governance: passano da una **richiesta** che l'Admin approva (le Offerte invece sono dirette).

**Perché i segreti non sono nel codice?**
Per sicurezza: stanno in `.env` (non versionato); nei file versionati ci sono solo segnaposto.

**Posso ripartire da zero col DB?**
Sì: `docker compose down -v` e al primo avvio il `DataLoader` riseminerà una volta sola.

**Come configuro una nuova Cucina/Tabellone/Cassa per una sede?**
Prendi il token della sede (`GET /api/locations/{id}/station-token`), mettilo in `VITE_STATION_TOKEN`
e ricostruisci quel frontend.

**Credenziali demo?** admin `admin@bluecrystal.it`/`admin123` (o `gspptesse@gmail.com`/`123456`),
manager `manager@bluecrystal.it`/`manager123`.

---

*Backend: `be_unification` (Spring Boot 4). Per avvio/porte vedi `docker-compose.yml` e `DOCKER.md`.*
