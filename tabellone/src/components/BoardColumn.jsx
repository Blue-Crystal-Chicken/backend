import OrderCard from "./OrderCard";
import { RESPONSE_FIELDS as F } from "../config";

export default function BoardColumn({ column, onAdvance }) {
  const orders = column.orders || [];
  return (
    <section className="flex flex-col min-h-0">
      {/* Header colonna */}
      <div className="flex items-center justify-between mb-4 px-1">
        <div className="flex items-center gap-2.5">
          <span
            className="w-3 h-3 rounded-full"
            style={{ backgroundColor: column.accent }}
          />
          <h2 className="font-['Syne'] text-[20px] font-extrabold tracking-tight text-[#f0f4ff]">
            {column.title}
          </h2>
        </div>
        <span
          className="font-['Syne'] text-[15px] font-bold px-3 py-1 rounded-full"
          style={{
            backgroundColor: `${column.accent}18`,
            color: column.accent,
            border: `1px solid ${column.accent}40`,
          }}
        >
          {orders.length}
        </span>
      </div>

      {/* Lista card */}
      <div className="grid grid-cols-1 xl:grid-cols-2 gap-4 content-start overflow-y-auto pr-1 flex-1">
        {orders.length === 0 ? (
          <div className="col-span-full border border-dashed border-white/10 rounded-[18px] py-16 flex flex-col items-center justify-center text-[#4e5566]">
            <svg
              width="36"
              height="36"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="1.5"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <path d="M5 12h14M5 12a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v4a2 2 0 0 1-2 2M5 12a2 2 0 0 0-2 2v4a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-4a2 2 0 0 0-2-2" />
            </svg>
            <span className="text-[12px] font-semibold uppercase tracking-wider mt-3">
              Nessun ordine
            </span>
          </div>
        ) : (
          orders.map((o) => (
            <OrderCard
              key={o[F.id]}
              order={o}
              column={column}
              onAdvance={onAdvance}
            />
          ))
        )}
      </div>
    </section>
  );
}
