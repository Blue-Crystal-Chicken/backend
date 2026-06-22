import {
  LineChart, Line,
  XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
} from "recharts";
import { SEDE_COLORS } from "../../hooks/useFinanze";

const fmtShort = (n) => {
  if (n == null) return "€0";
  if (n >= 1000) return `€${(n / 1000).toFixed(1)}k`;
  return `€${Number(n).toFixed(0)}`;
};

const TrendTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-[#181c23] border border-white/5 rounded-[10px] p-[10px_14px] shadow-2xl min-w-[140px]">
      <div className="text-[11px] text-[#8b92a8] mb-2">{label}</div>
      {payload.map((entry, i) => (
        <div key={i} className="flex items-center gap-2 mb-1 last:mb-0">
          <span className="w-2 h-2 rounded-full flex-shrink-0" style={{ background: entry.color }} />
          <span className="text-[12px] text-[#8b92a8] flex-1 truncate">{entry.name}</span>
          <span className="font-['Syne'] text-[13px] font-bold text-[#f0f4ff]">
            {fmtShort(entry.value)}
          </span>
        </div>
      ))}
    </div>
  );
};

export default function TrendRevenueChart({ trendData, activeIds, locationName, loading }) {
  return (
    <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6 mb-4">

      {/* Header */}
      <div className="flex items-start justify-between mb-5">
        <div>
          <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff]">
            Andamento fatturato
          </h3>
          <p className="text-[12px] text-[#8b92a8] mt-0.5">
            Ricavi giornalieri per sede selezionata
          </p>
        </div>

        {/* Legenda sedi */}
        <div className="flex items-center gap-4 flex-wrap justify-end">
          {activeIds.map((id, i) => (
            <div key={id} className="flex items-center gap-1.5">
              <span className="w-3 h-[2px] rounded-full" style={{ backgroundColor: SEDE_COLORS[i] }} />
              <span className="text-[12px] text-[#8b92a8]">
                {locationName(id === "all" ? null : id)}
              </span>
            </div>
          ))}
        </div>
      </div>

      {/* Chart */}
      <div className="h-[220px]">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={trendData}>
            <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" vertical={false} />
            <XAxis
              dataKey="label"
              tick={{ fill: "#4e5566", fontSize: 11 }}
              axisLine={false}
              tickLine={false}
            />
            <YAxis
              tickFormatter={fmtShort}
              tick={{ fill: "#4e5566", fontSize: 11 }}
              axisLine={false}
              tickLine={false}
              width={48}
            />
            <Tooltip content={<TrendTooltip />} />
            {activeIds.map((id, i) => (
              <Line
                key={id}
                type="monotone"
                dataKey={`loc_${id}`}
                name={locationName(id === "all" ? null : id)}
                stroke={SEDE_COLORS[i]}
                strokeWidth={2.5}
                dot={{ fill: SEDE_COLORS[i], r: 3, strokeWidth: 0 }}
                activeDot={{ r: 5, strokeWidth: 0 }}
              />
            ))}
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
