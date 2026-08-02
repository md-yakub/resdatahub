"use client";

import { useEffect, useState } from "react";
import { getCitation } from "@/lib/api";
import type { CitationFormat } from "@/lib/types";

const formats: CitationFormat[] = ["APA", "TEXT", "BIBTEX", "RIS"];

export function CitationPanel({ datasetId, versionId }: { datasetId: string; versionId: string }) {
  const [format, setFormat] = useState<CitationFormat>("APA");
  const [citation, setCitation] = useState("");
  const [status, setStatus] = useState("Loading citation");

  useEffect(() => {
    let active = true;
    setStatus("Loading citation");

    getCitation(datasetId, versionId, format)
      .then((response) => {
        if (active) {
          setCitation(response.citation);
          setStatus("");
        }
      })
      .catch((error: unknown) => {
        if (process.env.NODE_ENV === "development") {
          console.error(
            `Citation request failed for /api/public/datasets/${datasetId}/versions/${versionId}/citation?format=${format}`,
            error
          );
        }

        if (active) {
          setCitation("");
          setStatus("Could not load citation");
        }
      });

    return () => {
      active = false;
    };
  }, [datasetId, versionId, format]);

  async function copyCitation() {
    await navigator.clipboard.writeText(citation);
    setStatus("Citation copied");
  }

  return (
    <section className="border border-line bg-white p-5">
      <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <h2 className="text-lg font-semibold text-ink">Citation</h2>
        <select
          className="focus-ring border border-line bg-white px-3 py-2 text-sm"
          value={format}
          onChange={(event) => setFormat(event.target.value as CitationFormat)}
        >
          {formats.map((item) => (
            <option key={item} value={item}>
              {item}
            </option>
          ))}
        </select>
      </div>
      <pre className="mt-4 whitespace-pre-wrap border border-line bg-panel p-4 text-sm leading-6 text-ink">
        {citation || status}
      </pre>
      <button
        className="focus-ring mt-4 border border-line px-3 py-2 text-sm font-medium text-ink disabled:opacity-40"
        disabled={!citation}
        onClick={copyCitation}
        type="button"
      >
        Copy citation
      </button>
      {status && citation && <p className="mt-2 text-sm text-muted">{status}</p>}
    </section>
  );
}
