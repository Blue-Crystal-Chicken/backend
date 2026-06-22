const STATUS_ORDER = ["PENDING", "PREPARING", "READY", "DELIVERED"];
const STATUS_LABELS = {
  PENDING:   "In attesa",
  PREPARING: "In preparazione",
  READY:     "Pronto",
  DELIVERED: "Consegnato",
  CANCELLED: "Cancellato",
};
const STATUS_COLORS = {
  PENDING:   "#fbbf24",
  PREPARING: "#38b6ff",
  READY:     "#22d3a0",
  DELIVERED: "#a78bfa",
  CANCELLED: "#ff5e5e",
};

function deriveFunnel(orders) {
  const counts = { PENDING: 0, PREPARING: 0, READY: 0, DELIVERED: 0, CANCELLED: 0 };
  orders.forEach((o) => {
    if (counts[o.status] !== undefined) counts[o.status]++;
  });
  return counts;
}

function FunnelStep({ label, count, color, total, isFirst }) {
  const pct = total > 0 ? ((count / total) * 100).toFixed(1) : "0.0";
  const width = total > 0 ? Math.max(20, (count / total) * 100) : 20;

  return (
    <div className="flex items-center gap-4">
      <div className="w-[140px] text-right">
        <span className="text-[12px] font-semibold text-[#8b92a8]">{label}</span>
      </div>
      <div className="flex-1 relative">
        <div
          className="h-[40px] rounded-[8px] flex items-center px-4 transition-all duration-500"
          style={{
            width: `${width}%`,
            backgroundColor: `${color}18`,
            border: `1px solid ${color}40`,
          }}
        >
          <span className="font-['Syne'] text-[15px] font-bold" style={{ color }}>
            {count}
          </span>
        </div>
        {!isFirst && (
          <span className="absolute right-0 top-1/2 -translate-y-1/2 -translate-x-[-16px] text-[11px] text-[#4e5566]">
            {pct}%
          </span>
        )}
      </div>
    </div>
  );
}

function StatCard({ label, value, sub, color }) {
  return (
    <div className="bg-[#111318] border border-white/5 rounded-[14px] p-5 relative overflow-hidden">
      <div className="absolute top-0 left-0 right-0 h-[2px]" style={{ backgroundColor: color }} />
      <div className="text-[10px] font-semibold tracking-widest uppercase text-[#4e5566] mb-2">{label}</div>
      <div className="font-['Syne'] text-[26px] font-bold" style={{ color }}>{value}</div>
      {sub && <div className="text-[11px] text-[#8b92a8] mt-1">{sub}</div>}
    </div>
  );
}

export default function FunnelStati({ orders, loading }) {
  const counts  = deriveFunnel(orders);
  const total   = orders.length;
  const convRate = total > 0 ? ((counts.DELIVERED / total) * 100).toFixed(1) : "0.0";
  const cancelRate = total > 0 ? ((counts.CANCELLED / total) * 100).toFixed(1) : "0.0";
  const avgSteps = total > 0
    ? (
        counts.PENDING * 1 + counts.PREPARING * 2 + counts.READY * 3 + counts.DELIVERED * 4
      ) / (total - counts.CANCELLED || 1)
    : 0;

  return (
    <div className="space-y-6">

      {/* KPI cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard label="Ordini totali" value={loading ? "—" : total} color="#38b6ff" />
        <StatCard
          label="Tasso completamento"
          value={loading ? "—" : `${convRate}%`}
          sub={`${counts.DELIVERED} consegnati`}
          color="#22d3a0"
        />
        <StatCard
          label="Tasso cancellazione"
          value={loading ? "—" : `${cancelRate}%`}
          sub={`${counts.CANCELLED} cancellati`}
          color="#ff5e5e"
        />
        <StatCard
          label="Stato medio"
          value={loading ? "—" : avgSteps.toFixed(2)}
          sub="step medio raggiunto"
          color="#a78bfa"
        />
      </div>

      {/* Funnel principale */}
      <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
        <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff] mb-6">
          Funnel di completamento ordini
        </h3>
        {loading ? (
          <div className="space-y-4 animate-pulse">
            {[100, 80, 60, 40].map((w, i) => (
              <div key={i} className="flex items-center gap-4">
                <div className="w-[140px] h-3 bg-white/[0.05] rounded ml-auto" />
                <div className="h-[40px] bg-white/[0.05] rounded-[8px]" style={{ width: `${w}%` }} />
              </div>
            ))}
          </div>
        ) : (
          <div className="space-y-3">
            {STATUS_ORDER.map((s, i) => (
              <FunnelStep
                key={s}
                label={STATUS_LABELS[s]}
                count={counts[s]}
                color={STATUS_COLORS[s]}
                total={total}
                isFirst={i === 0}
              />
            ))}
          </div>
        )}
      </div>

      {/* CANCELLED separato */}
      <div className="bg-[#ff5e5e]/5 border border-[#ff5e5e]/15 rounded-[18px] p-6">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="font-['Syne'] text-[15px] font-bold text-[#ff5e5e] mb-1">
              Ordini cancellati
            </h3>
            <p className="text-[12px] text-[#8b92a8]">
              Usciti dal funnel prima del completamento
            </p>
          </div>
          <div className="text-right">
            <div className="font-['Syne'] text-[32px] font-bold text-[#ff5e5e]">
              {loading ? "—" : counts.CANCELLED}
            </div>
            <div className="text-[12px] text-[#8b92a8]">{cancelRate}% del totale</div>
          </div>
        </div>
      </div>
    </div>
  );
}
