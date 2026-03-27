import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext.tsx";
import { BookOpen, Search, Library, LogOut, User, Menu, X } from "lucide-react";
import { Button } from "@/components/ui/button.tsx";
import ThemeToggle from "@/components/ThemeToggle.tsx";
import { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";

const Navbar = () => {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();
  const [mobileOpen, setMobileOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  const navLinks = isAuthenticated
    ? [
        { to: "/search", label: "Discover", icon: Search },
        { to: "/collection", label: "My Library", icon: Library },
      ]
    : [
        { to: "/search", label: "Discover", icon: Search },
      ];

  return (
    <nav className="sticky top-0 z-50 border-b bg-background/80 backdrop-blur-md">
      <div className="container flex h-16 items-center justify-between">
        <Link to="/" className="flex items-center gap-2 font-display text-xl font-semibold text-foreground">
          <BookOpen className="h-6 w-6 text-primary" />
          MyBookList
        </Link>

        {/* Desktop nav */}
        <div className="hidden md:flex items-center gap-6">
          {navLinks.map(({ to, label }) => (
            <Link key={to} to={to} className="text-sm font-medium text-muted-foreground hover:text-foreground transition-colors">
              {label}
            </Link>
          ))}
          {isAuthenticated ? (
            <div className="flex items-center gap-3">
              <span className="text-sm text-muted-foreground flex items-center gap-1.5">
                <User className="h-4 w-4" />
                {user?.username}
              </span>
              <Button variant="ghost" size="sm" onClick={handleLogout}>
                <LogOut className="h-4 w-4 mr-1" /> Logout
              </Button>
            </div>
          ) : (
            <div className="flex items-center gap-2">
              <Button variant="ghost" size="sm" asChild>
                <Link to="/login">Sign in</Link>
              </Button>
              <Button size="sm" asChild>
                <Link to="/signup">Sign up</Link>
              </Button>
            </div>
          )}
          <ThemeToggle />
        </div>

        <div className="md:hidden flex items-center gap-1">
          <ThemeToggle />
          <button className="p-2" onClick={() => setMobileOpen(!mobileOpen)}>
            {mobileOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
          </button>
        </div>
      </div>

      {/* Mobile menu */}
      <AnimatePresence>
        {mobileOpen && (
          <motion.div
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: "auto", opacity: 1 }}
            exit={{ height: 0, opacity: 0 }}
            transition={{ duration: 0.2 }}
            className="md:hidden overflow-hidden border-b bg-background"
          >
            <div className="container py-4 flex flex-col gap-3">
              {navLinks.map(({ to, label, icon: Icon }) => (
                <Link key={to} to={to} onClick={() => setMobileOpen(false)} className="flex items-center gap-2 text-sm font-medium text-muted-foreground hover:text-foreground">
                  <Icon className="h-4 w-4" /> {label}
                </Link>
              ))}
              {isAuthenticated ? (
                <Button variant="ghost" size="sm" className="justify-start" onClick={() => { handleLogout(); setMobileOpen(false); }}>
                  <LogOut className="h-4 w-4 mr-2" /> Logout
                </Button>
              ) : (
                <>
                  <Link to="/login" onClick={() => setMobileOpen(false)} className="text-sm font-medium text-muted-foreground">Sign in</Link>
                  <Link to="/signup" onClick={() => setMobileOpen(false)} className="text-sm font-medium text-primary">Sign up</Link>
                </>
              )}
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </nav>
  );
};

export default Navbar;
