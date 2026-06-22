import { useState, useMemo } from "react";
import { useMarketing, offerStatus } from "../hooks/useMarketing";
import KpiMarketing from "../components/marketing/KpiMarketing";
import OfferteGrid from "../components/marketing/OfferteGrid";
import OffertaFormModal from "../components/marketing/OffertaFormModal";

const FILTERS = [
  { key: "ALL", label: "Tutte" },
  { key: "ATTIVA", label: "Attive" },
  { key: "PROGRAMMATA", label: "Programmate" },
  { key: "SCADUTA", label: "Scadute" },
  { key: "DISATTIVATA", label: "Disattivate" },
];

export default function Marketing() {
  const {
    offers, products, menus, loading, refreshing, error,
    kpi, listValueOf, refresh,
    createOffer, updateOffer, deleteOffer,
    addProductToOffer, removeProductFromOffer, addMenuToOffer,
  } = useMarketing();

  const [filter, setFilter] = useState("ALL");
  const [query, setQuery] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    return offers.filter((o) => {
      if (filter !== "ALL" && offerStatus(o) !== filter) return false;
      if (q && !o.name?.toLowerCase().includes(q) && !o.description?.toLowerCase().includes(q)) return false;
      return true;
    });
  }, [offers, filter, query]);

  const openCreate = () => { setEditing(null); setModalOpen(true); };
  const openEdit = (offer) => { setEditing(offer); setModalOpen(true); };

  const handleDelete = async (offer) => {
    if (!window.confirm(`Eliminare definitivamente l'offerta "${offer.name}"?`)) return;
    try { await deleteOffer(offer.id); } catch (e) { alert("Errore: " + (e.response?.data?.message || e.message)); }
  };

  if (loading) {
    return (
      <div className="flex h-screen items-center justify-center bg-[#0b0c10] text-[14px] font-semibold text-[#38b6ff] tracking-widest font-['Syne'] uppercase animate-pulse">
        Sincronizzazione Campagne...
      </div>
    );
  }

  return (
    <div className="p-[28px_32px] max-w-[1440px] mx-auto text-[#f0f4ff] bg-[#0b0c10] min-h-screen">

      {/* Header */}
      <div className="flex items-end justify-between mb-8 flex-wrap gap-4">
        <div>
          <h1 className="font-['Syne'] text-[26px] font-extrabold tracking-tight mb-1">Marketing — Offerte</h1>
          <p className="text-[13px] text-[#8b92a8] uppercase tracking-wide">Gestione campagne e promozioni della catena</p>
        </div>
        <div className="flex items-center gap-3">
          <button onClick={refresh} className="flex items-center gap-2 bg-[#111318] border border-white/5 rounded-xl px-3.5 py-2.5 text-[12px] font-medium text-[#8b92a8] hover:border-[#38b6ff] hover:text-[#38b6ff] transition-all">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" className={refreshing ? "animate-spin" : ""}><polyline points="23 4 23 10 17 10" /><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" /></svg>
            Aggiorna
          </button>
          <button onClick={openCreate} className="flex items-center gap-2 bg-[#38b6ff] text-[#111318] rounded-xl px-4 py-2.5 text-[12px] font-bold tracking-wide hover:bg-[#38b6ff]/90 transition-all shadow-lg shadow-[#38b6ff]/10">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5"><line x1="12" y1="5" x2="12" y2="19" /><line x1="5" y1="12" x2="19" y2="12" /></svg>
            NUOVA OFFERTA
          </button>
        </div>
      </div>

      {error && (
        <div className="mb-6 text-[13px] text-[#ff5e5e] bg-[#ff5e5e]/10 border border-[#ff5e5e]/20 rounded-xl px-4 py-3">{error}</div>
      )}

      {/* KPI */}
      <KpiMarketing kpi={kpi} />

      {/* Toolbar: filtri stato + ricerca */}
      <div className="flex items-center justify-between mb-5 flex-wrap gap-3">
        <div className="flex items-center gap-1 bg-[#111318] border border-white/5 rounded-xl p-1">
          {FILTERS.map((f) => (
            <button key={f.key} onClick={() => setFilter(f.key)}
              className={`px-3 py-1.5 rounded-lg text-[12px] font-semibold transition-all ${filter === f.key ? "bg-[#38b6ff] text-[#111318]" : "text-[#8b92a8] hover:text-[#f0f4ff]"}`}>
              {f.label}
            </button>
          ))}
        </div>
        <div className="relative">
          <span className="absolute left-3 top-1/2 -translate-y-1/2 text-[#4e5566]">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="11" cy="11" r="8" /><line x1="21" y1="21" x2="16.65" y2="16.65" /></svg>
          </span>
          <input type="text" value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Cerca offerta..."
            className="bg-[#181c23] border border-white/5 rounded-xl pl-9 pr-4 py-2 text-[12px] text-[#f0f4ff] focus:outline-none focus:border-[#38b6ff] w-56" />
        </div>
      </div>

      {/* Griglia offerte */}
      <OfferteGrid offers={filtered} listValueOf={listValueOf} onEdit={openEdit} onDelete={handleDelete} />

      {/* Modal crea/modifica */}
      <OffertaFormModal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        offer={editing}
        products={products}
        menus={menus}
        createOffer={createOffer}
        updateOffer={updateOffer}
        addProductToOffer={addProductToOffer}
        removeProductFromOffer={removeProductFromOffer}
        addMenuToOffer={addMenuToOffer}
      />
    </div>
  );
}
