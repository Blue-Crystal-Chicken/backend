import axiosClient from "../api/axiosClient";

// ─── INTERFACCE DI TIPIZZAZIONE ──────────────────────────────────────────────

export interface OfferProduct {
  productId: number;
  productName: string;
  quantity: number;
  unitPrice?: number;
}

export interface OfferMenu {
  id: number;
  name: string;
  price?: number;
}

/** OfferResponse.java (esteso con sconto/date/active). */
export interface Offer {
  id: number;
  name: string;
  description?: string;
  imgPath?: string;
  price?: number;
  discountPercentage?: number;
  startDate?: string;
  endDate?: string;
  active?: boolean;
  menus?: OfferMenu[];
  offerProducts?: OfferProduct[];
  updatedAt?: string;
}

/** Campi del form offerta (lato UI). */
export interface OfferFormData {
  name: string;
  description?: string;
  price?: number | string;
  discountPercentage?: number | string;
  startDate?: string; // "yyyy-MM-ddTHH:mm" (datetime-local)
  endDate?: string;
  active?: boolean;
  menuIds?: number[];
}

const BASE = "/api/offers";

// Costruisce il FormData multipart per create/update (@ModelAttribute lato BE)
function buildFormData(data: OfferFormData, image: File | null): FormData {
  const fd = new FormData();
  fd.append("name", data.name);
  if (data.description != null) fd.append("description", data.description);
  if (data.price !== "" && data.price != null) fd.append("price", String(data.price));
  if (data.discountPercentage !== "" && data.discountPercentage != null)
    fd.append("discountPercentage", String(data.discountPercentage));
  if (data.startDate) fd.append("startDate", data.startDate);
  if (data.endDate) fd.append("endDate", data.endDate);
  if (data.active != null) fd.append("active", String(data.active));
  if (data.menuIds) data.menuIds.forEach((id) => fd.append("menuIds", String(id)));
  if (image) fd.append("image", image);
  return fd;
}

const offerService = {
  // GET /api/offers
  getAll: async (): Promise<Offer[]> => {
    const res = await axiosClient.get<Offer[]>(BASE);
    return res.data;
  },

  // GET /api/offers/{id}
  getById: async (id: number | string): Promise<Offer> => {
    const res = await axiosClient.get<Offer>(`${BASE}/${id}`);
    return res.data;
  },

  // GET /api/offers/search?name=
  search: async (name: string): Promise<Offer[]> => {
    const res = await axiosClient.get<Offer[]>(`${BASE}/search`, { params: { name } });
    return res.data;
  },

  // GET /api/offers/{id}/products
  getProducts: async (id: number | string): Promise<OfferProduct[]> => {
    const res = await axiosClient.get<OfferProduct[]>(`${BASE}/${id}/products`);
    return res.data;
  },

  // POST /api/offers  (ADMIN, multipart)
  create: async (data: OfferFormData, image: File | null = null): Promise<Offer> => {
    const res = await axiosClient.post<Offer>(BASE, buildFormData(data, image), {
      headers: { "Content-Type": "multipart/form-data" },
    });
    return res.data;
  },

  // PUT /api/offers/{id}  (ADMIN, multipart)
  update: async (id: number | string, data: OfferFormData, image: File | null = null): Promise<Offer> => {
    const res = await axiosClient.put<Offer>(`${BASE}/${id}`, buildFormData(data, image), {
      headers: { "Content-Type": "multipart/form-data" },
    });
    return res.data;
  },

  // POST /api/offers/{offerId}/products/{productId}  (ADMIN)
  addProduct: async (offerId: number | string, productId: number | string, quantity: number): Promise<OfferProduct> => {
    const res = await axiosClient.post<OfferProduct>(`${BASE}/${offerId}/products/${productId}`, { quantity });
    return res.data;
  },

  // DELETE /api/offers/{offerId}/products/{productId}  (ADMIN)
  removeProduct: async (offerId: number | string, productId: number | string): Promise<void> => {
    await axiosClient.delete(`${BASE}/${offerId}/products/${productId}`);
  },

  // POST /api/offers/{offerId}/menus/{menuId}  (ADMIN)  — nota: il BE non espone la rimozione menu
  addMenu: async (offerId: number | string, menuId: number | string): Promise<Offer> => {
    const res = await axiosClient.post<Offer>(`${BASE}/${offerId}/menus/${menuId}`, {});
    return res.data;
  },

  // DELETE /api/offers/{id}  (ADMIN)
  remove: async (id: number | string): Promise<void> => {
    await axiosClient.delete(`${BASE}/${id}`);
  },
};

export default offerService;
