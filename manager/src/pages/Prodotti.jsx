import { useState, useEffect, useCallback, Fragment } from "react";
import productService from "../service/ProductService"; // Importa il service appena configurato
import requestService from "../service/RequestService"; // Manager: creazione = richiesta da approvare
import menuService from "../service/MenuService";
import pushNotificationService from "../service/PushNotificationService";
import { notifyPushSent, notifyPushError } from "../components/ui/PushToast";

// Helper valuta fedele al tuo stile originale
const fmt = (n) => new Intl.NumberFormat("it-IT", { style: "currency", currency: "EUR" }).format(n ?? 0);

// ─── ICONE SVG (no emoji, da regola di progetto) ─────────────────────────────
const Icon = {
    crown: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" {...p}><path d="m2 4 3 12h14l3-12-6 7-4-7-4 7-6-7z" /><path d="M5 20h14" /></svg>),
    gem: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" {...p}><path d="M6 3h12l4 6-10 13L2 9z" /><path d="M11 3 8 9l4 13 4-13-3-6" /><path d="M2 9h20" /></svg>),
    box: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...p}><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z" /><polyline points="3.27 6.96 12 12.01 20.73 6.96" /><line x1="12" y1="22.08" x2="12" y2="12" /></svg>),
    hash: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" {...p}><line x1="4" y1="9" x2="20" y2="9" /><line x1="4" y1="15" x2="20" y2="15" /><line x1="10" y1="3" x2="8" y2="21" /><line x1="16" y1="3" x2="14" y2="21" /></svg>),
    image: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...p}><rect x="3" y="3" width="18" height="18" rx="2" ry="2" /><circle cx="8.5" cy="8.5" r="1.5" /><polyline points="21 15 16 10 5 21" /></svg>),
    camera: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" {...p}><path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z" /><circle cx="12" cy="13" r="4" /></svg>),
    close: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...p}><line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" /></svg>),
    layers: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" {...p}><polygon points="12 2 2 7 12 12 22 7 12 2" /><polyline points="2 17 12 22 22 17" /><polyline points="2 12 12 17 22 12" /></svg>),
    chevron: (p) => (<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" {...p}><polyline points="6 9 12 15 18 9" /></svg>),
};

// Componente KPI Card mappato 1:1 dal tuo design Command Center
const KpiCard = ({ label, value, icon, subtext, accentColor }) => (
    <div className="bg-[#111318] border border-white/5 rounded-[18px] p-[22px_24px] relative overflow-hidden transition-all duration-200 hover:border-[#38b6ff]/25 hover:-translate-y-0.5 group">
        <div className="absolute top-0 left-0 right-0 h-[2px] opacity-70" style={{ backgroundColor: accentColor }} />
        <div className="flex items-center justify-between mb-[14px]">
            <span className="text-[11px] font-semibold tracking-[1px] uppercase text-[#8b92a8]">{label}</span>
            <span className="w-[34px] h-[34px] rounded-[9px] flex items-center justify-center transition-transform group-hover:scale-110" style={{ background: `${accentColor}18`, color: accentColor }}>
                {icon}
            </span>
        </div>
        <div className="font-['Syne'] text-[28px] font-bold leading-none mb-2 text-[#f0f4ff] truncate">{value}</div>
        {subtext && <div className="text-[12px] font-medium text-[#8b92a8] truncate">{subtext}</div>}
    </div>
);

export default function ProductDashboard() {
    const [products, setProducts] = useState([]);
    const [menus, setMenus] = useState([]);
    const [topSeller, setTopSeller] = useState(null);
    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false);

    // Stati Gestione Modale (Form)
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isEditing, setIsEditing] = useState(false);
    const [currentId, setCurrentId] = useState(null);
    const [formData, setFormData] = useState({ name: "", price: "", category: "" });
    const [selectedFile, setSelectedFile] = useState(null);
    const [imagePreview, setImagePreview] = useState(null);

    // Stati Menu (riga espansa + modale creazione)
    const [expandedMenuId, setExpandedMenuId] = useState(null);
    const [isMenuModalOpen, setIsMenuModalOpen] = useState(false);
    const [savingMenu, setSavingMenu] = useState(false);
    const [menuForm, setMenuForm] = useState({ name: "", price: "", description: "" });
    const [menuFile, setMenuFile] = useState(null);
    const [menuImagePreview, setMenuImagePreview] = useState(null);
    // Mappa { [productId]: { selected, quantity, obligatory } }
    const [menuItems, setMenuItems] = useState({});

    // Caricamento Dati Backend
    const fetchAllData = useCallback(async () => {
        try {
            const [allProducts, bestSellingList, allMenus] = await Promise.all([
                productService.getAllProducts(),
                productService.getBestSelling(1), // Ottieni il re delle vendite
                menuService.getAll()
            ]);
            setProducts(allProducts || []);
            setTopSeller(bestSellingList?.[0] || null);
            setMenus(allMenus || []);
        } catch (err) {
            console.error("Errore caricamento prodotti:", err);
        } finally {
            setLoading(false);
            setRefreshing(false);
        }
    }, []);

    useEffect(() => { fetchAllData(); }, [fetchAllData]);

    // Calcolo metriche derivate per le KPI Cards
    const maxPriceProduct = products.length > 0 ? [...products].sort((a, b) => b.price - a.price)[0] : null;
    const totalCategories = [...new Set(products.map(p => p.category))].length;

    // Gestione Modale
    const openCreateModal = () => {
        setIsEditing(false);
        setFormData({ name: "", price: "", category: "" });
        setSelectedFile(null);
        setImagePreview(null);
        setIsModalOpen(true);
    };

    const openEditModal = (product) => {
        setIsEditing(true);
        setCurrentId(product.id);
        setFormData({ name: product.name, price: product.price, category: product.category });
        setSelectedFile(null);
        setImagePreview(product.imageUrl || null);
        setIsModalOpen(true);
    };

    const handleFileChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            setSelectedFile(file);
            setImagePreview(URL.createObjectURL(file));
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            // MANAGER: prodotti NON modificabili direttamente → tutto via richiesta.
            // L'eventuale immagine viene caricata e referenziata come imgPath.
            if (isEditing) {
                await requestService.updateProduct(currentId, formData, selectedFile);
            } else {
                await requestService.createProduct(formData, selectedFile);
            }
            setIsModalOpen(false);
            alert("Richiesta inviata all'Admin per approvazione.\nLa trovi in 'Richieste' (stato In attesa).");
        } catch (err) {
            alert("Operazione fallita: " + (err?.response?.data || err.message));
        }
    };

    const handleDelete = async (id, name) => {
        if (window.confirm(`Inviare all'Admin la richiesta di eliminare il prodotto "${name}"?`)) {
            try {
                await requestService.deleteProduct(id, name);
                alert("Richiesta di eliminazione inviata all'Admin.\nLa trovi in 'Richieste'.");
            } catch (err) {
                alert("Operazione fallita: " + (err?.response?.data || err.message));
            }
        }
    };

    // ── Gestione Menu ────────────────────────────────────────────────────────
    const toggleMenuRow = (id) => setExpandedMenuId((prev) => (prev === id ? null : id));

    const openMenuModal = () => {
        setMenuForm({ name: "", price: "", description: "" });
        setMenuFile(null);
        setMenuImagePreview(null);
        setMenuItems({});
        setIsMenuModalOpen(true);
    };

    const handleMenuFileChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            setMenuFile(file);
            setMenuImagePreview(URL.createObjectURL(file));
        }
    };

    const toggleMenuItem = (productId) => {
        setMenuItems((prev) => {
            const existing = prev[productId];
            if (existing?.selected) {
                return { ...prev, [productId]: { ...existing, selected: false } };
            }
            return { ...prev, [productId]: { selected: true, quantity: existing?.quantity || 1, obligatory: existing?.obligatory || false } };
        });
    };

    const updateMenuItem = (productId, patch) => {
        setMenuItems((prev) => ({ ...prev, [productId]: { ...prev[productId], ...patch } }));
    };

    const handleMenuSubmit = async (e) => {
        e.preventDefault();
        const selected = Object.entries(menuItems)
            .filter(([, v]) => v.selected)
            .map(([productId, v]) => ({ productId: Number(productId), quantity: Number(v.quantity) || 1, obligatory: !!v.obligatory }));

        if (selected.length === 0) {
            alert("Seleziona almeno un prodotto da includere nel menu.");
            return;
        }

        setSavingMenu(true);
        try {
            // MANAGER: menu NON creabili direttamente → richiesta all'Admin (con immagine).
            await requestService.createMenu(menuForm, selected, menuFile);
            setIsMenuModalOpen(false);
            alert("Richiesta inviata all'Admin per approvazione.\nLa trovi in 'Richieste'.");
        } catch (err) {
            alert("Operazione fallita: " + (err?.response?.data || err?.message || "errore sconosciuto"));
        } finally {
            setSavingMenu(false);
        }
    };

    const handleMenuDelete = async (id, name) => {
        if (window.confirm(`Inviare all'Admin la richiesta di eliminare il menu "${name}"?`)) {
            try {
                await requestService.deleteMenu(id, name);
                alert("Richiesta di eliminazione inviata all'Admin.\nLa trovi in 'Richieste'.");
            } catch (err) {
                alert("Operazione fallita: " + (err?.response?.data || err.message));
            }
        }
    };

    if (loading) {
        return (
            <div className="flex h-screen items-center justify-center bg-[#0b0c10] text-[14px] font-semibold text-[#38b6ff] tracking-widest font-['Syne'] uppercase animate-pulse">
                Sincronizzazione Menu Cryo...
            </div>
        );
    }

    return (
        <div className="p-[28px_32px] max-w-[1440px] mx-auto text-[#f0f4ff] bg-[#0b0c10] min-h-screen">

            {/* Header */}
            <div className="flex items-end justify-between mb-8">
                <div>
                    <h1 className="font-['Syne'] text-[26px] font-extrabold tracking-tight mb-1">Gestione Catalogo</h1>
                    <p className="text-[13px] text-[#8b92a8] uppercase tracking-wide">Configurazione prodotti e allineamento listino</p>
                </div>
                <div className="flex items-center gap-3">
                    <button
                        onClick={() => { setRefreshing(true); fetchAllData(); }}
                        className="flex items-center gap-2 bg-[#111318] border border-white/5 rounded-xl px-3.5 py-2.5 text-[12px] font-medium text-[#8b92a8] hover:border-[#38b6ff] hover:text-[#38b6ff] transition-all"
                    >
                        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" className={refreshing ? "animate-spin" : ""}><polyline points="23 4 23 10 17 10" /><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" /></svg>
                        Aggiorna
                    </button>
                    <button
                        onClick={openMenuModal}
                        className="flex items-center gap-2 bg-[#a78bfa] text-[#111318] rounded-xl px-4 py-2.5 text-[12px] font-bold tracking-wide hover:bg-[#a78bfa]/90 transition-all shadow-lg shadow-[#a78bfa]/10"
                    >
                        <Icon.layers width="14" height="14" />
                        <span>NUOVO MENU</span>
                    </button>
                    <button
                        onClick={openCreateModal}
                        className="flex items-center gap-2 bg-[#38b6ff] text-[#111318] rounded-xl px-4 py-2.5 text-[12px] font-bold tracking-wide hover:bg-[#38b6ff]/90 transition-all shadow-lg shadow-[#38b6ff]/10"
                    >
                        <span>+ AGGIUNGI PRODOTTO</span>
                    </button>
                </div>
            </div>

            {/* KPI Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
                <KpiCard label="Prodotto Top Seller" value={topSeller ? topSeller.name : "N/D"} icon={<Icon.crown width="16" height="16" />} subtext="In cima alle vendite" accentColor="#38b6ff" />
                <KpiCard label="Articolo Premium" value={maxPriceProduct ? fmt(maxPriceProduct.price) : "—"} icon={<Icon.gem width="16" height="16" />} subtext={maxPriceProduct?.name || "Nessun prodotto"} accentColor="#a78bfa" />
                <KpiCard label="Articoli a Menu" value={products.length} icon={<Icon.box width="16" height="16" />} subtext="Referenze attive a DB" accentColor="#22d3a0" />
                <KpiCard label="Categorie Macro" value={totalCategories} icon={<Icon.hash width="16" height="16" />} subtext="Raggruppamenti menu" accentColor="#fbbf24" />
            </div>

            {/* Griglia a due colonne: Prodotti (sinistra) · Menu (destra) */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 items-start">

            {/* Tabella Prodotti */}
            <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
                <div className="flex items-center justify-between mb-5">
                    <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff]">Tutti i prodotti nel database</h3>
                    <span className="text-[11px] font-semibold px-2 py-0.5 rounded-full bg-[#22d3a0]/10 text-[#22d3a0] border border-[#22d3a0]/20">Attivi</span>
                </div>

                <div className="overflow-x-auto">
                    <table className="w-full">
                        <thead>
                            <tr className="border-b border-white/5">
                                <th className="text-left text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3 w-[80px]">Preview</th>
                                <th className="text-left text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3">Nome Prodotto</th>
                                <th className="text-left text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3">Categoria</th>
                                <th className="text-left text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3">Prezzo Listino</th>
                                <th className="text-right text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3">Azioni amministratore</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-white/[0.04]">
                            {products.map((product) => (
                                <tr key={product.id} className="hover:bg-[#181c23] transition-colors group">
                                    <td className="py-3 px-3">
                                        <div className="w-[44px] h-[44px] rounded-xl bg-[#181c23] border border-white/5 overflow-hidden flex items-center justify-center">
                                            {product.imageUrl ? (
                                                <img src={product.imageUrl} alt={product.name} className="w-full h-full object-cover" />
                                            ) : (
                                                <Icon.image width="18" height="18" className="text-[#4e5566]" />
                                            )}
                                        </div>
                                    </td>
                                    <td className="py-3 px-3 font-['Syne'] font-semibold text-[#f0f4ff] text-[13px]">{product.name}</td>
                                    <td className="py-3 px-3 text-[#8b92a8] text-[13px]">
                                        <span className="bg-[#181c23] px-2.5 py-1 rounded-md text-[11px] font-semibold border border-white/5 tracking-wider uppercase text-[#38b6ff]">
                                            {product.category}
                                        </span>
                                    </td>
                                    <td className="py-3 px-3 font-['Syne'] font-bold text-[#22d3a0] text-[13px]">{fmt(product.price)}</td>
                                    <td className="py-3 px-3 text-right">
                                        <div className="flex justify-end gap-2 opacity-60 group-hover:opacity-100 transition-opacity">
                                            <button
                                                onClick={() => openEditModal(product)}
                                                className="p-2 rounded-lg bg-[#181c23] border border-white/5 hover:border-[#38b6ff]/50 text-[#8b92a8] hover:text-[#38b6ff] transition-all"
                                                title="Modifica scheda"
                                            >
                                                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M11 5H6a2 2 0 0 0-2 2v11a2 2 0 0 0 2 2h11a2 2 0 0 0 2-2v-5" /><path d="M18.5 2.5a2.121 2.121 0 1 1 3 3L12 15l-4 1 1-4 9.5-9.5z" /></svg>
                                            </button>
                                            <button
                                                onClick={() => handleDelete(product.id, product.name)}
                                                className="p-2 rounded-lg bg-[#181c23] border border-white/5 hover:border-[#ff5e5e]/50 text-[#8b92a8] hover:text-[#ff5e5e] transition-all"
                                                title="Elimina dal database"
                                            >
                                                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="3 6 5 6 21 6" /><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" /></svg>
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                            {products.length === 0 && (
                                <tr>
                                    <td colSpan="5" className="text-center py-8 text-[13px] text-[#4e5566]">Nessun prodotto disponibile in catalogo.</td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* Tabella Menu */}
            <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
                <div className="flex items-center justify-between mb-5">
                    <h3 className="font-['Syne'] text-[15px] font-bold text-[#f0f4ff]">Menu esistenti</h3>
                    <span className="text-[11px] font-semibold px-2 py-0.5 rounded-full bg-[#a78bfa]/10 text-[#a78bfa] border border-[#a78bfa]/20">{menus.length} menu</span>
                </div>

                <div className="overflow-x-auto">
                    <table className="w-full">
                        <thead>
                            <tr className="border-b border-white/5">
                                <th className="text-left text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3">Nome Menu</th>
                                <th className="text-left text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3">Prezzo</th>
                                <th className="text-left text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3">Prodotti</th>
                                <th className="text-right text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] pb-3 px-3">Dettaglio</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-white/[0.04]">
                            {menus.map((menu) => {
                                const isOpen = expandedMenuId === menu.id;
                                const items = menu.menuProducts || [];
                                return (
                                    <Fragment key={menu.id}>
                                        <tr className="hover:bg-[#181c23] transition-colors group">
                                            <td className="py-3 px-3 font-['Syne'] font-semibold text-[#f0f4ff] text-[13px]">{menu.name}</td>
                                            <td className="py-3 px-3 font-['Syne'] font-bold text-[#22d3a0] text-[13px]">{fmt(menu.price)}</td>
                                            <td className="py-3 px-3">
                                                <span className="bg-[#181c23] px-2.5 py-1 rounded-md text-[11px] font-semibold border border-white/5 tracking-wider uppercase text-[#a78bfa]">
                                                    {items.length} ref.
                                                </span>
                                            </td>
                                            <td className="py-3 px-3 text-right">
                                                <div className="flex justify-end gap-2">
                                                    <button
                                                        onClick={() => toggleMenuRow(menu.id)}
                                                        className={`flex items-center gap-1.5 p-2 rounded-lg bg-[#181c23] border text-[11px] font-semibold transition-all ${isOpen ? "border-[#a78bfa]/50 text-[#a78bfa]" : "border-white/5 text-[#8b92a8] hover:border-[#a78bfa]/50 hover:text-[#a78bfa]"}`}
                                                        title="Mostra prodotti del menu"
                                                    >
                                                        <span>Dettaglio</span>
                                                        <Icon.chevron width="13" height="13" className={`transition-transform ${isOpen ? "rotate-180" : ""}`} />
                                                    </button>
                                                    <button
                                                        onClick={() => handleMenuDelete(menu.id, menu.name)}
                                                        className="p-2 rounded-lg bg-[#181c23] border border-white/5 hover:border-[#ff5e5e]/50 text-[#8b92a8] hover:text-[#ff5e5e] transition-all"
                                                        title="Elimina menu"
                                                    >
                                                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="3 6 5 6 21 6" /><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" /></svg>
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                        {isOpen && (
                                            <tr className="bg-[#0e1015]">
                                                <td colSpan="4" className="px-3 pb-4 pt-1">
                                                    {menu.description && (
                                                        <p className="text-[12px] text-[#8b92a8] mb-3 italic">{menu.description}</p>
                                                    )}
                                                    {items.length === 0 ? (
                                                        <div className="text-[12px] text-[#4e5566] py-2">Nessun prodotto associato a questo menu.</div>
                                                    ) : (
                                                        <div className="space-y-1.5">
                                                            <div className="text-[10px] font-semibold tracking-wider uppercase text-[#4e5566] mb-2">Prodotti inclusi</div>
                                                            {items.map((it) => (
                                                                <div key={it.productId} className="flex items-center justify-between bg-[#181c23] border border-white/5 rounded-lg px-3 py-2">
                                                                    <div className="flex items-center gap-2">
                                                                        <span className="text-[13px] font-medium text-[#f0f4ff]">{it.productName}</span>
                                                                        {it.obligatory && (
                                                                            <span className="text-[9px] font-bold px-1.5 py-0.5 rounded bg-[#fbbf24]/10 text-[#fbbf24] border border-[#fbbf24]/20 uppercase tracking-wide">Obbligatorio</span>
                                                                        )}
                                                                    </div>
                                                                    <div className="flex items-center gap-3">
                                                                        <span className="text-[12px] text-[#8b92a8]">x{it.quantity}</span>
                                                                        {it.unitPrice != null && (
                                                                            <span className="font-['Syne'] text-[12px] font-bold text-[#22d3a0]">{fmt(it.unitPrice)}</span>
                                                                        )}
                                                                    </div>
                                                                </div>
                                                            ))}
                                                        </div>
                                                    )}
                                                </td>
                                            </tr>
                                        )}
                                    </Fragment>
                                );
                            })}
                            {menus.length === 0 && (
                                <tr>
                                    <td colSpan="4" className="text-center py-8 text-[13px] text-[#4e5566]">Nessun menu configurato.</td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            </div>{/* /griglia due colonne */}

            {/* Modale Overlay (Creazione/Modifica) */}
            {isModalOpen && (
                <div className="fixed inset-0 bg-[#000]/70 backdrop-blur-md z-50 flex items-center justify-center p-4">
                    <div className="bg-[#111318] border border-white/10 rounded-[22px] max-w-md w-full p-6 shadow-2xl relative animate-fade-in">

                        <div className="flex items-center justify-between mb-6 border-b border-white/5 pb-4">
                            <h2 className="font-['Syne'] text-[18px] font-bold text-[#f0f4ff]">
                                {isEditing ? "Modifica Scheda Prodotto" : "Registra Nuovo Prodotto"}
                            </h2>
                            <button
                                onClick={() => setIsModalOpen(false)}
                                className="text-[#8b92a8] hover:text-[#f0f4ff] transition-colors"
                            >
                                <Icon.close width="18" height="18" />
                            </button>
                        </div>

                        <form onSubmit={handleSubmit} className="space-y-4">
                            {/* Input Nome */}
                            <div>
                                <label className="block text-[10px] font-semibold uppercase tracking-wider text-[#8b92a8] mb-1.5">Nome Prodotto</label>
                                <input
                                    type="text"
                                    required
                                    value={formData.name}
                                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                    className="w-full bg-[#181c23] border border-white/5 rounded-xl px-4 py-2.5 text-[13px] text-[#f0f4ff] focus:outline-none focus:border-[#38b6ff] transition-all"
                                    placeholder="Es. Blue Crystal Double Burger"
                                />
                            </div>

                            {/* Categoria & Prezzo */}
                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-[10px] font-semibold uppercase tracking-wider text-[#8b92a8] mb-1.5">Categoria</label>
                                    <select
                                        value={formData.category}
                                        onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                                        required
                                        className="w-full bg-[#181c23] border border-white/5 rounded-xl px-3 py-2.5 text-[13px] text-[#f0f4ff] focus:outline-none focus:border-[#38b6ff] transition-all"
                                    >
                                        <option value="" className="bg-[#111318]">Seleziona...</option>
                                        <option value="HAMBURGER" className="bg-[#111318]">Hamburger</option>
                                        <option value="FRIES" className="bg-[#111318]">Patatine</option>
                                        <option value="DRINK" className="bg-[#111318]">Bevande</option>
                                        <option value="SNACK" className="bg-[#111318]">Snack</option>
                                        <option value="ICE_CREAM" className="bg-[#111318]">Gelato</option>
                                        <option value="SALAD" className="bg-[#111318]">Insalata</option>
                                        <option value="WRAP" className="bg-[#111318]">Wrap</option>
                                        <option value="SAUCE" className="bg-[#111318]">Salse</option>
                                        <option value="DESSERT" className="bg-[#111318]">Dessert</option>
                                        <option value="BREAKFAST" className="bg-[#111318]">Colazione</option>
                                    </select>
                                </div>
                                <div>
                                    <label className="block text-[10px] font-semibold uppercase tracking-wider text-[#8b92a8] mb-1.5">Prezzo (€)</label>
                                    <input
                                        type="number"
                                        step="0.01"
                                        required
                                        value={formData.price}
                                        onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                                        className="w-full bg-[#181c23] border border-white/5 rounded-xl px-4 py-2.5 text-[13px] text-[#f0f4ff] focus:outline-none focus:border-[#38b6ff] transition-all"
                                        placeholder="Es. 8.50"
                                    />
                                </div>
                            </div>

                            {/* Zona Caricamento File */}
                            <div>
                                <label className="block text-[10px] font-semibold uppercase tracking-wider text-[#8b92a8] mb-1.5">Immagine Asset</label>
                                <div className="border border-dashed border-white/10 hover:border-[#38b6ff]/40 bg-[#181c23]/50 rounded-xl p-4 flex items-center gap-4 transition-all relative">
                                    <div className="w-[54px] h-[54px] rounded-lg bg-[#181c23] border border-white/5 overflow-hidden flex items-center justify-center shrink-0">
                                        {imagePreview ? (
                                            <img src={imagePreview} alt="Preview" className="w-full h-full object-cover" />
                                        ) : (
                                            <Icon.camera width="20" height="20" className="text-[#4e5566]" />
                                        )}
                                    </div>
                                    <div className="flex flex-col">
                                        <span className="text-[12px] font-medium text-[#38b6ff] cursor-pointer hover:underline">Sfoglia file asset</span>
                                        <span className="text-[10px] text-[#4e5566]">PNG o JPG (Max 5MB)</span>
                                    </div>
                                    <input
                                        type="file"
                                        accept="image/*"
                                        onChange={handleFileChange}
                                        className="absolute inset-0 opacity-0 cursor-pointer"
                                    />
                                </div>
                            </div>

                            {/* Azioni Modale */}
                            <div className="flex justify-end gap-3 pt-4 border-t border-white/5 mt-6">
                                <button
                                    type="button"
                                    onClick={() => setIsModalOpen(false)}
                                    className="bg-transparent border border-white/5 hover:bg-white/5 px-4 py-2 rounded-xl text-[12px] font-medium text-[#8b92a8] transition-all"
                                >
                                    Annulla
                                </button>
                                <button
                                    type="submit"
                                    className="bg-[#38b6ff] text-[#111318] px-5 py-2 rounded-xl text-[12px] font-bold tracking-wide hover:bg-[#38b6ff]/90 transition-all"
                                >
                                    {isEditing ? "SALVA MODIFICHE" : "CREA ASSET"}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Modale Nuovo Menu */}
            {isMenuModalOpen && (
                <div className="fixed inset-0 bg-[#000]/70 backdrop-blur-md z-50 flex items-center justify-center p-4">
                    <div className="bg-[#111318] border border-white/10 rounded-[22px] max-w-2xl w-full p-6 shadow-2xl relative animate-fade-in max-h-[90vh] overflow-y-auto">

                        <div className="flex items-center justify-between mb-6 border-b border-white/5 pb-4">
                            <h2 className="font-['Syne'] text-[18px] font-bold text-[#f0f4ff] flex items-center gap-2">
                                <Icon.layers width="18" height="18" className="text-[#a78bfa]" />
                                Crea Nuovo Menu
                            </h2>
                            <button
                                onClick={() => setIsMenuModalOpen(false)}
                                className="text-[#8b92a8] hover:text-[#f0f4ff] transition-colors"
                            >
                                <Icon.close width="18" height="18" />
                            </button>
                        </div>

                        <form onSubmit={handleMenuSubmit} className="space-y-4">
                            {/* Nome + Prezzo */}
                            <div className="grid grid-cols-1 sm:grid-cols-[1fr_140px] gap-4">
                                <div>
                                    <label className="block text-[10px] font-semibold uppercase tracking-wider text-[#8b92a8] mb-1.5">Nome Menu</label>
                                    <input
                                        type="text"
                                        required
                                        value={menuForm.name}
                                        onChange={(e) => setMenuForm({ ...menuForm, name: e.target.value })}
                                        className="w-full bg-[#181c23] border border-white/5 rounded-xl px-4 py-2.5 text-[13px] text-[#f0f4ff] focus:outline-none focus:border-[#a78bfa] transition-all"
                                        placeholder="Es. Menu Crystal Deluxe"
                                    />
                                </div>
                                <div>
                                    <label className="block text-[10px] font-semibold uppercase tracking-wider text-[#8b92a8] mb-1.5">Prezzo (€)</label>
                                    <input
                                        type="number"
                                        step="0.01"
                                        required
                                        value={menuForm.price}
                                        onChange={(e) => setMenuForm({ ...menuForm, price: e.target.value })}
                                        className="w-full bg-[#181c23] border border-white/5 rounded-xl px-4 py-2.5 text-[13px] text-[#f0f4ff] focus:outline-none focus:border-[#a78bfa] transition-all"
                                        placeholder="14.90"
                                    />
                                </div>
                            </div>

                            {/* Descrizione */}
                            <div>
                                <label className="block text-[10px] font-semibold uppercase tracking-wider text-[#8b92a8] mb-1.5">Descrizione</label>
                                <textarea
                                    rows={2}
                                    value={menuForm.description}
                                    onChange={(e) => setMenuForm({ ...menuForm, description: e.target.value })}
                                    className="w-full bg-[#181c23] border border-white/5 rounded-xl px-4 py-2.5 text-[13px] text-[#f0f4ff] focus:outline-none focus:border-[#a78bfa] transition-all resize-none"
                                    placeholder="Breve descrizione del menu..."
                                />
                            </div>

                            {/* Immagine */}
                            <div>
                                <label className="block text-[10px] font-semibold uppercase tracking-wider text-[#8b92a8] mb-1.5">Immagine Menu</label>
                                <div className="border border-dashed border-white/10 hover:border-[#a78bfa]/40 bg-[#181c23]/50 rounded-xl p-4 flex items-center gap-4 transition-all relative">
                                    <div className="w-[54px] h-[54px] rounded-lg bg-[#181c23] border border-white/5 overflow-hidden flex items-center justify-center shrink-0">
                                        {menuImagePreview ? (
                                            <img src={menuImagePreview} alt="Preview" className="w-full h-full object-cover" />
                                        ) : (
                                            <Icon.camera width="20" height="20" className="text-[#4e5566]" />
                                        )}
                                    </div>
                                    <div className="flex flex-col">
                                        <span className="text-[12px] font-medium text-[#a78bfa] cursor-pointer hover:underline">Sfoglia file asset</span>
                                        <span className="text-[10px] text-[#4e5566]">PNG o JPG (Max 5MB)</span>
                                    </div>
                                    <input
                                        type="file"
                                        accept="image/*"
                                        onChange={handleMenuFileChange}
                                        className="absolute inset-0 opacity-0 cursor-pointer"
                                    />
                                </div>
                            </div>

                            {/* Selezione Prodotti */}
                            <div>
                                <label className="block text-[10px] font-semibold uppercase tracking-wider text-[#8b92a8] mb-1.5">Prodotti del menu</label>
                                <div className="border border-white/5 rounded-xl bg-[#181c23]/40 max-h-[240px] overflow-y-auto divide-y divide-white/[0.04]">
                                    {products.length === 0 && (
                                        <div className="text-center py-6 text-[12px] text-[#4e5566]">Nessun prodotto disponibile.</div>
                                    )}
                                    {products.map((p) => {
                                        const item = menuItems[p.id] || {};
                                        const selected = !!item.selected;
                                        return (
                                            <div key={p.id} className={`flex items-center gap-3 px-3 py-2.5 transition-colors ${selected ? "bg-[#a78bfa]/[0.06]" : "hover:bg-white/[0.02]"}`}>
                                                <button
                                                    type="button"
                                                    onClick={() => toggleMenuItem(p.id)}
                                                    className={`w-[18px] h-[18px] rounded-md border flex items-center justify-center shrink-0 transition-all ${selected ? "bg-[#a78bfa] border-[#a78bfa]" : "border-white/15 hover:border-[#a78bfa]"}`}
                                                >
                                                    {selected && (
                                                        <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="#111318" strokeWidth="3.5" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12" /></svg>
                                                    )}
                                                </button>
                                                <div className="flex-1 min-w-0">
                                                    <div className="text-[13px] font-medium text-[#f0f4ff] truncate">{p.name}</div>
                                                    <div className="text-[11px] text-[#4e5566]">{p.category} · {fmt(p.price)}</div>
                                                </div>
                                                {selected && (
                                                    <div className="flex items-center gap-3 shrink-0">
                                                        <div className="flex items-center gap-1.5">
                                                            <span className="text-[10px] text-[#4e5566] uppercase tracking-wide">Qtà</span>
                                                            <input
                                                                type="number"
                                                                min="1"
                                                                value={item.quantity || 1}
                                                                onChange={(e) => updateMenuItem(p.id, { quantity: e.target.value })}
                                                                className="w-[52px] bg-[#111318] border border-white/5 rounded-lg px-2 py-1 text-[12px] text-[#f0f4ff] focus:outline-none focus:border-[#a78bfa]"
                                                            />
                                                        </div>
                                                        <label className="flex items-center gap-1.5 cursor-pointer">
                                                            <input
                                                                type="checkbox"
                                                                checked={!!item.obligatory}
                                                                onChange={(e) => updateMenuItem(p.id, { obligatory: e.target.checked })}
                                                                className="accent-[#fbbf24]"
                                                            />
                                                            <span className="text-[10px] text-[#fbbf24] uppercase tracking-wide">Obbl.</span>
                                                        </label>
                                                    </div>
                                                )}
                                            </div>
                                        );
                                    })}
                                </div>
                            </div>

                            {/* Azioni Modale */}
                            <div className="flex justify-end gap-3 pt-4 border-t border-white/5 mt-6">
                                <button
                                    type="button"
                                    onClick={() => setIsMenuModalOpen(false)}
                                    className="bg-transparent border border-white/5 hover:bg-white/5 px-4 py-2 rounded-xl text-[12px] font-medium text-[#8b92a8] transition-all"
                                >
                                    Annulla
                                </button>
                                <button
                                    type="submit"
                                    disabled={savingMenu}
                                    className="bg-[#a78bfa] text-[#111318] px-5 py-2 rounded-xl text-[12px] font-bold tracking-wide hover:bg-[#a78bfa]/90 transition-all disabled:opacity-50"
                                >
                                    {savingMenu ? "CREAZIONE..." : "CREA MENU"}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}