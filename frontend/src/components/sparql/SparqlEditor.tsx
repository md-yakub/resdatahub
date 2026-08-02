"use client";

import type { FormEvent } from "react";

interface SparqlEditorProps {
  query: string;
  loading: boolean;
  onChange: (query: string) => void;
  onRun: () => void;
  onClear: () => void;
}

export function SparqlEditor({ query, loading, onChange, onRun, onClear }: SparqlEditorProps) {
  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    onRun();
  }

  return (
    <form onSubmit={submit} className="border border-line bg-white p-5">
      <label htmlFor="sparql-query" className="text-sm font-medium text-ink">
        SPARQL SELECT query
      </label>
      <textarea
        id="sparql-query"
        value={query}
        onChange={(event) => onChange(event.target.value)}
        rows={16}
        spellCheck={false}
        className="focus-ring mt-3 w-full border border-line bg-panel px-3 py-3 font-mono text-sm leading-6 text-ink"
      />
      <div className="mt-4 flex flex-wrap gap-3">
        <button type="submit" disabled={loading || !query.trim()} className="focus-ring bg-accent px-4 py-2 text-sm font-medium text-white disabled:opacity-60">
          {loading ? "Running..." : "Run Query"}
        </button>
        <button type="button" onClick={onClear} disabled={loading} className="focus-ring border border-line px-4 py-2 text-sm font-medium text-muted hover:text-ink disabled:opacity-60">
          Clear
        </button>
      </div>
    </form>
  );
}
