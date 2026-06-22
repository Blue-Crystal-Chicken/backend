// ============================================================================
//  CUCINA (KDS) — CONFIGURAZIONE CENTRALE
//  Tutto ciò che può cambiare (porte, host, nomi campi, stati, colori categorie)
//  vive QUI. Per puntare a un altro backend basta modificare .env (vedi
//  .env.example) o i default qui sotto: nessun altro file va toccato.
//
//  CHI COMUNICA CON CHI (vedi ARCHITETTURA.md):
//   • Cucina  →  Backend :8080
//       - GET  /api/orders                      (polling, pubblico) ordini da preparare
//       - GET  /api/products/v1/products        (una volta, pubblico) mappa colore-categoria
//       - PUT  /api/orders/{id}/status?status=  (richiede token ADMIN/operatore)
//   • Effetto: cambiando lo stato sul Backend, il TABELLONE (polling su 8080) e
//     l'ADMIN vedono l'aggiornamento; il MANAGER della sede lo riceve via
//     Backend/RabbitMQ (FASE 2). La Cucina non chiama direttamente Admin/Manager:
//     il Backend :8080 è la sorgente unica di verità.
// ============================================================================

const env = import.meta.env;

// ── 1. Indirizzo del backend (cambia SOLO la porta per un altro ambiente) ────
export const API_CONFIG = {
  protocol: env.VITE_API_PROTOCOL || "http",
  // "??" (non "||"): se VITE_API_HOST è definita ma vuota (build Docker dietro
  // nginx reverse-proxy) resta "" → host() ritorna "" → URL relative same-origin.
  // In dev la variabile è assente → fallback su "localhost".
  host: env.VITE_API_HOST ?? "localhost",
  port: env.VITE_API_PORT || "8080",
  basePath: env.VITE_API_BASE || "/api/orders",
  productsPath: env.VITE_PRODUCTS_PATH || "/api/products/v1/products",
  // Token Bearer: opzionale (se in futuro le GET fossero protette o per un
  // operatore ADMIN). Le GET attuali sono pubbliche.
  token: env.VITE_API_TOKEN || "",
  // Token-stazione della SEDE: chiave 1-a-1 con la sede, autorizza il cambio
  // stato degli ordini di QUESTA sede. Copiala dal pannello Admin (Sedi →
  // token-stazione) e mettila in .env come VITE_STATION_TOKEN.
  stationToken: env.VITE_STATION_TOKEN || "",
};

// host vuoto ⇒ origine relativa "" (nginx proxy instrada /api al backend).
const host = () =>
  API_CONFIG.host
    ? `${API_CONFIG.protocol}://${API_CONFIG.host}:${API_CONFIG.port}`
    : "";

// ── 2. Endpoint ──────────────────────────────────────────────────────────────
export const ENDPOINTS = {
  list: () => `${host()}${API_CONFIG.basePath}`,
  updateStatus: (id) => `${host()}${API_CONFIG.basePath}/${id}/status`,
  products: () => `${host()}${API_CONFIG.productsPath}`,
  // Risolve il token-stazione nella sede di appartenenza (per mostrare quale sede).
  station: () => `${host()}/api/locations/by-station-token`,
};

// ── 3. Polling ───────────────────────────────────────────────────────────────
export const POLL_INTERVAL_MS = Number(env.VITE_POLL_MS) || 6000;

// ── 4. Enum stati ordine — DEVE rispecchiare OrderStatus nel backend ─────────
export const STATUS = {
  PENDING: "PENDING",
  PREPARING: "PREPARING",
  READY: "READY",
  DELIVERED: "DELIVERED",
  CANCELLED: "CANCELLED",
};

// Stati che la cucina deve PREPARARE (mostrati in pagina)
export const KITCHEN_STATUSES = [STATUS.PENDING, STATUS.PREPARING];

// ── 5. Azioni rapide (un solo click) ─────────────────────────────────────────
// "Fatto"      → READY      → l'ordine compare sul Tabellone come pronto/asporto
// "Cancellato" → CANCELLED  → sul Tabellone diventa rosso; Admin+Manager avvisati
export const ACTIONS = {
  done: { key: "done", label: "Fatto", status: STATUS.READY, color: "#22d3a0" },
  cancel: { key: "cancel", label: "Cancellato", status: STATUS.CANCELLED, color: "#ff5e5e" },
};

// ── 6. Mappatura campi della RISPOSTA (OrderResponse / OrderItemResponse) ─────
// Se il DTO usa nomi diversi, rimappali qui: il codice legge sempre tramite
// queste chiavi, così non "fa i capricci" se i nomi cambiano.
export const RESPONSE_FIELDS = {
  id: "id",
  status: "status",
  code: "code",
  orderId: "orderId",
  serviceType: "serviceType",
  tableNumber: "tableNumber",
  locationId: "locationId",
  items: "items",
  itemProductId: "productId",
  itemProductName: "productName",
  itemOfferId: "offerId",
  itemOfferName: "offerName",
  itemQuantity: "quantity",
  itemNote: "specialNote",
  createdAt: "createdAt",
  // campi del catalogo prodotti (per la mappa colore-categoria)
  productId: "id",
  productCategory: "category",
};

// ── 7. Come INVIARE il cambio stato (body parametrico) ───────────────────────
// Backend attuale:  PUT /api/orders/{id}/status?status=READY  (status come query)
export const STATUS_UPDATE = {
  mode: env.VITE_STATUS_MODE || "query", // "query" | "body"
  paramName: env.VITE_STATUS_PARAM || "status",
  valueMap: {
    [STATUS.READY]: STATUS.READY,
    [STATUS.CANCELLED]: STATUS.CANCELLED,
    [STATUS.PREPARING]: STATUS.PREPARING,
    [STATUS.PENDING]: STATUS.PENDING,
    [STATUS.DELIVERED]: STATUS.DELIVERED,
  },
};

// ── 8. COLORI per CATEGORIA (il cuore del colpo d'occhio) ────────────────────
// Chiavi = enum CategoryName del backend + "MENU" (item da offerta) + "DEFAULT".
// L'operatore riconosce il tipo dal colore PRIMA di leggere.
export const CATEGORY_COLORS = {
  HAMBURGER: "#ff7849", // arancio
  FRIES: "#ffd43b", // giallo
  DRINK: "#38b6ff", // blu
  SNACK: "#f4a261", // ambra
  ICE_CREAM: "#ff77c8", // rosa  → gelato
  SALAD: "#22d3a0", // verde
  WRAP: "#a3e635", // lime
  SAUCE: "#fb7185", // rosso tenue
  DESSERT: "#c084fc", // viola chiaro
  BREAKFAST: "#2dd4bf", // teal
  MENU: "#a78bfa", // viola → menu/offerta
  DEFAULT: "#8b92a8", // grigio → categoria sconosciuta
};

export const CATEGORY_LABELS = {
  HAMBURGER: "Hamburger",
  FRIES: "Patatine",
  DRINK: "Bibita",
  SNACK: "Snack",
  ICE_CREAM: "Gelato",
  SALAD: "Insalata",
  WRAP: "Wrap",
  SAUCE: "Salsa",
  DESSERT: "Dolce",
  BREAKFAST: "Colazione",
  MENU: "Menu",
  DEFAULT: "Altro",
};

// ── 9. Tipo di servizio (badge) ──────────────────────────────────────────────
export const SERVICE_TYPE_LABELS = {
  DINE_IN: "Sala",
  TAKEAWAY: "Asporto",
  DELIVERY: "Consegna",
};

// ── 10. Soglie attesa (minuti) per evidenziare gli ordini in ritardo ─────────
export const WAIT_THRESHOLDS = {
  warning: Number(env.VITE_WAIT_WARNING) || 6,
  danger: Number(env.VITE_WAIT_DANGER) || 12,
};
