import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { authApi, RegisterUserDto } from "@/lib/api.ts";
import { Button } from "@/components/ui/button.tsx";
import { Input } from "@/components/ui/input.tsx";
import { Label } from "@/components/ui/label.tsx";
import PageTransition from "@/components/PageTransition.tsx";
import { BookOpen } from "lucide-react";
import { useToast } from "@/hooks/use-toast.ts";

const SignupPage = () => {
  const [form, setForm] = useState<RegisterUserDto>({ email: "", password: "", username: "" });
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { toast } = useToast();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await authApi.signup(form);
      toast({ title: "Account created!", description: "Check your email for a verification code." });
      navigate("/verify", { state: { email: form.email } });
    } catch (err: any) {
      toast({ variant: "destructive", title: "Signup failed", description: err.message });
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
            <h1 className="font-display text-2xl font-bold text-foreground">Create account</h1>
            <p className="text-sm text-muted-foreground mt-1">Start tracking your reading</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <Label htmlFor="username">Username</Label>
              <Input id="username" required value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} />
            </div>
            <div>
              <Label htmlFor="email">Email</Label>
              <Input id="email" type="email" required value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
            </div>
            <div>
              <Label htmlFor="password">Password</Label>
              <Input id="password" type="password" required minLength={6} value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
            </div>
            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? "Creating account…" : "Create account"}
            </Button>
          </form>

          <p className="text-center text-sm text-muted-foreground mt-6">
            Already have an account?{" "}
            <Link to="/login" className="text-primary hover:underline">Sign in</Link>
          </p>
        </div>
      </div>
    </PageTransition>
  );
};

export default SignupPage;
