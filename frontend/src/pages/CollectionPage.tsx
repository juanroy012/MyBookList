import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { collectionApi, ReadingStatus, UserBook } from "@/lib/api.ts";
import { openLibrary } from "@/lib/openlibrary.ts";
import { useAuth } from "@/contexts/AuthContext.tsx";
import StatusBadge from "@/components/StatusBadge.tsx";
import RatingDisplay from "@/components/RatingDisplay.tsx";
import PageTransition from "@/components/PageTransition.tsx";
import { Button } from "@/components/ui/button.tsx";
import { Card, CardContent } from "@/components/ui/card.tsx";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select.tsx";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs.tsx";
import {
  Loader2,
  BookOpen,
  Library,
  LayoutList,
  LayoutGrid,
  Star,
  User,
  BookMarked,
  CheckCircle2,
  Clock,
  XCircle,
  Plus,
  Trash2,
} from "lucide-react";
import { useToast } from "@/hooks/use-toast.ts";
import { motion } from "framer-motion";

const MOCK_BOOKS: UserBook[] = [
  {
    id: 1,
    book: { openLibraryId: "OL45804W", title: "Dune", authorName: "Frank Herbert", coverId: 8226601 },
    readingStatus: "COMPLETED",
    rating: 9,
    review: "A masterpiece of science fiction that builds an incredibly detailed universe.",
  },
  {
    id: 2,
    book: { openLibraryId: "OL27448W", title: "The Great Gatsby", authorName: "F. Scott Fitzgerald", coverId: 8225261 },
    readingStatus: "COMPLETED",
    rating: 8,
    review: "Beautifully written critique of the American Dream.",
  },
  {
    id: 3,
    book: { openLibraryId: "OL82563W", title: "1984", authorName: "George Orwell", coverId: 12818987 },
    readingStatus: "COMPLETED",
    rating: 10,
    review: "Terrifyingly relevant. A must-read for everyone.",
  },
  {
    id: 4,
    book: { openLibraryId: "OL52151W", title: "The Hobbit", authorName: "J.R.R. Tolkien", coverId: 8739161 },
    readingStatus: "IN_PROGRESS",
  },
  {
    id: 5,
    book: { openLibraryId: "OL6990157W", title: "Project Hail Mary", authorName: "Andy Weir", coverId: 10590002 },
    readingStatus: "IN_PROGRESS",
    review: "Loving it so far!",
  },
  {
    id: 6,
    book: { openLibraryId: "OL17930368W", title: "Sapiens", authorName: "Yuval Noah Harari", coverId: 8474931 },
    readingStatus: "PLAN_TO_READ",
  },
  {
    id: 7,
    book: { openLibraryId: "OL27482W", title: "Brave New World", authorName: "Aldous Huxley", coverId: 12645157 },
    readingStatus: "PLAN_TO_READ",
  },
  {
    id: 8,
    book: { openLibraryId: "OL35137W", title: "Catch-22", authorName: "Joseph Heller", coverId: 8231990 },
    readingStatus: "DROPPED",
    rating: 4,
    review: "Couldn't get into the writing style.",
  },
];

const MOCK_USER = { username: "BookWorm42", email: "demo@mybooklist.app" };

type ViewMode = "list" | "compact";

const STATUS_ORDER: ReadingStatus[] = ["COMPLETED", "IN_PROGRESS", "PLAN_TO_READ", "DROPPED"];

const statusLabels: Record<ReadingStatus, string> = {
  COMPLETED: "Completed",
  IN_PROGRESS: "Reading",
  PLAN_TO_READ: "Plan to Read",
  DROPPED: "Dropped",
};

const statusBars: Record<ReadingStatus, string> = {
  COMPLETED: "bg-primary",
  IN_PROGRESS: "bg-secondary-foreground/40",
  PLAN_TO_READ: "bg-border",
  DROPPED: "bg-destructive/60",
};

const statusDots: Record<ReadingStatus, string> = {
  COMPLETED: "bg-primary",
  IN_PROGRESS: "bg-secondary-foreground/40",
  PLAN_TO_READ: "bg-border",
  DROPPED: "bg-destructive/60",
};

const CollectionPage = () => {
  const { isAuthenticated, user } = useAuth();
  const [books, setBooks] = useState<UserBook[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<ReadingStatus | "ALL">("ALL");
  const [viewMode, setViewMode] = useState<ViewMode>("list");
  const { toast } = useToast();

  useEffect(() => {
    if (isAuthenticated) {
      collectionApi
        .getAll()
        .then(setBooks)
        .catch((err: Error) => {
          toast({ variant: "destructive", title: "Could not load your library", description: err.message });
          setBooks(MOCK_BOOKS);
        })
        .finally(() => setLoading(false));
      return;
    }

    setBooks(MOCK_BOOKS);
    setLoading(false);
  }, [isAuthenticated, toast]);

  const handleDelete = async (openLibraryId: string, title: string) => {
    if (!isAuthenticated) {
      toast({ title: "Demo mode", description: "Sign in to remove books from your real collection." });
      return;
    }

    try {
      await collectionApi.remove(openLibraryId);
      setBooks((prev) => prev.filter((b) => b.book.openLibraryId !== openLibraryId));
      toast({ title: "Removed", description: `"${title}" removed from your library.` });
    } catch (err: any) {
      toast({ variant: "destructive", title: "Error", description: err.message });
    }
  };

  const filtered = useMemo(
    () => (filter === "ALL" ? books : books.filter((b) => b.readingStatus === filter)),
    [books, filter],
  );

  const stats = useMemo(() => {
    const completed = books.filter((b) => b.readingStatus === "COMPLETED").length;
    const reading = books.filter((b) => b.readingStatus === "IN_PROGRESS").length;
    const planned = books.filter((b) => b.readingStatus === "PLAN_TO_READ").length;
    const dropped = books.filter((b) => b.readingStatus === "DROPPED").length;
    const rated = books.filter((b) => typeof b.rating === "number");
    const reviewed = books.filter((b) => !!b.review).length;

    return {
      total: books.length,
      completed,
      reading,
      planned,
      dropped,
      reviewed,
      avgRating: rated.length
        ? (rated.reduce((sum, b) => sum + (b.rating || 0), 0) / rated.length).toFixed(1)
        : "-",
      completionRate: books.length ? `${Math.round((completed / books.length) * 100)}%` : "-",
    };
  }, [books]);

  const displayUser = isAuthenticated ? user : MOCK_USER;

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  return (
    <PageTransition>
      <div className="container py-8 max-w-6xl">
        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.35 }}
          className="flex flex-col sm:flex-row items-start sm:items-center gap-5 mb-8"
        >
          <div className="h-20 w-20 rounded-full bg-accent flex items-center justify-center shrink-0">
            <User className="h-10 w-10 text-accent-foreground" />
          </div>
          <div className="flex-1 min-w-0">
            <h1 className="font-display text-3xl font-bold text-foreground">{displayUser?.username || "Reader"}</h1>
            <p className="text-sm text-muted-foreground mt-0.5">{displayUser?.email}</p>
            {!isAuthenticated && (
              <p className="text-xs text-primary mt-1 font-medium">Demo profile - sign in to see your real collection</p>
            )}
          </div>
          <Button asChild>
            <Link to="/search">
              <Plus className="h-4 w-4 mr-1" /> Add Books
            </Link>
          </Button>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.35, delay: 0.08 }}
          className="grid grid-cols-2 sm:grid-cols-4 gap-3 mb-8"
        >
          <StatCard icon={<Library className="h-5 w-5 text-primary" />} label="Total Books" value={stats.total} />
          <StatCard icon={<CheckCircle2 className="h-5 w-5 text-primary" />} label="Completed" value={stats.completed} />
          <StatCard icon={<Clock className="h-5 w-5 text-primary" />} label="Reading" value={stats.reading} />
          <StatCard icon={<Star className="h-5 w-5 text-primary" />} label="Avg Rating" value={stats.avgRating} />
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.35, delay: 0.14 }}
          className="mb-8"
        >
          <Card>
            <CardContent className="p-4">
              <p className="text-xs font-medium text-muted-foreground mb-2 uppercase tracking-wide">Reading Breakdown</p>
              {stats.total > 0 ? (
                <>
                  <div className="flex h-3 rounded-full overflow-hidden bg-muted">
                    {STATUS_ORDER.map((status) => {
                      const value = books.filter((b) => b.readingStatus === status).length;
                      if (!value) return null;
                      return (
                        <div
                          key={status}
                          className={`${statusBars[status]} transition-all`}
                          style={{ width: `${(value / stats.total) * 100}%` }}
                          title={`${statusLabels[status]}: ${value}`}
                        />
                      );
                    })}
                  </div>
                  <div className="flex flex-wrap gap-x-5 gap-y-1 mt-2.5 text-xs text-muted-foreground">
                    {STATUS_ORDER.map((status) => {
                      const value = books.filter((b) => b.readingStatus === status).length;
                      return (
                        <span key={status} className="flex items-center gap-1.5">
                          <span className={`h-2.5 w-2.5 rounded-full inline-block ${statusDots[status]}`} />
                          {statusLabels[status]} ({value})
                        </span>
                      );
                    })}
                  </div>
                </>
              ) : (
                <p className="text-sm text-muted-foreground">No books yet.</p>
              )}
            </CardContent>
          </Card>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.35, delay: 0.18 }}
          className="grid grid-cols-3 gap-3 mb-8"
        >
          <Card className="text-center">
            <CardContent className="p-4">
              <p className="text-2xl font-display font-bold text-foreground">{stats.reviewed}</p>
              <p className="text-xs text-muted-foreground mt-0.5">Reviews Written</p>
            </CardContent>
          </Card>
          <Card className="text-center">
            <CardContent className="p-4">
              <p className="text-2xl font-display font-bold text-foreground">{stats.planned}</p>
              <p className="text-xs text-muted-foreground mt-0.5">In Backlog</p>
            </CardContent>
          </Card>
          <Card className="text-center">
            <CardContent className="p-4">
              <p className="text-2xl font-display font-bold text-foreground">{stats.completionRate}</p>
              <p className="text-xs text-muted-foreground mt-0.5">Completion Rate</p>
            </CardContent>
          </Card>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.35, delay: 0.22 }}
          className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-5"
        >
          <h2 className="font-display text-xl font-semibold text-foreground flex items-center gap-2">
            <BookOpen className="h-5 w-5 text-primary" /> Collection
          </h2>
          <div className="flex items-center gap-2">
            <Tabs value={viewMode} onValueChange={(v) => setViewMode(v as ViewMode)}>
              <TabsList className="h-9">
                <TabsTrigger value="list" className="px-2.5 gap-1">
                  <LayoutList className="h-4 w-4" /> List
                </TabsTrigger>
                <TabsTrigger value="compact" className="px-2.5 gap-1">
                  <LayoutGrid className="h-4 w-4" /> Compact
                </TabsTrigger>
              </TabsList>
            </Tabs>
            <Select value={filter} onValueChange={(v) => setFilter(v as ReadingStatus | "ALL")}>
              <SelectTrigger className="w-[145px] h-9">
                <SelectValue placeholder="Filter" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="ALL">All Books</SelectItem>
                <SelectItem value="IN_PROGRESS">Reading</SelectItem>
                <SelectItem value="COMPLETED">Completed</SelectItem>
                <SelectItem value="PLAN_TO_READ">Plan to Read</SelectItem>
                <SelectItem value="DROPPED">Dropped</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </motion.div>

        {filtered.length === 0 ? (
          <div className="text-center py-16">
            <BookOpen className="h-12 w-12 mx-auto text-muted-foreground/30 mb-4" />
            <p className="text-muted-foreground">
              {books.length === 0 ? "No books in collection yet." : "No books match this filter."}
            </p>
            {books.length === 0 && (
              <Button asChild className="mt-4">
                <Link to="/search">Discover Books</Link>
              </Button>
            )}
          </div>
        ) : viewMode === "list" ? (
          <div className="space-y-3">
            {filtered.map((ub, i) => (
              <ListItem key={ub.id} ub={ub} index={i} onDelete={handleDelete} />
            ))}
          </div>
        ) : (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
            {filtered.map((ub, i) => (
              <CompactItem key={ub.id} ub={ub} index={i} onDelete={handleDelete} />
            ))}
          </div>
        )}
      </div>
    </PageTransition>
  );
};

const StatCard = ({ icon, label, value }: { icon: React.ReactNode; label: string; value: string | number }) => (
  <Card>
    <CardContent className="p-4 flex items-center gap-3">
      <div className="h-10 w-10 rounded-lg bg-accent flex items-center justify-center shrink-0">{icon}</div>
      <div>
        <p className="text-xl font-display font-bold text-foreground leading-tight">{value}</p>
        <p className="text-xs text-muted-foreground">{label}</p>
      </div>
    </CardContent>
  </Card>
);

const ListItem = ({
  ub,
  index,
  onDelete,
}: {
  ub: UserBook;
  index: number;
  onDelete: (openLibraryId: string, title: string) => void;
}) => {
  const coverUrl = openLibrary.coverUrl(ub.book.coverId, "S");

  return (
    <motion.div
      initial={{ opacity: 0, y: 6 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.2, delay: index * 0.025 }}
      className="flex items-center gap-4 p-4 rounded-lg border bg-card hover:shadow-sm transition-shadow"
    >
      <Link to={`/book/${ub.book.openLibraryId}`} className="shrink-0">
        <div className="w-12 h-[72px] rounded overflow-hidden bg-muted shrink-0">
          {coverUrl ? (
            <img src={coverUrl} alt={ub.book.title} className="w-full h-full object-cover" loading="lazy" />
          ) : (
            <div className="w-full h-full flex items-center justify-center">
              <BookOpen className="h-5 w-5 text-muted-foreground" />
            </div>
          )}
        </div>
      </Link>

      <div className="flex-1 min-w-0">
        <Link to={`/book/${ub.book.openLibraryId}`} className="font-display text-sm font-semibold text-foreground line-clamp-1">
          {ub.book.title}
        </Link>
        <p className="text-xs text-muted-foreground">{ub.book.authorName || "Unknown author"}</p>
        {ub.review && <p className="text-xs text-muted-foreground mt-1 line-clamp-1 italic">"{ub.review}"</p>}
      </div>

      <div className="hidden sm:flex items-center gap-3">
        <StatusBadge status={ub.readingStatus} />
        {ub.rating != null && <RatingDisplay rating={ub.rating} />}
      </div>

      <div className="sm:hidden flex flex-col items-end gap-1">
        <StatusBadge status={ub.readingStatus} />
        {ub.rating != null && <RatingDisplay rating={ub.rating} />}
      </div>

      <Button
        variant="ghost"
        size="icon"
        className="shrink-0 text-muted-foreground hover:text-destructive"
        onClick={() => onDelete(ub.book.openLibraryId, ub.book.title)}
      >
        <Trash2 className="h-4 w-4" />
      </Button>
    </motion.div>
  );
};

const CompactItem = ({
  ub,
  index,
  onDelete,
}: {
  ub: UserBook;
  index: number;
  onDelete: (openLibraryId: string, title: string) => void;
}) => {
  const coverUrl = openLibrary.coverUrl(ub.book.coverId, "M");

  return (
    <motion.div
      initial={{ opacity: 0, scale: 0.97 }}
      animate={{ opacity: 1, scale: 1 }}
      transition={{ duration: 0.2, delay: index * 0.03 }}
      className="relative"
    >
      <Button
        variant="secondary"
        size="icon"
        className="absolute top-1.5 left-1.5 z-10 h-7 w-7"
        onClick={() => onDelete(ub.book.openLibraryId, ub.book.title)}
      >
        <Trash2 className="h-3.5 w-3.5" />
      </Button>
      <Link to={`/book/${ub.book.openLibraryId}`} className="group block">
        <div className="aspect-[2/3] rounded-lg overflow-hidden bg-muted relative">
          {coverUrl ? (
            <img src={coverUrl} alt={ub.book.title} className="w-full h-full object-cover" loading="lazy" />
          ) : (
            <div className="w-full h-full flex items-center justify-center">
              <BookOpen className="h-8 w-8 text-muted-foreground/40" />
            </div>
          )}
          <div className="absolute inset-0 bg-foreground/0 group-hover:bg-foreground/60 transition-colors flex items-end p-2 opacity-0 group-hover:opacity-100">
            <div className="w-full">
              {ub.rating != null && (
                <div className="flex items-center gap-1 mb-1">
                  <Star className="h-3 w-3 fill-primary text-primary" />
                  <span className="text-xs font-medium text-primary-foreground">{ub.rating}/10</span>
                </div>
              )}
              {ub.review && <p className="text-[10px] text-primary-foreground/80 line-clamp-2 italic">"{ub.review}"</p>}
            </div>
          </div>
          <div className="absolute top-1.5 right-1.5">
            <StatusBadge status={ub.readingStatus} />
          </div>
        </div>
        <p className="font-display text-xs font-semibold text-foreground mt-1.5 line-clamp-1">{ub.book.title}</p>
        <p className="text-[11px] text-muted-foreground line-clamp-1">{ub.book.authorName}</p>
      </Link>
    </motion.div>
  );
};

export default CollectionPage;
