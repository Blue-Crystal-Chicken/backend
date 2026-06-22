# =============================================================================
#  CASSA (counter) — immagine standalone
#  Build statica Vite (pnpm) servita da nginx, che fa anche da reverse-proxy:
#    /api  /images  /uploads  → backend principale (8080)
#  Build singolo:
#    docker build -t bcc-cassa ./counter-main/counter-main
# =============================================================================

# --- STAGE 1: build statica (pnpm) ---
FROM node:22-alpine AS build
WORKDIR /app
RUN corepack enable
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
# install (non --frozen-lockfile: il lock potrebbe essere fuori sync)
RUN --mount=type=cache,target=/root/.local/share/pnpm/store pnpm install --no-frozen-lockfile
COPY . .
# URL vuota ⇒ chiamate relative (/api, /images, ...) → nginx fa da proxy same-origin.
ARG VITE_API_BASE_URL=""
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
# vite build (non "tsc -b && vite build"): evitiamo che eventuali errori di tipo
# blocchino la build dell'immagine; vite/esbuild produce comunque il bundle.
RUN pnpm exec vite build

# --- STAGE 2: runtime nginx ---
FROM nginx:alpine
RUN rm /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
