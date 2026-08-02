import Link from "next/link";
import { DatasetCard } from "@/components/DatasetCard";
import { EmptyState } from "@/components/EmptyState";
import { ErrorMessage } from "@/components/ErrorMessage";
import { getCatalogInfo, getCatalogValidation, searchDatasets } from "@/lib/api";

export default async function HomePage() {
  const [catalogInfoResult, validationResult, latestResult] = await Promise.allSettled([
    getCatalogInfo(),
    getCatalogValidation(),
    searchDatasets({ size: 3, sort: "NEWEST" })
  ]);

  const catalogInfo = catalogInfoResult.status === "fulfilled" ? catalogInfoResult.value : null;
  const validation = validationResult.status === "fulfilled" ? validationResult.value : null;
  const latest = latestResult.status === "fulfilled" ? latestResult.value.items : [];

  return (
    <div className="mx-auto max-w-6xl px-6 py-12">
      <section className="grid gap-8 md:grid-cols-[1fr_360px] md:items-start">
        <div>
          <h1 className="text-4xl font-semibold tracking-normal text-ink md:text-5xl">ResDataHub</h1>
          <p className="mt-5 max-w-2xl text-lg leading-8 text-muted">
            Discover published research datasets, access files, copy citations, and export metadata for FAIR harvesting.
          </p>
          <form action="/datasets" className="mt-8 flex max-w-2xl flex-col gap-3 sm:flex-row">
            <input
              className="focus-ring min-h-11 flex-1 border border-line px-4 text-base"
              name="q"
              placeholder="Search datasets, creators, organizations, keywords"
            />
            <button className="focus-ring bg-accent px-5 py-3 text-sm font-medium text-white" type="submit">
              Search
            </button>
          </form>
        </div>

        <aside className="border border-line bg-panel p-5">
          <h2 className="text-lg font-semibold text-ink">Catalog Summary</h2>
          <dl className="mt-4 space-y-3 text-sm">
            <div>
              <dt className="font-medium text-ink">Title</dt>
              <dd className="mt-1 text-muted">{catalogInfo?.title ?? "Unavailable"}</dd>
            </div>
            <div>
              <dt className="font-medium text-ink">Published datasets</dt>
              <dd className="mt-1 text-muted">{validation?.checkedDatasets ?? "Unavailable"}</dd>
            </div>
            <div>
              <dt className="font-medium text-ink">Metadata formats</dt>
              <dd className="mt-1 text-muted">{catalogInfo?.supportedFormats.join(", ") ?? "Unavailable"}</dd>
            </div>
            <div>
              <dt className="font-medium text-ink">Validation</dt>
              <dd className="mt-1 text-muted">
                {validation ? (validation.conforms ? "Conforms" : "Issues found") : "Unavailable"}
              </dd>
            </div>
          </dl>
        </aside>
      </section>

      <section className="mt-14">
        <div className="mb-5 flex items-end justify-between gap-4">
          <div>
            <h2 className="text-2xl font-semibold text-ink">Latest Published Datasets</h2>
            <p className="mt-2 text-sm text-muted">Recently published records from the public catalog.</p>
          </div>
          <Link href="/datasets" className="text-sm font-medium text-accent">
            View all
          </Link>
        </div>
        {latestResult.status === "rejected" && <ErrorMessage message="Could not load latest datasets." />}
        {latestResult.status === "fulfilled" && latest.length === 0 && (
          <EmptyState title="No published datasets" message="Published datasets will appear here." />
        )}
        <div className="grid gap-4">
          {latest.map((dataset) => (
            <DatasetCard key={dataset.versionId} dataset={dataset} />
          ))}
        </div>
      </section>
    </div>
  );
}
