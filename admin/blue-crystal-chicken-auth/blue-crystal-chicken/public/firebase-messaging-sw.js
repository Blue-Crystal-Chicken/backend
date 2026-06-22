/* Service Worker per Firebase Cloud Messaging (ricezione in background).
 * NB: i SW non possono leggere import.meta.env, quindi la config è inline.
 * Sono chiavi client pubbliche (le stesse esposte nel bundle): nessun segreto qui. */
importScripts("https://www.gstatic.com/firebasejs/10.12.2/firebase-app-compat.js");
importScripts("https://www.gstatic.com/firebasejs/10.12.2/firebase-messaging-compat.js");

firebase.initializeApp({
  apiKey: "AIzaSyAP1viDYyJ6dNB7Pyh-xwT5A3gJfXhQMCQ",
  authDomain: "notification-cab0a.firebaseapp.com",
  projectId: "notification-cab0a",
  storageBucket: "notification-cab0a.firebasestorage.app",
  messagingSenderId: "1021605930612",
  appId: "1:1021605930612:web:bac71a93e6be97a48ad321",
  measurementId: "G-H8EPYXYSD4",
});

const messaging = firebase.messaging();

// Notifica mostrata quando l'app è in background / chiusa
messaging.onBackgroundMessage((payload) => {
  const title = payload.notification?.title || payload.data?.title || "Blue Crystal Chicken";
  const options = {
    body: payload.notification?.body || payload.data?.body || "",
    icon: "/vite.svg",
    data: payload.data || {},
  };
  self.registration.showNotification(title, options);
});
