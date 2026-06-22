# =============================================================================
#  TABELLONE — immagine standalone
#  Build statica Vite servita da nginx, che fa anche da reverse-proxy /api → backend.
#  Build singolo:
#    docker build -t bcc-tabellone ./tabellone
#    docker run -p 5180:80 bcc-tabellone
# =============================================================================

# --- STAGE 1: build statica ---
FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
# npm install (non "ci"): i lockfile del repo sono fuori sync; install li riallinea.
# Cache mount: la cache npm (~/.npm) è condivisa tra le build → i pacchetti si
# scaricano una volta sola e vengono riusati dalle build successive.
RUN --mount=type=cache,target=/root/.npm npm install --no-audit --no-fund
COPY . .
# Host vuoto ⇒ chiamate relative (/api/...) → nginx fa da proxy.
ARG VITE_API_HOST=""
# Token-stazione della sede (dal pannello Admin) per autorizzare i cambi stato.
ARG VITE_STATION_TOKEN=""
ENV VITE_API_HOST=$VITE_API_HOST \
    VITE_STATION_TOKEN=$VITE_STATION_TOKEN
RUN npm run build

# --- STAGE 2: runtime nginx ---
FROM nginx:alpine
RUN rm /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
