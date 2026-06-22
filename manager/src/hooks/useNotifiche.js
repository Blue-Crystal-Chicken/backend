import { useState, useEffect, useCallback, useMemo, useRef } from "react";
import notificationService from "../service/NotificationService";
import { managerLocationId } from "../lib/managerScope";

const POLL_INTERVAL_MS = 15_000; // 15 secondi

export const LEVEL_META = {
  INFO: { label: "Info", color: "#38b6ff" },
  SUCCESS: { label: "Successo", color: "#22d3a0" },
  WARNING: { label: "Attenzione", color: "#fbbf24" },
  ERROR: { label: "Errore", color: "#ff5e5e" },
};

export function useNotifiche() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState(null);
  const [isLive, setIsLive] = useState(true);
  const silent = useRef(false);

  const fetchAll = useCallback(async () => {
    try {
      const data = await notificationService.getAll();
      // MANAGER: mostra solo le notifiche della sua sede + quelle globali (locationId null)
      const scope = managerLocationId();
      const list = (data || []).filter(
        (n) => n.locationId == null || n.locationId === scope
      );
      setItems(list);
      setError(null);
    } catch (e) {
      setError("Errore nel caricamento delle notifiche.");
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => { setLoading(true); fetchAll(); }, [fetchAll]);

  // Polling automatico (LIVE) — refresh silenzioso, senza spinner globale
  useEffect(() => {
    if (!isLive) return;
    const id = setInterval(() => { silent.current = true; fetchAll(); }, POLL_INTERVAL_MS);
    return () => clearInterval(id);
  }, [isLive, fetchAll]);

  const refresh = useCallback(() => { setRefreshing(true); fetchAll(); }, [fetchAll]);
  const toggleLive = useCallback(() => setIsLive((v) => !v), []);

  const unreadCount = useMemo(() => items.filter((n) => !n.readFlag).length, [items]);

  // ── Azioni con update ottimistico ──────────────────────────────────────────
  const markRead = useCallback(async (id) => {
    setItems((prev) => prev.map((n) => (n.id === id ? { ...n, readFlag: true } : n)));
    try { await notificationService.markRead(id); } catch { fetchAll(); }
  }, [fetchAll]);

  const markAllRead = useCallback(async () => {
    setItems((prev) => prev.map((n) => ({ ...n, readFlag: true })));
    try { await notificationService.markAllRead(); } catch { fetchAll(); }
  }, [fetchAll]);

  const remove = useCallback(async (id) => {
    setItems((prev) => prev.filter((n) => n.id !== id));
    try { await notificationService.remove(id); } catch { fetchAll(); }
  }, [fetchAll]);

  const clearAll = useCallback(async () => {
    setItems([]);
    try { await notificationService.clearAll(); } catch { fetchAll(); }
  }, [fetchAll]);

  return {
    items, loading, refreshing, error, isLive,
    unreadCount, refresh, toggleLive,
    markRead, markAllRead, remove, clearAll,
  };
}
