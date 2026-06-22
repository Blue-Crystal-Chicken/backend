import {
  AreaChart, Area,
  PieChart, Pie, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer,
} from "recharts";

const fmtShort = (n) => {
  if (n == null) return "€0";
  if (n >= 1000) return `€${(n / 1000).toFixed(1)}k`;
  return `€${Number(n).toFixed(0)}`;
};

const ChartTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-[#181c23] border border-white/5 rounded-[10px] p-[10px_14px] shadow-2xl">
      <div className="text-[11px] text-[#8b92a8] mb-1">{label}</div>
      <div className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff]">
        {payload[0]?.name === "count"
          ? `${payload[0].value} ordini`
          : fmtShort(payload[0]?.value)}
      </div>
    </div>
  );
};

export default function TrendOrdini({ dailyData, serviceTypeData, loading }) {
  const total = serviceTypeData.reduce((s, d) => s + d.count, 0);

  return (
    <div className="grid grid-cols-1 lg:grid-cols-[1fr_340px] gap-4 mb-4">

      {/* Area chart — ordini per giorno */}
      <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
        <div className="flex items-center justify-between mb-5">
          <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff]">
            Andamento ordini
          </h3>
          <span className="text-[11px] font-semibold px-2 py-0.5 rounded-full bg-[#38b6ff]/10 text-[#38b6ff] border border-[#38b6ff]/20">
            Volumi
          </span>
        </div>
        <div className="h-[180px]">
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={dailyData}>
              <defs>
                <linearGradient id="gradOrdini" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%"  stopColor="#38b6ff" stopOpacity={0.25} />
                  <stop offset="95%" stopColor="#38b6ff" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" vertical={false} />
              <XAxis
                dataKey="label"
                tick={{ fill: "#4e5566", fontSize: 11 }}
                axisLine={false}
                tickLine={false}
              />
              <YAxis
                allowDecimals={false}
                tick={{ fill: "#4e5566", fontSize: 11 }}
                axisLine={false}
                tickLine={false}
                width={28}
              />
              <Tooltip content={<ChartTooltip />} />
              <Area
                type="monotone"
                dataKey="count"
                name="count"
                stroke="#38b6ff"
                strokeWidth={2.5}
                fill="url(#gradOrdini)"
                dot={{ fill: "#38b6ff", r: 3 }}
                activeDot={{ r: 5 }}
              />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Donut — distribuzione tipo servizio */}
      <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
        <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff] mb-5">
          Tipo di servizio
        </h3>
        {serviceTypeData.length === 0 ? (
          <div className="flex items-center justify-center h-[140px] text-[13px] text-[#4e5566]">
            Nessun ordine nel periodo.
          </div>
        ) : (
          <div className="flex items-center gap-4">
            <div className="w-[140px] h-[140px] flex-shrink-0">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={serviceTypeData}
                    dataKey="count"
                    innerRadius={42}
                    outerRadius={62}
                    paddingAngle={3}
                    strokeWidth={0}
                  >
                    {serviceTypeData.map((entry, i) => (
                      <Cell key={i} fill={entry.color} />
                    ))}
                  </Pie>
                </PieChart>
              </ResponsiveContainer>
            </div>
            <div className="flex flex-col gap-3 flex-1">
              {serviceTypeData.map((d, i) => (
                <div key={i} className="flex items-center gap-2.5">
                  <span className="w-2.5 h-2.5 rounded-[3px] flex-shrink-0" style={{ background: d.color }} />
                  <span className="text-[13px] text-[#8b92a8] flex-1">{d.label}</span>
                  <span className="text-[13px] font-bold font-['Syne'] text-[#f0f4ff]">
                    {total > 0 ? Math.round((d.count / total) * 100) : 0}%
                  </span>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>

    </div>
  );
}
