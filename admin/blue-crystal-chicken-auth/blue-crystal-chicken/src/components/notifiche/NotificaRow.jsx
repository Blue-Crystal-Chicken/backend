import { LEVEL_META } from "../../hooks/useNotifiche";

// Tempo relativo compatto ("2 min fa", "3 h fa", "ieri")
function relativeTime(iso) {
  if (!iso) return "";
  const diff = Date.now() - new Date(iso).getTime();
  const min = Math.floor(diff / 60000);
  if (min < 1) return "ora";
  if (min < 60) return `${min} min fa`;
  const h = Math.floor(min / 60);
  if (h < 24) return `${h} h fa`;
  const d = Math.floor(h / 24);
  if (d === 1) return "ieri";
  if (d < 7) return `${d} giorni fa`;
  return new Date(iso).toLocaleDateString("it-IT", { day: "2-digit", month: "short" });
}

const LevelIcon = ({ level, color }) => {
  const common = { width: 16, height: 16, viewBox: "0 0 24 24", fill: "none", stroke: color, strokeWidth: 2, strokeLinecap: "round", strokeLinejoin: "round" };
  if (level === "WARNING") return <svg {...common}><path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" /><line x1="12" y1="9" x2="12" y2="13" /><line x1="12" y1="17" x2="12.01" y2="17" /></svg>;
  if (level === "ERROR") return <svg {...common}><circle cx="12" cy="12" r="10" /><line x1="15" y1="9" x2="9" y2="15" /><line x1="9" y1="9" x2="15" y2="15" /></svg>;
  if (level === "SUCCESS") return <svg {...common}><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" /><polyline points="22 4 12 14.01 9 11.01" /></svg>;
  return <svg {...common}><circle cx="12" cy="12" r="10" /><line x1="12" y1="16" x2="12" y2="12" /><line x1="12" y1="8" x2="12.01" y2="8" /></svg>;
};

const Icon = {
  check: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" {...p}><polyline points="20 6 9 17 4 12" /></svg>),
  trash: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...p}><polyline points="3 6 5 6 21 6" /><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" /></svg>),
};

export default function NotificaRow({ notification, onRead, onDelete }) {
  const meta = LEVEL_META[notification.level] ?? LEVEL_META.INFO;
  const unread = !notification.readFlag;

  return (
    <div className={`flex items-start gap-4 rounded-[14px] border px-4 py-3.5 transition-all group ${unread ? "bg-[#13171f] border-white/8" : "bg-[#111318] border-white/5"}`}>
      {/* Icona livello */}
      <span className="w-9 h-9 rounded-[10px] flex items-center justify-center shrink-0 mt-0.5" style={{ background: `${meta.color}16` }}>
        <LevelIcon level={notification.level} color={meta.color} />
      </span>

      {/* Contenuto */}
      <div className="min-w-0 flex-1">
        <div className="flex items-center gap-2 mb-0.5">
          {unread && <span className="w-1.5 h-1.5 rounded-full shrink-0" style={{ background: meta.color }} />}
          <span className="font-['Syne'] font-bold text-[13.5px] text-[#f0f4ff] truncate">{notification.title}</span>
          {notification.source && (
            <span className="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-[#181c23] border border-white/5 text-[#8b92a8] uppercase tracking-wider shrink-0">{notification.source}</span>
          )}
        </div>
        <p className="text-[12.5px] text-[#8b92a8] leading-snug">{notification.message}</p>
      </div>

      {/* Tempo + azioni */}
      <div className="flex flex-col items-end gap-2 shrink-0">
        <span className="text-[11px] text-[#4e5566] whitespace-nowrap">{relativeTime(notification.createdAt)}</span>
        <div className="flex gap-1.5 opacity-60 group-hover:opacity-100 transition-opacity">
          {unread && (
            <button onClick={() => onRead(notification.id)} title="Segna come letta" className="p-1.5 rounded-md bg-[#181c23] border border-white/5 text-[#8b92a8] hover:text-[#22d3a0] hover:border-[#22d3a0]/50 transition-all">
              <Icon.check width="13" height="13" />
            </button>
          )}
          <button onClick={() => onDelete(notification.id)} title="Elimina" className="p-1.5 rounded-md bg-[#181c23] border border-white/5 text-[#8b92a8] hover:text-[#ff5e5e] hover:border-[#ff5e5e]/50 transition-all">
            <Icon.trash width="13" height="13" />
          </button>
        </div>
      </div>
    </div>
  );
}
