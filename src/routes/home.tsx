import { createFileRoute, Link } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { Bell, Search } from "lucide-react";
import { AppShell } from "@/components/app-shell";
import { LOGO_URL } from "@/lib/mock-data";
import { fetchCategories, type Category } from "@/lib/api";

export const Route = createFileRoute("/home")({
  head: () => ({
    meta: [
      { title: "Home — PERAMBALUR-AUDIOS" },
      { name: "description", content: "Browse Tamil music by category." },
    ],
  }),
  component: Home,
});

function Home() {
  const [categories, setCategories] = useState<Category[]>([]);

  useEffect(() => {
    fetchCategories().then(setCategories).catch(() => setCategories([]));
  }, []);

  return (
    <AppShell>
      <header className="relative px-5 pt-10 pb-4">
        <div className="absolute inset-x-0 top-0 h-64" style={{ background: "var(--gradient-glow)", opacity: 0.6 }} />
        <div className="relative flex items-center justify-between">
          <div className="flex items-center gap-3">
            <img src={LOGO_URL} alt="" className="h-10 w-10 rounded-xl object-cover ring-1 ring-primary/40" />
            <div>
              <p className="text-xs text-muted-foreground">Vanakkam 👋</p>
              <p className="text-sm font-semibold">Music lover</p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <button className="glass grid h-10 w-10 place-items-center rounded-full"><Bell className="h-4 w-4" /></button>
            <Link to="/search" className="glass grid h-10 w-10 place-items-center rounded-full">
              <Search className="h-4 w-4" />
            </Link>
          </div>
        </div>
      </header>

      <section className="mt-4 px-5">
        <h2 className="text-lg font-bold">Browse Categories</h2>
        <p className="mt-1 text-xs text-muted-foreground">Select a category to explore songs</p>

        {categories.length === 0 ? (
          <p className="mt-8 text-center text-sm text-muted-foreground">No categories available yet</p>
        ) : (
          <div className="mt-5 grid grid-cols-2 gap-3">
            {categories.map((cat) => (
              <Link
                key={cat.id}
                to="/category/$name"
                params={{ name: cat.name }}
                className={`relative h-28 overflow-hidden rounded-2xl bg-gradient-to-br ${cat.color} p-4 shadow-elevated transition-transform hover:scale-[1.02]`}
              >
                <p className="text-sm font-bold text-white">{cat.name}</p>
                <p className="mt-1 text-[11px] text-white/80">{cat.count} songs</p>
                <div className="absolute -bottom-4 -right-4 h-20 w-20 rounded-full bg-white/15 blur-2xl" />
              </Link>
            ))}
          </div>
        )}
      </section>
    </AppShell>
  );
}
