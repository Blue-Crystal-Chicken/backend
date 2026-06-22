import { useState, useEffect, useCallback } from "react";
import orderService from "../service/OrderService";
import { locationService } from "../service/LocationService";
import productService from "../service/ProductService";
import ingredientService from "../service/IngredientService";

const RANGE_OFFSET = { "1": 0, "7": 6, "14": 13, "30": 29 };

export const RANGE_OPTIONS = [
  { key: "1",  label: "Oggi" },
  { key: "7",  label: "7 giorni" },
  { key: "14", label: "14 giorni" },
  { key: "30", label: "30 giorni" },
];

function buildDateRange(rangeKey) {
  const offset = RANGE_OFFSET[rangeKey];
  const now    = new Date();
  const from   = new Date(now);
  from.setDate(from.getDate() - offset);
  from.setHours(0, 0, 0, 0);
  const to = rangeKey === "1" ? new Date() : new Date(new Date().setHours(23, 59, 59, 999));
  return {
    from: from.toISOString().slice(0, 19),
    to:   to.toISOString().slice(0, 19),
  };
}

export function useReport() {
  const [range, setRange]           = useState("7");
  const [locationId, setLocationId] = useState(null);
  const [locations, setLocations]   = useState([]);
  const [orders, setOrders]         = useState([]);
  const [products, setProducts]     = useState([]);
  const [ingredients, setIngredients] = useState([]);
  const [loading, setLoading]       = useState(true);
  const [error, setError]           = useState(null);

  // Dati di catalogo: indipendenti da range/sede, fetch una volta sola.
  useEffect(() => {
    locationService.getAll().then((d) => setLocations(Array.isArray(d) ? d : [])).catch(() => {});
    productService.getAllProducts().then((d) => setProducts(Array.isArray(d) ? d : [])).catch(() => {});
    ingredientService.getAll().then((d) => setIngredients(Array.isArray(d) ? d : [])).catch(() => {});
  }, []);

  const fetchOrders = useCallback(async () => {
    setLoading(true);
    setError(null);
    const { from, to } = buildDateRange(range);
    try {
      const data = locationId
        ? await orderService.getByLocationAndDateRange(locationId, from, to)
        : await orderService.getByDateRange(from, to);
      setOrders(data);
    } catch {
      setError("Errore nel caricamento degli ordini.");
    } finally {
      setLoading(false);
    }
  }, [range, locationId]);

  useEffect(() => { fetchOrders(); }, [fetchOrders]);

  return {
    range, setRange,
    locationId, setLocationId,
    locations, orders, products, ingredients, loading, error,
    refresh: fetchOrders,
  };
}
