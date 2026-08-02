import Link from "next/link";

export default function DatasetNotFound() {
  return (
    <div className="mx-auto max-w-6xl px-6 py-10">
      <div className="border border-line bg-panel p-6">
        <h1 className="text-2xl font-semibold text-ink">Dataset not found</h1>
        <p className="mt-2 text-sm text-muted">This published dataset version is unavailable.</p>
        <Link className="mt-4 inline-block text-sm font-medium text-accent" href="/datasets">
          Back to dataset search
        </Link>
      </div>
    </div>
  );
}
