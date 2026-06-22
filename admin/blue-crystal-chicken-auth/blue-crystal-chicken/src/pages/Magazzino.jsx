import { useState, useEffect, useCallback, useMemo } from "react";
import ingredientService from "../service/IngredientService";

// Helpers di formattazione (allineati allo stile delle altre pagine)
const fmtCur = (n) => new Intl.NumberFormat("it-IT", { style: "currency", currency: "EUR" }).format(n ?? 0);
const fmtQty = (n) => new Intl.NumberFormat("it-IT", { maximumFractionDigits: 2 }).format(n ?? 0);

// ─── ICONE SVG (no emoji, da regola di progetto) ─────────────────────────────
const Icon = {
  box: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...p}><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z" /><polyline points="3.27 6.96 12 12.01 20.73 6.96" /><line x1="12" y1="22.08" x2="12" y2="12" /></svg>),
  euro: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...p}><path d="M14 21a8 8 0 1 1 0-16" /><line x1="4" y1="10" x2="13" y2="10" /><line x1="4" y1="14" x2="11" y2="14" /></svg>),
  alert: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...p}><path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" /><line x1="12" y1="9" x2="12" y2="13" /><line x1="12" y1="17" x2="12.01" y2="17" /></svg>),
  empty: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...p}><circle cx="12" cy="12" r="10" /><line x1="4.93" y1="4.93" x2="19.07" y2="19.07" /></svg>),
  search: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...p}><circle cx="11" cy="11" r="8" /><line x1="21" y1="21" x2="16.65" y2="16.65" /></svg>),
  refresh: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" {...p}><polyline points="23 4 23 10 17 10" /><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" /></svg>),
  edit: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...p}><path d="M11 5H6a2 2 0 0 0-2 2v11a2 2 0 0 0 2 2h11a2 2 0 0 0 2-2v-5" /><path d="M18.5 2.5a2.121 2.121 0 1 1 3 3L12 15l-4 1 1-4 9.5-9.5z" /></svg>),
  trash: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...p}><polyline points="3 6 5 6 21 6" /><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" /></svg>),
  close: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...p}><line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" /></svg>),
  plus: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" {...p}><line x1="12" y1="5" x2="12" y2="19" /><line x1="5" y1="12" x2="19" y2="12" /></svg>),
};

// KPI Card (stile Command Center)
const KpiCard = ({ label, value, icon, subtext, accentColor }) => (
  <div className="bg-[#111318] border border-white/5 rounded-[18px] p-[22px_24px] relative overflow-hidden transition-all duration-200 hover:border-[#38b6ff]/25 hover:-translate-y-0.5 group">
    <div className="absolute top-0 left-0 right-0 h-[2px] opacity-70" style={{ backgroundColor: accentColor }} />
    <div className="flex items-center justify-between mb-[14px]">
      <span className="text-[11px] font-semibold tracking-[1px] uppercase text-[#8b92a8]">{label}</span>
      <span className="w-[34px] h-[34px] rounded-[9px] flex items-center justify-center transition-transform group-hover:scale-110" style={{ background: `${accentColor}18`, color: accentColor }}>
        {icon}
      </span>
    </div>
    <div className="font-['Syne'] text-[28px] font-bold leading-none mb-2 text-[#f0f4ff] truncate">{value}</div>
    {subtext && <div className="text-[12px] font-medium text-[#8b92a8] truncate">{subtext}</div>}
  </div>
);

// Badge stato giacenza in base alla soglia
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

const EMPTY_FORM = { name: "", description: "", price: "", quantity: "" };

export default function Magazzino() {
  const [ingredients, setIngredients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [query, setQuery] = useState("");
  const [threshold, setThreshold] = useState(10);

  // Modale CRUD
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [currentId, setCurrentId] = useState(null);
  const [formData, setFormData] = useState(EMPTY_FORM);
  const [saving, setSaving] = useState(false);

  const fetchData = useCallback(async () => {
    try {
      const data = await ingredientService.getAll();
      setIngredients(data || []);
    } catch (err) {
      console.error("Errore caricamento ingredienti:", err);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  // Filtro client-side (ricerca su nome/descrizione)
  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return ingredients;
    return ingredients.filter((i) =>
      i.name?.toLowerCase().includes(q) || i.description?.toLowerCase().includes(q)
    );
  }, [ingredients, query]);

  // Metriche KPI derivate
  const metrics = useMemo(() => {
    const total = ingredients.length;
    const value = ingredients.reduce((sum, i) => sum + (i.quantity ?? 0) * (i.price ?? 0), 0);
    const low = ingredients.filter((i) => (i.quantity ?? 0) > 0 && (i.quantity ?? 0) < threshold).length;
    const empty = ingredients.filter((i) => (i.quantity ?? 0) <= 0).length;
    return { total, value, low, empty };
  }, [ingredients, threshold]);

  // Gestione modale
  const openCreate = () => {
    setIsEditing(false);
    setCurrentId(null);
    setFormData(EMPTY_FORM);
    setIsModalOpen(true);
  };

  const openEdit = (ing) => {
    setIsEditing(true);
    setCurrentId(ing.id);
    setFormData({
      name: ing.name ?? "",
      description: ing.description ?? "",
      price: ing.price ?? "",
      quantity: ing.quantity ?? "",
    });
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      if (isEditing) {
        await ingredientService.update(currentId, formData);
      } else {
        await ingredientService.create(formData);
      }
      setIsModalOpen(false);
      fetchData();
    } catch (err) {
      alert("Operazione fallita: " + (err.response?.data?.message || err.message));
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id, name) => {
    if (!window.confirm(`Eliminare definitivamente l'ingrediente "${name}"?`)) return;
    try {
      await ingredientService.remove(id);
      fetchData();
    } catch (err) {
      alert("Errore durante l'eliminazione: " + (err.response?.data?.message || err.message));
    }
  };

  if (loading) {
    return (
      <div className="flex h-screen items-center justify-center bg-[#0b0c10] text-[14px] font-semibold text-[#38b6ff] tracking-widest font-['Syne'] uppercase animate-pulse">
        Sincronizzazione Magazzino Cryo...
      </div>
    );
  }

  return (
    <div className="p-[28px_32px] max-w-[1440px] mx-auto text-[#f0f4ff] bg-[#0b0c10] min-h-screen">

      {/* Header */}
      <div className="flex items-end justify-between mb-8 flex-wrap gap-4">
        <div>
          <h1 className="font-['Syne'] text-[26px] font-extrabold tracking-tight mb-1">Magazzino — Catalogo Ingredienti</h1>
          <p className="text-[13px] text-[#8b92a8] uppercase tracking-wide">Anagrafica materie prime e giacenza di catena</p>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={() => { setRefreshing(true); fetchData(); }}
            className="flex items-center gap-2 bg-[#111318] border border-white/5 rounded-xl px-3.5 py-2.5 text-[12px] font-medium text-[#8b92a8] hover:border-[#38b6ff] hover:text-[#38b6ff] transition-all"
          >
            <Icon.refresh width="13" height="13" className={refreshing ? "animate-spin" : ""} />
            Aggiorna
          </button>
          <button
            onClick={openCreate}
            className="flex items-center gap-2 bg-[#38b6ff] text-[#111318] rounded-xl px-4 py-2.5 text-[12px] font-bold tracking-wide hover:bg-[#38b6ff]/90 transition-all shadow-lg shadow-[#38b6ff]/10"
          >
            <Icon.plus width="14" height="14" /> AGGIUNGI INGREDIENTE
          </button>
        </div>
      </div>

      {/* KPI Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <KpiCard label="Ingredienti a catalogo" value={metrics.total} icon={<Icon.box width="16" height="16" />} subtext="Referenze attive a DB" accentColor="#38b6ff" />
        <KpiCard label="Valore magazzino" value={fmtCur(metrics.value)} icon={<Icon.euro width="16" height="16" />} subtext="Σ quantità × prezzo" accentColor="#22d3a0" />
        <KpiCard label="Sotto soglia" value={metrics.low} icon={<Icon.alert width="16" height="16" />} subtext={`Sotto ${fmtQty(threshold)} unità`} accentColor="#fbbf24" />
        <KpiCard label="Esauriti" value={metrics.empty} icon={<Icon.empty width="16" height="16" />} subtext="Da riordinare con urgenza" accentColor="#ff5e5e" />
      </div>

      {/* Tabella + toolbar */}
      <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
        <div className="flex items-center justify-between mb-5 flex-wrap gap-3">
          <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff]">Inventario ingredienti</h3>
          <div className="flex items-center gap-3">
            {/* Soglia */}
            <label className="flex items-center gap-2 text-[11px] font-semibold uppercase tracking-wider text-[#8b92a8]">
              Soglia
              <input
                type="number"
                min="0"
                value={threshold}
                onChange={(e) => setThreshold(Number(e.target.value) || 0)}
                className="w-16 bg-[#181c23] border border-white/5 rounded-lg px-2 py-1.5 text-[12px] text-[#f0f4ff] focus:outline-none focus:border-[#38b6ff]"
              />
            </label>
            {/* Ricerca */}
            <div className="relative">
              <span className="absolute left-3 top-1/2 -translate-y-1/2 text-[#4e5566]"><Icon.search width="14" height="14" /></span>
              <input
                type="text"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder="Cerca ingrediente..."
                className="bg-[#181c23] border border-white/5 rounded-xl pl-9 pr-4 py-2 text-[12px] text-[#f0f4ff] focus:outline-none focus:border-[#38b6ff] w-56"
              />
            </div>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-white/5">
                <th className="text-left text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3">Ingrediente</th>
                <th className="text-left text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3">Prezzo unitario</th>
                <th className="text-left text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3">Giacenza</th>
                <th className="text-left text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3">Valore</th>
                <th className="text-left text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3">Stato</th>
                <th className="text-right text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3">Azioni</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/[0.04]">
              {filtered.map((ing) => (
                <tr key={ing.id} className="hover:bg-[#181c23] transition-colors group">
                  <td className="py-3 px-3">
                    <div className="font-['Syne'] font-semibold text-[#f0f4ff] text-[13px]">{ing.name}</div>
                    {ing.description && <div className="text-[11px] text-[#8b92a8] truncate max-w-[280px]">{ing.description}</div>}
                  </td>
                  <td className="py-3 px-3 text-[#8b92a8] text-[13px]">{ing.price != null ? fmtCur(ing.price) : "—"}</td>
                  <td className="py-3 px-3 font-['Syne'] font-bold text-[#f0f4ff] text-[13px]">{fmtQty(ing.quantity)}</td>
                  <td className="py-3 px-3 font-['Syne'] font-bold text-[#22d3a0] text-[13px]">{fmtCur((ing.quantity ?? 0) * (ing.price ?? 0))}</td>
                  <td className="py-3 px-3"><StockBadge qty={ing.quantity} threshold={threshold} /></td>
                  <td className="py-3 px-3 text-right">
                    <div className="flex justify-end gap-2 opacity-60 group-hover:opacity-100 transition-opacity">
                      <button onClick={() => openEdit(ing)} title="Modifica" className="p-2 rounded-lg bg-[#181c23] border border-white/5 hover:border-[#38b6ff]/50 text-[#8b92a8] hover:text-[#38b6ff] transition-all">
                        <Icon.edit width="14" height="14" />
                      </button>
                      <button onClick={() => handleDelete(ing.id, ing.name)} title="Elimina" className="p-2 rounded-lg bg-[#181c23] border border-white/5 hover:border-[#ff5e5e]/50 text-[#8b92a8] hover:text-[#ff5e5e] transition-all">
                        <Icon.trash width="14" height="14" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
              {filtered.length === 0 && (
                <tr>
                  <td colSpan="6" className="text-center py-8 text-[13px] text-[#4e5566]">
                    {query ? "Nessun ingrediente corrisponde alla ricerca." : "Nessun ingrediente in magazzino."}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modale CRUD */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-[#000]/70 backdrop-blur-md z-50 flex items-center justify-center p-4">
          <div className="bg-[#111318] border border-white/10 rounded-[22px] max-w-md w-full p-6 shadow-2xl relative">
            <div className="flex items-center justify-between mb-6 border-b border-white/5 pb-4">
              <h2 className="font-['Syne'] text-[18px] font-bold text-[#f0f4ff]">
                {isEditing ? "Modifica Ingrediente" : "Nuovo Ingrediente"}
              </h2>
              <button onClick={() => setIsModalOpen(false)} className="text-[#8b92a8] hover:text-[#f0f4ff] transition-colors">
                <Icon.close width="18" height="18" />
              </button>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-[10px] font-semibold uppercase tracking-wider text-[#8b92a8] mb-1.5">Nome</label>
                <input
                  type="text" required minLength={3} maxLength={50}
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  className="w-full bg-[#181c23] border border-white/5 rounded-xl px-4 py-2.5 text-[13px] text-[#f0f4ff] focus:outline-none focus:border-[#38b6ff] transition-all"
                  placeholder="Es. Petto di pollo"
                />
              </div>

              <div>
                <label className="block text-[10px] font-semibold uppercase tracking-wider text-[#8b92a8] mb-1.5">Descrizione</label>
                <textarea
                  rows={2} maxLength={255}
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  className="w-full bg-[#181c23] border border-white/5 rounded-xl px-4 py-2.5 text-[13px] text-[#f0f4ff] focus:outline-none focus:border-[#38b6ff] transition-all resize-none"
                  placeholder="Note / fornitore / formato"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-[10px] font-semibold uppercase tracking-wider text-[#8b92a8] mb-1.5">Prezzo unitario (€)</label>
                  <input
                    type="number" step="0.01" min="0"
                    value={formData.price}
                    onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                    className="w-full bg-[#181c23] border border-white/5 rounded-xl px-4 py-2.5 text-[13px] text-[#f0f4ff] focus:outline-none focus:border-[#38b6ff] transition-all"
                    placeholder="Es. 4.50"
                  />
                </div>
                <div>
                  <label className="block text-[10px] font-semibold uppercase tracking-wider text-[#8b92a8] mb-1.5">Giacenza</label>
                  <input
                    type="number" step="0.01" min="0"
                    value={formData.quantity}
                    onChange={(e) => setFormData({ ...formData, quantity: e.target.value })}
                    className="w-full bg-[#181c23] border border-white/5 rounded-xl px-4 py-2.5 text-[13px] text-[#f0f4ff] focus:outline-none focus:border-[#38b6ff] transition-all"
                    placeholder="Es. 120"
                  />
                </div>
              </div>

              <div className="flex justify-end gap-3 pt-4 border-t border-white/5 mt-6">
                <button type="button" onClick={() => setIsModalOpen(false)} className="bg-transparent border border-white/5 hover:bg-white/5 px-4 py-2 rounded-xl text-[12px] font-medium text-[#8b92a8] transition-all">
                  Annulla
                </button>
                <button type="submit" disabled={saving} className="bg-[#38b6ff] text-[#111318] px-5 py-2 rounded-xl text-[12px] font-bold tracking-wide hover:bg-[#38b6ff]/90 transition-all disabled:opacity-50">
                  {saving ? "SALVATAGGIO..." : isEditing ? "SALVA MODIFICHE" : "CREA INGREDIENTE"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
