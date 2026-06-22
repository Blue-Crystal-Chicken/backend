import React, { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  LayoutDashboard,
  Package,
  ShoppingBag,
  Warehouse,
  Euro,
  BarChart3,
  Megaphone,
  Bell,
  Inbox,
  ChevronLeft,
  ChevronRight,
  LogOut
} from 'lucide-react';

const SidebarItem = ({ icon: Icon, label, to, isCollapsed }) => {
  return (
    <NavLink
      to={to}
      title={isCollapsed ? label : ""}
      className={({ isActive }) => `
        flex items-center gap-4 px-3 py-3 rounded-lg transition-all duration-200 group
        ${isActive
          ? 'bg-blue-600/20 text-blue-400 border-r-2 border-blue-500'
          : 'text-slate-400 hover:bg-slate-800 hover:text-slate-100'}
      `}
    >
      <Icon size={20} className="min-w-[20px]" />
      {!isCollapsed && <span className="font-medium whitespace-nowrap text-sm">{label}</span>}
    </NavLink>
  );
};

export default function Sidebar() {
  const [isCollapsed, setIsCollapsed] = useState(false);
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  // Manager: NIENTE "Sedi" (la gestione sedi è solo dell'Admin).
  const menuItems = [
    { icon: LayoutDashboard, label: 'Dashboard', to: '/dashboard' },
    { icon: Package, label: 'Prodotti', to: '/prodotti' },
    { icon: ShoppingBag, label: 'Ordini', to: '/ordini' },
    { icon: Warehouse, label: 'Magazzino', to: '/magazzino' },
    { icon: Euro, label: 'Finanze', to: '/finanze' },
    { icon: BarChart3, label: 'Report', to: '/report' },
    { icon: Megaphone, label: 'Offerte', to: '/marketing' },
    { icon: Inbox, label: 'Richieste', to: '/richieste' },
    { icon: Bell, label: 'Notifiche', to: '/notifiche' },
  ];

  const handleLogout = async () => {
    try { await logout(); } catch { /* ignora */ }
    navigate('/login', { replace: true });
  };

  return (
    <aside
      className={`bg-[#0f1218] border-r border-slate-800 h-screen sticky top-0 flex flex-col transition-all duration-300 z-50 ${isCollapsed ? 'w-20' : 'w-64'
        }`}
    >
      {/* Header Sidebar */}
      <div className="p-4 h-16 flex items-center justify-between border-b border-slate-800/50">
        {!isCollapsed && (
          <div className="flex items-center gap-2 overflow-hidden">
            <div className="w-8 h-8 bg-blue-600 rounded flex-shrink-0 flex items-center justify-center font-bold text-white">
              C
            </div>
            <div className="flex flex-col leading-tight overflow-hidden">
              <span className="text-slate-100 font-bold text-base tracking-tight truncate">
                CRISTAL BLU
              </span>
              <span className="text-[10px] font-semibold tracking-[1px] uppercase text-blue-400">
                Manager
              </span>
            </div>
          </div>
        )}
        <button
          onClick={() => setIsCollapsed(!isCollapsed)}
          className={`p-2 hover:bg-slate-800 rounded-lg text-slate-400 transition-colors ${isCollapsed ? 'mx-auto' : ''}`}
        >
          {isCollapsed ? <ChevronRight size={20} /> : <ChevronLeft size={20} />}
        </button>
      </div>

      {/* Badge sede del manager */}
      {!isCollapsed && (
        <div className="mx-3 mt-3 mb-2 px-3 py-2 rounded-lg bg-blue-600/10 border border-blue-500/20">
          <div className="text-[10px] font-semibold tracking-[1px] uppercase text-slate-400 mb-0.5">
            Sede gestita
          </div>
          <div className="text-sm font-bold text-blue-400 truncate">
            {user?.locationName || (user?.locationId ? `Sede #${user.locationId}` : 'Nessuna sede')}
          </div>
        </div>
      )}

      {/* Menu Navigation */}
      <nav className="flex-1 px-3 space-y-1 overflow-y-auto custom-scrollbar mt-2">
        {menuItems.map((item) => (
          <SidebarItem key={item.to} {...item} isCollapsed={isCollapsed} />
        ))}
      </nav>

      {/* Footer / Logout */}
      <div className="p-3 border-t border-slate-800">
        <button
          onClick={handleLogout}
          className="flex items-center gap-4 px-3 py-3 w-full text-slate-500 hover:bg-red-500/10 hover:text-red-400 rounded-lg transition-all group"
        >
          <LogOut size={20} />
          {!isCollapsed && <span className="font-medium text-sm">Esci Sessione</span>}
        </button>
      </div>
    </aside>
  );
}
