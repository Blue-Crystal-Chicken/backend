import { useState, useEffect, useCallback, useMemo } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axiosClient from "../api/axiosClient";
import ingredientService from "../service/IngredientService";

const fmtCur = (n) => new Intl.NumberFormat("it-IT", { style: "currency", currency: "EUR" }).format(n ?? 0);
const fmtQty = (n) => new Intl.NumberFormat("it-IT", { maximumFractionDigits: 2 }).format(n ?? 0);

const Icon = {
  back: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...p}><line x1="19" y1="12" x2="5" y2="12" /><polyline points="12 19 5 12 12 5" /></svg>),
  box: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...p}><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z" /><polyline points="3.27 6.96 12 12.01 20.73 6.96" /><line x1="12" y1="22.08" x2="12" y2="12" /></svg>),
  euro: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...p}><path d="M14 21a8 8 0 1 1 0-16" /><line x1="4" y1="10" x2="13" y2="10" /><line x1="4" y1="14" x2="11" y2="14" /></svg>),
  alert: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...p}><path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" /><line x1="12" y1="9" x2="12" y2="13" /><line x1="12" y1="17" x2="12.01" y2="17" /></svg>),
  save: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...p}><path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z" /><polyline points="17 21 17 13 7 13 7 21" /><polyline points="7 3 7 8 15 8" /></svg>),
  trash: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...p}><polyline points="3 6 5 6 21 6" /><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" /></svg>),
  plus: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" {...p}><line x1="12" y1="5" x2="12" y2="19" /><line x1="5" y1="12" x2="19" y2="12" /></svg>),
  close: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...p}><line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" /></svg>),
};

const KpiCard = ({ label, value, icon, subtext, accentColor }) => (
  <div className="bg-[#111318] border border-white/5 rounded-[18px] p-[22px_24px] relative overflow-hidden">
    <div className="absolute top-0 left-0 right-0 h-[2px] opacity-70" style={{ backgroundColor: accentColor }} />
    <div className="flex items-center justify-between mb-[14px]">
      <span className="text-[11px] font-semibold tracking-[1px] uppercase text-[#8b92a8]">{label}</span>
      <span className="w-[34px] h-[34px] rounded-[9px] flex items-center justify-center" style={{ background: `${accentColor}18`, color: accentColor }}>{icon}</span>
    </div>
    <div className="font-['Syne'] text-[28px] font-bold leading-none mb-2 text-[#f0f4ff] truncate">{value}</div>
    {subtext && <div className="text-[12px] font-medium text-[#8b92a8] truncate">{subtext}</div>}
  </div>
);

const StockBadge = ({ qty, threshold }) => {
  let cfg;
  if (qty == null || qty <= 0) cfg = { label: "Esaurito", c: "#ff5e5e" };
  else if (qty < threshold) cfg = { label: "Critico", c: "#fbbf24" };
  else cfg = { label: "Disponibile", c: "#22d3a0" };
  return (
    <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-semibold border" style={{ color: cfg.c, background: `${cfg.c}14`, borderColor: `${cfg.c}33` }}>
      <span className="h-1.5 w-1.5 rounded-full" style={{ background: cfg.c }} />
      {cfg.label}
    </span>
  );
};

export default function DettaglioSede() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [location, setLocation] = useState(null);
  const [stock, setStock] = useState([]);
  const [allIngredients, setAllIngredients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [threshold, setThreshold] = useState(10);

  // editing inline delle quantità: { [ingredientId]: value }
  const [edits, setEdits] = useState({});

  // modale aggiunta ingrediente alla sede
  const [isAddOpen, setIsAddOpen] = useState(false);
  const [addForm, setAddForm] = useState({ ingredientId: "", quantity: "" });
  const [saving, setSaving] = useState(false);

  const fetchData = useCallback(async () => {
    try {
      const [loc, st, ings] = await Promise.all([
        axiosClient.get(`/api/locations/${id}`).then((r) => r.data).catch(() => null),
        ingredientService.getStockByLocation(id),
        ingredientService.getAll(),
      ]);
      setLocation(loc);
      setStock(st || []);
      setAllIngredients(ings || []);
      setEdits({});
    } catch (err) {
      console.error("Errore caricamento dettaglio sede:", err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const ingIdOf = (li) => li.ingredient?.id ?? li.id?.ingredientId;
  const ingNameOf = (li) => li.ingredient?.name ?? `Ingrediente #${ingIdOf(li)}`;
  const priceOf = (li) => li.ingredient?.price ?? 0;

  const metrics = useMemo(() => {
    const count = stock.length;
    const value = stock.reduce((s, li) => s + (li.quantity ?? 0) * priceOf(li), 0);
    const low = stock.filter((li) => (li.quantity ?? 0) < threshold).length;
    return { count, value, low };
  }, [stock, threshold]);

  // mappa scorta corrente per ingredientId (per il rifornimento dalla modale)
  const stockByIngredient = useMemo(() => {
    const m = {};
    stock.forEach((li) => { m[ingIdOf(li)] = li; });
    return m;
  }, [stock]);

  const handleSaveQty = async (li) => {
    const ingredientId = ingIdOf(li);
    const raw = edits[ingredientId];
    if (raw == null || raw === "") return;
    try {
      await ingredientService.updateStock(id, ingredientId, Number(raw));
      fetchData();
    } catch (err) {
      alert("Aggiornamento fallito: " + (err.response?.data?.message || err.message));
    }
  };

  const handleRemove = async (li) => {
    if (!window.confirm(`Rimuovere "${ingNameOf(li)}" dalle scorte di questa sede?`)) return;
    try {
      await ingredientService.removeStock(id, ingIdOf(li));
      fetchData();
    } catch (err) {
      alert("Rimozione fallita: " + (err.response?.data?.message || err.message));
    }
  };

  const handleAdd = async (e) => {
    e.preventDefault();
    if (!addForm.ingredientId) return;
    const ingredientId = Number(addForm.ingredientId);
    const delta = Number(addForm.quantity) || 0;
    setSaving(true);
    try {
      const existing = stockByIngredient[ingredientId];
      if (existing) {
        // Ingrediente già a scorta → rifornimento: somma alla giacenza attuale.
        await ingredientService.updateStock(id, ingredientId, (existing.quantity ?? 0) + delta);
      } else {
        // Ingrediente non presente → crea la riga di scorta.
        await ingredientService.addStock(id, ingredientId, delta);
      }
      setIsAddOpen(false);
      setAddForm({ ingredientId: "", quantity: "" });
      fetchData();
    } catch (err) {
      alert("Aggiunta fallita: " + (err.response?.data?.message || err.message));
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="flex h-screen items-center justify-center bg-[#0b0c10] text-[14px] font-semibold text-[#38b6ff] tracking-widest font-['Syne'] uppercase animate-pulse">
        Caricamento giacenze sede...
      </div>
    );
  }

  return (
    <div className="p-[28px_32px] max-w-[1440px] mx-auto text-[#f0f4ff] bg-[#0b0c10] min-h-screen">

      {/* Header */}
      <div className="flex items-end justify-between mb-8 flex-wrap gap-4">
        <div className="flex items-center gap-4">
          <button onClick={() => navigate("/sedi")} title="Torna alle sedi" className="p-2.5 rounded-xl bg-[#111318] border border-white/5 text-[#8b92a8] hover:text-[#38b6ff] hover:border-[#38b6ff] transition-all">
            <Icon.back width="16" height="16" />
          </button>
          <div>
            <h1 className="font-['Syne'] text-[26px] font-extrabold tracking-tight mb-1">
              {location?.name || location?.city || `Sede #${id}`}
            </h1>
            <p className="text-[13px] text-[#8b92a8] uppercase tracking-wide">
              {location ? `${location.address || ""}${location.address ? ", " : ""}${location.city || ""}` : "Giacenze magazzino della sede"}
            </p>
          </div>
        </div>
        <button onClick={() => setIsAddOpen(true)} disabled={allIngredients.length === 0} className="flex items-center gap-2 bg-[#38b6ff] text-[#111318] rounded-xl px-4 py-2.5 text-[12px] font-bold tracking-wide hover:bg-[#38b6ff]/90 transition-all shadow-lg shadow-[#38b6ff]/10 disabled:opacity-40">
          <Icon.plus width="14" height="14" /> AGGIUNGI A SCORTE
        </button>
      </div>

      {/* KPI */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
        <KpiCard label="Voci in giacenza" value={metrics.count} icon={<Icon.box width="16" height="16" />} subtext="Ingredienti stoccati qui" accentColor="#38b6ff" />
        <KpiCard label="Valore scorte sede" value={fmtCur(metrics.value)} icon={<Icon.euro width="16" height="16" />} subtext="Σ quantità × prezzo" accentColor="#22d3a0" />
        <KpiCard label="Sotto soglia" value={metrics.low} icon={<Icon.alert width="16" height="16" />} subtext={`Sotto ${fmtQty(threshold)} unità`} accentColor="#fbbf24" />
      </div>

      {/* Tabella scorte */}
      <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
        <div className="flex items-center justify-between mb-5 flex-wrap gap-3">
          <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff]">Scorte di sede</h3>
          <label className="flex items-center gap-2 text-[11px] font-semibold uppercase tracking-wider text-[#8b92a8]">
            Soglia
            <input type="number" min="0" value={threshold} onChange={(e) => setThreshold(Number(e.target.value) || 0)} className="w-16 bg-[#181c23] border border-white/5 rounded-lg px-2 py-1.5 text-[12px] text-[#f0f4ff] focus:outline-none focus:border-[#38b6ff]" />
          </label>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-white/5">
                <th className="text-left text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3">Ingrediente</th>
                <th className="text-left text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3">Quantità in sede</th>
                <th className="text-left text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3">Valore</th>
                <th className="text-left text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3">Stato</th>
                <th className="text-right text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3">Azioni</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/[0.04]">
              {stock.map((li) => {
                const iid = ingIdOf(li);
                const editVal = edits[iid] ?? li.quantity ?? 0;
                const dirty = edits[iid] != null && Number(edits[iid]) !== (li.quantity ?? 0);
                return (
                  <tr key={iid} className="hover:bg-[#181c23] transition-colors group">
                    <td className="py-3 px-3 font-['Syne'] font-semibold text-[#f0f4ff] text-[13px]">{ingNameOf(li)}</td>
                    <td className="py-3 px-3">
                      <input
                        type="number" step="0.01" min="0"
                        value={editVal}
                        onChange={(e) => setEdits({ ...edits, [iid]: e.target.value })}
                        className={`w-24 bg-[#181c23] border rounded-lg px-2.5 py-1.5 text-[13px] text-[#f0f4ff] focus:outline-none transition-all ${dirty ? "border-[#38b6ff]" : "border-white/5 focus:border-[#38b6ff]"}`}
                      />
                    </td>
                    <td className="py-3 px-3 font-['Syne'] font-bold text-[#22d3a0] text-[13px]">{fmtCur((li.quantity ?? 0) * priceOf(li))}</td>
                    <td className="py-3 px-3"><StockBadge qty={li.quantity} threshold={threshold} /></td>
                    <td className="py-3 px-3 text-right">
                      <div className="flex justify-end gap-2">
                        <button onClick={() => handleSaveQty(li)} disabled={!dirty} title="Salva quantità" className="p-2 rounded-lg bg-[#181c23] border border-white/5 hover:border-[#22d3a0]/50 text-[#8b92a8] hover:text-[#22d3a0] transition-all disabled:opacity-30 disabled:hover:border-white/5">
                          <Icon.save width="14" height="14" />
                        </button>
                        <button onClick={() => handleRemove(li)} title="Rimuovi dalla sede" className="p-2 rounded-lg bg-[#181c23] border border-white/5 hover:border-[#ff5e5e]/50 text-[#8b92a8] hover:text-[#ff5e5e] transition-all">
                          <Icon.trash width="14" height="14" />
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })}
              {stock.length === 0 && (
                <tr><td colSpan="5" className="text-center py-8 text-[13px] text-[#4e5566]">Nessuna scorta registrata per questa sede.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modale aggiunta */}
      {isAddOpen && (
        <div className="fixed inset-0 bg-[#000]/70 backdrop-blur-md z-50 flex items-center justify-center p-4">
          <div className="bg-[#111318] border border-white/10 rounded-[22px] max-w-md w-full p-6 shadow-2xl relative">
            <div className="flex items-center justify-between mb-6 border-b border-white/5 pb-4">
              <h2 className="font-['Syne'] text-[18px] font-bold text-[#f0f4ff]">Aggiungi a scorte</h2>
              <button onClick={() => setIsAddOpen(false)} className="text-[#8b92a8] hover:text-[#f0f4ff] transition-colors"><Icon.close width="18" height="18" /></button>
            </div>
            <form onSubmit={handleAdd} className="space-y-4">
              <div>
                <label className="block text-[10px] font-semibold uppercase tracking-wider text-[#8b92a8] mb-1.5">Ingrediente</label>
                <select required value={addForm.ingredientId} onChange={(e) => setAddForm({ ...addForm, ingredientId: e.target.value })} className="w-full bg-[#181c23] border border-white/5 rounded-xl px-3 py-2.5 text-[13px] text-[#f0f4ff] focus:outline-none focus:border-[#38b6ff] transition-all">
                  <option value="" className="bg-[#111318]">Seleziona...</option>
                  {allIngredients.map((i) => {
                    const cur = stockByIngredient[i.id];
                    return (
                      <option key={i.id} value={i.id} className="bg-[#111318]">
                        {i.name}{cur ? ` — in scorta: ${fmtQty(cur.quantity)}` : " — non presente"}
                      </option>
                    );
                  })}
                </select>
              </div>
              <div>
                <label className="block text-[10px] font-semibold uppercase tracking-wider text-[#8b92a8] mb-1.5">Quantità da aggiungere</label>
                <input type="number" step="0.01" min="0" required value={addForm.quantity} onChange={(e) => setAddForm({ ...addForm, quantity: e.target.value })} className="w-full bg-[#181c23] border border-white/5 rounded-xl px-4 py-2.5 text-[13px] text-[#f0f4ff] focus:outline-none focus:border-[#38b6ff] transition-all" placeholder="Es. 50" />
                {addForm.ingredientId && stockByIngredient[Number(addForm.ingredientId)] && (
                  <p className="mt-1.5 text-[11px] text-[#8b92a8]">
                    Giacenza dopo: <span className="text-[#22d3a0] font-semibold">{fmtQty((stockByIngredient[Number(addForm.ingredientId)].quantity ?? 0) + (Number(addForm.quantity) || 0))}</span>
                  </p>
                )}
              </div>
              <div className="flex justify-end gap-3 pt-4 border-t border-white/5 mt-6">
                <button type="button" onClick={() => setIsAddOpen(false)} className="bg-transparent border border-white/5 hover:bg-white/5 px-4 py-2 rounded-xl text-[12px] font-medium text-[#8b92a8] transition-all">Annulla</button>
                <button type="submit" disabled={saving} className="bg-[#38b6ff] text-[#111318] px-5 py-2 rounded-xl text-[12px] font-bold tracking-wide hover:bg-[#38b6ff]/90 transition-all disabled:opacity-50">{saving ? "AGGIUNTA..." : "AGGIUNGI"}</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
