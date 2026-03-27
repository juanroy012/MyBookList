import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { openLibrary, OLWorkDetail } from "@/lib/openlibrary.ts";
import { useAuth } from "@/contexts/AuthContext.tsx";
import { collectionApi, ReadingStatus, Book } from "@/lib/api.ts";
import PageTransition from "@/components/PageTransition.tsx";
import RatingInput from "@/components/RatingInput.tsx";
import { Button } from "@/components/ui/button.tsx";
import { Textarea } from "@/components/ui/textarea.tsx";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select.tsx";
import { Label } from "@/components/ui/label.tsx";
import { BookOpen, ArrowLeft, Loader2, Plus, Check } from "lucide-react";
import { useToast } from "@/hooks/use-toast.ts";
import { motion } from "framer-motion";

const BookDetailPage = () => {
  const { workId } = useParams<{ workId: string }>();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const { toast } = useToast();

  const [work, setWork] = useState<OLWorkDetail | null>(null);
  const [loading, setLoading] = useState(true);

  // Collection form
  const [status, setStatus] = useState<ReadingStatus>("PLAN_TO_READ");
  const [rating, setRating] = useState(5);
  const [review, setReview] = useState("");
  const [adding, setAdding] = useState(false);
  const [added, setAdded] = useState(false);

  useEffect(() => {
    if (!workId) return;
    openLibrary.getWork(workId).then(setWork).catch(() => {}).finally(() => setLoading(false));
  }, [workId]);

  const description = work?.description
    ? typeof work.description === "string" ? work.description : work.description.value
    : null;
  const coverId = work?.covers?.[0];
  const coverUrl = openLibrary.coverUrl(coverId, "L");
  const firstSentence = work?.first_sentence?.value;

  const handleAddToCollection = async () => {
    if (!work || !workId) return;
    setAdding(true);
    const isbn = work.identifiers?.isbn_13?.[0] || work.identifiers?.isbn_10?.[0] || `no-isbn-${workId}`;
    const book: Book = {
      openLibraryId: workId,
      title: work.title,
      authorName: "Unknown author",
      coverId: coverId,
      isbn,
      firstSentence: firstSentence,
      description: description || undefined,
    };
    try {
      await collectionApi.add({ book, readingStatus: status, rating, review: review || undefined });
      setAdded(true);
      toast({ title: "Added to library!", description: `"${work.title}" is now in your collection.` });
    } catch (err: any) {
      toast({ variant: "destructive", title: "Error", description: err.message });
    } finally {
      setAdding(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  if (!work) {
    return (
      <div className="container py-12 text-center">
        <p className="text-muted-foreground">Book not found.</p>
      </div>
    );
  }

  return (
    <PageTransition>
      <div className="container py-8">
        <Button variant="ghost" size="sm" onClick={() => navigate(-1)} className="mb-6">
          <ArrowLeft className="h-4 w-4 mr-1" /> Back
        </Button>

        <div className="grid md:grid-cols-[280px_1fr] gap-8">
          {/* Cover */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.3 }}
          >
            <div className="aspect-[2/3] rounded-lg overflow-hidden bg-muted">
              {coverUrl ? (
                <img src={coverUrl} alt={work.title} className="w-full h-full object-cover" />
              ) : (
                <div className="w-full h-full flex items-center justify-center text-muted-foreground">
                  <BookOpen className="h-16 w-16" />
                </div>
              )}
            </div>
          </motion.div>

          {/* Details */}
          <div>
            <h1 className="font-display text-3xl font-bold text-foreground">{work.title}</h1>

            {work.subjects && work.subjects.length > 0 && (
              <div className="flex flex-wrap gap-1.5 mt-3">
                {work.subjects.slice(0, 8).map((s) => (
                  <span key={s} className="text-xs px-2 py-0.5 rounded-full bg-accent text-accent-foreground">
                    {s}
                  </span>
                ))}
              </div>
            )}

            {description && (
              <p className="mt-6 text-sm text-muted-foreground leading-relaxed max-w-2xl whitespace-pre-line">
                {description.slice(0, 1200)}
                {description.length > 1200 && "…"}
              </p>
            )}

            {firstSentence && !description && (
              <p className="mt-6 text-sm text-muted-foreground italic">"{firstSentence}"</p>
            )}

            {/* Add to Collection */}
            {isAuthenticated && (
              <div className="mt-10 p-6 rounded-lg border bg-card max-w-md">
                <h2 className="font-display text-lg font-semibold text-foreground mb-4">Add to Library</h2>

                <div className="space-y-4">
                  <div>
                    <Label>Reading Status</Label>
                    <Select value={status} onValueChange={(v) => setStatus(v as ReadingStatus)}>
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="PLAN_TO_READ">Plan to Read</SelectItem>
                        <SelectItem value="IN_PROGRESS">Currently Reading</SelectItem>
                        <SelectItem value="COMPLETED">Completed</SelectItem>
                        <SelectItem value="DROPPED">Dropped</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <div>
                    <Label>Rating</Label>
                    <RatingInput value={rating} onChange={setRating} />
                  </div>

                  <div>
                    <Label>Review (optional)</Label>
                    <Textarea
                      placeholder="Write your thoughts…"
                      value={review}
                      onChange={(e) => setReview(e.target.value)}
                      rows={3}
                    />
                  </div>

                  <Button onClick={handleAddToCollection} disabled={adding || added} className="w-full">
                    {added ? (
                      <><Check className="h-4 w-4 mr-1" /> Added</>
                    ) : adding ? (
                      <Loader2 className="h-4 w-4 animate-spin" />
                    ) : (
                      <><Plus className="h-4 w-4 mr-1" /> Add to Library</>
                    )}
                  </Button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </PageTransition>
  );
};

export default BookDetailPage;
