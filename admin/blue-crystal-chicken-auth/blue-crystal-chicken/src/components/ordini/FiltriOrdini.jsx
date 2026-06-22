const RANGE_OPTIONS = [
  { key: "1",  label: "Oggi" },
  { key: "7",  label: "7 giorni" },
  { key: "14", label: "14 giorni" },
  { key: "30", label: "30 giorni" },
];

export default function FiltriOrdini({
  locations,
  locationId,
  onLocationChange,
  range,
  onRangeChange,
  refreshing,
  onRefresh,
}) {
  return (
    <div className="flex flex-wrap items-center gap-3 mb-6">

      {/* Selettore sede */}
      <div className="relative">
        <select
          value={locationId ?? ""}
          onChange={(e) => onLocationChange(e.target.value ? Number(e.target.value) : null)}
          className="appearance-none bg-[#111318] border border-white/5 rounded-xl pl-4 pr-9 py-2.5 text-[13px] text-[#f0f4ff] focus:outline-none focus:border-[#38b6ff] transition-all cursor-pointer min-w-[180px]"
        >
          <option value="" className="bg-[#111318]">Tutte le sedi</option>
          {locations.map((l) => (
            <option key={l.id} value={l.id} className="bg-[#111318]">
              {l.name}
            </option>
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

      {/* Pillole range */}
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

      {/* Bottone refresh */}
      <button
        onClick={onRefresh}
        className="ml-auto flex items-center gap-2 bg-[#111318] border border-white/5 rounded-xl px-3.5 py-2.5 text-[12px] font-medium text-[#8b92a8] hover:border-[#38b6ff] hover:text-[#38b6ff] transition-all"
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
  );
}
