import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
// Porta del dev server del Manager (parametrica via VITE_PORT, default 5185).
// La porta del BACKEND si configura in .env (VITE_API_URL).
export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
  ],
  server: {
    port: Number(process.env.VITE_PORT) || 5185,
    host: true,
  },
})
