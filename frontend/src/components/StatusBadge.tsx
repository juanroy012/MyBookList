import { ReadingStatus } from "@/lib/api.ts";
import { Badge } from "@/components/ui/badge.tsx";

const statusConfig: Record<ReadingStatus, { label: string; variant: "default" | "secondary" | "destructive" | "outline" }> = {
  COMPLETED: { label: "Completed", variant: "default" },
  IN_PROGRESS: { label: "Reading", variant: "secondary" },
  PLAN_TO_READ: { label: "Plan to Read", variant: "outline" },
  DROPPED: { label: "Dropped", variant: "destructive" },
};

const StatusBadge = ({ status }: { status: ReadingStatus }) => {
  const config = statusConfig[status];
  return <Badge variant={config.variant}>{config.label}</Badge>;
};

export default StatusBadge;
