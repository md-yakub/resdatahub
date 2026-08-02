import { notFound } from "next/navigation";
import { CitationPanel } from "@/components/CitationPanel";
import { ErrorMessage } from "@/components/ErrorMessage";
import { FileList } from "@/components/FileList";
import { MetadataDownloadLinks } from "@/components/MetadataDownloadLinks";
import { formatDate } from "@/lib/format";
import { getPublicDataset } from "@/lib/api";

interface DatasetDetailsPageProps {
  params: Promise<{
    datasetId: string;
    versionId: string;
  }>;
}

export default async function DatasetDetailsPage({ params }: DatasetDetailsPageProps) {
  const { datasetId, versionId } = await params;

  try {
    const dataset = await getPublicDataset(datasetId, versionId);

    return (
      <div className="mx-auto max-w-6xl px-6 py-10">
        <article className="grid gap-8 lg:grid-cols-[1fr_320px]">
          <div>
            <p className="text-sm font-medium text-accent">Published dataset</p>
            <h1 className="mt-3 text-3xl font-semibold leading-tight text-ink md:text-4xl">
              {dataset.version.title}
            </h1>
            <p className="mt-5 whitespace-pre-wrap text-base leading-8 text-muted">{dataset.version.description}</p>
          </div>

          <aside className="border border-line bg-panel p-5">
            <h2 className="text-lg font-semibold text-ink">Record Details</h2>
            <dl className="mt-4 space-y-3 text-sm">
              <div>
                <dt className="font-medium text-ink">Version</dt>
                <dd className="mt-1 text-muted">{dataset.version.versionNumber}</dd>
              </div>
              <div>
                <dt className="font-medium text-ink">Published</dt>
                <dd className="mt-1 text-muted">{formatDate(dataset.version.publishedAt)}</dd>
              </div>
              <div>
                <dt className="font-medium text-ink">Organization</dt>
                <dd className="mt-1 text-muted">{dataset.organization.name}</dd>
              </div>
              <div>
                <dt className="font-medium text-ink">License</dt>
                <dd className="mt-1 text-muted">{dataset.license?.code ?? "Not specified"}</dd>
              </div>
            </dl>
          </aside>
        </article>

        <section className="mt-8 grid gap-5 md:grid-cols-2">
          <div className="border border-line bg-white p-5">
            <h2 className="text-lg font-semibold text-ink">Creators</h2>
            <ol className="mt-4 space-y-3">
              {dataset.creators.map((creator) => (
                <li key={creator.id} className="text-sm text-muted">
                  <span className="font-medium text-ink">
                    {creator.givenName} {creator.familyName}
                  </span>
                  {creator.affiliation && <span> - {creator.affiliation}</span>}
                  {creator.orcid && <span className="block text-xs">ORCID: {creator.orcid}</span>}
                </li>
              ))}
            </ol>
          </div>
          <div className="border border-line bg-white p-5">
            <h2 className="text-lg font-semibold text-ink">Keywords</h2>
            <div className="mt-4 flex flex-wrap gap-2">
              {dataset.keywords.map((keyword) => (
                <span key={keyword.id} className="border border-line bg-panel px-2 py-1 text-sm text-muted">
                  {keyword.value}
                </span>
              ))}
            </div>
          </div>
        </section>

        <div className="mt-8 grid gap-6">
          <FileList files={dataset.files} />
          <CitationPanel datasetId={datasetId} versionId={versionId} />
          <MetadataDownloadLinks datasetId={datasetId} versionId={versionId} />
        </div>
      </div>
    );
  } catch (error) {
    if (error instanceof Error && error.message.includes("404")) {
      notFound();
    }

    return (
      <div className="mx-auto max-w-6xl px-6 py-10">
        <ErrorMessage message="Could not load this dataset. Check the backend API configuration." />
      </div>
    );
  }
}
