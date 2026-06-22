import axiosClient from "../api/axiosClient";

/** NotificationEntity.java */
export interface Notification {
  id: number;
  type?: string;
  level?: "INFO" | "SUCCESS" | "WARNING" | "ERROR" | string;
  source?: string;
  title?: string;
  message?: string;
  readFlag?: boolean;
  createdAt?: string;
}

const BASE = "/api/notifications";

const notificationService = {
  // GET /api/notifications
  getAll: async (): Promise<Notification[]> => {
    const res = await axiosClient.get<Notification[]>(BASE);
    return res.data;
  },

  // GET /api/notifications/unread-count
  unreadCount: async (): Promise<number> => {
    const res = await axiosClient.get<{ count: number }>(`${BASE}/unread-count`);
    return res.data?.count ?? 0;
  },

  // PUT /api/notifications/{id}/read
  markRead: async (id: number | string): Promise<Notification> => {
    const res = await axiosClient.put<Notification>(`${BASE}/${id}/read`);
    return res.data;
  },

  // PUT /api/notifications/read-all
  markAllRead: async (): Promise<void> => {
    await axiosClient.put(`${BASE}/read-all`);
  },

  // DELETE /api/notifications/{id}
  remove: async (id: number | string): Promise<void> => {
    await axiosClient.delete(`${BASE}/${id}`);
  },

  // DELETE /api/notifications
  clearAll: async (): Promise<void> => {
    await axiosClient.delete(BASE);
  },
};

export default notificationService;
