import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";

// Porta del dev server del tabellone (parametrica via VITE_PORT).
// La porta del BACKEND si configura invece in .env (VITE_API_PORT) — vedi src/config.js
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: Number(process.env.VITE_PORT) || 5180,
    host: true,
  },
});
