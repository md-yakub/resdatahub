"use client";

import type { FormEvent } from "react";
import { useState } from "react";
import { ConfirmationDialog } from "./ConfirmationDialog";
import { FormFieldError } from "./FormFieldError";
import type { DatasetKeywordResponse, LicenseResponse } from "@/lib/types";

interface DiscoveryRightsStepProps {
  keywords: DatasetKeywordResponse[];
  licenses: LicenseResponse[];
  selectedLicenseId?: string | null;
  readOnly: boolean;
  busy: boolean;
  error?: string | null;
  onAddKeyword: (value: string) => Promise<void>;
  onDeleteKeyword: (keywordId: string) => Promise<void>;
  onChangeLicense: (licenseId: string) => Promise<void>;
}

export function DiscoveryRightsStep({
  keywords,
  licenses,
  selectedLicenseId,
  readOnly,
  busy,
  error,
  onAddKeyword,
  onDeleteKeyword,
  onChangeLicense
}: DiscoveryRightsStepProps) {
  const [keyword, setKeyword] = useState("");
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [fieldError, setFieldError] = useState<string | null>(null);

  async function handleKeywordSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const value = keyword.trim();

    if (!value) {
      setFieldError("Keyword value is required.");
      return;
    }

    setFieldError(null);
    await onAddKeyword(value);
    setKeyword("");
  }

  return (
    <div className="space-y-6">
      <div>
        <label htmlFor="licenseId" className="text-sm font-medium text-ink">
          License
        </label>
        <select
          id="licenseId"
          value={selectedLicenseId ?? ""}
          onChange={(event) => onChangeLicense(event.target.value)}
          disabled={readOnly || busy}
          className="mt-2 w-full border border-line bg-white px-3 py-2 text-sm text-ink disabled:bg-panel"
        >
          <option value="">Select license</option>
          {licenses
            .filter((license) => license.active)
            .map((license) => (
              <option key={license.id} value={license.id}>
                {license.code} - {license.name}
              </option>
            ))}
        </select>
      </div>

      <div>
        <h3 className="text-sm font-medium text-ink">Keywords</h3>
        <div className="mt-3 flex flex-wrap gap-2">
          {keywords.length === 0 && <p className="text-sm text-muted">No keywords added yet.</p>}
          {keywords.map((item) => (
            <span key={item.id} className="inline-flex items-center gap-2 border border-line bg-panel px-3 py-1 text-sm text-muted">
              {item.value}
              {!readOnly && (
                <button type="button" onClick={() => setDeleteId(item.id)} disabled={busy} className="text-red-700">
                  Remove
                </button>
              )}
            </span>
          ))}
        </div>
      </div>

      {!readOnly && (
        <form onSubmit={handleKeywordSubmit} className="flex flex-col gap-3 sm:flex-row">
          <input
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
            placeholder="Add keyword"
            disabled={busy}
            className="flex-1 border border-line px-3 py-2 text-sm"
          />
          <button type="submit" disabled={busy} className="bg-ink px-4 py-2 text-sm font-medium text-white disabled:opacity-60">
            Add keyword
          </button>
        </form>
      )}

      <FormFieldError message={fieldError ?? error} />

      <ConfirmationDialog
        open={Boolean(deleteId)}
        title="Delete keyword"
        message="This removes the keyword from the draft version."
        confirmLabel="Delete"
        busy={busy}
        onCancel={() => setDeleteId(null)}
        onConfirm={async () => {
          if (deleteId) {
            await onDeleteKeyword(deleteId);
            setDeleteId(null);
          }
        }}
      />
    </div>
  );
}
