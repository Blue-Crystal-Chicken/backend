import { useOrdini } from "../hooks/useOrdini";
import FiltriOrdini        from "../components/ordini/FiltriOrdini";
import KpiOrdini           from "../components/ordini/KpiOrdini";
import TrendOrdini         from "../components/ordini/TrendOrdini";
import TabellaOrdini       from "../components/ordini/TabellaOrdini";
import DettaglioOrdineModal from "../components/ordini/DettaglioOrdineModal";

export default function Ordini() {
  const {
    orders,
    locations,
    loading,
    refreshing,
    error,
    locationId,
    setLocationId,
    range,
    setRange,
    kpi,
    dailyData,
    serviceTypeData,
    locationName,
    selectedOrder,
    setSelectedOrder,
    refresh,
    updateStatus,
    deleteOrder,
  } = useOrdini();

  return (
    <div className="p-[28px_32px] max-w-[1440px] mx-auto text-[#f0f4ff] bg-[#0b0c10] min-h-screen">

      {/* Header */}
      <div className="flex items-end justify-between mb-6">
        <div>
          <h1 className="font-['Syne'] text-[26px] font-extrabold tracking-tight mb-1">
            Gestione Ordini
          </h1>
          <p className="text-[13px] text-[#8b92a8] uppercase tracking-wide">
            Monitoraggio e gestione degli ordini per sede
          </p>
        </div>
      </div>

      {/* Filtri */}
      <FiltriOrdini
        locations={locations}
        locationId={locationId}
        onLocationChange={setLocationId}
        range={range}
        onRangeChange={setRange}
        refreshing={refreshing}
        onRefresh={refresh}
      />

      {/* Errore */}
      {error && (
        <div className="mb-6 px-4 py-3 rounded-xl bg-[#ff5e5e]/10 border border-[#ff5e5e]/20 text-[13px] text-[#ff5e5e]">
          {error}
        </div>
      )}

      {/* KPI */}
      <KpiOrdini kpi={kpi} loading={loading} />

      {/* Trend */}
      <TrendOrdini dailyData={dailyData} serviceTypeData={serviceTypeData} loading={loading} />

      {/* Tabella */}
      <TabellaOrdini
        orders={orders}
        showLocation={locationId === null}
        locationName={locationName}
        onDetail={setSelectedOrder}
        onDelete={deleteOrder}
        loading={loading}
      />

      {/* Modal dettaglio */}
      {selectedOrder && (
        <DettaglioOrdineModal
          order={selectedOrder}
          locationName={locationName}
          onClose={() => setSelectedOrder(null)}
          onUpdateStatus={updateStatus}
        />
      )}

    </div>
  );
}
