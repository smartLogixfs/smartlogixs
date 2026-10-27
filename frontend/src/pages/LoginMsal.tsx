import { useMsal } from '@azure/msal-react';
import { InteractionStatus } from '@azure/msal-browser';
import { Database, ShieldCheck, LineChart } from 'lucide-react';
import { loginRequest } from '../authConfig';

/**
 * Pantalla de acceso con Microsoft Entra ID (MSAL).
 * Reemplaza el login casero por el flujo de redirect del IDaaS.
 */
export default function LoginMsal() {
  const { instance, inProgress } = useMsal();
  const busy = inProgress !== InteractionStatus.None;

  const handleLogin = () => {
    if (!busy) {
      instance.loginRedirect(loginRequest).catch((e) => console.error(e));
    }
  };

  return (
    <main className="flex w-full min-h-screen bg-slate-50">
      {/* Panel visual (desktop) */}
      <section className="hidden lg:flex w-1/2 bg-[#131b2e] relative overflow-hidden flex-col justify-center items-center p-12">
        <div className="absolute inset-0 opacity-15 pointer-events-none">
          <div className="absolute -top-[20%] -left-[20%] w-[80%] h-[80%] bg-[#006a61] rounded-full blur-[120px]"></div>
          <div className="absolute -bottom-[20%] -right-[20%] w-[80%] h-[80%] bg-[#bec6e0] rounded-full blur-[120px]"></div>
        </div>
        <div className="relative z-10 max-w-lg text-center">
          <h1 className="text-3xl font-bold text-white tracking-tight leading-snug mb-4">
            Gestión logística de última generación.
          </h1>
          <p className="text-slate-300 text-base leading-relaxed max-w-md mx-auto">
            Optimiza tu cadena de suministro con precisión quirúrgica y datos en tiempo real.
          </p>
        </div>
        <div className="absolute bottom-6 left-8 right-8 flex justify-between px-4 opacity-50 text-[#7c839b]">
          <div className="flex items-center gap-2">
            <ShieldCheck size={16} />
            <span className="text-xs font-semibold tracking-wider uppercase">Autenticación Microsoft Entra ID</span>
          </div>
          <div className="flex items-center gap-2">
            <LineChart size={16} />
            <span className="text-xs font-semibold tracking-wider uppercase">Análisis en tiempo real</span>
          </div>
        </div>
      </section>

      {/* Panel de acceso */}
      <section className="w-full lg:w-1/2 flex items-center justify-center p-8 md:p-16 bg-white">
        <div className="w-full max-w-[420px] flex flex-col">
          <div className="mb-8 flex items-center gap-2.5">
            <div className="w-10 h-10 bg-black rounded flex items-center justify-center shadow-md">
              <Database className="text-white" size={20} />
            </div>
            <span className="text-xl font-bold tracking-tight text-slate-900">SmartLogix</span>
          </div>

          <h2 className="text-2xl font-bold text-slate-900 mb-1.5">Bienvenido a SmartLogix</h2>
          <p className="text-slate-500 text-sm mb-8">
            Inicia sesión con tu cuenta corporativa de Microsoft para acceder al panel de control.
          </p>

          <button
            onClick={handleLogin}
            disabled={busy}
            className="w-full flex items-center justify-center gap-3 border border-slate-300 hover:bg-slate-50 py-3 px-6 rounded-lg text-sm font-semibold text-slate-800 transition-all active:scale-[0.99] disabled:opacity-60 disabled:cursor-not-allowed"
          >
            {/* Logo Microsoft */}
            <svg width="18" height="18" viewBox="0 0 21 21" aria-hidden="true">
              <rect x="1" y="1" width="9" height="9" fill="#f25022" />
              <rect x="11" y="1" width="9" height="9" fill="#7fba00" />
              <rect x="1" y="11" width="9" height="9" fill="#00a4ef" />
              <rect x="11" y="11" width="9" height="9" fill="#ffb900" />
            </svg>
            <span>{busy ? 'Redirigiendo…' : 'Iniciar sesión con Microsoft'}</span>
          </button>

          <p className="text-[11px] text-slate-400 mt-6 text-center">
            Serás redirigido a Microsoft Entra ID para autenticarte de forma segura.
          </p>
        </div>
      </section>
    </main>
  );
}
