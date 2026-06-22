# Dockerizzazione — Blue Crystal Chicken

Due livelli, come da richiesta:

1. **Docker singolo** per ogni componente (un `Dockerfile` nella sua cartella → si
   può buildare/runnare da solo).
2. **Docker globale** (`docker-compose.yml` in root) che orchestra tutto.

## Componenti e immagini

| Servizio (compose) | Cartella | Tipo | Porta host (default) |
|---|---|---|---|
| `postgres` | (immagine ufficiale) | DB | 5432 |
| `rabbitmq` | (immagine ufficiale) | broker | 5672 / 15672 |
| `bcc-backend` | `admin/backend` | Spring Boot | 8080 |
| `bcc-notification` | `notification/notification` | Spring Boot | 8085 |
| `bcc-admin-web` | `admin/blue-crystal-chicken-auth/blue-crystal-chicken` | React+nginx | 8090 |
| `bcc-manager` | `manager` | React+nginx | 5185 |
| `bcc-cucina` | `cucina` | React+nginx | 5183 |
| `bcc-tabellone` | `tabellone` | React+nginx | 5180 |

I 4 frontend sono build statiche servite da **nginx**, che fa anche da
**reverse-proxy**: il browser chiama `/api` (same-origin) e nginx inoltra al
backend giusto. `admin` e `manager` smistano anche verso il servizio notifiche
(`/api/notification`, `/devices`, `/user-devices`, `/device-notifications` → 8085).

## Avvio globale

```bash
cp .env.example .env        # opzionale: personalizza porte / Firebase
docker compose up -d --build
```

Poi:
- Admin → http://localhost:8090
- Manager → http://localhost:5185
- Cucina → http://localhost:5183
- Tabellone → http://localhost:5180
- RabbitMQ UI → http://localhost:15672 (guest/guest)

Stop: `docker compose down` (aggiungi `-v` per cancellare anche i dati del DB).

## Build/run di un singolo componente

```bash
docker build -t bcc-cucina ./cucina
docker build -t bcc-tabellone ./tabellone
docker build -t bcc-manager ./manager
docker build -t bcc-admin-web ./admin/blue-crystal-chicken-auth/blue-crystal-chicken
docker build -t bcc-backend ./admin/backend
docker build -t bcc-notification ./notification/notification
```

> Nota: un frontend da solo serve la SPA ma il suo proxy `/api` cerca i servizi
> `bcc-backend`/`bcc-notification` sulla rete Docker. In standalone vanno quindi
> messi sulla stessa rete del backend (o si usa il compose globale).

## Note

- **Database**: un solo PostgreSQL con due DB — `blue_crystal_db` (backend) e
  `bcc_db` (notifiche, creato da `docker/postgres/init.sql`).
- **JWT condiviso**: `BE_APP_JWTSECRET` deve essere identico tra backend e
  notifiche (default già allineato).
- **Config backend**: host DB/RabbitMQ sono iniettati via env nel compose
  (`SPRING_DATASOURCE_URL`, `SPRING_RABBITMQ_HOST`), senza toccare
  `application.properties`.
- **Proxy aziendale / SSL**: se `npm ci` fallisce in fase di build per il
  certificato aziendale (stesso problema visto in locale), va aggiunto il CA
  aziendale nell'immagine di build o configurato npm. In locale lo si aggira con
  `NODE_OPTIONS=--use-system-ca`.
