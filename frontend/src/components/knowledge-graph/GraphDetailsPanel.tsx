import Link from "next/link";
import type { KnowledgeGraphEdge, KnowledgeGraphNode, KnowledgeGraphNodeType } from "@/lib/types";
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

  const grouped = groupConnectedNodes(node, relationships, nodesById);

  return (
    <aside className="border border-line bg-white p-5">
      <p className="text-xs font-semibold uppercase tracking-normal text-accent">{nodeTypeLabels[node.type]}</p>
      <h2 className="mt-2 text-lg font-semibold leading-6 text-ink">{node.label}</h2>

      {node.type === "dataset" && (
        <>
          <DetailValue label="Version" value={node.details.version} />
          <DetailList label="Publisher" items={grouped.organization} />
          <DetailList label="Creators" items={grouped.creator} />
          <DetailList label="Keywords" items={grouped.keyword} />
          <DetailList label="License" items={grouped.license} />
          <DetailList label="Files" items={grouped.file} />
        </>
      )}

      {node.type === "creator" && (
        <>
          <DetailValue label="Full name" value={node.label} />
          <DetailValue label="Affiliation" value={node.details.affiliation} />
          <DetailValue label="ORCID" value={node.details.orcid} link />
          <DetailList label="Connected datasets" items={grouped.dataset} />
        </>
      )}

      {node.type === "organization" && (
        <>
          <DetailValue label="Name" value={node.label} />
          <DetailValue label="Short name" value={node.details.shortName} />
          <DetailValue label="Homepage" value={node.details.homepage} link />
          <DetailList label="Connected datasets" items={grouped.dataset} />
        </>
      )}

      {node.type === "keyword" && (
        <>
          <DetailValue label="Keyword" value={node.label} />
          <DetailList label="Connected datasets" items={grouped.dataset} />
        </>
      )}

      {node.type === "license" && (
        <>
          <DetailValue label="License" value={node.label} />
          <DetailList label="Connected datasets" items={grouped.dataset} />
        </>
      )}

      {node.type === "file" && (
        <>
          <DetailValue label="Filename" value={node.label} />
          <DetailValue label="Category" value={node.details.category} />
          <DetailValue label="Content type" value={node.details.contentType} />
          <DetailValue label="File size" value={formatBytes(node.details.fileSize)} />
          <DetailValue label="SHA-256" value={node.details.sha256} />
          <DetailValue label="Download URL" value={node.details.downloadUrl} link />
          <DetailList label="Connected datasets" items={grouped.dataset} />
        </>
      )}

      {node.uri && <DetailValue label="URI" value={node.uri} link />}

      {node.publicUrl && (
        <Link
          href={node.publicUrl}
          className="focus-ring mt-4 inline-flex border border-accent bg-accent px-3 py-2 text-sm font-medium text-white"
          aria-label={`Open dataset page for ${node.label}`}
        >
          Open Dataset
        </Link>
      )}

      <div className="mt-6">
        <h3 className="text-sm font-semibold text-ink">Visible relationships</h3>
        {relationships.length === 0 ? (
          <p className="mt-2 text-sm text-muted">No visible relationships for the current filters.</p>
        ) : (
          <ul className="mt-3 space-y-3">
            {relationships.map((edge) => {
              const other = otherNode(node, edge, nodesById);

              return (
                <li key={edge.id} className="border border-line bg-panel px-3 py-2 text-sm">
                  <p className="font-medium text-ink">{edge.label}</p>
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

function groupConnectedNodes(
  node: KnowledgeGraphNode,
  relationships: KnowledgeGraphEdge[],
  nodesById: Map<string, KnowledgeGraphNode>
) {
  const grouped: Record<KnowledgeGraphNodeType, KnowledgeGraphNode[]> = {
    dataset: [],
    organization: [],
    creator: [],
    keyword: [],
    license: [],
    file: []
  };

  relationships.forEach((edge) => {
    const connected = otherNode(node, edge, nodesById);

    if (connected) {
      grouped[connected.type].push(connected);
    }
  });

  return grouped;
}

function otherNode(node: KnowledgeGraphNode, edge: KnowledgeGraphEdge, nodesById: Map<string, KnowledgeGraphNode>) {
  return nodesById.get(edge.source === node.id ? edge.target : edge.source);
}

function DetailValue({
  label,
  value,
  link = false
}: {
  label: string;
  value: string | string[] | null | undefined;
  link?: boolean;
}) {
  if (value === undefined || value === null || value === "" || (Array.isArray(value) && value.length === 0)) {
    return null;
  }

  const displayValue = Array.isArray(value) ? value.join(", ") : value;

  return (
    <div className="mt-4">
      <p className="text-xs font-medium uppercase tracking-normal text-muted">{label}</p>
      {link && isHttpUrl(displayValue) ? (
        <a href={displayValue} className="mt-1 block break-all text-sm text-accent hover:underline">
          {displayValue}
        </a>
      ) : (
        <p className="mt-1 break-all text-sm text-ink">{displayValue}</p>
      )}
    </div>
  );
}

function DetailList({ label, items }: { label: string; items: KnowledgeGraphNode[] }) {
  if (items.length === 0) {
    return null;
  }

  return (
    <div className="mt-4">
      <p className="text-xs font-medium uppercase tracking-normal text-muted">{label}</p>
      <ul className="mt-2 space-y-1 text-sm text-ink">
        {items.map((item) => (
          <li key={item.id} className="break-words">
            {item.publicUrl ? (
              <Link href={item.publicUrl} className="text-accent hover:underline">
                {item.label}
              </Link>
            ) : (
              item.label
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}

function formatBytes(value: string | string[] | null | undefined) {
  const rawValue = Array.isArray(value) ? value[0] : value;
  const numericValue = rawValue ? Number(rawValue) : Number.NaN;

  if (!Number.isFinite(numericValue)) {
    return rawValue;
  }

  if (numericValue < 1024) {
    return `${numericValue} B`;
  }

  if (numericValue < 1024 * 1024) {
    return `${(numericValue / 1024).toFixed(1)} KB`;
  }

  return `${(numericValue / (1024 * 1024)).toFixed(1)} MB`;
}

function isHttpUrl(value: string) {
  return value.startsWith("http://") || value.startsWith("https://");
}
