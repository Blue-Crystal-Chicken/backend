import { useFinanze } from "../hooks/useFinanze";
import FiltriFinanze      from "../components/finanze/FiltriFinanze";
import KpiFinanze         from "../components/finanze/KpiFinanze";
import TrendRevenueChart  from "../components/finanze/TrendRevenueChart";
import CanaleChart        from "../components/finanze/CanaleChart";
import ConfrontoKpi       from "../components/finanze/ConfrontoKpi";

export default function Finanze() {
  const {
    primaryId,    handlePrimaryChange,
    comparedIds,  toggleComparison,
    range,        setRange,
    isLive,       toggleLive,
    locations,    loading, refreshing, error,
    activeIds,
    kpi,          trendData, canaleData, confrontoData,
    locationName,
    refresh,
  } = useFinanze();

  const hasConfronto = confrontoData.length > 0;

  return (
    <div className="p-[28px_32px] max-w-[1440px] mx-auto text-[#f0f4ff] bg-[#0b0c10] min-h-screen">

      {/* Header */}
      <div className="flex items-end justify-between mb-6">
        <div>
          <h1 className="font-['Syne'] text-[26px] font-extrabold tracking-tight mb-1">
            Analisi Finanziaria
          </h1>
          <p className="text-[13px] text-[#8b92a8] uppercase tracking-wide">
            Fatturato, trend e confronto tra sedi
          </p>
        </div>
      </div>

      {/* Filtri */}
      <FiltriFinanze
        locations={locations}
        primaryId={primaryId}
        onPrimaryChange={handlePrimaryChange}
        comparedIds={comparedIds}
        onToggleComparison={toggleComparison}
        range={range}
        onRangeChange={setRange}
        refreshing={refreshing}
        onRefresh={refresh}
        isLive={isLive}
        onToggleLive={toggleLive}
      />

      {/* Errore */}
      {error && (
        <div className="mb-6 px-4 py-3 rounded-xl bg-[#ff5e5e]/10 border border-[#ff5e5e]/20 text-[13px] text-[#ff5e5e]">
          {error}
        </div>
      )}

      {/* KPI */}
      <KpiFinanze kpi={kpi} loading={loading} />

      {/* Trend revenue — full width */}
      <TrendRevenueChart
        trendData={trendData}
        activeIds={activeIds}
        locationName={locationName}
        loading={loading}
      />

      {/* Riga inferiore: canale + confronto (se attivo) */}
      <div className={hasConfronto ? "grid grid-cols-1 lg:grid-cols-2 gap-4" : ""}>
        <CanaleChart
          canaleData={canaleData}
          primaryId={primaryId}
          locationName={locationName}
          loading={loading}
        />
        {hasConfronto && (
          <ConfrontoKpi confrontoData={confrontoData} />
        )}
      </div>

    </div>
  );
}
