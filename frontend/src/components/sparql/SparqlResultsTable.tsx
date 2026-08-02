"use client";

import type { SparqlQueryResponse } from "@/lib/types";

interface SparqlResultsTableProps {
  result: SparqlQueryResponse;
}

export function SparqlResultsTable({ result }: SparqlResultsTableProps) {
  if (result.rows.length === 0) {
    return (
      <div className="border border-dashed border-line bg-panel px-6 py-10 text-center">
        <h2 className="text-base font-semibold text-ink">No results</h2>
        <p className="mt-2 text-sm text-muted">The query ran successfully but returned no rows.</p>
      </div>
    );
  }

  return (
    <section className="border border-line bg-white">
      <div className="flex flex-col gap-2 border-b border-line px-5 py-4 md:flex-row md:items-center md:justify-between">
        <div className="text-sm text-muted">
          {result.rowCount} {result.rowCount === 1 ? "row" : "rows"}
        </div>
        {result.truncated && (
          <div className="border border-amber-200 bg-amber-50 px-3 py-1 text-sm text-amber-900">
            Results were truncated to the maximum allowed rows.
          </div>
        )}
      </div>

      <div className="overflow-x-auto">
        <table className="min-w-full border-collapse text-left text-sm">
          <thead className="bg-panel text-ink">
            <tr>
              {result.variables.map((variable) => (
                <th key={variable} className="border-b border-line px-4 py-3 font-medium">
                  {variable}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {result.rows.map((row, index) => (
              <tr key={index} className="border-b border-line last:border-b-0">
                {result.variables.map((variable) => (
                  <td key={variable} className="max-w-md break-words px-4 py-3 align-top text-muted">
                    <ResultValue value={row[variable] ?? null} />
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}

function ResultValue({ value }: { value: string | null }) {
  if (value === null) {
    return <span className="text-muted">null</span>;
  }

  if (isHttpUrl(value)) {
    return (
      <a href={value} target="_blank" rel="noreferrer" className="text-accent hover:underline">
        {value}
      </a>
    );
  }

  return <span>{value}</span>;
}

function isHttpUrl(value: string) {
  try {
    const url = new URL(value);
    return url.protocol === "http:" || url.protocol === "https:";
  } catch {
    return false;
  }
}
