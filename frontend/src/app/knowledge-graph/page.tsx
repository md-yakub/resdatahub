import { KnowledgeGraphViewer } from "@/components/knowledge-graph/KnowledgeGraphViewer";

export default function KnowledgeGraphPage() {
  return (
    <div className="mx-auto max-w-6xl px-6 py-10">
      <div className="mb-8">
        <p className="text-sm font-medium text-accent">Knowledge graph</p>
        <h1 className="mt-3 text-3xl font-semibold text-ink">ResDataHub Knowledge Graph</h1>
        <p className="mt-4 max-w-3xl text-sm leading-6 text-muted">
          Explore relationships between published datasets, organizations, creators, keywords, licenses, and files.
          The graph is built from the public RDF metadata through read-only SPARQL SELECT queries.
        </p>
        <div className="mt-4 border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900">
          The public graph API accepts only read-only SELECT queries. Graph updates and external SERVICE calls are not
          supported.
        </div>
      </div>

      <KnowledgeGraphViewer />
    </div>
  );
}
