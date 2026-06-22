// src/pages/Sedi.jsx
import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';
import { locationService } from '../service/LocationService';
import 'leaflet/dist/leaflet.css';

// Fix per l'icona custom Azzurra (Light Blue) usando SVG in Tailwind
const customAzureIcon = new L.DivIcon({
    html: `
    <div class="relative flex items-center justify-center">
      <span class="animate-ping absolute inline-flex h-6 w-6 rounded-full bg-sky-400 opacity-75"></span>
      <div class="relative bg-sky-500 text-white p-2 rounded-full shadow-lg border-2 border-white">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
          <path fill-rule="evenodd" d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z" clip-rule="evenodd" />
        </svg>
      </div>
    </div>`,
    className: 'custom-marker',
    iconSize: [30, 30],
    iconAnchor: [15, 30]
});

// Componente per aggiornare la vista della mappa dinamicamente
function ChangeView({ center }) {
    const map = useMap();
    useEffect(() => {
        if (center) {
            map.setView(center, 13);
        }
    }, [center, map]);
    return null;
}

// Componente per aggiornare la dimensione della mappa quando cambia lo stato di ingrandimento
function ResizeMap({ isMaximized }) {
    const map = useMap();
    useEffect(() => {
        const timer = setTimeout(() => {
            map.invalidateSize();
        }, 350); // Attendi la fine della transizione CSS (duration-300)
        return () => clearTimeout(timer);
    }, [isMaximized, map]);
    return null;
}

export default function Sedi() {
    const [locations, setLocations] = useState([]);
    const [geoLocations, setGeoLocations] = useState({}); // Mappa id -> [lat, lon]
    const [mapCenter, setMapCenter] = useState([41.9028, 12.4964]); // Default Italia (Roma)
    const [isMapMaximized, setIsMapMaximized] = useState(false);
    const [openCount, setOpenCount] = useState(0);
    const [closedCount, setClosedCount] = useState(0);
    // Scorte critiche per sede: { [id]: { critical, empty } }
    const [lowStock, setLowStock] = useState({});

    // Stato Menu Contestuale (Tasto destro)
    const [contextMenu, setContextMenu] = useState({ visible: false, x: 0, y: 0, targetId: null });

    // Stato Modal CRUD anagrafica
    const emptyForm = {
        name: '', address: '', city: '', phoneNumber: '', isOpen: true,
        // Account manager della sede (solo in creazione)
        managerEmail: '', managerPassword: '', managerName: '', managerSurname: '',
    };
    const [modal, setModal] = useState({ open: false, editing: null });
    const [form, setForm] = useState(emptyForm);
    const [saving, setSaving] = useState(false);

    const openCreate = () => {
        setForm(emptyForm);
        setModal({ open: true, editing: null });
    };

    const openEdit = (id) => {
        const loc = locations.find((l) => l.id === id);
        if (!loc) return;
        setForm({
            name: loc.name || '',
            address: loc.address || '',
            city: loc.city || '',
            phoneNumber: loc.phoneNumber || '',
            isOpen: loc.isOpen ?? true,
        });
        setModal({ open: true, editing: loc });
    };

    const closeModal = () => setModal({ open: false, editing: null });

    const handleSave = async (e) => {
        e.preventDefault();
        if (!form.name.trim() || !form.address.trim() || !form.city.trim()) return;
        setSaving(true);
        try {
            if (modal.editing) await locationService.update(modal.editing.id, form);
            else await locationService.create(form);
            closeModal();
            await loadData();
        } catch (err) {
            console.error('Errore salvataggio sede', err);
            const status = err.response?.status;
            const serverMsg = err.response?.data?.message || err.response?.data?.error;
            const msg =
                status === 401 || status === 403
                    ? 'Permesso negato: servono permessi ADMIN.'
                    : `Errore nel salvataggio della sede${serverMsg ? `: ${serverMsg}` : '.'}`;
            alert(msg);
        } finally {
            setSaving(false);
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Eliminare definitivamente questa sede?')) return;
        try {
            await locationService.remove(id);
            await loadData();
        } catch (err) {
            console.error('Errore eliminazione sede', err);
            const status = err.response?.status;
            const serverMsg = err.response?.data?.message || err.response?.data?.error;
            const msg =
                status === 401 || status === 403
                    ? 'Permesso negato: servono permessi ADMIN.'
                    : `Errore nell'eliminazione della sede${serverMsg ? `: ${serverMsg}` : '.'}`;
            alert(msg);
        }
    };

    useEffect(() => {
        loadData();
    }, []);

    const loadData = async () => {
        try {
            const allLocs = await locationService.getAll();
            const openLocs = await locationService.getOpen();
            const closedLocs = await locationService.getClosed();

            setLocations(allLocs);
            setOpenCount(openLocs.length);
            setClosedCount(closedLocs.length);

            // Risoluzione coordinate asincrona per ogni sede (Geocoding)
            allLocs.forEach(async (loc) => {
                const coords = await locationService.getCoordinates(loc.address || 'Via Roma 1', loc.city);
                if (coords) {
                    setGeoLocations(prev => ({ ...prev, [loc.id]: coords }));
                }
            });

            // Scorte critiche per sede (endpoint /stock/low, soglia 10)
            const lowMap = {};
            await Promise.all(allLocs.map(async (loc) => {
                try {
                    const low = await locationService.getLowStock(loc.id, 10);
                    const arr = Array.isArray(low) ? low : [];
                    lowMap[loc.id] = {
                        critical: arr.filter((li) => (li.quantity ?? 0) > 0).length,
                        empty: arr.filter((li) => (li.quantity ?? 0) <= 0).length,
                    };
                } catch {
                    lowMap[loc.id] = { critical: 0, empty: 0 };
                }
            }));
            setLowStock(lowMap);
        } catch (err) {
            console.error("Errore caricamento dati sedi", err);
        }
    };

    // Gestione Tasto Destro
    const handleContextMenu = (e, id) => {
        e.preventDefault();
        setContextMenu({
            visible: true,
            x: e.clientX,
            y: e.clientY,
            targetId: id
        });
    };

    // Chiude il menu quando si clicca altrove
    useEffect(() => {
        const closeMenu = () => setContextMenu(prev => ({ ...prev, visible: false }));
        window.addEventListener('click', closeMenu);
        return () => window.removeEventListener('click', closeMenu);
    }, []);

    const handleGlobalAction = async (action) => {
        if (action === 'open') await locationService.openAll();
        else await locationService.closeAll();
        loadData();
    };

    return (
        <div className="p-6 bg-slate-900 min-h-screen text-slate-100 space-y-6">

            {/* RIGA 1: LE 4 CARD KPI */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                <div className="bg-slate-800 p-4 rounded-xl border border-slate-700 shadow-lg">
                    <p className="text-slate-400 text-sm font-medium uppercase tracking-wider">Sedi Totali</p>
                    <p className="text-3xl font-bold mt-2 text-white">{locations.length}</p>
                </div>
                <div className="bg-slate-800 p-4 rounded-xl border border-slate-700 shadow-lg border-l-4 border-l-emerald-500">
                    <p className="text-slate-400 text-sm font-medium uppercase tracking-wider">Sedi Aperte</p>
                    <p className="text-3xl font-bold mt-2 text-emerald-400">{openCount}</p>
                </div>
                <div className="bg-slate-800 p-4 rounded-xl border border-slate-700 shadow-lg border-l-4 border-l-rose-500">
                    <p className="text-slate-400 text-sm font-medium uppercase tracking-wider">Sedi Chiuse</p>
                    <p className="text-3xl font-bold mt-2 text-rose-400">{closedCount}</p>
                </div>
                {/* KPI calibrato sul BE: Tasso operatività globale */}
                <div className="bg-slate-800 p-4 rounded-xl border border-slate-700 shadow-lg">
                    <p className="text-slate-400 text-sm font-medium uppercase tracking-wider">Efficienza Operativa</p>
                    <p className="text-3xl font-bold mt-2 text-sky-400">
                        {locations.length ? Math.round((openCount / locations.length) * 100) : 0}%
                    </p>
                </div>
            </div>

            {/* RIGA 2: MAPPA E TABELLA */}
            <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 relative">

                {/* CARD MAPPA (Occupa 5 colonne) */}
                <div className={`lg:col-span-5 bg-slate-800 rounded-xl border border-slate-700 shadow-lg p-4 flex flex-col h-[450px] transition-all duration-300 ${isMapMaximized ? 'fixed inset-10 z-50 h-auto shadow-2xl ring-1 ring-slate-700' : 'relative'
                    }`}>
                    <div className="flex justify-between items-center mb-3">
                        <h3 className="text-lg font-semibold text-white">Geolocalizzazione Sedi</h3>
                    </div>

                    <div className="flex-1 rounded-lg overflow-hidden z-10 relative">
                        <MapContainer center={mapCenter} zoom={6} className="h-full w-full">
                            <TileLayer
                                url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png" // Dark mode map matching slate dashboard
                                attribution='&copy; OpenStreetMap contributors'
                            />
                            <ChangeView center={mapCenter} />
                            <ResizeMap isMaximized={isMapMaximized} />
                            {locations.map(loc => {
                                const coords = geoLocations[loc.id];
                                if (!coords) return null;
                                return (
                                    <Marker
                                        key={loc.id}
                                        position={coords}
                                        icon={customAzureIcon}
                                        eventHandlers={{
                                            contextmenu: (e) => handleContextMenu(e.originalEvent, loc.id),
                                            click: () => setMapCenter(coords)
                                        }}
                                    >
                                        <Popup>
                                            <div className="text-slate-900 font-sans">
                                                <strong className="block text-sm">{loc.name || 'Sede'}</strong>
                                                <span className="text-xs text-slate-600">{loc.address}, {loc.city}</span>
                                            </div>
                                        </Popup>
                                    </Marker>
                                );
                            })}
                        </MapContainer>

                        {/* Tasto Ingrandimento personalizzato in basso a destra */}
                        <button
                            onClick={() => setIsMapMaximized(!isMapMaximized)}
                            className="absolute bottom-4 right-4 z-[1000] bg-slate-900/80 hover:bg-sky-600 text-white p-2 rounded-lg backdrop-blur-md border border-slate-700 shadow-lg transition-colors"
                        >
                            {isMapMaximized ? (
                                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" /></svg>
                            ) : (
                                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 8V4m0 0h4M4 4l5 5m11-1V4m0 0h-4m4 0l-5 5M4 16v4m0 0h4m-4 0l5-5m11 5v-4m0 4h-4m4 0l-5-5" /></svg>
                            )}
                        </button>
                    </div>
                </div>

                {/* Sfondo sfocato quando la mappa è ingrandita (Bordo Esterno Sfocato richiesto) */}
                {isMapMaximized && (
                    <div className="fixed inset-0 bg-slate-950/60 backdrop-blur-md z-40 transition-opacity duration-300" onClick={() => setIsMapMaximized(false)} />
                )}

                {/* TABELLA INFO (Occupa 7 colonne) */}
                <div className="lg:col-span-7 bg-slate-800 rounded-xl border border-slate-700 shadow-lg p-4 overflow-hidden flex flex-col h-[450px]">
                    <div className="flex justify-between items-center mb-4">
                        <h3 className="text-lg font-semibold text-white">Registro Anagrafico Sedi</h3>
                        <button
                            onClick={openCreate}
                            className="inline-flex items-center gap-1.5 bg-sky-600 hover:bg-sky-500 text-white text-sm font-medium py-1.5 px-3 rounded-lg transition-colors shadow-lg shadow-sky-900/20"
                        >
                            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" /></svg>
                            Nuova sede
                        </button>
                    </div>
                    <div className="overflow-auto flex-1 custom-scrollbar">
                        <table className="w-full text-left border-collapse">
                            <thead>
                                <tr className="border-b border-slate-700 text-slate-400 text-sm">
                                    <th className="pb-3 pl-2">Nome sede</th>
                                    <th className="pb-3">Città</th>
                                    <th className="pb-3">Stato</th>
                                    <th className="pb-3 text-right pr-2">Operatività</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-700/50 text-sm">
                                {locations.map((loc) => (
                                    <tr
                                        key={loc.id}
                                        onContextMenu={(e) => handleContextMenu(e, loc.id)}
                                        onClick={() => geoLocations[loc.id] && setMapCenter(geoLocations[loc.id])}
                                        className="hover:bg-slate-700/40 cursor-pointer transition-colors"
                                    >
                                        <td className="py-3 pl-2 font-medium text-white">{loc.name || '—'}</td>
                                        <td className="py-3 text-slate-300">{loc.city}</td>
                                        <td className="py-3">
                                            <span className="px-2 py-1 rounded text-xs bg-slate-900 text-slate-300 border border-slate-700">
                                                {loc.status || 'N/D'}
                                            </span>
                                        </td>
                                        <td className="py-3 text-right pr-2">
                                            <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium ${loc.isOpen ? 'bg-emerald-500/10 text-emerald-400' : 'bg-rose-500/10 text-rose-400'
                                                }`}>
                                                <span className={`h-1.5 w-1.5 rounded-full ${loc.isOpen ? 'bg-emerald-400' : 'bg-rose-400'}`} />
                                                {loc.isOpen ? 'Aperta' : 'Chiusa'}
                                            </span>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            {/* RIGA 3: OPERAZIONI DI MASSA & MONITORAGGIO (Basato su BE) */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">

                {/* Pannello di controllo globale (Aperture/Chiusure massive via Spring) */}
                <div className="bg-slate-800 p-5 rounded-xl border border-slate-700 shadow-lg flex flex-col justify-between">
                    <div>
                        <h4 className="text-md font-semibold text-white mb-1">Interruttore Globale Enterprise</h4>
                        <p className="text-xs text-slate-400 mb-4">Invia comandi immediati di override a tutte le filiali del sistema.</p>
                    </div>
                    <div className="flex gap-3">
                        <button
                            onClick={() => handleGlobalAction('open')}
                            className="flex-1 bg-emerald-600 hover:bg-emerald-500 text-white font-medium text-sm py-2 px-4 rounded-lg transition-colors shadow-lg shadow-emerald-900/20"
                        >
                            Apri Tutte
                        </button>
                        <button
                            onClick={() => handleGlobalAction('close')}
                            className="flex-1 bg-rose-600 hover:bg-rose-500 text-white font-medium text-sm py-2 px-4 rounded-lg transition-colors shadow-lg shadow-rose-900/20"
                        >
                            Chiudi Tutte
                        </button>
                    </div>
                </div>

                {/* Logistica: scorte critiche per sede (dati reali da /stock/low) */}
                <div className="md:col-span-2 bg-slate-800 p-5 rounded-xl border border-slate-700 shadow-lg">
                    <div className="flex items-center justify-between mb-3">
                        <h4 className="text-md font-semibold text-white">Logistica — Scorte critiche per sede</h4>
                        <span className="text-xs text-slate-400">soglia 10 unità</span>
                    </div>

                    {locations.length === 0 ? (
                        <p className="text-sm text-slate-400">Nessuna sede da monitorare.</p>
                    ) : (
                        <div className="space-y-2">
                            {locations.map((loc) => {
                                const ls = lowStock[loc.id] ?? { critical: 0, empty: 0 };
                                const totalAlert = ls.critical + ls.empty;
                                return (
                                    <button
                                        key={loc.id}
                                        onClick={() => window.location.href = `/sedi/${loc.id}`}
                                        className="w-full flex items-center justify-between gap-3 bg-slate-900/60 hover:bg-slate-700/40 border border-slate-700 rounded-lg px-3 py-2 transition-colors text-left"
                                    >
                                        <div className="flex items-center gap-2 min-w-0">
                                            <span className={`h-2 w-2 rounded-full shrink-0 ${totalAlert === 0 ? 'bg-emerald-400' : ls.empty > 0 ? 'bg-rose-400' : 'bg-amber-400'}`} />
                                            <span className="text-sm font-medium text-white truncate">{loc.name || loc.city}</span>
                                        </div>
                                        <div className="flex items-center gap-2 shrink-0">
                                            <span className="text-xs px-2 py-0.5 rounded-full bg-amber-500/10 text-amber-400 border border-amber-500/20">
                                                {ls.critical} critici
                                            </span>
                                            <span className="text-xs px-2 py-0.5 rounded-full bg-rose-500/10 text-rose-400 border border-rose-500/20">
                                                {ls.empty} esauriti
                                            </span>
                                        </div>
                                    </button>
                                );
                            })}
                        </div>
                    )}
                    <p className="mt-3 text-xs text-slate-500">Clicca una sede per ispezionare e rifornire le scorte nel dettaglio.</p>
                </div>
            </div>

            {/* COMPONENTE MENU CONTESTUALE (Tasto Destro Flottante) */}
            {contextMenu.visible && (
                <div
                    className="fixed bg-slate-800 border border-slate-700 rounded-lg shadow-2xl py-1 w-48 z-[9999] font-sans animate-in fade-in zoom-in-95 duration-100"
                    style={{ top: contextMenu.y, left: contextMenu.x }}
                >
                    <button
                        onClick={() => window.location.href = `/sedi/${contextMenu.targetId}`}
                        className="w-full text-left px-4 py-2 text-sm text-slate-200 hover:bg-sky-600 hover:text-white transition-colors flex items-center gap-2"
                    >
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" /><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" /></svg>
                        Visualizza sede
                    </button>
                    <button
                        onClick={() => openEdit(contextMenu.targetId)}
                        className="w-full text-left px-4 py-2 text-sm text-slate-200 hover:bg-sky-600 hover:text-white transition-colors flex items-center gap-2"
                    >
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" /></svg>
                        Modifica
                    </button>
                    <div className="my-1 border-t border-slate-700" />
                    <button
                        onClick={() => handleDelete(contextMenu.targetId)}
                        className="w-full text-left px-4 py-2 text-sm text-rose-400 hover:bg-rose-600 hover:text-white transition-colors flex items-center gap-2"
                    >
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" /></svg>
                        Elimina
                    </button>
                </div>
            )}

            {/* MODAL CRUD SEDE (compatto, minimale) */}
            {modal.open && (
                <div
                    className="fixed inset-0 z-[10000] flex items-center justify-center bg-slate-950/70 backdrop-blur-sm p-4"
                    onClick={closeModal}
                >
                    <form
                        onSubmit={handleSave}
                        onClick={(e) => e.stopPropagation()}
                        className="w-full max-w-md bg-slate-800 rounded-xl border border-slate-700 shadow-2xl p-6 space-y-4"
                    >
                        <div className="flex justify-between items-center">
                            <h3 className="text-lg font-semibold text-white">
                                {modal.editing ? `Modifica sede #${modal.editing.id}` : 'Nuova sede'}
                            </h3>
                            <button type="button" onClick={closeModal} className="text-slate-400 hover:text-white transition-colors">
                                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" /></svg>
                            </button>
                        </div>

                        <div>
                            <label className="block text-xs text-slate-400 uppercase tracking-wider mb-1">Nome</label>
                            <input
                                type="text" value={form.name} required
                                onChange={(e) => setForm({ ...form, name: e.target.value })}
                                className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:ring-1 focus:ring-sky-500"
                            />
                        </div>
                        <div>
                            <label className="block text-xs text-slate-400 uppercase tracking-wider mb-1">Indirizzo</label>
                            <input
                                type="text" value={form.address} required
                                onChange={(e) => setForm({ ...form, address: e.target.value })}
                                className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:ring-1 focus:ring-sky-500"
                            />
                        </div>
                        <div className="grid grid-cols-2 gap-3">
                            <div>
                                <label className="block text-xs text-slate-400 uppercase tracking-wider mb-1">Città</label>
                                <input
                                    type="text" value={form.city} required
                                    onChange={(e) => setForm({ ...form, city: e.target.value })}
                                    className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:ring-1 focus:ring-sky-500"
                                />
                            </div>
                            <div>
                                <label className="block text-xs text-slate-400 uppercase tracking-wider mb-1">Telefono</label>
                                <input
                                    type="text" value={form.phoneNumber}
                                    onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })}
                                    className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:ring-1 focus:ring-sky-500"
                                />
                            </div>
                        </div>
                        <label className="flex items-center gap-2 text-sm text-slate-300 cursor-pointer select-none">
                            <input
                                type="checkbox" checked={form.isOpen}
                                onChange={(e) => setForm({ ...form, isOpen: e.target.checked })}
                                className="h-4 w-4 rounded border-slate-600 bg-slate-900 text-sky-500 focus:ring-sky-500"
                            />
                            Sede aperta
                        </label>

                        {/* Account manager: solo in creazione di una nuova sede */}
                        {!modal.editing && (
                            <div className="pt-2 mt-2 border-t border-slate-700 space-y-3">
                                <div className="flex items-center gap-2">
                                    <span className="text-xs font-semibold text-slate-300 uppercase tracking-wider">Manager della sede</span>
                                    <span className="text-[10px] text-slate-500">(account dipendente — opzionale)</span>
                                </div>
                                <div className="grid grid-cols-2 gap-3">
                                    <div>
                                        <label className="block text-xs text-slate-400 uppercase tracking-wider mb-1">Nome</label>
                                        <input
                                            type="text" value={form.managerName}
                                            onChange={(e) => setForm({ ...form, managerName: e.target.value })}
                                            className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:ring-1 focus:ring-sky-500"
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-xs text-slate-400 uppercase tracking-wider mb-1">Cognome</label>
                                        <input
                                            type="text" value={form.managerSurname}
                                            onChange={(e) => setForm({ ...form, managerSurname: e.target.value })}
                                            className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:ring-1 focus:ring-sky-500"
                                        />
                                    </div>
                                </div>
                                <div>
                                    <label className="block text-xs text-slate-400 uppercase tracking-wider mb-1">Email aziendale</label>
                                    <input
                                        type="email" value={form.managerEmail} autoComplete="off"
                                        onChange={(e) => setForm({ ...form, managerEmail: e.target.value })}
                                        placeholder="manager@bluecrystal.it"
                                        className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:ring-1 focus:ring-sky-500"
                                    />
                                </div>
                                <div>
                                    <label className="block text-xs text-slate-400 uppercase tracking-wider mb-1">Password</label>
                                    <input
                                        type="password" value={form.managerPassword} autoComplete="new-password"
                                        onChange={(e) => setForm({ ...form, managerPassword: e.target.value })}
                                        placeholder="••••••••"
                                        className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:ring-1 focus:ring-sky-500"
                                    />
                                    <p className="text-[10px] text-slate-500 mt-1">Compila email + password per creare subito l'account Manager di questa sede.</p>
                                </div>
                            </div>
                        )}

                        <div className="flex gap-3 pt-2">
                            <button
                                type="button" onClick={closeModal}
                                className="flex-1 bg-slate-700 hover:bg-slate-600 text-slate-200 font-medium text-sm py-2 rounded-lg transition-colors"
                            >
                                Annulla
                            </button>
                            <button
                                type="submit" disabled={saving}
                                className="flex-1 bg-sky-600 hover:bg-sky-500 disabled:opacity-50 text-white font-medium text-sm py-2 rounded-lg transition-colors"
                            >
                                {saving ? 'Salvataggio…' : modal.editing ? 'Salva modifiche' : 'Crea sede'}
                            </button>
                        </div>
                    </form>
                </div>
            )}

        </div>
    );
}