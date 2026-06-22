// ============================================================================
//  Livello API della Cucina — fetch nativo, nessuna dipendenza esterna.
//  URL e forma chiamata arrivano da config.js: per cambiare backend non si
//  tocca questo file.
// ============================================================================

import { ENDPOINTS, API_CONFIG, STATUS_UPDATE } from "../config";

const authHeaders = () =>
  API_CONFIG.token ? { Authorization: `Bearer ${API_CONFIG.token}` } : {};

// Token-stazione della sede: autorizza il cambio stato lato backend.
const stationHeaders = () =>
  API_CONFIG.stationToken ? { "X-Station-Token": API_CONFIG.stationToken } : {};

// Scopre a quale SEDE appartiene il token-stazione configurato.
// Ritorna { id, name, city } oppure null se manca/è errato (così la UI lo segnala).
export async function fetchStation() {
  if (!API_CONFIG.stationToken) return null;
  try {
    const res = await fetch(ENDPOINTS.station(), { headers: { Accept: "application/json", ...stationHeaders() } });
    if (!res.ok) return null;
    return await res.json();
  } catch {
    return null;
  }
}

// GET ordini (pubblico) — oggetto su cui si fa il polling
export async function fetchOrders(signal) {
  const res = await fetch(ENDPOINTS.list(), {
    method: "GET",
    headers: { Accept: "application/json", ...authHeaders() },
    signal,
  });
  if (!res.ok) throw new Error(`GET ordini: HTTP ${res.status}`);
  return res.json();
}

// GET catalogo prodotti (pubblico) — per la mappa productId -> categoria
export async function fetchProducts(signal) {
  const res = await fetch(ENDPOINTS.products(), {
    method: "GET",
    headers: { Accept: "application/json", ...authHeaders() },
    signal,
  });
  if (!res.ok) throw new Error(`GET prodotti: HTTP ${res.status}`);
  return res.json();
}

// PUT cambio stato — richiede token ADMIN/operatore (vedi config.token).
// `logicalStatus` viene tradotto col valueMap nel valore accettato dal DTO.
export async function updateOrderStatus(id, logicalStatus) {
  const { mode, paramName, valueMap } = STATUS_UPDATE;
  const value = valueMap[logicalStatus] ?? logicalStatus;

  let url = ENDPOINTS.updateStatus(id);
  const opts = { method: "PUT", headers: { ...authHeaders(), ...stationHeaders() } };

  if (mode === "body") {
    opts.headers["Content-Type"] = "application/json";
    opts.body = JSON.stringify({ [paramName]: value });
  } else {
    const qs = new URLSearchParams({ [paramName]: value });
    url += `?${qs.toString()}`;
  }

  const res = await fetch(url, opts);
  if (!res.ok) {
    if (res.status === 401 || res.status === 403) {
      throw new Error(
        `cambio stato non autorizzato (HTTP ${res.status}): imposta VITE_STATION_TOKEN con il token-stazione della sede`
      );
    }
    throw new Error(`PUT stato: HTTP ${res.status}`);
  }
  return res.json();
}
