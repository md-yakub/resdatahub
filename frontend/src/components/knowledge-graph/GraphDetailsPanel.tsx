import Link from "next/link";
import type { KnowledgeGraphEdge, KnowledgeGraphNode } from "@/lib/types";
import { nodeTypeLabels } from "./GraphLegend";

interface GraphDetailsPanelProps {
  node: KnowledgeGraphNode | null;
  relationships: KnowledgeGraphEdge[];
  nodesById: Map<string, KnowledgeGraphNode>;
}

export function GraphDetailsPanel({ node, relationships, nodesById }: GraphDetailsPanelProps) {
  if (!node) {
    return (
      <aside className="border border-line bg-white p-5">
        <h2 className="text-base font-semibold text-ink">Selection</h2>
        <p className="mt-2 text-sm leading-6 text-muted">Select a node to inspect its URI and relationships.</p>
      </aside>
    );
  }

  return (
    <aside className="border border-line bg-white p-5">
      <p className="text-xs font-semibold uppercase tracking-normal text-accent">{nodeTypeLabels[node.type]}</p>
      <h2 className="mt-2 text-lg font-semibold text-ink">{node.label}</h2>

      {node.uri && (
        <div className="mt-4">
          <p className="text-xs font-medium uppercase tracking-normal text-muted">URI</p>
          {isHttpUrl(node.uri) ? (
            <a href={node.uri} className="mt-1 block break-all text-sm text-accent hover:underline">
              {node.uri}
            </a>
          ) : (
            <p className="mt-1 break-all text-sm text-muted">{node.uri}</p>
          )}
        </div>
      )}

      {node.publicUrl && (
        <Link
          href={node.publicUrl}
          className="focus-ring mt-4 inline-flex border border-accent bg-accent px-3 py-2 text-sm font-medium text-white"
        >
          Open dataset page
        </Link>
      )}

      <div className="mt-6">
        <h3 className="text-sm font-semibold text-ink">Connected relationships</h3>
        {relationships.length === 0 ? (
          <p className="mt-2 text-sm text-muted">No visible relationships for the current filters.</p>
        ) : (
          <ul className="mt-3 space-y-3">
            {relationships.map((edge) => {
              const source = nodesById.get(edge.source);
              const target = nodesById.get(edge.target);
              const other = edge.source === node.id ? target : source;

              return (
                <li key={edge.id} className="border border-line bg-panel px-3 py-2 text-sm">
                  <p className="font-medium text-ink">{edge.label.replaceAll("_", " ")}</p>
                  <p className="mt-1 text-muted">{other?.label ?? "Unknown resource"}</p>
                </li>
              );
            })}
          </ul>
        )}
      </div>
    </aside>
  );
}

function isHttpUrl(value: string) {
  return value.startsWith("http://") || value.startsWith("https://");
}
