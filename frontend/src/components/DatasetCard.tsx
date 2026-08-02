import Link from "next/link";
import type { PublicSearchItem } from "@/lib/types";
import { formatDate } from "@/lib/format";

export function DatasetCard({ dataset }: { dataset: PublicSearchItem }) {
  const href = `/datasets/${dataset.datasetId}/versions/${dataset.versionId}`;

  return (
    <article className="border border-line bg-white p-5">
      <div className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
        <div>
          <Link href={href} className="text-xl font-semibold text-ink hover:text-accent">
            {dataset.title}
          </Link>
          <p className="mt-2 line-clamp-3 text-sm leading-6 text-muted">{dataset.description}</p>
        </div>
        <div className="shrink-0 text-sm text-muted">Version {dataset.versionNumber}</div>
      </div>

      <dl className="mt-5 grid gap-3 text-sm md:grid-cols-3">
        <div>
          <dt className="font-medium text-ink">Organization</dt>
          <dd className="mt-1 text-muted">{dataset.organization.shortName ?? dataset.organization.name}</dd>
        </div>
        <div>
          <dt className="font-medium text-ink">Published</dt>
          <dd className="mt-1 text-muted">{formatDate(dataset.publishedAt)}</dd>
        </div>
        <div>
          <dt className="font-medium text-ink">License</dt>
          <dd className="mt-1 text-muted">{dataset.licenseCode ?? "Not specified"}</dd>
        </div>
      </dl>

      {dataset.keywords.length > 0 && (
        <div className="mt-5 flex flex-wrap gap-2">
          {dataset.keywords.map((keyword) => (
            <span key={keyword} className="border border-line bg-panel px-2 py-1 text-xs text-muted">
              {keyword}
            </span>
          ))}
        </div>
      )}
    </article>
  );
}
