"use client";

import { useState } from "react";
import { BasicMetadataStep } from "./BasicMetadataStep";
import { CreatorsStep } from "./CreatorsStep";
import { DiscoveryRightsStep } from "./DiscoveryRightsStep";
import { FilesStep } from "./FilesStep";
import { ManagementSidebar } from "./ManagementSidebar";
import { ReviewPublishStep } from "./ReviewPublishStep";
import {
  addCreator,
  addKeyword,
  createDataset,
  deleteCreator,
  deleteFile,
  deleteKeyword,
  getCreators,
  getFiles,
  getKeywords,
  publishVersion,
  updateCreator,
  updateDatasetVersion,
  updateVersionLicense,
  uploadFile
} from "@/lib/api";
import type {
  CreateDatasetCreatorRequest,
  DatasetCreatorResponse,
  DatasetFileCategory,
  DatasetFileResponse,
  DatasetKeywordResponse,
  DatasetResponse,
  DatasetVersionResponse,
  LicenseResponse,
  OrganizationResponse
} from "@/lib/types";

interface DatasetWizardProps {
  organizations: OrganizationResponse[];
  licenses: LicenseResponse[];
  initialDataset?: DatasetResponse | null;
  initialVersion?: DatasetVersionResponse | null;
  initialCreators?: DatasetCreatorResponse[];
  initialKeywords?: DatasetKeywordResponse[];
  initialFiles?: DatasetFileResponse[];
}

export function DatasetWizard({
  organizations,
  licenses,
  initialDataset = null,
  initialVersion = null,
  initialCreators = [],
  initialKeywords = [],
  initialFiles = []
}: DatasetWizardProps) {
  const [step, setStep] = useState(initialDataset && initialVersion ? 1 : 0);
  const [dataset, setDataset] = useState<DatasetResponse | null>(initialDataset);
  const [version, setVersion] = useState<DatasetVersionResponse | null>(initialVersion);
  const [creators, setCreators] = useState(initialCreators);
  const [keywords, setKeywords] = useState(initialKeywords);
  const [files, setFiles] = useState(initialFiles);
  const [title, setTitle] = useState(initialVersion?.title ?? initialDataset?.latestVersion?.title ?? "");
  const [description, setDescription] = useState(initialVersion?.description ?? "");
  const [organizationId, setOrganizationId] = useState(initialDataset?.organization.id ?? "");
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const readOnly = version ? version.status !== "DRAFT" : false;
  const highestEnabledStep = dataset && version ? 4 : 0;

  async function runAction(action: () => Promise<void>, success?: string) {
    setBusy(true);
    setError(null);
    setMessage(null);

    try {
      await action();
      if (success) {
        setMessage(success);
      }
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "The request failed.");
    } finally {
      setBusy(false);
    }
  }

  async function refreshCollections(datasetId = dataset?.id, versionId = version?.id) {
    if (!datasetId || !versionId) {
      return;
    }

    const [nextCreators, nextKeywords, nextFiles] = await Promise.all([
      getCreators(datasetId, versionId),
      getKeywords(datasetId, versionId),
      getFiles(datasetId, versionId)
    ]);

    setCreators(nextCreators);
    setKeywords(nextKeywords);
    setFiles(nextFiles);
  }

  function requireIds() {
    if (!dataset || !version) {
      throw new Error("Create the dataset before continuing.");
    }

    return { datasetId: dataset.id, versionId: version.id };
  }

  return (
    <div className="grid gap-6 lg:grid-cols-[260px_1fr]">
      <ManagementSidebar currentStep={step} highestEnabledStep={highestEnabledStep} onSelectStep={setStep} />

      <section className="border border-line bg-white p-6">
        <div className="mb-6">
          <p className="text-sm font-medium text-accent">Development workspace</p>
          <h1 className="mt-2 text-2xl font-semibold text-ink">Dataset Management</h1>
          <p className="mt-2 text-sm leading-6 text-muted">
            Authentication is not implemented yet. Use this workspace only for local development.
          </p>
          {readOnly && <p className="mt-2 text-sm text-muted">This version is {version?.status} and cannot be edited.</p>}
          {message && <p className="mt-3 border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-800">{message}</p>}
          {error && <p className="mt-3 border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-800">{error}</p>}
        </div>

        {step === 0 && (
          <BasicMetadataStep
            organizations={organizations}
            dataset={dataset}
            title={title}
            description={description}
            organizationId={organizationId}
            readOnly={readOnly}
            busy={busy}
            error={null}
            onChange={(values) => {
              setTitle(values.title);
              setDescription(values.description);
              setOrganizationId(values.organizationId);
            }}
            onCreate={(request) =>
              runAction(async () => {
                const created = await createDataset(request);
                const versionId = created.latestVersion?.id;

                if (!versionId) {
                  throw new Error("Dataset was created but no draft version was returned.");
                }

                setDataset(created);
                setVersion({
                  id: versionId,
                  datasetId: created.id,
                  versionNumber: created.latestVersion?.versionNumber ?? "1.0",
                  title: created.latestVersion?.title ?? request.title,
                  description: request.description,
                  changeNote: null,
                  status: created.latestVersion?.status ?? "DRAFT",
                  license: null,
                  createdAt: created.createdAt,
                  updatedAt: created.updatedAt,
                  publishedAt: created.latestVersion?.publishedAt ?? null
                });
                await refreshCollections(created.id, versionId);
                setStep(1);
              }, "Dataset draft created.")
            }
            onUpdate={(request) =>
              runAction(async () => {
                const { datasetId, versionId } = requireIds();
                const updated = await updateDatasetVersion(datasetId, versionId, request);
                setVersion(updated);
              }, "Metadata saved.")
            }
          />
        )}

        {step === 1 && (
          <CreatorsStep
            creators={creators}
            readOnly={readOnly}
            busy={busy}
            error={null}
            onAdd={(request) =>
              runAction(async () => {
                const { datasetId, versionId } = requireIds();
                await addCreator(datasetId, versionId, request);
                await refreshCollections(datasetId, versionId);
              }, "Creator saved.")
            }
            onUpdate={(creatorId, request) =>
              runAction(async () => {
                const { datasetId, versionId } = requireIds();
                await updateCreator(datasetId, versionId, creatorId, request);
                await refreshCollections(datasetId, versionId);
              }, "Creator updated.")
            }
            onDelete={(creatorId) =>
              runAction(async () => {
                const { datasetId, versionId } = requireIds();
                await deleteCreator(datasetId, versionId, creatorId);
                await refreshCollections(datasetId, versionId);
              }, "Creator deleted.")
            }
          />
        )}

        {step === 2 && (
          <DiscoveryRightsStep
            keywords={keywords}
            licenses={licenses}
            selectedLicenseId={version?.license?.id}
            readOnly={readOnly}
            busy={busy}
            error={null}
            onAddKeyword={(value) =>
              runAction(async () => {
                const { datasetId, versionId } = requireIds();
                await addKeyword(datasetId, versionId, { value });
                await refreshCollections(datasetId, versionId);
              }, "Keyword added.")
            }
            onDeleteKeyword={(keywordId) =>
              runAction(async () => {
                const { datasetId, versionId } = requireIds();
                await deleteKeyword(datasetId, versionId, keywordId);
                await refreshCollections(datasetId, versionId);
              }, "Keyword deleted.")
            }
            onChangeLicense={(licenseId) =>
              runAction(async () => {
                const { datasetId, versionId } = requireIds();
                const updated = await updateVersionLicense(datasetId, versionId, licenseId);
                setVersion(updated);
              }, "License saved.")
            }
          />
        )}

        {step === 3 && (
          <FilesStep
            files={files}
            readOnly={readOnly}
            busy={busy}
            error={null}
            onUpload={(file, category: DatasetFileCategory) =>
              runAction(async () => {
                const { datasetId, versionId } = requireIds();
                await uploadFile(datasetId, versionId, file, category);
                await refreshCollections(datasetId, versionId);
              }, "File uploaded.")
            }
            onDelete={(fileId) =>
              runAction(async () => {
                const { datasetId, versionId } = requireIds();
                await deleteFile(datasetId, versionId, fileId);
                await refreshCollections(datasetId, versionId);
              }, "File deleted.")
            }
          />
        )}

        {step === 4 && (
          <ReviewPublishStep
            dataset={dataset}
            version={version}
            creators={creators}
            keywords={keywords}
            files={files}
            busy={busy}
            error={null}
            onPublish={async () => {
              setBusy(true);
              setError(null);
              setMessage(null);

              try {
                const { datasetId, versionId } = requireIds();
                const published = await publishVersion(datasetId, versionId);
                setVersion(published);
                setMessage("Dataset version published.");
                return published;
              } catch (caught) {
                setError(caught instanceof Error ? caught.message : "The publish request failed.");
                throw caught;
              } finally {
                setBusy(false);
              }
            }}
          />
        )}

        <div className="mt-8 flex justify-between border-t border-line pt-5">
          <button type="button" onClick={() => setStep(Math.max(0, step - 1))} disabled={step === 0} className="border border-line px-4 py-2 text-sm text-muted disabled:opacity-50">
            Back
          </button>
          <button type="button" onClick={() => setStep(Math.min(highestEnabledStep, step + 1))} disabled={step >= highestEnabledStep} className="border border-line px-4 py-2 text-sm text-muted disabled:opacity-50">
            Next
          </button>
        </div>
      </section>
    </div>
  );
}
