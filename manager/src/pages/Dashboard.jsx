import { useState, useEffect, useCallback, useMemo } from "react";
import {
    AreaChart, Area,
    PieChart, Pie, Cell,
    XAxis, YAxis, CartesianGrid, Tooltip,
    ResponsiveContainer
} from "recharts";
import axiosClient from "../api/axiosClient";
import { managerLocationId } from "../lib/managerScope";

// ─── Costanti grafici (Mantenute fedeli al CSS) ──────────────────────────────
const DONUT_COLORS = {
    COMPLETED: "#22d3a0",
    IN_PROGRESS: "#38b6ff",
    PENDING: "#fbbf24",
    CANCELLED: "#ff5e5e",
};

const DONUT_LABELS = {
    COMPLETED: "Completati",
    IN_PROGRESS: "In corso",
    PENDING: "In attesa",
    CANCELLED: "Annullati",
};

// ─── Range temporale (stessa logica di useFinanze) ───────────────────────────
const RANGE_OPTIONS = [
    { key: "1", label: "Oggi" },
    { key: "7", label: "7 giorni" },
    { key: "14", label: "14 giorni" },
    { key: "30", label: "30 giorni" },
];
const RANGE_OFFSET = { "1": 0, "7": 6, "14": 13, "30": 29 };
const LIVE_INTERVAL_MS = 60_000; // 60 secondi

function computeDateRanges(rangeKey) {
    const offset = RANGE_OFFSET[rangeKey];
    const now = new Date();

    const from = new Date(now);
    from.setDate(from.getDate() - offset);
    from.setHours(0, 0, 0, 0);

    const to = new Date(now);
    to.setHours(23, 59, 59, 999);

    const prevTo = new Date(from.getTime() - 1);
    const prevFrom = new Date(prevTo);
    prevFrom.setDate(prevTo.getDate() - offset);
    prevFrom.setHours(0, 0, 0, 0);

    return {
        from: from.toISOString().slice(0, 19),
        to: to.toISOString().slice(0, 19),
        prevFrom: prevFrom.toISOString().slice(0, 19),
        prevTo: prevTo.toISOString().slice(0, 19),
    };
}

// "oggi" → bucket orari fino all'ora corrente; altri range → bucket giornalieri
function buildBuckets(rangeKey) {
    if (rangeKey === "1") {
        const currentHour = new Date().getHours();
        const buckets = [];
        for (let h = 0; h <= currentHour; h++) {
            buckets.push({ key: `h${h}`, label: `${String(h).padStart(2, "0")}:00` });
        }
        return buckets;
    }
    const totalDays = RANGE_OFFSET[rangeKey] + 1;
    const buckets = [];
    for (let i = totalDays - 1; i >= 0; i--) {
        const d = new Date();
        d.setDate(d.getDate() - i);
        d.setHours(0, 0, 0, 0);
        buckets.push({
            key: d.toDateString(),
            label: d.toLocaleDateString("it-IT", { weekday: "short", day: "numeric" }),
        });
    }
    return buckets;
}

function groupByBucket(orders, rangeKey) {
    const map = {};
    orders.forEach((o) => {
        const d = new Date(o.createdAt);
        const k = rangeKey === "1" ? `h${d.getHours()}` : d.toDateString();
        if (!map[k]) map[k] = [];
        map[k].push(o);
    });
    return map;
}

// ─── Helpers ─────────────────────────────────────────────────────────────────
const fmt = (n) => new Intl.NumberFormat("it-IT", { style: "currency", currency: "EUR" }).format(n ?? 0);
const fmtShort = (n) => {
    if (n == null) return "€0";
    if (n >= 1000) return `€${(n / 1000).toFixed(1)}k`;
    return `€${Number(n).toFixed(0)}`;
};
const sumRevenue = (orders) => orders.reduce((s, o) => s + (o.totalAt ?? 0), 0);
const deltaPct = (curr, prev) => (prev > 0 ? (((curr - prev) / prev) * 100).toFixed(1) : null);
const pctStock = (quantity, max = 100) => Math.min(100, Math.round((quantity / max) * 100));

// ─── ICONE SVG (no emoji, da regola di progetto) ─────────────────────────────
const Icon = {
    euro: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" {...p}><path d="M14 21a8 8 0 1 1 0-16" /><line x1="4" y1="10" x2="13" y2="10" /><line x1="4" y1="14" x2="11" y2="14" /></svg>),
    clipboard: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" {...p}><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2" /><rect x="8" y="2" width="8" height="4" rx="1" ry="1" /></svg>),
    receipt: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" {...p}><path d="M4 2v20l2-1 2 1 2-1 2 1 2-1 2 1 2-1 2 1V2l-2 1-2-1-2 1-2-1-2 1-2-1-2 1Z" /><path d="M8 7h8" /><path d="M8 11h8" /><path d="M8 15h5" /></svg>),
    alert: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" {...p}><path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" /><line x1="12" y1="9" x2="12" y2="13" /><line x1="12" y1="17" x2="12.01" y2="17" /></svg>),
    up: (p) => (<svg viewBox="0 0 24 24" fill="currentColor" {...p}><path d="M12 5l7 12H5z" /></svg>),
    down: (p) => (<svg viewBox="0 0 24 24" fill="currentColor" {...p}><path d="M12 19 5 7h14z" /></svg>),
    dot: (p) => (<svg viewBox="0 0 24 24" fill="currentColor" {...p}><circle cx="12" cy="12" r="6" /></svg>),
};

// ─── Custom Tooltip (Mappato da .dash-tooltip) ──────────────────────────────
const RevenueTooltip = ({ active, payload, label }) => {
    if (!active || !payload?.length) return null;
    return (
        <div className="bg-[#181c23] border border-white/5 rounded-[10px] p-[10px_14px] shadow-2xl">
            <div className="text-[11px] color-[#8b92a8] mb-1">{label}</div>
            <div className="font-['Syne'] text-[16px] font-bold text-[#f0f4ff]">{fmtShort(payload[0]?.value)}</div>
        </div>
    );
};

// ─── Componente KPI (Mappato da .dash-kpi-card) ──────────────────────────────
const KpiCard = ({ label, value, icon, delta, deltaDir, accentColor }) => (
    <div
        className="bg-[#111318] border border-white/5 rounded-[18px] p-[22px_24px] relative overflow-hidden transition-all duration-200 hover:border-[#38b6ff]/25 hover:-translate-y-0.5 group"
    >
        {/* Linea superiore di accento */}
        <div className="absolute top-0 left-0 right-0 h-[2px] opacity-70" style={{ backgroundColor: accentColor }} />

        <div className="flex items-center justify-between mb-[14px]">
            <span className="text-[11px] font-semibold tracking-[1px] uppercase text-[#8b92a8]">{label}</span>
            <span
                className="w-[34px] h-[34px] rounded-[9px] flex items-center justify-center transition-transform group-hover:scale-110"
                style={{ background: `${accentColor}18`, color: accentColor }}
            >
                {icon}
            </span>
        </div>
        <div className="font-['Syne'] text-[32px] font-bold leading-none mb-2 text-[#f0f4ff]">{value}</div>
        {delta && (
            <div className={`text-[12px] font-medium flex items-center gap-1 ${deltaDir === "up" ? "text-[#22d3a0]" : deltaDir === "down" ? "text-[#ff5e5e]" : "text-[#8b92a8]"
                }`}>
                {deltaDir === "up" ? <Icon.up width="9" height="9" /> : deltaDir === "down" ? <Icon.down width="9" height="9" /> : <Icon.dot width="7" height="7" />} {delta}
            </div>
        )}
    </div>
);

export default function Dashboard() {
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [refreshing, setRefreshing] = useState(false);
    const [orders, setOrders] = useState([]);
    const [prevOrders, setPrevOrders] = useState([]);
    const [ingredients, setIngredients] = useState([]);

    // Filtri periodo (stessa logica delle altre pagine)
    const [range, setRange] = useState("7");
    const [isLive, setIsLive] = useState(false);

    const { from, to, prevFrom, prevTo } = useMemo(() => computeDateRanges(range), [range]);

    const toggleLive = useCallback(() => {
        setIsLive((v) => {
            if (!v) setRange("1"); // attivando live → forza range su "oggi"
            return !v;
        });
    }, []);

    // Disattiva live se si cambia range a qualcosa diverso da "oggi"
    useEffect(() => {
        if (range !== "1") setIsLive(false);
    }, [range]);

    const fetchAll = useCallback(async () => {
        // Per "oggi" usiamo l'ora corrente precisa come "to", non la fine giornata
        const effectiveTo = range === "1" ? new Date().toISOString().slice(0, 19) : to;
        // MANAGER: ordini filtrati sulla propria sede
        const scope = managerLocationId();
        const ordersUrl = (f, t) => scope
            ? `/api/orders/location/${scope}/date-range?from=${f}&to=${t}`
            : `/api/orders/date-range?from=${f}&to=${t}`;
        try {
            const [ordCurr, ordPrev, ing] = await Promise.all([
                axiosClient.get(ordersUrl(from, effectiveTo)),
                axiosClient.get(ordersUrl(prevFrom, prevTo)),
                axiosClient.get(`/api/ingredients`),
            ]);
            setOrders(Array.isArray(ordCurr.data) ? ordCurr.data : []);
            setPrevOrders(Array.isArray(ordPrev.data) ? ordPrev.data : []);
            setIngredients(Array.isArray(ing.data) ? ing.data : []);
            setError(null);
        } catch (e) {
            setError("Errore nel caricamento dei dati.");
        } finally {
            setLoading(false);
            setRefreshing(false);
        }
    }, [from, to, prevFrom, prevTo, range]);

    // Fetch iniziale e al cambio periodo
    useEffect(() => { setLoading(true); fetchAll(); }, [fetchAll]);

    // Auto-refresh LIVE: solo su range "oggi"
    useEffect(() => {
        if (!isLive || range !== "1") return;
        const id = setInterval(() => { setRefreshing(true); fetchAll(); }, LIVE_INTERVAL_MS);
        return () => clearInterval(id);
    }, [isLive, range, fetchAll]);

    // ── Metriche derivate sul periodo ────────────────────────────────────────
    const revenue = sumRevenue(orders);
    const prevRevenue = sumRevenue(prevOrders);
    const totalOrders = orders.length;
    const prevCount = prevOrders.length;
    const avgTicket = totalOrders > 0 ? revenue / totalOrders : 0;
    const prevAvg = prevCount > 0 ? prevRevenue / prevCount : 0;

    const revDelta = deltaPct(revenue, prevRevenue);
    const revDir = revDelta == null ? "neutral" : revDelta >= 0 ? "up" : "down";
    const countDelta = deltaPct(totalOrders, prevCount);
    const countDir = countDelta == null ? "neutral" : countDelta >= 0 ? "up" : "down";
    const avgDelta = deltaPct(avgTicket, prevAvg);
    const avgDir = avgDelta == null ? "neutral" : avgDelta >= 0 ? "up" : "down";

    const rangeLabel = RANGE_OPTIONS.find((o) => o.key === range)?.label ?? "";
    const cmpLabel = range === "1" ? "vs ieri" : "vs periodo prec.";

    const revenueChart = useMemo(() => {
        const buckets = buildBuckets(range);
        const byBucket = groupByBucket(orders, range);
        return buckets.map(({ key, label }) => ({ label, revenue: sumRevenue(byBucket[key] ?? []) }));
    }, [orders, range]);

    const donutData = useMemo(() => {
        const statusMap = {};
        orders.forEach((o) => {
            const s = o.status ?? o.orderStatus ?? "UNKNOWN";
            statusMap[s] = (statusMap[s] ?? 0) + 1;
        });
        return Object.entries(statusMap).map(([status, count]) => ({
            status, count, label: DONUT_LABELS[status] ?? status, color: DONUT_COLORS[status] ?? "#8b92a8",
        }));
    }, [orders]);

    const lastOrders = useMemo(
        () => [...orders].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)).slice(0, 6),
        [orders]
    );
    const stockData = [...ingredients].sort((a, b) => (a.quantity ?? 0) - (b.quantity ?? 0)).slice(0, 6);
    const maxQty = Math.max(...ingredients.map(i => i.quantity ?? 0), 1);

    const now = new Date().toLocaleString("it-IT", { weekday: "long", day: "numeric", month: "long", hour: "2-digit", minute: "2-digit" });

    return (
        <div className="p-[28px_32px] max-w-[1440px] mx-auto">

                {/* Header */}
                <div className="flex items-end justify-between mb-6 flex-wrap gap-4">
                    <div>
                        <h1 className="font-['Syne'] text-[26px] font-extrabold tracking-tight mb-1">Command Center</h1>
                        <p className="text-[13px] text-[#8b92a8] uppercase tracking-wide">{now}</p>
                    </div>
                    <div className="flex items-center gap-3">
                        {/* Selettore arco temporale */}
                        <div className="flex items-center gap-1 bg-[#111318] border border-white/5 rounded-xl p-1">
                            {RANGE_OPTIONS.map((opt) => (
                                <button
                                    key={opt.key}
                                    onClick={() => setRange(opt.key)}
                                    className={`px-3 py-1.5 rounded-lg text-[12px] font-semibold transition-all ${range === opt.key
                                        ? "bg-[#38b6ff] text-[#111318]"
                                        : "text-[#8b92a8] hover:text-[#f0f4ff]"
                                        }`}
                                >
                                    {opt.label}
                                </button>
                            ))}
                        </div>

                        {/* Bottone LIVE — se attivato reimposta il range su oggi */}
                        <button
                            onClick={toggleLive}
                            className={`flex items-center gap-1.5 rounded-full px-3 py-1.5 text-[12px] font-semibold border transition-all ${isLive
                                ? "bg-[#22d3a0]/10 border-[#22d3a0]/25 text-[#22d3a0]"
                                : "bg-[#111318] border-white/5 text-[#8b92a8] hover:border-[#22d3a0]/30 hover:text-[#22d3a0]"
                                }`}
                        >
                            <span className={`w-1.5 h-1.5 rounded-full ${isLive ? "bg-[#22d3a0] animate-pulse" : "bg-[#8b92a8]"}`} />
                            LIVE
                        </button>

                        <button
                            onClick={() => { setRefreshing(true); fetchAll(); }}
                            className="flex items-center gap-2 bg-[#111318] border border-white/5 rounded-xl px-3.5 py-1.5 text-[12px] font-medium text-[#8b92a8] hover:border-[#38b6ff] hover:text-[#38b6ff] transition-all"
                        >
                            <RefreshCwIcon className={refreshing ? "animate-spin" : ""} />
                            Aggiorna
                        </button>
                    </div>
                </div>

                {/* KPI Grid */}
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-5">
                    <KpiCard label={`Ricavi — ${rangeLabel}`} value={loading ? "—" : fmt(revenue)} icon={<Icon.euro width="16" height="16" />} delta={revDelta ? `${revDelta}% ${cmpLabel}` : "Nessun dato"} deltaDir={revDir} accentColor="#38b6ff" />
                    <KpiCard label="Ordini Totali" value={loading ? "—" : totalOrders} icon={<Icon.clipboard width="16" height="16" />} delta={countDelta ? `${countDelta}% ${cmpLabel}` : rangeLabel} deltaDir={countDir} accentColor="#22d3a0" />
                    <KpiCard label="Scontrino Medio" value={loading ? "—" : fmt(avgTicket)} icon={<Icon.receipt width="16" height="16" />} delta={avgDelta ? `${avgDelta}% ${cmpLabel}` : `media ${rangeLabel.toLowerCase()}`} deltaDir={avgDir} accentColor="#a78bfa" />
                    <KpiCard label="Ingredienti Critici" value={loading ? "—" : ingredients.filter(i => (i.quantity ?? 0) < 20).length} icon={<Icon.alert width="16" height="16" />} delta="sotto 20%" deltaDir={ingredients.filter(i => (i.quantity ?? 0) < 20).length > 0 ? "down" : "neutral"} accentColor="#fbbf24" />
                </div>

                {/* Charts Row */}
                <div className="grid grid-cols-1 lg:grid-cols-[1fr_380px] gap-4 mb-4">
                    <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
                        <div className="flex items-center justify-between mb-5">
                            <h3 className="font-['Syne'] text-[15px] font-bold">Ricavi — {range === "1" ? "oggi (per ora)" : rangeLabel.toLowerCase()}</h3>
                            <span className="text-[11px] font-semibold px-2 py-0.5 rounded-full bg-[#38b6ff]/10 text-[#38b6ff] border border-[#38b6ff]/20">Area</span>
                        </div>
                        <div className="h-[200px]">
                            <ResponsiveContainer width="100%" height="100%">
                                <AreaChart data={revenueChart}>
                                    <defs>
                                        <linearGradient id="gradRevenue" x1="0" y1="0" x2="0" y2="1">
                                            <stop offset="5%" stopColor="#38b6ff" stopOpacity={0.25} />
                                            <stop offset="95%" stopColor="#38b6ff" stopOpacity={0} />
                                        </linearGradient>
                                    </defs>
                                    <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" vertical={false} />
                                    <XAxis dataKey="label" tick={{ fill: "#4e5566", fontSize: 11 }} axisLine={false} tickLine={false} />
                                    <YAxis tickFormatter={fmtShort} tick={{ fill: "#4e5566", fontSize: 11 }} axisLine={false} tickLine={false} width={48} />
                                    <Tooltip content={<RevenueTooltip />} />
                                    <Area type="monotone" dataKey="revenue" stroke="#38b6ff" strokeWidth={2.5} fill="url(#gradRevenue)" dot={{ fill: "#38b6ff", r: 3 }} activeDot={{ r: 5 }} />
                                </AreaChart>
                            </ResponsiveContainer>
                        </div>
                    </div>

                    <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
                        <h3 className="font-['Syne'] text-[15px] font-bold mb-5">Ordini per stato</h3>
                        {donutData.length === 0 ? (
                            <div className="flex items-center justify-center h-[160px] text-[13px] text-[#4e5566]">
                                Nessun ordine disponibile.
                            </div>
                        ) : (
                            <div className="flex items-center gap-4">
                                <div className="w-[160px] h-[160px]">
                                    <ResponsiveContainer width="100%" height="100%">
                                        <PieChart>
                                            <Pie data={donutData} innerRadius={50} outerRadius={72} paddingAngle={3} dataKey="count" strokeWidth={0}>
                                                {donutData.map((entry, i) => <Cell key={i} fill={entry.color} />)}
                                            </Pie>
                                        </PieChart>
                                    </ResponsiveContainer>
                                </div>
                                <div className="flex flex-col gap-2.5 flex-1">
                                    {donutData.map((d, i) => (
                                        <div key={i} className="flex items-center gap-2.5">
                                            <span className="w-2.5 h-2.5 rounded-[3px]" style={{ background: d.color }} />
                                            <span className="text-[13px] text-[#8b92a8] flex-1">{d.label}</span>
                                            <span className="text-[13px] font-bold font-['Syne']">{Math.round((d.count / totalOrders) * 100)}%</span>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        )}
                    </div>
                </div>

                {/* Bottom Row */}
                <div className="grid grid-cols-1 lg:grid-cols-[1fr_420px] gap-4">
                    <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
                        <div className="flex items-center justify-between mb-5">
                            <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff]">Ultimi ordini</h3>
                            <span className="text-[11px] font-semibold px-2 py-0.5 rounded-full bg-[#38b6ff]/10 text-[#38b6ff]">{rangeLabel}</span>
                        </div>
                        <table className="w-full">
                            <thead>
                                <tr className="border-b border-white/5">
                                    <th className="text-left text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3">#</th>
                                    <th className="text-left text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3">Tipo</th>
                                    <th className="text-left text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3">Pagamento</th>
                                    <th className="text-left text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3">Totale</th>
                                    <th className="text-left text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3">Stato</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-white/[0.04]">
                                {lastOrders.map((o) => (
                                    <tr key={o.id} className="hover:bg-[#181c23] transition-colors">
                                        <td className="py-3 px-3 font-['Syne'] font-semibold text-[#38b6ff] text-[13px]">#{o.orderId ?? o.id}</td>
                                        <td className="py-3 px-3 text-[#f0f4ff] font-medium text-[13px]">{o.serviceType ?? "—"}</td>
                                        <td className="py-3 px-3 text-[#8b92a8] text-[13px]">{o.paymentType ?? "—"}</td>
                                        <td className="py-3 px-3 font-['Syne'] font-semibold text-[#f0f4ff] text-[13px]">{fmt(o.totalAt)}</td>
                                        <td className="py-3 px-3">
                                            <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-bold status-${o.status}`}>
                                                {DONUT_LABELS[o.status] ?? o.status}
                                            </span>
                                        </td>
                                    </tr>
                                ))}
                                {lastOrders.length === 0 && (
                                    <tr>
                                        <td colSpan="5" className="text-center py-8 text-[13px] text-[#4e5566]">Nessun ordine disponibile.</td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>

                    <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
                        <div className="flex items-center justify-between mb-5">
                            <h3 className="font-['Syne'] text-[15px] font-bold">Stock ingredienti</h3>
                            <span className="text-[11px] font-semibold px-2 py-0.5 rounded-full bg-[#ff5e5e]/10 text-[#ff5e5e] border border-[#ff5e5e]/20">
                                {ingredients.filter(i => (i.quantity ?? 0) < 20).length} critici
                            </span>
                        </div>
                        <div className="flex flex-col gap-3.5">
                            {stockData.map((ing) => {
                                const pct = pctStock(ing.quantity ?? 0, maxQty);
                                const color = pct < 20 ? "#ff5e5e" : pct < 45 ? "#fbbf24" : "#22d3a0";
                                return (
                                    <div key={ing.id}>
                                        <div className="flex justify-between items-center mb-1.5">
                                            <span className="text-[13px] font-medium text-[#f0f4ff]">{ing.name}</span>
                                            <span className="text-[12px] font-bold" style={{ color }}>{ing.quantity} kg</span>
                                        </div>
                                        <div className="h-1.5 bg-[#181c23] rounded-full overflow-hidden">
                                            <div className="h-full rounded-full transition-all duration-700" style={{ width: `${pct}%`, backgroundColor: color }} />
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    </div>
            </div>
        </div>
    );
}

// Icona semplice per il refresh (SVG inline come nel tuo originale)
const RefreshCwIcon = ({ className }) => (
    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" className={className}>
        <polyline points="23 4 23 10 17 10" />
        <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" />
    </svg>
);
