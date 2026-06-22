# Cucina (KDS) — Blue Crystal Chicken

Display da cucina a **pagina unica**: mostra gli ordini **da preparare**
(`PENDING` + `PREPARING`) in card grandi e leggibili, con **bordo colorato per
categoria** così l'operatore capisce cosa preparare *prima ancora di leggere*.

Ogni card ha **due bottoni rapidi** (un click):

- **Fatto** → stato `READY` → l'ordine compare sul **Tabellone** come pronto/asporto
- **Cancellato** → stato `CANCELLED` → sul **Tabellone** diventa rosso

In entrambi i casi il cambio stato viaggia verso il **Backend :8080**, quindi
**Admin** e **Manager della sede** vedono l'aggiornamento (il Backend è la
sorgente unica di verità; il Manager lo riceve via Backend/RabbitMQ in FASE 2).

## Colori categorie

Bordo sinistro **segmentato**: un segmento per ogni categoria presente nell'ordine.
Ogni articolo ha inoltre il suo colore e l'etichetta. Mappa in `src/config.js`
(`CATEGORY_COLORS`), allineata all'enum `CategoryName` del backend:

Hamburger · Patatine · Bibita · Snack · **Gelato** · Insalata · Wrap · Salsa ·
Dolce · Colazione · **Menu** (item da offerta).

> La categoria non è nell'ordine: si ricava da `GET /api/products/v1/products`
> (mappa `productId → categoria`). Gli item con `offerId` sono **Menu**.

## Avvio

```bash
cd cucina
cp .env.example .env        # imposta VITE_API_TOKEN (serve per Fatto/Cancellato)
npm install                 # se fallisce per proxy: NODE_OPTIONS=--use-system-ca npm install
npm run dev                 # default http://localhost:5183
```

Oppure doppio click su `start.bat` / `stop.bat` (Windows).

## Comunicazioni (vedi ARCHITETTURA.md)

| Verso | Endpoint | Auth |
|-------|----------|------|
| GET ordini (polling) | `GET {backend}/api/orders` | pubblico |
| GET catalogo (colori) | `GET {backend}/api/products/v1/products` | pubblico |
| Fatto / Cancellato | `PUT {backend}/api/orders/{id}/status?status=...` | **token ADMIN** |

## Tutto parametrico — `src/config.js`

| Cosa | Variabile `.env` | Default |
|------|------------------|---------|
| Porta backend | `VITE_API_PORT` | `8080` |
| Path ordini | `VITE_API_BASE` | `/api/orders` |
| Path prodotti | `VITE_PRODUCTS_PATH` | `/api/products/v1/products` |
| Token (per il PUT) | `VITE_API_TOKEN` | _(vuoto)_ |
| Porta dev server | `VITE_PORT` | `5183` |
| Intervallo polling | `VITE_POLL_MS` | `6000` |
| Modalità cambio stato | `VITE_STATUS_MODE` | `query` |
| Nome parametro stato | `VITE_STATUS_PARAM` | `status` |

Se il DTO/enum del backend cambia: aggiorna `STATUS`, `RESPONSE_FIELDS`,
`STATUS_UPDATE.valueMap`, `CATEGORY_COLORS`/`CATEGORY_LABELS` in `config.js`.
