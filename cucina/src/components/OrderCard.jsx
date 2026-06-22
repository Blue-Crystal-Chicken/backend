import {
  RESPONSE_FIELDS as F,
  ACTIONS,
  SERVICE_TYPE_LABELS,
  WAIT_THRESHOLDS,
} from "../config";
import {
  itemCategory,
  categoryColor,
  categoryLabel,
  orderCategories,
} from "../lib/categories";

function minutesSince(iso) {
  if (!iso) return 0;
  return Math.max(0, Math.floor((Date.now() - new Date(iso).getTime()) / 60000));
}
function waitColor(min) {
  if (min >= WAIT_THRESHOLDS.danger) return "#ff5e5e";
  if (min >= WAIT_THRESHOLDS.warning) return "#fbbf24";
  return "#8b92a8";
}

export default function OrderCard({ order, categoryMap, busy, onAct }) {
  const items = order[F.items] || [];
  const code = order[F.code] || order[F.orderId] || `#${order[F.id]}`;
  const table = order[F.tableNumber];
  const serviceType = order[F.serviceType];
  const min = minutesSince(order[F.createdAt]);
  const wc = waitColor(min);

  // categorie distinte -> bordo sinistro segmentato (colpo d'occhio)
  const cats = orderCategories(items, categoryMap);

  return (
    <div
      className={`relative bg-[#111318] border border-white/5 rounded-[18px] overflow-hidden transition-all duration-200 ${
        busy ? "opacity-40 pointer-events-none scale-[0.98]" : ""
      }`}
    >
      {/* Bordo sinistro segmentato per categoria */}
      <div className="absolute left-0 top-0 bottom-0 w-[7px] flex flex-col">
        {cats.map((c) => (
          <div
            key={c}
            className="flex-1"
            style={{ backgroundColor: categoryColor(c) }}
          />
        ))}
      </div>

      <div className="pl-[20px] pr-[18px] py-[16px]">
        {/* Header: numero ordine + attesa */}
        <div className="flex items-start justify-between mb-2.5">
          <div>
            <div className="font-['Syne'] text-[26px] font-extrabold leading-none text-[#f0f4ff]">
              {code}
            </div>
            <div className="flex items-center gap-2 mt-2">
              <span className="text-[10px] font-bold px-2 py-0.5 rounded-full tracking-wider uppercase bg-[#181c23] border border-white/5 text-[#8b92a8]">
                {SERVICE_TYPE_LABELS[serviceType] ?? serviceType ?? "—"}
              </span>
              {table && (
                <span className="text-[10px] font-bold px-2 py-0.5 rounded-full tracking-wider uppercase bg-[#181c23] border border-white/5 text-[#8b92a8]">
                  Tavolo {table}
                </span>
              )}
            </div>
          </div>
          <div
            className="flex items-center gap-1.5 text-[15px] font-bold font-['Syne'] tabular-nums"
            style={{ color: wc }}
            title="Minuti di attesa"
          >
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <circle cx="12" cy="12" r="9" />
              <polyline points="12 7 12 12 16 14" />
            </svg>
            {min}′
          </div>
        </div>

        {/* Articoli da preparare — ognuno col suo colore di categoria */}
        <ul className="space-y-1.5 mb-4 mt-3">
          {items.length === 0 && (
            <li className="text-[12px] text-[#4e5566] italic">Nessun articolo</li>
          )}
          {items.map((it, i) => {
            const cat = itemCategory(it, categoryMap);
            const color = categoryColor(cat);
            const name =
              it[F.itemOfferName] || it[F.itemProductName] || "Articolo";
            const qty = it[F.itemQuantity] ?? 1;
            const note = it[F.itemNote];
            return (
              <li
                key={i}
                className="flex items-start gap-2.5 rounded-lg px-2.5 py-2"
                style={{ backgroundColor: `${color}12`, borderLeft: `3px solid ${color}` }}
              >
                <span
                  className="font-['Syne'] font-extrabold text-[18px] leading-none min-w-[34px]"
                  style={{ color }}
                >
                  {qty}×
                </span>
                <div className="min-w-0 flex-1">
                  <div className="text-[15px] font-semibold text-[#f0f4ff] leading-tight">
                    {name}
                  </div>
                  <div className="flex items-center gap-2 mt-1">
                    <span
                      className="text-[9px] font-bold px-1.5 py-0.5 rounded tracking-wider uppercase"
                      style={{ backgroundColor: `${color}20`, color }}
                    >
                      {categoryLabel(cat)}
                    </span>
                    {note && (
                      <span className="text-[11px] text-[#fbbf24] italic truncate">
                        ⚠ {note}
                      </span>
                    )}
                  </div>
                </div>
              </li>
            );
          })}
        </ul>

        {/* Due bottoni rapidi — un click */}
        <div className="grid grid-cols-2 gap-2.5">
          <button
            onClick={() => onAct(order[F.id], ACTIONS.cancel.status)}
            className="flex items-center justify-center gap-2 rounded-xl py-3 text-[14px] font-bold tracking-wide transition-all active:scale-95"
            style={{
              backgroundColor: `${ACTIONS.cancel.color}18`,
              color: ACTIONS.cancel.color,
              border: `1px solid ${ACTIONS.cancel.color}40`,
            }}
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
            {ACTIONS.cancel.label}
          </button>
          <button
            onClick={() => onAct(order[F.id], ACTIONS.done.status)}
            className="flex items-center justify-center gap-2 rounded-xl py-3 text-[14px] font-extrabold tracking-wide transition-all active:scale-95"
            style={{ backgroundColor: ACTIONS.done.color, color: "#0b0c10" }}
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3.5" strokeLinecap="round" strokeLinejoin="round">
              <polyline points="20 6 9 17 4 12" />
            </svg>
            {ACTIONS.done.label}
          </button>
        </div>
      </div>
    </div>
  );
}
