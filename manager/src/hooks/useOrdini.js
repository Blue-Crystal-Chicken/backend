import { useState, useEffect, useCallback, useMemo } from "react";
import orderService from "../service/OrderService";
import { locationService } from "../service/LocationService";
import { managerLocationId } from "../lib/managerScope";

export const STATUS_COLORS = {
  PENDING:    "#fbbf24",
  PREPARING:  "#38b6ff",
  READY:      "#22d3a0",
  DELIVERED:  "#a78bfa",
  CANCELLED:  "#ff5e5e",
};

export const STATUS_LABELS = {
  PENDING:    "In attesa",
  PREPARING:  "In preparazione",
  READY:      "Pronto",
  DELIVERED:  "Consegnato",
  CANCELLED:  "Annullato",
};

export const SERVICE_TYPE_COLORS = {
  DINE_IN:  "#38b6ff",
  TAKEAWAY: "#22d3a0",
  DELIVERY: "#a78bfa",
};

export const SERVICE_TYPE_LABELS = {
  DINE_IN:  "Sala",
  TAKEAWAY: "Asporto",
  DELIVERY: "Consegna",
};

// Giorni da sottrarre a "oggi" per ottenere il from di ogni range
const RANGE_OFFSET = { "1": 0, "7": 6, "14": 13, "30": 29 };

function buildDateRange(rangeKey) {
  const now = new Date();
  const to = new Date(now);
  to.setHours(23, 59, 59, 999);
  const from = new Date(now);
  from.setDate(now.getDate() - RANGE_OFFSET[rangeKey]);
  from.setHours(0, 0, 0, 0);
  return {
    from: from.toISOString().slice(0, 19),
    to:   to.toISOString().slice(0, 19),
  };
}

function buildDailyData(orders, rangeKey) {
  const totalDays = rangeKey === "1" ? 1 : RANGE_OFFSET[rangeKey] + 1;
  const map = {};
  for (let i = totalDays - 1; i >= 0; i--) {
    const d = new Date();
    d.setDate(d.getDate() - i);
    d.setHours(0, 0, 0, 0);
    map[d.toDateString()] = {
      label:   d.toLocaleDateString("it-IT", { weekday: "short", day: "numeric" }),
      count:   0,
      revenue: 0,
    };
  }
  orders.forEach((o) => {
    const key = new Date(o.createdAt).toDateString();
    if (map[key]) {
      map[key].count   += 1;
      map[key].revenue += o.totalAt ?? 0;
    }
  });
  return Object.values(map);
}

export function useOrdini() {
  // MANAGER: bloccato sulla propria sede (scope). setLocationId è no-op se assegnata.
  const scope = managerLocationId();
  const [locationId, setLocationIdRaw] = useState(scope); // sede del manager
  const setLocationId = scope ? () => {} : setLocationIdRaw;
  const [range, setRange]           = useState("7");
  const [orders, setOrders]         = useState([]);
  const [locations, setLocations]   = useState([]);
  const [loading, setLoading]       = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError]           = useState(null);
  const [selectedOrder, setSelectedOrder] = useState(null);

  const { from, to } = useMemo(() => buildDateRange(range), [range]);

  const fetchOrders = useCallback(async () => {
    try {
      const data = locationId
        ? await orderService.getByLocationAndDateRange(locationId, from, to)
        : await orderService.getByDateRange(from, to);
      setOrders(data);
      setError(null);
    } catch {
      setError("Errore nel caricamento degli ordini.");
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [locationId, from, to]);

  useEffect(() => {
    setLoading(true);
    fetchOrders();
  }, [fetchOrders]);

  useEffect(() => {
    locationService.getAll()
      .then((d) => setLocations(scope ? (d || []).filter((l) => l.id === scope) : (d || [])))
      .catch(() => {});
  }, [scope]);

  const kpi = useMemo(() => {
    const total     = orders.length;
    const revenue   = orders.reduce((s, o) => s + (o.totalAt ?? 0), 0);
    const cancelled = orders.filter((o) => o.status === "CANCELLED").length;
    const avgTicket = total > 0 ? revenue / total : 0;
    const cancelledPct = total > 0 ? ((cancelled / total) * 100).toFixed(1) : "0.0";
    return { total, revenue, cancelled, cancelledPct, avgTicket };
  }, [orders]);

  const dailyData = useMemo(() => buildDailyData(orders, range), [orders, range]);

  const serviceTypeData = useMemo(() => {
    const map = {};
    orders.forEach((o) => {
      const t = o.serviceType ?? "ALTRO";
      map[t] = (map[t] ?? 0) + 1;
    });
    return Object.entries(map).map(([type, count]) => ({
      type,
      count,
      label: SERVICE_TYPE_LABELS[type] ?? type,
      color: SERVICE_TYPE_COLORS[type] ?? "#8b92a8",
    }));
  }, [orders]);

  const locationName = useCallback(
    (locId) => locations.find((l) => l.id === locId)?.name ?? (locId ? `Sede #${locId}` : "—"),
    [locations]
  );

  const updateStatus = useCallback(
    async (id, status) => {
      await orderService.updateStatus(id, status);
      setOrders((prev) => prev.map((o) => (o.id === id ? { ...o, status } : o)));
      setSelectedOrder((prev) => (prev?.id === id ? { ...prev, status } : prev));
    },
    []
  );

  const deleteOrder = useCallback(async (id) => {
    await orderService.delete(id);
    setOrders((prev) => prev.filter((o) => o.id !== id));
  }, []);

  return {
    orders,
    locations,
    loading,
    refreshing,
    error,
    locationId,
    setLocationId,
    range,
    setRange,
    kpi,
    dailyData,
    serviceTypeData,
    locationName,
    selectedOrder,
    setSelectedOrder,
    refresh: () => { setRefreshing(true); fetchOrders(); },
    updateStatus,
    deleteOrder,
  };
}
