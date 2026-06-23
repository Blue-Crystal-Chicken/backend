import { useState, useEffect } from "react";
import { useOrderBoard } from "./hooks/useOrderBoard";
import { fetchSedi } from "./api/orderApi";
import BoardColumn from "./components/BoardColumn";

function useClock() {
  const [now, setNow] = useState(new Date());
  useEffect(() => {
    const id = setInterval(() => setNow(new Date()), 1000);
    return () => clearInterval(id);
  }, []);
  return now;
}

// Selettore sede: carica l'elenco e ricorda la scelta in localStorage.
function useSedi() {
  const [sedi, setSedi] = useState([]);
  const [selected, setSelected] = useState(() => localStorage.getItem("tabellone_sede") || "");
  useEffect(() => { fetchSedi().then(setSedi); }, []);
  const select = (id) => {
    setSelected(id);
    if (id) localStorage.setItem("tabellone_sede", id); else localStorage.removeItem("tabellone_sede");
  };
  return { sedi, selected, select };
}

export default function App() {
  const { sedi, selected, select } = useSedi();
  const {
    columns,
    loading,
    refreshing,
    error,
    isLive,
    lastUpdate,
    refresh,
    toggleLive,
    advance,
  } = useOrderBoard(selected ? Number(selected) : null);
  const now = useClock();

  if (loading) {
    return (
      <div className="flex h-screen items-center justify-center bg-[#0b0c10] text-[14px] font-semibold text-[#38b6ff] tracking-widest font-['Syne'] uppercase animate-pulse">
        Caricamento tabellone…
      </div>
    );
  }

  return (
    <div className="p-[24px_32px] mx-auto text-[#f0f4ff] bg-[#0b0c10] min-h-screen flex flex-col">
      {/* Header */}
      <header className="flex items-end justify-between mb-6 flex-shrink-0">
        <div>
          <div className="flex items-center gap-3 mb-1">
            <h1 className="font-['Syne'] text-[28px] font-extrabold tracking-tight">
              Tabellone Ordini
            </h1>
            {/* Dropdown selezione sede: al cambio, filtra gli ordini di quella sede */}
            <select
              value={selected}
              onChange={(e) => select(e.target.value)}
              className="font-['Syne'] text-[14px] font-bold px-3 py-1 rounded-full bg-[#a78bfa]/10 text-[#a78bfa] border border-[#a78bfa]/30 focus:outline-none cursor-pointer"
            >
              <option value="">Tutte le sedi</option>
              {sedi.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name}{s.city ? ` · ${s.city}` : ""}
                </option>
              ))}
            </select>
          </div>
          <p className="text-[13px] text-[#8b92a8] uppercase tracking-wide">
            Blue Crystal Chicken — Cucina &amp; Ritiro
          </p>
        </div>

        <div className="flex items-center gap-3">
          {/* Orologio */}
          <div className="font-['Syne'] text-[26px] font-bold text-[#f0f4ff] tabular-nums">
            {now.toLocaleTimeString("it-IT", {
              hour: "2-digit",
              minute: "2-digit",
              second: "2-digit",
            })}
          </div>

          {/* Badge LIVE / toggle */}
          <button
            onClick={toggleLive}
            className="flex items-center gap-2 rounded-xl px-3.5 py-2.5 text-[12px] font-bold tracking-wide border transition-all"
            style={
              isLive
                ? {
                    backgroundColor: "#22d3a018",
                    color: "#22d3a0",
                    borderColor: "#22d3a040",
                  }
                : {
                    backgroundColor: "#111318",
                    color: "#8b92a8",
                    borderColor: "rgba(255,255,255,0.05)",
                  }
            }
          >
            <span
              className={`w-2 h-2 rounded-full ${isLive ? "animate-pulse-slow" : ""}`}
              style={{ backgroundColor: isLive ? "#22d3a0" : "#8b92a8" }}
            />
            {isLive ? "LIVE" : "IN PAUSA"}
          </button>

          {/* Refresh manuale */}
          <button
            onClick={refresh}
            className="flex items-center gap-2 bg-[#111318] border border-white/5 rounded-xl px-3.5 py-2.5 text-[12px] font-medium text-[#8b92a8] hover:border-[#38b6ff] hover:text-[#38b6ff] transition-all"
          >
            <svg
              width="13"
              height="13"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2.5"
              strokeLinecap="round"
              strokeLinejoin="round"
              className={refreshing ? "animate-spin" : ""}
            >
              <polyline points="23 4 23 10 17 10" />
              <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" />
            </svg>
            Aggiorna
          </button>
        </div>
      </header>

      {/* Banner errore (non blocca il tabellone, mostra l'ultimo dato valido) */}
      {error && (
        <div className="flex items-center gap-2 bg-[#ff5e5e]/10 border border-[#ff5e5e]/20 text-[#ff5e5e] rounded-xl px-4 py-2.5 text-[12px] font-semibold mb-4 flex-shrink-0">
          <svg
            width="14"
            height="14"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
          >
            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
            <line x1="12" y1="9" x2="12" y2="13" />
            <line x1="12" y1="17" x2="12.01" y2="17" />
          </svg>
          Connessione al backend non riuscita ({error}). Riprovo automaticamente…
        </div>
      )}

      {/* Colonne */}
      <main className="grid grid-cols-1 lg:grid-cols-2 gap-6 flex-1 min-h-0">
        {columns.map((col) => (
          <BoardColumn key={col.key} column={col} onAdvance={advance} />
        ))}
      </main>

      {/* Footer */}
      <footer className="flex items-center justify-between text-[11px] text-[#4e5566] mt-5 flex-shrink-0">
        <span className="uppercase tracking-wider font-semibold">
          Aggiornamento automatico ogni polling
        </span>
        {lastUpdate && (
          <span>
            Ultimo aggiornamento:{" "}
            {lastUpdate.toLocaleTimeString("it-IT", {
              hour: "2-digit",
              minute: "2-digit",
              second: "2-digit",
            })}
          </span>
        )}
      </footer>
    </div>
  );
}
