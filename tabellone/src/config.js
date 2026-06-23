// ============================================================================
//  CONFIGURAZIONE CENTRALE DEL TABELLONE
//  Tutto ciò che può cambiare (porte, host, nomi campi, stati) vive QUI.
//  Per puntare a un altro backend basta modificare il file .env (vedi
//  .env.example) oppure i default qui sotto: nessun altro file va toccato.
// ============================================================================

const env = import.meta.env;

// ── 1. Indirizzo del backend ────────────────────────────────────────────────
// Cambia SOLO la porta (o l'host) per spostarti su un altro ambiente.
export const API_CONFIG = {
  protocol: env.VITE_API_PROTOCOL || "http",
  // "??" (non "||"): se VITE_API_HOST è definita ma vuota (build Docker dietro
  // nginx reverse-proxy) resta "" → root() ritorna URL relative same-origin.
  // In dev la variabile è assente → fallback su "localhost".
  host: env.VITE_API_HOST ?? "localhost",
  port: env.VITE_API_PORT || "8080",
  basePath: env.VITE_API_BASE || "/api/orders",
  // Token Bearer opzionale: serve solo se in futuro gli endpoint GET/PUT
  // verranno protetti. Lasciare vuoto se l'endpoint è pubblico.
  token: env.VITE_API_TOKEN || "",
  // Token-stazione della SEDE: autorizza il cambio stato degli ordini di QUESTA
  // sede (PUT .../status ora è protetto). Copialo dal pannello Admin → Sedi.
  stationToken: env.VITE_STATION_TOKEN || "",
  // Id della sede di questo tabellone (più semplice del token).
  locationId: env.VITE_LOCATION_ID || "",
};

// host vuoto ⇒ origine relativa (nginx proxy instrada /api al backend).
const origin = () =>
  API_CONFIG.host
    ? `${API_CONFIG.protocol}://${API_CONFIG.host}:${API_CONFIG.port}`
    : "";
const root = () => `${origin()}${API_CONFIG.basePath}`;

// ── 2. Endpoint usati dal tabellone ─────────────────────────────────────────
export const ENDPOINTS = {
  // GET lista ordini su cui si fa polling (ritorna ogni ordine col suo status)
  list: () => root(),
  // PUT cambio stato di un ordine
  updateStatus: (id) => `${root()}/${id}/status`,
  // Elenco di tutte le sedi (per il dropdown di selezione).
  locations: () => `${origin()}/api/locations`,
};

// ── 3. Polling ───────────────────────────────────────────────────────────────
export const POLL_INTERVAL_MS = Number(env.VITE_POLL_MS) || 8000;

// ── 4. Enum stati ordine — DEVE rispecchiare OrderStatus nel backend ─────────
// Se nel backend cambiano i nomi dell'enum, aggiornali SOLO qui.
export const STATUS = {
  PENDING: "PENDING",
  PREPARING: "PREPARING",
  READY: "READY",
  DELIVERED: "DELIVERED",
  CANCELLED: "CANCELLED",
};

export const STATUS_LABELS = {
  [STATUS.PENDING]: "In attesa",
  [STATUS.PREPARING]: "In preparazione",
  [STATUS.READY]: "Pronto",
  [STATUS.DELIVERED]: "Consegnato",
  [STATUS.CANCELLED]: "Annullato",
};

export const STATUS_COLORS = {
  [STATUS.PENDING]: "#fbbf24",
  [STATUS.PREPARING]: "#38b6ff",
  [STATUS.READY]: "#22d3a0",
  [STATUS.DELIVERED]: "#a78bfa",
  [STATUS.CANCELLED]: "#ff5e5e",
};

// ── 5. Mappatura dei campi della RISPOSTA (OrderResponse) ────────────────────
// Se il DTO del backend usa nomi diversi, rimappali qui: il tabellone leggerà
// sempre tramite queste chiavi, così non "fa i capricci" se i nomi cambiano.
export const RESPONSE_FIELDS = {
  id: "id",
  status: "status",
  code: "code",
  orderId: "orderId",
  serviceType: "serviceType",
  tableNumber: "tableNumber",
  locationId: "locationId", // sede dell'ordine (per filtrare alla sede del tabellone)
  items: "items",
  itemName: "productName", // nome prodotto dentro ogni item
  itemOfferName: "offerName",
  itemQuantity: "quantity",
  createdAt: "createdAt",
};

// ── 6. Come INVIARE il cambio stato (body parametrico) ───────────────────────
// Il backend attuale espone:  PUT /api/orders/{id}/status?status=PREPARING
// (status come query param). Se il DTO/endpoint cambiasse e volesse il valore
// nel body o con un altro nome, basta cambiare qui — niente capricci del DTO.
export const STATUS_UPDATE = {
  mode: env.VITE_STATUS_MODE || "query", // "query" | "body"
  paramName: env.VITE_STATUS_PARAM || "status", // nome del parametro/campo
  // Mappa: stato logico del tabellone -> valore accettato dal backend.
  // Default = identità. Cambia i valori se l'enum del DTO ha nomi diversi.
  valueMap: {
    [STATUS.PENDING]: STATUS.PENDING,
    [STATUS.PREPARING]: STATUS.PREPARING,
    [STATUS.READY]: STATUS.READY,
    [STATUS.DELIVERED]: STATUS.DELIVERED,
    [STATUS.CANCELLED]: STATUS.CANCELLED,
  },
};

// ── 7. Tipo di servizio (badge) ──────────────────────────────────────────────
export const SERVICE_TYPE_LABELS = {
  DINE_IN: "Sala",
  TAKEAWAY: "Asporto",
  DELIVERY: "Consegna",
};

export const SERVICE_TYPE_COLORS = {
  DINE_IN: "#38b6ff",
  TAKEAWAY: "#22d3a0",
  DELIVERY: "#a78bfa",
};

// ── 8. Colonne del tabellone ─────────────────────────────────────────────────
// Ogni colonna mostra gli ordini con uno degli stati elencati e (se definito)
// permette di avanzare l'ordine allo stato `advanceTo` con un click.
export const COLUMNS = [
  {
    key: "preparing",
    title: "In Preparazione",
    accent: STATUS_COLORS[STATUS.PREPARING],
    statuses: [STATUS.PENDING, STATUS.PREPARING],
    advanceTo: STATUS.READY,
    advanceLabel: "Segna Pronto",
  },
  {
    key: "ready",
    title: "Pronti — Asporto",
    accent: STATUS_COLORS[STATUS.READY],
    statuses: [STATUS.READY],
    advanceTo: STATUS.DELIVERED,
    advanceLabel: "Consegnato",
  },
];

// ── 9. Soglie di attesa per evidenziare gli ordini in ritardo (minuti) ───────
export const WAIT_THRESHOLDS = {
  warning: Number(env.VITE_WAIT_WARNING) || 8, // giallo
  danger: Number(env.VITE_WAIT_DANGER) || 15, // rosso
};
