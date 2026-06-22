import axios from "axios";

// Client dedicato al servizio notifiche Firebase (separato dal backend principale).
// Base URL parametrica: VITE_NOTIFICATION_API_URL (default localhost:8085).
// "??": vuota in Docker (nginx instrada al servizio notifiche); localhost in dev.
const BASE_URL = import.meta.env.VITE_NOTIFICATION_API_URL ?? "http://localhost:8085";

const notificationClient = axios.create({
  baseURL: BASE_URL,
  headers: { "Content-Type": "application/json" },
  timeout: 15000,
});

// Attacca lo stesso JWT del backend principale, se presente.
notificationClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("bcc_token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export default notificationClient;
