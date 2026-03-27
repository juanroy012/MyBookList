import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Route, Routes } from "react-router-dom";
import { Toaster as Sonner } from "@/components/ui/sonner.tsx";
import { Toaster } from "@/components/ui/toaster.tsx";
import { TooltipProvider } from "@/components/ui/tooltip.tsx";
import { AuthProvider } from "@/contexts/AuthContext.tsx";
import Navbar from "@/components/Navbar.tsx";
import HomePage from "@/pages/HomePage.tsx";
import LoginPage from "@/pages/LoginPage.tsx";
import SignupPage from "@/pages/SignupPage.tsx";
import VerifyPage from "@/pages/VerifyPage.tsx";
import SearchPage from "@/pages/SearchPage.tsx";
import BookDetailPage from "@/pages/BookDetailPage.tsx";
import CollectionPage from "@/pages/CollectionPage.tsx";
import ProtectedRoute from "@/components/ProtectedRoute.tsx";
import NotFound from "@/pages/NotFound.tsx";

const queryClient = new QueryClient();

const App = () => (
  <QueryClientProvider client={queryClient}>
    <TooltipProvider>
      <Toaster />
      <Sonner />
      <BrowserRouter>
        <AuthProvider>
          <Navbar />
          <main className="min-h-[calc(100vh-4rem)]">
            <Routes>
              <Route path="/" element={<HomePage />} />
              <Route path="/login" element={<LoginPage />} />
              <Route path="/signup" element={<SignupPage />} />
              <Route path="/verify" element={<VerifyPage />} />
              <Route path="/search" element={<SearchPage />} />
              <Route path="/book/:workId" element={<BookDetailPage />} />
              <Route path="/collection" element={<ProtectedRoute><CollectionPage /></ProtectedRoute>} />
              <Route path="*" element={<NotFound />} />
            </Routes>
          </main>
        </AuthProvider>
      </BrowserRouter>
    </TooltipProvider>
  </QueryClientProvider>
);

export default App;
