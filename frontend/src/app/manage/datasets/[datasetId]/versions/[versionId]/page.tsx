import { notFound } from "next/navigation";
import { DatasetWizard } from "@/components/manage/DatasetWizard";
import { ErrorMessage } from "@/components/ErrorMessage";
import {
  getCreators,
  getDataset,
  getDatasetVersion,
  getFiles,
  getKeywords,
  getLicenses,
  getOrganizations
} from "@/lib/api";

interface ManagedDatasetVersionPageProps {
  params: Promise<{
    datasetId: string;
    versionId: string;
  }>;
}

export default async function ManagedDatasetVersionPage({ params }: ManagedDatasetVersionPageProps) {
  const { datasetId, versionId } = await params;

  try {
    const [organizations, licenses, dataset, version, creators, keywords, files] = await Promise.all([
      getOrganizations(),
      getLicenses(),
      getDataset(datasetId),
      getDatasetVersion(datasetId, versionId),
      getCreators(datasetId, versionId),
      getKeywords(datasetId, versionId),
      getFiles(datasetId, versionId)
    ]);

    return (
      <div className="mx-auto max-w-6xl px-6 py-10">
        <DatasetWizard
          organizations={organizations}
          licenses={licenses}
          initialDataset={dataset}
          initialVersion={version}
          initialCreators={creators}
          initialKeywords={keywords}
          initialFiles={files}
        />
      </div>
    );
  } catch (error) {
    if (error instanceof Error && error.message.includes("404")) {
      notFound();
    }

    return (
      <div className="mx-auto max-w-6xl px-6 py-10">
        <ErrorMessage message="Could not load this managed dataset version." />
      </div>
    );
  }
}
