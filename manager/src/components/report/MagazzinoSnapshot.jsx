const fmtEur = (n) => new Intl.NumberFormat("it-IT", { style: "currency", currency: "EUR" }).format(n ?? 0);
const THRESHOLD = 10;

function EmptyState({ text }) {
  return (
    <div className="bg-[#111318] border border-white/5 rounded-[18px] p-10 text-center text-[13px] text-[#8b92a8]">
      {text}
    </div>
  );
}

function StockBadge({ qty }) {
  let cfg;
  if ((qty ?? 0) <= 0) cfg = { label: "Esaurito", c: "#ff5e5e" };
  else if (qty < THRESHOLD) cfg = { label: "Critico", c: "#fbbf24" };
  else cfg = { label: "OK", c: "#22d3a0" };
  return (
    <span className="text-[11px] font-semibold px-2 py-0.5 rounded-full border" style={{ color: cfg.c, backgroundColor: `${cfg.c}1a`, borderColor: `${cfg.c}33` }}>
      {cfg.label}
    </span>
  );
}

export default function MagazzinoSnapshot({ ingredients = [], loading }) {
  const total = ingredients.length;
  const value = ingredients.reduce((s, i) => s + (i.quantity ?? 0) * (i.price ?? 0), 0);
  const low = ingredients.filter((i) => (i.quantity ?? 0) > 0 && (i.quantity ?? 0) < THRESHOLD).length;
  const empty = ingredients.filter((i) => (i.quantity ?? 0) <= 0).length;

  // ordinati per criticità (quantità crescente)
  const critical = [...ingredients].sort((a, b) => (a.quantity ?? 0) - (b.quantity ?? 0));

  if (loading) return <EmptyState text="Caricamento dati…" />;
  if (total === 0) return <EmptyState text="Nessun ingrediente a catalogo." />;

  return (
    <div className="space-y-6">
      {/* KPI */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
        {[
          { label: "Ingredienti a catalogo", value: total, color: "#38b6ff" },
          { label: "Valore magazzino", value: fmtEur(value), color: "#22d3a0" },
          { label: "Sotto soglia", value: low, color: "#fbbf24" },
          { label: "Esauriti", value: empty, color: "#ff5e5e" },
        ].map((k) => (
          <div key={k.label} className="bg-[#111318] border border-white/5 rounded-[16px] p-5">
            <div className="text-[11px] uppercase tracking-wider text-[#8b92a8] mb-2">{k.label}</div>
            <div className="font-['Syne'] text-[22px] font-bold truncate" style={{ color: k.color }}>{k.value}</div>
          </div>
        ))}
      </div>

      {/* Tabella criticità */}
      <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
        <div className="flex items-center justify-between mb-5">
          <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff]">Snapshot scorte (per criticità)</h3>
          <span className="text-[11px] text-[#4e5566]">soglia {THRESHOLD} unità</span>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-white/5">
                {["Ingrediente", "Quantità", "Prezzo", "Valore", "Stato"].map((h, i) => (
                  <th key={i} className={`text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3 ${i > 0 ? "text-right" : "text-left"}`}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-white/[0.04]">
              {critical.map((i) => (
                <tr key={i.id} className="hover:bg-[#181c23] transition-colors">
                  <td className="py-3 px-3 text-[13px] font-medium text-[#f0f4ff]">{i.name}</td>
                  <td className="py-3 px-3 text-right font-['Syne'] text-[13px] font-bold text-[#f0f4ff]">{i.quantity ?? 0}</td>
                  <td className="py-3 px-3 text-right text-[13px] text-[#8b92a8]">{i.price != null ? fmtEur(i.price) : "—"}</td>
                  <td className="py-3 px-3 text-right font-['Syne'] text-[13px] font-bold text-[#22d3a0]">{fmtEur((i.quantity ?? 0) * (i.price ?? 0))}</td>
                  <td className="py-3 px-3 text-right"><StockBadge qty={i.quantity} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
