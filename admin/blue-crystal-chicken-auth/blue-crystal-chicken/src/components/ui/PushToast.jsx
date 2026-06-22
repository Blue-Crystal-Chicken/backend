import { useEffect, useState } from "react";

// Riscontro globale per gli invii push automatici (creazione prodotto/menu/offerta).
// Il pannello web è solo trigger e non riceve push: senza questo segnale sembra
// che non sia successo nulla. NON interagisce col sistema push/Firebase: è solo UI.

const EVENT = "bcc:push-sent";

/** Mostra un toast di successo da qualunque punto dell'app. */
export function notifyPushSent(message = "Notifica push inviata") {
  window.dispatchEvent(new CustomEvent(EVENT, { detail: { message, variant: "success" } }));
}

/** Mostra un toast di errore da qualunque punto dell'app. */
export function notifyPushError(message = "Invio notifica fallito") {
  window.dispatchEvent(new CustomEvent(EVENT, { detail: { message, variant: "error" } }));
}

// Colori per variante (palette del design system).
const VARIANTS = {
  success: { color: "#22d3a0", icon: <polyline points="20 6 9 17 4 12" /> },
  error: { color: "#ff5e5e", icon: <><line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" /></> },
};

/** Host del toast: montalo una sola volta (nel layout). Stacka e auto-dismissa a 5s. */
export default function PushToast() {
  const [toasts, setToasts] = useState([]);

  useEffect(() => {
    const onPush = (e) => {
      const id = Date.now() + Math.random();
      const message = e.detail?.message || "Notifica push inviata";
      const variant = e.detail?.variant === "error" ? "error" : "success";
      setToasts((prev) => [...prev, { id, message, variant }]);
      setTimeout(() => {
        setToasts((prev) => prev.filter((t) => t.id !== id));
      }, 5000);
    };
    window.addEventListener(EVENT, onPush);
    return () => window.removeEventListener(EVENT, onPush);
  }, []);

  if (toasts.length === 0) return null;

  return (
    <div className="fixed bottom-6 right-6 z-[60] flex flex-col gap-2.5">
      {toasts.map((t) => {
        const v = VARIANTS[t.variant];
        return (
          <div
            key={t.id}
            className="flex items-center gap-2.5 bg-[#111318] rounded-xl px-4 py-3 shadow-2xl animate-fade-in border"
            style={{ borderColor: `${v.color}4d`, boxShadow: `0 0 25px -5px ${v.color}1a` }}
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke={v.color} strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">{v.icon}</svg>
            <span className="text-[13px] font-semibold text-[#f0f4ff]">{t.message}</span>
          </div>
        );
      })}
    </div>
  );
}
