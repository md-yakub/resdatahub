import type { KnowledgeGraphNodeType } from "@/lib/types";
import { nodeTypeLabels } from "./GraphLegend";

const nodeTypes = Object.keys(nodeTypeLabels) as KnowledgeGraphNodeType[];

interface GraphControlsProps {
  search: string;
  enabledTypes: Set<KnowledgeGraphNodeType>;
  showRelationshipLabels: boolean;
  onSearchChange: (value: string) => void;
  onToggleType: (type: KnowledgeGraphNodeType) => void;
  onShowRelationshipLabelsChange: (value: boolean) => void;
  onFit: () => void;
  onResetLayout: () => void;
}

export function GraphControls({
  search,
  enabledTypes,
  showRelationshipLabels,
  onSearchChange,
  onToggleType,
  onShowRelationshipLabelsChange,
  onFit,
  onResetLayout
}: GraphControlsProps) {
  return (
    <div className="border border-line bg-white p-4">
      <div className="grid gap-4 lg:grid-cols-[1fr_auto_auto] lg:items-end">
        <label className="block">
          <span className="text-sm font-medium text-ink">Search nodes</span>
          <input
            value={search}
            onChange={(event) => onSearchChange(event.target.value)}
            placeholder="Filter by label"
            className="focus-ring mt-2 w-full border border-line px-3 py-2 text-sm text-ink"
          />
        </label>

        <button
          type="button"
          onClick={onFit}
          className="focus-ring border border-line bg-white px-4 py-2 text-sm font-medium text-ink hover:bg-panel"
        >
          Fit graph
        </button>
        <button
          type="button"
          onClick={onResetLayout}
          className="focus-ring border border-line bg-white px-4 py-2 text-sm font-medium text-ink hover:bg-panel"
        >
          Reset layout
        </button>
      </div>

      <fieldset className="mt-4">
        <legend className="text-sm font-medium text-ink">Node types</legend>
        <div className="mt-2 flex flex-wrap gap-3">
          {nodeTypes.map((type) => (
            <label key={type} className="flex items-center gap-2 text-sm text-muted">
              <input
                type="checkbox"
                checked={enabledTypes.has(type)}
                onChange={() => onToggleType(type)}
                className="focus-ring h-4 w-4"
              />
              {nodeTypeLabels[type]}
            </label>
          ))}
        </div>
      </fieldset>

      <label className="mt-4 flex items-center gap-2 text-sm text-muted">
        <input
          type="checkbox"
          checked={showRelationshipLabels}
          onChange={(event) => onShowRelationshipLabelsChange(event.target.checked)}
          className="focus-ring h-4 w-4"
        />
        Show relationship labels
      </label>
    </div>
  );
}
