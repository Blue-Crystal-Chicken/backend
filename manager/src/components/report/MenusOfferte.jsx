import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell, PieChart, Pie } from "recharts";

const fmtEur = (n) => new Intl.NumberFormat("it-IT", { style: "currency", currency: "EUR" }).format(n ?? 0);
const fmtShort = (n) => n >= 1000 ? `€${(n / 1000).toFixed(1)}k` : `€${Number(n).toFixed(0)}`;

function deriveOffersMenus(orders) {
  const offerMap   = {};
  let offerRevenue = 0;
  let prodRevenue  = 0;
  let offerUnits   = 0;
  let prodUnits    = 0;

  orders.forEach((o) => {
    (o.items ?? []).forEach((item) => {
      const qty = item.quantity ?? 1;
      const rev = (item.price ?? 0) * qty;

      if (item.offerId && item.offerName) {
        const key = item.offerName;
        if (!offerMap[key]) offerMap[key] = { name: key, units: 0, revenue: 0 };
        offerMap[key].units   += qty;
        offerMap[key].revenue += rev;
        offerRevenue += rev;
        offerUnits   += qty;
      } else {
        prodRevenue += rev;
        prodUnits   += qty;
      }
    });
  });

  const topOffers = Object.values(offerMap).sort((a, b) => b.revenue - a.revenue).slice(0, 8);

  return { topOffers, offerRevenue, prodRevenue, offerUnits, prodUnits };
}

const OfferTooltip = ({ active, payload }) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-[#181c23] border border-white/5 rounded-[10px] p-[8px_12px]">
      <div className="font-['Syne'] text-[13px] font-bold text-[#f0f4ff]">{payload[0].payload.name}</div>
      <div className="text-[12px] text-[#fbbf24]">{fmtEur(payload[0].value)}</div>
    </div>
  );
};

const SplitTooltip = ({ active, payload }) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-[#181c23] border border-white/5 rounded-[10px] p-[8px_12px]">
      <div className="font-['Syne'] text-[13px] font-bold text-[#f0f4ff]">{payload[0].name}</div>
      <div className="text-[12px] text-[#22d3a0]">{fmtEur(payload[0].value)}</div>
    </div>
  );
};

function ComingSoon({ label }) {
  return (
    <div className="bg-[#111318] border border-white/5 border-dashed rounded-[18px] p-6 flex flex-col items-center justify-center min-h-[160px] gap-2">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#4e5566" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <circle cx="12" cy="12" r="10" />
        <line x1="12" y1="8" x2="12" y2="12" />
        <line x1="12" y1="16" x2="12.01" y2="16" />
      </svg>
      <span className="text-[12px] text-[#4e5566] text-center">{label}</span>
    </div>
  );
}

export default function MenusOfferte({ orders, loading }) {
  const { topOffers, offerRevenue, prodRevenue, offerUnits, prodUnits } = deriveOffersMenus(orders);
  const totalRevenue = offerRevenue + prodRevenue;
  const totalUnits   = offerUnits + prodUnits;

  const splitData = [
    { name: "Prodotti singoli", value: prodRevenue,  color: "#38b6ff", units: prodUnits },
    { name: "Offerte",          value: offerRevenue, color: "#fbbf24", units: offerUnits },
  ];

  return (
    <div className="space-y-4">

      {/* KPI */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {[
          { label: "Offerte distinte", value: loading ? "—" : topOffers.length, color: "#fbbf24" },
          { label: "Fatturato da offerte", value: loading ? "—" : fmtShort(offerRevenue), color: "#fbbf24" },
          { label: "% fatturato offerte", value: loading ? "—" : `${totalRevenue > 0 ? ((offerRevenue / totalRevenue) * 100).toFixed(1) : 0}%`, color: "#22d3a0" },
          { label: "Unità offerte vendute", value: loading ? "—" : offerUnits, color: "#a78bfa" },
        ].map(({ label, value, color }) => (
          <div key={label} className="bg-[#111318] border border-white/5 rounded-[14px] p-5 relative overflow-hidden">
            <div className="absolute top-0 left-0 right-0 h-[2px]" style={{ backgroundColor: color }} />
            <div className="text-[10px] font-semibold tracking-widest uppercase text-[#4e5566] mb-2">{label}</div>
            <div className="font-['Syne'] text-[24px] font-bold" style={{ color }}>{value}</div>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-[1fr_340px] gap-4">

        {/* Top offerte per fatturato */}
        <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
          <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff] mb-5">Top offerte per fatturato</h3>
          {loading ? (
            <div className="space-y-3 animate-pulse">
              {Array.from({ length: 6 }).map((_, i) => (
                <div key={i} className="flex gap-3"><div className="h-3 w-[30%] bg-white/[0.05] rounded" /><div className="h-6 flex-1 bg-white/[0.05] rounded" /></div>
              ))}
            </div>
          ) : topOffers.length === 0 ? (
            <div className="flex items-center justify-center h-[200px] text-[13px] text-[#4e5566]">
              Nessuna offerta ordinata nel periodo.
            </div>
          ) : (
            <div className="h-[240px]">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={topOffers} layout="vertical" margin={{ left: 0, right: 16 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.04)" horizontal={false} />
                  <XAxis type="number" tickFormatter={fmtShort} tick={{ fill: "#4e5566", fontSize: 11 }} axisLine={false} tickLine={false} />
                  <YAxis type="category" dataKey="name" tick={{ fill: "#8b92a8", fontSize: 11 }} axisLine={false} tickLine={false} width={130} />
                  <Tooltip content={<OfferTooltip />} />
                  <Bar dataKey="revenue" radius={[0, 4, 4, 0]} maxBarSize={18}>
                    {topOffers.map((_, i) => <Cell key={i} fill="#fbbf24" fillOpacity={0.85 - i * 0.05} />)}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          )}
        </div>

        {/* Split prodotti vs offerte */}
        <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
          <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff] mb-5">Split fatturato</h3>
          {loading ? (
            <div className="flex flex-col gap-4 animate-pulse">
              <div className="w-[140px] h-[140px] mx-auto rounded-full bg-white/[0.04]" />
              {[1,2].map(i => <div key={i} className="h-4 bg-white/[0.05] rounded" />)}
            </div>
          ) : (
            <>
              <div className="flex justify-center mb-4">
                <div className="w-[160px] h-[160px]">
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie data={splitData} dataKey="value" innerRadius={48} outerRadius={70} paddingAngle={4} strokeWidth={0}>
                        {splitData.map((d) => <Cell key={d.name} fill={d.color} />)}
                      </Pie>
                      <Tooltip content={<SplitTooltip />} />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
              </div>
              <div className="space-y-3">
                {splitData.map((d) => (
                  <div key={d.name} className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <span className="w-2.5 h-2.5 rounded-[3px]" style={{ background: d.color }} />
                      <span className="text-[12px] text-[#8b92a8]">{d.name}</span>
                    </div>
                    <div className="text-right">
                      <div className="font-['Syne'] text-[13px] font-bold text-[#f0f4ff]">{fmtEur(d.value)}</div>
                      <div className="text-[10px] text-[#4e5566]">{d.units} unità</div>
                    </div>
                  </div>
                ))}
              </div>
            </>
          )}
        </div>
      </div>

      {/* Menus — richiede endpoint dedicato */}
      <ComingSoon label="Analisi Menu: richiede endpoint /api/menus — da implementare nel backend" />
    </div>
  );
}
