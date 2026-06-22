import { useState } from "react";
import pushNotificationService from "../../service/PushNotificationService";

const Close = (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...p}><line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" /></svg>);
const Plus = (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" {...p}><line x1="12" y1="5" x2="12" y2="19" /><line x1="5" y1="12" x2="19" y2="12" /></svg>);
const Trash = (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...p}><polyline points="3 6 5 6 21 6" /><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" /></svg>);

// Deve combaciare con l'enum Type del servizio notifiche (8085): NEW_PRODUCT/NEW_MENU/NEW_OFFER.
const TYPE_OPTIONS = [
  { value: "NEW_PRODUCT", label: "Nuovo prodotto" },
  { value: "NEW_MENU", label: "Nuovo menu" },
  { value: "NEW_OFFER", label: "Nuova offerta" },
];

export default function InviaNotificaModal({ onClose, onSent }) {
  const [form, setForm] = useState({ title: "", body: "", type: "NEW_PRODUCT" });
  const [payloadRows, setPayloadRows] = useState([{ key: "", value: "" }]);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState(null);

  const updateRow = (i, patch) =>
    setPayloadRows((rows) => rows.map((r, idx) => (idx === i ? { ...r, ...patch } : r)));
  const addRow = () => setPayloadRows((rows) => [...rows, { key: "", value: "" }]);
  const removeRow = (i) => setPayloadRows((rows) => rows.filter((_, idx) => idx !== i));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSending(true);
    try {
      const payload = payloadRows
        .filter((r) => r.key.trim() !== "")
        .reduce((acc, r) => ({ ...acc, [r.key.trim()]: r.value }), {});

      const { sent } = await pushNotificationService.createAndSend({
        title: form.title,
        body: form.body,
        type: form.type,
        payload,
      });
      onSent?.(sent);
      onClose();
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || "Invio fallito.");
    } finally {
      setSending(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-[#000]/70 backdrop-blur-md z-50 flex items-center justify-center p-4">
      <div className="bg-[#111318] border border-white/10 rounded-[22px] max-w-lg w-full p-6 shadow-2xl relative animate-fade-in max-h-[90vh] overflow-y-auto">

        <div className="flex items-center justify-between mb-6 border-b border-white/5 pb-4">
          <h2 className="font-['Syne'] text-[18px] font-bold text-[#f0f4ff]">Invia notifica push</h2>
          <button onClick={onClose} className="text-[#8b92a8] hover:text-[#f0f4ff] transition-colors">
            <Close width="18" height="18" />
          </button>
        </div>

        {error && (
          <div className="mb-4 text-[13px] text-[#ff5e5e] bg-[#ff5e5e]/10 border border-[#ff5e5e]/20 rounded-xl px-4 py-3">{error}</div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Titolo */}
          <div>
            <label className="block text-[10px] font-semibold uppercase tracking-wider text-[#8b92a8] mb-1.5">Titolo</label>
            <input
              type="text"
              required
              value={form.title}
              onChange={(e) => setForm({ ...form, title: e.target.value })}
              className="w-full bg-[#181c23] border border-white/5 rounded-xl px-4 py-2.5 text-[13px] text-[#f0f4ff] focus:outline-none focus:border-[#38b6ff] transition-all"
              placeholder="Es. Offerta del giorno"
            />
          </div>

          {/* Corpo */}
          <div>
            <label className="block text-[10px] font-semibold uppercase tracking-wider text-[#8b92a8] mb-1.5">Messaggio</label>
            <textarea
              required
              rows={3}
              value={form.body}
              onChange={(e) => setForm({ ...form, body: e.target.value })}
              className="w-full bg-[#181c23] border border-white/5 rounded-xl px-4 py-2.5 text-[13px] text-[#f0f4ff] focus:outline-none focus:border-[#38b6ff] transition-all resize-none"
              placeholder="Testo della notifica..."
            />
          </div>

          {/* Tipo */}
          <div>
            <label className="block text-[10px] font-semibold uppercase tracking-wider text-[#8b92a8] mb-1.5">Tipo</label>
            <select
              value={form.type}
              onChange={(e) => setForm({ ...form, type: e.target.value })}
              className="w-full bg-[#181c23] border border-white/5 rounded-xl px-3 py-2.5 text-[13px] text-[#f0f4ff] focus:outline-none focus:border-[#38b6ff] transition-all"
            >
              {TYPE_OPTIONS.map((t) => (
                <option key={t.value} value={t.value} className="bg-[#111318]">{t.label}</option>
              ))}
            </select>
          </div>

          {/* Payload (key/value opzionale) */}
          <div>
            <div className="flex items-center justify-between mb-1.5">
              <label className="block text-[10px] font-semibold uppercase tracking-wider text-[#8b92a8]">Payload (opzionale)</label>
              <button type="button" onClick={addRow} className="flex items-center gap-1 text-[11px] font-semibold text-[#38b6ff] hover:underline">
                <Plus width="12" height="12" /> Aggiungi
              </button>
            </div>
            <div className="space-y-2">
              {payloadRows.map((row, i) => (
                <div key={i} className="flex items-center gap-2">
                  <input
                    type="text"
                    value={row.key}
                    onChange={(e) => updateRow(i, { key: e.target.value })}
                    className="w-[40%] bg-[#181c23] border border-white/5 rounded-lg px-3 py-2 text-[12px] text-[#f0f4ff] focus:outline-none focus:border-[#38b6ff]"
                    placeholder="key"
                  />
                  <input
                    type="text"
                    value={row.value}
                    onChange={(e) => updateRow(i, { value: e.target.value })}
                    className="flex-1 bg-[#181c23] border border-white/5 rounded-lg px-3 py-2 text-[12px] text-[#f0f4ff] focus:outline-none focus:border-[#38b6ff]"
                    placeholder="value"
                  />
                  <button
                    type="button"
                    onClick={() => removeRow(i)}
                    className="p-2 rounded-lg bg-[#181c23] border border-white/5 text-[#8b92a8] hover:text-[#ff5e5e] hover:border-[#ff5e5e]/40 transition-all"
                  >
                    <Trash width="13" height="13" />
                  </button>
                </div>
              ))}
            </div>
          </div>

          {/* Azioni */}
          <div className="flex justify-end gap-3 pt-4 border-t border-white/5 mt-6">
            <button type="button" onClick={onClose}
              className="bg-transparent border border-white/5 hover:bg-white/5 px-4 py-2 rounded-xl text-[12px] font-medium text-[#8b92a8] transition-all">
              Annulla
            </button>
            <button type="submit" disabled={sending}
              className="bg-[#38b6ff] text-[#111318] px-5 py-2 rounded-xl text-[12px] font-bold tracking-wide hover:bg-[#38b6ff]/90 transition-all disabled:opacity-50">
              {sending ? "INVIO..." : "CREA E INVIA"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
