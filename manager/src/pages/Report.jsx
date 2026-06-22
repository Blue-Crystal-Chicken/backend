import { useState } from "react";
import { useReport, RANGE_OPTIONS } from "../hooks/useReport";

import TopProdotti           from "../components/report/TopProdotti";
import DistribuzioneCategorie from "../components/report/DistribuzioneCategorie";
import FunnelStati           from "../components/report/FunnelStati";
import HeatmapOraria         from "../components/report/HeatmapOraria";
import AnalisiPagamenti      from "../components/report/AnalisiPagamenti";
import Personalizzazioni     from "../components/report/Personalizzazioni";
import ProfiloNutrizionale   from "../components/report/ProfiloNutrizionale";
import MenusOfferte          from "../components/report/MenusOfferte";
import StaffLeaderboard      from "../components/report/StaffLeaderboard";
import MagazzinoSnapshot     from "../components/report/MagazzinoSnapshot";
import ProdottiFantasma      from "../components/report/ProdottiFantasma";
import TavoliServizio        from "../components/report/TavoliServizio";

// ── Struttura a gruppi ────────────────────────────────────────────────────────

const GRUPPI = [
  {
    key:   "prodotti",
    label: "Prodotti",
    color: "#38b6ff",
    icon: (
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
        <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z" />
        <line x1="3" y1="6" x2="21" y2="6" />
        <path d="M16 10a4 4 0 0 1-8 0" />
      </svg>
    ),
    sezioni: [
      {
        key: "top-prodotti",
        label: "Top Prodotti",
        component: TopProdotti,
        icon: (
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <line x1="18" y1="20" x2="18" y2="10" /><line x1="12" y1="20" x2="12" y2="4" /><line x1="6" y1="20" x2="6" y2="14" />
          </svg>
        ),
      },
      {
        key: "categorie",
        label: "Categorie",
        component: DistribuzioneCategorie,
        icon: (
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <rect x="3" y="3" width="7" height="7" /><rect x="14" y="3" width="7" height="7" />
            <rect x="14" y="14" width="7" height="7" /><rect x="3" y="14" width="7" height="7" />
          </svg>
        ),
      },
      {
        key: "nutrizionale",
        label: "Nutrizionale",
        component: ProfiloNutrizionale,
        icon: (
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
          </svg>
        ),
      },
    ],
  },
  {
    key:   "ordini",
    label: "Ordini",
    color: "#22d3a0",
    icon: (
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
        <path d="M9 11l3 3L22 4" />
        <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" />
      </svg>
    ),
    sezioni: [
      {
        key: "funnel",
        label: "Funnel Stati",
        component: FunnelStati,
        icon: (
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3" />
          </svg>
        ),
      },
      {
        key: "heatmap",
        label: "Heatmap Oraria",
        component: HeatmapOraria,
        icon: (
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <rect x="3" y="4" width="18" height="18" rx="2" /><line x1="16" y1="2" x2="16" y2="6" />
            <line x1="8" y1="2" x2="8" y2="6" /><line x1="3" y1="10" x2="21" y2="10" />
          </svg>
        ),
      },
      {
        key: "personalizzazioni",
        label: "Personalizzazioni",
        component: Personalizzazioni,
        icon: (
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M12 20h9" /><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z" />
          </svg>
        ),
      },
    ],
  },
  {
    key:   "business",
    label: "Business",
    color: "#fbbf24",
    icon: (
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
        <line x1="12" y1="1" x2="12" y2="23" />
        <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" />
      </svg>
    ),
    sezioni: [
      {
        key: "pagamenti",
        label: "Pagamenti",
        component: AnalisiPagamenti,
        icon: (
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <rect x="1" y="4" width="22" height="16" rx="2" /><line x1="1" y1="10" x2="23" y2="10" />
          </svg>
        ),
      },
      {
        key: "menus-offerte",
        label: "Menus & Offerte",
        component: MenusOfferte,
        icon: (
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z" />
          </svg>
        ),
      },
      {
        key: "tavoli",
        label: "Tavoli & Canali",
        component: TavoliServizio,
        icon: (
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <rect x="3" y="3" width="18" height="18" rx="2" /><path d="M3 9h18" /><path d="M9 21V9" />
          </svg>
        ),
      },
    ],
  },
  {
    key:   "risorse",
    label: "Risorse",
    color: "#a78bfa",
    icon: (
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
        <circle cx="9" cy="7" r="4" />
        <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
        <path d="M16 3.13a4 4 0 0 1 0 7.75" />
      </svg>
    ),
    sezioni: [
      {
        key: "staff",
        label: "Staff",
        component: StaffLeaderboard,
        icon: (
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" /><circle cx="9" cy="7" r="4" />
          </svg>
        ),
      },
      {
        key: "magazzino",
        label: "Magazzino",
        component: MagazzinoSnapshot,
        icon: (
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z" />
            <polyline points="3.27 6.96 12 12.01 20.73 6.96" /><line x1="12" y1="22.08" x2="12" y2="12" />
          </svg>
        ),
      },
      {
        key: "fantasma",
        label: "Prodotti Fantasma",
        component: ProdottiFantasma,
        icon: (
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" />
            <line x1="1" y1="1" x2="23" y2="23" />
          </svg>
        ),
      },
    ],
  },
];

// ── Componente ────────────────────────────────────────────────────────────────

export default function Report() {
  const [gruppoKey, setGruppoKey]   = useState("prodotti");
  const [sezioneKey, setSezioneKey] = useState("top-prodotti");

  const { range, setRange, locationId, setLocationId, locations, orders, products, ingredients, loading, error, refresh } = useReport();

  const gruppo  = GRUPPI.find((g) => g.key === gruppoKey);
  const sezione = gruppo?.sezioni.find((s) => s.key === sezioneKey);
  const Component = sezione?.component;

  function selectGruppo(g) {
    setGruppoKey(g.key);
    setSezioneKey(g.sezioni[0].key);
  }

  return (
    <div className="p-[28px_32px] max-w-[1440px] mx-auto text-[#f0f4ff] bg-[#0b0c10] min-h-screen">

      {/* Header */}
      <div className="flex items-end justify-between mb-6">
        <div>
          <h1 className="font-['Syne'] text-[26px] font-extrabold tracking-tight mb-1">Report</h1>
          <p className="text-[13px] text-[#8b92a8] uppercase tracking-wide">
            Analisi avanzata — tutti i dati disponibili
          </p>
        </div>

        {/* Filtri globali inline a destra */}
        <div className="flex items-center gap-2">
          <div className="relative">
            <select
              value={locationId ?? ""}
              onChange={(e) => setLocationId(e.target.value ? Number(e.target.value) : null)}
              className="appearance-none bg-[#111318] border border-white/5 rounded-xl pl-4 pr-9 py-2.5 text-[13px] text-[#f0f4ff] focus:outline-none focus:border-[#38b6ff] transition-all cursor-pointer min-w-[160px]"
            >
              <option value="" className="bg-[#111318]">Tutte le sedi</option>
              {locations.map((l) => (
                <option key={l.id} value={l.id} className="bg-[#111318]">{l.name}</option>
              ))}
            </select>
            <svg className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-[#4e5566]" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
              <polyline points="6 9 12 15 18 9" />
            </svg>
          </div>

          <div className="flex items-center gap-1 bg-[#111318] border border-white/5 rounded-xl p-1">
            {RANGE_OPTIONS.map((opt) => (
              <button
                key={opt.key}
                onClick={() => setRange(opt.key)}
                className={`px-3 py-1.5 rounded-lg text-[12px] font-semibold transition-all ${
                  range === opt.key ? "bg-[#38b6ff] text-[#111318]" : "text-[#8b92a8] hover:text-[#f0f4ff]"
                }`}
              >
                {opt.label}
              </button>
            ))}
          </div>

          <button
            onClick={refresh}
            className="flex items-center gap-2 bg-[#111318] border border-white/5 rounded-xl px-3.5 py-2.5 text-[12px] font-medium text-[#8b92a8] hover:border-[#38b6ff] hover:text-[#38b6ff] transition-all"
          >
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" className={loading ? "animate-spin" : ""}>
              <polyline points="23 4 23 10 17 10" /><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" />
            </svg>
            Aggiorna
          </button>
        </div>
      </div>

      {/* ── Livello 1: card gruppo ── */}
      <div className="grid grid-cols-4 gap-3 mb-3">
        {GRUPPI.map((g) => {
          const isActive = g.key === gruppoKey;
          return (
            <button
              key={g.key}
              onClick={() => selectGruppo(g)}
              className={`relative text-left rounded-[16px] p-5 border transition-all duration-200 overflow-hidden ${
                isActive
                  ? "border-transparent"
                  : "bg-[#111318] border-white/5 hover:border-white/10"
              }`}
              style={isActive
                ? { backgroundColor: `${g.color}12`, borderColor: `${g.color}35` }
                : {}
              }
            >
              {/* Accent line top */}
              <div
                className="absolute top-0 left-0 right-0 h-[2px] transition-all duration-200"
                style={{ backgroundColor: isActive ? g.color : "transparent" }}
              />

              {/* Icon */}
              <span
                className="block mb-3 transition-colors"
                style={{ color: isActive ? g.color : "#4e5566" }}
              >
                {g.icon}
              </span>

              {/* Label */}
              <div
                className="font-['Syne'] text-[14px] font-bold mb-2 transition-colors"
                style={{ color: isActive ? g.color : "#8b92a8" }}
              >
                {g.label}
              </div>

              {/* Sezioni list */}
              <div className="space-y-0.5">
                {g.sezioni.map((s) => (
                  <div
                    key={s.key}
                    className="text-[11px] transition-colors"
                    style={{ color: isActive && s.key === sezioneKey ? g.color : "#4e5566" }}
                  >
                    {s.label}
                  </div>
                ))}
              </div>
            </button>
          );
        })}
      </div>

      {/* ── Livello 2: pill sezione ── */}
      {gruppo && (
        <div className="flex items-center gap-2 mb-6 pl-1">
          <div
            className="w-px h-4 rounded-full opacity-40"
            style={{ backgroundColor: gruppo.color }}
          />
          {gruppo.sezioni.map((s) => {
            const isActive = s.key === sezioneKey;
            return (
              <button
                key={s.key}
                onClick={() => setSezioneKey(s.key)}
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-[8px] text-[12px] font-semibold border transition-all ${
                  isActive ? "border-transparent" : "bg-transparent border-white/[0.06] text-[#8b92a8] hover:text-[#f0f4ff] hover:border-white/10"
                }`}
                style={isActive
                  ? { backgroundColor: `${gruppo.color}20`, borderColor: `${gruppo.color}40`, color: gruppo.color }
                  : {}
                }
              >
                <span style={{ color: isActive ? gruppo.color : "#4e5566" }}>{s.icon}</span>
                {s.label}
              </button>
            );
          })}
        </div>
      )}

      {/* Errore */}
      {error && (
        <div className="mb-6 px-4 py-3 rounded-xl bg-[#ff5e5e]/10 border border-[#ff5e5e]/20 text-[13px] text-[#ff5e5e]">
          {error}
        </div>
      )}

      {/* ── Sezione attiva ── */}
      {Component && (
        <Component
          orders={orders}
          products={products}
          ingredients={ingredients}
          loading={loading}
          error={error}
        />
      )}

    </div>
  );
}
