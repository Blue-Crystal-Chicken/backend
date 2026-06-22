import { useState, useMemo } from "react";
import StatusBadge from "./StatusBadge";
import { STATUS_LABELS } from "../../hooks/useOrdini";

const fmt = (n) =>
  new Intl.NumberFormat("it-IT", { style: "currency", currency: "EUR" }).format(n ?? 0);

const SERVICE_TYPE_LABELS = { DINE_IN: "Sala", TAKEAWAY: "Asporto", DELIVERY: "Consegna" };

function formatTime(iso) {
  if (!iso) return "—";
  return new Date(iso).toLocaleString("it-IT", {
    day: "2-digit", month: "2-digit",
    hour: "2-digit", minute: "2-digit",
  });
}

const SKEL_WIDTHS = [55, 70, 45, 65, 50, 80, 60, 40];

function SkeletonRows({ cols }) {
  return Array.from({ length: 6 }).map((_, i) => (
    <tr key={`skel-${i}`} className="border-b border-white/[0.04] animate-pulse">
      {Array.from({ length: cols }).map((_, j) => (
        <td key={j} className="py-3.5 px-3">
          <div
            className="h-3 bg-white/[0.05] rounded"
            style={{ width: `${SKEL_WIDTHS[(i + j) % SKEL_WIDTHS.length]}%` }}
          />
        </td>
      ))}
    </tr>
  ));
}

// Solo le colonne categoriali hanno un dropdown di filtro.
const EMPTY_FILTERS = {
  sede: "", servizio: "", pagamento: "", stato: "",
};

// Valore mostrato (e usato per il match esatto del dropdown) per le colonne filtrabili.
function columnValues(order, locationName) {
  return {
    sede: locationName(order.locationId) ?? "—",
    servizio: SERVICE_TYPE_LABELS[order.serviceType] ?? order.serviceType ?? "—",
    pagamento: order.paymentType ?? "—",
    stato: STATUS_LABELS[order.status] ?? order.status ?? "—",
  };
}

function ColumnFilter({ value, onChange, options }) {
  return (
    <select
      value={value}
      onChange={(e) => onChange(e.target.value)}
      className={`w-full bg-[#0e1015] border rounded-md px-2 py-1 text-[11px] focus:outline-none focus:border-[#38b6ff] transition-colors cursor-pointer ${
        value ? "border-[#38b6ff] text-[#f0f4ff]" : "border-white/5 text-[#8b92a8]"
      }`}
    >
      <option value="" className="bg-[#111318] text-[#8b92a8]">Tutti</option>
      {options.map((opt) => (
        <option key={opt} value={opt} className="bg-[#111318] text-[#f0f4ff]">{opt}</option>
      ))}
    </select>
  );
}

export default function TabellaOrdini({
  orders,
  showLocation,
  locationName,
  onDetail,
  onDelete,
  loading,
}) {
  const colCount = showLocation ? 8 : 7;

  const [filters, setFilters] = useState(EMPTY_FILTERS);
  const setFilter = (key) => (val) => setFilters((f) => ({ ...f, [key]: val }));
  const hasActiveFilters = Object.values(filters).some((v) => v !== "");

  // Opzioni preimpostate dei dropdown: valori distinti presenti nei dati, in ordine alfabetico.
  const options = useMemo(() => {
    const sets = {};
    Object.keys(EMPTY_FILTERS).forEach((k) => (sets[k] = new Set()));
    orders.forEach((order) => {
      const vals = columnValues(order, locationName);
      Object.keys(sets).forEach((k) => {
        if (vals[k] != null && vals[k] !== "") sets[k].add(vals[k]);
      });
    });
    const out = {};
    Object.keys(sets).forEach((k) => {
      out[k] = [...sets[k]].sort((a, b) => String(a).localeCompare(String(b), "it"));
    });
    return out;
  }, [orders, locationName]);

  // Filtri sovrapponibili: ogni filtro selezionato deve combaciare (AND, match esatto).
  const filteredOrders = useMemo(() => {
    if (!hasActiveFilters) return orders;
    const active = Object.entries(filters).filter(([, v]) => v !== "");
    return orders.filter((order) => {
      const vals = columnValues(order, locationName);
      return active.every(([key, val]) => vals[key] === val);
    });
  }, [orders, filters, hasActiveFilters, locationName]);

  return (
    <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
      <div className="flex items-center justify-between mb-5">
        <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff]">
          Lista ordini
        </h3>
        <div className="flex items-center gap-2">
          {hasActiveFilters && (
            <button
              onClick={() => setFilters(EMPTY_FILTERS)}
              className="text-[11px] font-semibold px-2.5 py-1 rounded-full bg-[#181c23] border border-white/5 text-[#8b92a8] hover:text-[#ff5e5e] hover:border-[#ff5e5e]/40 transition-all"
            >
              Azzera filtri
            </button>
          )}
          <span className="text-[11px] font-semibold px-2 py-0.5 rounded-full bg-[#38b6ff]/10 text-[#38b6ff] border border-[#38b6ff]/20">
            {loading ? "..." : `${filteredOrders.length} record`}
          </span>
        </div>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full">
          <thead>
            <tr className="border-b border-white/5">
              <th className="text-left text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3">
                # Ordine
              </th>
              {showLocation && (
                <th className="text-left text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3">
                  Sede
                </th>
              )}
              <th className="text-left text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3">
                Servizio
              </th>
              <th className="text-left text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3">
                Pagamento
              </th>
              <th className="text-left text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3">
                Totale
              </th>
              <th className="text-left text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3">
                Stato
              </th>
              <th className="text-left text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3">
                Data / Ora
              </th>
              <th className="text-right text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3">
                Azioni
              </th>
            </tr>
            {/* Riga filtri per-colonna (dropdown preimpostati, sovrapponibili) */}
            <tr className="border-b border-white/5">
              <th className="pb-3 px-3" />
              {showLocation && (
                <th className="pb-3 px-3 align-top">
                  <ColumnFilter value={filters.sede} onChange={setFilter("sede")} options={options.sede} />
                </th>
              )}
              <th className="pb-3 px-3 align-top">
                <ColumnFilter value={filters.servizio} onChange={setFilter("servizio")} options={options.servizio} />
              </th>
              <th className="pb-3 px-3 align-top">
                <ColumnFilter value={filters.pagamento} onChange={setFilter("pagamento")} options={options.pagamento} />
              </th>
              <th className="pb-3 px-3" />
              <th className="pb-3 px-3 align-top">
                <ColumnFilter value={filters.stato} onChange={setFilter("stato")} options={options.stato} />
              </th>
              <th className="pb-3 px-3" />
              <th className="pb-3 px-3" />
            </tr>
          </thead>
          <tbody className="divide-y divide-white/[0.04]">
            {loading ? (
              <SkeletonRows cols={colCount} />
            ) : filteredOrders.length === 0 ? (
              <tr>
                <td
                  colSpan={colCount}
                  className="text-center py-10 text-[13px] text-[#4e5566]"
                >
                  {orders.length === 0
                    ? "Nessun ordine trovato per i filtri selezionati."
                    : "Nessun ordine corrisponde ai filtri di colonna."}
                </td>
              </tr>
            ) : (
              filteredOrders.map((order) => (
                <tr key={order.id} className="hover:bg-[#181c23] transition-colors group">
                  <td className="py-3 px-3 font-['Syne'] font-semibold text-[#38b6ff] text-[13px]">
                    #{order.orderId ?? order.id}
                  </td>
                  {showLocation && (
                    <td className="py-3 px-3 text-[13px] text-[#8b92a8]">
                      {locationName(order.locationId)}
                    </td>
                  )}
                  <td className="py-3 px-3">
                    <span className="bg-[#181c23] px-2.5 py-1 rounded-md text-[11px] font-semibold border border-white/5 tracking-wider uppercase text-[#38b6ff]">
                      {SERVICE_TYPE_LABELS[order.serviceType] ?? order.serviceType ?? "—"}
                    </span>
                  </td>
                  <td className="py-3 px-3 text-[13px] text-[#8b92a8]">
                    {order.paymentType ?? "—"}
                  </td>
                  <td className="py-3 px-3 font-['Syne'] font-bold text-[#22d3a0] text-[13px]">
                    {fmt(order.totalAt)}
                  </td>
                  <td className="py-3 px-3">
                    <StatusBadge status={order.status} />
                  </td>
                  <td className="py-3 px-3 text-[13px] text-[#8b92a8]">
                    {formatTime(order.createdAt)}
                  </td>
                  <td className="py-3 px-3 text-right">
                    <div className="flex justify-end gap-2 opacity-60 group-hover:opacity-100 transition-opacity">
                      <button
                        onClick={() => onDetail(order)}
                        className="p-2 rounded-lg bg-[#181c23] border border-white/5 hover:border-[#38b6ff]/50 text-[#8b92a8] hover:text-[#38b6ff] transition-all"
                        title="Visualizza dettaglio"
                      >
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                          <circle cx="12" cy="12" r="3" />
                        </svg>
                      </button>
                      <button
                        onClick={() => {
                          if (window.confirm(`Eliminare l'ordine #${order.orderId ?? order.id}?`))
                            onDelete(order.id);
                        }}
                        className="p-2 rounded-lg bg-[#181c23] border border-white/5 hover:border-[#ff5e5e]/50 text-[#8b92a8] hover:text-[#ff5e5e] transition-all"
                        title="Elimina ordine"
                      >
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <polyline points="3 6 5 6 21 6" />
                          <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                        </svg>
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
