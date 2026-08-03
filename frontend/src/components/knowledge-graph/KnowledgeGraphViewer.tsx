"use client";

import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import cytoscape from "cytoscape";
import type { Core, ElementDefinition } from "cytoscape";
import fcose from "cytoscape-fcose";
import { getKnowledgeGraphData } from "@/lib/api";
import type { KnowledgeGraphData, KnowledgeGraphNodeType } from "@/lib/types";
import { GraphControls } from "./GraphControls";
import { GraphDetailsPanel } from "./GraphDetailsPanel";
import { GraphErrorState } from "./GraphErrorState";
import { GraphLegend, nodeTypeColors, nodeTypeLabels, nodeTypeShapes } from "./GraphLegend";
import { GraphLoadingState } from "./GraphLoadingState";

const allNodeTypes = Object.keys(nodeTypeLabels) as KnowledgeGraphNodeType[];
const cytoscapeWithExtensions = cytoscape as typeof cytoscape & { resdatahubFcoseRegistered?: boolean };

if (!cytoscapeWithExtensions.resdatahubFcoseRegistered) {
  cytoscape.use(fcose);
  cytoscapeWithExtensions.resdatahubFcoseRegistered = true;
}

export function KnowledgeGraphViewer() {
  const containerRef = useRef<HTMLDivElement | null>(null);
  const cyRef = useRef<Core | null>(null);
  const selectedNodeIdRef = useRef<string | null>(null);
  const [graph, setGraph] = useState<KnowledgeGraphData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedNodeId, setSelectedNodeId] = useState<string | null>(null);
  const [search, setSearch] = useState("");
  const [enabledTypes, setEnabledTypes] = useState<Set<KnowledgeGraphNodeType>>(new Set(allNodeTypes));
  const [showRelationshipLabels, setShowRelationshipLabels] = useState(false);

  const loadGraph = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const nextGraph = await getKnowledgeGraphData();
      setGraph(nextGraph);
      setSelectedNodeId(null);
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Could not load graph data.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadGraph();
  }, [loadGraph]);

  const visibleGraph = useMemo(() => {
    const normalizedSearch = search.trim().toLocaleLowerCase();
    const visibleNodes = (graph?.nodes ?? []).filter((node) => {
      const typeVisible = enabledTypes.has(node.type);
      const searchVisible = !normalizedSearch || node.label.toLocaleLowerCase().includes(normalizedSearch);

      return typeVisible && searchVisible;
    });
    const visibleNodeIds = new Set(visibleNodes.map((node) => node.id));
    const visibleEdges = (graph?.edges ?? []).filter(
      (edge) => visibleNodeIds.has(edge.source) && visibleNodeIds.has(edge.target)
    );

    return { nodes: visibleNodes, edges: visibleEdges };
  }, [enabledTypes, graph, search]);

  const nodesById = useMemo(
    () => new Map(visibleGraph.nodes.map((node) => [node.id, node])),
    [visibleGraph.nodes]
  );
  const selectedNode = selectedNodeId ? nodesById.get(selectedNodeId) ?? null : null;
  const selectedRelationships = useMemo(
    () =>
      selectedNode
        ? visibleGraph.edges.filter((edge) => edge.source === selectedNode.id || edge.target === selectedNode.id)
        : [],
    [selectedNode, visibleGraph.edges]
  );

  const runLayout = useCallback(() => {
    const cy = cyRef.current;

    if (!cy) {
      return;
    }

    cy.layout(buildLayoutOptions()).run();
  }, []);

  const fitGraph = useCallback(() => {
    cyRef.current?.fit(undefined, 40);
  }, []);

  useEffect(() => {
    selectedNodeIdRef.current = selectedNodeId;
    applyFocus(cyRef.current, selectedNodeId);
  }, [selectedNodeId]);

  useEffect(() => {
    if (!containerRef.current || !graph) {
      return;
    }

    const elements: ElementDefinition[] = [
      ...visibleGraph.nodes.map((node) => ({
        data: {
          id: node.id,
          graphLabel: node.graphLabel,
          type: node.type
        }
      })),
      ...visibleGraph.edges.map((edge) => ({
        data: {
          id: edge.id,
          source: edge.source,
          target: edge.target,
          label: edge.label.replaceAll("_", " ")
        }
      }))
    ];

    const cy = cytoscape({
      container: containerRef.current,
      elements,
      style: [
        {
          selector: "node",
          style: {
            "background-color": (element) =>
              nodeTypeColors[element.data("type") as KnowledgeGraphNodeType] ?? nodeTypeColors.dataset,
            "border-color": "#ffffff",
            "border-width": 2,
            color: "#17202a",
            "font-size": 11,
            height: (element) => (element.data("type") === "dataset" ? 48 : 38),
            label: "data(graphLabel)",
            "min-zoomed-font-size": 8,
            "overlay-padding": 6,
            shape: (element) =>
              nodeTypeShapes[element.data("type") as KnowledgeGraphNodeType] ?? nodeTypeShapes.dataset,
            "text-background-color": "#ffffff",
            "text-background-opacity": 0.9,
            "text-background-padding": 3,
            "text-margin-y": 6,
            "text-max-width": 150,
            "text-valign": "bottom",
            "text-wrap": "wrap",
            width: (element) => (element.data("type") === "dataset" ? 74 : 42)
          }
        },
        {
          selector: "node:selected",
          style: {
            "border-color": "#0f766e",
            "border-style": "double",
            "border-width": 5
          }
        },
        {
          selector: "node.focused, node.neighbor",
          style: {
            opacity: 1,
            "text-background-opacity": 1
          }
        },
        {
          selector: "edge.neighbor",
          style: {
            "line-color": "#0f766e",
            opacity: 1,
            "target-arrow-color": "#0f766e",
            width: 2.5
          }
        },
        {
          selector: ".faded",
          style: {
            opacity: 0.18
          }
        },
        {
          selector: "edge",
          style: {
            "curve-style": "bezier",
            "font-size": 9,
            label: showRelationshipLabels ? "data(label)" : "",
            "line-color": "#a8b3c4",
            "text-background-color": "#ffffff",
            "text-background-opacity": 0.95,
            "text-background-padding": 2,
            "text-margin-y": -8,
            "text-rotation": "autorotate",
            "target-arrow-color": "#a8b3c4",
            "target-arrow-shape": "triangle",
            width: 1.5
          }
        }
      ],
      layout: buildLayoutOptions(),
      minZoom: 0.25,
      maxZoom: 2.5,
      wheelSensitivity: 0.25
    });

    cy.on("mouseover", "node", (event) => {
      if (!selectedNodeIdRef.current) {
        applyFocus(cy, event.target.id());
      }
    });
    cy.on("mouseout", "node", () => {
      if (!selectedNodeIdRef.current) {
        applyFocus(cy, null);
      }
    });
    cy.on("tap", "node", (event) => {
      setSelectedNodeId(event.target.id());
    });
    cy.on("dbltap", "node", (event) => {
      const node = visibleGraph.nodes.find((candidate) => candidate.id === event.target.id());

      if (node?.type === "dataset" && node.publicUrl) {
        window.location.href = node.publicUrl;
      }
    });
    cy.on("tap", (event) => {
      if (event.target === cy) {
        setSelectedNodeId(null);
      }
    });

    cyRef.current = cy;
    applyFocus(cy, selectedNodeIdRef.current);

    return () => {
      cy.destroy();
      cyRef.current = null;
    };
  }, [graph, showRelationshipLabels, visibleGraph.edges, visibleGraph.nodes]);

  function toggleType(type: KnowledgeGraphNodeType) {
    setEnabledTypes((current) => {
      const next = new Set(current);

      if (next.has(type)) {
        next.delete(type);
      } else {
        next.add(type);
      }

      return next;
    });
    setSelectedNodeId(null);
  }

  if (loading) {
    return <GraphLoadingState />;
  }

  if (error) {
    return <GraphErrorState message={error} onRetry={loadGraph} />;
  }

  if (!graph || graph.nodes.length === 0) {
    return (
      <div className="border border-line bg-panel px-5 py-4 text-sm text-muted">
        No graph data is available yet. Publish a dataset version to populate the knowledge graph.
      </div>
    );
  }

  return (
    <div className="space-y-5">
      <GraphControls
        search={search}
        enabledTypes={enabledTypes}
        showRelationshipLabels={showRelationshipLabels}
        onSearchChange={setSearch}
        onToggleType={toggleType}
        onShowRelationshipLabelsChange={setShowRelationshipLabels}
        onFit={fitGraph}
        onResetLayout={runLayout}
      />

      {graph.truncated && (
        <div className="border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900">
          Showing a bounded graph view with at most {graph.maxNodes} nodes. Refine the graph with search or type filters.
        </div>
      )}

      <div className="grid gap-5 lg:grid-cols-[1fr_320px]">
        <section className="border border-line bg-white">
          <div className="border-b border-line px-4 py-3 text-sm text-muted">
            {visibleGraph.nodes.length} nodes, {visibleGraph.edges.length} relationships
          </div>
          <div
            ref={containerRef}
            aria-label="Interactive knowledge graph"
            className="h-[560px] min-h-[520px] w-full outline-none md:h-[700px] xl:h-[780px]"
            tabIndex={0}
          />
        </section>

        <div className="space-y-5 lg:hidden">
          <details open>
            <summary className="focus-ring cursor-pointer border border-line bg-white px-4 py-3 text-sm font-medium text-ink">
              Node details
            </summary>
            <GraphDetailsPanel node={selectedNode} relationships={selectedRelationships} nodesById={nodesById} />
          </details>
          <GraphLegend />
        </div>

        <div className="hidden space-y-5 lg:sticky lg:top-5 lg:block lg:self-start">
          <GraphLegend />
          <GraphDetailsPanel node={selectedNode} relationships={selectedRelationships} nodesById={nodesById} />
        </div>
      </div>
    </div>
  );
}

function buildLayoutOptions() {
  return {
    name: "fcose",
    animate: false,
    fit: true,
    padding: 70,
    randomize: false,
    nodeSeparation: 120,
    idealEdgeLength: 180,
    nodeRepulsion: 12000,
    edgeElasticity: 0.35,
    gravity: 0.2,
    gravityRangeCompound: 1.5,
    gravityCompound: 1,
    numIter: 2500
  };
}

function applyFocus(cy: Core | null, nodeId: string | null) {
  if (!cy) {
    return;
  }

  cy.elements().removeClass("focused neighbor faded");

  if (!nodeId) {
    return;
  }

  const node = cy.getElementById(nodeId);

  if (node.empty()) {
    return;
  }

  const neighborhood = node.closedNeighborhood();
  node.addClass("focused");
  neighborhood.addClass("neighbor");
  cy.elements().not(neighborhood).addClass("faded");
}
