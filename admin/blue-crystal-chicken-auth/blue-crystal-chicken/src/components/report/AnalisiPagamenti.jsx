import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer, BarChart, Bar, XAxis, YAxis, CartesianGrid } from "recharts";

const fmtEur = (n) => new Intl.NumberFormat("it-IT", { style: "currency", currency: "EUR" }).format(n ?? 0);
const fmtShort = (n) => n >= 1000 ? `€${(n / 1000).toFixed(1)}k` : `€${Number(n).toFixed(0)}`;

const TYPE_COLORS = {
  CASH:   "#fbbf24",
  CARD:   "#38b6ff",
  ONLINE: "#22d3a0",
  OTHER:  "#a78bfa",
};
const TYPE_LABELS = {
  CASH:   "Contante",
  CARD:   "Carta",
  ONLINE: "Online",
  OTHER:  "Altro",
};

function normalizeType(t) {
  if (!t) return "OTHER";
  const up = t.toUpperCase();
  if (up.includes("CASH") || up.includes("CONT")) return "CASH";
  if (up.includes("CARD") || up.includes("CARTA") || up.includes("POS")) return "CARD";
  if (up.includes("ONLINE") || up.includes("DIGITAL")) return "ONLINE";
  return "OTHER";
}

function derivePayments(orders) {
  const map = {};
  let totalDelta = 0;
  let ordersWithDelta = 0;

  orders.forEach((o) => {
    const key = normalizeType(o.paymentType);
    if (!map[key]) map[key] = { type: key, count: 0, revenue: 0, totalTicket: 0 };
    map[key].count++;
    map[key].revenue += o.totalAt ?? 0;
    map[key].totalTicket += o.totalAt ?? 0;

    if (o.paymentAmount != null && o.totalAt != null) {
      totalDelta += o.paymentAmount - o.totalAt;
      ordersWithDelta++;
    }
  });

  const types = Object.values(map).map((t) => ({
    ...t,
    color: TYPE_COLORS[t.type] ?? "#8b92a8",
    label: TYPE_LABELS[t.type] ?? t.type,
    avgTicket: t.count > 0 ? t.totalTicket / t.count : 0,
  })).sort((a, b) => b.revenue - a.revenue);

  return { types, avgDelta: ordersWithDelta > 0 ? totalDelta / ordersWithDelta : 0, ordersWithDelta };
}

const DonutTooltip = ({ active, payload }) => {
  if (!active || !payload?.length) return null;
  const d = payload[0];
  return (
    <div className="bg-[#181c23] border border-white/5 rounded-[10px] p-[8px_12px]">
      <div className="font-['Syne'] text-[13px] font-bold" style={{ color: d.payload.color }}>{d.payload.label}</div>
      <div className="text-[12px] text-[#8b92a8]">{d.value} ordini</div>
      <div className="text-[12px] text-[#f0f4ff]">{fmtEur(d.payload.revenue)}</div>
    </div>
  );
};

const BarTooltip = ({ active, payload }) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-[#181c23] border border-white/5 rounded-[10px] p-[8px_12px]">
      <div className="font-['Syne'] text-[13px] font-bold text-[#f0f4ff]">{payload[0].payload.label}</div>
      <div className="text-[12px] text-[#22d3a0]">{fmtEur(payload[0].value)}</div>
    </div>
  );
};

export default function AnalisiPagamenti({ orders, loading }) {
  const { types, avgDelta, ordersWithDelta } = derivePayments(orders);
  const totalRevenue = types.reduce((s, t) => s + t.revenue, 0);
  const totalOrders  = types.reduce((s, t) => s + t.count, 0);

  return (
    <div className="space-y-4">

      {/* KPI */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {[
          { label: "Metodi pagamento", value: loading ? "—" : types.length, color: "#38b6ff" },
          { label: "Fatturato totale", value: loading ? "—" : fmtShort(totalRevenue), color: "#22d3a0" },
          { label: "Scontrino medio", value: loading ? "—" : fmtEur(totalOrders > 0 ? totalRevenue / totalOrders : 0), color: "#a78bfa" },
          {
            label: "Delta incasso medio",
            value: loading ? "—" : (avgDelta >= 0 ? `+${fmtEur(avgDelta)}` : fmtEur(avgDelta)),
            color: avgDelta >= 0 ? "#22d3a0" : "#ff5e5e",
            sub: `su ${ordersWithDelta} ordini con importo`,
          },
        ].map(({ label, value, color, sub }) => (
          <div key={label} className="bg-[#111318] border border-white/5 rounded-[14px] p-5 relative overflow-hidden">
            <div className="absolute top-0 left-0 right-0 h-[2px]" style={{ backgroundColor: color }} />
            <div className="text-[10px] font-semibold tracking-widest uppercase text-[#4e5566] mb-2">{label}</div>
            <div className="font-['Syne'] text-[24px] font-bold" style={{ color }}>{value}</div>
            {sub && <div className="text-[10px] text-[#4e5566] mt-1">{sub}</div>}
          </div>
        ))}
      </div>

      {/* Grafici */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">

        {/* Donut distribuzione */}
        <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
          <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff] mb-5">Distribuzione metodi</h3>
          {loading ? (
            <div className="h-[220px] flex items-center justify-center">
              <div className="w-[140px] h-[140px] rounded-full bg-white/[0.04] animate-pulse" />
            </div>
          ) : (
            <div className="flex items-center gap-6">
              <div className="w-[160px] h-[160px] flex-shrink-0">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie data={types} dataKey="count" innerRadius={48} outerRadius={70} paddingAngle={3} strokeWidth={0}>
                      {types.map((t) => <Cell key={t.type} fill={t.color} />)}
                    </Pie>
                    <Tooltip content={<DonutTooltip />} />
                  </PieChart>
                </ResponsiveContainer>
              </div>
              <div className="flex flex-col gap-3 flex-1">
                {types.map((t) => (
                  <div key={t.type} className="flex items-center gap-2.5">
                    <span className="w-2.5 h-2.5 rounded-[3px] flex-shrink-0" style={{ background: t.color }} />
                    <span className="text-[13px] text-[#8b92a8] flex-1">{t.label}</span>
                    <span className="font-['Syne'] text-[13px] font-bold text-[#f0f4ff]">{t.count}</span>
                    <span className="text-[11px] text-[#4e5566] w-[38px] text-right">
                      {totalOrders > 0 ? Math.round((t.count / totalOrders) * 100) : 0}%
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Bar scontrino medio per metodo */}
        <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
          <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff] mb-5">Scontrino medio per metodo</h3>
          {loading ? (
            <div className="h-[220px] flex items-end gap-3 animate-pulse">
              {[70, 90, 60, 80].map((h, i) => <div key={i} className="flex-1 bg-white/[0.05] rounded-t-[4px]" style={{ height: `${h}%` }} />)}
            </div>
          ) : (
            <div className="h-[220px]">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={types} margin={{ top: 4, right: 4 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.04)" vertical={false} />
                  <XAxis dataKey="label" tick={{ fill: "#8b92a8", fontSize: 11 }} axisLine={false} tickLine={false} />
                  <YAxis tickFormatter={fmtShort} tick={{ fill: "#4e5566", fontSize: 11 }} axisLine={false} tickLine={false} width={48} />
                  <Tooltip content={<BarTooltip />} />
                  <Bar dataKey="avgTicket" radius={[4, 4, 0, 0]} maxBarSize={48}>
                    {types.map((t) => <Cell key={t.type} fill={t.color} />)}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          )}
        </div>
      </div>

      {/* Tabella dettaglio */}
      <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
        <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff] mb-5">Dettaglio per metodo</h3>
        <table className="w-full">
          <thead>
            <tr className="border-b border-white/5">
              {["Metodo", "Ordini", "Fatturato", "% fatturato", "Scontrino medio"].map((h, i) => (
                <th key={h} className={`text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3 ${i > 0 ? "text-right" : "text-left"}`}>{h}</th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-white/[0.04]">
            {types.map((t) => (
              <tr key={t.type} className="hover:bg-[#181c23] transition-colors">
                <td className="py-3 px-3">
                  <div className="flex items-center gap-2">
                    <span className="w-2.5 h-2.5 rounded-[3px]" style={{ background: t.color }} />
                    <span className="text-[13px] font-medium text-[#f0f4ff]">{t.label}</span>
                  </div>
                </td>
                <td className="py-3 px-3 text-right font-['Syne'] font-bold text-[#38b6ff] text-[13px]">{t.count}</td>
                <td className="py-3 px-3 text-right font-['Syne'] font-bold text-[#22d3a0] text-[13px]">{fmtEur(t.revenue)}</td>
                <td className="py-3 px-3 text-right text-[13px] text-[#8b92a8]">
                  {totalRevenue > 0 ? ((t.revenue / totalRevenue) * 100).toFixed(1) : 0}%
                </td>
                <td className="py-3 px-3 text-right font-['Syne'] font-bold text-[#a78bfa] text-[13px]">{fmtEur(t.avgTicket)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
