import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useEffect } from "react";
import { LOGO_URL } from "@/lib/mock-data";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "PERAMBALUR-AUDIOS" },
      { name: "description", content: "Premium Tamil music streaming and offline downloads." },
      { property: "og:title", content: "PERAMBALUR-AUDIOS" },
      { property: "og:description", content: "Premium Tamil music streaming and offline downloads." },
    ],
  }),
  component: SplashScreen,
});

function SplashScreen() {
  const navigate = useNavigate();

  useEffect(() => {
    const t = setTimeout(() => navigate({ to: "/onboarding" }), 3800);
    return () => clearTimeout(t);
  }, [navigate]);

  return (
    <main className="relative grid min-h-screen place-items-center overflow-hidden bg-background">
      <div className="pointer-events-none absolute inset-0" style={{ background: "var(--gradient-glow)" }} />

      <div className="pointer-events-none absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2">
        {[0, 1, 2].map((i) => (
          <span
            key={i}
            className="absolute left-1/2 top-1/2 block h-64 w-64 -translate-x-1/2 -translate-y-1/2 rounded-full border border-primary/40 animate-speaker-pulse"
            style={{ animationDelay: `${i * 0.6}s` }}
          />
        ))}
      </div>

      <div className="relative z-10 flex flex-col items-center px-6">
        <div className="relative animate-logo-fade-in">
          <div className="absolute inset-0 rounded-3xl animate-logo-glow" />
          <div className="relative overflow-hidden rounded-3xl">
            <img
              src={LOGO_URL}
              alt="PERAMBALUR-AUDIOS"
              className="relative h-56 w-56 rounded-3xl object-contain"
            />
            <div className="pointer-events-none absolute inset-0 overflow-hidden">
              <div className="absolute inset-y-0 -left-1/2 w-1/2 bg-gradient-to-r from-transparent via-white/30 to-transparent animate-shine" />
            </div>
          </div>
        </div>

        <p className="mt-4 text-2xl font-bold text-gradient-primary animate-fade-up" style={{ animationDelay: "0.6s" }}>
          PERAMBALUR-AUDIOS
        </p>

        <div className="mt-6 flex h-10 items-end gap-1.5">
          {Array.from({ length: 18 }).map((_, i) => (
            <span
              key={i}
              className="block w-1 rounded-full bg-gradient-primary animate-equalizer"
              style={{ height: `${20 + (i % 5) * 10}px`, animationDelay: `${i * 70}ms` }}
            />
          ))}
        </div>

        <p className="mt-8 text-xs uppercase tracking-[0.35em] text-muted-foreground animate-fade-up" style={{ animationDelay: "1.2s" }}>
          Powered by
        </p>
        <p className="mt-1 text-sm font-semibold text-gradient-primary animate-fade-up" style={{ animationDelay: "1.4s" }}>
          Rasick-EX
        </p>

        <Link to="/onboarding" className="mt-10 text-[11px] text-muted-foreground hover:text-foreground">Skip intro →</Link>
      </div>
    </main>
  );
}
