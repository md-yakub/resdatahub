import type cytoscape from "cytoscape";
import type { KnowledgeGraphNodeType } from "@/lib/types";

export const nodeTypeLabels: Record<KnowledgeGraphNodeType, string> = {
  dataset: "Dataset",
  organization: "Organization",
  creator: "Creator",
  keyword: "Keyword",
  license: "License",
  file: "File"
};

export const nodeTypeColors: Record<KnowledgeGraphNodeType, string> = {
  dataset: "#0f766e",
  organization: "#334155",
  creator: "#7c3aed",
  keyword: "#b45309",
  license: "#0369a1",
  file: "#4b5563"
};

export const nodeTypeShapes: Record<KnowledgeGraphNodeType, cytoscape.Css.NodeShape> = {
  dataset: "roundrectangle",
  organization: "hexagon",
  creator: "ellipse",
  keyword: "diamond",
  license: "roundrectangle",
  file: "rectangle"
};

const nodeTypes = Object.keys(nodeTypeLabels) as KnowledgeGraphNodeType[];

export function GraphLegend() {
  return (
    <div className="border border-line bg-white p-4">
      <h2 className="text-sm font-semibold text-ink">Legend</h2>
      <div className="mt-3 grid gap-2 sm:grid-cols-2 lg:grid-cols-1">
        {nodeTypes.map((type) => (
          <div key={type} className="flex items-center gap-2 text-sm text-muted">
            <span
              className="h-3 w-3 border border-white shadow-sm"
              style={{ backgroundColor: nodeTypeColors[type] }}
              aria-hidden="true"
            />
            <span>
              {nodeTypeLabels[type]} <span className="text-xs">({shapeLabel(nodeTypeShapes[type])})</span>
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}

function shapeLabel(shape: string) {
  return shape.replace("-", " ");
}
