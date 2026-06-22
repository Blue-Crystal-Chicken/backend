import React, { useState } from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  Package,
  ShoppingBag,
  MapPin,
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


  const menuItems = [
    { icon: LayoutDashboard, label: 'Dashboard', to: '/dashboard' },
    { icon: Package, label: 'Prodotti', to: '/prodotti' },
    { icon: ShoppingBag, label: 'Ordini', to: '/ordini' },
    { icon: MapPin, label: 'Sedi', to: '/sedi' },
    { icon: Warehouse, label: 'Magazzino', to: '/magazzino' },
    { icon: Euro, label: 'Finanze', to: '/finanze' },
    { icon: BarChart3, label: 'Report', to: '/report' },
    { icon: Megaphone, label: 'Marketing', to: '/marketing' },
    { icon: Inbox, label: 'Richieste', to: '/richieste' },
    { icon: Bell, label: 'Notifiche', to: '/notifiche' },
  ];

  return (
    <aside
      className={`bg-[#0f1218] border-r border-slate-800 h-screen sticky top-0 flex flex-col transition-all duration-300 z-50 ${isCollapsed ? 'w-20' : 'w-64'
        }`}
    >
      {/* Header Sidebar */}
      <div className="p-4 h-16 flex items-center justify-between border-b border-slate-800/50 mb-4">
        {!isCollapsed && (
          <div className="flex items-center gap-2 overflow-hidden">
            <div className="w-8 h-8 bg-blue-600 rounded flex-shrink-0 flex items-center justify-center font-bold text-white">
              C
            </div>
            <span className="text-slate-100 font-bold text-lg tracking-tight truncate">
              CRISTAL BLU
            </span>
          </div>
        )}
        <button
          onClick={() => setIsCollapsed(!isCollapsed)}
          className={`p-2 hover:bg-slate-800 rounded-lg text-slate-400 transition-colors ${isCollapsed ? 'mx-auto' : ''}`}
        >
          {isCollapsed ? <ChevronRight size={20} /> : <ChevronLeft size={20} />}
        </button>
      </div>

      {/* Menu Navigation */}
      <nav className="flex-1 px-3 space-y-1 overflow-y-auto custom-scrollbar">
        {menuItems.map((item) => (
          <SidebarItem key={item.to} {...item} isCollapsed={isCollapsed} />
        ))}
      </nav>

      {/* Footer / Logout */}
      <div className="p-3 border-t border-slate-800">
        <button className="flex items-center gap-4 px-3 py-3 w-full text-slate-500 hover:bg-red-500/10 hover:text-red-400 rounded-lg transition-all group">
          <LogOut size={20} />
          {!isCollapsed && <span className="font-medium text-sm">Esci Sessione</span>}
        </button>
      </div>
    </aside>
  );
}