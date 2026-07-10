import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { Shield, User } from "lucide-react";
import { LOGO_URL } from "@/lib/mock-data";

export const Route = createFileRoute("/login-selection")({
  head: () => ({ meta: [{ title: "Login — RASICK-EX" }] }),
  component: LoginSelection,
});

function LoginSelection() {
  const navigate = useNavigate();

  return (
    <main className="relative flex min-h-screen flex-col items-center justify-center bg-background px-6 pb-10">
      {/* Background glow */}
      <div
        className="pointer-events-none absolute inset-0"
        style={{ background: "var(--gradient-glow)", opacity: 0.5 }}
      />

      <div className="relative z-10 flex w-full max-w-sm flex-col items-center">
        {/* Logo */}
        <div className="relative animate-logo-fade-in">
          <div className="absolute inset-0 rounded-3xl animate-logo-glow" />
          <div className="relative overflow-hidden rounded-3xl">
            <img
              src={LOGO_URL}
              alt="RASICK-EX"
              className="h-24 w-24 rounded-3xl object-contain"
            />
            <div className="pointer-events-none absolute inset-0 overflow-hidden">
              <div className="absolute inset-y-0 -left-1/2 w-1/2 bg-gradient-to-r from-transparent via-white/30 to-transparent animate-shine" />
            </div>
          </div>
        </div>

        <p className="mt-4 text-xs uppercase tracking-[0.35em] text-muted-foreground animate-fade-up">
          RASICK-EX
        </p>

        <h1 className="mt-6 text-2xl font-bold text-gradient-primary animate-fade-up" style={{ animationDelay: "100ms" }}>
          Welcome Back
        </h1>
        <p className="mt-2 text-sm text-muted-foreground animate-fade-up" style={{ animationDelay: "180ms" }}>
          Choose how you want to sign in
        </p>

        {/* Equalizer decoration */}
        <div className="mt-6 flex h-6 items-end gap-1 animate-fade-up" style={{ animationDelay: "240ms" }}>
          {Array.from({ length: 12 }).map((_, i) => (
            <span
              key={i}
              className="block w-1 rounded-full bg-gradient-primary animate-equalizer"
              style={{ height: `${10 + (i % 4) * 6}px`, animationDelay: `${i * 80}ms` }}
            />
          ))}
        </div>

        {/* Login type buttons */}
        <div className="mt-10 flex w-full flex-col gap-4 animate-fade-up" style={{ animationDelay: "350ms" }}>
          <button
            onClick={() => navigate({ to: "/login/admin" })}
            className="glass group flex items-center gap-4 rounded-2xl p-5 text-left transition-all hover:bg-secondary/60 hover:shadow-glow"
          >
            <div className="grid h-12 w-12 place-items-center rounded-xl bg-gradient-primary shadow-glow transition-transform group-hover:scale-110">
              <Shield className="h-6 w-6 text-primary-foreground" />
            </div>
            <div className="flex-1">
              <p className="font-semibold text-foreground">Admin Login</p>
              <p className="mt-0.5 text-xs text-muted-foreground">Upload, manage songs &amp; categories</p>
            </div>
            <div className="text-muted-foreground group-hover:text-primary transition-colors">→</div>
          </button>

          <button
            onClick={() => navigate({ to: "/login/user" })}
            className="glass group flex items-center gap-4 rounded-2xl p-5 text-left transition-all hover:bg-secondary/60"
          >
            <div className="grid h-12 w-12 place-items-center rounded-xl bg-secondary transition-transform group-hover:scale-110">
              <User className="h-6 w-6 text-primary" />
            </div>
            <div className="flex-1">
              <p className="font-semibold text-foreground">User Login</p>
              <p className="mt-0.5 text-xs text-muted-foreground">Stream &amp; download Tamil music</p>
            </div>
            <div className="text-muted-foreground group-hover:text-primary transition-colors">→</div>
          </button>
        </div>
      </div>
    </main>
  );
}
