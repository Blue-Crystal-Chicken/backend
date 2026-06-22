# 📬 Notification Service

Microservizio per la gestione e l'invio di notifiche push via **Firebase Cloud Messaging (FCM)**.  
Espone API REST per creare notifiche, registrare device e tracciare l'invio delle notifiche ai singoli device.

---

## 🚀 Tech Stack

| Tecnologia | Versione |
|---|---|
| Java | 21 |
| Spring Boot | 4.0.6 |
| Spring Data JPA |
| Firebase Admin SDK | 9.4.3 |
| Database (dev) | H2 (in-memory) |
| Database (prod) | PostgreSQL |
| Documentazione API | Swagger UI |

---

## ⚙️ Configurazione

### Prerequisiti

- **Java 21+**
- **Maven 3.8+**
- **Account Firebase** con progetto configurato per FCM

### File di configurazione

Creare il file `src/main/resources/application.properties` (escluso dal Git):

```properties
spring.application.name=notification
server.port=8082

# Firebase
firebase.config.path=classpath:serviceAccountKey.json

# H2 (sviluppo)
spring.datasource.url=jdbc:h2:mem:notificationdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.username=sa
spring.datasource.password=<tua_password>
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Swagger
springdoc.swagger-ui.path=/swagger-ui/index.html
springdoc.api-docs.path=/v3/api-docs
```

Per usare **PostgreSQL** in produzione, sostituire le proprietà datasource:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/notificationdb
spring.datasource.username=<username>
spring.datasource.password=<password>
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
```

### Firebase Service Account Key

1. Accedere alla [Firebase Console](https://console.firebase.google.com/)
2. Progetto → Impostazioni → Account di servizio → **Genera nuova chiave privata**
3. Rinominare il file scaricato in `serviceAccountKey.json`
4. Inserirlo in `src/main/resources/serviceAccountKey.json`

> ⚠️ **Questo file non deve MAI essere committato su Git.**

---

## ▶️ Avvio

```bash
# Con Maven Wrapper
./mvnw spring-boot:run

# Oppure con Maven installato
mvn spring-boot:run
```

Il servizio sarà disponibile su: `http://localhost:8082`

---

## 📚 Documentazione API

| Strumento | URL |
|---|---|
| Swagger UI | http://localhost:8082/swagger-ui/index.html |
| OpenAPI JSON | http://localhost:8082/v3/api-docs |
| H2 Console | http://localhost:8082/h2-console |

---

## 🗂️ Struttura del Progetto

```
src/main/java/com/r2u/notification/
├── config/                         # Configurazioni (Firebase, ecc.)
├── controller/                     # Controller REST
│   ├── NotificationController.java
│   ├── DeviceController.java
│   └── DeviceNotificationController.java
├── converter/                      # Converter JPA (es. JsonNode)
├── dto/
│   ├── request/                    # DTO di input
│   └── response/                   # DTO di output
├── entity/                         # Entità JPA
│   ├── enums/                      # Enum (Type, Status)
│   ├── NotificationEntity.java
│   ├── DeviceEntity.java
│   └── DeviceNotification.java
├── exception/                      # Eccezioni custom
├── repository/                     # Repository JPA
├── security/                       # Configurazione Spring Security
│   └── WebSecurityConfig.java
└── service/                        # Business logic
```

---

## 📡 API Reference

### 🔔 Notifications — `/api/notification`

#### `POST /api/notification/create`
Crea una nuova notifica.

**Request Body:**
```json
{
  "title": "Richiesta ferie",
  "body": "Giovanni Rossi ha richiesto ferie dal 01/01/2025 al 10/01/2025",
  "type": "VACATION_REQUEST",
  "payload": {
    "startDate": "2025-01-01",
    "endDate": "2025-01-10"
  }
}
```

**Response:** `200 OK` — `NotificationResponse`

---

#### `GET /api/notification/{id}`
Recupera una notifica tramite ID.

**Response:** `200 OK` — `NotificationResponse` | `404 Not Found`

---

### 📱 Devices — `/devices`

#### `POST /devices`
Registra un nuovo device (token FCM).

**Request Body:**
```json
{
  "userId": "user-123",
  "token": "fcm-device-token",
  "platform": "ANDROID"
}
```

**Response:** `200 OK` — `DeviceResponse`

---

#### `GET /devices/{id}`
Recupera un device tramite ID.

**Response:** `200 OK` — `DeviceResponse` | `404 Not Found`

---

#### `PUT /devices/{id}`
Aggiorna un device esistente.

**Request Body:** uguale a `POST /devices`

**Response:** `200 OK` — `DeviceResponse`

---

#### `DELETE /devices/{id}`
Elimina un device.

**Response:** `200 OK`

---

### 📨 Device Notifications — `/device-notifications`

#### `POST /device-notifications/send`
Invia una notifica ad un device specifico via FCM.

**Request Body:**
```json
{
  "notificationId": 1,
  "deviceToken": "fcm-device-token"
}
```

**Response:** `200 OK`

---

#### `GET /device-notifications/{id}`
Recupera il dettaglio di una device notification tramite ID.

**Response:** `200 OK` — `DeviceNotificationResponse`

---

## 🗃️ Modello Dati

### `notifications`
| Campo | Tipo | Note |
|---|---|---|
| `id` | Long | PK auto-generato |
| `title` | String | Titolo della notifica |
| `body` | String | Testo della notifica |
| `type` | Enum | Valori: `VACATION_REQUEST` |
| `payload` | JSON (TEXT) | Dati aggiuntivi arbitrari |
| `createdAt` | LocalDateTime | Auto-generato |
| `updatedAt` | LocalDateTime | Auto-aggiornato |

### `devices`
| Campo | Tipo | Note |
|---|---|---|
| `id` | Long | PK auto-generato |
| `userId` | String | ID utente proprietario |
| `deviceToken` | String | Token FCM del device |
| `platform` | String | Es. `ANDROID`, `IOS` |
| `isActive` | Boolean | Stato del device |
| `createdAt` | LocalDateTime | Auto-generato |
| `updatedAt` | LocalDateTime | Auto-aggiornato |

### `device_notifications`
| Campo | Tipo | Note |
|---|---|---|
| `id` | Long | PK auto-generato |
| `notificationId` | Long | FK → `notifications` |
| `deviceToken` | String | Token FCM del device |
| `status` | Enum | `PENDING` / `SENT` / `FAILED` |
| `errorMessage` | String | Messaggio di errore (se FAILED) |
| `createdAt` | LocalDateTime | Auto-generato |
| `updatedAt` | LocalDateTime | Auto-aggiornato |

---

## 🔐 Sicurezza

- Sessioni **stateless** (nessun cookie/sessione lato server)
- **CORS** abilitato per: `localhost:5173`, `localhost:8081`, `localhost:8082`, `127.0.0.1:5173`, `127.0.0.1:8081`
- CSRF disabilitato (API REST)
- Tutte le route attualmente aperte (`.anyRequest().permitAll()`)

---

## 📮 Postman Collection

La collection Postman con tutte le chiamate è disponibile in:

```
postman/notification-api.postman_collection.json
```

Per importarla: **Postman → File → Import → seleziona il file**.

La variabile `{{baseUrl}}` è preimpostata a `http://localhost:8082`.


## Risoluzione del problema di autenticazione JWT

Per ricevere un token JWT lanciare il comando:

curl -X POST http://localhost:PORTA/realms/NOME_REALM/protocol/openid-connect/token -d "grant_type=password&client_id=NOME_CLIENT&username=TUO_USERNAME&password=TUA_PASSWORD"
