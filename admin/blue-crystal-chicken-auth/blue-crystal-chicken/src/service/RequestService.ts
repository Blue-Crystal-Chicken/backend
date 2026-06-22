import axiosClient from "../api/axiosClient";

// ============================================================================
//  RequestService (Admin) — gestione delle richieste dei Manager.
//  L'Admin vede le richieste in attesa e le approva (esegue la create reale)
//  o le rifiuta. Backend: /api/requests. Vedi ARCHITETTURA.md.
// ============================================================================

export interface ChangeRequestResponse {
  id: number;
  type: string;        // CREATE_PRODUCT | CREATE_OFFER
  status: string;      // PENDING | APPROVED | REJECTED
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

const requestService = {
  // Tutte le richieste (opz. filtrate per stato: PENDING|APPROVED|REJECTED)
  getAll: async (status?: string): Promise<ChangeRequestResponse[]> => {
    const res = await axiosClient.get<ChangeRequestResponse[]>(BASE, {
      params: status ? { status } : undefined,
    });
    return res.data;
  },

  approve: async (id: number): Promise<ChangeRequestResponse> => {
    const res = await axiosClient.put<ChangeRequestResponse>(`${BASE}/${id}/approve`);
    return res.data;
  },

  reject: async (id: number, note?: string): Promise<ChangeRequestResponse> => {
    const res = await axiosClient.put<ChangeRequestResponse>(`${BASE}/${id}/reject`, { note: note ?? "" });
    return res.data;
  },
};

export default requestService;
