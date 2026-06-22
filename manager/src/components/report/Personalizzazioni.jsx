import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from "recharts";

const fmtEur = (n) => new Intl.NumberFormat("it-IT", { style: "currency", currency: "EUR" }).format(n ?? 0);

function deriveCustom(orders) {
  const productNotes = {};
  let ordersWithNote = 0;
  let ordersWithAdditions = 0;
  let totalAdditions = 0;
  let additionsCount = 0;
  const noteKeywords = {};

  orders.forEach((o) => {
    let hasNote = false;
    let hasAdd  = false;

    (o.items ?? []).forEach((item) => {
      if (item.specialNote && item.specialNote.trim() !== "") {
        hasNote = true;
        const key = item.productName ?? "Sconosciuto";
        productNotes[key] = (productNotes[key] ?? 0) + 1;
        // keyword frequency (split by space, filter short words)
        item.specialNote.toLowerCase().split(/\s+/).forEach((w) => {
          if (w.length > 3) noteKeywords[w] = (noteKeywords[w] ?? 0) + 1;
        });
      }
      if (item.additions && item.additions > 0) {
        hasAdd = true;
        totalAdditions += item.additions;
        additionsCount++;
      }
    });

    if (hasNote) ordersWithNote++;
    if (hasAdd)  ordersWithAdditions++;
  });

  const topProducts = Object.entries(productNotes)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 10)
    .map(([name, count]) => ({ name, count }));

  const topKeywords = Object.entries(noteKeywords)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 8)
    .map(([word, count]) => ({ word, count }));

  return {
    ordersWithNote,
    ordersWithAdditions,
    notePct: orders.length > 0 ? ((ordersWithNote / orders.length) * 100).toFixed(1) : "0.0",
    addPct:  orders.length > 0 ? ((ordersWithAdditions / orders.length) * 100).toFixed(1) : "0.0",
    avgAdditions: additionsCount > 0 ? totalAdditions / additionsCount : 0,
    topProducts,
    topKeywords,
  };
}

const BarTooltip = ({ active, payload }) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-[#181c23] border border-white/5 rounded-[10px] p-[8px_12px]">
      <div className="font-['Syne'] text-[13px] font-bold text-[#f0f4ff]">{payload[0].payload.name}</div>
      <div className="text-[12px] text-[#a78bfa]">{payload[0].value} note speciali</div>
    </div>
  );
};

export default function Personalizzazioni({ orders, loading }) {
  const data = deriveCustom(orders);

  return (
    <div className="space-y-4">

      {/* KPI */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {[
          { label: "Ordini con nota speciale", value: loading ? "—" : `${data.notePct}%`, sub: `${data.ordersWithNote} ordini`, color: "#a78bfa" },
          { label: "Ordini con aggiunte", value: loading ? "—" : `${data.addPct}%`, sub: `${data.ordersWithAdditions} ordini`, color: "#fbbf24" },
          { label: "Valore medio aggiunte", value: loading ? "—" : fmtEur(data.avgAdditions), sub: "per item con addition", color: "#22d3a0" },
          { label: "Personalizzazione media", value: loading ? "—" : `${((Number(data.notePct) + Number(data.addPct)) / 2).toFixed(1)}%`, sub: "media tra note e aggiunte", color: "#38b6ff" },
        ].map(({ label, value, sub, color }) => (
          <div key={label} className="bg-[#111318] border border-white/5 rounded-[14px] p-5 relative overflow-hidden">
            <div className="absolute top-0 left-0 right-0 h-[2px]" style={{ backgroundColor: color }} />
            <div className="text-[10px] font-semibold tracking-widest uppercase text-[#4e5566] mb-2">{label}</div>
            <div className="font-['Syne'] text-[24px] font-bold" style={{ color }}>{value}</div>
            <div className="text-[10px] text-[#4e5566] mt-1">{sub}</div>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">

        {/* Top prodotti personalizzati */}
        <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
          <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff] mb-5">
            Prodotti più personalizzati
          </h3>
          {loading ? (
            <div className="space-y-3 animate-pulse">
              {Array.from({ length: 6 }).map((_, i) => (
                <div key={i} className="flex items-center gap-3">
                  <div className="h-3 bg-white/[0.05] rounded w-[35%]" />
                  <div className="h-6 bg-white/[0.05] rounded flex-1" />
                </div>
              ))}
            </div>
          ) : data.topProducts.length === 0 ? (
            <div className="flex items-center justify-center h-[200px] text-[13px] text-[#4e5566]">
              Nessuna nota speciale nel periodo.
            </div>
          ) : (
            <div className="h-[240px]">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={data.topProducts} layout="vertical" margin={{ left: 0, right: 16 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.04)" horizontal={false} />
                  <XAxis type="number" allowDecimals={false} tick={{ fill: "#4e5566", fontSize: 11 }} axisLine={false} tickLine={false} />
                  <YAxis type="category" dataKey="name" tick={{ fill: "#8b92a8", fontSize: 11 }} axisLine={false} tickLine={false} width={110} />
                  <Tooltip content={<BarTooltip />} />
                  <Bar dataKey="count" radius={[0, 4, 4, 0]} maxBarSize={16}>
                    {data.topProducts.map((_, i) => (
                      <Cell key={i} fill="#a78bfa" fillOpacity={0.85 - i * 0.05} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          )}
        </div>

        {/* Keyword più frequenti nelle note */}
        <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
          <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff] mb-5">
            Parole chiave nelle note
          </h3>
          {loading ? (
            <div className="flex flex-wrap gap-2 animate-pulse">
              {[80, 60, 100, 70, 90, 55, 75, 65].map((w, i) => (
                <div key={i} className="h-8 bg-white/[0.05] rounded-full" style={{ width: w }} />
              ))}
            </div>
          ) : data.topKeywords.length === 0 ? (
            <div className="flex items-center justify-center h-[200px] text-[13px] text-[#4e5566]">
              Nessuna nota speciale nel periodo.
            </div>
          ) : (
            <div className="flex flex-wrap gap-2 content-start">
              {data.topKeywords.map(({ word, count }, i) => {
                const size = Math.max(11, 11 + (i === 0 ? 6 : i === 1 ? 4 : i < 4 ? 2 : 0));
                const opacity = 1 - i * 0.08;
                return (
                  <span
                    key={word}
                    className="px-3 py-1.5 rounded-full border font-semibold"
                    style={{
                      fontSize: size,
                      opacity,
                      backgroundColor: "#a78bfa18",
                      borderColor: "#a78bfa40",
                      color: "#a78bfa",
                    }}
                  >
                    {word}
                    <span className="ml-1.5 text-[10px] opacity-60">{count}</span>
                  </span>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
