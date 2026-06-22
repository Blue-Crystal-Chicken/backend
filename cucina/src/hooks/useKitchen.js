import { useState, useEffect, useCallback, useRef, useMemo } from "react";
import { fetchOrders, fetchProducts, updateOrderStatus } from "../api/kitchenApi";
import { buildCategoryMap } from "../lib/categories";
import { POLL_INTERVAL_MS, KITCHEN_STATUSES, RESPONSE_FIELDS } from "../config";

const F = RESPONSE_FIELDS;

export function useKitchen(stationLocationId = null) {
  const [orders, setOrders] = useState([]);
  const [categoryMap, setCategoryMap] = useState({});
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState(null);
  const [actionError, setActionError] = useState(null);
  const [isLive, setIsLive] = useState(true);
  const [lastUpdate, setLastUpdate] = useState(null);
  // ordini su cui è in corso un'azione (per disabilitare i bottoni / animazione uscita)
  const [busy, setBusy] = useState({});
  const abortRef = useRef(null);

  const loadOrders = useCallback(async () => {
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

  // Catalogo prodotti: una volta sola (mappa colore-categoria)
  useEffect(() => {
    let alive = true;
    fetchProducts()
      .then((prods) => alive && setCategoryMap(buildCategoryMap(prods)))
      .catch(() => {}); // i colori degradano a DEFAULT, non bloccante
    return () => {
      alive = false;
    };
  }, []);

  useEffect(() => {
    setLoading(true);
    loadOrders();
    return () => abortRef.current?.abort();
  }, [loadOrders]);

  useEffect(() => {
    if (!isLive) return;
    const id = setInterval(loadOrders, POLL_INTERVAL_MS);
    return () => clearInterval(id);
  }, [isLive, loadOrders]);

  const refresh = useCallback(() => {
    setRefreshing(true);
    loadOrders();
  }, [loadOrders]);

  const toggleLive = useCallback(() => setIsLive((v) => !v), []);

  // Azione rapida (Fatto / Cancellato): un click, update ottimistico.
  // L'ordine esce dalla lista cucina; il cambio stato viaggia verso il Backend
  // (→ Tabellone, Admin, Manager di sede).
  const act = useCallback(
    async (orderId, nextStatus) => {
      setBusy((b) => ({ ...b, [orderId]: true }));
      setActionError(null);
      const snapshot = orders;
      // rimozione ottimistica dalla lista di preparazione
      setOrders((prev) => prev.filter((o) => o[F.id] !== orderId));
      try {
        await updateOrderStatus(orderId, nextStatus);
      } catch (e) {
        setActionError(e.message || "Azione non riuscita");
        setOrders(snapshot); // rollback
      } finally {
        setBusy((b) => {
          const { [orderId]: _, ...rest } = b;
          return rest;
        });
      }
    },
    [orders]
  );

  // Solo gli ordini da preparare della PROPRIA sede, ordinati dal più vecchio.
  // Se la sede non è nota (token assente), mostra tutto (comportamento di sviluppo).
  const kitchenOrders = useMemo(() => {
    const set = new Set(KITCHEN_STATUSES);
    return orders
      .filter((o) => set.has(o[F.status]))
      .filter((o) => stationLocationId == null || Number(o[F.locationId]) === Number(stationLocationId))
      .sort((a, b) => new Date(a[F.createdAt]) - new Date(b[F.createdAt]));
  }, [orders, stationLocationId]);

  return {
    orders: kitchenOrders,
    categoryMap,
    loading,
    refreshing,
    error,
    actionError,
    isLive,
    lastUpdate,
    busy,
    refresh,
    toggleLive,
    act,
    clearActionError: () => setActionError(null),
  };
}
