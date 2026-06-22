import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell,
} from "recharts";

const fmtEur = (n) => new Intl.NumberFormat("it-IT", { style: "currency", currency: "EUR" }).format(n ?? 0);
const fmtShort = (n) => n >= 1000 ? `€${(n / 1000).toFixed(1)}k` : `€${Number(n).toFixed(0)}`;

const COLORS = ["#38b6ff", "#22d3a0", "#a78bfa", "#fbbf24", "#ff5e5e",
                "#38b6ff", "#22d3a0", "#a78bfa", "#fbbf24", "#ff5e5e"];

function deriveProducts(orders) {
  const map = {};
  orders.forEach((o) => {
    (o.items ?? []).forEach((item) => {
      if (!item.productName) return;
      const key = item.productName;
      if (!map[key]) map[key] = { name: key, units: 0, revenue: 0 };
      map[key].units   += item.quantity ?? 1;
      map[key].revenue += (item.price ?? 0) * (item.quantity ?? 1);
    });
  });
  return Object.values(map).sort((a, b) => b.revenue - a.revenue);
}

const CustomTooltipUnits = ({ active, payload }) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-[#181c23] border border-white/5 rounded-[10px] p-[8px_12px]">
      <div className="font-['Syne'] text-[13px] font-bold text-[#f0f4ff]">{payload[0].payload.name}</div>
      <div className="text-[12px] text-[#8b92a8] mt-0.5">{payload[0].value} unità</div>
    </div>
  );
};

const CustomTooltipRev = ({ active, payload }) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-[#181c23] border border-white/5 rounded-[10px] p-[8px_12px]">
      <div className="font-['Syne'] text-[13px] font-bold text-[#f0f4ff]">{payload[0].payload.name}</div>
      <div className="text-[12px] text-[#22d3a0] mt-0.5">{fmtEur(payload[0].value)}</div>
    </div>
  );
};

function SkeletonBar() {
  return (
    <div className="space-y-3 animate-pulse">
      {Array.from({ length: 8 }).map((_, i) => (
        <div key={i} className="flex items-center gap-3">
          <div className="h-3 bg-white/[0.05] rounded w-[30%]" />
          <div className="h-5 bg-white/[0.05] rounded flex-1" style={{ width: `${40 + i * 7}%`, maxWidth: "100%" }} />
        </div>
      ))}
    </div>
  );
}

export default function TopProdotti({ orders, loading }) {
  const products = deriveProducts(orders);
  const top10Vol = [...products].sort((a, b) => b.units - a.units).slice(0, 10);
  const top10Rev = products.slice(0, 10);
  const totalRevenue = products.reduce((s, p) => s + p.revenue, 0);

  return (
    <div className="space-y-6">

      {/* Grafici affiancati */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">

        {/* Per volume */}
        <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
          <div className="flex items-center justify-between mb-5">
            <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff]">Top 10 per volume</h3>
            <span className="text-[11px] font-semibold px-2 py-0.5 rounded-full bg-[#38b6ff]/10 text-[#38b6ff] border border-[#38b6ff]/20">
              Unità vendute
            </span>
          </div>
          {loading ? <SkeletonBar /> : (
            <div className="h-[280px]">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={top10Vol} layout="vertical" margin={{ left: 0, right: 16 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.04)" horizontal={false} />
                  <XAxis type="number" tick={{ fill: "#4e5566", fontSize: 11 }} axisLine={false} tickLine={false} />
                  <YAxis type="category" dataKey="name" tick={{ fill: "#8b92a8", fontSize: 11 }} axisLine={false} tickLine={false} width={110} />
                  <Tooltip content={<CustomTooltipUnits />} />
                  <Bar dataKey="units" radius={[0, 4, 4, 0]} maxBarSize={18}>
                    {top10Vol.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} fillOpacity={0.8 - i * 0.04} />)}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          )}
        </div>

        {/* Per fatturato */}
        <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
          <div className="flex items-center justify-between mb-5">
            <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff]">Top 10 per fatturato</h3>
            <span className="text-[11px] font-semibold px-2 py-0.5 rounded-full bg-[#22d3a0]/10 text-[#22d3a0] border border-[#22d3a0]/20">
              Ricavi
            </span>
          </div>
          {loading ? <SkeletonBar /> : (
            <div className="h-[280px]">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={top10Rev} layout="vertical" margin={{ left: 0, right: 16 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.04)" horizontal={false} />
                  <XAxis type="number" tickFormatter={fmtShort} tick={{ fill: "#4e5566", fontSize: 11 }} axisLine={false} tickLine={false} />
                  <YAxis type="category" dataKey="name" tick={{ fill: "#8b92a8", fontSize: 11 }} axisLine={false} tickLine={false} width={110} />
                  <Tooltip content={<CustomTooltipRev />} />
                  <Bar dataKey="revenue" radius={[0, 4, 4, 0]} maxBarSize={18}>
                    {top10Rev.map((_, i) => <Cell key={i} fill="#22d3a0" fillOpacity={0.85 - i * 0.04} />)}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          )}
        </div>
      </div>

      {/* Tabella completa */}
      <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
        <div className="flex items-center justify-between mb-5">
          <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff]">Tutti i prodotti</h3>
          <span className="text-[11px] text-[#4e5566]">{products.length} prodotti nel periodo</span>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-white/5">
                {["#", "Prodotto", "Unità vendute", "Fatturato", "% sul totale"].map((h, i) => (
                  <th key={i} className={`text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3 ${i > 1 ? "text-right" : "text-left"}`}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-white/[0.04]">
              {loading ? (
                Array.from({ length: 8 }).map((_, i) => (
                  <tr key={i} className="animate-pulse">
                    {[1,2,3,4,5].map((j) => (
                      <td key={j} className="py-3 px-3"><div className="h-3 bg-white/[0.05] rounded w-[70%]" /></td>
                    ))}
                  </tr>
                ))
              ) : products.map((p, i) => (
                <tr key={p.name} className="hover:bg-[#181c23] transition-colors">
                  <td className="py-3 px-3 text-[12px] text-[#4e5566] w-8">{i + 1}</td>
                  <td className="py-3 px-3 text-[13px] font-medium text-[#f0f4ff]">{p.name}</td>
                  <td className="py-3 px-3 text-right font-['Syne'] text-[13px] font-bold text-[#38b6ff]">{p.units}</td>
                  <td className="py-3 px-3 text-right font-['Syne'] text-[13px] font-bold text-[#22d3a0]">{fmtEur(p.revenue)}</td>
                  <td className="py-3 px-3 text-right">
                    <div className="flex items-center justify-end gap-2">
                      <div className="w-[80px] h-1.5 bg-white/[0.05] rounded-full overflow-hidden">
                        <div className="h-full bg-[#a78bfa] rounded-full" style={{ width: `${totalRevenue > 0 ? (p.revenue / totalRevenue) * 100 : 0}%` }} />
                      </div>
                      <span className="text-[12px] text-[#8b92a8] w-[36px] text-right">
                        {totalRevenue > 0 ? ((p.revenue / totalRevenue) * 100).toFixed(1) : 0}%
                      </span>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
