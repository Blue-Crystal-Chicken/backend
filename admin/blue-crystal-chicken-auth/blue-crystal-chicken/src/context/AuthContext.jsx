import { createContext, useContext, useState, useCallback } from "react";
import { loginRequest, logoutRequest } from "../api/authService";

// ── Costanti chiavi localStorage ────────────────────────────────────────────
const TOKEN_KEY = "bcc_token";
const USER_KEY = "bcc_user";

// ── Helpers persist / restore ───────────────────────────────────────────────
const persistSession = (token, user) => {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(USER_KEY, JSON.stringify(user));
};

const clearSession = () => {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
};

const restoreUser = () => {
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
};

// ── Context ─────────────────────────────────────────────────────────────────
const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => restoreUser());
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Derivato dallo stato `user` per essere reattivo ai re-render
  const isAuthenticated = !!user && Boolean(localStorage.getItem(TOKEN_KEY));

  /**
   * login(email, password)
   * Chiama il backend, salva token + user, aggiorna lo stato.
   * Rilancia l'errore così il form può mostrare il messaggio.
   */
  const login = useCallback(async (email, password) => {
    setLoading(true);
    setError(null);
    try {
      const response = await loginRequest(email, password);
      const data = response.data;

      // Debug: verifica la struttura della risposta del backend
      console.log("[AuthContext] Risposta login backend:", data);
      
      // Il backend restituisce i dati utente "appiattiti" nella radice della risposta e i ruoli come array
      const userData = {
        id: data.id,
        email: data.email,
        role: data.roles && data.roles.length > 0 ? data.roles[0] : null,
        locationId: null, // locationId non fornito attualmente dal backend
        name: data.name,
        surname: data.surname
      };

      persistSession(data.token, userData);
      setUser(userData);
      return userData;
    } catch (err) {
      const message =
        err.response?.data?.message ||
        err.response?.data ||
        "Credenziali non valide. Riprova.";
      setError(message);
      throw new Error(message);
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * logout()
   * Notifica il backend (se disponibile) e pulisce la sessione locale.
   */
  const logout = useCallback(async () => {
    await logoutRequest();
    clearSession();
    setUser(null);
  }, []);

  const value = {
    user,          // { id, email, role, locationId } | null
    isAuthenticated,
    loading,       // true durante la chiamata di login
    error,         // stringa errore dall'ultimo tentativo fallito
    login,
    logout,
    clearError: () => setError(null),
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

// ── Hook di accesso ──────────────────────────────────────────────────────────
export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth deve essere usato dentro <AuthProvider>");
  return ctx;
};
