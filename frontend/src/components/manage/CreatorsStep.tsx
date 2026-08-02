"use client";

import type { FormEvent } from "react";
import { useState } from "react";
import { ConfirmationDialog } from "./ConfirmationDialog";
import { FormFieldError } from "./FormFieldError";
import type { CreateDatasetCreatorRequest, DatasetCreatorResponse } from "@/lib/types";

const emptyForm = { givenName: "", familyName: "", affiliation: "", orcid: "", position: 1 };

interface CreatorsStepProps {
  creators: DatasetCreatorResponse[];
  readOnly: boolean;
  busy: boolean;
  error?: string | null;
  onAdd: (request: CreateDatasetCreatorRequest) => Promise<void>;
  onUpdate: (creatorId: string, request: CreateDatasetCreatorRequest) => Promise<void>;
  onDelete: (creatorId: string) => Promise<void>;
}

export function CreatorsStep({ creators, readOnly, busy, error, onAdd, onUpdate, onDelete }: CreatorsStepProps) {
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [fieldError, setFieldError] = useState<string | null>(null);

  function startEdit(creator: DatasetCreatorResponse) {
    setEditingId(creator.id);
    setForm({
      givenName: creator.givenName,
      familyName: creator.familyName,
      affiliation: creator.affiliation ?? "",
      orcid: creator.orcid ?? "",
      position: creator.position
    });
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!form.givenName.trim() || !form.familyName.trim() || form.position < 1) {
      setFieldError("Given name, family name, and a positive position are required.");
      return;
    }

    const request = {
      givenName: form.givenName.trim(),
      familyName: form.familyName.trim(),
      affiliation: form.affiliation.trim() || undefined,
      orcid: form.orcid.trim() || undefined,
      position: form.position
    };

    setFieldError(null);

    if (editingId) {
      await onUpdate(editingId, request);
    } else {
      await onAdd(request);
    }

    setEditingId(null);
    setForm(emptyForm);
  }

  return (
    <div className="space-y-6">
      <div className="space-y-3">
        {creators.length === 0 && <p className="text-sm text-muted">No creators added yet.</p>}
        {creators
          .slice()
          .sort((a, b) => a.position - b.position)
          .map((creator) => (
            <div key={creator.id} className="flex flex-col gap-3 border border-line bg-panel p-4 md:flex-row md:items-center md:justify-between">
              <div className="text-sm">
                <p className="font-medium text-ink">
                  {creator.position}. {creator.givenName} {creator.familyName}
                </p>
                <p className="mt-1 text-muted">{creator.affiliation || "No affiliation"}</p>
                {creator.orcid && <p className="mt-1 text-xs text-muted">ORCID: {creator.orcid}</p>}
              </div>
              {!readOnly && (
                <div className="flex gap-2">
                  <button type="button" onClick={() => startEdit(creator)} className="border border-line px-3 py-2 text-sm text-muted hover:text-ink">
                    Edit
                  </button>
                  <button type="button" onClick={() => setDeleteId(creator.id)} className="border border-line px-3 py-2 text-sm text-red-700">
                    Delete
                  </button>
                </div>
              )}
            </div>
          ))}
      </div>

      {!readOnly && (
        <form onSubmit={handleSubmit} className="grid gap-4 border border-line bg-white p-4 md:grid-cols-2">
          <input value={form.givenName} onChange={(e) => setForm({ ...form, givenName: e.target.value })} placeholder="Given name" className="border border-line px-3 py-2 text-sm" disabled={busy} />
          <input value={form.familyName} onChange={(e) => setForm({ ...form, familyName: e.target.value })} placeholder="Family name" className="border border-line px-3 py-2 text-sm" disabled={busy} />
          <input value={form.affiliation} onChange={(e) => setForm({ ...form, affiliation: e.target.value })} placeholder="Affiliation" className="border border-line px-3 py-2 text-sm" disabled={busy} />
          <input value={form.orcid} onChange={(e) => setForm({ ...form, orcid: e.target.value })} placeholder="ORCID" className="border border-line px-3 py-2 text-sm" disabled={busy} />
          <input type="number" min={1} value={form.position} onChange={(e) => setForm({ ...form, position: Number(e.target.value) })} className="border border-line px-3 py-2 text-sm" disabled={busy} />
          <div className="flex gap-2">
            <button type="submit" disabled={busy} className="bg-ink px-4 py-2 text-sm font-medium text-white disabled:opacity-60">
              {editingId ? "Save creator" : "Add creator"}
            </button>
            {editingId && (
              <button type="button" onClick={() => { setEditingId(null); setForm(emptyForm); }} className="border border-line px-4 py-2 text-sm text-muted">
                Cancel
              </button>
            )}
          </div>
          <div className="md:col-span-2">
            <FormFieldError message={fieldError ?? error} />
          </div>
        </form>
      )}

      <ConfirmationDialog
        open={Boolean(deleteId)}
        title="Delete creator"
        message="This removes the creator from the draft version."
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
