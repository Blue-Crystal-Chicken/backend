import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell, PieChart, Pie } from "recharts";

const fmtEur  = (n) => new Intl.NumberFormat("it-IT", { style: "currency", currency: "EUR" }).format(n ?? 0);
const fmtShort = (n) => n >= 1000 ? `€${(n / 1000).toFixed(1)}k` : `€${Number(n).toFixed(0)}`;

const SERVICE_COLORS = { DINE_IN: "#38b6ff", TAKEAWAY: "#22d3a0", DELIVERY: "#a78bfa" };
const SERVICE_LABELS = { DINE_IN: "Sala", TAKEAWAY: "Asporto", DELIVERY: "Consegna" };
const TABLE_COLORS   = ["#38b6ff", "#22d3a0", "#a78bfa", "#fbbf24", "#ff5e5e",
                        "#38b6ff", "#22d3a0", "#a78bfa", "#fbbf24", "#ff5e5e"];

function deriveData(orders) {
  const serviceMap = {};
  const tableMap   = {};

  orders.forEach((o) => {
    // Servizio
    const svc = o.serviceType ?? "OTHER";
    if (!serviceMap[svc]) serviceMap[svc] = { type: svc, count: 0, revenue: 0 };
    serviceMap[svc].count++;
    serviceMap[svc].revenue += o.totalAt ?? 0;

    // Tavolo (solo DINE_IN con tableNumber)
    if (o.serviceType === "DINE_IN" && o.tableNumber) {
      const t = `Tavolo ${o.tableNumber}`;
      if (!tableMap[t]) tableMap[t] = { table: t, count: 0, revenue: 0 };
      tableMap[t].count++;
      tableMap[t].revenue += o.totalAt ?? 0;
    }
  });

  const services = Object.values(serviceMap).map((s) => ({
    ...s,
    label: SERVICE_LABELS[s.type] ?? s.type,
    color: SERVICE_COLORS[s.type] ?? "#8b92a8",
    avgTicket: s.count > 0 ? s.revenue / s.count : 0,
  })).sort((a, b) => b.revenue - a.revenue);

  const tables = Object.values(tableMap)
    .sort((a, b) => b.revenue - a.revenue)
    .slice(0, 12);

  return { services, tables };
}

const TableTooltip = ({ active, payload }) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-[#181c23] border border-white/5 rounded-[10px] p-[8px_12px]">
      <div className="font-['Syne'] text-[13px] font-bold text-[#f0f4ff]">{payload[0].payload.table}</div>
      <div className="text-[12px] text-[#22d3a0]">{fmtEur(payload[0].value)}</div>
      <div className="text-[11px] text-[#4e5566]">{payload[0].payload.count} ordini</div>
    </div>
  );
};

const ServiceTooltip = ({ active, payload }) => {
  if (!active || !payload?.length) return null;
  const d = payload[0].payload;
  return (
    <div className="bg-[#181c23] border border-white/5 rounded-[10px] p-[8px_12px]">
      <div className="font-['Syne'] text-[13px] font-bold" style={{ color: d.color }}>{d.label}</div>
      <div className="text-[12px] text-[#f0f4ff]">{d.count} ordini</div>
      <div className="text-[12px] text-[#22d3a0]">{fmtEur(d.revenue)}</div>
    </div>
  );
};

export default function TavoliServizio({ orders, loading }) {
  const { services, tables } = deriveData(orders);
  const totalOrders = orders.length;
  const dineInOrders = orders.filter((o) => o.serviceType === "DINE_IN").length;

  return (
    <div className="space-y-4">

      {/* KPI */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {[
          { label: "Canali attivi", value: loading ? "—" : services.length, color: "#38b6ff" },
          { label: "Tavoli distinti usati", value: loading ? "—" : tables.length, color: "#22d3a0" },
          { label: "Ordini in sala", value: loading ? "—" : dineInOrders, color: "#a78bfa" },
          { label: "% ordini in sala", value: loading ? "—" : `${totalOrders > 0 ? ((dineInOrders / totalOrders) * 100).toFixed(1) : 0}%`, color: "#fbbf24" },
        ].map(({ label, value, color }) => (
          <div key={label} className="bg-[#111318] border border-white/5 rounded-[14px] p-5 relative overflow-hidden">
            <div className="absolute top-0 left-0 right-0 h-[2px]" style={{ backgroundColor: color }} />
            <div className="text-[10px] font-semibold tracking-widest uppercase text-[#4e5566] mb-2">{label}</div>
            <div className="font-['Syne'] text-[24px] font-bold" style={{ color }}>{value}</div>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-[340px_1fr] gap-4">

        {/* Distribuzione canali */}
        <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
          <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff] mb-5">Distribuzione canali</h3>
          {loading ? (
            <div className="flex flex-col items-center gap-4 animate-pulse">
              <div className="w-[160px] h-[160px] rounded-full bg-white/[0.04]" />
            </div>
          ) : (
            <>
              <div className="flex justify-center mb-4">
                <div className="w-[160px] h-[160px]">
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie data={services} dataKey="count" innerRadius={48} outerRadius={70} paddingAngle={3} strokeWidth={0}>
                        {services.map((s) => <Cell key={s.type} fill={s.color} />)}
                      </Pie>
                      <Tooltip content={<ServiceTooltip />} />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
              </div>
              <div className="space-y-3">
                {services.map((s) => (
                  <div key={s.type} className="flex items-center gap-2.5">
                    <span className="w-2.5 h-2.5 rounded-[3px] flex-shrink-0" style={{ background: s.color }} />
                    <span className="text-[13px] text-[#8b92a8] flex-1">{s.label}</span>
                    <span className="font-['Syne'] text-[13px] font-bold text-[#f0f4ff]">{s.count}</span>
                    <span className="text-[11px] text-[#4e5566] w-[38px] text-right">
                      {totalOrders > 0 ? Math.round((s.count / totalOrders) * 100) : 0}%
                    </span>
                  </div>
                ))}
              </div>
            </>
          )}
        </div>

        {/* Tavoli per fatturato */}
        <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
          <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff] mb-5">
            Fatturato per tavolo
            <span className="ml-2 text-[11px] font-normal text-[#4e5566]">solo ordini in sala</span>
          </h3>
          {loading ? (
            <div className="h-[200px] flex items-end gap-2 animate-pulse">
              {[60,85,50,75,90,65,70,55,80,45,95,40].map((h, i) => (
                <div key={i} className="flex-1 bg-white/[0.05] rounded-t-[4px]" style={{ height: `${h}%` }} />
              ))}
            </div>
          ) : tables.length === 0 ? (
            <div className="flex items-center justify-center h-[200px] text-[13px] text-[#4e5566]">
              Nessun ordine in sala con numero tavolo nel periodo.
            </div>
          ) : (
            <div className="h-[200px]">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={tables} margin={{ top: 4, right: 4 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.04)" vertical={false} />
                  <XAxis dataKey="table" tick={{ fill: "#8b92a8", fontSize: 10 }} axisLine={false} tickLine={false} />
                  <YAxis tickFormatter={fmtShort} tick={{ fill: "#4e5566", fontSize: 11 }} axisLine={false} tickLine={false} width={48} />
                  <Tooltip content={<TableTooltip />} />
                  <Bar dataKey="revenue" radius={[4, 4, 0, 0]} maxBarSize={36}>
                    {tables.map((_, i) => <Cell key={i} fill={TABLE_COLORS[i % TABLE_COLORS.length]} fillOpacity={0.8} />)}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          )}
        </div>
      </div>

      {/* Tabella servizi */}
      <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
        <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff] mb-5">Dettaglio per canale</h3>
        <table className="w-full">
          <thead>
            <tr className="border-b border-white/5">
              {["Canale", "Ordini", "Fatturato", "Scontrino medio", "% ordini"].map((h, i) => (
                <th key={h} className={`text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3 ${i > 0 ? "text-right" : "text-left"}`}>{h}</th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-white/[0.04]">
            {loading ? (
              Array.from({ length: 3 }).map((_, i) => (
                <tr key={i} className="animate-pulse">
                  {[1,2,3,4,5].map((j) => <td key={j} className="py-3 px-3"><div className="h-3 bg-white/[0.05] rounded" /></td>)}
                </tr>
              ))
            ) : services.map((s) => (
              <tr key={s.type} className="hover:bg-[#181c23] transition-colors">
                <td className="py-3 px-3">
                  <div className="flex items-center gap-2">
                    <span className="w-2.5 h-2.5 rounded-[3px]" style={{ background: s.color }} />
                    <span className="text-[13px] font-medium text-[#f0f4ff]">{s.label}</span>
                  </div>
                </td>
                <td className="py-3 px-3 text-right font-['Syne'] font-bold text-[#38b6ff] text-[13px]">{s.count}</td>
                <td className="py-3 px-3 text-right font-['Syne'] font-bold text-[#22d3a0] text-[13px]">{fmtEur(s.revenue)}</td>
                <td className="py-3 px-3 text-right font-['Syne'] font-bold text-[#a78bfa] text-[13px]">{fmtEur(s.avgTicket)}</td>
                <td className="py-3 px-3 text-right text-[12px] text-[#8b92a8]">
                  {totalOrders > 0 ? ((s.count / totalOrders) * 100).toFixed(1) : 0}%
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
