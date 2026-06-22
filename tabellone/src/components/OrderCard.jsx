import {
  RESPONSE_FIELDS as F,
  STATUS_LABELS,
  STATUS_COLORS,
  SERVICE_TYPE_LABELS,
  SERVICE_TYPE_COLORS,
  WAIT_THRESHOLDS,
} from "../config";

// minuti trascorsi da createdAt
function minutesSince(iso) {
  if (!iso) return 0;
  const diff = (Date.now() - new Date(iso).getTime()) / 60000;
  return Math.max(0, Math.floor(diff));
}

function waitColor(min) {
  if (min >= WAIT_THRESHOLDS.danger) return "#ff5e5e";
  if (min >= WAIT_THRESHOLDS.warning) return "#fbbf24";
  return "#8b92a8";
}

export default function OrderCard({ order, column, onAdvance }) {
  const status = order[F.status];
  const statusColor = STATUS_COLORS[status] ?? "#8b92a8";
  const serviceType = order[F.serviceType];
  const svcColor = SERVICE_TYPE_COLORS[serviceType] ?? "#8b92a8";
  const items = order[F.items] || [];
  const code = order[F.code] || order[F.orderId] || `#${order[F.id]}`;
  const table = order[F.tableNumber];
  const min = minutesSince(order[F.createdAt]);
  const wc = waitColor(min);

  return (
    <div className="bg-[#111318] border border-white/5 rounded-[18px] p-[18px_20px] relative overflow-hidden transition-all duration-200 hover:border-white/10">
      {/* Linea accento di colonna */}
      <div
        className="absolute top-0 left-0 right-0 h-[3px] opacity-80"
        style={{ backgroundColor: column.accent }}
      />

      {/* Header card: codice ordine + tempo di attesa */}
      <div className="flex items-start justify-between mb-3">
        <div>
          <div className="font-['Syne'] text-[22px] font-extrabold leading-none text-[#f0f4ff]">
            {code}
          </div>
          {table && (
            <div className="text-[11px] font-semibold tracking-[1px] uppercase text-[#8b92a8] mt-1.5">
              Tavolo {table}
            </div>
          )}
        </div>
        <div
          className="flex items-center gap-1.5 text-[13px] font-bold font-['Syne']"
          style={{ color: wc }}
          title="Minuti di attesa"
        >
          <svg
            width="14"
            height="14"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2.5"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <circle cx="12" cy="12" r="9" />
            <polyline points="12 7 12 12 16 14" />
          </svg>
          {min}′
        </div>
      </div>

      {/* Badge servizio + stato */}
      <div className="flex items-center gap-2 mb-3 flex-wrap">
        <span
          className="text-[10px] font-bold px-2 py-0.5 rounded-full tracking-wider uppercase"
          style={{
            backgroundColor: `${svcColor}18`,
            color: svcColor,
            border: `1px solid ${svcColor}40`,
          }}
        >
          {SERVICE_TYPE_LABELS[serviceType] ?? serviceType ?? "—"}
        </span>
        <span
          className="inline-flex items-center gap-1.5 text-[10px] font-bold px-2 py-0.5 rounded-full tracking-wider uppercase"
          style={{
            backgroundColor: `${statusColor}18`,
            color: statusColor,
            border: `1px solid ${statusColor}40`,
          }}
        >
          <span
            className="w-1.5 h-1.5 rounded-full"
            style={{ backgroundColor: statusColor }}
          />
          {STATUS_LABELS[status] ?? status}
        </span>
      </div>

      {/* Lista prodotti */}
      <ul className="space-y-1 mb-4">
        {items.length === 0 && (
          <li className="text-[12px] text-[#4e5566] italic">Nessun articolo</li>
        )}
        {items.map((it, i) => {
          const name = it[F.itemName] || it[F.itemOfferName] || "Articolo";
          const qty = it[F.itemQuantity] ?? 1;
          return (
            <li
              key={i}
              className="flex items-center gap-2 text-[13px] text-[#f0f4ff]"
            >
              <span
                className="font-['Syne'] font-bold text-[#38b6ff] min-w-[26px]"
              >
                {qty}×
              </span>
              <span className="truncate">{name}</span>
            </li>
          );
        })}
      </ul>

      {/* Azione: avanza stato */}
      {column.advanceTo && (
        <button
          onClick={() => onAdvance(order[F.id], column.advanceTo)}
          className="w-full flex items-center justify-center gap-2 rounded-xl px-4 py-2.5 text-[12px] font-bold tracking-wide transition-all"
          style={{
            backgroundColor: column.accent,
            color: "#0b0c10",
          }}
        >
          <svg
            width="14"
            height="14"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="3"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <polyline points="20 6 9 17 4 12" />
          </svg>
          {column.advanceLabel}
        </button>
      )}
    </div>
  );
}
