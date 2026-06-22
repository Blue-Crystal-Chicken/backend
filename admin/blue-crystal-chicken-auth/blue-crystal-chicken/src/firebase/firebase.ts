import { initializeApp, type FirebaseApp } from "firebase/app";
import { getMessaging, getToken, onMessage, isSupported, type Messaging, type MessagePayload } from "firebase/messaging";

// ─── Config da variabili d'ambiente Vite ─────────────────────────────────────
const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
  appId: import.meta.env.VITE_FIREBASE_APP_ID,
  measurementId: import.meta.env.VITE_FIREBASE_MEASUREMENT_ID,
};

const VAPID_KEY = import.meta.env.VITE_FIREBASE_VAPID_KEY as string;

let app: FirebaseApp | null = null;
let messagingPromise: Promise<Messaging | null> | null = null;

function getApp(): FirebaseApp {
  if (!app) app = initializeApp(firebaseConfig);
  return app;
}

/** Restituisce l'istanza Messaging, o null se il browser non la supporta. */
export async function getMessagingInstance(): Promise<Messaging | null> {
  if (!messagingPromise) {
    messagingPromise = (async () => {
      const supported = await isSupported().catch(() => false);
      if (!supported) {
        console.warn("[firebase] Cloud Messaging non supportato su questo browser.");
        return null;
      }
      return getMessaging(getApp());
    })();
  }
  return messagingPromise;
}

/**
 * Richiede il permesso notifiche all'utente e ottiene il token FCM del device.
 * Registra il service worker dedicato per la ricezione in background.
 * @returns il token del device, oppure null se permesso negato / non supportato.
 */
export async function requestNotificationToken(): Promise<string | null> {
  if (typeof Notification === "undefined") {
    console.warn("[firebase] Notification API non disponibile.");
    return null;
  }

  const permission = await Notification.requestPermission();
  if (permission !== "granted") {
    console.info("[firebase] Permesso notifiche non concesso:", permission);
    return null;
  }

  const messaging = await getMessagingInstance();
  if (!messaging) return null;

  // Registra il service worker (deve stare nella root pubblica)
  const swRegistration = await navigator.serviceWorker.register("/firebase-messaging-sw.js");

  const token = await getToken(messaging, {
    vapidKey: VAPID_KEY,
    serviceWorkerRegistration: swRegistration,
  });

  return token || null;
}

/** Sottoscrive i messaggi in foreground. Ritorna la funzione di unsubscribe. */
export async function onForegroundMessage(
  handler: (payload: MessagePayload) => void
): Promise<() => void> {
  const messaging = await getMessagingInstance();
  if (!messaging) return () => {};
  return onMessage(messaging, handler);
}
