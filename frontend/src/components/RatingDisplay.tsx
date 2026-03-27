import { Star } from "lucide-react";

const RatingDisplay = ({ rating, max = 10 }: { rating: number; max?: number }) => {
  return (
    <div className="flex items-center gap-1">
      <Star className="h-4 w-4 fill-primary text-primary" />
      <span className="text-sm font-medium text-foreground">{rating}</span>
      <span className="text-xs text-muted-foreground">/ {max}</span>
    </div>
  );
};

export default RatingDisplay;
