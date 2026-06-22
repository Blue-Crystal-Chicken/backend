import axiosClient from "../api/axiosClient";

// ─── INTERFACCE DI TIPIZZAZIONE (ENTERPRISE STANDARD) ────────────────────────

/**
 * Rappresenta la risposta del backend (ProductResponse.java)
 */
export interface ProductResponse {
  id: number;
  name: string;
  price: number;
  category: string;
  imageUrl?: string; // Se il tuo backend restituisce l'URL dell'immagine salvata
}

/**
 * Rappresenta i dati testuali richiesti per la creazione/modifica (ProductRequest.java)
 */
export interface ProductRequest {
  name: string;
  price: number | string;
  category: string;
}

// Corrisponde esattamente all'annotazione @RequestMapping("/api/products") del controller
const BASE_URL = "/api/products"; 

const productService = {
  
  // GET /api/products/v1/products
  getAllProducts: async (): Promise<ProductResponse[]> => {
    const response = await axiosClient.get<ProductResponse[]>(`${BASE_URL}/v1/products`);
    return response.data;
  },

  // GET /api/products/v1/products/{id}
  getProductById: async (id: number | string): Promise<ProductResponse> => {
    const response = await axiosClient.get<ProductResponse>(`${BASE_URL}/v1/products/${id}`);
    return response.data;
  },

  // GET /api/products/v1/category/{category}
  getProductsByCategory: async (category: string): Promise<ProductResponse[]> => {
    // Gestiamo il trim e l'upperCase anche lato client per sicurezza
    const normalizedCategory = category.trim().toUpperCase();
    const response = await axiosClient.get<ProductResponse[]>(`${BASE_URL}/v1/category/${normalizedCategory}`);
    return response.data;
  },

  // GET /api/products/v1/best_selling
  getBestSelling: async (limit: number = 5): Promise<ProductResponse[]> => {
    const response = await axiosClient.get<ProductResponse[]>(`${BASE_URL}/v1/best_selling`, {
      params: { limit }
    });
    return response.data;
  },

  // POST /api/products/v1/products
  // Riceve l'oggetto con le info del prodotto e il file binario dell'immagine
  createProduct: async (productData: ProductRequest, imageFile: File | null): Promise<ProductResponse> => {
    const formData = new FormData();
    formData.append("name", productData.name);
    formData.append("price", String(productData.price));
    formData.append("categoryName", productData.category.toUpperCase()); // BE: ProductRequest.categoryName

    if (imageFile) {
      formData.append("image", imageFile);
    }

    // Usiamo l'istanza axiosClient impostando il corretto content-type per il @ModelAttribute
    const response = await axiosClient.post<ProductResponse>(`${BASE_URL}/v1/products`, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
    return response.data;
  },

  // PUT /api/products/v1/products/{id}
  updateProduct: async (id: number | string, productData: ProductRequest, imageFile: File | null): Promise<ProductResponse> => {
    const formData = new FormData();
    formData.append("name", productData.name);
    formData.append("price", String(productData.price));
    formData.append("categoryName", productData.category.toUpperCase()); // BE: ProductRequest.categoryName

    if (imageFile) {
      formData.append("image", imageFile);
    }

    const response = await axiosClient.put<ProductResponse>(`${BASE_URL}/v1/products/${id}`, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
    return response.data;
  },

  // DELETE /api/products/v1/products/{id}
  deleteProduct: async (id: number | string): Promise<any> => {
    const response = await axiosClient.delete(`${BASE_URL}/v1/products/${id}`);
    return response.data;
  },
};

export default productService;