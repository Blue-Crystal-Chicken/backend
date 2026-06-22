import axios from "axios";

// "??" (non "||"): vuota in Docker dietro nginx → URL relative; localhost in dev.
const BASE_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

const axiosClient = axios.create({
  baseURL: BASE_URL,
  headers: { "Content-Type": "application/json" },
  timeout: 15000,
});

// ── Request interceptor ─────────────────────────────────────────────────────
// Attacca il JWT a ogni richiesta se presente in localStorage
axiosClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("bcc_token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ── Response interceptor ────────────────────────────────────────────────────
// Se il backend risponde 401 (token scaduto / non valido) → logout forzato
axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // Escludi gli endpoint di autenticazione dal redirect forzato:
    // un 401 durante il login deve mostrare l'errore, non ricaricare la pagina.
    const requestUrl = error.config?.url || "";
    const isAuthEndpoint = requestUrl.includes("/api/auth/");

    if (error.response?.status === 401 && !isAuthEndpoint) {
      localStorage.removeItem("bcc_token");
      localStorage.removeItem("bcc_user");
      // Redirect al login senza dipendere dal router React
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

export default axiosClient;
