import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { authApi, LoginUserDto } from "@/lib/api.ts";
import { useAuth } from "@/contexts/AuthContext.tsx";
import { Button } from "@/components/ui/button.tsx";
import { Input } from "@/components/ui/input.tsx";
import { Label } from "@/components/ui/label.tsx";
import PageTransition from "@/components/PageTransition.tsx";
import { BookOpen } from "lucide-react";
import { useToast } from "@/hooks/use-toast.ts";

const LoginPage = () => {
  const [form, setForm] = useState<LoginUserDto>({ email: "", password: "" });
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();
  const { toast } = useToast();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await authApi.login(form);
      login(res.token, res.expiresIn);
      navigate("/collection");
    } catch (err: any) {
      toast({ variant: "destructive", title: "Login failed", description: err.message });
    } finally {
      setLoading(false);
    }
  };

  return (
    <PageTransition>
      <div className="flex min-h-[80vh] items-center justify-center">
        <div className="w-full max-w-sm mx-auto px-4">
          <div className="text-center mb-8">
            <BookOpen className="h-10 w-10 text-primary mx-auto mb-3" />
            <h1 className="font-display text-2xl font-bold text-foreground">Welcome back</h1>
            <p className="text-sm text-muted-foreground mt-1">Sign in to your account</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <Label htmlFor="email">Email</Label>
              <Input id="email" type="email" required value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
            </div>
            <div>
              <Label htmlFor="password">Password</Label>
              <Input id="password" type="password" required value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
            </div>
            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? "Signing in…" : "Sign in"}
            </Button>
          </form>

          <p className="text-center text-sm text-muted-foreground mt-6">
            Don't have an account?{" "}
            <Link to="/signup" className="text-primary hover:underline">Sign up</Link>
          </p>
        </div>
      </div>
    </PageTransition>
  );
};

export default LoginPage;
