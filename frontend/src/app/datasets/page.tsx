import { DatasetCard } from "@/components/DatasetCard";
import { EmptyState } from "@/components/EmptyState";
import { ErrorMessage } from "@/components/ErrorMessage";
import { Pagination } from "@/components/Pagination";
import { SearchFilters } from "@/components/SearchFilters";
import { searchDatasets } from "@/lib/api";
import type { SearchSort } from "@/lib/types";

interface DatasetsPageProps {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}

export default async function DatasetsPage({ searchParams }: DatasetsPageProps) {
  const params = await searchParams;
  const page = toNumber(first(params.page), 0);
  const size = toNumber(first(params.size), 20);

  try {
    const results = await searchDatasets({
      q: first(params.q),
      page,
      size,
      organizationId: first(params.organizationId),
      keyword: first(params.keyword),
      licenseCode: first(params.licenseCode),
      sort: toSort(first(params.sort))
    });

    return (
      <div className="mx-auto max-w-6xl px-6 py-10">
        <div className="mb-6">
          <h1 className="text-3xl font-semibold text-ink">Search Datasets</h1>
          <p className="mt-2 text-sm text-muted">Search only published dataset versions.</p>
        </div>
        <SearchFilters />
        <div className="mt-6 text-sm text-muted">{results.totalElements} results</div>
        <div className="mt-4 grid gap-4">
          {results.items.map((dataset) => (
            <DatasetCard key={dataset.versionId} dataset={dataset} />
          ))}
        </div>
        {results.items.length === 0 && (
          <div className="mt-4">
            <EmptyState title="No datasets found" message="Try a broader query or remove filters." />
          </div>
        )}
        <div className="mt-6">
          <Pagination page={results.page} totalPages={results.totalPages} />
        </div>
      </div>
    );
  } catch {
    return (
      <div className="mx-auto max-w-6xl px-6 py-10">
        <div className="mb-6">
          <h1 className="text-3xl font-semibold text-ink">Search Datasets</h1>
        </div>
        <SearchFilters />
        <div className="mt-6">
          <ErrorMessage message="Could not load search results. Check the backend API configuration." />
        </div>
      </div>
    );
  }
}

function first(value: string | string[] | undefined) {
  return Array.isArray(value) ? value[0] : value;
}

function toNumber(value: string | undefined, fallback: number) {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : fallback;
}

function toSort(value: string | undefined): SearchSort {
  if (value === "OLDEST" || value === "TITLE_ASC" || value === "TITLE_DESC") {
    return value;
  }

  return "NEWEST";
}
