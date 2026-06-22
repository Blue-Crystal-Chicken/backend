import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from "recharts";

const fmtEur  = (n) => new Intl.NumberFormat("it-IT", { style: "currency", currency: "EUR" }).format(n ?? 0);
const fmtShort = (n) => n >= 1000 ? `€${(n / 1000).toFixed(1)}k` : `€${Number(n).toFixed(0)}`;

const RANK_COLORS = ["#fbbf24", "#8b92a8", "#a78bfa", "#38b6ff", "#22d3a0"];

function deriveStaff(orders) {
  const map = {};
  orders.forEach((o) => {
    if (!o.userId || !o.userName) return;
    const key = o.userId;
    if (!map[key]) map[key] = { id: key, name: o.userName, orders: 0, revenue: 0 };
    map[key].orders++;
    map[key].revenue += o.totalAt ?? 0;
  });
  return Object.values(map)
    .map((s) => ({ ...s, avgTicket: s.orders > 0 ? s.revenue / s.orders : 0 }))
    .sort((a, b) => b.revenue - a.revenue);
}

const BarTooltip = ({ active, payload }) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-[#181c23] border border-white/5 rounded-[10px] p-[8px_12px]">
      <div className="font-['Syne'] text-[13px] font-bold text-[#f0f4ff]">{payload[0].payload.name}</div>
      <div className="text-[12px] text-[#22d3a0]">{fmtEur(payload[0].value)}</div>
      <div className="text-[11px] text-[#4e5566]">{payload[0].payload.orders} ordini</div>
    </div>
  );
};

function Medal({ rank }) {
  const colors = ["#fbbf24", "#8b92a8", "#a78bfa"];
  if (rank >= 3) return <span className="text-[11px] text-[#4e5566] font-bold w-5 text-center">{rank + 1}</span>;
  return (
    <span className="w-5 h-5 rounded-full flex items-center justify-center text-[10px] font-bold text-[#111318]"
          style={{ backgroundColor: colors[rank] }}>
      {rank + 1}
    </span>
  );
}

export default function StaffLeaderboard({ orders, loading }) {
  const staff    = deriveStaff(orders);
  const top5     = staff.slice(0, 5);
  const hasStaff = staff.length > 0;
  const totalRev = staff.reduce((s, op) => s + op.revenue, 0);

  const noUserOrders = orders.filter((o) => !o.userId).length;

  return (
    <div className="space-y-4">

      {/* KPI */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {[
          { label: "Operatori attivi", value: loading ? "—" : staff.length, color: "#38b6ff" },
          { label: "Top performer", value: loading || !hasStaff ? "—" : staff[0].name.split(" ")[0], color: "#fbbf24" },
          { label: "Max fatturato singolo", value: loading || !hasStaff ? "—" : fmtShort(staff[0]?.revenue), color: "#22d3a0" },
          { label: "Ordini senza operatore", value: loading ? "—" : noUserOrders, color: "#4e5566", sub: "ordini anonimi" },
        ].map(({ label, value, color, sub }) => (
          <div key={label} className="bg-[#111318] border border-white/5 rounded-[14px] p-5 relative overflow-hidden">
            <div className="absolute top-0 left-0 right-0 h-[2px]" style={{ backgroundColor: color }} />
            <div className="text-[10px] font-semibold tracking-widest uppercase text-[#4e5566] mb-2">{label}</div>
            <div className="font-['Syne'] text-[24px] font-bold truncate" style={{ color }}>{value}</div>
            {sub && <div className="text-[10px] text-[#4e5566] mt-1">{sub}</div>}
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-[1fr_380px] gap-4">

        {/* Bar chart top 5 */}
        <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
          <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff] mb-5">Top 5 per fatturato generato</h3>
          {loading ? (
            <div className="space-y-3 animate-pulse">
              {Array.from({ length: 5 }).map((_, i) => (
                <div key={i} className="flex gap-3">
                  <div className="h-3 w-[25%] bg-white/[0.05] rounded" />
                  <div className="h-7 flex-1 bg-white/[0.05] rounded" />
                </div>
              ))}
            </div>
          ) : !hasStaff ? (
            <div className="flex items-center justify-center h-[200px] text-[13px] text-[#4e5566]">
              Nessun operatore tracciato nel periodo.
            </div>
          ) : (
            <div className="h-[200px]">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={top5} layout="vertical" margin={{ left: 0, right: 16 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.04)" horizontal={false} />
                  <XAxis type="number" tickFormatter={fmtShort} tick={{ fill: "#4e5566", fontSize: 11 }} axisLine={false} tickLine={false} />
                  <YAxis type="category" dataKey="name" tick={{ fill: "#8b92a8", fontSize: 11 }} axisLine={false} tickLine={false} width={100} />
                  <Tooltip content={<BarTooltip />} />
                  <Bar dataKey="revenue" radius={[0, 4, 4, 0]} maxBarSize={20}>
                    {top5.map((_, i) => <Cell key={i} fill={RANK_COLORS[i] ?? "#38b6ff"} />)}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          )}
        </div>

        {/* Podio top 3 */}
        <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
          <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff] mb-5">Podio</h3>
          {loading ? (
            <div className="space-y-4 animate-pulse">
              {[1,2,3].map((i) => (
                <div key={i} className="flex items-center gap-3">
                  <div className="w-5 h-5 rounded-full bg-white/[0.06]" />
                  <div className="h-4 flex-1 bg-white/[0.05] rounded" />
                  <div className="h-4 w-16 bg-white/[0.05] rounded" />
                </div>
              ))}
            </div>
          ) : !hasStaff ? (
            <div className="flex items-center justify-center h-[120px] text-[13px] text-[#4e5566]">
              Nessun dato disponibile.
            </div>
          ) : (
            <div className="space-y-3">
              {staff.slice(0, 5).map((op, i) => (
                <div
                  key={op.id}
                  className={`flex items-center gap-3 p-3 rounded-[10px] ${i === 0 ? "bg-[#fbbf24]/8 border border-[#fbbf24]/15" : "hover:bg-[#181c23]"} transition-colors`}
                >
                  <Medal rank={i} />
                  <div className="flex-1 min-w-0">
                    <div className="text-[13px] font-semibold text-[#f0f4ff] truncate">{op.name}</div>
                    <div className="text-[11px] text-[#4e5566]">{op.orders} ordini</div>
                  </div>
                  <div className="text-right flex-shrink-0">
                    <div className="font-['Syne'] text-[13px] font-bold text-[#22d3a0]">{fmtEur(op.revenue)}</div>
                    <div className="text-[10px] text-[#4e5566]">
                      {totalRev > 0 ? ((op.revenue / totalRev) * 100).toFixed(1) : 0}% del tot.
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Tabella completa */}
      {!loading && staff.length > 0 && (
        <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
          <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff] mb-5">Tutti gli operatori</h3>
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-white/5">
                  {["#", "Nome", "Ordini gestiti", "Fatturato", "Scontrino medio", "% sul totale"].map((h, i) => (
                    <th key={h} className={`text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3 ${i > 1 ? "text-right" : "text-left"}`}>{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-white/[0.04]">
                {staff.map((op, i) => (
                  <tr key={op.id} className="hover:bg-[#181c23] transition-colors">
                    <td className="py-3 px-3 w-8"><Medal rank={i} /></td>
                    <td className="py-3 px-3 text-[13px] font-medium text-[#f0f4ff]">{op.name}</td>
                    <td className="py-3 px-3 text-right font-['Syne'] font-bold text-[#38b6ff] text-[13px]">{op.orders}</td>
                    <td className="py-3 px-3 text-right font-['Syne'] font-bold text-[#22d3a0] text-[13px]">{fmtEur(op.revenue)}</td>
                    <td className="py-3 px-3 text-right font-['Syne'] font-bold text-[#a78bfa] text-[13px]">{fmtEur(op.avgTicket)}</td>
                    <td className="py-3 px-3 text-right text-[12px] text-[#8b92a8]">
                      {totalRev > 0 ? ((op.revenue / totalRev) * 100).toFixed(1) : 0}%
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
