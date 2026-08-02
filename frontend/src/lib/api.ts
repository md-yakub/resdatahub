import type {
  CatalogInfo,
  CatalogValidation,
  CitationFormat,
  CitationResponse,
  MetadataFormat,
  PublicDatasetResponse,
  PublicSearchResponse,
  SearchSort
} from "./types";

export interface SearchParams {
  q?: string;
  page?: number;
  size?: number;
  organizationId?: string;
  keyword?: string;
  licenseCode?: string;
  sort?: SearchSort;
}

export function getApiBaseUrl() {
  const baseUrl = process.env.NEXT_PUBLIC_API_BASE_URL;

  if (!baseUrl) {
    throw new Error("NEXT_PUBLIC_API_BASE_URL is not configured");
  }

  return baseUrl.replace(/\/+$/, "");
}

export function buildBackendUrl(path: string, params?: Record<string, string | number | undefined>) {
  const url = new URL(path, getApiBaseUrl());

  Object.entries(params ?? {}).forEach(([key, value]) => {
    if (value !== undefined && value !== "") {
      url.searchParams.set(key, String(value));
    }
  });

  return url.toString();
}

export function buildMetadataUrl(datasetId: string, versionId: string, format: MetadataFormat) {
  return buildBackendUrl(`/api/public/datasets/${datasetId}/versions/${versionId}/metadata`, { format });
}

export function buildDownloadUrl(downloadUrl: string) {
  if (downloadUrl.startsWith("http://") || downloadUrl.startsWith("https://")) {
    return downloadUrl;
  }

  return `${getApiBaseUrl()}${downloadUrl.startsWith("/") ? "" : "/"}${downloadUrl}`;
}

export async function getCatalogInfo() {
  return fetchJson<CatalogInfo>(buildBackendUrl("/api/public/catalog/info"));
}

export async function getCatalogValidation() {
  return fetchJson<CatalogValidation>(buildBackendUrl("/api/public/catalog/validation"));
}

export async function searchDatasets(params: SearchParams = {}) {
  return fetchJson<PublicSearchResponse>(
    buildBackendUrl("/api/public/search", {
      q: params.q,
      page: params.page ?? 0,
      size: params.size ?? 20,
      organizationId: params.organizationId,
      keyword: params.keyword,
      licenseCode: params.licenseCode,
      sort: params.sort ?? "NEWEST"
    })
  );
}

export async function getPublicDataset(datasetId: string, versionId: string) {
  return fetchJson<PublicDatasetResponse>(
    buildBackendUrl(`/api/public/datasets/${datasetId}/versions/${versionId}`)
  );
}

export async function getCitation(datasetId: string, versionId: string, format: CitationFormat) {
  return fetchJson<CitationResponse>(
    buildBackendUrl(`/api/public/datasets/${datasetId}/versions/${versionId}/citation`, { format })
  );
}

async function fetchJson<T>(url: string): Promise<T> {
  const response = await fetch(url, { cache: "no-store" });

  if (!response.ok) {
    let message = response.statusText;

    try {
      const body = await response.json();
      if (typeof body.message === "string") {
        message = body.message;
      }
    } catch {
      const body = await response.text();
      if (body) {
        message = body;
      }
    }

    throw new Error(`Request failed with status ${response.status}: ${message}`);
  }

  return response.json() as Promise<T>;
}
