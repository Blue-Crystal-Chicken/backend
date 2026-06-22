import { CANALE_LABELS, CANALE_COLORS } from "../../hooks/useFinanze";

const fmt = (n) =>
  new Intl.NumberFormat("it-IT", { style: "currency", currency: "EUR" }).format(n ?? 0);

function Delta({ value }) {
  if (value === null) {
    return <span className="text-[11px] text-[#4e5566]">Nessun dato precedente</span>;
  }
  const isPos = Number(value) >= 0;
  return (
    <span className={`text-[12px] font-semibold flex items-center gap-1 ${isPos ? "text-[#22d3a0]" : "text-[#ff5e5e]"}`}>
      <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
        {isPos
          ? <><line x1="12" y1="19" x2="12" y2="5"/><polyline points="5 12 12 5 19 12"/></>
          : <><line x1="12" y1="5" x2="12" y2="19"/><polyline points="19 12 12 19 5 12"/></>
        }
      </svg>
      {isPos ? "+" : ""}{value}% vs prec.
    </span>
  );
}

function KpiCard({ label, accentColor, children }) {
  return (
    <div className="bg-[#111318] border border-white/5 rounded-[18px] p-[22px_24px] relative overflow-hidden transition-all duration-200 hover:border-[#38b6ff]/25 hover:-translate-y-0.5">
      <div className="absolute top-0 left-0 right-0 h-[2px] opacity-70" style={{ backgroundColor: accentColor }} />
      <span className="text-[11px] font-semibold tracking-[1px] uppercase text-[#8b92a8] block mb-[14px]">
        {label}
      </span>
      {children}
    </div>
  );
}

export default function KpiFinanze({ kpi, loading }) {
  const dash = loading ? "—" : null;

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">

      {/* Fatturato */}
      <KpiCard label="Fatturato periodo" accentColor="#38b6ff">
        <div className="font-['Syne'] text-[28px] font-bold leading-none mb-2 text-[#f0f4ff] truncate">
          {dash ?? fmt(kpi.revenue)}
        </div>
        <Delta value={loading ? null : kpi.revDelta} />
      </KpiCard>

      {/* Scontrino medio */}
      <KpiCard label="Scontrino medio" accentColor="#22d3a0">
        <div className="font-['Syne'] text-[28px] font-bold leading-none mb-2 text-[#f0f4ff] truncate">
          {dash ?? fmt(kpi.avgTicket)}
        </div>
        <Delta value={loading ? null : kpi.avgDelta} />
      </KpiCard>

      {/* Numero ordini */}
      <KpiCard label="Ordini nel periodo" accentColor="#a78bfa">
        <div className="font-['Syne'] text-[28px] font-bold leading-none mb-2 text-[#f0f4ff]">
          {dash ?? kpi.count}
        </div>
        <Delta value={loading ? null : kpi.countDelta} />
      </KpiCard>

      {/* Canale dominante */}
      <KpiCard label="Canale dominante" accentColor="#fbbf24">
        {loading || !kpi.topCanale ? (
          <div className="font-['Syne'] text-[28px] font-bold leading-none text-[#f0f4ff]">—</div>
        ) : (
          <>
            <div className="font-['Syne'] text-[22px] font-bold leading-none mb-2 text-[#f0f4ff]">
              {CANALE_LABELS[kpi.topCanale] ?? kpi.topCanale}
            </div>
            <div className="flex items-center gap-2">
              <div className="flex-1 h-1.5 bg-[#181c23] rounded-full overflow-hidden">
                <div
                  className="h-full rounded-full transition-all duration-700"
                  style={{
                    width: `${kpi.topCanalePct ?? 0}%`,
                    backgroundColor: CANALE_COLORS[kpi.topCanale] ?? "#fbbf24",
                  }}
                />
              </div>
              <span className="text-[12px] font-semibold text-[#8b92a8] flex-shrink-0">
                {kpi.topCanalePct}%
              </span>
            </div>
          </>
        )}
      </KpiCard>

    </div>
  );
}
