import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";

// Porta del dev server della Cucina (parametrica via VITE_PORT, default 5183).
// La porta del BACKEND si configura in .env (VITE_API_PORT) — vedi src/config.js
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: Number(process.env.VITE_PORT) || 5183,
    host: true,
  },
});
