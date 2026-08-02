"use client";

import { useEffect, useState } from "react";
import { SparqlEditor } from "@/components/sparql/SparqlEditor";
import { SparqlErrorMessage } from "@/components/sparql/SparqlErrorMessage";
import { SparqlExamples } from "@/components/sparql/SparqlExamples";
import { SparqlResultsTable } from "@/components/sparql/SparqlResultsTable";
import { executeSparqlQuery, getSparqlExamples } from "@/lib/api";
import type { SparqlExampleResponse, SparqlQueryResponse } from "@/lib/types";

export default function SparqlPage() {
  const [examples, setExamples] = useState<SparqlExampleResponse[]>([]);
  const [selectedExample, setSelectedExample] = useState("");
  const [query, setQuery] = useState("");
  const [result, setResult] = useState<SparqlQueryResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [examplesLoading, setExamplesLoading] = useState(true);
  const [queryLoading, setQueryLoading] = useState(false);

  useEffect(() => {
    let active = true;

    async function loadExamples() {
      try {
        const loadedExamples = await getSparqlExamples();

        if (!active) {
          return;
        }

        setExamples(loadedExamples);
        if (loadedExamples.length > 0) {
          setSelectedExample(loadedExamples[0].title);
          setQuery(loadedExamples[0].query);
        }
      } catch (caught) {
        if (active) {
          setError(caught instanceof Error ? caught.message : "Could not load SPARQL examples.");
        }
      } finally {
        if (active) {
          setExamplesLoading(false);
        }
      }
    }

    void loadExamples();

    return () => {
      active = false;
    };
  }, []);

  async function runQuery() {
    setQueryLoading(true);
    setError(null);
    setResult(null);

    try {
      const response = await executeSparqlQuery(query);
      setResult(response);
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Could not run SPARQL query.");
    } finally {
      setQueryLoading(false);
    }
  }

  function clearQuery() {
    setQuery("");
    setSelectedExample("");
    setResult(null);
    setError(null);
  }

  return (
    <div className="mx-auto max-w-6xl px-6 py-10">
      <div className="mb-8">
        <p className="text-sm font-medium text-accent">Knowledge graph</p>
        <h1 className="mt-3 text-3xl font-semibold text-ink">Knowledge Graph Explorer</h1>
        <p className="mt-4 max-w-3xl text-sm leading-6 text-muted">
          ResDataHub publishes dataset metadata as RDF so catalogs and tools can read relationships between datasets,
          creators, organizations, licenses, keywords, and files. Use SPARQL to query that public graph.
        </p>
        <div className="mt-4 border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900">
          Only read-only SPARQL SELECT queries are supported. Update operations, SERVICE clauses, and graph-changing
          query forms are rejected by the backend.
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-[320px_1fr]">
        <SparqlExamples
          examples={examples}
          selectedTitle={selectedExample}
          loading={examplesLoading}
          onSelect={(nextQuery, title) => {
            setSelectedExample(title);
            setQuery(nextQuery);
            setResult(null);
            setError(null);
          }}
        />

        <div className="space-y-5">
          <SparqlEditor
            query={query}
            loading={queryLoading}
            onChange={setQuery}
            onRun={runQuery}
            onClear={clearQuery}
          />

          {queryLoading && (
            <div className="border border-line bg-panel px-4 py-3 text-sm text-muted">Running SPARQL query...</div>
          )}

          {error && <SparqlErrorMessage message={error} />}

          {result && <SparqlResultsTable result={result} />}
        </div>
      </div>
    </div>
  );
}
