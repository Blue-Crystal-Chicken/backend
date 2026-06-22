import { SEDE_COLORS } from "../../hooks/useFinanze";

const RANGE_OPTIONS = [
  { key: "1",  label: "Oggi" },
  { key: "7",  label: "7 giorni" },
  { key: "14", label: "14 giorni" },
  { key: "30", label: "30 giorni" },
];

export default function FiltriFinanze({
  locations,
  primaryId,
  onPrimaryChange,
  comparedIds,
  onToggleComparison,
  range,
  onRangeChange,
  refreshing,
  onRefresh,
  isLive,
  onToggleLive,
}) {
  const availableForComparison = locations.filter((l) => l.id !== primaryId);

  return (
    <div className="mb-6 space-y-3">

      {/* Riga 1: sede primaria + range + refresh */}
      <div className="flex flex-wrap items-center gap-3">

        <div className="relative">
          <select
            value={primaryId ?? ""}
            onChange={(e) => onPrimaryChange(e.target.value ? Number(e.target.value) : null)}
            className="appearance-none bg-[#111318] border border-white/5 rounded-xl pl-4 pr-9 py-2.5 text-[13px] text-[#f0f4ff] focus:outline-none focus:border-[#38b6ff] transition-all cursor-pointer min-w-[180px]"
          >
            <option value="" className="bg-[#111318]">Tutte le sedi</option>
            {locations.map((l) => (
              <option key={l.id} value={l.id} className="bg-[#111318]">{l.name}</option>
            ))}
          </select>
          <svg
            className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-[#4e5566]"
            width="12" height="12" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"
          >
            <polyline points="6 9 12 15 18 9" />
          </svg>
        </div>

        <div className="flex items-center gap-1 bg-[#111318] border border-white/5 rounded-xl p-1">
          {RANGE_OPTIONS.map((opt) => (
            <button
              key={opt.key}
              onClick={() => onRangeChange(opt.key)}
              className={`px-3 py-1.5 rounded-lg text-[12px] font-semibold transition-all ${
                range === opt.key
                  ? "bg-[#38b6ff] text-[#111318]"
                  : "text-[#8b92a8] hover:text-[#f0f4ff]"
              }`}
            >
              {opt.label}
            </button>
          ))}
        </div>

        <div className="ml-auto flex items-center gap-2">
          {/* Bottone LIVE — sempre visibile; se range != oggi lo reimposta */}
          <button
            onClick={onToggleLive}
            className={`flex items-center gap-1.5 rounded-full px-3 py-1.5 text-[12px] font-semibold border transition-all ${
              isLive
                ? "bg-[#22d3a0]/10 border-[#22d3a0]/25 text-[#22d3a0]"
                : "bg-[#111318] border-white/5 text-[#8b92a8] hover:border-[#22d3a0]/30 hover:text-[#22d3a0]"
            }`}
          >
            <span
              className={`w-1.5 h-1.5 rounded-full ${isLive ? "bg-[#22d3a0] animate-pulse" : "bg-[#8b92a8]"}`}
            />
            LIVE
          </button>

          <button
            onClick={onRefresh}
            className="flex items-center gap-2 bg-[#111318] border border-white/5 rounded-xl px-3.5 py-2.5 text-[12px] font-medium text-[#8b92a8] hover:border-[#38b6ff] hover:text-[#38b6ff] transition-all"
          >
            <svg
              width="13" height="13" viewBox="0 0 24 24" fill="none"
              stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"
              className={refreshing ? "animate-spin" : ""}
            >
              <polyline points="23 4 23 10 17 10" />
              <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" />
            </svg>
            Aggiorna
          </button>
        </div>
      </div>

      {/* Riga 2: chip confronto (solo con sede specifica selezionata) */}
      {primaryId !== null && availableForComparison.length > 0 && (
        <div className="flex flex-wrap items-center gap-2">
          <span className="text-[11px] font-semibold uppercase tracking-wider text-[#4e5566]">
            Confronta con:
          </span>
          {availableForComparison.map((loc) => {
            const isActive   = comparedIds.includes(loc.id);
            const colorIndex = comparedIds.indexOf(loc.id) + 1; // 1 o 2
            const color      = isActive ? SEDE_COLORS[colorIndex] : null;
            const isDisabled = !isActive && comparedIds.length >= 2;

            return (
              <button
                key={loc.id}
                onClick={() => onToggleComparison(loc.id)}
                disabled={isDisabled}
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-[12px] font-semibold border transition-all ${
                  isActive
                    ? "border-current"
                    : isDisabled
                      ? "border-white/5 text-[#4e5566] cursor-not-allowed"
                      : "border-white/5 text-[#8b92a8] hover:border-white/20 hover:text-[#f0f4ff]"
                }`}
                style={isActive ? { color, borderColor: `${color}50`, backgroundColor: `${color}12` } : {}}
              >
                <span
                  className="w-2 h-2 rounded-full"
                  style={{ backgroundColor: isActive ? color : "currentColor", opacity: isActive ? 1 : 0.4 }}
                />
                {loc.name}
              </button>
            );
          })}
          {comparedIds.length > 0 && (
            <span className="text-[11px] text-[#4e5566]">
              {2 - comparedIds.length === 0 ? "Massimo raggiunto" : `Puoi aggiungere ancora ${2 - comparedIds.length}`}
            </span>
          )}
        </div>
      )}
    </div>
  );
}
