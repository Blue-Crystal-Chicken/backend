import axiosClient from "../api/axiosClient";

// ============================================================================
//  RequestService (Manager) — flusso di approvazione.
//  Il Manager NON scrive direttamente PRODOTTI e MENU: invia una RICHIESTA che
//  l'Admin deve approvare. (Le OFFERTE invece le gestisce direttamente.)
//  Le immagini si caricano prima via /api/uploads/image → imgPath nel payload.
//  Backend: /api/requests + /api/uploads. Vedi ARCHITETTURA.md.
// ============================================================================

export interface ChangeRequestResponse {
  id: number;
  type: string;        // CREATE_/UPDATE_/DELETE_ PRODUCT|MENU
  status: string;      // PENDING | APPROVED | REJECTED
  targetId: number | null;
  payload: string;     // JSON serializzato
  summary: string;
  requestedById: number | null;
  requestedByEmail: string | null;
  locationId: number | null;
  locationName: string | null;
  resolvedById: number | null;
  resolutionNote: string | null;
  resultMessage: string | null;
  createdAt: string;
  resolvedAt: string | null;
}

const BASE = "/api/requests";

// Carica un'immagine e restituisce l'imgPath (filename) da mettere nel payload
async function uploadImage(file: File | null): Promise<string | null> {
  if (!file) return null;
  const fd = new FormData();
  fd.append("image", file);
  const res = await axiosClient.post<{ imgPath: string }>("/api/uploads/image", fd, {
    headers: { "Content-Type": "multipart/form-data" },
  });
  return res.data?.imgPath ?? null;
}

const requestService = {
  uploadImage,

  // Richiesta generica
  create: async (
    type: string,
    payload: unknown,
    summary?: string,
    targetId?: number | null
  ): Promise<ChangeRequestResponse> => {
    const res = await axiosClient.post<ChangeRequestResponse>(BASE, {
      type,
      payload,
      summary,
      targetId: targetId ?? null,
    });
    return res.data;
  },

  mine: async (): Promise<ChangeRequestResponse[]> => {
    const res = await axiosClient.get<ChangeRequestResponse[]>(`${BASE}/mine`);
    return res.data;
  },

  // ── PRODOTTI ────────────────────────────────────────────────────────────
  createProduct: async (form: any, file: File | null = null): Promise<ChangeRequestResponse> => {
    const imgPath = (await uploadImage(file)) ?? form.imgPath ?? null;
    const payload = buildProductPayload(form, imgPath);
    return requestService.create("CREATE_PRODUCT", payload, `Nuovo prodotto: ${form.name}`);
  },

  updateProduct: async (id: number, form: any, file: File | null = null): Promise<ChangeRequestResponse> => {
    const imgPath = (await uploadImage(file)) ?? form.imgPath ?? null;
    const payload = buildProductPayload(form, imgPath);
    return requestService.create("UPDATE_PRODUCT", payload, `Modifica prodotto: ${form.name}`, id);
  },

  deleteProduct: async (id: number, name?: string): Promise<ChangeRequestResponse> => {
    return requestService.create("DELETE_PRODUCT", { id }, `Elimina prodotto: ${name ?? "#" + id}`, id);
  },

  // ── MENU ────────────────────────────────────────────────────────────────
  createMenu: async (form: any, products: any[] = [], file: File | null = null): Promise<ChangeRequestResponse> => {
    const imgPath = (await uploadImage(file)) ?? form.imgPath ?? null;
    const payload = buildMenuPayload(form, products, imgPath);
    return requestService.create("CREATE_MENU", payload, `Nuovo menu: ${form.name}`);
  },

  updateMenu: async (id: number, form: any, products: any[] = [], file: File | null = null): Promise<ChangeRequestResponse> => {
    const imgPath = (await uploadImage(file)) ?? form.imgPath ?? null;
    const payload = buildMenuPayload(form, products, imgPath);
    return requestService.create("UPDATE_MENU", payload, `Modifica menu: ${form.name}`, id);
  },

  deleteMenu: async (id: number, name?: string): Promise<ChangeRequestResponse> => {
    return requestService.create("DELETE_MENU", { id }, `Elimina menu: ${name ?? "#" + id}`, id);
  },
};

// payload allineato a ProductRequest del backend
function buildProductPayload(form: any, imgPath: string | null) {
  return {
    name: form.name,
    price: form.price !== "" && form.price != null ? Number(form.price) : null,
    categoryName: (form.category || form.categoryName || "").toString().toUpperCase(),
    description: form.description ?? null,
    imgPath: imgPath ?? null,
  };
}

// payload allineato a MenuRequest del backend
function buildMenuPayload(form: any, products: any[], imgPath: string | null) {
  return {
    name: form.name,
    price: form.price !== "" && form.price != null ? Number(form.price) : null,
    description: form.description ?? null,
    imgPath: imgPath ?? null,
    products: (products ?? []).map((p) => ({
      productId: Number(p.productId),
      quantity: Number(p.quantity) || 1,
      obligatory: !!p.obligatory,
    })),
  };
}

export default requestService;
