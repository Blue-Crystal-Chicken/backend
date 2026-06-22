const fmt = (n) =>
  new Intl.NumberFormat("it-IT", { style: "currency", currency: "EUR" }).format(n ?? 0);

function KpiCard({ label, children, accentColor }) {
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

const SKELETON_BARS = [45, 62, 38, 72, 55, 68, 42, 78, 58, 65, 48, 70];

function SkeletonVal({ wide }) {
  return (
    <div
      className={`h-[30px] bg-white/[0.06] rounded-lg animate-pulse ${wide ? "w-[120px]" : "w-[80px]"}`}
    />
  );
}

export default function KpiOrdini({ kpi, loading }) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">

      {/* Card 1: Ordini totali + % cancellati */}
      <KpiCard label="Ordini nel periodo" accentColor="#38b6ff">
        <div className="font-['Syne'] text-[28px] font-bold leading-none mb-3 text-[#f0f4ff]">
          {loading ? <SkeletonVal /> : kpi.total}
        </div>
        <div className="h-px bg-white/5 mb-3" />
        <div className="flex items-center justify-between">
          {loading ? (
            <div className="h-[18px] w-[100px] bg-white/[0.06] rounded animate-pulse" />
          ) : (
            <>
              <span className="text-[12px] text-[#8b92a8]">
                {kpi.cancelled} cancellat{kpi.cancelled === 1 ? "o" : "i"}
              </span>
              <span
                className="text-[12px] font-bold px-2 py-0.5 rounded-full"
                style={{
                  backgroundColor: kpi.cancelledPct > 10 ? "#ff5e5e18" : "#22d3a018",
                  color: kpi.cancelledPct > 10 ? "#ff5e5e" : "#22d3a0",
                }}
              >
                {kpi.cancelledPct}%
              </span>
            </>
          )}
        </div>
      </KpiCard>

      {/* Card 2: Fatturato */}
      <KpiCard label="Fatturato periodo" accentColor="#22d3a0">
        <div className="flex items-start justify-between mb-[14px]">
          <div />
          <span
            className="w-[34px] h-[34px] rounded-[9px] flex items-center justify-center"
            style={{ background: "#22d3a018" }}
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#22d3a0" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <line x1="12" y1="1" x2="12" y2="23" />
              <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" />
            </svg>
          </span>
        </div>
        <div className="font-['Syne'] text-[28px] font-bold leading-none mb-2 text-[#f0f4ff] truncate">
          {loading ? <SkeletonVal wide /> : fmt(kpi.revenue)}
        </div>
        <div className="text-[12px] font-medium text-[#8b92a8]">Totale incassato</div>
      </KpiCard>

      {/* Card 3: Scontrino medio */}
      <KpiCard label="Scontrino medio" accentColor="#a78bfa">
        <div className="flex items-start justify-between mb-[14px]">
          <div />
          <span
            className="w-[34px] h-[34px] rounded-[9px] flex items-center justify-center"
            style={{ background: "#a78bfa18" }}
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#a78bfa" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
              <polyline points="14 2 14 8 20 8" />
              <line x1="16" y1="13" x2="8" y2="13" />
              <line x1="16" y1="17" x2="8" y2="17" />
              <polyline points="10 9 9 9 8 9" />
            </svg>
          </span>
        </div>
        <div className="font-['Syne'] text-[28px] font-bold leading-none mb-2 text-[#f0f4ff] truncate">
          {loading ? <SkeletonVal wide /> : fmt(kpi.avgTicket)}
        </div>
        <div className="text-[12px] font-medium text-[#8b92a8]">Per ordine</div>
      </KpiCard>

    </div>
  );
}
