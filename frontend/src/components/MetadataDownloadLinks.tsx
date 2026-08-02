import { buildMetadataUrl } from "@/lib/api";

export function MetadataDownloadLinks({ datasetId, versionId }: { datasetId: string; versionId: string }) {
  const links = [
    { label: "Turtle", format: "TURTLE" as const },
    { label: "JSON-LD", format: "JSON_LD" as const },
    { label: "RDF/XML", format: "RDF_XML" as const }
  ];

  return (
    <section className="border border-line bg-white p-5">
      <h2 className="text-lg font-semibold text-ink">Metadata</h2>
      <div className="mt-4 flex flex-wrap gap-3">
        {links.map((link) => (
          <a
            key={link.format}
            className="focus-ring border border-line px-3 py-2 text-sm font-medium text-ink hover:border-accent hover:text-accent"
            href={buildMetadataUrl(datasetId, versionId, link.format)}
          >
            Download {link.label}
          </a>
        ))}
      </div>
    </section>
  );
}
