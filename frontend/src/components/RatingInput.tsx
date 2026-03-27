import { Star } from "lucide-react";

interface RatingInputProps {
  value: number;
  onChange: (v: number) => void;
  max?: number;
}

const RatingInput = ({ value, onChange, max = 10 }: RatingInputProps) => {
  return (
    <div className="flex items-center gap-1">
      {Array.from({ length: max }, (_, i) => i + 1).map((n) => (
        <button
          key={n}
          type="button"
          onClick={() => onChange(n)}
          className="p-0.5 transition-colors"
        >
          <Star
            className={`h-5 w-5 transition-colors ${
              n <= value ? "fill-primary text-primary" : "text-muted-foreground/30"
            }`}
          />
        </button>
      ))}
      <span className="ml-2 text-sm font-medium text-foreground">{value}/10</span>
    </div>
  );
};

export default RatingInput;
