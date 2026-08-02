"use client";

import { useRouter, useSearchParams } from "next/navigation";

export function Pagination({ page, totalPages }: { page: number; totalPages: number }) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const canGoBack = page > 0;
  const canGoForward = page + 1 < totalPages;

  function goTo(nextPage: number) {
    const params = new URLSearchParams(searchParams.toString());
    params.set("page", String(nextPage));
    router.push(`/datasets?${params.toString()}`);
  }

  if (totalPages <= 1) {
    return null;
  }

  return (
    <nav className="flex items-center justify-between border-t border-line pt-5 text-sm">
      <button
        className="focus-ring border border-line px-3 py-2 disabled:cursor-not-allowed disabled:opacity-40"
        disabled={!canGoBack}
        onClick={() => goTo(page - 1)}
        type="button"
      >
        Previous
      </button>
      <span className="text-muted">
        Page {page + 1} of {totalPages}
      </span>
      <button
        className="focus-ring border border-line px-3 py-2 disabled:cursor-not-allowed disabled:opacity-40"
        disabled={!canGoForward}
        onClick={() => goTo(page + 1)}
        type="button"
      >
        Next
      </button>
    </nav>
  );
}
