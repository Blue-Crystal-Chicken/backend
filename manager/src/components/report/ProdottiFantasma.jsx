const fmtEur = (n) => new Intl.NumberFormat("it-IT", { style: "currency", currency: "EUR" }).format(n ?? 0);

function EmptyState({ text }) {
  return (
    <div className="bg-[#111318] border border-white/5 rounded-[18px] p-10 text-center text-[13px] text-[#8b92a8]">
      {text}
    </div>
  );
}

export default function ProdottiFantasma({ orders = [], products = [], loading }) {
  // ID prodotto effettivamente ordinati nel periodo
  const orderedIds = new Set();
  orders.forEach((o) => (o.items ?? []).forEach((it) => { if (it.productId != null) orderedIds.add(it.productId); }));

  const ghosts = products
    .filter((p) => !orderedIds.has(p.id))
    .sort((a, b) => (b.price ?? 0) - (a.price ?? 0));

  const total = products.length;
  const ghostCount = ghosts.length;
  const ghostPct = total > 0 ? (ghostCount / total) * 100 : 0;
  const lostListino = ghosts.reduce((s, p) => s + (p.price ?? 0), 0);

  if (loading) return <EmptyState text="Caricamento dati…" />;
  if (total === 0) return <EmptyState text="Catalogo prodotti non disponibile." />;

  return (
    <div className="space-y-6">
      {/* KPI */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
        {[
          { label: "Prodotti fantasma", value: ghostCount, color: "#ff5e5e" },
          { label: "% sul catalogo", value: `${ghostPct.toFixed(0)}%`, color: "#fbbf24" },
          { label: "Prodotti venduti", value: total - ghostCount, color: "#22d3a0" },
          { label: "Valore listino fermo", value: fmtEur(lostListino), color: "#a78bfa" },
        ].map((k) => (
          <div key={k.label} className="bg-[#111318] border border-white/5 rounded-[16px] p-5">
            <div className="text-[11px] uppercase tracking-wider text-[#8b92a8] mb-2">{k.label}</div>
            <div className="font-['Syne'] text-[22px] font-bold truncate" style={{ color: k.color }}>{k.value}</div>
          </div>
        ))}
      </div>

      {/* Lista fantasma */}
      <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
        <div className="flex items-center justify-between mb-5">
          <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff]">Prodotti mai ordinati nel periodo</h3>
          <span className="text-[11px] text-[#4e5566]">{ghostCount} di {total} prodotti</span>
        </div>

        {ghostCount === 0 ? (
          <div className="flex flex-col items-center gap-3 py-8 text-center">
            <div className="w-12 h-12 rounded-full bg-[#22d3a0]/10 border border-[#22d3a0]/20 flex items-center justify-center">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#22d3a0" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12" /></svg>
            </div>
            <p className="text-[13px] text-[#8b92a8]">Ottimo: ogni prodotto del catalogo è stato ordinato almeno una volta nel periodo.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-white/5">
                  {["#", "Prodotto", "Categoria", "Disponibile", "Prezzo"].map((h, i) => (
                    <th key={i} className={`text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3 ${i > 1 ? "text-right" : "text-left"}`}>{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-white/[0.04]">
                {ghosts.map((p, i) => (
                  <tr key={p.id} className="hover:bg-[#181c23] transition-colors">
                    <td className="py-3 px-3 text-[12px] text-[#4e5566] w-8">{i + 1}</td>
                    <td className="py-3 px-3 text-[13px] font-medium text-[#f0f4ff]">{p.name}</td>
                    <td className="py-3 px-3 text-[12px] text-[#8b92a8]">{p.category ?? "—"}</td>
                    <td className="py-3 px-3 text-right">
                      {p.available === false ? (
                        <span className="text-[11px] font-semibold px-2 py-0.5 rounded-full bg-[#8b92a8]/10 text-[#8b92a8] border border-[#8b92a8]/20">Non disp.</span>
                      ) : (
                        <span className="text-[11px] font-semibold px-2 py-0.5 rounded-full bg-[#fbbf24]/10 text-[#fbbf24] border border-[#fbbf24]/20">Mai ordinato</span>
                      )}
                    </td>
                    <td className="py-3 px-3 text-right font-['Syne'] text-[13px] font-bold text-[#f0f4ff]">{fmtEur(p.price)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
