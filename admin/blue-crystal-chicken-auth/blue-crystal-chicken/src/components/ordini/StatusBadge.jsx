import { STATUS_COLORS, STATUS_LABELS } from "../../hooks/useOrdini";

export default function StatusBadge({ status }) {
  const color = STATUS_COLORS[status] ?? "#8b92a8";
  const label = STATUS_LABELS[status]  ?? status;
  return (
    <span
      className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-bold whitespace-nowrap"
      style={{ backgroundColor: `${color}18`, color, border: `1px solid ${color}40` }}
    >
      <span className="w-1.5 h-1.5 rounded-full flex-shrink-0" style={{ backgroundColor: color }} />
      {label}
    </span>
  );
}
