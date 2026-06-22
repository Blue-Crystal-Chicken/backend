// ============================================================================
//  Livello API del tabellone — usa fetch nativo (nessuna dipendenza esterna).
//  Tutte le URL e i nomi parametro arrivano da config.js: per cambiare backend
//  o forma della chiamata non si tocca questo file.
// ============================================================================

import { ENDPOINTS, API_CONFIG, STATUS_UPDATE } from "../config";

const authHeaders = () =>
  API_CONFIG.token ? { Authorization: `Bearer ${API_CONFIG.token}` } : {};

// Token-stazione: autorizza il cambio stato lato backend (PUT .../status).
const stationHeaders = () =>
  API_CONFIG.stationToken ? { "X-Station-Token": API_CONFIG.stationToken } : {};

// Scopre la sede a cui appartiene il token-stazione. Ritorna {id,name,city} o null.
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

// GET lista ordini (oggetto su cui si fa il polling dello stato)
export async function fetchOrders(signal) {
  const res = await fetch(ENDPOINTS.list(), {
    method: "GET",
    headers: { Accept: "application/json", ...authHeaders() },
    signal,
  });
  if (!res.ok) throw new Error(`GET ordini fallita: HTTP ${res.status}`);
  return res.json();
}

// PUT cambio stato — body/param costruiti in modo PARAMETRICO da config.
// `logicalStatus` è uno dei valori di STATUS; viene tradotto col valueMap
// nel valore realmente accettato dal DTO del backend.
export async function updateOrderStatus(id, logicalStatus) {
  const { mode, paramName, valueMap } = STATUS_UPDATE;
  const value = valueMap[logicalStatus] ?? logicalStatus;

  let url = ENDPOINTS.updateStatus(id);
  const opts = { method: "PUT", headers: { ...authHeaders(), ...stationHeaders() } };

  if (mode === "body") {
    opts.headers["Content-Type"] = "application/json";
    opts.body = JSON.stringify({ [paramName]: value });
  } else {
    // default: query param  ->  ...?status=READY
    const qs = new URLSearchParams({ [paramName]: value });
    url += `?${qs.toString()}`;
  }

  const res = await fetch(url, opts);
  if (!res.ok) throw new Error(`PUT stato fallita: HTTP ${res.status}`);
  return res.json();
}
