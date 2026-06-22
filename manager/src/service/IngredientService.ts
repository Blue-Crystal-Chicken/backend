import axiosClient from "../api/axiosClient";

// ─── INTERFACCE DI TIPIZZAZIONE ──────────────────────────────────────────────

/**
 * Rappresenta un ingrediente del catalogo globale (IngredientEntity.java).
 * Le collezioni `products` / `locationIngredients` possono non essere
 * serializzate dal backend (lazy), quindi vanno trattate come opzionali.
 */
export interface Ingredient {
  id: number;
  name: string;
  description?: string;
  price?: number;
  quantity?: number; // giacenza globale a livello catena
  createdAt?: string;
  updatedAt?: string;
  products?: { id: number; name?: string }[];
  locationIngredients?: LocationIngredient[];
}

/** Payload di creazione/modifica ingrediente. */
export interface IngredientRequest {
  name: string;
  description?: string;
  price?: number | string;
  quantity?: number | string;
}

/** Giacenza di un ingrediente in una singola sede (LocationIngredient.java). */
export interface LocationIngredient {
  id?: { locationId: number; ingredientId: number };
  quantity: number;
  createdAt?: string;
  updatedAt?: string;
  ingredient?: Ingredient;
  location?: { id: number; city?: string; name?: string };
}

const BASE = "/api/ingredients";
const LOC = "/api/locations";

const ingredientService = {
  // ── CATALOGO GLOBALE ────────────────────────────────────────────────────

  // GET /api/ingredients
  getAll: async (): Promise<Ingredient[]> => {
    const res = await axiosClient.get<Ingredient[]>(BASE);
    return res.data;
  },

  // GET /api/ingredients/{id}
  getById: async (id: number | string): Promise<Ingredient> => {
    const res = await axiosClient.get<Ingredient>(`${BASE}/${id}`);
    return res.data;
  },

  // GET /api/ingredients/search?name=
  search: async (name: string): Promise<Ingredient[]> => {
    const res = await axiosClient.get<Ingredient[]>(`${BASE}/search`, {
      params: { name },
    });
    return res.data;
  },

  // GET /api/ingredients/low-stock?threshold=
  getLowStock: async (threshold: number): Promise<Ingredient[]> => {
    const res = await axiosClient.get<Ingredient[]>(`${BASE}/low-stock`, {
      params: { threshold },
    });
    return res.data;
  },

  // GET /api/ingredients/by-location/{locationId}
  getByLocation: async (locationId: number | string): Promise<Ingredient[]> => {
    const res = await axiosClient.get<Ingredient[]>(`${BASE}/by-location/${locationId}`);
    return res.data;
  },

  // GET /api/ingredients/by-product/{productId}
  getByProduct: async (productId: number | string): Promise<Ingredient[]> => {
    const res = await axiosClient.get<Ingredient[]>(`${BASE}/by-product/${productId}`);
    return res.data;
  },

  // POST /api/ingredients  (ADMIN)
  create: async (data: IngredientRequest): Promise<Ingredient> => {
    const res = await axiosClient.post<Ingredient>(BASE, normalize(data));
    return res.data;
  },

  // PUT /api/ingredients/{id}  (ADMIN)
  update: async (id: number | string, data: IngredientRequest): Promise<Ingredient> => {
    const res = await axiosClient.put<Ingredient>(`${BASE}/${id}`, normalize(data));
    return res.data;
  },

  // DELETE /api/ingredients/{id}  (ADMIN)
  remove: async (id: number | string): Promise<void> => {
    await axiosClient.delete(`${BASE}/${id}`);
  },

  // ── SCORTE PER SEDE ─────────────────────────────────────────────────────

  // GET /api/locations/{id}/stock
  getStockByLocation: async (locationId: number | string): Promise<LocationIngredient[]> => {
    const res = await axiosClient.get<LocationIngredient[]>(`${LOC}/${locationId}/stock`);
    return res.data;
  },

  // GET /api/locations/{id}/stock/low?threshold=
  getLowStockByLocation: async (
    locationId: number | string,
    threshold = 10
  ): Promise<LocationIngredient[]> => {
    const res = await axiosClient.get<LocationIngredient[]>(`${LOC}/${locationId}/stock/low`, {
      params: { threshold },
    });
    return res.data;
  },

  // POST /api/locations/{locationId}/stock/{ingredientId}  (ADMIN)
  addStock: async (
    locationId: number | string,
    ingredientId: number | string,
    quantity: number
  ): Promise<LocationIngredient> => {
    const res = await axiosClient.post<LocationIngredient>(
      `${LOC}/${locationId}/stock/${ingredientId}`,
      { quantity }
    );
    return res.data;
  },

  // PUT /api/locations/{locationId}/stock/{ingredientId}  (ADMIN)
  updateStock: async (
    locationId: number | string,
    ingredientId: number | string,
    quantity: number
  ): Promise<LocationIngredient> => {
    const res = await axiosClient.put<LocationIngredient>(
      `${LOC}/${locationId}/stock/${ingredientId}`,
      { quantity }
    );
    return res.data;
  },

  // DELETE /api/locations/{locationId}/stock/{ingredientId}  (ADMIN)
  removeStock: async (
    locationId: number | string,
    ingredientId: number | string
  ): Promise<void> => {
    await axiosClient.delete(`${LOC}/${locationId}/stock/${ingredientId}`);
  },
};

/** Converte i campi numerici testuali (input form) in Number per il backend. */
function normalize(data: IngredientRequest): IngredientRequest {
  return {
    name: data.name?.trim(),
    description: data.description?.trim() || undefined,
    price: data.price === "" || data.price == null ? undefined : Number(data.price),
    quantity: data.quantity === "" || data.quantity == null ? undefined : Number(data.quantity),
  };
}

export default ingredientService;
