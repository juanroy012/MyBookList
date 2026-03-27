import { Link } from "react-router-dom";
import { openLibrary, OLSearchResult, OLTrendingResult } from "@/lib/openlibrary.ts";
import { BookOpen } from "lucide-react";

interface BookCardProps {
  book: OLSearchResult | OLTrendingResult;
}

const BookCard = ({ book }: BookCardProps) => {
  const coverId = "cover_i" in book ? (book as any).cover_i : (book as any).cover_id;
  const coverUrl = openLibrary.coverUrl(coverId, "M");
  const workId = openLibrary.workIdFromKey(book.key);
  const authors = book.author_name?.join(", ") || "Unknown author";
  const year = book.first_publish_year;

  return (
    <Link
      to={`/book/${workId}`}
      className="group block rounded-lg overflow-hidden bg-card border transition-shadow hover:shadow-lg"
    >
      <div className="aspect-[2/3] bg-muted relative overflow-hidden">
        {coverUrl ? (
          <img
            src={coverUrl}
            alt={book.title}
            className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-[1.03]"
            loading="lazy"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center text-muted-foreground">
            <BookOpen className="h-12 w-12" />
          </div>
        )}
      </div>
      <div className="p-3">
        <h3 className="font-display text-sm font-semibold leading-tight line-clamp-2 text-foreground">
          {book.title}
        </h3>
        <p className="text-xs text-muted-foreground mt-1 line-clamp-1">{authors}</p>
        {year && <p className="text-xs text-muted-foreground mt-0.5">{year}</p>}
      </div>
    </Link>
  );
};

export default BookCard;
