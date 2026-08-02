import Link from "next/link";
import { DraftStatusBadge } from "@/components/manage/DraftStatusBadge";
import { ErrorMessage } from "@/components/ErrorMessage";
import { formatDate } from "@/lib/format";
import { getDatasets } from "@/lib/api";

export default async function ManagePage() {
  try {
    const datasets = await getDatasets();

    return (
      <div className="mx-auto max-w-6xl px-6 py-10">
        <div className="flex flex-col gap-5 md:flex-row md:items-start md:justify-between">
          <div>
            <p className="text-sm font-medium text-accent">Development workspace</p>
            <h1 className="mt-3 text-3xl font-semibold text-ink">Dataset Management</h1>
            <p className="mt-3 max-w-3xl text-sm leading-6 text-muted">
              Authentication is not implemented yet. Use this management area only in local development.
            </p>
          </div>
          <Link href="/manage/datasets/new" className="bg-ink px-4 py-2 text-sm font-medium text-white">
            Create dataset
          </Link>
        </div>

        <section className="mt-8 space-y-4">
          {datasets.length === 0 && (
            <div className="border border-line bg-white p-6 text-sm text-muted">No datasets have been created yet.</div>
          )}

          {datasets.map((dataset) => {
            const latest = dataset.latestVersion;

            return (
              <article key={dataset.id} className="border border-line bg-white p-5">
                <div className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
                  <div>
                    <h2 className="text-xl font-semibold text-ink">{latest?.title ?? "Untitled dataset"}</h2>
                    <p className="mt-2 text-sm text-muted">
                      {dataset.organization.shortName ?? dataset.organization.name}
                    </p>
                  </div>
                  <DraftStatusBadge status={latest?.status} />
                </div>

                <dl className="mt-5 grid gap-3 text-sm md:grid-cols-3">
                  <div>
                    <dt className="font-medium text-ink">Latest version</dt>
                    <dd className="mt-1 text-muted">{latest?.versionNumber ?? "None"}</dd>
                  </div>
                  <div>
                    <dt className="font-medium text-ink">Published</dt>
                    <dd className="mt-1 text-muted">{formatDate(latest?.publishedAt)}</dd>
                  </div>
                  <div>
                    <dt className="font-medium text-ink">Updated</dt>
                    <dd className="mt-1 text-muted">{formatDate(dataset.updatedAt)}</dd>
                  </div>
                </dl>

                {latest && (
                  <div className="mt-5">
                    <Link
                      href={`/manage/datasets/${dataset.id}/versions/${latest.id}`}
                      className="border border-line px-4 py-2 text-sm font-medium text-muted hover:text-ink"
                    >
                      {latest.status === "DRAFT" ? "Continue draft" : "View read-only"}
                    </Link>
                  </div>
                )}
              </article>
            );
          })}
        </section>
      </div>
    );
  } catch {
    return (
      <div className="mx-auto max-w-6xl px-6 py-10">
        <ErrorMessage message="Could not load datasets for the management workspace." />
      </div>
    );
  }
}
