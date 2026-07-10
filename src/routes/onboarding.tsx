import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useState } from "react";
import { Music, Download, Headphones } from "lucide-react";

export const Route = createFileRoute("/onboarding")({
  head: () => ({ meta: [{ title: "Welcome — RASICK-EX" }] }),
  component: Onboarding,
});

const slides = [
  { icon: Music, title: "Unlimited Tamil Entertainment", desc: "Stream the latest Tamil songs, albums and playlists anytime." },
  { icon: Download, title: "Download & Listen Offline", desc: "Save your favourite songs and enjoy without internet." },
  { icon: Headphones, title: "Premium Audio Experience", desc: "High quality sound with seamless playback." },
];

function Onboarding() {
  const [i, setI] = useState(0);
  const navigate = useNavigate();
  const Icon = slides[i].icon;
  const isLast = i === slides.length - 1;

  return (
    <main className="relative flex min-h-screen flex-col bg-gradient-hero px-6 pt-10 pb-8">
      <div className="flex justify-end">
        <Link to="/login-selection" className="text-sm text-muted-foreground hover:text-foreground">Skip</Link>
      </div>

      <div className="flex flex-1 flex-col items-center justify-center text-center" key={i}>
        <div className="relative grid h-60 w-60 place-items-center animate-fade-up">
          <div className="absolute inset-0 rounded-full bg-primary/20 blur-3xl" />
          <div className="absolute inset-4 rounded-full border border-primary/30 animate-spin-slow" />
          <div className="absolute inset-10 rounded-full border border-primary/20" />
          <div className="relative grid h-28 w-28 place-items-center rounded-full bg-gradient-primary shadow-glow">
            <Icon className="h-12 w-12 text-primary-foreground" />
          </div>
          <div className="absolute -bottom-2 flex items-end gap-1">
            {[0,1,2,3,4,5,6].map((b) => (
              <span key={b} className="block w-1 rounded-full bg-primary animate-equalizer" style={{ height: `${10 + (b%3)*8}px`, animationDelay: `${b*100}ms` }} />
            ))}
          </div>
        </div>

        <h1 className="mt-12 text-3xl font-bold animate-fade-up">{slides[i].title}</h1>
        <p className="mt-3 max-w-xs text-sm text-muted-foreground animate-fade-up" style={{ animationDelay: "100ms" }}>
          {slides[i].desc}
        </p>
      </div>

      <div className="mb-6 flex justify-center gap-2">
        {slides.map((_, idx) => (
          <span key={idx} className={`h-1.5 rounded-full transition-all ${idx === i ? "w-8 bg-primary" : "w-1.5 bg-muted"}`} />
        ))}
      </div>

      <div className="flex items-center justify-between">
        <button onClick={() => setI((p) => Math.max(0, p - 1))} disabled={i === 0} className="text-sm text-muted-foreground disabled:opacity-30">
          Back
        </button>
        {isLast ? (
          <button onClick={() => navigate({ to: "/login-selection" })} className="rounded-full bg-gradient-primary px-8 py-3 text-sm font-semibold text-primary-foreground shadow-glow transition-transform hover:scale-105">
            Get Started
          </button>
        ) : (
          <button onClick={() => setI((p) => p + 1)} className="rounded-full bg-gradient-primary px-8 py-3 text-sm font-semibold text-primary-foreground shadow-glow transition-transform hover:scale-105">
            Next
          </button>
        )}
      </div>
    </main>
  );
}