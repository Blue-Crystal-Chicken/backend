import { useState, useEffect, useMemo } from "react";

const fmtCur = (n) => new Intl.NumberFormat("it-IT", { style: "currency", currency: "EUR" }).format(n ?? 0);
const toLocalInput = (iso) => (iso ? iso.slice(0, 16) : ""); // ISO → "yyyy-MM-ddTHH:mm"

const Icon = {
  close: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...p}><line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" /></svg>),
  plus: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" {...p}><line x1="12" y1="5" x2="12" y2="19" /><line x1="5" y1="12" x2="19" y2="12" /></svg>),
  x: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...p}><line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" /></svg>),
};

const EMPTY = { name: "", description: "", price: "", discountPercentage: "", startDate: "", endDate: "", active: true };

export default function OffertaFormModal({
  isOpen, onClose, offer, products, menus,
  createOffer, updateOffer, addProductToOffer, removeProductFromOffer, addMenuToOffer,
}) {
  const isEditing = !!offer;
  const [form, setForm] = useState(EMPTY);
  const [imageFile, setImageFile] = useState(null);
  const [menuIds, setMenuIds] = useState([]); // solo in creazione
  const [lines, setLines] = useState([]); // [{ productId, productName, quantity }]
  const [newLine, setNewLine] = useState({ productId: "", quantity: 1 });
  const [saving, setSaving] = useState(false);

  // Inizializza il form all'apertura
  useEffect(() => {
    if (!isOpen) return;
    if (offer) {
      setForm({
        name: offer.name ?? "",
        description: offer.description ?? "",
        price: offer.price ?? "",
        discountPercentage: offer.discountPercentage ?? "",
        startDate: toLocalInput(offer.startDate),
        endDate: toLocalInput(offer.endDate),
        active: offer.active !== false,
      });
      setLines((offer.offerProducts ?? []).map((op) => ({ productId: op.productId, productName: op.productName, quantity: op.quantity ?? 1 })));
      setMenuIds([]);
    } else {
      setForm(EMPTY);
      setLines([]);
      setMenuIds([]);
    }
    setImageFile(null);
    setNewLine({ productId: "", quantity: 1 });
  }, [isOpen, offer]);

  const availableProducts = useMemo(() => {
    const present = new Set(lines.map((l) => l.productId));
    return products.filter((p) => !present.has(p.id));
  }, [products, lines]);

  if (!isOpen) return null;

  const priceOf = (id) => products.find((p) => p.id === id)?.price ?? 0;
  const listValue = lines.reduce((s, l) => s + (l.quantity || 1) * priceOf(l.productId), 0);

  // Aggiunta riga prodotto
  const handleAddLine = async () => {
    const pid = Number(newLine.productId);
    if (!pid) return;
    const prod = products.find((p) => p.id === pid);
    const qty = Number(newLine.quantity) || 1;
    if (isEditing) {
      try { await addProductToOffer(offer.id, pid, qty); } catch (e) { alert("Errore: " + (e.response?.data?.message || e.message)); return; }
    }
    setLines([...lines, { productId: pid, productName: prod?.name ?? `#${pid}`, quantity: qty }]);
    setNewLine({ productId: "", quantity: 1 });
  };

  // Rimozione riga prodotto
  const handleRemoveLine = async (productId) => {
    if (isEditing) {
      try { await removeProductFromOffer(offer.id, productId); } catch (e) { alert("Errore: " + (e.response?.data?.message || e.message)); return; }
    }
    setLines(lines.filter((l) => l.productId !== productId));
  };

  const toggleMenu = (id) => {
    setMenuIds((prev) => (prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]));
  };

  const handleAddMenuEdit = async (menuId) => {
    if (!menuId) return;
    try { await addMenuToOffer(offer.id, Number(menuId)); } catch (e) { alert("Errore: " + (e.response?.data?.message || e.message)); }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      const payload = {
        name: form.name,
        description: form.description,
        price: form.price,
        discountPercentage: form.discountPercentage,
        startDate: form.startDate || undefined,
        endDate: form.endDate || undefined,
        active: form.active,
        menuIds: isEditing ? undefined : menuIds,
      };
      if (isEditing) {
        await updateOffer(offer.id, payload, imageFile);
      } else {
        await createOffer(payload, imageFile, lines);
      }
      onClose();
    } catch (err) {
      alert("Operazione fallita: " + (err.response?.data?.message || err.message));
    } finally {
      setSaving(false);
    }
  };

  const linkedMenus = offer?.menus ?? [];
  const addableMenus = menus.filter((m) => !linkedMenus.some((lm) => lm.id === m.id));

  return (
    <div className="fixed inset-0 bg-[#000]/70 backdrop-blur-md z-50 flex items-center justify-center p-4">
      <div className="bg-[#111318] border border-white/10 rounded-[22px] max-w-2xl w-full p-6 shadow-2xl relative max-h-[90vh] overflow-y-auto custom-scrollbar">
        <div className="flex items-center justify-between mb-6 border-b border-white/5 pb-4">
          <h2 className="font-['Syne'] text-[18px] font-bold text-[#f0f4ff]">{isEditing ? "Modifica Offerta" : "Nuova Offerta"}</h2>
          <button onClick={onClose} className="text-[#8b92a8] hover:text-[#f0f4ff] transition-colors"><Icon.close width="18" height="18" /></button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Nome */}
          <div>
            <label className="block text-[10px] font-semibold uppercase tracking-wider text-[#8b92a8] mb-1.5">Nome offerta</label>
            <input type="text" required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })}
              className="w-full bg-[#181c23] border border-white/5 rounded-xl px-4 py-2.5 text-[13px] text-[#f0f4ff] focus:outline-none focus:border-[#38b6ff]" placeholder="Es. Family Crystal Pack" />
          </div>

          {/* Descrizione */}
          <div>
            <label className="block text-[10px] font-semibold uppercase tracking-wider text-[#8b92a8] mb-1.5">Descrizione</label>
            <textarea rows={2} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })}
              className="w-full bg-[#181c23] border border-white/5 rounded-xl px-4 py-2.5 text-[13px] text-[#f0f4ff] focus:outline-none focus:border-[#38b6ff] resize-none" placeholder="Cosa include l'offerta" />
          </div>

          {/* Prezzo + sconto */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-[10px] font-semibold uppercase tracking-wider text-[#8b92a8] mb-1.5">Prezzo offerta (€)</label>
              <input type="number" step="0.01" min="0" required value={form.price} onChange={(e) => setForm({ ...form, price: e.target.value })}
                className="w-full bg-[#181c23] border border-white/5 rounded-xl px-4 py-2.5 text-[13px] text-[#f0f4ff] focus:outline-none focus:border-[#38b6ff]" placeholder="Es. 19.90" />
            </div>
            <div>
              <label className="block text-[10px] font-semibold uppercase tracking-wider text-[#8b92a8] mb-1.5">Sconto (%)</label>
              <input type="number" step="1" min="0" max="100" value={form.discountPercentage} onChange={(e) => setForm({ ...form, discountPercentage: e.target.value })}
                className="w-full bg-[#181c23] border border-white/5 rounded-xl px-4 py-2.5 text-[13px] text-[#f0f4ff] focus:outline-none focus:border-[#38b6ff]" placeholder="Es. 20" />
            </div>
          </div>

          {/* Date validità + attiva */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-[10px] font-semibold uppercase tracking-wider text-[#8b92a8] mb-1.5">Inizio validità</label>
              <input type="datetime-local" value={form.startDate} onChange={(e) => setForm({ ...form, startDate: e.target.value })}
                className="w-full bg-[#181c23] border border-white/5 rounded-xl px-3 py-2.5 text-[13px] text-[#f0f4ff] focus:outline-none focus:border-[#38b6ff]" />
            </div>
            <div>
              <label className="block text-[10px] font-semibold uppercase tracking-wider text-[#8b92a8] mb-1.5">Fine validità</label>
              <input type="datetime-local" value={form.endDate} onChange={(e) => setForm({ ...form, endDate: e.target.value })}
                className="w-full bg-[#181c23] border border-white/5 rounded-xl px-3 py-2.5 text-[13px] text-[#f0f4ff] focus:outline-none focus:border-[#38b6ff]" />
            </div>
          </div>

          {/* Attiva + immagine */}
          <div className="flex items-center justify-between gap-4">
            <label className="flex items-center gap-2.5 cursor-pointer">
              <button type="button" onClick={() => setForm({ ...form, active: !form.active })}
                className={`w-10 h-[22px] rounded-full p-0.5 transition-colors ${form.active ? "bg-[#22d3a0]" : "bg-[#3a4150]"}`}>
                <span className={`block w-[18px] h-[18px] rounded-full bg-white transition-transform ${form.active ? "translate-x-[18px]" : ""}`} />
              </button>
              <span className="text-[12px] font-medium text-[#f0f4ff]">{form.active ? "Offerta attiva" : "Offerta disattivata"}</span>
            </label>
            <label className="text-[12px] text-[#38b6ff] cursor-pointer hover:underline">
              {imageFile ? imageFile.name : "Carica immagine"}
              <input type="file" accept="image/*" onChange={(e) => setImageFile(e.target.files[0] || null)} className="hidden" />
            </label>
          </div>

          {/* ── Prodotti del bundle ── */}
          <div className="border-t border-white/5 pt-4">
            <div className="flex items-center justify-between mb-2.5">
              <span className="text-[11px] font-semibold uppercase tracking-wider text-[#8b92a8]">Prodotti nel bundle</span>
              {listValue > 0 && <span className="text-[11px] text-[#22d3a0]">listino {fmtCur(listValue)}</span>}
            </div>

            <div className="space-y-2 mb-3">
              {lines.map((l) => (
                <div key={l.productId} className="flex items-center gap-3 bg-[#181c23] border border-white/5 rounded-xl px-3 py-2">
                  <span className="text-[13px] text-[#f0f4ff] flex-1 truncate">{l.productName}</span>
                  <span className="text-[12px] text-[#8b92a8]">×{l.quantity}</span>
                  <span className="text-[12px] text-[#8b92a8] w-20 text-right">{fmtCur((l.quantity || 1) * priceOf(l.productId))}</span>
                  <button type="button" onClick={() => handleRemoveLine(l.productId)} className="p-1.5 rounded-md text-[#8b92a8] hover:text-[#ff5e5e] hover:bg-[#ff5e5e]/10 transition-all">
                    <Icon.x width="13" height="13" />
                  </button>
                </div>
              ))}
              {lines.length === 0 && <p className="text-[12px] text-[#4e5566]">Nessun prodotto nel bundle.</p>}
            </div>

            <div className="flex items-center gap-2">
              <select value={newLine.productId} onChange={(e) => setNewLine({ ...newLine, productId: e.target.value })}
                className="flex-1 bg-[#181c23] border border-white/5 rounded-xl px-3 py-2 text-[12px] text-[#f0f4ff] focus:outline-none focus:border-[#38b6ff]">
                <option value="" className="bg-[#111318]">Seleziona prodotto...</option>
                {availableProducts.map((p) => <option key={p.id} value={p.id} className="bg-[#111318]">{p.name}{p.price != null ? ` — ${fmtCur(p.price)}` : ""}</option>)}
              </select>
              <input type="number" min="1" value={newLine.quantity} onChange={(e) => setNewLine({ ...newLine, quantity: e.target.value })}
                className="w-16 bg-[#181c23] border border-white/5 rounded-xl px-2.5 py-2 text-[12px] text-[#f0f4ff] focus:outline-none focus:border-[#38b6ff]" />
              <button type="button" onClick={handleAddLine} disabled={!newLine.productId}
                className="p-2 rounded-xl bg-[#38b6ff]/15 border border-[#38b6ff]/30 text-[#38b6ff] hover:bg-[#38b6ff]/25 transition-all disabled:opacity-40">
                <Icon.plus width="16" height="16" />
              </button>
            </div>
          </div>

          {/* ── Menu collegati ── */}
          <div className="border-t border-white/5 pt-4">
            <span className="text-[11px] font-semibold uppercase tracking-wider text-[#8b92a8] block mb-2.5">Menu collegati</span>
            {isEditing ? (
              <div className="space-y-2.5">
                <div className="flex flex-wrap gap-2">
                  {linkedMenus.length === 0 && <span className="text-[12px] text-[#4e5566]">Nessun menu collegato.</span>}
                  {linkedMenus.map((m) => (
                    <span key={m.id} className="px-2.5 py-1 rounded-lg text-[12px] bg-[#181c23] border border-white/5 text-[#f0f4ff]">{m.name}</span>
                  ))}
                </div>
                {addableMenus.length > 0 && (
                  <select defaultValue="" onChange={(e) => { handleAddMenuEdit(e.target.value); e.target.value = ""; }}
                    className="w-full bg-[#181c23] border border-white/5 rounded-xl px-3 py-2 text-[12px] text-[#f0f4ff] focus:outline-none focus:border-[#38b6ff]">
                    <option value="" className="bg-[#111318]">+ Collega un menu...</option>
                    {addableMenus.map((m) => <option key={m.id} value={m.id} className="bg-[#111318]">{m.name}</option>)}
                  </select>
                )}
                <p className="text-[10px] text-[#4e5566]">Lo scollegamento di un menu non è supportato dal backend.</p>
              </div>
            ) : (
              <div className="flex flex-wrap gap-2">
                {menus.map((m) => {
                  const active = menuIds.includes(m.id);
                  return (
                    <button key={m.id} type="button" onClick={() => toggleMenu(m.id)}
                      className={`px-3 py-1.5 rounded-lg text-[12px] font-semibold border transition-all ${active ? "bg-[#38b6ff]/15 border-[#38b6ff]/40 text-[#38b6ff]" : "border-white/5 text-[#8b92a8] hover:text-[#f0f4ff] hover:border-white/20"}`}>
                      {m.name}
                    </button>
                  );
                })}
                {menus.length === 0 && <span className="text-[12px] text-[#4e5566]">Nessun menu disponibile.</span>}
              </div>
            )}
          </div>

          {/* Azioni */}
          <div className="flex justify-end gap-3 pt-4 border-t border-white/5 mt-2">
            <button type="button" onClick={onClose} className="bg-transparent border border-white/5 hover:bg-white/5 px-4 py-2 rounded-xl text-[12px] font-medium text-[#8b92a8] transition-all">Annulla</button>
            <button type="submit" disabled={saving} className="bg-[#38b6ff] text-[#111318] px-5 py-2 rounded-xl text-[12px] font-bold tracking-wide hover:bg-[#38b6ff]/90 transition-all disabled:opacity-50">
              {saving ? "SALVATAGGIO..." : isEditing ? "SALVA MODIFICHE" : "CREA OFFERTA"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
