import React, { createContext, useContext, useState, useEffect, useCallback } from "react";
import { User, userApi } from "@/lib/api.ts";

interface AuthContextType {
  user: User | null;
  token: string | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  login: (token: string, expiresIn: number) => void;
  logout: () => void;
  refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
};

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(() => localStorage.getItem("jwt_token"));
  const [isLoading, setIsLoading] = useState(!!localStorage.getItem("jwt_token"));

  const logout = useCallback(() => {
    localStorage.removeItem("jwt_token");
    localStorage.removeItem("jwt_expires");
    setToken(null);
    setUser(null);
  }, []);

  const refreshUser = useCallback(async () => {
    try {
      const u = await userApi.me();
      setUser(u);
    } catch {
      logout();
    }
  }, [logout]);

  const login = useCallback((newToken: string, expiresIn: number) => {
    localStorage.setItem("jwt_token", newToken);
    localStorage.setItem("jwt_expires", String(Date.now() + expiresIn));
    setToken(newToken);
  }, []);

  useEffect(() => {
    if (token) {
      const expires = Number(localStorage.getItem("jwt_expires") || 0);
      if (expires && Date.now() > expires) {
        logout();
        setIsLoading(false);
        return;
      }
      refreshUser().finally(() => setIsLoading(false));
    } else {
      setIsLoading(false);
    }
  }, [token, refreshUser, logout]);

  return (
    <AuthContext.Provider value={{ user, token, isLoading, isAuthenticated: !!user, login, logout, refreshUser }}>
      {children}
    </AuthContext.Provider>
  );
};
