import { useState, useEffect, useCallback, useMemo } from "react";
import offerService from "../service/OfferService";
import productService from "../service/ProductService";
import pushNotificationService from "../service/PushNotificationService";
import { notifyPushSent, notifyPushError } from "../components/ui/PushToast";
import axiosClient from "../api/axiosClient";

// ── Stato derivato di un'offerta in base a flag + finestra di validità ────────
export function offerStatus(o) {
  const now = new Date();
  if (o.active === false) return "DISATTIVATA";
  if (o.startDate && new Date(o.startDate) > now) return "PROGRAMMATA";
  if (o.endDate && new Date(o.endDate) < now) return "SCADUTA";
  return "ATTIVA";
}

export const STATUS_META = {
  ATTIVA: { label: "Attiva", color: "#22d3a0" },
  PROGRAMMATA: { label: "Programmata", color: "#38b6ff" },
  SCADUTA: { label: "Scaduta", color: "#8b92a8" },
  DISATTIVATA: { label: "Disattivata", color: "#ff5e5e" },
};

export function useMarketing() {
  const [offers, setOffers] = useState([]);
  const [products, setProducts] = useState([]);
  const [menus, setMenus] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState(null);

  const fetchAll = useCallback(async () => {
    try {
      const [offs, prods, menusRes] = await Promise.all([
        offerService.getAll(),
        productService.getAllProducts().catch(() => []),
        axiosClient.get("/api/menus").then((r) => r.data).catch(() => []),
      ]);
      setOffers(offs || []);
      setProducts(prods || []);
      setMenus(menusRes || []);
      setError(null);
    } catch {
      setError("Errore nel caricamento delle offerte.");
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => { setLoading(true); fetchAll(); }, [fetchAll]);

  const refresh = useCallback(() => { setRefreshing(true); fetchAll(); }, [fetchAll]);

  // Mappa prodotto → prezzo a listino (per calcolare il risparmio reale del bundle)
  const priceById = useMemo(() => {
    const map = {};
    products.forEach((p) => { map[p.id] = p.price ?? 0; });
    return map;
  }, [products]);

  // Valore a listino del bundle di un'offerta (Σ qty × prezzo prodotto)
  const listValueOf = useCallback(
    (o) => (o.offerProducts ?? []).reduce((s, op) => s + (op.quantity ?? 1) * (priceById[op.productId] ?? 0), 0),
    [priceById]
  );

  // ── KPI "mondo offerte" ───────────────────────────────────────────────────
  const kpi = useMemo(() => {
    const total = offers.length;
    const active = offers.filter((o) => offerStatus(o) === "ATTIVA").length;

    const priced = offers.filter((o) => o.price != null);
    const avgPrice = priced.length ? priced.reduce((s, o) => s + o.price, 0) / priced.length : 0;

    // Prodotti distinti coinvolti in almeno un'offerta
    const productSet = new Set();
    offers.forEach((o) => (o.offerProducts ?? []).forEach((op) => productSet.add(op.productId)));
    const productsInPromo = productSet.size;

    // Risparmio medio: media (valore listino − prezzo offerta) sulle offerte calcolabili
    const savings = offers
      .map((o) => ({ list: listValueOf(o), price: o.price ?? 0 }))
      .filter((x) => x.list > 0)
      .map((x) => x.list - x.price);
    const avgSaving = savings.length ? savings.reduce((s, v) => s + v, 0) / savings.length : 0;

    // Sconto medio dichiarato (campo discountPercentage)
    const discounts = offers.filter((o) => o.discountPercentage != null).map((o) => o.discountPercentage);
    const avgDiscount = discounts.length ? discounts.reduce((s, v) => s + v, 0) / discounts.length : 0;

    return { total, active, avgPrice, productsInPromo, avgSaving, avgDiscount };
  }, [offers, listValueOf]);

  // ── Azioni CRUD (poi refresh) ───────────────────────────────────────────────
  // OFFERTE: il Manager le gestisce DIRETTAMENTE (niente approvazione admin).
  const createOffer = useCallback(async (data, image, productLines) => {
    const created = await offerService.create(data, image);
    // Associa i prodotti del bundle dopo la creazione (endpoint separato)
    for (const line of productLines ?? []) {
      if (line.productId) await offerService.addProduct(created.id, line.productId, Number(line.quantity) || 1);
    }
    // Notifica push (non blocca: fire-and-forget)
    const pushRes = await pushNotificationService.notifyEvent({
      title: "Nuova offerta",
      body: `L'offerta "${data.name}" è stata creata.`,
      type: "NEW_OFFER",
      payload: { name: data.name },
    });
    if (pushRes.ok) notifyPushSent("Notifica push inviata: nuova offerta");
    else notifyPushError(`Notifica offerta non inviata: ${pushRes.error}`);
    await fetchAll();
    return created;
  }, [fetchAll]);

  const updateOffer = useCallback(async (id, data, image) => {
    const res = await offerService.update(id, data, image);
    await fetchAll();
    return res;
  }, [fetchAll]);

  const deleteOffer = useCallback(async (id) => {
    await offerService.remove(id);
    await fetchAll();
  }, [fetchAll]);

  const addProductToOffer = useCallback(async (offerId, productId, quantity) => {
    await offerService.addProduct(offerId, productId, Number(quantity) || 1);
    await fetchAll();
  }, [fetchAll]);

  const removeProductFromOffer = useCallback(async (offerId, productId) => {
    await offerService.removeProduct(offerId, productId);
    await fetchAll();
  }, [fetchAll]);

  const addMenuToOffer = useCallback(async (offerId, menuId) => {
    await offerService.addMenu(offerId, menuId);
    await fetchAll();
  }, [fetchAll]);

  return {
    offers, products, menus,
    loading, refreshing, error,
    kpi, listValueOf, refresh,
    createOffer, updateOffer, deleteOffer,
    addProductToOffer, removeProductFromOffer, addMenuToOffer,
  };
}
