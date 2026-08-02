import { buildDownloadUrl } from "@/lib/api";
import { formatBytes, formatDate } from "@/lib/format";
import type { PublicFile } from "@/lib/types";

export function FileList({ files }: { files: PublicFile[] }) {
  return (
    <section className="border border-line bg-white p-5">
      <h2 className="text-lg font-semibold text-ink">Files</h2>
      <div className="mt-4 divide-y divide-line border border-line">
        {files.map((file) => (
          <div key={file.id} className="grid gap-4 p-4 md:grid-cols-[1fr_auto] md:items-start">
            <div>
              <h3 className="font-medium text-ink">{file.originalFilename}</h3>
              <dl className="mt-3 grid gap-2 text-sm text-muted md:grid-cols-2">
                <div>
                  <dt className="font-medium text-ink">Type</dt>
                  <dd>{file.contentType}</dd>
                </div>
                <div>
                  <dt className="font-medium text-ink">Size</dt>
                  <dd>{formatBytes(file.fileSize)}</dd>
                </div>
                <div>
                  <dt className="font-medium text-ink">Category</dt>
                  <dd>{file.category}</dd>
                </div>
                <div>
                  <dt className="font-medium text-ink">Added</dt>
                  <dd>{formatDate(file.createdAt)}</dd>
                </div>
              </dl>
              <p className="mt-3 break-all text-xs text-muted">SHA-256: {file.sha256}</p>
            </div>
            <a
              className="focus-ring border border-line px-3 py-2 text-center text-sm font-medium text-ink hover:border-accent hover:text-accent"
              href={buildDownloadUrl(file.downloadUrl)}
            >
              Download
            </a>
          </div>
        ))}
      </div>
    </section>
  );
}
