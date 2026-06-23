import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
  ],
  // Porta del dev server admin (parametrica via VITE_PORT, default 5173).
  server: {
    port: Number(process.env.VITE_PORT) || 5173,
    host: true,
  },
})
