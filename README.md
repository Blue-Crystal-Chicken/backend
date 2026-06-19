# BCC Backend - Spring Boot REST API

Questo è il backend del progetto **Blue Crystal Kitchen (BCC)**, sviluppato in **Spring Boot** (Java 21) e configurato per connettersi ad un database **PostgreSQL**.

---

## 🛠️ Requisiti

- **Java 21**
- **Maven** (o usa il wrapper `mvnw` incluso)
- **Docker & Docker Compose** (opzionale, ma consigliato per i servizi)

---

## ⚙️ Configurazione & Variabili d'Ambiente

Il backend è configurato per connettersi a PostgreSQL. Quando avviato tramite **Docker Compose** (`docker-compose.yml`), le variabili d'ambiente necessarie sono configurate automaticamente:

- `SPRING_DATASOURCE_URL`: `jdbc:postgresql://db:5432/bcc_db`
- `SPRING_DATASOURCE_USERNAME`: `bcc_user`
- `SPRING_DATASOURCE_PASSWORD`: `bcc_password`
- `BE_APP_JWTSECRET`: Chiave segreta utilizzata per la firma e verifica dei token JWT.
- `BE_APP_UPLOAD_DIR`: Cartella per il caricamento dei file/immagini.

---

## 🚀 Come Eseguire il Backend

### 1. Tramite Docker Compose (Consigliato)
Dalla cartella principale del progetto (`bcc`), esegui:
```bash
docker-compose up -d --build backend
```
Questo avvierà anche il database Postgres e configurerà le reti necessarie.

### 2. In Locale (Sviluppo)
Se vuoi eseguire il backend direttamente sulla tua macchina locale:
1. Assicurati che un database PostgreSQL sia attivo sulla porta `5432` con le credenziali corrette (configurate in `src/main/resources/application.properties`).
2. Esegui il comando Maven:
   ```bash
   ./mvnw spring-boot:run
   ```

Il server sarà accessibile su `http://localhost:8080`.

---

## 📚 Documentazione API (Swagger)

Se abilitato, puoi visualizzare la documentazione interattiva Swagger UI al seguente indirizzo:
- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **API Docs (JSON)**: `http://localhost:8080/v3/api-docs`

---

## 🔄 Integrazione CI/CD (Jenkins + GitHub + Ngrok)

Per automatizzare i test e il build ad ogni push su GitHub, puoi attivare una pipeline Jenkins locale attivata da un Webhook GitHub. 

Poiché Jenkins gira in locale (porta `9090` tramite docker-compose), GitHub ha bisogno di un indirizzo pubblico fornito da **ngrok** per inviare le notifiche dei push.

### Step 1: Avviare Ngrok
Apri un terminale sulla tua macchina ed esponi la porta di **Jenkins** (configurata su `9090` nel file `docker-compose.yml`):
```bash
ngrok http 9090
```
*Nota: Se desideri esporre invece il backend stesso per testarlo su dispositivi fisici tramite Expo, il comando è `ngrok http 8080`.*

### Step 2: Configurare il Webhook su GitHub
1. Copia l'URL pubblico generato da ngrok (es. `https://a1b2-34-56-78.ngrok-free.app`).
2. Vai sulla pagina del tuo repository su **GitHub**.
3. Clicca su **Settings** (Impostazioni) -> **Webhooks** -> **Add webhook** (Aggiungi webhook).
4. Configura i seguenti campi:
   - **Payload URL**: Incolla l'URL di ngrok e aggiungi `/github-webhook/` alla fine.
     *Esempio:* `https://a1b2-34-56-78.ngrok-free.app/github-webhook/` (la barra finale `/` è obbligatoria).
   - **Content type**: Seleziona `application/json`.
   - **Which events...**: Seleziona `Just the push event` (Solo l'evento push).
5. Clicca su **Add webhook**.

### Step 3: Configurare la Pipeline su Jenkins
1. Accedi a Jenkins su `http://localhost:9090`.
2. Seleziona il tuo Job/Pipeline e clicca su **Configure** (Configura).
3. Nella sezione **Build Triggers** (Trigger di build), spunta la voce:
   - **GitHub hook trigger for GITScm polling**.
4. Salva la configurazione.

Adesso, ogni volta che eseguirai un `git push` sul repository di GitHub, GitHub invierà una notifica tramite l'indirizzo ngrok a Jenkins, che avvierà automaticamente la pipeline di test e build!
