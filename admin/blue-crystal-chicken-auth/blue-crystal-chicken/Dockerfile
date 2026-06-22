# =============================================================================
#  ADMIN (frontend) — immagine standalone
#  Build statica Vite servita da nginx, reverse-proxy verso DUE backend:
#    /api/notification, /devices, /user-devices, /device-notifications → 8085
#    /api/*                                                            → 8080
#  Build singolo:
#    docker build -t bcc-admin-web ./admin/blue-crystal-chicken-auth/blue-crystal-chicken
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

# URL vuote ⇒ chiamate relative same-origin; nginx smista al servizio giusto.
ARG VITE_API_URL=""
ARG VITE_NOTIFICATION_API_URL=""
# Credenziali Firebase (push). Lasciare vuote se non si usano le notifiche push.
ARG VITE_FIREBASE_API_KEY=""
ARG VITE_FIREBASE_AUTH_DOMAIN=""
ARG VITE_FIREBASE_PROJECT_ID=""
ARG VITE_FIREBASE_STORAGE_BUCKET=""
ARG VITE_FIREBASE_MESSAGING_SENDER_ID=""
ARG VITE_FIREBASE_APP_ID=""
ARG VITE_FIREBASE_MEASUREMENT_ID=""
ARG VITE_FIREBASE_VAPID_KEY=""
ENV VITE_API_URL=$VITE_API_URL \
    VITE_NOTIFICATION_API_URL=$VITE_NOTIFICATION_API_URL \
    VITE_FIREBASE_API_KEY=$VITE_FIREBASE_API_KEY \
    VITE_FIREBASE_AUTH_DOMAIN=$VITE_FIREBASE_AUTH_DOMAIN \
    VITE_FIREBASE_PROJECT_ID=$VITE_FIREBASE_PROJECT_ID \
    VITE_FIREBASE_STORAGE_BUCKET=$VITE_FIREBASE_STORAGE_BUCKET \
    VITE_FIREBASE_MESSAGING_SENDER_ID=$VITE_FIREBASE_MESSAGING_SENDER_ID \
    VITE_FIREBASE_APP_ID=$VITE_FIREBASE_APP_ID \
    VITE_FIREBASE_MEASUREMENT_ID=$VITE_FIREBASE_MEASUREMENT_ID \
    VITE_FIREBASE_VAPID_KEY=$VITE_FIREBASE_VAPID_KEY
RUN npm run build

# --- STAGE 2: runtime nginx ---
FROM nginx:alpine
RUN rm /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
