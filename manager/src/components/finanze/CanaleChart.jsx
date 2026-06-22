import {
  LineChart, Line,
  XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
} from "recharts";
import { CANALE_COLORS, CANALE_LABELS } from "../../hooks/useFinanze";

const fmtShort = (n) => {
  if (n == null) return "€0";
  if (n >= 1000) return `€${(n / 1000).toFixed(1)}k`;
  return `€${Number(n).toFixed(0)}`;
};

const CanaleTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-[#181c23] border border-white/5 rounded-[10px] p-[10px_14px] shadow-2xl min-w-[150px]">
      <div className="text-[11px] text-[#8b92a8] mb-2">{label}</div>
      {payload.map((entry, i) => (
        <div key={i} className="flex items-center gap-2 mb-1 last:mb-0">
          <span className="w-2 h-2 rounded-full flex-shrink-0" style={{ background: entry.color }} />
          <span className="text-[12px] text-[#8b92a8] flex-1">{CANALE_LABELS[entry.dataKey] ?? entry.dataKey}</span>
          <span className="font-['Syne'] text-[13px] font-bold text-[#f0f4ff]">
            {fmtShort(entry.value)}
          </span>
        </div>
      ))}
    </div>
  );
};

const CANALI = ["DINE_IN", "TAKEAWAY", "DELIVERY"];

export default function CanaleChart({ canaleData, locationName, primaryId, loading }) {
  return (
    <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">

      {/* Header */}
      <div className="flex items-start justify-between mb-5">
        <div>
          <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff]">
            Fatturato per canale
          </h3>
          <p className="text-[12px] text-[#8b92a8] mt-0.5">
            {locationName(primaryId)}
          </p>
        </div>

        {/* Legenda canali */}
        <div className="flex items-center gap-3 flex-wrap justify-end">
          {CANALI.map((c) => (
            <div key={c} className="flex items-center gap-1.5">
              <span className="w-3 h-[2px] rounded-full" style={{ backgroundColor: CANALE_COLORS[c] }} />
              <span className="text-[12px] text-[#8b92a8]">{CANALE_LABELS[c]}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Chart */}
      <div className="h-[200px]">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={canaleData}>
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
            <Tooltip content={<CanaleTooltip />} />
            {CANALI.map((c) => (
              <Line
                key={c}
                type="monotone"
                dataKey={c}
                stroke={CANALE_COLORS[c]}
                strokeWidth={2}
                dot={{ fill: CANALE_COLORS[c], r: 2.5, strokeWidth: 0 }}
                activeDot={{ r: 4.5, strokeWidth: 0 }}
              />
            ))}
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
