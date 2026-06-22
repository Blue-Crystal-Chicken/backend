import { offerStatus, STATUS_META } from "../../hooks/useMarketing";

const fmtCur = (n) => new Intl.NumberFormat("it-IT", { style: "currency", currency: "EUR" }).format(n ?? 0);
const fmtDate = (d) => (d ? new Date(d).toLocaleDateString("it-IT", { day: "2-digit", month: "short" }) : "—");

const Icon = {
  edit: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...p}><path d="M11 5H6a2 2 0 0 0-2 2v11a2 2 0 0 0 2 2h11a2 2 0 0 0 2-2v-5" /><path d="M18.5 2.5a2.121 2.121 0 1 1 3 3L12 15l-4 1 1-4 9.5-9.5z" /></svg>),
  trash: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...p}><polyline points="3 6 5 6 21 6" /><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" /></svg>),
  box: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...p}><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z" /><polyline points="3.27 6.96 12 12.01 20.73 6.96" /><line x1="12" y1="22.08" x2="12" y2="12" /></svg>),
  menu: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" {...p}><line x1="8" y1="6" x2="21" y2="6" /><line x1="8" y1="12" x2="21" y2="12" /><line x1="8" y1="18" x2="21" y2="18" /><line x1="3" y1="6" x2="3.01" y2="6" /><line x1="3" y1="12" x2="3.01" y2="12" /><line x1="3" y1="18" x2="3.01" y2="18" /></svg>),
  calendar: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" {...p}><rect x="3" y="4" width="18" height="18" rx="2" ry="2" /><line x1="16" y1="2" x2="16" y2="6" /><line x1="8" y1="2" x2="8" y2="6" /><line x1="3" y1="10" x2="21" y2="10" /></svg>),
};

const StatusBadge = ({ status }) => {
  const meta = STATUS_META[status] ?? STATUS_META.SCADUTA;
  return (
    <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-semibold border" style={{ color: meta.color, background: `${meta.color}14`, borderColor: `${meta.color}33` }}>
      <span className="h-1.5 w-1.5 rounded-full" style={{ background: meta.color }} />
      {meta.label}
    </span>
  );
};

function OffertaCard({ offer, listValue, onEdit, onDelete }) {
  const status = offerStatus(offer);
  const saving = listValue > 0 ? listValue - (offer.price ?? 0) : null;
  const productsCount = offer.offerProducts?.length ?? 0;
  const menusCount = offer.menus?.length ?? 0;

  return (
    <div className="bg-[#111318] border border-white/5 rounded-[18px] p-5 flex flex-col gap-4 hover:border-[#38b6ff]/25 transition-all group">
      {/* Header */}
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <h3 className="font-['Syne'] text-[16px] font-bold text-[#f0f4ff] truncate">{offer.name}</h3>
          {offer.description && <p className="text-[12px] text-[#8b92a8] mt-1 line-clamp-2">{offer.description}</p>}
        </div>
        <StatusBadge status={status} />
      </div>

      {/* Prezzo + sconto */}
      <div className="flex items-end gap-3">
        <span className="font-['Syne'] text-[26px] font-bold text-[#38b6ff] leading-none">{fmtCur(offer.price)}</span>
        {offer.discountPercentage != null && (
          <span className="text-[12px] font-semibold text-[#fbbf24] mb-0.5">−{offer.discountPercentage}%</span>
        )}
        {saving != null && saving > 0 && (
          <span className="text-[11px] text-[#22d3a0] mb-0.5 ml-auto">risparmi {fmtCur(saving)}</span>
        )}
      </div>

      {/* Meta: prodotti / menu / validità */}
      <div className="flex flex-wrap items-center gap-x-4 gap-y-2 text-[11px] text-[#8b92a8] border-t border-white/5 pt-3">
        <span className="inline-flex items-center gap-1.5"><Icon.box width="13" height="13" />{productsCount} prodotti</span>
        <span className="inline-flex items-center gap-1.5"><Icon.menu width="13" height="13" />{menusCount} menu</span>
        <span className="inline-flex items-center gap-1.5"><Icon.calendar width="13" height="13" />{fmtDate(offer.startDate)} – {fmtDate(offer.endDate)}</span>
      </div>

      {/* Azioni */}
      <div className="flex justify-end gap-2 opacity-70 group-hover:opacity-100 transition-opacity">
        <button onClick={() => onEdit(offer)} title="Modifica offerta" className="p-2 rounded-lg bg-[#181c23] border border-white/5 hover:border-[#38b6ff]/50 text-[#8b92a8] hover:text-[#38b6ff] transition-all">
          <Icon.edit width="14" height="14" />
        </button>
        <button onClick={() => onDelete(offer)} title="Elimina offerta" className="p-2 rounded-lg bg-[#181c23] border border-white/5 hover:border-[#ff5e5e]/50 text-[#8b92a8] hover:text-[#ff5e5e] transition-all">
          <Icon.trash width="14" height="14" />
        </button>
      </div>
    </div>
  );
}

export default function OfferteGrid({ offers, listValueOf, onEdit, onDelete }) {
  if (offers.length === 0) {
    return (
      <div className="bg-[#111318] border border-dashed border-white/10 rounded-[18px] p-12 text-center text-[13px] text-[#4e5566]">
        Nessuna offerta presente. Crea la prima campagna promozionale.
      </div>
    );
  }
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
      {offers.map((o) => (
        <OffertaCard key={o.id} offer={o} listValue={listValueOf(o)} onEdit={onEdit} onDelete={onDelete} />
      ))}
    </div>
  );
}
