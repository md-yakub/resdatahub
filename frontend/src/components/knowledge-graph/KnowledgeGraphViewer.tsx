"use client";

import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import cytoscape from "cytoscape";
import type { Core, ElementDefinition } from "cytoscape";
import { getKnowledgeGraphData } from "@/lib/api";
import type { KnowledgeGraphData, KnowledgeGraphNodeType } from "@/lib/types";
import { GraphControls } from "./GraphControls";
import { GraphDetailsPanel } from "./GraphDetailsPanel";
import { GraphErrorState } from "./GraphErrorState";
import { GraphLegend, nodeTypeColors, nodeTypeLabels } from "./GraphLegend";
import { GraphLoadingState } from "./GraphLoadingState";

const allNodeTypes = Object.keys(nodeTypeLabels) as KnowledgeGraphNodeType[];

export function KnowledgeGraphViewer() {
  const containerRef = useRef<HTMLDivElement | null>(null);
  const cyRef = useRef<Core | null>(null);
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

    cy.layout({
      name: "cose",
      animate: false,
      fit: true,
      padding: 40,
      nodeRepulsion: 9000,
      idealEdgeLength: 120
    }).run();
  }, []);

  const fitGraph = useCallback(() => {
    cyRef.current?.fit(undefined, 40);
  }, []);

  useEffect(() => {
    if (!containerRef.current || !graph) {
      return;
    }

    const elements: ElementDefinition[] = [
      ...visibleGraph.nodes.map((node) => ({
        data: {
          id: node.id,
          label: node.label,
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
            color: "#17202a",
            "font-size": 10,
            height: 34,
            label: "data(label)",
            "min-zoomed-font-size": 8,
            "overlay-padding": 6,
            shape: "ellipse",
            "text-background-color": "#ffffff",
            "text-background-opacity": 0.85,
            "text-background-padding": 2,
            "text-max-width": 120,
            "text-valign": "bottom",
            "text-wrap": "wrap",
            width: 34
          }
        },
        {
          selector: "node:selected",
          style: {
            "border-color": "#0f766e",
            "border-width": 4
          }
        },
        {
          selector: "edge",
          style: {
            "curve-style": "bezier",
            "font-size": 9,
            label: showRelationshipLabels ? "data(label)" : "",
            "line-color": "#a8b3c4",
            "target-arrow-color": "#a8b3c4",
            "target-arrow-shape": "triangle",
            width: 1.5
          }
        }
      ],
      layout: {
        name: "cose",
        animate: false,
        fit: true,
        padding: 40,
        nodeRepulsion: 9000,
        idealEdgeLength: 120
      },
      minZoom: 0.25,
      maxZoom: 2.5,
      wheelSensitivity: 0.25
    });

    cy.on("tap", "node", (event) => {
      setSelectedNodeId(event.target.id());
    });
    cy.on("tap", (event) => {
      if (event.target === cy) {
        setSelectedNodeId(null);
      }
    });

    cyRef.current = cy;

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
            className="h-[520px] w-full outline-none md:h-[640px]"
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

        <div className="hidden space-y-5 lg:block">
          <GraphLegend />
          <GraphDetailsPanel node={selectedNode} relationships={selectedRelationships} nodesById={nodesById} />
        </div>
      </div>
    </div>
  );
}
