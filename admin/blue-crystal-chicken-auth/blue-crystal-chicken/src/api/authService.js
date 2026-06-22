import axiosClient from "./axiosClient";

/**
 * POST /api/auth/login
 * Il backend di Blue Crystal Chicken deve rispondere con:
 * { token: string, id: number, email: string, roles: string[], name: string, surname: string }
 */
export const loginRequest = (email, password) =>
  axiosClient.post("/api/auth/login", { email, password });

/**
 * POST /api/auth/logout  (opzionale — il JWT è stateless,
 * ma utile se il backend mantiene una blacklist)
 */
export const logoutRequest = () =>
  axiosClient.post("/api/auth/logout").catch(() => {
    // ignora errori di rete durante logout — il token
    // viene rimosso lato client in ogni caso
  });
