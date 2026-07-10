import { createContext, useContext, useState, useEffect, type ReactNode } from "react";
import { getStoredUser, loginApi, setStoredUser, type AuthUser } from "@/lib/api";

export type UserRole = "admin" | "user" | null;

interface AuthContextValue {
  user: AuthUser | null;
  login: (username: string, password: string, role: "admin" | "user") => Promise<{ success: boolean; error?: string }>;
  logout: () => void;
  isAdmin: boolean;
  isLoggedIn: boolean;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);

  useEffect(() => {
    setUser(getStoredUser());
  }, []);

  const login = async (
    username: string,
    password: string,
    role: "admin" | "user"
  ): Promise<{ success: boolean; error?: string }> => {
    const result = await loginApi(username, password, role);

    if ("error" in result) {
      return { success: false, error: result.error };
    }

    setUser(result.user);
    setStoredUser(result.user);
    return { success: true };
  };

  const logout = () => {
    setUser(null);
    setStoredUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        login,
        logout,
        isAdmin: user?.role === "admin",
        isLoggedIn: user !== null,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
