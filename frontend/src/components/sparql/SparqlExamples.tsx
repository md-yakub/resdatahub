"use client";

import type { SparqlExampleResponse } from "@/lib/types";

interface SparqlExamplesProps {
  examples: SparqlExampleResponse[];
  selectedTitle: string;
  loading: boolean;
  onSelect: (query: string, title: string) => void;
}

const helpItems = [
  "All datasets and titles",
  "Datasets by keyword",
  "Datasets and publishers",
  "Datasets and creators",
  "Datasets and files"
];

export function SparqlExamples({ examples, selectedTitle, loading, onSelect }: SparqlExamplesProps) {
  return (
    <aside className="border border-line bg-panel p-5">
      <h2 className="text-lg font-semibold text-ink">Query Help</h2>
      <p className="mt-2 text-sm leading-6 text-muted">
        Start from a sample SELECT query, then adjust variables, filters, or limits.
      </p>

      <label className="mt-5 block">
        <span className="text-sm font-medium text-ink">Example query</span>
        <select
          value={selectedTitle}
          disabled={loading || examples.length === 0}
          onChange={(event) => {
            const selected = examples.find((example) => example.title === event.target.value);
            if (selected) {
              onSelect(selected.query, selected.title);
            }
          }}
          className="focus-ring mt-2 w-full border border-line bg-white px-3 py-2 text-sm text-ink disabled:bg-panel"
        >
          <option value="">{loading ? "Loading examples..." : "Select an example"}</option>
          {examples.map((example) => (
            <option key={example.title} value={example.title}>
              {example.title}
            </option>
          ))}
        </select>
      </label>

      <div className="mt-6">
        <h3 className="text-sm font-medium text-ink">Available examples</h3>
        <ul className="mt-3 space-y-2 text-sm text-muted">
          {helpItems.map((item) => (
            <li key={item}>{item}</li>
          ))}
        </ul>
      </div>
    </aside>
  );
}
