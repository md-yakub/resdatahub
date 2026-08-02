"use client";

import type { FormEvent } from "react";
import { useState } from "react";
import { FormFieldError } from "./FormFieldError";
import type { CreateDatasetRequest, DatasetResponse, OrganizationResponse } from "@/lib/types";

interface BasicMetadataStepProps {
  organizations: OrganizationResponse[];
  dataset?: DatasetResponse | null;
  title: string;
  description: string;
  organizationId: string;
  readOnly: boolean;
  busy: boolean;
  error?: string | null;
  onChange: (values: { title: string; description: string; organizationId: string }) => void;
  onCreate: (request: CreateDatasetRequest) => Promise<void>;
  onUpdate?: (request: { title: string; description: string }) => Promise<void>;
}

export function BasicMetadataStep({
  organizations,
  dataset,
  title,
  description,
  organizationId,
  readOnly,
  busy,
  error,
  onChange,
  onCreate,
  onUpdate
}: BasicMetadataStepProps) {
  const [fieldError, setFieldError] = useState<string | null>(null);
  const created = Boolean(dataset);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (readOnly) {
      return;
    }

    if (!organizationId || !title.trim() || !description.trim()) {
      setFieldError("Organization, title, and description are required.");
      return;
    }

    setFieldError(null);

    if (created && onUpdate) {
      await onUpdate({ title: title.trim(), description: description.trim() });
      return;
    }

    await onCreate({ organizationId, title: title.trim(), description: description.trim() });
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-5">
      <div>
        <label htmlFor="organizationId" className="text-sm font-medium text-ink">
          Organization
        </label>
        <select
          id="organizationId"
          value={organizationId}
          onChange={(event) => onChange({ title, description, organizationId: event.target.value })}
          disabled={created || readOnly || busy}
          className="mt-2 w-full border border-line bg-white px-3 py-2 text-sm text-ink disabled:bg-panel"
        >
          <option value="">Select organization</option>
          {organizations.map((organization) => (
            <option key={organization.id} value={organization.id}>
              {organization.name}
            </option>
          ))}
        </select>
      </div>

      <div>
        <label htmlFor="title" className="text-sm font-medium text-ink">
          Title
        </label>
        <input
          id="title"
          value={title}
          onChange={(event) => onChange({ title: event.target.value, description, organizationId })}
          disabled={readOnly || busy}
          className="mt-2 w-full border border-line px-3 py-2 text-sm text-ink disabled:bg-panel"
        />
      </div>

      <div>
        <label htmlFor="description" className="text-sm font-medium text-ink">
          Description
        </label>
        <textarea
          id="description"
          value={description}
          onChange={(event) => onChange({ title, description: event.target.value, organizationId })}
          disabled={readOnly || busy}
          rows={6}
          className="mt-2 w-full border border-line px-3 py-2 text-sm leading-6 text-ink disabled:bg-panel"
        />
      </div>

      <FormFieldError message={fieldError ?? error} />

      {!readOnly && (
        <button type="submit" disabled={busy} className="bg-ink px-4 py-2 text-sm font-medium text-white disabled:opacity-60">
          {busy ? "Saving..." : created ? "Save metadata" : "Create dataset"}
        </button>
      )}
    </form>
  );
}
