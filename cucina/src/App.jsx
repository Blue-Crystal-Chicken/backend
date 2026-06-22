import { useState, useEffect } from "react";
import { useKitchen } from "./hooks/useKitchen";
import { fetchStation } from "./api/kitchenApi";
import OrderCard from "./components/OrderCard";
import { CATEGORY_COLORS, CATEGORY_LABELS } from "./config";

// Badge che indica a quale SEDE è collegata questa cucina (dal token-stazione).
function useStation() {
  const [station, setStation] = useState(undefined); // undefined=loading, null=non configurata
  useEffect(() => {
    fetchStation().then((s) => setStation(s ?? null));
  }, []);
  return station;
}

function useClock() {
  const [now, setNow] = useState(new Date());
  useEffect(() => {
    const id = setInterval(() => setNow(new Date()), 1000);
    return () => clearInterval(id);
  }, []);
  return now;
}

// Legenda colori categorie (sempre visibile: l'operatore impara i colori)
function Legend() {
  const keys = Object.keys(CATEGORY_LABELS).filter((k) => k !== "DEFAULT");
  return (
    <div className="flex flex-wrap items-center gap-x-3 gap-y-1.5">
      {keys.map((k) => (
        <span key={k} className="flex items-center gap-1.5">
          <span
            className="w-2.5 h-2.5 rounded-sm"
            style={{ backgroundColor: CATEGORY_COLORS[k] }}
          />
          <span className="text-[11px] font-semibold text-[#8b92a8]">
            {CATEGORY_LABELS[k]}
          </span>
        </span>
      ))}
    </div>
  );
}

export default function App() {
  const station = useStation();
  const {
    orders,
    categoryMap,
    loading,
    refreshing,
    error,
    actionError,
    isLive,
    lastUpdate,
    busy,
    refresh,
    toggleLive,
    act,
    clearActionError,
  } = useKitchen(station ? station.id : null);
  const now = useClock();

  if (loading) {
    return (
      <div className="flex h-screen items-center justify-center bg-[#0b0c10] text-[14px] font-semibold text-[#38b6ff] tracking-widest font-['Syne'] uppercase animate-pulse">
        Caricamento cucina…
      </div>
    );
  }

  return (
    <div className="p-[20px_28px] mx-auto text-[#f0f4ff] bg-[#0b0c10] min-h-screen flex flex-col">
      {/* Header */}
      <header className="flex items-start justify-between mb-4 flex-shrink-0">
        <div>
          <div className="flex items-center gap-3 mb-1">
            <h1 className="font-['Syne'] text-[26px] font-extrabold tracking-tight">
              Cucina{station && station.city ? ` · ${station.city}` : ""}
            </h1>
            {/* Sede a cui è collegata questa cucina (dal token-stazione) */}
            {station === undefined ? null : station ? (
              <span className="font-['Syne'] text-[14px] font-bold px-3 py-0.5 rounded-full bg-[#a78bfa]/10 text-[#a78bfa] border border-[#a78bfa]/20">
                {station.name}{station.city ? ` · ${station.city}` : ""}
              </span>
            ) : (
              <span className="font-['Syne'] text-[13px] font-bold px-3 py-0.5 rounded-full bg-[#ff5e5e]/10 text-[#ff5e5e] border border-[#ff5e5e]/20">
                Sede non configurata — imposta VITE_STATION_TOKEN
              </span>
            )}
            <span
              className="font-['Syne'] text-[14px] font-bold px-3 py-0.5 rounded-full bg-[#38b6ff]/10 text-[#38b6ff] border border-[#38b6ff]/20"
            >
              {orders.length} da preparare
            </span>
          </div>
          <p className="text-[12px] text-[#8b92a8] uppercase tracking-wide">
            Blue Crystal Chicken — Stazione di preparazione
          </p>
        </div>

        <div className="flex items-center gap-3">
          <div className="font-['Syne'] text-[24px] font-bold text-[#f0f4ff] tabular-nums">
            {now.toLocaleTimeString("it-IT", {
              hour: "2-digit",
              minute: "2-digit",
              second: "2-digit",
            })}
          </div>
          <button
            onClick={toggleLive}
            className="flex items-center gap-2 rounded-xl px-3.5 py-2.5 text-[12px] font-bold tracking-wide border transition-all"
            style={
              isLive
                ? { backgroundColor: "#22d3a018", color: "#22d3a0", borderColor: "#22d3a040" }
                : { backgroundColor: "#111318", color: "#8b92a8", borderColor: "rgba(255,255,255,0.05)" }
            }
          >
            <span
              className={`w-2 h-2 rounded-full ${isLive ? "animate-pulse-slow" : ""}`}
              style={{ backgroundColor: isLive ? "#22d3a0" : "#8b92a8" }}
            />
            {isLive ? "LIVE" : "IN PAUSA"}
          </button>
          <button
            onClick={refresh}
            className="flex items-center gap-2 bg-[#111318] border border-white/5 rounded-xl px-3.5 py-2.5 text-[12px] font-medium text-[#8b92a8] hover:border-[#38b6ff] hover:text-[#38b6ff] transition-all"
          >
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" className={refreshing ? "animate-spin" : ""}>
              <polyline points="23 4 23 10 17 10" />
              <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" />
            </svg>
            Aggiorna
          </button>
        </div>
      </header>

      {/* Legenda categorie */}
      <div className="bg-[#111318] border border-white/5 rounded-[14px] px-4 py-2.5 mb-4 flex-shrink-0">
        <Legend />
      </div>

      {/* Errori */}
      {error && (
        <div className="flex items-center gap-2 bg-[#ff5e5e]/10 border border-[#ff5e5e]/20 text-[#ff5e5e] rounded-xl px-4 py-2.5 text-[12px] font-semibold mb-4 flex-shrink-0">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" /><line x1="12" y1="9" x2="12" y2="13" /><line x1="12" y1="17" x2="12.01" y2="17" /></svg>
          Backend non raggiungibile ({error}). Riprovo automaticamente…
        </div>
      )}
      {actionError && (
        <div className="flex items-center justify-between gap-2 bg-[#fbbf24]/10 border border-[#fbbf24]/20 text-[#fbbf24] rounded-xl px-4 py-2.5 text-[12px] font-semibold mb-4 flex-shrink-0">
          <span>Azione non riuscita: {actionError}</span>
          <button onClick={clearActionError} className="text-[#fbbf24] hover:text-[#f0f4ff]">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5"><line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" /></svg>
          </button>
        </div>
      )}

      {/* Griglia ordini */}
      <main className="flex-1 min-h-0 overflow-y-auto">
        {orders.length === 0 ? (
          <div className="h-full min-h-[300px] border border-dashed border-white/10 rounded-[18px] flex flex-col items-center justify-center text-[#4e5566]">
            <svg width="44" height="44" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
              <polyline points="20 6 9 17 4 12" />
            </svg>
            <span className="text-[13px] font-semibold uppercase tracking-wider mt-3">
              Nessun ordine da preparare
            </span>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4 gap-4 content-start">
            {orders.map((o) => (
              <OrderCard
                key={o.id}
                order={o}
                categoryMap={categoryMap}
                busy={!!busy[o.id]}
                onAct={act}
              />
            ))}
          </div>
        )}
      </main>

      {/* Footer */}
      <footer className="flex items-center justify-between text-[11px] text-[#4e5566] mt-4 flex-shrink-0">
        <span className="uppercase tracking-wider font-semibold">
          Fatto → Tabellone (Pronto) · Cancellato → Tabellone (rosso) · Admin &amp; Manager avvisati
        </span>
        {lastUpdate && (
          <span>
            Aggiornato:{" "}
            {lastUpdate.toLocaleTimeString("it-IT", { hour: "2-digit", minute: "2-digit", second: "2-digit" })}
          </span>
        )}
      </footer>
    </div>
  );
}
