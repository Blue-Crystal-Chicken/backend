// ============================================================================
//  Risoluzione categoria -> colore per ogni articolo di un ordine.
//  - item con offerId      -> "MENU"
//  - item con productId    -> categoria dal catalogo (mappa productId->categoria)
//  - altrimenti            -> "DEFAULT"
// ============================================================================

import { CATEGORY_COLORS, CATEGORY_LABELS, RESPONSE_FIELDS as F } from "../config";

// Normalizza una stringa categoria al formato enum (HAMBURGER, ICE_CREAM, …)
export function normalizeCategory(raw) {
  if (!raw) return "DEFAULT";
  // Il backend unificato (Giuseppe) può restituire la categoria come oggetto {id,name}
  if (typeof raw === "object") raw = raw.name ?? raw.id ?? "";
  const key = String(raw).trim().toUpperCase().replace(/[\s-]+/g, "_");
  return key in CATEGORY_COLORS ? key : "DEFAULT";
}

// Costruisce la mappa productId -> categoria normalizzata dal catalogo prodotti
export function buildCategoryMap(products) {
  const map = {};
  (products || []).forEach((p) => {
    map[p[F.productId]] = normalizeCategory(p[F.productCategory]);
  });
  return map;
}

// Categoria di un singolo item dell'ordine
export function itemCategory(item, categoryMap) {
  if (item[F.itemOfferId] != null) return "MENU";
  const pid = item[F.itemProductId];
  if (pid != null && categoryMap[pid]) return categoryMap[pid];
  return "DEFAULT";
}

export const categoryColor = (key) => CATEGORY_COLORS[key] || CATEGORY_COLORS.DEFAULT;
export const categoryLabel = (key) => CATEGORY_LABELS[key] || CATEGORY_LABELS.DEFAULT;

// Categorie DISTINTE presenti in un ordine (per il bordo segmentato della card)
export function orderCategories(items, categoryMap) {
  const seen = [];
  (items || []).forEach((it) => {
    const c = itemCategory(it, categoryMap);
    if (!seen.includes(c)) seen.push(c);
  });
  return seen.length ? seen : ["DEFAULT"];
}
