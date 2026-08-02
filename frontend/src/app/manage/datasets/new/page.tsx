import { DatasetWizard } from "@/components/manage/DatasetWizard";
import { ErrorMessage } from "@/components/ErrorMessage";
import { getLicenses, getOrganizations } from "@/lib/api";

export default async function NewManagedDatasetPage() {
  try {
    const [organizations, licenses] = await Promise.all([getOrganizations(), getLicenses()]);

    return (
      <div className="mx-auto max-w-6xl px-6 py-10">
        <DatasetWizard organizations={organizations} licenses={licenses} />
      </div>
    );
  } catch {
    return (
      <div className="mx-auto max-w-6xl px-6 py-10">
        <ErrorMessage message="Could not load organizations or licenses for the dataset wizard." />
      </div>
    );
  }
}
