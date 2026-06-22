export default function EndpointRequired({ title, description, endpoint, fields }) {
  return (
    <div className="space-y-4">
      <div className="bg-[#111318] border border-white/5 rounded-[18px] p-8 flex flex-col items-center text-center gap-4">
        <div className="w-14 h-14 rounded-full bg-[#38b6ff]/8 border border-[#38b6ff]/15 flex items-center justify-center">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#38b6ff" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
            <polyline points="16 18 22 12 16 6" />
            <polyline points="8 6 2 12 8 18" />
          </svg>
        </div>
        <div>
          <h3 className="font-['Syne'] text-[17px] font-bold text-[#f0f4ff] mb-2">{title}</h3>
          <p className="text-[13px] text-[#8b92a8] max-w-[480px]">{description}</p>
        </div>
        <div className="bg-[#0b0c10] border border-white/5 rounded-[10px] px-4 py-2.5 font-mono text-[12px] text-[#38b6ff]">
          {endpoint}
        </div>
      </div>

      {fields && fields.length > 0 && (
        <div className="bg-[#111318] border border-white/5 rounded-[18px] p-6">
          <h4 className="font-['Syne'] text-[13px] font-bold text-[#f0f4ff] mb-4">
            Dati che verranno mostrati
          </h4>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
            {fields.map((f) => (
              <div key={f.label} className="flex items-start gap-3 p-3 rounded-[10px] bg-[#181c23] border border-white/[0.04]">
                <div className="w-1.5 h-1.5 rounded-full mt-1.5 flex-shrink-0" style={{ backgroundColor: f.color ?? "#38b6ff" }} />
                <div>
                  <div className="text-[12px] font-semibold text-[#f0f4ff]">{f.label}</div>
                  <div className="text-[11px] text-[#4e5566] mt-0.5">{f.desc}</div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
