// src/services/locationService.tsx
import axiosClient from '../api/axiosClient';

const API_BASE_URL = '/api/locations';

export interface LocationEntity {
    id: number;
    name: string; // Assunto dall'entità
    address: string; // Assunto dall'entità
    city: string;
    status: string;
    isOpen: boolean;
    phoneNumber?: string;
}

// Payload minimale per create/update (i campi auto-gestiti dal BE — id, timestamps — si omettono)
export interface LocationInput {
    name: string;
    address: string;
    city: string;
    phoneNumber?: string;
    isOpen?: boolean;
    // Account manager della sede (solo in creazione): se email+password sono
    // valorizzati, il backend crea l'utente MANAGER collegato alla sede.
    managerEmail?: string;
    managerPassword?: string;
    managerName?: string;
    managerSurname?: string;
    managerPhone?: string;
}

export interface LocationIngredient {
    id: number;
    quantity: number;
    ingredientName: string; // Assunto dall'entità ingrediente
}

export const locationService = {
    // Get all locations
    getAll: async (): Promise<LocationEntity[]> => {
        const res = await axiosClient.get<LocationEntity[]>(API_BASE_URL);
        return res.data;
    },

    // Get open locations
    getOpen: async (): Promise<LocationEntity[]> => {
        const res = await axiosClient.get<LocationEntity[]>(`${API_BASE_URL}/status/open`);
        return res.data;
    },

    // Get closed locations
    getClosed: async (): Promise<LocationEntity[]> => {
        const res = await axiosClient.get<LocationEntity[]>(`${API_BASE_URL}/status/closed`);
        return res.data;
    },

    // Get low stock for a location
    getLowStock: async (id: number, threshold = 10): Promise<LocationIngredient[]> => {
        const res = await axiosClient.get<LocationIngredient[]>(`${API_BASE_URL}/${id}/stock/low`, {
            params: { threshold },
        });
        return res.data;
    },

    // Global actions: Open/Close all
    openAll: async (): Promise<void> => {
        await axiosClient.put(`${API_BASE_URL}/status/open/all`);
    },

    closeAll: async (): Promise<void> => {
        await axiosClient.put(`${API_BASE_URL}/status/closed/all`);
    },

    // ── CRUD anagrafica (protetto da @PreAuthorize ADMIN → axiosClient porta il JWT) ──
    // Create new location
    create: async (data: LocationInput): Promise<LocationEntity> => {
        const res = await axiosClient.post<LocationEntity>(API_BASE_URL, {
            ...data,
            isOpen: data.isOpen ?? true,
        });
        return res.data;
    },

    // Update existing location
    update: async (id: number, data: LocationInput): Promise<LocationEntity> => {
        const res = await axiosClient.put<LocationEntity>(`${API_BASE_URL}/${id}`, data);
        return res.data;
    },

    // Delete location
    remove: async (id: number): Promise<void> => {
        await axiosClient.delete(`${API_BASE_URL}/${id}`);
    },

    // Free Geocoding API (Nominatim OpenStreetMap)
    getCoordinates: async (address: string, city: string): Promise<[number, number] | null> => {
        try {
            const query = encodeURIComponent(`${address}, ${city}`);
            const res = await fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${query}&limit=1`);
            const data = await res.json();
            if (data && data.length > 0) {
                return [parseFloat(data[0].lat), parseFloat(data[0].lon)];
            }
            return null;
        } catch (error) {
            console.error("Errore Geocoding:", error);
            return null;
        }
    }
};