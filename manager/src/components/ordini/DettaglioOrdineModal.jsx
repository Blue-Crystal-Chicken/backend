import StatusBadge from "./StatusBadge";
import { STATUS_LABELS } from "../../hooks/useOrdini";

const fmt = (n) =>
  new Intl.NumberFormat("it-IT", { style: "currency", currency: "EUR" }).format(n ?? 0);

const STATUS_OPTIONS = Object.entries(STATUS_LABELS);

const SERVICE_TYPE_LABELS = { DINE_IN: "Sala", TAKEAWAY: "Asporto", DELIVERY: "Consegna" };

function formatDateTime(iso) {
  if (!iso) return "—";
  return new Date(iso).toLocaleString("it-IT", {
    weekday: "short", day: "2-digit", month: "long",
    hour: "2-digit", minute: "2-digit",
  });
}

export default function DettaglioOrdineModal({ order, locationName, onClose, onUpdateStatus }) {
  if (!order) return null;

  const items  = order.items ?? [];
  const subtot = items.reduce((s, i) => s + (i.price ?? 0) * (i.quantity ?? 1), 0);

  return (
    <div className="fixed inset-0 bg-[#000]/70 backdrop-blur-md z-50 flex items-center justify-center p-4">
      <div className="bg-[#111318] border border-white/10 rounded-[22px] w-full max-w-lg p-6 shadow-2xl flex flex-col max-h-[90vh]">

        {/* Header */}
        <div className="flex items-start justify-between mb-6 border-b border-white/5 pb-4 flex-shrink-0">
          <div>
            <h2 className="font-['Syne'] text-[18px] font-bold text-[#f0f4ff]">
              Ordine #{order.orderId ?? order.id}
            </h2>
            <p className="text-[12px] text-[#8b92a8] mt-0.5">
              {SERVICE_TYPE_LABELS[order.serviceType] ?? order.serviceType}
              {order.tableNumber ? ` · Tavolo ${order.tableNumber}` : ""}
              {" · "}
              {locationName(order.locationId)}
            </p>
            <p className="text-[11px] text-[#4e5566] mt-0.5">{formatDateTime(order.createdAt)}</p>
          </div>
          <button
            onClick={onClose}
            className="text-[#8b92a8] hover:text-[#f0f4ff] transition-colors flex-shrink-0 ml-4"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
              <line x1="18" y1="6" x2="6"  y2="18" />
              <line x1="6"  y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        {/* Aggiornamento stato */}
        <div className="flex items-center gap-3 mb-5 flex-shrink-0">
          <span className="text-[11px] font-semibold uppercase tracking-wider text-[#8b92a8]">
            Stato ordine
          </span>
          <div className="flex items-center gap-2 flex-1">
            <StatusBadge status={order.status} />
            <select
              value={order.status}
              onChange={(e) => onUpdateStatus(order.id, e.target.value)}
              className="ml-auto bg-[#181c23] border border-white/5 rounded-xl px-3 py-1.5 text-[12px] text-[#f0f4ff] focus:outline-none focus:border-[#38b6ff] transition-all cursor-pointer"
            >
              {STATUS_OPTIONS.map(([value, label]) => (
                <option key={value} value={value} className="bg-[#111318]">
                  {label}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Lista prodotti */}
        <div className="flex-1 overflow-y-auto min-h-0">
          <div className="border border-white/5 rounded-[14px] overflow-hidden">
            <div className="grid grid-cols-[1fr_auto_auto] gap-3 px-4 py-2.5 border-b border-white/5">
              <span className="text-[10px] font-semibold tracking-wider uppercase text-[#4e5566]">Prodotto / Offerta</span>
              <span className="text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] text-center">Qtà</span>
              <span className="text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] text-right">Totale</span>
            </div>
            {items.length === 0 ? (
              <div className="px-4 py-6 text-center text-[13px] text-[#4e5566]">
                Nessun articolo registrato.
              </div>
            ) : (
              items.map((item, i) => {
                const name  = item.productName ?? item.offerName ?? "—";
                const total = (item.price ?? 0) * (item.quantity ?? 1);
                return (
                  <div
                    key={i}
                    className="grid grid-cols-[1fr_auto_auto] gap-3 px-4 py-3 items-center border-b border-white/[0.04] last:border-0 hover:bg-[#181c23] transition-colors"
                  >
                    <div>
                      <div className="text-[13px] font-medium text-[#f0f4ff]">{name}</div>
                      {item.specialNote && (
                        <div className="text-[11px] text-[#4e5566] mt-0.5 italic">
                          {item.specialNote}
                        </div>
                      )}
                    </div>
                    <span className="text-[13px] text-[#8b92a8] text-center w-8">
                      ×{item.quantity ?? 1}
                    </span>
                    <span className="font-['Syne'] font-semibold text-[13px] text-[#f0f4ff] text-right">
                      {fmt(total)}
                    </span>
                  </div>
                );
              })
            )}
          </div>
        </div>

        {/* Footer totale */}
        <div className="flex-shrink-0 mt-4 pt-4 border-t border-white/5 space-y-1.5">
          <div className="flex justify-between text-[12px] text-[#8b92a8]">
            <span>Subtotale</span>
            <span>{fmt(subtot)}</span>
          </div>
          <div className="flex justify-between">
            <span className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff]">Totale</span>
            <span className="font-['Syne'] text-[15px] font-bold text-[#22d3a0]">
              {fmt(order.totalAt)}
            </span>
          </div>
          {order.paymentType && (
            <div className="flex justify-between text-[11px] text-[#4e5566] pt-1">
              <span>Pagamento</span>
              <span>{order.paymentType}</span>
            </div>
          )}
        </div>

      </div>
    </div>
  );
}
