# Tabellone Ordini — Blue Crystal Chicken

Display da cucina/ritiro che fa **polling** sugli ordini del backend admin e li
mostra in due colonne:

- **In Preparazione** — ordini `PENDING` + `PREPARING`
- **Pronti — Asporto** — ordini `READY`

Ogni card può **avanzare lo stato** con un click (Preparazione → Pronto →
Consegnato), usando la chiamata di cambio stato del backend.

## Avvio

```bash
cd tabellone
cp .env.example .env       # poi modifica le porte se serve
npm install                # se l'install fallisce per proxy aziendale:
                           #   NODE_OPTIONS=--use-system-ca npm install
npm run dev
```

Apri l'URL che stampa Vite (default `http://localhost:5180`).

## Tutto è parametrico — file `src/config.js`

Per cambiare ambiente **non si tocca il codice**, solo `.env` (o i default in
`config.js`):

| Cosa | Variabile `.env` | Default |
|------|------------------|---------|
| Protocollo backend | `VITE_API_PROTOCOL` | `http` |
| Host backend | `VITE_API_HOST` | `localhost` |
| **Porta backend** | `VITE_API_PORT` | `8080` |
| Base path API | `VITE_API_BASE` | `/api/orders` |
| Token Bearer (opz.) | `VITE_API_TOKEN` | _(vuoto)_ |
| Porta dev server | `VITE_PORT` | `5180` |
| Intervallo polling (ms) | `VITE_POLL_MS` | `8000` |
| Modalità cambio stato | `VITE_STATUS_MODE` | `query` |
| Nome parametro stato | `VITE_STATUS_PARAM` | `status` |

### Endpoint usati
- **Polling:** `GET {base}` → lista ordini, si legge il campo `status` di ognuno.
- **Cambio stato:** `PUT {base}/{id}/status?status=READY`.

### Se il DTO/enum del backend cambia i nomi
In `src/config.js`:
- `STATUS` → valori dell'enum `OrderStatus` (PENDING, PREPARING, READY, …).
- `RESPONSE_FIELDS` → nomi dei campi nella risposta (es. se `status` si chiamasse
  diversamente).
- `STATUS_UPDATE.valueMap` → traduce lo stato logico nel valore accettato dal DTO,
  così la chiamata **non fa i capricci** anche se i nomi non corrispondono.
- `STATUS_UPDATE.mode` → `"query"` o `"body"` per inviare lo stato come query param
  o come corpo JSON.
- `COLUMNS` → quali stati finiscono in quale colonna.
