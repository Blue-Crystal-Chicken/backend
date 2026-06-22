import axiosClient from "../api/axiosClient";

export interface OrderItemResponse {
  productId: number | null;
  productName: string | null;
  offerId: number | null;
  offerName: string | null;
  quantity: number;
  price: number;
  additions: number | null;
  specialNote: string | null;
}

export interface OrderResponse {
  id: number;
  orderId: string;
  code: string | null;
  serviceType: string;
  orderType: string | null;
  tableNumber: string | null;
  paymentType: string | null;
  paymentAmount: number | null;
  totalAt: number;
  status: string;
  userId: number | null;
  userName: string | null;
  locationId: number | null;
  items: OrderItemResponse[];
  createdAt: string;
  updatedAt: string;
}

export interface UpdateOrderRequest {
  serviceType?: string;
  orderType?: string;
  tableNumber?: string;
  paymentType?: string;
  status?: string;
}

const BASE = "/api/orders";

const orderService = {
  getByDateRange: async (from: string, to: string): Promise<OrderResponse[]> => {
    const res = await axiosClient.get<OrderResponse[]>(`${BASE}/date-range`, { params: { from, to } });
    return res.data;
  },

  getByLocationAndDateRange: async (locationId: number, from: string, to: string): Promise<OrderResponse[]> => {
    const res = await axiosClient.get<OrderResponse[]>(`${BASE}/location/${locationId}/date-range`, { params: { from, to } });
    return res.data;
  },

  updateStatus: async (id: number, status: string): Promise<OrderResponse> => {
    const res = await axiosClient.put<OrderResponse>(`${BASE}/${id}/status`, null, { params: { status } });
    return res.data;
  },

  update: async (id: number, data: UpdateOrderRequest): Promise<OrderResponse> => {
    const res = await axiosClient.put<OrderResponse>(`${BASE}/${id}`, data);
    return res.data;
  },

  delete: async (id: number): Promise<void> => {
    await axiosClient.delete(`${BASE}/${id}`);
  },
};

export default orderService;
