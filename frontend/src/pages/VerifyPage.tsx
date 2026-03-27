import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { authApi } from "@/lib/api.ts";
import { Button } from "@/components/ui/button.tsx";
import { Input } from "@/components/ui/input.tsx";
import { Label } from "@/components/ui/label.tsx";
import PageTransition from "@/components/PageTransition.tsx";
import { BookOpen } from "lucide-react";
import { useToast } from "@/hooks/use-toast.ts";

const VerifyPage = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { toast } = useToast();
  const [email, setEmail] = useState((location.state as any)?.email || "");
  const [code, setCode] = useState("");
  const [loading, setLoading] = useState(false);
  const [resending, setResending] = useState(false);

  const handleVerify = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await authApi.verify({ email, verificationCode: code });
      toast({ title: "Email verified!", description: "You can now sign in." });
      navigate("/login");
    } catch (err: any) {
      toast({ variant: "destructive", title: "Verification failed", description: err.message });
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    if (!email) return;
    setResending(true);
    try {
      await authApi.resend(email);
      toast({ title: "Code resent", description: "Check your email for a new verification code." });
    } catch (err: any) {
      toast({ variant: "destructive", title: "Error", description: err.message });
    } finally {
      setResending(false);
    }
  };

  return (
    <PageTransition>
      <div className="flex min-h-[80vh] items-center justify-center">
        <div className="w-full max-w-sm mx-auto px-4">
          <div className="text-center mb-8">
            <BookOpen className="h-10 w-10 text-primary mx-auto mb-3" />
            <h1 className="font-display text-2xl font-bold text-foreground">Verify your email</h1>
            <p className="text-sm text-muted-foreground mt-1">Enter the code sent to your email</p>
          </div>

          <form onSubmit={handleVerify} className="space-y-4">
            <div>
              <Label htmlFor="email">Email</Label>
              <Input id="email" type="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
            </div>
            <div>
              <Label htmlFor="code">Verification Code</Label>
              <Input id="code" required placeholder="123456" value={code} onChange={(e) => setCode(e.target.value)} />
            </div>
            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? "Verifying…" : "Verify"}
            </Button>
          </form>

          <div className="text-center mt-4">
            <Button variant="link" size="sm" onClick={handleResend} disabled={resending || !email}>
              {resending ? "Resending…" : "Resend code"}
            </Button>
          </div>
        </div>
      </div>
    </PageTransition>
  );
};

export default VerifyPage;
