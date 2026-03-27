import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { openLibrary, OLTrendingResult } from "@/lib/openlibrary.ts";
import { useAuth } from "@/contexts/AuthContext.tsx";
import BookCard from "@/components/BookCard.tsx";
import PageTransition from "@/components/PageTransition.tsx";
import { Button } from "@/components/ui/button.tsx";
import { Search, BookOpen, TrendingUp, ArrowRight } from "lucide-react";
import { motion } from "framer-motion";

const HomePage = () => {
  const { isAuthenticated } = useAuth();
  const [trending, setTrending] = useState<OLTrendingResult[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    openLibrary.getTrending("weekly").then((res) => {
      setTrending(res.works?.slice(0, 12) || []);
      setLoading(false);
    }).catch(() => setLoading(false));
  }, []);

  return (
    <PageTransition>
      {/* Hero */}
      <section className="relative overflow-hidden border-b">
        <div className="container py-20 md:py-28">
          <div className="max-w-2xl">
            <motion.h1
              className="font-display text-4xl md:text-5xl lg:text-6xl font-bold tracking-tight text-foreground leading-[1.1]"
              initial={{ opacity: 0, y: 12 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.4 }}
            >
              Track every book.
              <br />
              <span className="text-primary">Share your journey.</span>
            </motion.h1>
            <motion.p
              className="mt-5 text-lg text-muted-foreground max-w-lg"
              initial={{ opacity: 0, y: 12 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.4, delay: 0.1 }}
            >
              Your personal reading companion. Catalog books you've read, track what you're reading, and discover what to read next.
            </motion.p>
            <motion.div
              className="mt-8 flex gap-3"
              initial={{ opacity: 0, y: 12 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.4, delay: 0.2 }}
            >
              {isAuthenticated ? (
                <>
                  <Button asChild size="lg">
                    <Link to="/collection"><BookOpen className="mr-2 h-4 w-4" /> My Library</Link>
                  </Button>
                  <Button asChild variant="outline" size="lg">
                    <Link to="/search"><Search className="mr-2 h-4 w-4" /> Discover</Link>
                  </Button>
                </>
              ) : (
                <>
                  <Button asChild size="lg">
                    <Link to="/signup">Get Started <ArrowRight className="ml-2 h-4 w-4" /></Link>
                  </Button>
                  <Button asChild variant="outline" size="lg">
                    <Link to="/search"><Search className="mr-2 h-4 w-4" /> Browse Books</Link>
                  </Button>
                </>
              )}
            </motion.div>
          </div>
        </div>
        {/* Subtle decorative element */}
        <div className="absolute top-0 right-0 w-1/3 h-full opacity-[0.03] pointer-events-none">
          <BookOpen className="w-full h-full" />
        </div>
      </section>

      {/* Trending */}
      <section className="container py-16">
        <div className="flex items-center justify-between mb-8">
          <div className="flex items-center gap-2">
            <TrendingUp className="h-5 w-5 text-primary" />
            <h2 className="font-display text-2xl font-semibold text-foreground">Trending This Week</h2>
          </div>
          <Link to="/search" className="text-sm text-primary hover:underline flex items-center gap-1">
            View all <ArrowRight className="h-3 w-3" />
          </Link>
        </div>

        {loading ? (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-4">
            {Array.from({ length: 6 }).map((_, i) => (
              <div key={i} className="animate-pulse">
                <div className="aspect-[2/3] bg-muted rounded-lg" />
                <div className="mt-2 h-4 bg-muted rounded w-3/4" />
                <div className="mt-1 h-3 bg-muted rounded w-1/2" />
              </div>
            ))}
          </div>
        ) : (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-4">
            {trending.map((book, i) => (
              <motion.div
                key={book.key}
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.3, delay: i * 0.04 }}
              >
                <BookCard book={book} />
              </motion.div>
            ))}
          </div>
        )}
      </section>

      {/* Features */}
      <section className="border-t bg-card">
        <div className="container py-16">
          <h2 className="font-display text-2xl font-semibold text-foreground text-center mb-10">
            Everything a reader needs
          </h2>
          <div className="grid sm:grid-cols-3 gap-8 max-w-3xl mx-auto">
            {[
              { title: "Track Progress", desc: "Mark books as reading, completed, plan to read, or dropped." },
              { title: "Rate & Review", desc: "Score books from 1 to 10 and write personal reviews." },
              { title: "Discover", desc: "Search millions of books via the Open Library catalog." },
            ].map((f, i) => (
              <motion.div
                key={f.title}
                className="text-center"
                initial={{ opacity: 0, y: 10 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.3, delay: i * 0.1 }}
              >
                <h3 className="font-display text-lg font-semibold text-foreground">{f.title}</h3>
                <p className="mt-2 text-sm text-muted-foreground">{f.desc}</p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>
    </PageTransition>
  );
};

export default HomePage;
