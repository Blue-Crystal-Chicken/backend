import { useState, useMemo } from "react";
import { useNotifiche } from "../hooks/useNotifiche";
import KpiNotifiche from "../components/notifiche/KpiNotifiche";
import NotificaRow from "../components/notifiche/NotificaRow";
import InviaNotificaModal from "../components/notifiche/InviaNotificaModal";

const LEVEL_FILTERS = [
  { key: "ALL", label: "Tutte" },
  { key: "WARNING", label: "Attenzione" },
  { key: "ERROR", label: "Errori" },
  { key: "SUCCESS", label: "Successi" },
  { key: "INFO", label: "Info" },
];

export default function Notifiche() {
  const {
    items, loading, refreshing, error, isLive,
    unreadCount, refresh, toggleLive,
    markRead, markAllRead, remove, clearAll,
  } = useNotifiche();

  const [filter, setFilter] = useState("ALL");
  const [onlyUnread, setOnlyUnread] = useState(false);
  const [sendOpen, setSendOpen] = useState(false);
  const [sentToast, setSentToast] = useState(false);

  // Riscontro locale: il pannello web è solo trigger e non riceve push, quindi
  // a invio riuscito mostriamo una conferma temporanea così non sembra che non
  // sia successo nulla. Nessuna interazione col sistema push/Firebase.
  const handleSent = () => {
    refresh();
    setSentToast(true);
    setTimeout(() => setSentToast(false), 4000);
  };

  const filtered = useMemo(() => items.filter((n) => {
    if (filter !== "ALL" && n.level !== filter) return false;
    if (onlyUnread && n.readFlag) return false;
    return true;
  }), [items, filter, onlyUnread]);

  if (loading) {
    return (
      <div className="flex h-screen items-center justify-center bg-[#0b0c10] text-[14px] font-semibold text-[#38b6ff] tracking-widest font-['Syne'] uppercase animate-pulse">
        Caricamento centro notifiche...
      </div>
    );
  }

  return (
    <div className="p-[28px_32px] max-w-[1100px] mx-auto text-[#f0f4ff] bg-[#0b0c10] min-h-screen">

      {/* Header */}
      <div className="flex items-end justify-between mb-8 flex-wrap gap-4">
        <div>
          <h1 className="font-['Syne'] text-[26px] font-extrabold tracking-tight mb-1">Centro Notifiche</h1>
          <p className="text-[13px] text-[#8b92a8] uppercase tracking-wide">
            Log eventi via RabbitMQ · {unreadCount} non lette
          </p>
        </div>
        <div className="flex items-center gap-3">
          <button onClick={() => setSendOpen(true)}
            className="flex items-center gap-2 bg-[#38b6ff] text-[#111318] rounded-xl px-4 py-2.5 text-[12px] font-bold tracking-wide hover:bg-[#38b6ff]/90 transition-all shadow-lg shadow-[#38b6ff]/10">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><line x1="22" y1="2" x2="11" y2="13" /><polygon points="22 2 15 22 11 13 2 9 22 2" /></svg>
            INVIA NOTIFICA
          </button>
          <button onClick={toggleLive}
            className={`flex items-center gap-1.5 rounded-full px-3 py-1.5 text-[12px] font-semibold border transition-all ${isLive ? "bg-[#22d3a0]/10 border-[#22d3a0]/25 text-[#22d3a0]" : "bg-[#111318] border-white/5 text-[#8b92a8] hover:border-[#22d3a0]/30 hover:text-[#22d3a0]"}`}>
            <span className={`w-1.5 h-1.5 rounded-full ${isLive ? "bg-[#22d3a0] animate-pulse" : "bg-[#8b92a8]"}`} />
            LIVE
          </button>
          <button onClick={refresh} className="flex items-center gap-2 bg-[#111318] border border-white/5 rounded-xl px-3.5 py-2.5 text-[12px] font-medium text-[#8b92a8] hover:border-[#38b6ff] hover:text-[#38b6ff] transition-all">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" className={refreshing ? "animate-spin" : ""}><polyline points="23 4 23 10 17 10" /><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" /></svg>
            Aggiorna
          </button>
        </div>
      </div>

      {error && (
        <div className="mb-6 text-[13px] text-[#ff5e5e] bg-[#ff5e5e]/10 border border-[#ff5e5e]/20 rounded-xl px-4 py-3">{error}</div>
      )}

      {/* Stats */}
      <KpiNotifiche items={items} unreadCount={unreadCount} />

      {/* Toolbar: filtri + azioni di massa */}
      <div className="flex items-center justify-between mb-5 flex-wrap gap-3">
        <div className="flex items-center gap-3 flex-wrap">
          <div className="flex items-center gap-1 bg-[#111318] border border-white/5 rounded-xl p-1">
            {LEVEL_FILTERS.map((f) => (
              <button key={f.key} onClick={() => setFilter(f.key)}
                className={`px-3 py-1.5 rounded-lg text-[12px] font-semibold transition-all ${filter === f.key ? "bg-[#38b6ff] text-[#111318]" : "text-[#8b92a8] hover:text-[#f0f4ff]"}`}>
                {f.label}
              </button>
            ))}
          </div>
          <button onClick={() => setOnlyUnread((v) => !v)}
            className={`px-3 py-1.5 rounded-lg text-[12px] font-semibold border transition-all ${onlyUnread ? "bg-[#a78bfa]/15 border-[#a78bfa]/40 text-[#a78bfa]" : "border-white/5 text-[#8b92a8] hover:text-[#f0f4ff]"}`}>
            Solo non lette
          </button>
        </div>
        <div className="flex items-center gap-2">
          <button onClick={markAllRead} disabled={unreadCount === 0}
            className="text-[12px] font-medium text-[#8b92a8] hover:text-[#22d3a0] disabled:opacity-40 transition-colors px-2 py-1">
            Segna tutte lette
          </button>
          <button onClick={() => { if (window.confirm("Svuotare tutte le notifiche?")) clearAll(); }} disabled={items.length === 0}
            className="text-[12px] font-medium text-[#8b92a8] hover:text-[#ff5e5e] disabled:opacity-40 transition-colors px-2 py-1">
            Svuota tutto
          </button>
        </div>
      </div>

      {/* Lista */}
      <div className="flex flex-col gap-2.5">
        {filtered.map((n) => (
          <NotificaRow key={n.id} notification={n} onRead={markRead} onDelete={remove} />
        ))}
        {filtered.length === 0 && (
          <div className="bg-[#111318] border border-dashed border-white/10 rounded-[18px] p-12 text-center text-[13px] text-[#4e5566]">
            {items.length === 0 ? "Nessuna notifica. Gli eventi compariranno qui appena generati." : "Nessuna notifica per questo filtro."}
          </div>
        )}
      </div>

      {sendOpen && (
        <InviaNotificaModal onClose={() => setSendOpen(false)} onSent={handleSent} />
      )}

      {sentToast && (
        <div className="fixed bottom-6 right-6 z-[60] flex items-center gap-2.5 bg-[#111318] border border-[#22d3a0]/30 rounded-xl px-4 py-3 shadow-2xl shadow-[#22d3a0]/10 animate-fade-in">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#22d3a0" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12" /></svg>
          <span className="text-[13px] font-semibold text-[#f0f4ff]">Notifica inviata correttamente</span>
        </div>
      )}
    </div>
  );
}
