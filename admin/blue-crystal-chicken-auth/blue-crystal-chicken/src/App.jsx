import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import PrivateRoute from "./components/layout/PrivateRoute";
import MainLayout from "./components/layout/MainLayout";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import Prodotti from "./pages/Prodotti";
import Ordini from "./pages/Ordini";
import Finanze from "./pages/Finanze";
import Report  from "./pages/Report";
import Sedi from "./pages/sedi";
import Magazzino from "./pages/Magazzino";
import DettaglioSede from "./pages/DettaglioSede";
import Marketing from "./pages/Marketing";
import Notifiche from "./pages/Notifiche";
import Richieste from "./pages/Richieste";

// Componente temporaneo per le sezioni in sviluppo
const Placeholder = ({ name }) => (
  <div className="p-8 text-slate-400 border border-dashed border-slate-700 rounded-xl bg-[#0f1218]">
    <h2 className="text-2xl font-bold text-white mb-2">{name}</h2>
    <p className="text-slate-500">Modulo enterprise in fase di configurazione.</p>
  </div>
);

const App = () => (
  <BrowserRouter>
    <AuthProvider>
      <Routes>
        {/* Rotta Pubblica */}
        <Route path="/login" element={<Login />} />

        {/* Rotte Protette con Sidebar e MainLayout */}
        <Route element={<PrivateRoute><MainLayout /></PrivateRoute>}>

          {/* 1. Dashboard */}
          <Route path="/dashboard" element={<Dashboard />} />

          {/* 2. Prodotti */}
          <Route path="/prodotti" element={<Prodotti />} />

          {/* 3. Ordini */}
          <Route path="/ordini" element={<Ordini />} />

          {/* 4. Sedi (Lista e Dettaglio) */}
          <Route path="/sedi" element={<Sedi />} />
          <Route path="/sedi/:id" element={<DettaglioSede />} />

          {/* 5. Magazzino */}
          <Route path="/magazzino" element={<Magazzino />} />

          {/* 6. Finanze */}
          <Route path="/finanze" element={<Finanze />} />

          {/* 7. Report */}
          <Route path="/report" element={<Report />} />

          {/* 8. Marketing */}
          <Route path="/marketing" element={<Marketing />} />

          {/* 9. Notifiche */}
          <Route path="/notifiche" element={<Notifiche />} />

          {/* 10. Richieste (approvazioni dei manager) */}
          <Route path="/richieste" element={<Richieste />} />

        </Route>

        {/* Reindirizzamenti di default */}
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </AuthProvider>
  </BrowserRouter>
);

export default App;