import notificationClient from "../api/notificationClient";

// ─── DTO (allineati al servizio notifiche r2u su 8085) ───────────────────────

/** POST /devices — request (registra un device "fisico"/logico). */
export interface DeviceRequest {
  token: string;
  platform: string;
}

/** POST /user-devices — request (associa un device a un utente). */
export interface UserDeviceRequest {
  userId: string;
  token: string;
  platform: string;
}

/** POST /api/notification/create — request */
export interface NotificationCreateRequest {
  title: string;
  body: string;
  type: string; // enum 8085: NEW_PRODUCT | NEW_MENU | NEW_OFFER
  payload?: Record<string, unknown>;
}

/** POST /api/notification/create — response */
export interface NotificationCreateResponse {
  id: number;
  title: string;
  body: string;
  type: string;
  payload?: Record<string, unknown>;
  createdAt: string;
  updatedAt: string;
}

/** POST /device-notifications/send-all — request */
export interface SendNotificationRequest {
  notificationId: number;
  deviceTokenRequest: string; // token del device "richiedente" (chi scatena l'invio)
}

// ─── Requester del pannello admin ────────────────────────────────────────────
// Il pannello web è solo un TRIGGER: non riceve push (i destinatari sono le app
// mobili registrate su 8085). Usiamo un token "requester" stabile e DUMMY,
// registrato SOLO via /devices (non /user-devices) così non finisce tra i
// destinatari di send-all, ma supera il controllo "requester esistente".
const REQUESTER_TOKEN = "bcc-admin-web-requester";
const REQUESTER_PLATFORM = "WEB";

let requesterReady: Promise<void> | null = null;

const pushNotificationService = {
  // POST /devices  (idempotente lato BE)
  createDevice: async (req: DeviceRequest): Promise<void> => {
    await notificationClient.post("/devices", req);
  },

  // POST /user-devices  (associazione device↔utente; usata dalle app, non dal web)
  registerDevice: async (req: UserDeviceRequest): Promise<void> => {
    await notificationClient.post("/user-devices", req);
  },

  // POST /api/notification/create
  createNotification: async (req: NotificationCreateRequest): Promise<NotificationCreateResponse> => {
    const res = await notificationClient.post<NotificationCreateResponse>("/api/notification/create", req);
    return res.data;
  },

  // POST /device-notifications/send-all
  sendAll: async (req: SendNotificationRequest): Promise<unknown> => {
    const res = await notificationClient.post("/device-notifications/send-all", req);
    return res.data;
  },

  /** Registra (una volta per sessione) il device requester dummy del pannello. */
  ensureRequester: async (): Promise<void> => {
    if (!requesterReady) {
      requesterReady = pushNotificationService
        .createDevice({ token: REQUESTER_TOKEN, platform: REQUESTER_PLATFORM })
        .catch((err: any) => {
          // reset così un prossimo tentativo può riprovare
          requesterReady = null;
          throw err;
        });
    }
    return requesterReady;
  },

  /**
   * Flusso completo (usato dal modal "Invia notifica"): registra il requester,
   * crea la notifica e la invia a tutti i device attivi (le app mobili).
   */
  createAndSend: async (
    data: NotificationCreateRequest
  ): Promise<{ notification: NotificationCreateResponse; sent: unknown }> => {
    await pushNotificationService.ensureRequester();
    const notification = await pushNotificationService.createNotification(data);
    const sent = await pushNotificationService.sendAll({
      notificationId: notification.id,
      deviceTokenRequest: REQUESTER_TOKEN,
    });
    return { notification, sent };
  },

  /**
   * Notifica "di evento" (creazione prodotto/menu/offerta): fire-and-forget,
   * non blocca né lancia. Registra il requester, crea e invia a tutti.
   */
  notifyEvent: async (data: NotificationCreateRequest): Promise<{ ok: boolean; error?: string }> => {
    try {
      await pushNotificationService.ensureRequester();
      const notification = await pushNotificationService.createNotification(data);
      await pushNotificationService.sendAll({
        notificationId: notification.id,
        deviceTokenRequest: REQUESTER_TOKEN,
      });
      return { ok: true };
    } catch (err: any) {
      const error = err?.response?.data?.message || err?.message || "Invio notifica fallito";
      console.warn("[push] Invio notifica evento fallito:", error);
      return { ok: false, error };
    }
  },
};

export default pushNotificationService;
