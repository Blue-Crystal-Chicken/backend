import axiosClient from "../api/axiosClient";

// ─── INTERFACCE DI TIPIZZAZIONE ──────────────────────────────────────────────

/** MenuProductResponse.java — riga prodotto all'interno di un menu. */
export interface MenuProduct {
  productId: number;
  productName: string;
  quantity: number;
  obligatory: boolean;
  unitPrice?: number;
}

/** MenuResponse.java */
export interface Menu {
  id: number;
  name: string;
  price?: number;
  description?: string;
  imgPath?: string;
  menuProducts?: MenuProduct[];
  updatedAt?: string;
}

/** Riga prodotto selezionata nel form di creazione menu. */
export interface MenuProductFormItem {
  productId: number;
  quantity: number;
  obligatory: boolean;
}

/** Campi del form menu (lato UI). */
export interface MenuFormData {
  name: string;
  price?: number | string;
  description?: string;
  products?: MenuProductFormItem[];
}

const BASE = "/api/menus";

const menuService = {
  // GET /api/menus
  getAll: async (): Promise<Menu[]> => {
    const res = await axiosClient.get<Menu[]>(BASE);
    return res.data;
  },

  // GET /api/menus/{id}
  getById: async (id: number | string): Promise<Menu> => {
    const res = await axiosClient.get<Menu>(`${BASE}/${id}`);
    return res.data;
  },

  // GET /api/menus/{id}/products
  getProducts: async (id: number | string): Promise<MenuProduct[]> => {
    const res = await axiosClient.get<MenuProduct[]>(`${BASE}/${id}/products`);
    return res.data;
  },

  // POST /api/menus  (ADMIN, multipart) — crea il menu base
  create: async (data: MenuFormData, image: File | null = null): Promise<Menu> => {
    const fd = new FormData();
    fd.append("name", data.name);
    if (data.price !== "" && data.price != null) fd.append("price", String(data.price));
    if (data.description != null) fd.append("description", data.description);
    if (image) fd.append("image", image);
    const res = await axiosClient.post<Menu>(BASE, fd, {
      headers: { "Content-Type": "multipart/form-data" },
    });
    return res.data;
  },

  // POST /api/menus/{menuId}/products/{productId}  (ADMIN)
  addProduct: async (
    menuId: number | string,
    productId: number | string,
    quantity: number,
    obligatory: boolean
  ): Promise<MenuProduct> => {
    const res = await axiosClient.post<MenuProduct>(`${BASE}/${menuId}/products/${productId}`, {
      quantity,
      obligatory,
    });
    return res.data;
  },

  // DELETE /api/menus/{menuId}/products/{productId}  (ADMIN)
  removeProduct: async (menuId: number | string, productId: number | string): Promise<void> => {
    await axiosClient.delete(`${BASE}/${menuId}/products/${productId}`);
  },

  // DELETE /api/menus/{id}  (ADMIN)
  remove: async (id: number | string): Promise<void> => {
    await axiosClient.delete(`${BASE}/${id}`);
  },

  // Helper UI: crea il menu e poi aggancia i prodotti selezionati.
  createWithProducts: async (data: MenuFormData, image: File | null = null): Promise<Menu> => {
    const menu = await menuService.create(data, image);
    if (data.products?.length) {
      for (const p of data.products) {
        await menuService.addProduct(menu.id, p.productId, p.quantity, p.obligatory);
      }
    }
    return menuService.getById(menu.id);
  },
};

export default menuService;
