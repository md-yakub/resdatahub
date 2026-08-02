"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { ConfirmationDialog } from "./ConfirmationDialog";
import { FormFieldError } from "./FormFieldError";
import { DraftStatusBadge } from "./DraftStatusBadge";
import { formatBytes } from "@/lib/format";
import type {
  DatasetCreatorResponse,
  DatasetFileResponse,
  DatasetKeywordResponse,
  DatasetResponse,
  DatasetVersionResponse
} from "@/lib/types";

interface ReviewPublishStepProps {
  dataset: DatasetResponse | null;
  version: DatasetVersionResponse | null;
  creators: DatasetCreatorResponse[];
  keywords: DatasetKeywordResponse[];
  files: DatasetFileResponse[];
  busy: boolean;
  error?: string | null;
  onPublish: () => Promise<DatasetVersionResponse>;
}

export function ReviewPublishStep({
  dataset,
  version,
  creators,
  keywords,
  files,
  busy,
  error,
  onPublish
}: ReviewPublishStepProps) {
  const router = useRouter();
  const [confirmOpen, setConfirmOpen] = useState(false);
  const readOnly = version?.status !== "DRAFT";
  const checks = [
    { label: "Title", ready: Boolean(version?.title.trim()) },
    { label: "Description", ready: Boolean(version?.description.trim()) },
    { label: "At least one creator", ready: creators.length > 0 },
    { label: "Selected license", ready: Boolean(version?.license) },
    { label: "At least one file", ready: files.length > 0 }
  ];

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between gap-4">
        <div>
          <h2 className="text-xl font-semibold text-ink">{version?.title ?? "Untitled dataset"}</h2>
          <p className="mt-2 whitespace-pre-wrap text-sm leading-6 text-muted">{version?.description ?? ""}</p>
        </div>
        <DraftStatusBadge status={version?.status} />
      </div>

      <dl className="grid gap-4 text-sm md:grid-cols-3">
        <div>
          <dt className="font-medium text-ink">Organization</dt>
          <dd className="mt-1 text-muted">{dataset?.organization.name ?? "Not selected"}</dd>
        </div>
        <div>
          <dt className="font-medium text-ink">Version</dt>
          <dd className="mt-1 text-muted">{version?.versionNumber ?? "Not created"}</dd>
        </div>
        <div>
          <dt className="font-medium text-ink">License</dt>
          <dd className="mt-1 text-muted">{version?.license?.code ?? "Not selected"}</dd>
        </div>
      </dl>

      <div className="grid gap-5 md:grid-cols-2">
        <section>
          <h3 className="text-sm font-medium text-ink">Creators</h3>
          <ol className="mt-3 space-y-2 text-sm text-muted">
            {creators.map((creator) => (
              <li key={creator.id}>
                {creator.position}. {creator.givenName} {creator.familyName}
              </li>
            ))}
          </ol>
        </section>
        <section>
          <h3 className="text-sm font-medium text-ink">Keywords</h3>
          <div className="mt-3 flex flex-wrap gap-2">
            {keywords.map((keyword) => (
              <span key={keyword.id} className="border border-line bg-panel px-2 py-1 text-xs text-muted">
                {keyword.value}
              </span>
            ))}
          </div>
        </section>
      </div>

      <section>
        <h3 className="text-sm font-medium text-ink">Files</h3>
        <div className="mt-3 space-y-2">
          {files.map((file) => (
            <p key={file.id} className="text-sm text-muted">
              {file.originalFilename} - {file.category} - {formatBytes(file.fileSize)}
            </p>
          ))}
        </div>
      </section>

      <section className="border border-line bg-panel p-4">
        <h3 className="text-sm font-medium text-ink">Validation checklist</h3>
        <ul className="mt-3 grid gap-2 text-sm md:grid-cols-2">
          {checks.map((check) => (
            <li key={check.label} className={check.ready ? "text-emerald-800" : "text-red-700"}>
              {check.ready ? "Ready" : "Missing"}: {check.label}
            </li>
          ))}
        </ul>
      </section>

      <FormFieldError message={error} />

      {!readOnly && (
        <button type="button" onClick={() => setConfirmOpen(true)} disabled={busy || !version} className="bg-ink px-4 py-2 text-sm font-medium text-white disabled:opacity-60">
          Publish
        </button>
      )}

      {readOnly && version?.status === "PUBLISHED" && dataset && (
        <button
          type="button"
          onClick={() => router.push(`/datasets/${dataset.id}/versions/${version.id}`)}
          className="bg-ink px-4 py-2 text-sm font-medium text-white"
        >
          Open public page
        </button>
      )}

      <ConfirmationDialog
        open={confirmOpen}
        title="Publish dataset version"
        message="Publishing makes this dataset version immutable and publicly visible."
        confirmLabel="Publish"
        busy={busy}
        onCancel={() => setConfirmOpen(false)}
        onConfirm={() => {
          void (async () => {
            try {
              const published = await onPublish();
              setConfirmOpen(false);
              if (dataset) {
                router.push(`/datasets/${dataset.id}/versions/${published.id}`);
              }
            } catch {
              setConfirmOpen(false);
            }
          })();
        }}
      />
    </div>
  );
}
