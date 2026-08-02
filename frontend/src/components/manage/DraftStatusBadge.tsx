import type { DatasetVersionStatus } from "@/lib/types";

export function DraftStatusBadge({ status }: { status?: DatasetVersionStatus }) {
  const label = status ?? "Not created";
  const className =
    status === "DRAFT"
      ? "border-emerald-200 bg-emerald-50 text-emerald-800"
      : status === "PUBLISHED"
        ? "border-blue-200 bg-blue-50 text-blue-800"
        : "border-line bg-panel text-muted";

  return <span className={`inline-flex border px-2 py-1 text-xs font-medium ${className}`}>{label}</span>;
}
