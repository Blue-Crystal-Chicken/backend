import { useState, useEffect, useCallback, useRef, useMemo } from "react";
import requestService from "../service/RequestService";

const POLL_MS = 10000;

// Filtri per stato (stile toolbar Notifiche admin)
const STATUS_FILTERS = [
  { key: "ALL", label: "Tutte" },
  { key: "PENDING", label: "In attesa" },
  { key: "APPROVED", label: "Approvate" },
  { key: "REJECTED", label: "Rifiutate" },
];

const STATUS_META = {
  PENDING: { label: "In attesa", color: "#fbbf24" },
  APPROVED: { label: "Approvata", color: "#22d3a0" },
  REJECTED: { label: "Rifiutata", color: "#ff5e5e" },
};

const TYPE_LABELS = {
  CREATE_PRODUCT: "Nuovo prodotto",
  UPDATE_PRODUCT: "Modifica prodotto",
  DELETE_PRODUCT: "Elimina prodotto",
  CREATE_MENU: "Nuovo menu",
  UPDATE_MENU: "Modifica menu",
  DELETE_MENU: "Elimina menu",
  CREATE_OFFER: "Nuova offerta",
};

const fmtDate = (iso) =>
  iso ? new Date(iso).toLocaleString("it-IT", { day: "2-digit", month: "2-digit", hour: "2-digit", minute: "2-digit" }) : "—";

function StatusBadge({ status }) {
  const m = STATUS_META[status] || { label: status, color: "#8b92a8" };
  return (
    <span
      className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-bold whitespace-nowrap"
      style={{ backgroundColor: `${m.color}18`, color: m.color, border: `1px solid ${m.color}40` }}
    >
      <span className="w-1.5 h-1.5 rounded-full" style={{ backgroundColor: m.color }} />
      {m.label}
    </span>
  );
}

function Kpi({ label, value, color }) {
  return (
    <div className="bg-[#111318] border border-white/5 rounded-[18px] p-[20px_22px] relative overflow-hidden">
      <div className="absolute top-0 left-0 right-0 h-[2px] opacity-70" style={{ backgroundColor: color }} />
      <div className="text-[11px] font-semibold tracking-[1px] uppercase text-[#8b92a8] mb-3">{label}</div>
      <div className="font-['Syne'] text-[28px] font-bold leading-none" style={{ color }}>{value}</div>
    </div>
  );
}

export default function Richieste() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const timer = useRef(null);

  const load = useCallback(async () => {
    try {
      const data = await requestService.mine();
      setItems(Array.isArray(data) ? data : []);
      setError(null);
    } catch (e) {
      setError(e?.response?.data || e.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
    timer.current = setInterval(load, POLL_MS);
    return () => clearInterval(timer.current);
  }, [load]);

  const [filter, setFilter] = useState("ALL");
  const filtered = useMemo(
    () => items.filter((r) => filter === "ALL" || r.status === filter),
    [items, filter]
  );

  const count = (s) => items.filter((i) => i.status === s).length;

  if (loading) {
    return (
      <div className="flex h-[60vh] items-center justify-center text-[14px] font-semibold text-[#38b6ff] tracking-widest font-['Syne'] uppercase animate-pulse">
        Caricamento richieste…
      </div>
    );
  }

  return (
    <div className="p-[28px_32px] max-w-[1440px] mx-auto text-[#f0f4ff]">
      <div className="flex items-end justify-between mb-8">
        <div>
          <h1 className="font-['Syne'] text-[26px] font-extrabold tracking-tight mb-1">Le mie richieste</h1>
          <p className="text-[13px] text-[#8b92a8] uppercase tracking-wide">
            Creazioni in attesa di approvazione dell'Admin
          </p>
        </div>
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <Kpi label="Totali" value={items.length} color="#38b6ff" />
        <Kpi label="In attesa" value={count("PENDING")} color="#fbbf24" />
        <Kpi label="Approvate" value={count("APPROVED")} color="#22d3a0" />
        <Kpi label="Rifiutate" value={count("REJECTED")} color="#ff5e5e" />
      </div>

      {error && (
        <div className="bg-[#ff5e5e]/10 border border-[#ff5e5e]/20 text-[#ff5e5e] rounded-xl px-4 py-2.5 text-[12px] font-semibold mb-4">
          Errore: {String(error)}
        </div>
      )}

      {/* Filtri per stato (stile Notifiche) */}
      <div className="flex items-center gap-3 flex-wrap mb-5">
        <div className="flex items-center gap-1 bg-[#111318] border border-white/5 rounded-xl p-1">
          {STATUS_FILTERS.map((f) => (
            <button
              key={f.key}
              onClick={() => setFilter(f.key)}
              className={`px-3 py-1.5 rounded-lg text-[12px] font-semibold transition-all ${
                filter === f.key ? "bg-[#38b6ff] text-[#111318]" : "text-[#8b92a8] hover:text-[#f0f4ff]"
              }`}
            >
              {f.label}
            </button>
          ))}
        </div>
      </div>

      <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-white/5">
                {["Tipo", "Descrizione", "Stato", "Creata", "Esito"].map((h) => (
                  <th key={h} className="text-left text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-white/[0.04]">
              {filtered.length === 0 ? (
                <tr><td colSpan={5} className="py-10 text-center text-[13px] text-[#4e5566]">
                  {items.length === 0 ? "Nessuna richiesta inviata." : "Nessuna richiesta per questo filtro."}
                </td></tr>
              ) : filtered.map((r) => (
                <tr key={r.id} className="hover:bg-[#181c23] transition-colors">
                  <td className="py-3 px-3 text-[12px] font-semibold text-[#38b6ff]">{TYPE_LABELS[r.type] || r.type}</td>
                  <td className="py-3 px-3 text-[13px] text-[#f0f4ff]">{r.summary}</td>
                  <td className="py-3 px-3"><StatusBadge status={r.status} /></td>
                  <td className="py-3 px-3 text-[12px] text-[#8b92a8] whitespace-nowrap">{fmtDate(r.createdAt)}</td>
                  <td className="py-3 px-3 text-[12px] text-[#8b92a8]">
                    {r.status === "APPROVED" && (r.resultMessage || "Approvata")}
                    {r.status === "REJECTED" && (r.resolutionNote ? `Rifiutata: ${r.resolutionNote}` : "Rifiutata")}
                    {r.status === "PENDING" && "—"}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
