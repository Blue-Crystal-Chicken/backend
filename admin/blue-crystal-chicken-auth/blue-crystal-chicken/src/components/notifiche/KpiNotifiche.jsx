const Icon = {
  bell: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" {...p}><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" /><path d="M13.73 21a2 2 0 0 1-3.46 0" /></svg>),
  dot: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...p}><circle cx="12" cy="12" r="9" /><circle cx="12" cy="12" r="3" fill="currentColor" /></svg>),
  alert: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" {...p}><path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" /><line x1="12" y1="9" x2="12" y2="13" /><line x1="12" y1="17" x2="12.01" y2="17" /></svg>),
  clock: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" {...p}><circle cx="12" cy="12" r="10" /><polyline points="12 6 12 12 16 14" /></svg>),
};

const Card = ({ label, value, icon, subtext, accentColor }) => (
  <div className="bg-[#111318] border border-white/5 rounded-[18px] p-[20px_22px] relative overflow-hidden">
    <div className="absolute top-0 left-0 right-0 h-[2px] opacity-70" style={{ backgroundColor: accentColor }} />
    <div className="flex items-center justify-between mb-3">
      <span className="text-[11px] font-semibold tracking-[1px] uppercase text-[#8b92a8]">{label}</span>
      <span className="w-[32px] h-[32px] rounded-[9px] flex items-center justify-center" style={{ background: `${accentColor}18`, color: accentColor }}>{icon}</span>
    </div>
    <div className="font-['Syne'] text-[26px] font-bold leading-none mb-1.5 text-[#f0f4ff]">{value}</div>
    {subtext && <div className="text-[12px] font-medium text-[#8b92a8] truncate">{subtext}</div>}
  </div>
);

export default function KpiNotifiche({ items, unreadCount }) {
  const total = items.length;
  const warnings = items.filter((n) => n.level === "WARNING" || n.level === "ERROR").length;
  const today = items.filter((n) => {
    if (!n.createdAt) return false;
    const d = new Date(n.createdAt);
    const now = new Date();
    return d.toDateString() === now.toDateString();
  }).length;

  return (
    <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <Card label="Notifiche totali" value={total} icon={<Icon.bell width="15" height="15" />} subtext="log raccolti" accentColor="#38b6ff" />
      <Card label="Non lette" value={unreadCount} icon={<Icon.dot width="15" height="15" />} subtext="da revisionare" accentColor="#a78bfa" />
      <Card label="Da attenzionare" value={warnings} icon={<Icon.alert width="15" height="15" />} subtext="warning / errori" accentColor="#fbbf24" />
      <Card label="Oggi" value={today} icon={<Icon.clock width="15" height="15" />} subtext="nelle ultime 24h" accentColor="#22d3a0" />
    </div>
  );
}
