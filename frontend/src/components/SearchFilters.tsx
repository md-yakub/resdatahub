"use client";

import { useRouter, useSearchParams } from "next/navigation";
import type { FormEvent } from "react";
import type { SearchSort } from "@/lib/types";

const sortOptions: { value: SearchSort; label: string }[] = [
  { value: "NEWEST", label: "Newest" },
  { value: "OLDEST", label: "Oldest" },
  { value: "TITLE_ASC", label: "Title A-Z" },
  { value: "TITLE_DESC", label: "Title Z-A" }
];

export function SearchFilters() {
  const router = useRouter();
  const searchParams = useSearchParams();

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const formData = new FormData(event.currentTarget);
    const params = new URLSearchParams();

    for (const key of ["q", "organizationId", "keyword", "licenseCode", "sort"]) {
      const value = String(formData.get(key) ?? "").trim();
      if (value) {
        params.set(key, value);
      }
    }

    params.set("page", "0");
    router.push(`/datasets?${params.toString()}`);
  }

  return (
    <form onSubmit={submit} className="grid gap-3 border border-line bg-panel p-4 md:grid-cols-6">
      <label className="md:col-span-2">
        <span className="text-sm font-medium text-ink">Search</span>
        <input
          name="q"
          defaultValue={searchParams.get("q") ?? ""}
          className="focus-ring mt-1 w-full border border-line bg-white px-3 py-2 text-sm"
          placeholder="Title, creator, organization, keyword"
        />
      </label>
      <label>
        <span className="text-sm font-medium text-ink">Organization ID</span>
        <input
          name="organizationId"
          defaultValue={searchParams.get("organizationId") ?? ""}
          className="focus-ring mt-1 w-full border border-line bg-white px-3 py-2 text-sm"
        />
      </label>
      <label>
        <span className="text-sm font-medium text-ink">Keyword</span>
        <input
          name="keyword"
          defaultValue={searchParams.get("keyword") ?? ""}
          className="focus-ring mt-1 w-full border border-line bg-white px-3 py-2 text-sm"
        />
      </label>
      <label>
        <span className="text-sm font-medium text-ink">License</span>
        <input
          name="licenseCode"
          defaultValue={searchParams.get("licenseCode") ?? ""}
          className="focus-ring mt-1 w-full border border-line bg-white px-3 py-2 text-sm"
          placeholder="CC-BY-4.0"
        />
      </label>
      <label>
        <span className="text-sm font-medium text-ink">Sort</span>
        <select
          name="sort"
          defaultValue={searchParams.get("sort") ?? "NEWEST"}
          className="focus-ring mt-1 w-full border border-line bg-white px-3 py-2 text-sm"
        >
          {sortOptions.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </label>
      <div className="md:col-span-6">
        <button className="focus-ring bg-accent px-4 py-2 text-sm font-medium text-white" type="submit">
          Search datasets
        </button>
      </div>
    </form>
  );
}
