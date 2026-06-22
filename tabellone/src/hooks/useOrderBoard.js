import { useState, useEffect, useCallback, useRef, useMemo } from "react";
import { fetchOrders, updateOrderStatus } from "../api/orderApi";
import { POLL_INTERVAL_MS, COLUMNS, RESPONSE_FIELDS } from "../config";

const F = RESPONSE_FIELDS;

export function useOrderBoard(stationLocationId = null) {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState(null);
  const [isLive, setIsLive] = useState(true);
  const [lastUpdate, setLastUpdate] = useState(null);
  const abortRef = useRef(null);

  const load = useCallback(async () => {
    abortRef.current?.abort();
    const ctrl = new AbortController();
    abortRef.current = ctrl;
    try {
      const data = await fetchOrders(ctrl.signal);
      setOrders(Array.isArray(data) ? data : []);
      setError(null);
      setLastUpdate(new Date());
    } catch (e) {
      if (e.name !== "AbortError") setError(e.message || "Errore di rete");
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => {
    setLoading(true);
    load();
    return () => abortRef.current?.abort();
  }, [load]);

  // Polling LIVE
  useEffect(() => {
    if (!isLive) return;
    const id = setInterval(load, POLL_INTERVAL_MS);
    return () => clearInterval(id);
  }, [isLive, load]);

  const refresh = useCallback(() => {
    setRefreshing(true);
    load();
  }, [load]);

  const toggleLive = useCallback(() => setIsLive((v) => !v), []);

  // Avanzamento stato con update ottimistico
  const advance = useCallback(
    async (orderId, nextStatus) => {
      setOrders((prev) =>
        prev.map((o) =>
          o[F.id] === orderId ? { ...o, [F.status]: nextStatus } : o
        )
      );
      try {
        await updateOrderStatus(orderId, nextStatus);
      } catch {
        load(); // rollback dal server in caso di errore
      }
    },
    [load]
  );

  // Raggruppa gli ordini nelle colonne configurate
  const columns = useMemo(() => {
    return COLUMNS.map((col) => {
      const set = new Set(col.statuses);
      const list = orders
        .filter((o) => set.has(o[F.status]))
        // Solo la sede di questo tabellone (se nota dal token); altrimenti tutte.
        .filter((o) => stationLocationId == null || Number(o[F.locationId]) === Number(stationLocationId))
        .sort((a, b) => new Date(a[F.createdAt]) - new Date(b[F.createdAt]));
      return { ...col, orders: list };
    });
  }, [orders, stationLocationId]);

  return {
    columns,
    loading,
    refreshing,
    error,
    isLive,
    lastUpdate,
    refresh,
    toggleLive,
    advance,
  };
}
