import { useState, useEffect, useCallback } from "react";
import { useSearchParams } from "react-router-dom";
import { openLibrary, OLSearchResult } from "@/lib/openlibrary.ts";
import BookCard from "@/components/BookCard.tsx";
import PageTransition from "@/components/PageTransition.tsx";
import { Input } from "@/components/ui/input.tsx";
import { Button } from "@/components/ui/button.tsx";
import { Search, Loader2 } from "lucide-react";
import { motion } from "framer-motion";

const SearchPage = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [query, setQuery] = useState(searchParams.get("q") || "");
  const [results, setResults] = useState<OLSearchResult[]>([]);
  const [totalResults, setTotalResults] = useState(0);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);

  const doSearch = useCallback(async (q: string, p: number) => {
    if (!q.trim()) return;
    setLoading(true);
    setSearched(true);
    try {
      const res = await openLibrary.search(q, p, 24);
      setResults(res.docs || []);
      setTotalResults(res.numFound || 0);
    } catch {
      setResults([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const q = searchParams.get("q");
    if (q) {
      setQuery(q);
      doSearch(q, 1);
    }
  }, []); // eslint-disable-line

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(1);
    setSearchParams({ q: query });
    doSearch(query, 1);
  };

  const handlePageChange = (newPage: number) => {
    setPage(newPage);
    doSearch(query, newPage);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  return (
    <PageTransition>
      <div className="container py-8">
        <h1 className="font-display text-3xl font-bold text-foreground mb-6">Discover Books</h1>

        <form onSubmit={handleSubmit} className="flex gap-2 mb-8 max-w-xl">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Search by title, author, or ISBN…"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              className="pl-10"
            />
          </div>
          <Button type="submit" disabled={loading}>
            {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : "Search"}
          </Button>
        </form>

        {loading && (
          <div className="flex items-center justify-center py-20">
            <Loader2 className="h-8 w-8 animate-spin text-primary" />
          </div>
        )}

        {!loading && searched && results.length === 0 && (
          <div className="text-center py-20 text-muted-foreground">
            <p>No books found. Try a different search term.</p>
          </div>
        )}

        {!loading && results.length > 0 && (
          <>
            <p className="text-sm text-muted-foreground mb-4">
              {totalResults.toLocaleString()} results found
            </p>
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-4">
              {results.map((book, i) => (
                <motion.div
                  key={book.key}
                  initial={{ opacity: 0, y: 8 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.25, delay: i * 0.02 }}
                >
                  <BookCard book={book} />
                </motion.div>
              ))}
            </div>

            {/* Pagination */}
            <div className="flex justify-center gap-2 mt-10">
              <Button variant="outline" size="sm" disabled={page <= 1} onClick={() => handlePageChange(page - 1)}>
                Previous
              </Button>
              <span className="flex items-center text-sm text-muted-foreground px-3">Page {page}</span>
              <Button variant="outline" size="sm" disabled={results.length < 24} onClick={() => handlePageChange(page + 1)}>
                Next
              </Button>
            </div>
          </>
        )}

        {!searched && !loading && (
          <div className="text-center py-20 text-muted-foreground">
            <Search className="h-12 w-12 mx-auto mb-4 opacity-30" />
            <p>Search for books by title, author, or ISBN</p>
          </div>
        )}
      </div>
    </PageTransition>
  );
};

export default SearchPage;
