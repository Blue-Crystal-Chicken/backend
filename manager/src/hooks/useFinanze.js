import { useState, useEffect, useCallback, useMemo } from "react";
import orderService from "../service/OrderService";
import { locationService } from "../service/LocationService";
import { managerLocationId } from "../lib/managerScope";

export const SEDE_COLORS = ["#38b6ff", "#22d3a0", "#a78bfa"];

export const CANALE_COLORS = {
  DINE_IN:  "#38b6ff",
  TAKEAWAY: "#22d3a0",
  DELIVERY: "#a78bfa",
};

export const CANALE_LABELS = {
  DINE_IN:  "Sala",
  TAKEAWAY: "Asporto",
  DELIVERY: "Consegna",
};

const RANGE_OFFSET = { "1": 0, "7": 6, "14": 13, "30": 29 };

const LIVE_INTERVAL_MS = 60_000; // 60 secondi

// ── Date helpers ──────────────────────────────────────────────────────────────

function computeDateRanges(rangeKey) {
  const offset = RANGE_OFFSET[rangeKey];
  const now    = new Date();

  const from = new Date(now);
  from.setDate(from.getDate() - offset);
  from.setHours(0, 0, 0, 0);

  // Per "oggi": to = fine giornata statica per il memo; in fetchAll viene
  // sostituito con l'ora corrente precisa al momento della chiamata.
  const to = new Date(now);
  to.setHours(23, 59, 59, 999);

  const prevTo   = new Date(from.getTime() - 1);
  const prevFrom = new Date(prevTo);
  prevFrom.setDate(prevTo.getDate() - offset);
  prevFrom.setHours(0, 0, 0, 0);

  return {
    from:     from.toISOString().slice(0, 19),
    to:       to.toISOString().slice(0, 19),
    prevFrom: prevFrom.toISOString().slice(0, 19),
    prevTo:   prevTo.toISOString().slice(0, 19),
  };
}

// ── Bucketing ─────────────────────────────────────────────────────────────────

/**
 * Costruisce la lista di bucket per il grafico.
 * "oggi" → un bucket per ogni ora trascorsa da mezzanotte ad adesso.
 * altri  → un bucket per ogni giorno nel range.
 */
function buildBuckets(rangeKey) {
  if (rangeKey === "1") {
    const now         = new Date();
    const currentHour = now.getHours();
    const buckets     = [];
    for (let h = 0; h <= currentHour; h++) {
      buckets.push({
        key:   `h${h}`,                              // "h0", "h1", …
        label: `${String(h).padStart(2, "0")}:00`,   // "00:00", "01:00", …
      });
    }
    return buckets;
  }

  // Risoluzione giornaliera per 7 / 14 / 30 giorni
  const totalDays = RANGE_OFFSET[rangeKey] + 1;
  const buckets   = [];
  for (let i = totalDays - 1; i >= 0; i--) {
    const d = new Date();
    d.setDate(d.getDate() - i);
    d.setHours(0, 0, 0, 0);
    buckets.push({
      key:   d.toDateString(),
      label: d.toLocaleDateString("it-IT", { weekday: "short", day: "numeric" }),
    });
  }
  return buckets;
}

/**
 * Raggruppa gli ordini per bucket (orario o giornaliero a seconda del range).
 */
function groupByBucket(orders, rangeKey) {
  const map = {};
  if (rangeKey === "1") {
    orders.forEach((o) => {
      const k = `h${new Date(o.createdAt).getHours()}`;
      if (!map[k]) map[k] = [];
      map[k].push(o);
    });
  } else {
    orders.forEach((o) => {
      const k = new Date(o.createdAt).toDateString();
      if (!map[k]) map[k] = [];
      map[k].push(o);
    });
  }
  return map;
}

// ── Helpers ───────────────────────────────────────────────────────────────────

function sumRevenue(orders) {
  return orders.reduce((s, o) => s + (o.totalAt ?? 0), 0);
}

function deltaPct(curr, prev) {
  if (prev <= 0) return null;
  return ((curr - prev) / prev * 100).toFixed(1);
}

// ── Hook ──────────────────────────────────────────────────────────────────────

export function useFinanze() {
  // MANAGER: la sede primaria è fissa sulla sua sede, niente confronti.
  const scope = managerLocationId();
  const [primaryId, setPrimaryId]     = useState(scope);
  const [comparedIds, setComparedIds] = useState([]);
  const [range, setRange]             = useState("7");
  const [ordersMap, setOrdersMap]     = useState({});
  const [prevOrdersMap, setPrevOrdersMap] = useState({});
  const [locations, setLocations]     = useState([]);
  const [loading, setLoading]         = useState(true);
  const [refreshing, setRefreshing]   = useState(false);
  const [error, setError]             = useState(null);
  const [isLive, setIsLive]           = useState(false);

  const { from, to, prevFrom, prevTo } = useMemo(() => computeDateRanges(range), [range]);

  const handlePrimaryChange = useCallback((id) => {
    if (scope) return; // manager: sede bloccata
    setPrimaryId(id);
    setComparedIds([]);
  }, [scope]);

  const toggleComparison = useCallback((id) => {
    if (scope) return; // manager: niente confronti tra sedi
    setComparedIds((prev) => {
      if (prev.includes(id)) return prev.filter((x) => x !== id);
      if (prev.length >= 2)  return prev;
      return [...prev, id];
    });
  }, [scope]);

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
    const ids     = primaryId === null ? ["all"] : [primaryId, ...comparedIds];
    const prevKey = primaryId ?? "all";

    // Per "oggi" usiamo l'ora corrente precisa come "to", non la fine giornata
    const effectiveTo = range === "1"
      ? new Date().toISOString().slice(0, 19)
      : to;

    try {
      const currentFetches = ids.map((id) =>
        id === "all"
          ? orderService.getByDateRange(from, effectiveTo).then((data) => [id, data])
          : orderService.getByLocationAndDateRange(id, from, effectiveTo).then((data) => [id, data])
      );

      const prevFetch =
        prevKey === "all"
          ? orderService.getByDateRange(prevFrom, prevTo).then((data) => [prevKey, data])
          : orderService.getByLocationAndDateRange(prevKey, prevFrom, prevTo).then((data) => [prevKey, data]);

      const [currentResults, [pk, prevData]] = await Promise.all([
        Promise.all(currentFetches),
        prevFetch,
      ]);

      setOrdersMap(Object.fromEntries(currentResults));
      setPrevOrdersMap({ [pk]: prevData });
      setError(null);
    } catch {
      setError("Errore nel caricamento dei dati finanziari.");
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [primaryId, comparedIds, from, to, prevFrom, prevTo, range]);

  // Fetch iniziale e al cambio filtri
  useEffect(() => {
    setLoading(true);
    fetchAll();
  }, [fetchAll]);

  // Auto-refresh LIVE: si attiva solo su range "oggi"
  useEffect(() => {
    if (!isLive || range !== "1") return;
    const id = setInterval(() => {
      setRefreshing(true);
      fetchAll();
    }, LIVE_INTERVAL_MS);
    return () => clearInterval(id);
  }, [isLive, range, fetchAll]);

  // Locations (caricato una volta sola; per il manager solo la sua sede)
  useEffect(() => {
    locationService.getAll()
      .then((d) => setLocations(scope ? (d || []).filter((l) => l.id === scope) : (d || [])))
      .catch(() => {});
  }, [scope]);

  // ── Derived data ──────────────────────────────────────────────────────────

  const kpi = useMemo(() => {
    const pk       = primaryId ?? "all";
    const orders   = ordersMap[pk]     ?? [];
    const prevOrds = prevOrdersMap[pk] ?? [];

    const revenue     = sumRevenue(orders);
    const prevRevenue = sumRevenue(prevOrds);
    const count       = orders.length;
    const prevCount   = prevOrds.length;
    const avgTicket   = count > 0 ? revenue / count : 0;
    const prevAvg     = prevCount > 0 ? prevRevenue / prevCount : 0;

    const canaleMap = {};
    orders.forEach((o) => {
      const t = o.serviceType ?? "ALTRO";
      canaleMap[t] = (canaleMap[t] ?? 0) + (o.totalAt ?? 0);
    });
    const topEntry     = Object.entries(canaleMap).sort((a, b) => b[1] - a[1])[0];
    const topCanale    = topEntry?.[0] ?? null;
    const topCanalePct = revenue > 0 && topEntry
      ? ((topEntry[1] / revenue) * 100).toFixed(0)
      : null;

    return {
      revenue,    revDelta:   deltaPct(revenue, prevRevenue),
      count,      countDelta: deltaPct(count, prevCount),
      avgTicket,  avgDelta:   deltaPct(avgTicket, prevAvg),
      topCanale,  topCanalePct,
    };
  }, [ordersMap, prevOrdersMap, primaryId]);

  // Trend: una serie per sede, risoluzione adattiva per range
  const trendData = useMemo(() => {
    const ids     = primaryId === null ? ["all"] : [primaryId, ...comparedIds];
    const buckets = buildBuckets(range);
    const byBucketMap = {};
    ids.forEach((id) => {
      byBucketMap[id] = groupByBucket(ordersMap[id] ?? [], range);
    });
    return buckets.map(({ key, label }) => {
      const point = { label };
      ids.forEach((id) => {
        point[`loc_${id}`] = sumRevenue(byBucketMap[id][key] ?? []);
      });
      return point;
    });
  }, [ordersMap, primaryId, comparedIds, range]);

  // Canale: 3 linee (DINE_IN / TAKEAWAY / DELIVERY) per sede primaria
  const canaleData = useMemo(() => {
    const pk      = primaryId ?? "all";
    const buckets = buildBuckets(range);
    const byBucket = groupByBucket(ordersMap[pk] ?? [], range);
    return buckets.map(({ key, label }) => {
      const bOrders = byBucket[key] ?? [];
      return {
        label,
        DINE_IN:  sumRevenue(bOrders.filter((o) => o.serviceType === "DINE_IN")),
        TAKEAWAY: sumRevenue(bOrders.filter((o) => o.serviceType === "TAKEAWAY")),
        DELIVERY: sumRevenue(bOrders.filter((o) => o.serviceType === "DELIVERY")),
      };
    });
  }, [ordersMap, primaryId, range]);

  // Confronto sedi (solo quando ci sono sedi comparate)
  const confrontoData = useMemo(() => {
    if (primaryId === null || comparedIds.length === 0) return [];
    return [primaryId, ...comparedIds].map((id, idx) => {
      const orders    = ordersMap[id] ?? [];
      const revenue   = sumRevenue(orders);
      const count     = orders.length;
      const avgTicket = count > 0 ? revenue / count : 0;
      const loc       = locations.find((l) => l.id === id);
      return {
        id,   color: SEDE_COLORS[idx],
        name: loc?.name ?? `Sede #${id}`,
        revenue, count, avgTicket,
      };
    });
  }, [ordersMap, primaryId, comparedIds, locations]);

  const locationName = useCallback(
    (id) => {
      if (id === null || id === "all") return "Tutte le sedi";
      return locations.find((l) => l.id === id)?.name ?? `Sede #${id}`;
    },
    [locations]
  );

  const activeIds = primaryId === null ? ["all"] : [primaryId, ...comparedIds];

  return {
    primaryId,    handlePrimaryChange,
    comparedIds,  toggleComparison,
    range,        setRange,
    isLive,       toggleLive,
    locations,    loading, refreshing, error,
    activeIds,
    kpi,          trendData, canaleData, confrontoData,
    locationName,
    refresh: () => { setRefreshing(true); fetchAll(); },
  };
}
