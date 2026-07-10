import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { AppShell } from "@/components/app-shell";
import { Crown, Bell, Lock, Moon, LogOut, ChevronRight, Settings, Heart, LogIn } from "lucide-react";
import { LOGO_URL } from "@/lib/mock-data";
import { useAuth } from "@/contexts/auth-context";

export const Route = createFileRoute("/profile")({
  head: () => ({ meta: [{ title: "Profile — RASICK-EX" }] }),
  component: Profile,
});

const items = [
  { icon: Crown, label: "Subscription", value: "Free plan" },
  { icon: Heart, label: "Liked songs", value: "24" },
  { icon: Bell, label: "Notifications", value: "On" },
  { icon: Lock, label: "Privacy", value: "" },
  { icon: Moon, label: "Dark mode", value: "Always" },
  { icon: Settings, label: "Settings", value: "" },
];

function Profile() {
  const { user, logout, isLoggedIn } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate({ to: "/login-selection" });
  };

  return (
    <AppShell>
      <header className="relative px-5 pt-10 text-center">
        <div className="absolute inset-x-0 top-0 h-60" style={{ background: "var(--gradient-glow)", opacity: 0.5 }} />
        <div className="relative mx-auto h-24 w-24 overflow-hidden rounded-full ring-2 ring-primary/50 shadow-glow">
          <img src={LOGO_URL} alt="" className="h-full w-full object-cover" />
        </div>
        <h1 className="relative mt-3 text-xl font-bold">{user?.username ?? "Guest"}</h1>
        <p className="relative text-xs text-muted-foreground">
          {isLoggedIn ? `${user?.role === "admin" ? "Administrator" : "Music Lover"} · RASICK-EX` : "Not signed in"}
        </p>
        {isLoggedIn ? (
          <button className="relative mt-4 rounded-full bg-gradient-primary px-5 py-2 text-xs font-semibold text-primary-foreground shadow-glow">
            Edit profile
          </button>
        ) : (
          <button
            onClick={() => navigate({ to: "/login-selection" })}
            className="relative mt-4 inline-flex items-center gap-1.5 rounded-full bg-gradient-primary px-5 py-2 text-xs font-semibold text-primary-foreground shadow-glow"
          >
            <LogIn className="h-3.5 w-3.5" /> Sign In
          </button>
        )}
      </header>

      <section className="mt-8 px-3">
        <ul className="glass divide-y divide-border/60 overflow-hidden rounded-2xl">
          {items.map(({ icon: Icon, label, value }) => (
            <li key={label}>
              <button className="flex w-full items-center gap-4 px-4 py-3 text-left hover:bg-secondary/40">
                <div className="grid h-9 w-9 place-items-center rounded-lg bg-primary/10 text-primary">
                  <Icon className="h-4 w-4" />
                </div>
                <span className="flex-1 text-sm">{label}</span>
                <span className="text-xs text-muted-foreground">{value}</span>
                <ChevronRight className="h-4 w-4 text-muted-foreground" />
              </button>
            </li>
          ))}
        </ul>

        {isLoggedIn && (
          <button
            onClick={handleLogout}
            className="mt-5 flex w-full items-center justify-center gap-2 rounded-2xl border border-destructive/40 py-3 text-sm font-semibold text-destructive hover:bg-destructive/10"
          >
            <LogOut className="h-4 w-4" /> Log out
          </button>
        )}
      </section>
    </AppShell>
  );
}