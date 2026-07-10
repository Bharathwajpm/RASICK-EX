import { Link, useLocation } from "@tanstack/react-router";
import { Home, Search, Library, User, Download, LayoutDashboard } from "lucide-react";
import { cn } from "@/lib/utils";
import { useAuth } from "@/contexts/auth-context";

export function BottomNav() {
  const { pathname } = useLocation();
  const { isAdmin, isLoggedIn } = useAuth();

  const items = [
    { to: "/home", label: "Home", icon: Home, alwaysShow: true },
    { to: "/search", label: "Search", icon: Search, alwaysShow: true },
    { to: "/downloads", label: "Downloads", icon: Download, alwaysShow: true },
    { to: "/library", label: "Library", icon: Library, alwaysShow: true },
    { to: "/admin/dashboard", label: "Admin", icon: LayoutDashboard, adminOnly: true },
    { to: "/profile", label: "Profile", icon: User, alwaysShow: true },
  ] as const;

  const visibleItems = items.filter((item) => {
    if ("adminOnly" in item && item.adminOnly) return isAdmin;
    return true;
  });

  return (
    <nav className="glass fixed bottom-0 left-1/2 z-40 w-full max-w-[460px] -translate-x-1/2 border-t border-border/40 pb-[env(safe-area-inset-bottom)]">
      <ul className="flex items-center justify-around px-2 py-2">
        {visibleItems.map(({ to, label, icon: Icon }) => {
          const active = pathname === to || (to === "/home" && pathname === "/");
          return (
            <li key={to} className="flex-1">
              <Link
                to={to}
                className={cn(
                  "flex flex-col items-center gap-1 rounded-xl px-3 py-2 text-xs font-medium transition-all",
                  active ? "text-primary" : "text-muted-foreground hover:text-foreground"
                )}
              >
                <Icon className={cn("h-5 w-5 transition-transform", active && "scale-110")} />
                <span>{label}</span>
              </Link>
            </li>
          );
        })}
      </ul>
    </nav>
  );
}
