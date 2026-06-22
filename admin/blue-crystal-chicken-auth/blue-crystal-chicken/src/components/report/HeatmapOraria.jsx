const GIORNI = ["Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom"];
const HOURS  = Array.from({ length: 24 }, (_, i) => i);

function buildHeatmap(orders) {
  // grid[weekday 0=Mon][hour] = count
  const grid = Array.from({ length: 7 }, () => Array(24).fill(0));
  orders.forEach((o) => {
    const d   = new Date(o.createdAt);
    const wd  = (d.getDay() + 6) % 7; // 0=Mon
    const hr  = d.getHours();
    grid[wd][hr]++;
  });
  return grid;
}

function buildHourlyTotals(orders) {
  const arr = Array(24).fill(0);
  orders.forEach((o) => { arr[new Date(o.createdAt).getHours()]++; });
  return arr;
}

function buildWeekdayTotals(orders) {
  const arr = Array(7).fill(0);
  orders.forEach((o) => { arr[(new Date(o.createdAt).getDay() + 6) % 7]++; });
  return arr;
}

function intensityColor(value, max) {
  if (max === 0 || value === 0) return "rgba(255,255,255,0.03)";
  const ratio = value / max;
  if (ratio < 0.25) return `rgba(56,182,255,${0.12 + ratio * 0.4})`;
  if (ratio < 0.5)  return `rgba(56,182,255,${0.25 + ratio * 0.4})`;
  if (ratio < 0.75) return `rgba(34,211,160,${0.4 + ratio * 0.3})`;
  return `rgba(34,211,160,${0.65 + ratio * 0.25})`;
}

function textColor(value, max) {
  if (max === 0 || value === 0) return "#4e5566";
  return value / max > 0.4 ? "#f0f4ff" : "#8b92a8";
}

export default function HeatmapOraria({ orders, loading }) {
  const grid          = buildHeatmap(orders);
  const hourlyTotals  = buildHourlyTotals(orders);
  const weekdayTotals = buildWeekdayTotals(orders);
  const max           = Math.max(...grid.flatMap((row) => row));
  const maxHour       = hourlyTotals.indexOf(Math.max(...hourlyTotals));
  const maxDay        = GIORNI[weekdayTotals.indexOf(Math.max(...weekdayTotals))];

  return (
    <div className="space-y-4">

      {/* Stat rapide */}
      <div className="grid grid-cols-3 gap-4">
        {[
          { label: "Ora di punta", value: loading ? "—" : `${String(maxHour).padStart(2,"0")}:00`, color: "#38b6ff" },
          { label: "Giorno di punta", value: loading ? "—" : maxDay, color: "#22d3a0" },
          { label: "Ordini totali", value: loading ? "—" : orders.length, color: "#a78bfa" },
        ].map(({ label, value, color }) => (
          <div key={label} className="bg-[#111318] border border-white/5 rounded-[14px] p-5 relative overflow-hidden">
            <div className="absolute top-0 left-0 right-0 h-[2px]" style={{ backgroundColor: color }} />
            <div className="text-[10px] font-semibold tracking-widest uppercase text-[#4e5566] mb-2">{label}</div>
            <div className="font-['Syne'] text-[26px] font-bold" style={{ color }}>{value}</div>
          </div>
        ))}
      </div>

      {/* Heatmap */}
      <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
        <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff] mb-5">
          Intensità per giorno × ora
        </h3>

        {loading ? (
          <div className="animate-pulse space-y-2">
            {Array.from({ length: 7 }).map((_, r) => (
              <div key={r} className="flex gap-1">
                <div className="w-8 h-7 bg-white/[0.04] rounded" />
                {Array.from({ length: 24 }).map((_, c) => (
                  <div key={c} className="flex-1 h-7 bg-white/[0.03] rounded" />
                ))}
              </div>
            ))}
          </div>
        ) : (
          <>
            {/* Header ore */}
            <div className="flex gap-1 mb-1 ml-9">
              {HOURS.map((h) => (
                <div key={h} className="flex-1 text-center text-[9px] text-[#4e5566]">
                  {h % 4 === 0 ? String(h).padStart(2, "0") : ""}
                </div>
              ))}
            </div>

            {/* Righe giorni */}
            {GIORNI.map((g, gi) => (
              <div key={g} className="flex items-center gap-1 mb-1">
                <div className="w-8 text-right text-[11px] text-[#4e5566] pr-1 flex-shrink-0">{g}</div>
                {HOURS.map((h) => {
                  const v = grid[gi][h];
                  return (
                    <div
                      key={h}
                      title={`${g} ${String(h).padStart(2,"0")}:00 — ${v} ordini`}
                      className="flex-1 h-7 rounded-[3px] flex items-center justify-center text-[9px] font-bold cursor-default transition-all hover:scale-110"
                      style={{
                        backgroundColor: intensityColor(v, max),
                        color: textColor(v, max),
                      }}
                    >
                      {v > 0 ? v : ""}
                    </div>
                  );
                })}
              </div>
            ))}

            {/* Legenda */}
            <div className="flex items-center justify-end gap-3 mt-4">
              <span className="text-[10px] text-[#4e5566]">Bassa</span>
              {[0.1, 0.3, 0.5, 0.7, 0.9].map((r) => (
                <div
                  key={r}
                  className="w-5 h-4 rounded-[3px]"
                  style={{ backgroundColor: intensityColor(Math.round(r * 10), 10) }}
                />
              ))}
              <span className="text-[10px] text-[#4e5566]">Alta</span>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
