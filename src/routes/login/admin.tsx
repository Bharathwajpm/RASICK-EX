import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { useState } from "react";
import { Shield, Eye, EyeOff, ArrowLeft } from "lucide-react";
import { useAuth } from "@/contexts/auth-context";
import { LOGO_URL } from "@/lib/mock-data";

export const Route = createFileRoute("/login/admin")({
  head: () => ({ meta: [{ title: "Admin Login — RASICK-EX" }] }),
  component: AdminLogin,
});

function AdminLogin() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    setError("");
    if (!username.trim() || !password.trim()) {
      setError("Please enter both username and password.");
      return;
    }
    setLoading(true);
    const result = await login(username.trim(), password, "admin");
    setLoading(false);
    if (result.success) {
      navigate({ to: "/admin/dashboard" });
    } else {
      setError(result.error ?? "Invalid Username or Password");
    }
  };

  return (
    <main className="relative flex min-h-screen flex-col bg-background px-6 pb-10">
      <div
        className="pointer-events-none absolute inset-0"
        style={{ background: "var(--gradient-glow)", opacity: 0.4 }}
      />

      {/* Back button */}
      <button
        onClick={() => navigate({ to: "/login-selection" })}
        className="relative z-10 mt-10 flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground transition-colors"
      >
        <ArrowLeft className="h-4 w-4" /> Back
      </button>

      <div className="relative z-10 mt-8 flex flex-col items-center">
        {/* Badge */}
        <div className="flex items-center gap-2 rounded-full border border-primary/30 bg-primary/10 px-4 py-1.5">
          <Shield className="h-3.5 w-3.5 text-primary" />
          <span className="text-xs font-medium text-primary">Admin Portal</span>
        </div>

        <img
          src={LOGO_URL}
          alt="RASICK-EX"
          className="mt-6 h-16 w-16 rounded-2xl object-contain shadow-glow"
        />
        <h1 className="mt-4 text-2xl font-bold text-foreground">Admin Login</h1>
        <p className="mt-1 text-xs text-muted-foreground">Sign in to manage your music platform</p>
      </div>

      <div className="relative z-10 mt-10 flex flex-col gap-4">
        {/* Error message */}
        {error && (
          <div className="rounded-xl border border-destructive/40 bg-destructive/10 px-4 py-3 text-sm text-destructive">
            {error}
          </div>
        )}

        {/* Username */}
        <div className="flex flex-col gap-1.5">
          <label className="text-xs font-medium text-muted-foreground uppercase tracking-wider">Username</label>
          <div className="glass flex items-center rounded-xl border border-input px-4 py-3 focus-within:border-primary/60 transition-colors">
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              onKeyDown={(e) => e.key === "Enter" && handleSubmit()}
              placeholder="Enter admin username"
              className="w-full bg-transparent text-sm text-foreground placeholder:text-muted-foreground focus:outline-none"
            />
          </div>
        </div>

        {/* Password */}
        <div className="flex flex-col gap-1.5">
          <label className="text-xs font-medium text-muted-foreground uppercase tracking-wider">Password</label>
          <div className="glass flex items-center rounded-xl border border-input px-4 py-3 focus-within:border-primary/60 transition-colors">
            <input
              type={showPassword ? "text" : "password"}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              onKeyDown={(e) => e.key === "Enter" && handleSubmit()}
              placeholder="Enter admin password"
              className="w-full bg-transparent text-sm text-foreground placeholder:text-muted-foreground focus:outline-none"
            />
            <button
              onClick={() => setShowPassword((v) => !v)}
              className="ml-2 text-muted-foreground hover:text-foreground transition-colors"
            >
              {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
            </button>
          </div>
        </div>

        {/* Login button */}
        <button
          onClick={handleSubmit}
          disabled={loading}
          className="mt-2 rounded-full bg-gradient-primary py-3.5 text-sm font-semibold text-primary-foreground shadow-glow transition-transform hover:scale-[1.02] active:scale-[0.98] disabled:opacity-60"
        >
          {loading ? "Signing in…" : "Login"}
        </button>

        <p className="mt-4 text-center text-xs text-muted-foreground">
          Not an admin?{" "}
          <button
            onClick={() => navigate({ to: "/login/user" })}
            className="text-primary hover:underline"
          >
            User login
          </button>
        </p>
      </div>
    </main>
  );
}
