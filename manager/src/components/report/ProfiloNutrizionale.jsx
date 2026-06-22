function EmptyState({ text }) {
  return (
    <div className="bg-[#111318] border border-white/5 rounded-[18px] p-10 text-center text-[13px] text-[#8b92a8]">
      {text}
    </div>
  );
}

function Donut({ value, color, label }) {
  // anello SVG: percentuale 0-100
  const r = 34, c = 2 * Math.PI * r;
  const pct = Math.max(0, Math.min(100, value));
  return (
    <div className="flex flex-col items-center gap-2">
      <svg width="92" height="92" viewBox="0 0 92 92">
        <circle cx="46" cy="46" r={r} fill="none" stroke="rgba(255,255,255,0.06)" strokeWidth="9" />
        <circle cx="46" cy="46" r={r} fill="none" stroke={color} strokeWidth="9" strokeLinecap="round"
          strokeDasharray={c} strokeDashoffset={c - (pct / 100) * c} transform="rotate(-90 46 46)" />
        <text x="46" y="51" textAnchor="middle" className="font-['Syne']" fontSize="18" fontWeight="700" fill="#f0f4ff">{pct.toFixed(0)}%</text>
      </svg>
      <span className="text-[12px] text-[#8b92a8]">{label}</span>
    </div>
  );
}

export default function ProfiloNutrizionale({ orders = [], products = [], loading }) {
  const prodById = Object.fromEntries(products.map((p) => [p.id, p]));

  let units = 0, calories = 0, veg = 0, vegan = 0, gf = 0, spicy = 0;
  const spicyMap = {};
  orders.forEach((o) =>
    (o.items ?? []).forEach((it) => {
      const p = prodById[it.productId];
      if (!p) return; // salta offerte / prodotti non trovati
      const q = it.quantity ?? 1;
      units += q;
      calories += (p.calories ?? 0) * q;
      if (p.isVegetarian) veg += q;
      if (p.isVegan) vegan += q;
      if (p.isGlutenFree) gf += q;
      if (p.isSpicy) {
        spicy += q;
        spicyMap[p.name] = (spicyMap[p.name] ?? 0) + q;
      }
    })
  );

  const avgCal = units > 0 ? calories / units : 0;
  const pct = (n) => (units > 0 ? (n / units) * 100 : 0);
  const topSpicy = Object.entries(spicyMap).map(([name, u]) => ({ name, units: u })).sort((a, b) => b.units - a.units).slice(0, 5);
  const maxSpicy = topSpicy[0]?.units ?? 1;

  if (loading) return <EmptyState text="Caricamento dati…" />;
  if (units === 0) return <EmptyState text="Nessun prodotto del catalogo ordinato nel periodo." />;

  return (
    <div className="space-y-6">
      {/* KPI calorie */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
        {[
          { label: "Calorie medie / prodotto", value: `${avgCal.toFixed(0)} kcal`, color: "#fbbf24" },
          { label: "Calorie totali vendute", value: `${(calories / 1000).toFixed(1)}k kcal`, color: "#ff5e5e" },
          { label: "Prodotti venduti", value: units, color: "#38b6ff" },
          { label: "Prodotti spicy venduti", value: spicy, color: "#ff5e5e" },
        ].map((k) => (
          <div key={k.label} className="bg-[#111318] border border-white/5 rounded-[16px] p-5">
            <div className="text-[11px] uppercase tracking-wider text-[#8b92a8] mb-2">{k.label}</div>
            <div className="font-['Syne'] text-[22px] font-bold truncate" style={{ color: k.color }}>{k.value}</div>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        {/* Anelli dieta */}
        <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
          <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff] mb-5">Composizione su unità vendute</h3>
          <div className="flex items-center justify-around py-2">
            <Donut value={pct(veg)} color="#22d3a0" label="Vegetariano" />
            <Donut value={pct(vegan)} color="#5eead4" label="Vegano" />
            <Donut value={pct(gf)} color="#a78bfa" label="Gluten Free" />
          </div>
        </div>

        {/* Top spicy */}
        <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
          <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff] mb-5">Top prodotti piccanti</h3>
          {topSpicy.length === 0 ? (
            <div className="text-[13px] text-[#8b92a8] py-6 text-center">Nessun prodotto spicy ordinato nel periodo.</div>
          ) : (
            <div className="space-y-3">
              {topSpicy.map((s) => (
                <div key={s.name} className="flex items-center gap-3">
                  <span className="text-[12px] text-[#8b92a8] w-[120px] truncate">{s.name}</span>
                  <div className="flex-1 h-5 bg-white/[0.05] rounded-md overflow-hidden">
                    <div className="h-full rounded-md bg-[#ff5e5e]" style={{ width: `${(s.units / maxSpicy) * 100}%`, opacity: 0.85 }} />
                  </div>
                  <span className="font-['Syne'] text-[13px] font-bold text-[#f0f4ff] w-[36px] text-right">{s.units}</span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
