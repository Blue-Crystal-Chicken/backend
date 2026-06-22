# Blue Crystal Chicken — Feature future / idee

> Raccolta di miglioramenti non ancora implementati. Ultimo aggiornamento: **2026-06-21**.
> Ordinati per priorità indicativa. Spuntare quando completati.

---

## 1. Report → Magazzino Snapshot per-sede
**Stato:** da fare · **Sforzo:** basso

Oggi la sezione `Report → Risorse → Magazzino` (`components/report/MagazzinoSnapshot.jsx`)
mostra il **catalogo ingredienti globale** (giacenza a livello catena, da `GET /api/ingredients`).

**Idea:** quando nell'header del Report è selezionata una sede (`locationId`), lo snapshot
deve mostrare le **scorte di quella sede** invece del catalogo globale.

**Come:**
- Endpoint già esistente: `GET /api/locations/{id}/stock` (ritorna `LocationIngredient[]`
  con `ingredient` e `quantity`; `location` è `@JsonIgnore`).
- In `useReport.js`: quando `locationId` cambia, fetchare lo stock della sede (o passare
  `locationId` al componente e fare il fetch lì).
- In `MagazzinoSnapshot.jsx`: se arriva lo stock di sede usarlo (quantità/valore per quella
  filiale), altrimenti fallback al catalogo globale. Il calcolo valore usa
  `li.quantity * li.ingredient.price`.
- KPI extra possibili: "ingredienti a zero in questa sede", confronto con la media catena.

---

## 2. Endpoint `POST /api/auth/logout` (o rimozione chiamata FE)
**Stato:** da decidere · **Sforzo:** banale

Il frontend chiama `POST /api/auth/logout` (`api/authService.js`) ma il backend non espone
la rotta (solo `/login`, `/register`, `/me`). Oggi è innocuo (catch silenzioso, JWT stateless),
ma è un mismatch.

**Opzioni:** aggiungere un `@PostMapping("/logout")` no-op lato backend (utile se in futuro si
vuole una blacklist token), **oppure** togliere la chiamata di rete dal FE.

---

## 3. Allineare immagine prodotti `imgPath` ↔ `imageUrl`
**Stato:** da fare · **Sforzo:** basso

`ProductService.tsx` (FE) dichiara `imageUrl`, ma il backend `ProductResponse` espone `imgPath`
→ la preview prodotto resta sempre placeholder. Allineare il nome campo (lato FE o aggiungere
alias lato BE).

## 4. Riattivare il serving immagini
**Stato:** da fare · **Sforzo:** basso

`security/WebMvcConfig.java` ha il resource handler `/images/**` **commentato** → le immagini
caricate (prodotti e offerte) non si vedono. L'upload funziona, manca solo il serving statico.

## 5. Rimozione menu da un'offerta
**Stato:** da fare · **Sforzo:** basso

Il backend espone solo `POST /api/offers/{offerId}/menus/{menuId}` (aggiunta), non la DELETE.
Il modal Marketing già lo segnala. Aggiungere l'endpoint di rimozione + bottone nel FE.

---

## 6. Pagine "dormienti": Utenti e Menu
**Stato:** feature nuova · **Sforzo:** medio

Il backend ha già il CRUD completo, ma mancano le pagine FE e le voci in sidebar:
- **Utenti** — `GET/PUT/DELETE /api/users/v1/users/*` (gestione staff).
- **Menu** — `/api/menus/*` (search, price, products, POST/PUT, add/remove product, DELETE);
  oggi i menu sono usati solo in sola-lettura dentro Marketing.

## 7. `GET /api/auth/me` per sessione robusta
**Stato:** da fare · **Sforzo:** basso

Oggi l'utente si ricostruisce dal token in `localStorage`. Validare la sessione al reload con
`/me` eviterebbe token scaduti "fantasma".

---

## 8. Fase 2 — Ruolo MANAGER con approvazioni (RabbitMQ)
**Stato:** progettato, da fare · **Sforzo:** alto · vedi `HANDOFF.md §4`

Ruolo `MANAGER` che esegue le stesse azioni dell'admin ma come **richieste** (via coda
`bcc.requests`) che l'admin **approva/rifiuta**; solo all'approvazione avviene la mutazione reale.
Richiede: entità `ChangeRequest` (PENDING/APPROVED/REJECTED), consumer, endpoint approve/reject,
`@PreAuthorize` differenziati, UI manager + pagina approvazioni.

---

## 9. Pipeline test Java negli script di avvio
**Stato:** da fare · **Sforzo:** basso

Oggi `start.ps1` / `restart.ps1` avviano il backend con `mvn spring-boot:run` e i test sono
saltati (`-DskipTests`). Aggiungere un passo di **pipeline locale** che esegue i test prima
dell'avvio.

**Come:**
- In `start.ps1` aggiungere uno step (es. flag `-WithTests` / `-SkipTests`) che lancia
  `& $Mvn -q test` nella cartella `backend` **prima** di `spring-boot:run`.
- Controllo esito con `$LASTEXITCODE`: se i test falliscono → stampare il report e
  **interrompere** l'avvio (o solo avvisare, configurabile).
- In alternativa uno script dedicato `test.ps1` richiamato anche dalla CI (vedi
  *Road to Production §D — CI/CD*), così la stessa pipeline gira in locale e su GitHub Actions.
- Prerequisito utile: completare il **Maven wrapper** (`./mvnw`) così la pipeline non dipende
  dal `mvn` di sistema.

> Nota: oggi esiste solo `NotificationApplicationTests` (smoke) lato servizio notifiche; il
> backend principale ha pochi/nessun test → la pipeline ha valore reale solo insieme a
> *Road to Production §C — Test*.

---

## 10. `.env`: indirizzo IP del servizio notifiche (Giuseppe) impostato in autonomia
**Stato:** da valutare · **Sforzo:** basso→medio · **vedi analisi sotto**

Oggi `VITE_NOTIFICATION_API_URL` nel `.env` è un IP **hardcodato** (`http://192.168.1.35:8085`).
Su DHCP quell'IP cambia → va aggiornato a mano. Idea: che lo script di avvio lo **rilevi/imposti
da solo**.

### Analisi di fattibilità

**Vincolo:** Vite legge il `.env` **all'avvio** del dev server → l'IP va scritto nel `.env`
*prima* di `npm run dev` (quindi nel `start.ps1`). La scrittura del `.env` da PowerShell è
banale (riscrivere la riga `VITE_NOTIFICATION_API_URL=...`).

| Approccio | Fattibilità | Pro | Contro |
|---|---|---|---|
| **A. DHCP reservation / IP statico** sul router per il PC di Giuseppe | ✅ alta (zero codice) | L'IP non cambia mai → hardcode una volta sola | Serve accesso al router; non è "automatico" via script |
| **B. Hostname mDNS** (`http://NOME-PC-GIUSEPPE.local:8085`) | ✅ media-alta | Nessun IP, si adatta da solo; 1 riga nel `.env` | Risoluzione `.local` su Windows non sempre affidabile; serve conoscere l'hostname |
| **C. Scan della subnet** in `start.ps1`: prova `Test-NetConnection :8085` su `192.168.1.1-254` in parallelo, poi verifica la "firma" (es. `GET /v3/api-docs` o 401 su `/`) e scrive l'IP nel `.env` | ⚠️ media (fragile) | Davvero autonomo, nessuna config manuale | Lento/rumoroso (fino a 254 host), può trovare il server sbagliato, va filtrato bene; "sa di port-scan" |
| **D. Parametro/prompt** `start.ps1 -NotificationHost <ip\|host>` che scrive il `.env` | ✅ alta | Semplice, esplicito, riusabile | Non è "autonomo": l'IP lo dai tu all'avvio |
| **E. Service discovery** (il servizio di Giuseppe pubblica il suo IP su un registro condiviso) | ❌ bassa | Robusto a livello enterprise | Sovradimensionato per 2 PC, richiede infra extra lato Giuseppe |

**Raccomandazione:** per un uso reale **A** (DHCP reservation) o **B** (hostname mDNS) sono le più
solide e a costo quasi nullo. Se serve "click-and-go" senza toccare il router, **D** (parametro
che riscrive il `.env`) è il compromesso migliore: semplice e affidabile. **C** (auto-scan) è
realizzabile ma fragile/lento — da considerare solo come *fallback* se B/D non bastano.

**Schema implementazione (variante D, la più pratica):**
- `start.ps1` accetta `-NotificationHost` (default = valore già nel `.env`).
- Se passato, riscrive la riga `VITE_NOTIFICATION_API_URL=http://<host>:8085` nel `.env`
  (regex replace) prima di avviare Vite.
- Validazione veloce con `Test-NetConnection -Port 8085` → warning se irraggiungibile.

---
---

# Road to Production — checklist

> Cosa separa "app finita e funzionante" da "app usabile in azienda con dati e utenti reali".
> Non è altro codice di feature: è il lavoro che rende l'app **affidabile sotto stress reale**.
> Nessuna di queste voci richiede di rifare l'architettura — sono tutte incrementali.
> Ordine = priorità consigliata. Spuntare man mano.

## A. Sicurezza (priorità massima)
- [x] **Segreti fuori dal codice** — credenziali DB, JWT secret e password ora arrivano da
      variabili d'ambiente (`.env`, non versionato); nei `application.properties` restano solo
      segnaposto fittizi. Vecchio TODO: erano in chiaro in `application.properties`. Spostare in variabili d'ambiente /
      secret manager; non committare mai i valori reali.
- [ ] **Password admin reale** — eliminare `123456` e il seeding dell'admin di default in prod
      (o forzare cambio password al primo accesso).
- [ ] **HTTPS** — TLS davanti al backend (reverse proxy: Nginx/Traefik/caddy).
- [ ] **CORS ristretto** — consentire solo l'origin del frontend di produzione, non `*`.
- [ ] **Rate limiting** sul login (anti brute-force) e idealmente sugli endpoint pubblici.
- [ ] **Gestione token** — scadenza ragionevole + refresh token; valutare `/api/auth/me`
      (vedi §7) per validare la sessione lato server.
- [ ] **Validazione input** uniforme su tutti i `@RequestBody` + gestione errori centralizzata
      (`@ControllerAdvice`) invece di `catch` che restituiscono stringhe.

## B. Dati e persistenza (priorità massima)
- [ ] **Migrazioni DB** — sostituire `ddl-auto=create-drop` con `validate` + **Flyway** o
      **Liquibase**. È il cambio più importante per non perdere i dati a ogni avvio.
- [ ] **Paginazione** — gli endpoint lista (`/api/orders`, `/api/products`, …) restituiscono
      *tutto*: con decine di migliaia di righe esplodono. Aggiungere `Pageable` + filtri
      server-side; adeguare le tabelle FE.
- [ ] **Coordinate sedi nel DB** — salvare lat/lon su `LocationEntity` invece di geocodificare
      via Nominatim dal browser a ogni load (non scala oltre poche sedi, e dipende da internet).
- [ ] **Backup automatici** del database + procedura di restore testata.

## C. Affidabilità e qualità
- [ ] **Test** — almeno unit sui service critici e integration sugli endpoint principali
      (oggi gira con `-DskipTests`). Bug come il `@Transactional` mancante o le ricorsioni
      Jackson sono esattamente ciò che i test intercettano.
- [ ] **Niente `catch` silenziosi** — loggare gli errori e restituire status/messaggi coerenti.
- [ ] **Logging strutturato** (livelli, correlazione richieste) al posto dei `System.out.println`.
- [ ] **Audit dei `@Transactional`** — verificare che ogni metodo che scrive (specie le bulk
      `@Modifying`) sia transazionale.

## D. Operatività / DevOps
- [ ] **Dockerizzare** backend e frontend (oltre a RabbitMQ già in compose) + un
      `docker-compose` di produzione con Postgres gestito.
- [ ] **CI/CD** — build + test + deploy automatici (GitHub Actions o simili).
- [ ] **Monitoraggio** — health check (Spring Actuator), metriche, alerting di base.
- [ ] **Maven wrapper** — completare `.mvn/wrapper` così `./mvnw` funziona ovunque (oggi serve
      il mvn di sistema).

## E. Frontend / UX
- [ ] **Code splitting** — il bundle è > 500KB; lazy-load delle pagine per ridurre il caricamento.
- [ ] **Error boundary** React globale — evitare che un singolo errore di render lasci la pagina
      bianca (già capitato con la ricorsione Jackson).
- [ ] **Coerenza service layer** — completata la migrazione `LocationService` da `fetch` ad
      axiosClient; verificare che non restino altre chiamate raw.
- [ ] **Serving immagini** — riattivare `/images/**` (vedi §4) o passare a storage esterno (S3).

## F. Solo se diventa un prodotto SaaS multi-cliente (campionato diverso)
- [ ] Multi-tenancy (isolamento dati per cliente)
- [ ] Audit log delle azioni admin
- [ ] Conformità GDPR (consensi, cancellazione dati, data retention)
- [ ] SLA, scalabilità orizzontale, gestione carichi
