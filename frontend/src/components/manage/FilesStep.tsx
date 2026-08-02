"use client";

import type { FormEvent } from "react";
import { useState } from "react";
import { ConfirmationDialog } from "./ConfirmationDialog";
import { FormFieldError } from "./FormFieldError";
import { formatBytes, formatDate } from "@/lib/format";
import type { DatasetFileCategory, DatasetFileResponse } from "@/lib/types";

const categories: DatasetFileCategory[] = ["RAW", "PROCESSED", "DOCUMENTATION", "SUPPLEMENTARY"];

interface FilesStepProps {
  files: DatasetFileResponse[];
  readOnly: boolean;
  busy: boolean;
  error?: string | null;
  onUpload: (file: File, category: DatasetFileCategory) => Promise<void>;
  onDelete: (fileId: string) => Promise<void>;
}

export function FilesStep({ files, readOnly, busy, error, onUpload, onDelete }: FilesStepProps) {
  const [file, setFile] = useState<File | null>(null);
  const [category, setCategory] = useState<DatasetFileCategory>("RAW");
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [fieldError, setFieldError] = useState<string | null>(null);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!file) {
      setFieldError("Choose a file to upload.");
      return;
    }

    setFieldError(null);
    await onUpload(file, category);
    setFile(null);
    event.currentTarget.reset();
  }

  return (
    <div className="space-y-6">
      <div className="space-y-3">
        {files.length === 0 && <p className="text-sm text-muted">No files uploaded yet.</p>}
        {files.map((item) => (
          <div key={item.id} className="flex flex-col gap-3 border border-line bg-panel p-4 md:flex-row md:items-center md:justify-between">
            <div>
              <p className="text-sm font-medium text-ink">{item.originalFilename}</p>
              <p className="mt-1 text-xs text-muted">
                {item.category} / {item.contentType} / {formatBytes(item.fileSize)} / {formatDate(item.createdAt)}
              </p>
              <p className="mt-1 break-all text-xs text-muted">SHA-256: {item.sha256}</p>
            </div>
            {!readOnly && (
              <button type="button" onClick={() => setDeleteId(item.id)} className="border border-line px-3 py-2 text-sm text-red-700" disabled={busy}>
                Delete
              </button>
            )}
          </div>
        ))}
      </div>

      {!readOnly && (
        <form onSubmit={handleSubmit} className="grid gap-4 border border-line bg-white p-4 md:grid-cols-[1fr_220px_auto]">
          <input type="file" onChange={(event) => setFile(event.target.files?.[0] ?? null)} disabled={busy} className="text-sm text-muted" />
          <select value={category} onChange={(event) => setCategory(event.target.value as DatasetFileCategory)} disabled={busy} className="border border-line bg-white px-3 py-2 text-sm">
            {categories.map((item) => (
              <option key={item} value={item}>
                {item}
              </option>
            ))}
          </select>
          <button type="submit" disabled={busy} className="bg-ink px-4 py-2 text-sm font-medium text-white disabled:opacity-60">
            Upload file
          </button>
          <div className="md:col-span-3">
            <FormFieldError message={fieldError ?? error} />
          </div>
        </form>
      )}

      <ConfirmationDialog
        open={Boolean(deleteId)}
        title="Delete file"
        message="This removes the file metadata and stored object from the draft version."
        confirmLabel="Delete"
        busy={busy}
        onCancel={() => setDeleteId(null)}
        onConfirm={async () => {
          if (deleteId) {
            await onDelete(deleteId);
            setDeleteId(null);
          }
        }}
      />
    </div>
  );
}
