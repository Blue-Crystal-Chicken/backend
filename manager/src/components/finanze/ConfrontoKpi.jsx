const fmt = (n) =>
  new Intl.NumberFormat("it-IT", { style: "currency", currency: "EUR" }).format(n ?? 0);

const METRICHE = [
  {
    key:    "revenue",
    label:  "Fatturato",
    format: (v) => fmt(v),
  },
  {
    key:    "avgTicket",
    label:  "Scontrino medio",
    format: (v) => fmt(v),
  },
  {
    key:    "count",
    label:  "Ordini totali",
    format: (v) => v,
  },
];

export default function ConfrontoKpi({ confrontoData }) {
  if (!confrontoData || confrontoData.length === 0) return null;

  // La sede con fatturato maggiore per ogni metrica (per l'highlight)
  const maxByKey = {};
  METRICHE.forEach(({ key }) => {
    maxByKey[key] = Math.max(...confrontoData.map((s) => s[key] ?? 0));
  });

  return (
    <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
      <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff] mb-5">
        Confronto sedi
      </h3>

      <div className="overflow-x-auto">
        <table className="w-full">
          <thead>
            <tr className="border-b border-white/5">
              {/* Colonna metrica */}
              <th className="text-left text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 pr-4 w-[120px]">
                Metrica
              </th>
              {/* Colonna per ogni sede */}
              {confrontoData.map((sede) => (
                <th key={sede.id} className="text-right pb-3 px-3">
                  <div className="flex items-center justify-end gap-1.5">
                    <span className="w-2 h-2 rounded-full" style={{ backgroundColor: sede.color }} />
                    <span
                      className="text-[11px] font-semibold truncate max-w-[100px]"
                      style={{ color: sede.color }}
                    >
                      {sede.name}
                    </span>
                  </div>
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-white/[0.04]">
            {METRICHE.map(({ key, label, format }) => (
              <tr key={key} className="hover:bg-[#181c23] transition-colors">
                <td className="py-3 pr-4 text-[12px] text-[#8b92a8]">{label}</td>
                {confrontoData.map((sede) => {
                  const isMax = sede[key] === maxByKey[key] && maxByKey[key] > 0;
                  return (
                    <td key={sede.id} className="py-3 px-3 text-right">
                      <span
                        className={`font-['Syne'] text-[14px] font-bold ${
                          isMax ? "" : "text-[#8b92a8]"
                        }`}
                        style={isMax ? { color: sede.color } : {}}
                      >
                        {format(sede[key])}
                      </span>
                    </td>
                  );
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
