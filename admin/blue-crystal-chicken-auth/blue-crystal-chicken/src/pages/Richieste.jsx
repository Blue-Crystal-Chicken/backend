import { useState, useEffect, useCallback, useRef } from "react";
import requestService from "../service/RequestService";

const POLL_MS = 10000;

// Filtri per stato (stile toolbar Notifiche). key "" = tutte (param server).
const STATUS_FILTERS = [
  { key: "", label: "Tutte" },
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
    <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-bold whitespace-nowrap"
      style={{ backgroundColor: `${m.color}18`, color: m.color, border: `1px solid ${m.color}40` }}>
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

// Mostra in modo leggibile i campi principali del payload JSON
function PayloadPreview({ payload }) {
  let obj = {};
  try { obj = JSON.parse(payload || "{}"); } catch { /* ignore */ }
  const entries = Object.entries(obj).filter(([, v]) => v != null && v !== "" && !(Array.isArray(v) && v.length === 0));
  if (!entries.length) return <span className="text-[#4e5566] text-[12px]">—</span>;
  return (
    <div className="flex flex-wrap gap-1.5">
      {entries.map(([k, v]) => (
        <span key={k} className="text-[11px] bg-[#181c23] border border-white/5 rounded-md px-2 py-0.5 text-[#8b92a8]">
          <span className="text-[#4e5566]">{k}:</span> <span className="text-[#f0f4ff]">{String(v)}</span>
        </span>
      ))}
    </div>
  );
}

export default function Richieste() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState({});
  const [filter, setFilter] = useState("PENDING");
  const timer = useRef(null);

  const load = useCallback(async () => {
    try {
      const data = await requestService.getAll(filter || undefined);
      setItems(Array.isArray(data) ? data : []);
      setError(null);
    } catch (e) {
      setError(e?.response?.data || e.message);
    } finally {
      setLoading(false);
    }
  }, [filter]);

  useEffect(() => {
    setLoading(true);
    load();
    timer.current = setInterval(load, POLL_MS);
    return () => clearInterval(timer.current);
  }, [load]);

  const approve = async (id) => {
    setBusy((b) => ({ ...b, [id]: true }));
    try { await requestService.approve(id); await load(); }
    catch (e) { alert("Approvazione fallita: " + (e?.response?.data || e.message)); }
    finally { setBusy((b) => { const { [id]: _, ...r } = b; return r; }); }
  };

  const reject = async (id) => {
    const note = window.prompt("Motivo del rifiuto (opzionale):", "");
    if (note === null) return;
    setBusy((b) => ({ ...b, [id]: true }));
    try { await requestService.reject(id, note); await load(); }
    catch (e) { alert("Rifiuto fallito: " + (e?.response?.data || e.message)); }
    finally { setBusy((b) => { const { [id]: _, ...r } = b; return r; }); }
  };

  const count = (s) => items.filter((i) => i.status === s).length;

  return (
    <div className="p-[28px_32px] max-w-[1440px] mx-auto text-[#f0f4ff]">
      <div className="flex items-end justify-between mb-8">
        <div>
          <h1 className="font-['Syne'] text-[26px] font-extrabold tracking-tight mb-1">Richieste</h1>
          <p className="text-[13px] text-[#8b92a8] uppercase tracking-wide">
            Approva o rifiuta le richieste dei manager
          </p>
        </div>
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <Kpi label="In vista" value={items.length} color="#38b6ff" />
        <Kpi label="In attesa" value={count("PENDING")} color="#fbbf24" />
        <Kpi label="Approvate" value={count("APPROVED")} color="#22d3a0" />
        <Kpi label="Rifiutate" value={count("REJECTED")} color="#ff5e5e" />
      </div>

      {/* Filtri per stato (stile Notifiche) */}
      <div className="flex items-center gap-3 flex-wrap mb-5">
        <div className="flex items-center gap-1 bg-[#111318] border border-white/5 rounded-xl p-1">
          {STATUS_FILTERS.map((f) => (
            <button
              key={f.key || "ALL"}
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

      {error && (
        <div className="bg-[#ff5e5e]/10 border border-[#ff5e5e]/20 text-[#ff5e5e] rounded-xl px-4 py-2.5 text-[12px] font-semibold mb-4">
          Errore: {String(error)}
        </div>
      )}

      {loading ? (
        <div className="py-16 text-center text-[13px] text-[#38b6ff] font-['Syne'] uppercase tracking-widest animate-pulse">Caricamento…</div>
      ) : items.length === 0 ? (
        <div className="border border-dashed border-white/10 rounded-[18px] py-16 text-center text-[#4e5566] text-[13px] font-semibold uppercase tracking-wider">
          Nessuna richiesta
        </div>
      ) : (
        <div className="space-y-3">
          {items.map((r) => (
            <div key={r.id} className="bg-[#111318] border border-white/5 rounded-[18px] p-5">
              <div className="flex items-start justify-between gap-4 flex-wrap">
                <div className="min-w-0 flex-1">
                  <div className="flex items-center gap-2 mb-2 flex-wrap">
                    <span className="text-[11px] font-bold px-2 py-0.5 rounded-full bg-[#38b6ff]/10 text-[#38b6ff] border border-[#38b6ff]/20 uppercase tracking-wider">
                      {TYPE_LABELS[r.type] || r.type}
                    </span>
                    <StatusBadge status={r.status} />
                    {r.locationName && (
                      <span className="text-[11px] text-[#8b92a8]">sede: <span className="text-[#f0f4ff] font-semibold">{r.locationName}</span></span>
                    )}
                  </div>
                  <div className="font-['Syne'] text-[16px] font-bold text-[#f0f4ff] mb-2">{r.summary}</div>
                  <PayloadPreview payload={r.payload} />
                  <div className="text-[11px] text-[#4e5566] mt-2">
                    da {r.requestedByEmail || "—"} · {fmtDate(r.createdAt)}
                    {r.status !== "PENDING" && r.resolvedAt && ` · gestita ${fmtDate(r.resolvedAt)}`}
                    {r.resultMessage && ` · ${r.resultMessage}`}
                    {r.resolutionNote && ` · nota: ${r.resolutionNote}`}
                  </div>
                </div>

                {r.status === "PENDING" && (
                  <div className="flex items-center gap-2">
                    <button onClick={() => reject(r.id)} disabled={busy[r.id]}
                      className="flex items-center gap-2 rounded-xl px-4 py-2.5 text-[12px] font-bold tracking-wide border transition-all disabled:opacity-40"
                      style={{ backgroundColor: "#ff5e5e18", color: "#ff5e5e", borderColor: "#ff5e5e40" }}>
                      Rifiuta
                    </button>
                    <button onClick={() => approve(r.id)} disabled={busy[r.id]}
                      className="flex items-center gap-2 rounded-xl px-5 py-2.5 text-[12px] font-extrabold tracking-wide transition-all disabled:opacity-40"
                      style={{ backgroundColor: "#22d3a0", color: "#0b0c10" }}>
                      Approva
                    </button>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
