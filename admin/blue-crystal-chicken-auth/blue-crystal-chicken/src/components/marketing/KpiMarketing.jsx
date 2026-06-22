const fmtCur = (n) => new Intl.NumberFormat("it-IT", { style: "currency", currency: "EUR" }).format(n ?? 0);

const Icon = {
  tag: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" {...p}><path d="M20.59 13.41 13.42 20.6a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z" /><line x1="7" y1="7" x2="7.01" y2="7" /></svg>),
  euro: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" {...p}><path d="M14 21a8 8 0 1 1 0-16" /><line x1="4" y1="10" x2="13" y2="10" /><line x1="4" y1="14" x2="11" y2="14" /></svg>),
  box: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...p}><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z" /><polyline points="3.27 6.96 12 12.01 20.73 6.96" /><line x1="12" y1="22.08" x2="12" y2="12" /></svg>),
  savings: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" {...p}><path d="M19 5 5 19" /><circle cx="6.5" cy="6.5" r="2.5" /><circle cx="17.5" cy="17.5" r="2.5" /></svg>),
};

const Card = ({ label, value, icon, subtext, accentColor }) => (
  <div className="bg-[#111318] border border-white/5 rounded-[18px] p-[22px_24px] relative overflow-hidden transition-all duration-200 hover:border-[#38b6ff]/25 hover:-translate-y-0.5 group">
    <div className="absolute top-0 left-0 right-0 h-[2px] opacity-70" style={{ backgroundColor: accentColor }} />
    <div className="flex items-center justify-between mb-[14px]">
      <span className="text-[11px] font-semibold tracking-[1px] uppercase text-[#8b92a8]">{label}</span>
      <span className="w-[34px] h-[34px] rounded-[9px] flex items-center justify-center transition-transform group-hover:scale-110" style={{ background: `${accentColor}18`, color: accentColor }}>
        {icon}
      </span>
    </div>
    <div className="font-['Syne'] text-[28px] font-bold leading-none mb-2 text-[#f0f4ff] truncate">{value}</div>
    {subtext && <div className="text-[12px] font-medium text-[#8b92a8] truncate">{subtext}</div>}
  </div>
);

export default function KpiMarketing({ kpi }) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <Card
        label="Offerte attive"
        value={kpi.active}
        icon={<Icon.tag width="16" height="16" />}
        subtext={`su ${kpi.total} totali`}
        accentColor="#22d3a0"
      />
      <Card
        label="Prezzo medio offerta"
        value={fmtCur(kpi.avgPrice)}
        icon={<Icon.euro width="16" height="16" />}
        subtext="media prezzo bundle"
        accentColor="#38b6ff"
      />
      <Card
        label="Prodotti in promo"
        value={kpi.productsInPromo}
        icon={<Icon.box width="16" height="16" />}
        subtext="referenze nei bundle"
        accentColor="#a78bfa"
      />
      <Card
        label="Risparmio medio"
        value={fmtCur(kpi.avgSaving)}
        icon={<Icon.savings width="16" height="16" />}
        subtext={kpi.avgDiscount ? `sconto medio ${kpi.avgDiscount.toFixed(0)}%` : "vs prezzo a listino"}
        accentColor="#fbbf24"
      />
    </div>
  );
}
