import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from "recharts";

const fmtEur = (n) => new Intl.NumberFormat("it-IT", { style: "currency", currency: "EUR" }).format(n ?? 0);
const COLORS = ["#38b6ff", "#22d3a0", "#a78bfa", "#fbbf24", "#ff5e5e", "#5eead4", "#f0abfc", "#fb923c", "#60a5fa"];

const Tip = ({ active, payload }) => {
  if (!active || !payload?.length) return null;
  const d = payload[0].payload;
  return (
    <div className="bg-[#181c23] border border-white/5 rounded-[10px] p-[8px_12px]">
      <div className="font-['Syne'] text-[13px] font-bold text-[#f0f4ff]">{d.category}</div>
      <div className="text-[12px] text-[#22d3a0] mt-0.5">{fmtEur(d.revenue)} · {d.units} unità</div>
    </div>
  );
};

function EmptyState({ text }) {
  return (
    <div className="bg-[#111318] border border-white/5 rounded-[18px] p-10 text-center text-[13px] text-[#8b92a8]">
      {text}
    </div>
  );
}

export default function DistribuzioneCategorie({ orders = [], products = [], loading }) {
  const prodById = Object.fromEntries(products.map((p) => [p.id, p]));

  const catMap = {};
  orders.forEach((o) =>
    (o.items ?? []).forEach((it) => {
      const cat = it.offerId ? "OFFERTE" : (prodById[it.productId]?.category ?? "ALTRO");
      if (!catMap[cat]) catMap[cat] = { category: cat, units: 0, revenue: 0 };
      catMap[cat].units += it.quantity ?? 1;
      catMap[cat].revenue += (it.price ?? 0) * (it.quantity ?? 1);
    })
  );
  const cats = Object.values(catMap).sort((a, b) => b.revenue - a.revenue);
  const totalRev = cats.reduce((s, c) => s + c.revenue, 0);
  const totalUnits = cats.reduce((s, c) => s + c.units, 0);
  const dominant = cats[0];

  // numero prodotti a catalogo per categoria
  const prodPerCat = {};
  products.forEach((p) => { prodPerCat[p.category] = (prodPerCat[p.category] ?? 0) + 1; });

  if (loading) return <EmptyState text="Caricamento dati…" />;
  if (cats.length === 0) return <EmptyState text="Nessun ordine nel periodo selezionato." />;

  return (
    <div className="space-y-6">
      {/* KPI */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
        {[
          { label: "Categorie attive", value: cats.length, color: "#38b6ff" },
          { label: "Categoria top", value: dominant?.category ?? "—", color: "#22d3a0" },
          { label: "Fatturato totale", value: fmtEur(totalRev), color: "#fbbf24" },
          { label: "Unità vendute", value: totalUnits, color: "#a78bfa" },
        ].map((k) => (
          <div key={k.label} className="bg-[#111318] border border-white/5 rounded-[16px] p-5">
            <div className="text-[11px] uppercase tracking-wider text-[#8b92a8] mb-2">{k.label}</div>
            <div className="font-['Syne'] text-[22px] font-bold truncate" style={{ color: k.color }}>{k.value}</div>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        {/* Donut fatturato per categoria */}
        <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
          <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff] mb-5">Quota fatturato per categoria</h3>
          <div className="h-[280px]">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie data={cats} dataKey="revenue" nameKey="category" cx="50%" cy="50%" innerRadius={70} outerRadius={110} paddingAngle={2} stroke="none">
                  {cats.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                </Pie>
                <Tooltip content={<Tip />} />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Barre volume per categoria */}
        <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
          <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff] mb-5">Unità vendute per categoria</h3>
          <div className="space-y-3">
            {cats.map((c, i) => (
              <div key={c.category} className="flex items-center gap-3">
                <span className="text-[12px] text-[#8b92a8] w-[90px] truncate">{c.category}</span>
                <div className="flex-1 h-5 bg-white/[0.05] rounded-md overflow-hidden">
                  <div className="h-full rounded-md" style={{ width: `${totalUnits > 0 ? (c.units / cats[0].units) * 100 : 0}%`, backgroundColor: COLORS[i % COLORS.length], opacity: 0.85 }} />
                </div>
                <span className="font-['Syne'] text-[13px] font-bold text-[#f0f4ff] w-[40px] text-right">{c.units}</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Tabella dettaglio */}
      <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
        <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff] mb-5">Dettaglio categorie</h3>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-white/5">
                {["Categoria", "Prodotti a catalogo", "Unità", "Fatturato", "% fatturato"].map((h, i) => (
                  <th key={i} className={`text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3 ${i > 0 ? "text-right" : "text-left"}`}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-white/[0.04]">
              {cats.map((c, i) => (
                <tr key={c.category} className="hover:bg-[#181c23] transition-colors">
                  <td className="py-3 px-3 text-[13px] font-medium text-[#f0f4ff]">
                    <span className="inline-flex items-center gap-2">
                      <span className="w-2 h-2 rounded-full" style={{ backgroundColor: COLORS[i % COLORS.length] }} />
                      {c.category}
                    </span>
                  </td>
                  <td className="py-3 px-3 text-right text-[13px] text-[#8b92a8]">{prodPerCat[c.category] ?? "—"}</td>
                  <td className="py-3 px-3 text-right font-['Syne'] text-[13px] font-bold text-[#38b6ff]">{c.units}</td>
                  <td className="py-3 px-3 text-right font-['Syne'] text-[13px] font-bold text-[#22d3a0]">{fmtEur(c.revenue)}</td>
                  <td className="py-3 px-3 text-right text-[12px] text-[#8b92a8]">{totalRev > 0 ? ((c.revenue / totalRev) * 100).toFixed(1) : 0}%</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
